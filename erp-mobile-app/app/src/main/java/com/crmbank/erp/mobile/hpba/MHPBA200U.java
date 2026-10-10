package com.crmbank.erp.mobile.hpba;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.CodeDto;
import com.crmbank.erp.mobile.PopupAdapter;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.RetrofitClient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 [MHPBA200U] 모바일 품목별 표준공정도 (조회 전용)
 * 웹 HPBA200U.vue 기반 100% 매칭 (ProdItemHelp 제품/반제품 gbncd: A 적용)
 * 1. 생산라인 및 재고자산 옵션 선택, 제품 선택 팝업 (proditem gbncd: A)
 * 2. 라인/자산별 대상 품목 리스트 조회 (HPBA_200U_STR 'S1')
 * 3. 품목 선택 시 적용되는 표준 공정 및 생산능력 조회 (HPBA_200U_STR 'S0')
 */
public class MHPBA200U extends BaseActivity {

    private Spinner spLine, spAstKind;
    private TextView tvSelectedItem;
    private ListView lvItemList, lvProcList;

    private ItemAdapter itemAdapter;
    private ProcAdapter procAdapter;

    private final List<Map<String, Object>> itemList = new ArrayList<>();
    private final List<Map<String, Object>> procList = new ArrayList<>();
    private final List<CodeDto> lineList = new ArrayList<>();
    private final List<CodeDto> astKindList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";
    private String selectedItemCd = "";
    private String selectedItemNm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpba200u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        loadOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("품목별 표준공정도");

        spLine = findViewById(R.id.spLine);
        spAstKind = findViewById(R.id.spAstKind);
        tvSelectedItem = findViewById(R.id.tvSelectedItem);

        lvItemList = findViewById(R.id.lvItemList);
        lvProcList = findViewById(R.id.lvProcList);

        itemAdapter = new ItemAdapter();
        lvItemList.setAdapter(itemAdapter);

        procAdapter = new ProcAdapter();
        lvProcList.setAdapter(procAdapter);

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> searchItems());

        lvItemList.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> item = itemList.get(position);
            selectedItemCd = getStringVal(item, "itemcd");
            if (selectedItemCd.isEmpty()) selectedItemCd = getStringVal(item, "code");

            selectedItemNm = getStringVal(item, "itemnm");
            if (selectedItemNm.isEmpty()) selectedItemNm = getStringVal(item, "cdnm");

            if (tvSelectedItem != null) {
                tvSelectedItem.setText(String.format("[%s] %s", selectedItemCd, selectedItemNm));
            }
            fetchProcesses(selectedItemCd);
        });

        spLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchItems();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spAstKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchItems();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadOptions() {
        // 1. 재고자산 옵션 (200: 제품, 210: 반제품)
        astKindList.clear();
        CodeDto p1 = new CodeDto(); p1.codecd = "200"; p1.codenm = "제품"; astKindList.add(p1);
        CodeDto p2 = new CodeDto(); p2.codecd = "210"; p2.codenm = "반제품"; astKindList.add(p2);

        ArrayAdapter<CodeDto> astAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, astKindList);
        astAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAstKind.setAdapter(astAdapter);

        // 2. 생산라인 옵션 로드 (HP00_000S_STR L0)
        Map<String, Object> pLine = new HashMap<>();
        pLine.put("gubun", "L0");
        pLine.put("cmpycd", cmpycd);
        pLine.put("gbncd", "Y");
        pLine.put("code", "");
        pLine.put("codenm", "");
        pLine.put("etcval", "");

        apiService.executeHp00Procedure("HP00_000S_STR", pLine).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lineList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "linecd");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "code");
                        dto.codenm = getStringVal(m, "linenm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "cdnm");
                        lineList.add(dto);
                    }
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPBA200U.this, android.R.layout.simple_spinner_item, lineList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLine.setAdapter(adapter);

                    if (!lineList.isEmpty()) {
                        spLine.setSelection(0);
                    }
                }
                searchItems();
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) { searchItems(); }
        });
    }

    private void searchItems() {
        String selectedLine = "010";
        if (spLine != null && spLine.getSelectedItem() != null) {
            selectedLine = ((CodeDto) spLine.getSelectedItem()).codecd;
        }

        String selectedAst = "200";
        if (spAstKind != null && spAstKind.getSelectedItem() != null) {
            selectedAst = ((CodeDto) spAstKind.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("actkind", "S1");
        p.put("cmpycd", cmpycd);
        p.put("itemcd", "");
        p.put("linecd", selectedLine);
        p.put("astkind", selectedAst);
        p.put("progcd", "");
        p.put("itsize", "");
        p.put("unit", "");
        p.put("progord", 0);
        p.put("capahh", 0.0);
        p.put("gadrate", 0.0);
        p.put("pqtytt", 0.0);
        p.put("pqtydd", 0.0);
        p.put("gadtmdd", 0.0);
        p.put("jungrate", 0.0);
        p.put("stdworkhh", 0.0);
        p.put("updemp", "SYSTEM");

        apiService.executeHpbaProcedure("HPBA_200U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    itemAdapter.notifyDataSetChanged();

                    procList.clear();
                    procAdapter.notifyDataSetChanged();
                    selectedItemCd = "";
                    selectedItemNm = "";
                    if (tvSelectedItem != null) tvSelectedItem.setText("품목을 선택하세요");

                    Toast.makeText(MHPBA200U.this, "품목 조회 완료 (" + itemList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPBA200U.this, "조회된 품목이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPBA200U.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProcesses(String itemcd) {
        if (itemcd == null || itemcd.isEmpty()) return;

        String selectedLine = "010";
        if (spLine != null && spLine.getSelectedItem() != null) {
            selectedLine = ((CodeDto) spLine.getSelectedItem()).codecd;
        }

        String selectedAst = "200";
        if (spAstKind != null && spAstKind.getSelectedItem() != null) {
            selectedAst = ((CodeDto) spAstKind.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("actkind", "S0");
        p.put("cmpycd", cmpycd);
        p.put("itemcd", itemcd);
        p.put("linecd", selectedLine);
        p.put("astkind", selectedAst);
        p.put("progcd", "");
        p.put("itsize", "");
        p.put("unit", "");
        p.put("progord", 0);
        p.put("capahh", 0.0);
        p.put("gadrate", 0.0);
        p.put("pqtytt", 0.0);
        p.put("pqtydd", 0.0);
        p.put("gadtmdd", 0.0);
        p.put("jungrate", 0.0);
        p.put("stdworkhh", 0.0);
        p.put("updemp", "SYSTEM");

        apiService.executeHpbaProcedure("HPBA_200U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procList.clear();
                    for (Map<String, Object> row : response.body()) {
                        String useyn = getStringVal(row, "useyn");
                        if ("Y".equalsIgnoreCase(useyn) || row.containsKey("progcd") || row.containsKey("PROGCD")) {
                            procList.add(row);
                        }
                    }
                    procAdapter.notifyDataSetChanged();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void initializeForm() {
        itemList.clear();
        procList.clear();
        itemAdapter.notifyDataSetChanged();
        procAdapter.notifyDataSetChanged();
        selectedItemCd = "";
        selectedItemNm = "";
        if (tvSelectedItem != null) tvSelectedItem.setText("품목을 선택하세요");
        searchItems();
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(Locale.ROOT));
        return val != null ? String.valueOf(val).trim() : "";
    }

    private double getDoubleVal(Map<String, Object> map, String key) {
        try { return Double.parseDouble(getStringVal(map, key).replace(",", "")); } catch (Exception e) { return 0.0; }
    }

    @Override protected String getProgramTitle() { return "품목별 표준공정도"; }
    @Override protected String getProgramId() { return "MHPBA200U"; }

    private class ItemAdapter extends BaseAdapter {
        @Override public int getCount() { return itemList.size(); }
        @Override public Object getItem(int p) { return itemList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPBA200U.this).inflate(R.layout.item_mhpba200u_item, parent, false);
                holder = new ItemViewHolder();
                holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvUnit = convertView.findViewById(R.id.tvUnit);
                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }

            Map<String, Object> item = itemList.get(position);

            String cd = getStringVal(item, "itemcd");
            if (cd.isEmpty()) cd = getStringVal(item, "code");
            holder.tvItemCode.setText(cd);

            String nm = getStringVal(item, "itemnm");
            if (nm.isEmpty()) nm = getStringVal(item, "cdnm");
            holder.tvItemName.setText(nm);

            holder.tvUnit.setText(getStringVal(item, "unit"));

            return convertView;
        }
    }

    static class ItemViewHolder {
        TextView tvItemCode, tvItemName, tvUnit;
    }

    private class ProcAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");
        private final DecimalFormat dfDec = new DecimalFormat("#,##0.0");

        @Override public int getCount() { return procList.size(); }
        @Override public Object getItem(int p) { return procList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProcViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPBA200U.this).inflate(R.layout.item_mhpba200u_proc, parent, false);
                holder = new ProcViewHolder();
                holder.tvProgCd = convertView.findViewById(R.id.tvProgCd);
                holder.tvProgNm = convertView.findViewById(R.id.tvProgNm);
                holder.tvDspOrd = convertView.findViewById(R.id.tvDspOrd);
                holder.tvRates = convertView.findViewById(R.id.tvRates);
                holder.tvTimes = convertView.findViewById(R.id.tvTimes);
                holder.tvPQtyDd = convertView.findViewById(R.id.tvPQtyDd);
                convertView.setTag(holder);
            } else {
                holder = (ProcViewHolder) convertView.getTag();
            }

            Map<String, Object> item = procList.get(position);

            String progCd = getStringVal(item, "progcd");
            if (progCd.isEmpty()) progCd = getStringVal(item, "PROGCD");
            holder.tvProgCd.setText(progCd);

            String progNm = getStringVal(item, "prognm");
            if (progNm.isEmpty()) progNm = getStringVal(item, "PROGNM");
            holder.tvProgNm.setText(progNm);

            double dspOrd = getDoubleVal(item, "dspord");
            if (dspOrd == 0) dspOrd = getDoubleVal(item, "progord");
            holder.tvDspOrd.setText("순서: " + (int) dspOrd);

            double gadRate = getDoubleVal(item, "gadrate");
            double jungRate = getDoubleVal(item, "jungrate");
            holder.tvRates.setText(String.format("가동 %.0f%% / 양품 %.0f%%", gadRate, jungRate));

            double capahh = getDoubleVal(item, "capahh");
            if (capahh > 0) capahh = Math.round(capahh * 60.0 * 10.0) / 10.0;
            double gadTmDd = getDoubleVal(item, "gadtmdd");
            holder.tvTimes.setText(String.format("소요: %s분 / 일가동: %.0fh", dfDec.format(capahh), gadTmDd));

            double pQtyDd = getDoubleVal(item, "pqtydd");
            holder.tvPQtyDd.setText("일생산: " + df.format(pQtyDd));

            return convertView;
        }
    }

    static class ProcViewHolder {
        TextView tvProgCd, tvProgNm, tvDspOrd, tvRates, tvTimes, tvPQtyDd;
    }
}
