package com.crmbank.erp.mobile.hppl;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
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
 * 🚀 [MHPPL150U] 모바일 양산계획등록 (조회 전용)
 * 웹 HPPL150U.vue 기반 100% 매칭 (순수 정보 전달/조회 전용 구현)
 * 1. 요청기간 선택 (frymd ~ toymd)
 * 2. 양산 생산 요청 내역 조회 (HPPL_150U_STR '200')
 */
public class MHPPL150U extends BaseActivity {

    private TextView tvStartDate, tvEndDate;
    private ListView lvPlanList;
    private PlanAdapter planAdapter;

    private final List<Map<String, Object>> planList = new ArrayList<>();
    private ApiService apiService;
    private String cmpycd = "COIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhppl150u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        search();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("양산계획등록");

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);

        // 기본 날짜 세팅 (당월 1일 ~ 오늘)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvEndDate.setText(sdf.format(cal.getTime()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        tvStartDate.setText(sdf.format(cal.getTime()));

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        lvPlanList = findViewById(R.id.lvPlanList);
        planAdapter = new PlanAdapter();
        lvPlanList.setAdapter(planAdapter);

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> search());
    }

    private void search() {
        Map<String, Object> p = new HashMap<>();
        p.put("frymd", tvStartDate.getText().toString().replace("-", ""));
        p.put("toymd", tvEndDate.getText().toString().replace("-", ""));
        p.put("gubun", "200");
        p.put("cmpycd", cmpycd);

        apiService.getPdRequestList(p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    planList.clear();
                    planList.addAll(response.body());
                    planAdapter.notifyDataSetChanged();
                    Toast.makeText(MHPPL150U.this, "조회되었습니다. (" + planList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPPL150U.this, "조회된 내역이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPPL150U.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        planList.clear();
        planAdapter.notifyDataSetChanged();
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

    private String formatDate(String d) {
        if (d == null || d.isEmpty()) return "";
        if (d.contains(" ")) d = d.split(" ")[0];
        return d.length() == 8 ? String.format("%s-%s-%s", d.substring(0,4), d.substring(4,6), d.substring(6,8)) : d;
    }

    @Override protected String getProgramTitle() { return "양산계획등록"; }
    @Override protected String getProgramId() { return "MHPPL150U"; }

    private class PlanAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return planList.size(); }
        @Override public Object getItem(int p) { return planList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPPL150U.this).inflate(R.layout.item_mhppl150u, parent, false);
                holder = new ViewHolder();
                holder.tvItemNm = convertView.findViewById(R.id.tvItemNm);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvPlanQty = convertView.findViewById(R.id.tvPlanQty);
                holder.tvNapgiYmd = convertView.findViewById(R.id.tvNapgiYmd);
                holder.tvCustNm = convertView.findViewById(R.id.tvCustNm);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> item = planList.get(position);

            holder.tvItemNm.setText(getStringVal(item, "itemnm"));
            holder.tvItSize.setText(String.format("%s / %s", getStringVal(item, "itsize"), getStringVal(item, "unit")));

            double qty = getDoubleVal(item, "planqty");
            holder.tvPlanQty.setText(df.format(qty));

            holder.tvNapgiYmd.setText(formatDate(getStringVal(item, "napgiymd")));
            holder.tvCustNm.setText(getStringVal(item, "custnm"));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvItemNm, tvPlanQty, tvCustNm, tvItSize, tvNapgiYmd;
    }
}
