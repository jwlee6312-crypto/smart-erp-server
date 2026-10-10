package com.crmbank.erp.mobile.hpio;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
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
 * 🚀 [MHPIO420S] 모바일 제품입고현황
 * 웹 HPIO420S.vue 기반 100% 매칭
 * 1. 입고창고 스피너 옵션 로드 (HS00_000S_STR W0)
 * 2. 입고일자 기간 선택 (tvStartDate ~ tvEndDate)
 * 3. 창고별 제품 입고 집계 현황 조회 (HPIO_420S_STR)
 */
public class MHPIO420S extends BaseActivity {

    private Spinner spWarehouse;
    private TextView tvStartDate, tvEndDate, tvTotalCount;
    private ListView lvStatus;
    private StatusAdapter adapter;

    private final List<Map<String, Object>> statusList = new ArrayList<>();
    private final List<CodeDto> whList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpio420s);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        loadWhOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("제품입고현황");

        spWarehouse = findViewById(R.id.spWarehouse);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvTotalCount = findViewById(R.id.tvTotalCount);

        lvStatus = findViewById(R.id.lvStatus);
        adapter = new StatusAdapter();
        lvStatus.setAdapter(adapter);

        // 기본 날짜 세팅 (당월 1일 ~ 오늘)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvEndDate.setText(sdf.format(cal.getTime()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        tvStartDate.setText(sdf.format(cal.getTime()));

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> search());
    }

    private void loadWhOptions() {
        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "W0");
        p.put("cmpycd", cmpycd);

        apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    whList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "whcd");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "code");
                        dto.codenm = getStringVal(m, "whnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "cdnm");
                        whList.add(dto);
                    }
                    ArrayAdapter<CodeDto> whAdapter = new ArrayAdapter<>(MHPIO420S.this, android.R.layout.simple_spinner_item, whList);
                    whAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWarehouse.setAdapter(whAdapter);

                    // 기본 200 창고선택
                    for (int i = 0; i < whList.size(); i++) {
                        if ("200".equals(whList.get(i).codecd)) {
                            spWarehouse.setSelection(i);
                            break;
                        }
                    }
                }
                search();
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) { search(); }
        });
    }

    private void search() {
        String selectedWh = "200";
        if (spWarehouse != null && spWarehouse.getSelectedItem() != null) {
            selectedWh = ((CodeDto) spWarehouse.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("cmpycd", cmpycd);
        p.put("whcd", selectedWh);
        p.put("fromdt", tvStartDate.getText().toString().replace("-", "").trim());
        p.put("todt", tvEndDate.getText().toString().replace("-", "").trim());

        apiService.executeHpioProcedure("HPIO_420S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statusList.clear();
                    statusList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (tvTotalCount != null) {
                        tvTotalCount.setText("Total: " + statusList.size() + "건");
                    }
                    Toast.makeText(MHPIO420S.this, "조회되었습니다. (" + statusList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPIO420S.this, "조회된 내역이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPIO420S.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        statusList.clear();
        adapter.notifyDataSetChanged();
        if (tvTotalCount != null) tvTotalCount.setText("Total: 0건");
        search();
    }

    private void showDatePicker(TextView tv) {
        Calendar cal = Calendar.getInstance();
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tv.getText().toString());
            if (d != null) cal.setTime(d);
        } catch (Exception ignored) {}

        new DatePickerDialog(this, (view, y, m, d) -> {
            tv.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d));
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

    @Override protected String getProgramTitle() { return "제품입고현황"; }
    @Override protected String getProgramId() { return "MHPIO420S"; }

    private class StatusAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return statusList.size(); }
        @Override public Object getItem(int p) { return statusList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPIO420S.this).inflate(R.layout.item_mhpio420s, parent, false);
                holder = new ViewHolder();
                holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvLineProg = convertView.findViewById(R.id.tvLineProg);
                holder.tvInQty = convertView.findViewById(R.id.tvInQty);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> item = statusList.get(position);

            holder.tvItemCode.setText(getStringVal(item, "itemcd"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvItSize.setText(String.format("%s / %s", getStringVal(item, "itsize"), getStringVal(item, "unit")));

            String line = getStringVal(item, "linenm");
            String prog = getStringVal(item, "prognm");
            String lineProg = !line.isEmpty() ? String.format("%s / %s", line, prog) : prog;
            if (lineProg.isEmpty()) lineProg = "-";
            holder.tvLineProg.setText(lineProg);

            double inQty = getDoubleVal(item, "inqty");
            holder.tvInQty.setText("입고량: " + df.format(inQty));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvItemCode, tvItemName, tvItSize, tvLineProg, tvInQty;
    }
}
