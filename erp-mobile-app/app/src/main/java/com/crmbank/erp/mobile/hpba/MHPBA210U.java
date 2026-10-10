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
 * 🚀 [MHPBA210U] 모바일 표준 BOM (조회 전용)
 * 웹 HPBA210U.vue 기반 100% 매칭 (ProdItemHelp gbncd: A 제품/반제품 전용 적용)
 * 1. 생산라인 및 재고자산 옵션 선택, 제품 선택 팝업 (proditem gbncd: A)
 * 2. 제품별 생산 공정 리스트 조회 (HP00_000S_STR 'G1')
 * 3. 공정 선택 시 투입 자재 명세(BOM) 상세 조회 (HPBA_210U_STR 'S0')
 */
public class MHPBA210U extends BaseActivity {

    private Spinner spLine, spAstKind;
    private EditText etItemNm;
    private TextView tvSelectedProg;
    private ListView lvProcList, lvBomList;

    private ProcAdapter procAdapter;
    private BomAdapter bomAdapter;

    private final List<Map<String, Object>> procList = new ArrayList<>();
    private final List<Map<String, Object>> bomList = new ArrayList<>();
    private final List<CodeDto> lineList = new ArrayList<>();
    private final List<CodeDto> astKindList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";
    private String selectedItemCd = "";
    private String selectedItemNm = "";
    private String selectedProgCd = "";
    private String selectedProgNm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpba210u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        loadOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("표준 BOM ");

        spLine = findViewById(R.id.spLine);
        spAstKind = findViewById(R.id.spAstKind);
        etItemNm = findViewById(R.id.etItemNm);
        tvSelectedProg = findViewById(R.id.tvSelectedProg);

        lvProcList = findViewById(R.id.lvProcList);
        lvBomList = findViewById(R.id.lvBomList);

        procAdapter = new ProcAdapter();
        lvProcList.setAdapter(procAdapter);

        bomAdapter = new BomAdapter();
        lvBomList.setAdapter(bomAdapter);

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> searchProcesses());
        findViewById(R.id.btnSearchProd).setOnClickListener(v -> openProdItemPopup());
        etItemNm.setOnClickListener(v -> openProdItemPopup());

        lvProcList.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> prog = procList.get(position);
            selectedProgCd = getStringVal(prog, "progcd");
            if (selectedProgCd.isEmpty()) selectedProgCd = getStringVal(prog, "code");

            selectedProgNm = getStringVal(prog, "prognm");
            if (selectedProgNm.isEmpty()) selectedProgNm = getStringVal(prog, "cdnm");

            if (tvSelectedProg != null) {
                tvSelectedProg.setText(String.format("[%s] %s", selectedProgCd, selectedProgNm));
            }
            fetchBomDetails(selectedProgCd);
        });

        spLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchProcesses();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spAstKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchProcesses();
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
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPBA210U.this, android.R.layout.simple_spinner_item, lineList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLine.setAdapter(adapter);

                    if (!lineList.isEmpty()) {
                        spLine.setSelection(0);
                    }
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void openProdItemPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_customer_search, null);
        builder.setTitle("생산품목 선택 (ProdItemHelp)").setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.etSearchQuery);
        RecyclerView rv = dialogView.findViewById(R.id.rvPopupList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Map<String, Object>> list = new ArrayList<>();
        AlertDialog dialog = builder.create();

        PopupAdapter popupAdapter = new PopupAdapter(list, "ITEM", item -> {
            selectedItemCd = getStringVal(item, "itemcd");
            if (selectedItemCd.isEmpty()) selectedItemCd = getStringVal(item, "code");

            selectedItemNm = getStringVal(item, "itemnm");
            if (selectedItemNm.isEmpty()) selectedItemNm = getStringVal(item, "cdnm");

            etItemNm.setText(selectedItemNm);
            searchProcesses();
            dialog.dismiss();
        });
        rv.setAdapter(popupAdapter);

        dialogView.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Map<String, Object> p = new HashMap<>();
            p.put("gubun", "I1");
            p.put("gbncd", "A"); // 🚀 ProdItemHelp 생산품목(제품 200 / 반제품 210 전용) 구분코드 'A' 100% 동일 적용!
            p.put("cmpycd", cmpycd);
            p.put("codenm", etSearch.getText().toString().trim());
            p.put("code", "");
            p.put("etcval", "");

            apiService.executeHp00Procedure("HP00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> c, @NonNull Response<List<Map<String, Object>>> r) {
                    if (r.isSuccessful() && r.body() != null) { list.clear(); list.addAll(r.body()); popupAdapter.notifyDataSetChanged(); }
                }
                @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> c, @NonNull Throwable t) {}
            });
        });
        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        dialogView.findViewById(R.id.btnSearch).performClick();
    }

    private void searchProcesses() {
        if (selectedItemCd.isEmpty()) {
            return;
        }

        String selectedLine = "010";
        if (spLine != null && spLine.getSelectedItem() != null) {
            selectedLine = ((CodeDto) spLine.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "G1");
        p.put("cmpycd", cmpycd);
        p.put("gbncd", selectedLine);
        p.put("code", selectedItemCd);
        p.put("codenm", "");
        p.put("etcval", "");

        apiService.executeHp00Procedure("HP00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procList.clear();
                    procList.addAll(response.body());
                    procAdapter.notifyDataSetChanged();

                    bomList.clear();
                    bomAdapter.notifyDataSetChanged();
                    selectedProgCd = "";
                    selectedProgNm = "";
                    if (tvSelectedProg != null) tvSelectedProg.setText("공정을 선택하세요");

                    Toast.makeText(MHPBA210U.this, "생산 공정 조회 완료 (" + procList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPBA210U.this, "조회된 공정이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPBA210U.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBomDetails(String progcd) {
        if (selectedItemCd.isEmpty() || progcd == null || progcd.isEmpty()) return;

        String selectedLine = "010";
        if (spLine != null && spLine.getSelectedItem() != null) {
            selectedLine = ((CodeDto) spLine.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("actkind", "S0");
        p.put("cmpycd", cmpycd);
        p.put("itemcd", selectedItemCd);
        p.put("linecd", selectedLine);
        p.put("progcd", progcd);
        p.put("inqty", 0);
        p.put("losrate", 0);

        apiService.executeHpbaProcedure("HPBA_210U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bomList.clear();
                    for (Map<String, Object> item : response.body()) {
                        String mitem = getStringVal(item, "mitemcd");
                        if (mitem.isEmpty()) mitem = getStringVal(item, "code");
                        if (!mitem.isEmpty()) bomList.add(item);
                    }
                    bomAdapter.notifyDataSetChanged();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void initializeForm() {
        selectedItemCd = "";
        selectedItemNm = "";
        selectedProgCd = "";
        selectedProgNm = "";
        etItemNm.setText("");
        procList.clear();
        bomList.clear();
        procAdapter.notifyDataSetChanged();
        bomAdapter.notifyDataSetChanged();
        if (tvSelectedProg != null) tvSelectedProg.setText("공정을 선택하세요");
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

    @Override protected String getProgramTitle() { return "표준 BOM"; }
    @Override protected String getProgramId() { return "MHPBA210U"; }

    private class ProcAdapter extends BaseAdapter {
        @Override public int getCount() { return procList.size(); }
        @Override public Object getItem(int p) { return procList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProcViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPBA210U.this).inflate(R.layout.item_mhpba210u_proc, parent, false);
                holder = new ProcViewHolder();
                holder.tvProgCd = convertView.findViewById(R.id.tvProgCd);
                holder.tvProgNm = convertView.findViewById(R.id.tvProgNm);
                convertView.setTag(holder);
            } else {
                holder = (ProcViewHolder) convertView.getTag();
            }

            Map<String, Object> item = procList.get(position);

            String progCd = getStringVal(item, "progcd");
            if (progCd.isEmpty()) progCd = getStringVal(item, "code");
            holder.tvProgCd.setText(progCd);

            String progNm = getStringVal(item, "prognm");
            if (progNm.isEmpty()) progNm = getStringVal(item, "cdnm");
            holder.tvProgNm.setText(progNm);

            return convertView;
        }
    }

    static class ProcViewHolder {
        TextView tvProgCd, tvProgNm;
    }

    private class BomAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,##0.0");

        @Override public int getCount() { return bomList.size(); }
        @Override public Object getItem(int p) { return bomList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BomViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPBA210U.this).inflate(R.layout.item_mhpba210u_bom, parent, false);
                holder = new BomViewHolder();
                holder.tvMItemCode = convertView.findViewById(R.id.tvMItemCode);
                holder.tvMItemName = convertView.findViewById(R.id.tvMItemName);
                holder.tvBProgNm = convertView.findViewById(R.id.tvBProgNm);
                holder.tvMItSize = convertView.findViewById(R.id.tvMItSize);
                holder.tvInQty = convertView.findViewById(R.id.tvInQty);
                holder.tvLosRate = convertView.findViewById(R.id.tvLosRate);
                convertView.setTag(holder);
            } else {
                holder = (BomViewHolder) convertView.getTag();
            }

            Map<String, Object> item = bomList.get(position);

            String code = getStringVal(item, "mitemcd");
            if (code.isEmpty()) code = getStringVal(item, "code");
            holder.tvMItemCode.setText(code);

            String name = getStringVal(item, "mitemnm");
            if (name.isEmpty()) name = getStringVal(item, "cdnm");
            holder.tvMItemName.setText(name);

            String bProgNm = getStringVal(item, "bprognm");
            if (bProgNm.isEmpty()) bProgNm = getStringVal(item, "befprognm");
            if (bProgNm.isEmpty()) bProgNm = "-";
            holder.tvBProgNm.setText("이전: " + bProgNm);

            holder.tvMItSize.setText(String.format("%s / %s", getStringVal(item, "mitsize"), getStringVal(item, "munit")));

            double inQty = getDoubleVal(item, "inqty");
            double losRate = getDoubleVal(item, "losrate");

            holder.tvInQty.setText("소요량: " + df.format(inQty));
            holder.tvLosRate.setText(String.format("LOSS: %.0f%%", losRate));

            return convertView;
        }
    }

    static class BomViewHolder {
        TextView tvMItemCode, tvMItemName, tvBProgNm, tvMItSize, tvInQty, tvLosRate;
    }
}
