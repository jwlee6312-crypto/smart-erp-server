package com.crmbank.erp.mobile.hpio;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crmbank.erp.mobile.ApiResponse;
import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.CodeDto;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.RetrofitClient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 [MHPIO360S] 모바일 생산일보
 * 웹 HPIO360S.vue 기반 100% 매칭
 * 1. 생산라인/생산공정 스피너 옵션 로드 (HP00_000S_STR L0/G0)
 * 2. 일일 생산 실적 현황 (HPIO_360S_STR 'S0')
 * 3. 자재 투입 현황 및 월 누계 (HPIO_360S_STR 'S1')
 */
public class MHPIO360S extends BaseActivity {

    private Spinner spLine, spProg;
    private TextView tvProYmd;
    private ListView lvProdList, lvMatList;

    private ProdAdapter prodAdapter;
    private MatAdapter matAdapter;

    private final List<Map<String, Object>> prodList = new ArrayList<>();
    private final List<Map<String, Object>> matList = new ArrayList<>();
    private final List<CodeDto> lineList = new ArrayList<>();
    private final List<CodeDto> progList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpio360s);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        loadLineOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("생산일보");

        spLine = findViewById(R.id.spLine);
        spProg = findViewById(R.id.spProg);
        tvProYmd = findViewById(R.id.tvProYmd);

        lvProdList = findViewById(R.id.lvProdList);
        lvMatList = findViewById(R.id.lvMatList);

        prodAdapter = new ProdAdapter();
        lvProdList.setAdapter(prodAdapter);

        matAdapter = new MatAdapter();
        lvMatList.setAdapter(matAdapter);

        // 오늘 날짜 기본값 세팅
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvProYmd.setText(today);
        tvProYmd.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> search());

        spLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < lineList.size()) {
                    loadProgOptions(lineList.get(position).codecd);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spProg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                search();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadLineOptions() {
        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "L0");
        p.put("cmpycd", cmpycd);
        p.put("gbncd", "Y");
        p.put("code", "");
        p.put("codenm", "");
        p.put("etcval", "");

        apiService.executeHp00Procedure("HP00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
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
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPIO360S.this, android.R.layout.simple_spinner_item, lineList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLine.setAdapter(adapter);

                    if (!lineList.isEmpty()) {
                        spLine.setSelection(0);
                        loadProgOptions(lineList.get(0).codecd);
                    }
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadProgOptions(String linecd) {
        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "G0");
        p.put("cmpycd", cmpycd);
        p.put("gbncd", linecd);
        p.put("code", "");
        p.put("codenm", "");
        p.put("etcval", "");

        apiService.executeHp00Procedure("HP00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    progList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "progcd");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "code");
                        dto.codenm = getStringVal(m, "prognm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "cdnm");
                        progList.add(dto);
                    }
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPIO360S.this, android.R.layout.simple_spinner_item, progList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spProg.setAdapter(adapter);

                    if (!progList.isEmpty()) {
                        spProg.setSelection(0);
                    }
                }
                search();
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) { search(); }
        });
    }

    private void search() {
        String selectedLine = "";
        if (spLine != null && spLine.getSelectedItem() != null) {
            selectedLine = ((CodeDto) spLine.getSelectedItem()).codecd;
        }

        String selectedProg = "";
        if (spProg != null && spProg.getSelectedItem() != null) {
            selectedProg = ((CodeDto) spProg.getSelectedItem()).codecd;
        }

        String proYmd = tvProYmd.getText().toString().replace("-", "").trim();

        // 1. 일일 생산 실적 현황 (HPIO_360S_STR 'S0')
        Map<String, Object> pProd = new HashMap<>();
        pProd.put("actkind", "S0");
        pProd.put("cmpycd", cmpycd);
        pProd.put("linecd", selectedLine);
        pProd.put("progcd", selectedProg);
        pProd.put("proymd", proYmd);

        apiService.executeHpioProcedure("HPIO_360S_STR", pProd).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prodList.clear();
                    prodList.addAll(response.body());
                    prodAdapter.notifyDataSetChanged();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });

        // 2. 자재 투입 현황 및 월 누계 (HPIO_360S_STR 'S1')
        Map<String, Object> pMat = new HashMap<>();
        pMat.put("actkind", "S1");
        pMat.put("cmpycd", cmpycd);
        pMat.put("linecd", selectedLine);
        pMat.put("progcd", selectedProg);
        pMat.put("proymd", proYmd);

        apiService.executeHpioProcedure("HPIO_360S_STR", pMat).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    matList.clear();
                    matList.addAll(response.body());
                    matAdapter.notifyDataSetChanged();
                    Toast.makeText(MHPIO360S.this, "생산일보가 조회되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void initializeForm() {
        prodList.clear();
        matList.clear();
        prodAdapter.notifyDataSetChanged();
        matAdapter.notifyDataSetChanged();
        search();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tvProYmd.getText().toString());
            if (d != null) cal.setTime(d);
        } catch (Exception ignored) {}

        new DatePickerDialog(this, (view, y, m, d) -> {
            tvProYmd.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
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

    @Override protected String getProgramTitle() { return "생산일보"; }
    @Override protected String getProgramId() { return "MHPIO360S"; }

    // 🔴 1. 생산 실적 어댑터
    private class ProdAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return prodList.size(); }
        @Override public Object getItem(int p) { return prodList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProdViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPIO360S.this).inflate(R.layout.item_mhpio360s_prod, parent, false);
                holder = new ProdViewHolder();
                holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvProdName = convertView.findViewById(R.id.tvProdName);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvPrdQty = convertView.findViewById(R.id.tvPrdQty);
                holder.tvGodQty = convertView.findViewById(R.id.tvGodQty);
                holder.tvPrdQtyM = convertView.findViewById(R.id.tvPrdQtyM);
                convertView.setTag(holder);
            } else {
                holder = (ProdViewHolder) convertView.getTag();
            }

            Map<String, Object> item = prodList.get(position);

            holder.tvItemCode.setText(getStringVal(item, "itemcd"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvProdName.setText(getStringVal(item, "prodnm"));
            holder.tvItSize.setText(String.format("%s / %s", getStringVal(item, "itsize"), getStringVal(item, "unit")));

            double prdQty = getDoubleVal(item, "prdqty");
            double godQty = getDoubleVal(item, "godqty");
            double prdQtyM = getDoubleVal(item, "prdqty_m");

            holder.tvPrdQty.setText("생산: " + df.format(prdQty));
            holder.tvGodQty.setText("양품: " + df.format(godQty));
            holder.tvPrdQtyM.setText("월누계: " + df.format(prdQtyM));

            return convertView;
        }
    }

    static class ProdViewHolder {
        TextView tvItemCode, tvItemName, tvProdName, tvItSize, tvPrdQty, tvGodQty, tvPrdQtyM;
    }

    // 🟢 2. 자재 투입 어댑터
    private class MatAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return matList.size(); }
        @Override public Object getItem(int p) { return matList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MatViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPIO360S.this).inflate(R.layout.item_mhpio360s_mat, parent, false);
                holder = new MatViewHolder();
                holder.tvMItemCode = convertView.findViewById(R.id.tvMItemCode);
                holder.tvMItemName = convertView.findViewById(R.id.tvMItemName);
                holder.tvParentItemName = convertView.findViewById(R.id.tvParentItemName);
                holder.tvMItSize = convertView.findViewById(R.id.tvMItSize);
                holder.tvInQty = convertView.findViewById(R.id.tvInQty);
                holder.tvInQtyM = convertView.findViewById(R.id.tvInQtyM);
                convertView.setTag(holder);
            } else {
                holder = (MatViewHolder) convertView.getTag();
            }

            Map<String, Object> item = matList.get(position);

            holder.tvMItemCode.setText(getStringVal(item, "mitemcd"));
            holder.tvMItemName.setText(getStringVal(item, "mitemnm"));
            holder.tvParentItemName.setText("[" + getStringVal(item, "itemnm") + "]");
            holder.tvMItSize.setText(String.format("%s / %s", getStringVal(item, "mitsize"), getStringVal(item, "munit")));

            double inQty = getDoubleVal(item, "inqty");
            double inQtyM = getDoubleVal(item, "inqty_m");

            holder.tvInQty.setText("투입: " + df.format(inQty));
            holder.tvInQtyM.setText("월누계: " + df.format(inQtyM));

            return convertView;
        }
    }

    static class MatViewHolder {
        TextView tvMItemCode, tvMItemName, tvParentItemName, tvMItSize, tvInQty, tvInQtyM;
    }
}
