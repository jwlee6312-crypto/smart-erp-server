package com.crmbank.erp.mobile.hppl;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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
 * 🚀 [MHPPL170U] 모바일 주간생산계획
 * 웹 HPPL170U.vue 기반 100% 매칭
 * 1. 납기기간 (tvStartDate ~ tvEndDate) 및 대상구분 (100:주문, 200:양산, 300:외주) 선택
 * 2. 주문/생산요청 대상 목록 조회 (/product/pdplan/targetlist)
 * 3. 공정별 확정 생산계획 목록 조회 (/product/pdplan/list)
 * 4. 생산라인 지정 계획 생성 및 선택 삭제 (/product/pdplan/save)
 */
public class MHPPL170U extends BaseActivity {

    private TextView tvStartDate, tvEndDate;
    private Spinner spGubun, spApplyLine;
    private ListView lvTargetList, lvPlanList;

    private TargetAdapter targetAdapter;
    private PlanAdapter planAdapter;

    private final List<Map<String, Object>> targetList = new ArrayList<>();
    private final List<Map<String, Object>> planList = new ArrayList<>();
    private final List<CodeDto> lineList = new ArrayList<>();
    private final List<CodeDto> gubunList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";
    private String userid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhppl170u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        initViews();
        loadLineOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("주간 생산계획");

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        spGubun = findViewById(R.id.spGubun);

        lvTargetList = findViewById(R.id.lvTargetList);
        lvPlanList = findViewById(R.id.lvPlanList);

        targetAdapter = new TargetAdapter();
        lvTargetList.setAdapter(targetAdapter);

        planAdapter = new PlanAdapter();
        lvPlanList.setAdapter(planAdapter);

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

        initGubunOptions();
    }

    private void initGubunOptions() {
        gubunList.clear();
        CodeDto g1 = new CodeDto(); g1.codecd = "100"; g1.codenm = "주문건"; gubunList.add(g1);
        CodeDto g2 = new CodeDto(); g2.codecd = "200"; g2.codenm = "양산요청"; gubunList.add(g2);
        CodeDto g3 = new CodeDto(); g3.codecd = "300"; g3.codenm = "외주요청"; gubunList.add(g3);

        ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gubunList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGubun.setAdapter(adapter);
    }

    private void loadLineOptions() {
        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "L0");
        p.put("cmpycd", cmpycd);
        p.put("gbncd", "Y");
        p.put("code", "");

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
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPPL170U.this, android.R.layout.simple_spinner_item, lineList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    if (spApplyLine != null) spApplyLine.setAdapter(adapter);
                }
                search();
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) { search(); }
        });
    }

    private void search() {
        String selectedGubun = "100";
        if (spGubun != null && spGubun.getSelectedItem() != null) {
            selectedGubun = ((CodeDto) spGubun.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("frymd", tvStartDate.getText().toString().replace("-", "").trim());
        p.put("toymd", tvEndDate.getText().toString().replace("-", "").trim());
        p.put("gubun", selectedGubun);
        p.put("cmpycd", cmpycd);

        // 1. 상단: 생산계획 대상 조회 (/product/pdplan/targetlist)
        apiService.getPdPlanTargetList(p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    targetList.clear();
                    for (Map<String, Object> item : response.body()) {
                        Map<String, Object> row = new HashMap<>(item);
                        row.put("chk", false);
                        targetList.add(row);
                    }
                    targetAdapter.notifyDataSetChanged();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });

        // 2. 하단: 확정된 생산계획 목록 조회 (/product/pdplan/list)
        apiService.getPdPlanList(p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    planList.clear();
                    for (Map<String, Object> item : response.body()) {
                        Map<String, Object> row = new HashMap<>(item);
                        row.put("chk", false);
                        planList.add(row);
                    }
                    planAdapter.notifyDataSetChanged();
                    Toast.makeText(MHPPL170U.this, "주간 생산계획이 조회되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void handleMakePlan() {
        String selectedLine = "";
        if (spApplyLine != null && spApplyLine.getSelectedItem() != null) {
            selectedLine = ((CodeDto) spApplyLine.getSelectedItem()).codecd;
        }

        if (selectedLine.isEmpty()) {
            Toast.makeText(this, "적용할 생산라인을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> selectedTargets = new ArrayList<>();
        for (Map<String, Object> item : targetList) {
            Boolean chk = (Boolean) item.get("chk");
            if (Boolean.TRUE.equals(chk)) {
                Map<String, Object> row = new HashMap<>(item);
                row.put("_status", "입력");
                row.put("linecd", selectedLine);
                String napgi = getStringVal(item, "napgiymd");
                if (napgi.isEmpty()) napgi = getStringVal(item, "napgi_dt");
                row.put("yymmdd", napgi.replace("-", ""));

                String qty = getStringVal(item, "planqty");
                if (qty.isEmpty() || "0".equals(qty)) qty = getStringVal(item, "ordqty");
                row.put("planqty", qty);
                row.put("cmpycd", cmpycd);
                row.put("updemp", userid);
                selectedTargets.add(row);
            }
        }

        if (selectedTargets.isEmpty()) {
            Toast.makeText(this, "계획을 생성할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("details", selectedTargets);

        apiService.executeHpplProcedure("save", payload).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHPPL170U.this, "성공적으로 생산계획이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    search();
                } else {
                    Toast.makeText(MHPPL170U.this, "계획 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPPL170U.this, "생성 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeletePlan() {
        List<Map<String, Object>> selectedPlans = new ArrayList<>();
        for (Map<String, Object> item : planList) {
            Boolean chk = (Boolean) item.get("chk");
            if (Boolean.TRUE.equals(chk)) {
                Map<String, Object> row = new HashMap<>(item);
                row.put("_status", "삭제");
                row.put("cmpycd", cmpycd);
                row.put("updemp", userid);
                selectedPlans.add(row);
            }
        }

        if (selectedPlans.isEmpty()) {
            Toast.makeText(this, "삭제할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("details", selectedPlans);

        apiService.executeHpplProcedure("save", payload).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHPPL170U.this, "성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    search();
                } else {
                    Toast.makeText(MHPPL170U.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPPL170U.this, "삭제 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        targetList.clear();
        planList.clear();
        targetAdapter.notifyDataSetChanged();
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

    @Override protected String getProgramTitle() { return "주간생산계획"; }
    @Override protected String getProgramId() { return "MHPPL170U"; }

    // 🔴 1. 주문/생산요청 대상 어댑터
    private class TargetAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return targetList.size(); }
        @Override public Object getItem(int p) { return targetList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TargetViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPPL170U.this).inflate(R.layout.item_mhppl170u_target, parent, false);
                holder = new TargetViewHolder();
                holder.cbSelect = convertView.findViewById(R.id.cbSelect);
                holder.tvGubun = convertView.findViewById(R.id.tvGubun);
                holder.tvShowSord = convertView.findViewById(R.id.tvShowSord);
                holder.tvCustNm = convertView.findViewById(R.id.tvCustNm);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvOrdQty = convertView.findViewById(R.id.tvOrdQty);
                holder.tvNapgiYmd = convertView.findViewById(R.id.tvNapgiYmd);
                convertView.setTag(holder);
            } else {
                holder = (TargetViewHolder) convertView.getTag();
            }

            Map<String, Object> item = targetList.get(position);

            Boolean chk = (Boolean) item.get("chk");
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(Boolean.TRUE.equals(chk));
            holder.cbSelect.setOnCheckedChangeListener((btn, isChecked) -> item.put("chk", isChecked));

            String gubun = getStringVal(item, "gubun");
            if ("200".equals(gubun)) holder.tvGubun.setText("양산요청");
            else if ("300".equals(gubun)) holder.tvGubun.setText("외주요청");
            else holder.tvGubun.setText("주문건");

            String ordYm = getStringVal(item, "ordymd");
            String ordNo = getStringVal(item, "ordno");
            String showSord = !ordYm.isEmpty() ? String.format("%s-%s", ordYm, ordNo) : getStringVal(item, "showSord");
            if (showSord.isEmpty()) showSord = "-";
            holder.tvShowSord.setText(showSord);

            holder.tvCustNm.setText(getStringVal(item, "custnm"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvItSize.setText(String.format("%s / %s", getStringVal(item, "itsize"), getStringVal(item, "unit")));

            double ordQty = getDoubleVal(item, "ordqty");
            holder.tvOrdQty.setText("요청: " + df.format(ordQty));
            holder.tvNapgiYmd.setText("납기: " + formatDate(getStringVal(item, "napgiymd")));

            return convertView;
        }
    }

    static class TargetViewHolder {
        CheckBox cbSelect;
        TextView tvGubun, tvShowSord, tvCustNm, tvItemName, tvItSize, tvOrdQty, tvNapgiYmd;
    }

    // 🟢 2. 확정 생산계획 어댑터
    private class PlanAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return planList.size(); }
        @Override public Object getItem(int p) { return planList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PlanViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPPL170U.this).inflate(R.layout.item_mhppl170u_plan, parent, false);
                holder = new PlanViewHolder();
                holder.cbSelect = convertView.findViewById(R.id.cbSelect);
                holder.tvPlanYmd = convertView.findViewById(R.id.tvPlanYmd);
                holder.tvLineProg = convertView.findViewById(R.id.tvLineProg);
                holder.tvCustNm = convertView.findViewById(R.id.tvCustNm);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvPlanQty = convertView.findViewById(R.id.tvPlanQty);
                convertView.setTag(holder);
            } else {
                holder = (PlanViewHolder) convertView.getTag();
            }

            Map<String, Object> item = planList.get(position);

            Boolean chk = (Boolean) item.get("chk");
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(Boolean.TRUE.equals(chk));
            holder.cbSelect.setOnCheckedChangeListener((btn, isChecked) -> item.put("chk", isChecked));

            String yymmdd = formatDate(getStringVal(item, "yymmdd"));
            String dayNm = getStringVal(item, "day_nm");
            holder.tvPlanYmd.setText(!dayNm.isEmpty() ? String.format("%s (%s)", yymmdd, dayNm) : yymmdd);

            String line = getStringVal(item, "linenm");
            String prog = getStringVal(item, "prognm");
            holder.tvLineProg.setText(String.format("%s / %s", line, prog));

            holder.tvCustNm.setText(getStringVal(item, "custnm"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvItSize.setText(String.format("%s / %s", getStringVal(item, "itsize"), getStringVal(item, "unit")));

            double planQty = getDoubleVal(item, "planqty");
            holder.tvPlanQty.setText("계획: " + df.format(planQty));

            return convertView;
        }
    }

    static class PlanViewHolder {
        CheckBox cbSelect;
        TextView tvPlanYmd, tvLineProg, tvCustNm, tvItemName, tvItSize, tvPlanQty;
    }
}
