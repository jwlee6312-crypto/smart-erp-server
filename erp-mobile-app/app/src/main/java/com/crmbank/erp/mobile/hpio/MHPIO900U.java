package com.crmbank.erp.mobile.hpio;

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
 * 🚀 [MHPIO900U] 모바일 제품검사의뢰
 * 웹 HPIO900U.vue 기반 100% 매칭
 * 1. 검사분류 (E0/820) & 생산라인 (L0) 스피너 로드
 * 2. 생산실적 기반 검사요청 대상 목록 조회 (/product/insp-req/target-list)
 * 3. 검사요청 저장 (/product/insp-req/save) 및 요청취소 (/product/insp-req/delete)
 */
public class MHPIO900U extends BaseActivity {

    private Spinner spInspGb, spLine;
    private TextView tvProYmdFr, tvProYmdTo, tvInspReqDt, tvTotalCount;
    private ListView lvTargetList;
    private TargetAdapter adapter;

    private final List<Map<String, Object>> targetList = new ArrayList<>();
    private final List<CodeDto> inspGbList = new ArrayList<>();
    private final List<CodeDto> lineList = new ArrayList<>();

    private ApiService apiService;
    private String cmpycd = "COIT";
    private String userid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhpio900u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        initViews();
        loadOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("제품검사의뢰");

        spInspGb = findViewById(R.id.spInspGb);
        spLine = findViewById(R.id.spLine);

        tvProYmdFr = findViewById(R.id.tvProYmdFr);
        tvProYmdTo = findViewById(R.id.tvProYmdTo);
        tvInspReqDt = findViewById(R.id.tvInspReqDt);
        tvTotalCount = findViewById(R.id.tvTotalCount);

        lvTargetList = findViewById(R.id.lvTargetList);
        adapter = new TargetAdapter();
        lvTargetList.setAdapter(adapter);

        // 기본 날짜 세팅 (당월 1일 ~ 오늘)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(cal.getTime());
        tvProYmdTo.setText(today);
        tvInspReqDt.setText(today);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        tvProYmdFr.setText(sdf.format(cal.getTime()));

        tvProYmdFr.setOnClickListener(v -> showDatePicker(tvProYmdFr));
        tvProYmdTo.setOnClickListener(v -> showDatePicker(tvProYmdTo));
        tvInspReqDt.setOnClickListener(v -> showDatePicker(tvInspReqDt));

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> search());

        spInspGb.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                search();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spLine.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                search();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadOptions() {
        // 1. 검사분류 로드 (HP00_000S_STR gubun: E0, gbncd: 820)
        Map<String, Object> pGb = new HashMap<>();
        pGb.put("gubun", "E0");
        pGb.put("cmpycd", cmpycd);
        pGb.put("gbncd", "820");
        pGb.put("code", "");
        pGb.put("codenm", "");
        pGb.put("etcval", "");

        apiService.executeHp00Procedure("HP00_000S_STR", pGb).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    inspGbList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "code");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "codecd");
                        dto.codenm = getStringVal(m, "cdnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "codenm");
                        inspGbList.add(dto);
                    }
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPIO900U.this, android.R.layout.simple_spinner_item, inspGbList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spInspGb.setAdapter(adapter);

                    if (!inspGbList.isEmpty()) {
                        spInspGb.setSelection(0);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });

        // 2. 생산라인 로드 (HP00_000S_STR gubun: L0, gbncd: Y)
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
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHPIO900U.this, android.R.layout.simple_spinner_item, lineList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLine.setAdapter(adapter);

                    if (!lineList.isEmpty()) {
                        spLine.setSelection(0);
                    }
                }
                search();
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) { search(); }
        });
    }

    private void search() {
        String inspGb = "";
        if (spInspGb != null && spInspGb.getSelectedItem() != null) {
            inspGb = ((CodeDto) spInspGb.getSelectedItem()).codecd;
        }

        String linecd = "";
        if (spLine != null && spLine.getSelectedItem() != null) {
            linecd = ((CodeDto) spLine.getSelectedItem()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("cmpycd", cmpycd);
        p.put("insp_gb", inspGb);
        p.put("linecd", linecd);
        p.put("proymd_fr", tvProYmdFr.getText().toString().replace("-", "").trim());
        p.put("proymd_to", tvProYmdTo.getText().toString().replace("-", "").trim());

        apiService.getPdInspReqTargetList(p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    targetList.clear();
                    for (Map<String, Object> item : response.body()) {
                        Map<String, Object> row = new HashMap<>(item);
                        row.put("chk", false);
                        targetList.add(row);
                    }
                    adapter.notifyDataSetChanged();

                    if (tvTotalCount != null) {
                        tvTotalCount.setText("Total: " + targetList.size() + "건");
                    }
                    Toast.makeText(MHPIO900U.this, "조회되었습니다. (" + targetList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHPIO900U.this, "조회된 의뢰 대상이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPIO900U.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        List<Map<String, Object>> selectedItems = new ArrayList<>();
        String inspGb = "";
        if (spInspGb != null && spInspGb.getSelectedItem() != null) {
            inspGb = ((CodeDto) spInspGb.getSelectedItem()).codecd;
        }
        String inspReqDt = tvInspReqDt.getText().toString().replace("-", "").trim();

        for (Map<String, Object> item : targetList) {
            Boolean chk = (Boolean) item.get("chk");
            String reqNo = getStringVal(item, "insp_req_no");
            if (Boolean.TRUE.equals(chk) && reqNo.isEmpty()) {
                Map<String, Object> row = new HashMap<>(item);
                row.put("state", "C");
                row.put("cmpycd", cmpycd);
                row.put("insp_gb", inspGb);
                row.put("insp_req_dt", inspReqDt);
                row.put("updemp", userid);
                selectedItems.add(row);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "의뢰 대기 중인 항목을 선택해 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("items", selectedItems);

        apiService.executeHpioProcedure("HPIO_900U_SAVE", payload).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHPIO900U.this, "성공적으로 의뢰 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    search();
                } else {
                    Toast.makeText(MHPIO900U.this, "의뢰 저장 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPIO900U.this, "저장 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void delete() {
        List<Map<String, Object>> deleteTargets = new ArrayList<>();
        String inspGb = "";
        if (spInspGb != null && spInspGb.getSelectedItem() != null) {
            inspGb = ((CodeDto) spInspGb.getSelectedItem()).codecd;
        }

        for (Map<String, Object> item : targetList) {
            Boolean chk = (Boolean) item.get("chk");
            String reqNo = getStringVal(item, "insp_req_no");
            if (Boolean.TRUE.equals(chk) && !reqNo.isEmpty()) {
                Map<String, Object> row = new HashMap<>(item);
                row.put("cmpycd", cmpycd);
                row.put("insp_gb", inspGb);
                row.put("insp_req_dt", getStringVal(item, "insp_req_dt").replace("-", ""));
                row.put("insp_req_no", reqNo);
                deleteTargets.add(row);
            }
        }

        if (deleteTargets.isEmpty()) {
            Toast.makeText(this, "취소할 의뢰 건을 선택해 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("items", deleteTargets);

        apiService.executeHpioProcedure("HPIO_900U_DELETE", payload).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHPIO900U.this, "성공적으로 의뢰 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    search();
                } else {
                    Toast.makeText(MHPIO900U.this, "의뢰 취소 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHPIO900U.this, "취소 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        targetList.clear();
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

    @Override protected String getProgramTitle() { return "제품검사의뢰"; }
    @Override protected String getProgramId() { return "MHPIO900U"; }

    private class TargetAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return targetList.size(); }
        @Override public Object getItem(int p) { return targetList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHPIO900U.this).inflate(R.layout.item_mhpio900u, parent, false);
                holder = new ViewHolder();
                holder.cbSelect = convertView.findViewById(R.id.cbSelect);
                holder.tvStatusBadge = convertView.findViewById(R.id.tvStatusBadge);
                holder.tvProYmd = convertView.findViewById(R.id.tvProYmd);
                holder.tvProgNm = convertView.findViewById(R.id.tvProgNm);
                holder.tvInspReqNo = convertView.findViewById(R.id.tvInspReqNo);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvPrdQty = convertView.findViewById(R.id.tvPrdQty);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> item = targetList.get(position);

            Boolean chk = (Boolean) item.get("chk");
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(Boolean.TRUE.equals(chk));
            holder.cbSelect.setOnCheckedChangeListener((btn, isChecked) -> item.put("chk", isChecked));

            String reqNo = getStringVal(item, "insp_req_no");
            String reqDt = getStringVal(item, "insp_req_dt");
            if (!reqNo.isEmpty()) {
                holder.tvStatusBadge.setText("의뢰완료");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_button_gradient_teal);
                holder.tvInspReqNo.setText(String.format("%s-%s", formatDate(reqDt), reqNo));
            } else {
                holder.tvStatusBadge.setText("의뢰대기");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_button_gradient_blue);
                holder.tvInspReqNo.setText("-");
            }

            holder.tvProYmd.setText(formatDate(getStringVal(item, "proymd")));
            holder.tvProgNm.setText(getStringVal(item, "prognm"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvItSize.setText(String.format("%s / %s", getStringVal(item, "itsize"), getStringVal(item, "unit")));

            double prdQty = getDoubleVal(item, "prdqty");
            holder.tvPrdQty.setText("생산량: " + df.format(prdQty));

            return convertView;
        }
    }

    static class ViewHolder {
        CheckBox cbSelect;
        TextView tvStatusBadge, tvProYmd, tvProgNm, tvInspReqNo, tvItemName, tvItSize, tvPrdQty;
    }
}
