package com.crmbank.erp.mobile.hpio;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
 * 🚀 [MHPIO720S] 모바일 창고별현재고
 * 웹 HPIO720S.vue 기반 100% 매칭
 * 1. 창고 (W0) & 재고자산 (J0/100) 옵션 로드
 * 2. 기준일자별 창고/자산 실시간 재고 현황 조회 (HPIO_720S_STR)
 */
public class MHPIO720S extends BaseActivity {

    private Spinner spWarehouse, spAstKind;
    private TextView tvYmd, tvTotalCount;
    private ListView lvInventoryList;
    private InventoryAdapter adapter;

    private final List<Map<String, Object>> inventoryList = new ArrayList<>();
    private final List<CodeDto> whList = new ArrayList<>();
    private final List<CodeDto> astList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpio720s);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        loadOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("창고별현재고");

        spWarehouse = findViewById(R.id.spWarehouse);
        spAstKind = findViewById(R.id.spAstKind);

        tvYmd = findViewById(R.id.tvYmd);
        tvTotalCount = findViewById(R.id.tvTotalCount);

        lvInventoryList = findViewById(R.id.lvInventoryList);
        adapter = new InventoryAdapter();
        lvInventoryList.setAdapter(adapter);

        // 오늘 날짜 기본값 세팅
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvYmd.setText(today);
        tvYmd.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> search());
    }

    private void loadOptions() {
        // 1. 창고 옵션 로드 (HS00_000S_STR W0)
        Map<String, Object> pWh = new HashMap<>();
        pWh.put("gubun", "W0");
        pWh.put("cmpycd", cmpycd);

        apiService.executeHs00Procedure("HS00_000S_STR", pWh).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    whList.clear();
                    CodeDto all = new CodeDto(); all.codecd = "000"; all.codenm = "전체창고"; whList.add(all);
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "whcd");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "code");
                        dto.codenm = getStringVal(m, "whnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "cdnm");
                        whList.add(dto);
                    }
                    ArrayAdapter<CodeDto> whAdapter = new ArrayAdapter<>(MHPIO720S.this, android.R.layout.simple_spinner_item, whList);
                    whAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWarehouse.setAdapter(whAdapter);
                    spWarehouse.setSelection(0);
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });

        // 2. 재고자산 옵션 로드 (HP00_000S_STR J0/100)
        Map<String, Object> pAst = new HashMap<>();
        pAst.put("gubun", "J0");
        pAst.put("gbncd", "100");
        pAst.put("cmpycd", cmpycd);

        apiService.executeHp00Procedure("HP00_000S_STR", pAst).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    astList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "code");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "codecd");
                        dto.codenm = getStringVal(m, "cdnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "codenm");
                        astList.add(dto);
                    }
                    ArrayAdapter<CodeDto> astAdapter = new ArrayAdapter<>(MHPIO720S.this, android.R.layout.simple_spinner_item, astList);
                    astAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spAstKind.setAdapter(astAdapter);

                    if (!astList.isEmpty()) spAstKind.setSelection(0);
                }
                search();
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) { search(); }
        });
    }

    private void search() {
        String selectedWh = "000";
        if (spWarehouse != null && spWarehouse.getSelectedItem() != null) {
            selectedWh = ((CodeDto) spWarehouse.getSelectedItem()).codecd;
        }

        String selectedAst = "100";
        if (spAstKind != null && spAstKind.getSelectedItem() != null) {
            selectedAst = ((CodeDto) spAstKind.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("cmpycd", cmpycd);
        p.put("ymd", tvYmd.getText().toString().replace("-", "").trim());
        p.put("whcd", selectedWh);
        p.put("astkind", selectedAst);

        apiService.executeHpioProcedure("HPIO_720S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    inventoryList.clear();
                    inventoryList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (tvTotalCount != null) {
                        tvTotalCount.setText("Total: " + inventoryList.size() + "건");
                    }
                    Toast.makeText(MHPIO720S.this, "조회되었습니다. (" + inventoryList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPIO720S.this, "조회된 내역이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPIO720S.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        inventoryList.clear();
        adapter.notifyDataSetChanged();
        if (tvTotalCount != null) tvTotalCount.setText("Total: 0건");
        search();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tvYmd.getText().toString());
            if (d != null) cal.setTime(d);
        } catch (Exception ignored) {}

        new DatePickerDialog(this, (view, y, m, d) -> {
            tvYmd.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d));
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

    @Override protected String getProgramTitle() { return "창고별현재고"; }
    @Override protected String getProgramId() { return "MHPIO720S"; }

    private class InventoryAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return inventoryList.size(); }
        @Override public Object getItem(int p) { return inventoryList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPIO720S.this).inflate(R.layout.item_mhpio720s, parent, false);
                holder = new ViewHolder();
                holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvUnit = convertView.findViewById(R.id.tvUnit);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvStock = convertView.findViewById(R.id.tvStock);
                holder.tvStkQty = convertView.findViewById(R.id.tvStkQty);
                holder.tvDiff = convertView.findViewById(R.id.tvDiff);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> item = inventoryList.get(position);

            holder.tvItemCode.setText(getStringVal(item, "itemcd"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvUnit.setText(getStringVal(item, "unit"));
            holder.tvItSize.setText(getStringVal(item, "itsize"));

            double stock = getDoubleVal(item, "stock");
            double stkQty = getDoubleVal(item, "stkqty");
            double diff = stkQty - stock;

            holder.tvStock.setText("적정: " + df.format(stock));
            holder.tvStkQty.setText("현재고: " + df.format(stkQty));
            holder.tvDiff.setText("과부족: " + df.format(diff));

            if (stkQty < stock) {
                holder.tvStkQty.setTextColor(Color.RED);
            } else {
                holder.tvStkQty.setTextColor(Color.parseColor("#2E7D32"));
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvItemCode, tvItemName, tvUnit, tvItSize, tvStock, tvStkQty, tvDiff;
    }
}
