package com.crmbank.erp.mobile.hpio;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.PopupAdapter;
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
 * 🚀 [MHPIO252S] 모바일 자재불출요청 현황
 * 웹 HPIO252S.vue 기반 100% 매칭
 * 1. 요청부서 및 요청기간 (frymd ~ toymd) 조회
 * 2. 불출 요청된 자재의 현황 및 불출량 조회 (HPIO_252S_STR)
 */
public class MHPIO252S extends BaseActivity {

    private TextView tvStartDate, tvEndDate, tvTotalCount;
    private ListView lvRequestList;
    private RequestAdapter adapter;

    private final List<Map<String, Object>> requestList = new ArrayList<>();
    private ApiService apiService;
    private String cmpycd = "COIT";
    private String deptcd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpio252s);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        initViews();
        search();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("자재불출요청현황");

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvTotalCount = findViewById(R.id.tvTotalCount);

        lvRequestList = findViewById(R.id.lvRequestList);
        adapter = new RequestAdapter();
        lvRequestList.setAdapter(adapter);

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

    private void search() {
        Map<String, Object> p = new HashMap<>();
        p.put("cmpycd", cmpycd);
        p.put("deptcd", "");
        p.put("deptnm", "");
        p.put("fromdt", tvStartDate.getText().toString().replace("-", "").trim());
        p.put("todt", tvEndDate.getText().toString().replace("-", "").trim());

        apiService.executeHpioProcedure("HPIO_252S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (tvTotalCount != null) {
                        tvTotalCount.setText("Total: " + requestList.size() + "건");
                    }
                    Toast.makeText(MHPIO252S.this, "조회되었습니다. (" + requestList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPIO252S.this, "조회된 내역이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPIO252S.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        deptcd = "";
        requestList.clear();
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

    private String formatDate(String d) {
        if (d == null || d.isEmpty()) return "";
        if (d.contains(" ")) d = d.split(" ")[0];
        return d.length() == 8 ? String.format("%s-%s-%s", d.substring(0,4), d.substring(4,6), d.substring(6,8)) : d;
    }

    @Override protected String getProgramTitle() { return "자재불출요청현황"; }
    @Override protected String getProgramId() { return "MHPIO252S"; }

    private class RequestAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return requestList.size(); }
        @Override public Object getItem(int p) { return requestList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPIO252S.this).inflate(R.layout.item_mhpio252s, parent, false);
                holder = new ViewHolder();
                holder.tvOutYmNo = convertView.findViewById(R.id.tvOutYmNo);
                holder.tvWhNm = convertView.findViewById(R.id.tvWhNm);
                holder.tvIWhNm = convertView.findViewById(R.id.tvIWhNm);
                holder.tvMItemName = convertView.findViewById(R.id.tvMItemName);
                holder.tvMItSize = convertView.findViewById(R.id.tvMItSize);
                holder.tvReqQty = convertView.findViewById(R.id.tvReqQty);
                holder.tvOutQty = convertView.findViewById(R.id.tvOutQty);
                holder.tvIoYmNo = convertView.findViewById(R.id.tvIoYmNo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> item = requestList.get(position);

            String outYm = getStringVal(item, "outym");
            String outNo = getStringVal(item, "outno");
            String outYmNo = !outYm.isEmpty() ? String.format("%s-%s", outYm, outNo) : getStringVal(item, "outymno");
            if (outYmNo.isEmpty()) outYmNo = "-";
            holder.tvOutYmNo.setText(outYmNo);

            holder.tvWhNm.setText("출고: " + getStringVal(item, "whnm"));
            holder.tvIWhNm.setText("입고: " + getStringVal(item, "iwhnm"));

            holder.tvMItemName.setText(getStringVal(item, "mitemnm"));
            holder.tvMItSize.setText(String.format("%s / %s", getStringVal(item, "mitsize"), getStringVal(item, "munit")));

            double reqQty = getDoubleVal(item, "reqqty");
            double outQty = getDoubleVal(item, "outqty");

            holder.tvReqQty.setText("요청: " + df.format(reqQty));
            holder.tvOutQty.setText("불출: " + df.format(outQty));

            String ioYm = getStringVal(item, "ioym");
            String ioNo = getStringVal(item, "iono");
            String ioYmNo = !ioYm.isEmpty() ? String.format("%s-%s", ioYm, ioNo) : getStringVal(item, "ioymno");
            if (ioYmNo.isEmpty()) ioYmNo = "-";
            holder.tvIoYmNo.setText("출고번호: " + ioYmNo);

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvOutYmNo, tvWhNm, tvIWhNm, tvMItemName, tvMItSize, tvReqQty, tvOutQty, tvIoYmNo;
    }
}
