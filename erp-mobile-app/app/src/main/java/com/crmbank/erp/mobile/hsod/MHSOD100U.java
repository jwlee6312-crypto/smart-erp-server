package com.crmbank.erp.mobile.hsod;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.RetrofitClient;
import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.ApiResponse;
import com.crmbank.erp.mobile.PopupAdapter;
import com.crmbank.erp.mobile.R;

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
 * 🚀 [MHSOD100U] 주문등록
 * MHSIO052U 패턴을 적용한 고성능 주문 등록 및 조회 모듈
 */
public class MHSOD100U extends BaseActivity {

    private TextView tvOrderDate;
    private EditText etOrderNo, etCustomerName, etRemarks;
    private EditText etTranNm, etPostNo, etAddress, etDAddress;
    private OrderAdapter adapter;
    private final List<Map<String, Object>> orderItems = new ArrayList<>();
    private final Map<String, Object> masterData = new HashMap<>();
    private ApiService apiService;
    private String cmpycd, deptcd, userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhsod100u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "coit").trim();
        deptcd = prefs.getString("deptcd", "");
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        tvOrderDate = findViewById(R.id.tvOrderDate);
        etOrderNo = findViewById(R.id.etOrderNo);
        etCustomerName = findViewById(R.id.etCustomerName);
        etRemarks = findViewById(R.id.etRemarks);
        etTranNm = findViewById(R.id.etTranNm);
        etPostNo = findViewById(R.id.etPostNo);
        etAddress = findViewById(R.id.etAddress);
        etDAddress = findViewById(R.id.etDAddress);
        ListView lvOrderList = findViewById(R.id.lvOrderList);

        adapter = new OrderAdapter();
        lvOrderList.setAdapter(adapter);

        tvOrderDate.setOnClickListener(v -> showDatePicker(tvOrderDate));
        etCustomerName.setOnClickListener(v -> openHelp("CUST"));
        etTranNm.setOnClickListener(v -> openHelp("ADDR"));
        etPostNo.setOnClickListener(v -> openHelp("ADDR"));
        etAddress.setOnClickListener(v -> openHelp("ADDR"));
        findViewById(R.id.btnAddItem).setOnClickListener(v -> openHelp("ITEM"));
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnReset).setOnClickListener(v -> initialize());
        findViewById(R.id.btnDelete).setOnClickListener(v -> handleFullDelete());
        findViewById(R.id.btnOrderSearch).setOnClickListener(v -> showOrderSearchPopup());

        initialize();
    }

    private void initialize() {
        masterData.clear();
        String todayYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // 🚀 모든 필드 초기화 (백엔드 에러 방지를 위해 공백/디폴트값 설정)
        masterData.put("cmpycd", cmpycd);
        masterData.put("ordym", todayYmd.replace("-", "").substring(0, 6));
        masterData.put("ordno", "0000");
        masterData.put("ordymd", todayYmd.replace("-", ""));
        masterData.put("outymd", todayYmd.replace("-", ""));
        masterData.put("deptcd", deptcd); // 세션 부서코드
        masterData.put("custcd", "");
        masterData.put("custnm", "");
        masterData.put("remark", "");
        masterData.put("trancd", "");
        masterData.put("postno", "");
        masterData.put("address", "");
        masterData.put("d_address", "");
        masterData.put("userid", userid); // 세션 사용자ID
        masterData.put("updemp", userid);

        etOrderNo.setText("");
        etOrderNo.setHint("(자동 생성)");
        etCustomerName.setText("");
        etRemarks.setText("");
        etTranNm.setText("");
        etPostNo.setText("");
        etAddress.setText("");
        etDAddress.setText("");
        tvOrderDate.setText(todayYmd);
        
        orderItems.clear();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showDatePicker(TextView tv) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d);
            tv.setText(date);
            if (tv.getId() == R.id.tvOrderDate) {
                masterData.put("ordymd", date.replace("-", ""));
                masterData.put("ordym", date.replace("-", "").substring(0, 6));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showOrderSearchPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sales_order_search, null);
        builder.setTitle("주문 목록 조회").setView(dialogView);

        TextView tvPopStart = dialogView.findViewById(R.id.tvPopStartDate);
        TextView tvPopEnd = dialogView.findViewById(R.id.tvPopEndDate);
        EditText etPopCust = dialogView.findViewById(R.id.etPopCustNm);
        ListView lv = dialogView.findViewById(R.id.lvPopOrderList);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvPopEnd.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        tvPopStart.setText(sdf.format(cal.getTime()));

        tvPopStart.setOnClickListener(v -> showDatePicker(tvPopStart));
        tvPopEnd.setOnClickListener(v -> showDatePicker(tvPopEnd));

        List<Map<String, Object>> popList = new ArrayList<>();
        BaseAdapter popAdapter = new BaseAdapter() {
            @Override public int getCount() { return popList.size(); }
            @Override public Object getItem(int p) { return popList.get(p); }
            @Override public long getItemId(int p) { return p; }
            @Override public View getView(int p, View v, ViewGroup pr) {
                if (v == null) v = LayoutInflater.from(pr.getContext()).inflate(R.layout.item_mhsod100u_pop, pr, false);
                Map<String, Object> item = popList.get(p);
                ((TextView) v.findViewById(R.id.tvPopCustNm)).setText(getStringVal(item, "custnm"));
                ((TextView) v.findViewById(R.id.tvPopOrdYmd)).setText(getStringVal(item, "ordymd"));
                String bNo = getStringVal(item, "ordym") + "-" + getStringVal(item, "ordno");
                ((TextView) v.findViewById(R.id.tvPopOrdno)).setText(bNo);
                return v;
            }
        };
        lv.setAdapter(popAdapter);

        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btnPopSearch).setOnClickListener(v -> {
            Map<String, Object> p = new HashMap<>();
            // 🚀 웹 표준(HSOD100U.vue)에 따라 조회 시 actkind='S1' 사용
            p.put("actkind", "S1");
            p.put("cmpycd", cmpycd);
            p.put("fromdt", tvPopStart.getText().toString().replace("-", ""));
            p.put("todt", tvPopEnd.getText().toString().replace("-", ""));
            p.put("custnm", etPopCust.getText().toString().trim());
            
            // 🚀 필수 파라미터 누락 방지 (웹 표준 준수)
            p.put("ordno", "0000");
            p.put("ordkind", "100");
            p.put("ordemp", userid);
            p.put("deptcd", deptcd);

            apiService.executeHsodProcedure("HSOD_100U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        popList.clear(); popList.addAll(response.body()); popAdapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
            });
        });

        lv.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            fetchDetail(popList.get(position));
        });

        dialogView.findViewById(R.id.btnPopClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void fetchDetail(Map<String, Object> row) {
        if (row == null) return;
        
        masterData.clear();
        masterData.putAll(row);
        
        String ordym = getStringVal(row, "ordym");
        String ordno = getStringVal(row, "ordno");
        
        // 🚀 리스트에서 가져온 데이터를 즉시 UI에 반영 (중복 서버 호출 제거)
        etOrderNo.setText(String.format("%s-%s", ordym, ordno));
        etCustomerName.setText(getStringVal(row, "custnm"));
        etRemarks.setText(getStringVal(row, "remark"));
        etTranNm.setText(getStringVal(row, "trannm"));
        etPostNo.setText(getStringVal(row, "postno"));
        etAddress.setText(getStringVal(row, "address"));
        etDAddress.setText(getStringVal(row, "d_address"));
        tvOrderDate.setText(formatDate(getStringVal(row, "ordymd")));
        
        // 상세 품목 내역만 서버에서 조회
        fetchItems(ordym, ordno);
    }

    private void fetchItems(String ordym, String ordno) {
        Map<String, Object> p = new HashMap<>();
        // 🚀 상세 행 조회 시 actkind='S0' 사용 (웹 표준)
        p.put("actkind", "S0");
        p.put("cmpycd", cmpycd);
        p.put("ordym", ordym);
        p.put("ordno", ordno);

        apiService.executeHsodProcedure("HSOD_101U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderItems.clear();
                    for (Map<String, Object> item : response.body()) {
                        item.put("_status", ""); orderItems.add(item);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void save() {
        if (getStringVal(masterData, "custcd").isEmpty() || orderItems.isEmpty()) {
            Toast.makeText(this, "필수 정보를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> mst = new HashMap<>(masterData);
        String ordno = getStringVal(mst, "ordno");
        // 🚀 웹 표준(HSOD100U.vue)에 따라 마스터 actkind도 A0, U0 사용
        mst.put("actkind", (ordno.isEmpty() || "0000".equals(ordno)) ? "A0" : "U0");
        mst.put("cmpycd", cmpycd);
        mst.put("ordymd", tvOrderDate.getText().toString().replace("-", ""));
        mst.put("outymd", tvOrderDate.getText().toString().replace("-", ""));
        mst.put("remark", etRemarks.getText().toString().trim());
        
        // 🚀 주소 정보 추가
        mst.put("trancd", getStringVal(masterData, "trancd"));
        mst.put("postno", etPostNo.getText().toString().trim());
        mst.put("address", etAddress.getText().toString().trim());
        mst.put("d_address", etDAddress.getText().toString().trim());

        // 🚀 세션값 강제 지정 및 누락 방지
        mst.put("deptcd", deptcd);
        mst.put("userid", userid);
        mst.put("ordkind", "100");
        mst.put("sts", "Y");
        mst.put("updemp", userid);
        if (getStringVal(mst, "ordym").isEmpty()) {
            mst.put("ordym", tvOrderDate.getText().toString().replace("-", "").substring(0, 6));
        }

        List<Map<String, Object>> dtl = new ArrayList<>();
        for (Map<String, Object> item : orderItems) {
            String status = getStringVal(item, "_status");
            Map<String, Object> d = new HashMap<>(item);
            
            // 🚀 상세 행 필드 누락 방지
            d.put("cmpycd", cmpycd);
            d.put("ordym", mst.get("ordym"));
            d.put("ordno", mst.get("ordno"));
            d.put("actkind", status.equals("입력") ? "A0" : (status.equals("삭제") ? "D0" : "U0"));
            d.put("updemp", userid);
            
            // 수치형 필드 안전 처리
            if (d.get("ordqty") == null) d.put("ordqty", 0.0);
            if (d.get("price") == null) d.put("price", 0.0);
            if (d.get("ordamt") == null) d.put("ordamt", 0.0);
            if (d.get("ordvat") == null) d.put("ordvat", 0.0);
            if (getStringVal(d, "itsize").isEmpty()) d.put("itsize", "");
            if (getStringVal(d, "unit").isEmpty()) d.put("unit", "");
            
            dtl.add(d);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("mst", mst);
        payload.put("dtl", dtl);

        apiService.saveHsod100U(payload).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MHSOD100U.this, "저장 완료", Toast.LENGTH_SHORT).show();
                    fetchDetail(response.body().getData());
                } else {
                    Toast.makeText(MHSOD100U.this, "저장 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHSOD100U.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFullDelete() {
        if ("0000".equals(getStringVal(masterData, "ordno"))) return;
        new AlertDialog.Builder(this).setTitle("전체 삭제").setMessage("주문 정보를 모두 삭제하시겠습니까?")
            .setPositiveButton("예", (d, w) -> {
                Map<String, Object> mst = new HashMap<>();
                // 🚀 삭제 시 actkind='D0' 사용 (웹 표준)
                mst.put("actkind", "D0");
                mst.put("cmpycd", cmpycd);
                mst.put("ordym", getStringVal(masterData, "ordym"));
                mst.put("ordno", getStringVal(masterData, "ordno"));
                mst.put("updemp", userid);

                Map<String, Object> payload = new HashMap<>();
                payload.put("mst", mst);
                payload.put("dtl", new ArrayList<>());

                apiService.saveHsod100U(payload).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> c, @NonNull Response<ApiResponse<Map<String, Object>>> r) {
                        if (r.isSuccessful()) { initialize(); Toast.makeText(MHSOD100U.this, "삭제 완료", Toast.LENGTH_SHORT).show(); }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> c, @NonNull Throwable t) {}
                });
            }).setNegativeButton("아니오", null).show();
    }

    private void openHelp(String type) {
        if (type.equals("ADDR") && getStringVal(masterData, "custcd").isEmpty()) {
            Toast.makeText(this, "거래처를 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_customer_search, null);
        builder.setView(dialogView);
        
        String titleText = "";
        if (type.equals("CUST")) titleText = "거래처 검색";
        else if (type.equals("ITEM")) titleText = "품목 검색";
        else if (type.equals("ADDR")) titleText = "배송처 검색";
        
        builder.setTitle(titleText);

        EditText etSearch = dialogView.findViewById(R.id.etSearchQuery);
        RecyclerView rv = dialogView.findViewById(R.id.rvPopupList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        List<Map<String, Object>> list = new ArrayList<>();
        AlertDialog dialog = builder.create();

        PopupAdapter popupAdapter = new PopupAdapter(list, type, item -> {
            if (type.equals("CUST")) {
                masterData.put("custcd", getStringVal(item, "custcd"));
                etCustomerName.setText(getStringVal(item, "custnm"));
                
                // 거래처 변경 시 주소 정보 초기화
                masterData.put("trancd", "");
                etTranNm.setText("");
                etPostNo.setText("");
                etAddress.setText("");
                etDAddress.setText("");
            } else if (type.equals("ADDR")) {
                masterData.put("trancd", getStringVal(item, "trancd"));
                etTranNm.setText(getStringVal(item, "custnm")); // 배송처명
                etPostNo.setText(getStringVal(item, "postno"));
                etAddress.setText(getStringVal(item, "address"));
                etDAddress.setText(getStringVal(item, "d_address"));
            } else {
                Map<String, Object> newItem = new HashMap<>();
                newItem.put("itemcd", getStringVal(item, "itemcd"));
                newItem.put("itemnm", getStringVal(item, "itemnm"));
                newItem.put("itsize", getStringVal(item, "itsize"));
                newItem.put("unit", getStringVal(item, "unit"));
                
                double price = parseToDouble(item.get("omprice"));
                newItem.put("price", price);
                newItem.put("ordqty", 1.0);
                newItem.put("ordamt", price);
                newItem.put("ordvat", Math.floor(price * 0.1));
                
                // 🚀 행 추가 시 상태값 명시
                newItem.put("_status", "입력");
                orderItems.add(newItem);
                adapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        });
        rv.setAdapter(popupAdapter);

        dialogView.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Map<String, Object> p = new HashMap<>();
            p.put("cmpycd", cmpycd);
            String keyword = etSearch.getText().toString().trim();
            
            if (type.equals("CUST")) {
                p.put("gubun", "C4"); p.put("remark", keyword);
                apiService.executeHa00Procedure("HA00_00P_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> c, @NonNull Response<List<Map<String, Object>>> r) {
                        if (r.isSuccessful() && r.body() != null) { list.clear(); list.addAll(r.body()); popupAdapter.notifyDataSetChanged(); }
                    }
                    @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> c, @NonNull Throwable t) {}
                });
            } else if (type.equals("ADDR")) {
                p.put("gubun", "T0");
                p.put("gbncd", "");
                p.put("code", getStringVal(masterData, "custcd"));
                p.put("codenm", keyword);
                p.put("etcval", "");
                
                apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> c, @NonNull Response<List<Map<String, Object>>> r) {
                        if (r.isSuccessful() && r.body() != null) { list.clear(); list.addAll(r.body()); popupAdapter.notifyDataSetChanged(); }
                    }
                    @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> c, @NonNull Throwable t) {}
                });
            } else {
                p.put("gubun", "I1"); p.put("gbncd", "2"); p.put("codenm", keyword);
                apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> c, @NonNull Response<List<Map<String, Object>>> r) {
                        if (r.isSuccessful() && r.body() != null) { list.clear(); list.addAll(r.body()); popupAdapter.notifyDataSetChanged(); }
                    }
                    @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> c, @NonNull Throwable t) {}
                });
            }
        });
        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key.toLowerCase());
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val).trim() : "";
    }

    private double parseToDouble(Object val) {
        if (val == null) return 0.0;
        try { return Double.parseDouble(String.valueOf(val).replace(",", "")); } catch (Exception e) { return 0.0; }
    }

    private String formatDate(String d) {
        return d != null && d.length() == 8 ? String.format("%s-%s-%s", d.substring(0,4), d.substring(4,6), d.substring(6,8)) : d;
    }

    @Override protected String getProgramTitle() { return "주문등록"; }
    @Override protected String getProgramId() { return "MHSOD100U"; }

    private class OrderAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");
        @Override public int getCount() { return orderItems.size(); }
        @Override public Object getItem(int p) { return orderItems.get(p); }
        @Override public long getItemId(int p) { return p; }
        @Override public View getView(int p, View v, ViewGroup pr) {
            if (v == null) v = LayoutInflater.from(pr.getContext()).inflate(R.layout.item_mhsod100u, pr, false);
            Map<String, Object> item = orderItems.get(p);
            
            ((TextView) v.findViewById(R.id.tvItemName)).setText(getStringVal(item, "itemnm"));
            EditText etQty = v.findViewById(R.id.etQuantity);
            EditText etPrice = v.findViewById(R.id.etPrice);
            TextView tvAmt = v.findViewById(R.id.etAmount);
            TextView tvVat = v.findViewById(R.id.etVat);

            double qty = parseToDouble(item.get("ordqty"));
            double price = parseToDouble(item.get("price"));
            
            // 🚀 실제 서버에서 내려온 공급가와 부가세를 우선 표시 (없을 경우에만 계산)
            double amt = item.get("ordamt") != null ? parseToDouble(item.get("ordamt")) : Math.floor(qty * price);
            double vat = item.get("ordvat") != null ? parseToDouble(item.get("ordvat")) : Math.floor(amt * 0.1);

            etQty.setText(String.valueOf(qty));
            etPrice.setText(String.valueOf(price));
            tvAmt.setText(String.valueOf((long)amt)); // EditText이므로 콤마 없이 설정 (필요시 df.format)
            tvVat.setText(String.valueOf((long)vat));

            if (getStringVal(item, "_status").equals("삭제")) v.setBackgroundColor(Color.LTGRAY);
            else v.setBackgroundColor(Color.WHITE);

            View.OnFocusChangeListener listener = (view, focus) -> {
                if (!focus) {
                    try {
                        double nQty = parseToDouble(etQty.getText().toString());
                        double nPrice = parseToDouble(etPrice.getText().toString());
                        double nAmt = parseToDouble(tvAmt.getText().toString());
                        double nVat = parseToDouble(tvVat.getText().toString());
                        
                        item.put("ordqty", nQty);
                        item.put("price", nPrice);
                        
                        // 사용자가 직접 수정한 경우 계산 로직 (또는 그대로 유지)
                        if (view == etQty || view == etPrice) {
                            nAmt = Math.floor(nQty * nPrice);
                            nVat = Math.floor(nAmt * 0.1);
                        }
                        
                        item.put("ordamt", nAmt);
                        item.put("ordvat", nVat);
                        
                        if (getStringVal(item, "_status").isEmpty()) item.put("_status", "수정");
                        notifyDataSetChanged();
                    } catch (Exception ignored) {}
                }
            };
            etQty.setOnFocusChangeListener(listener);
            etPrice.setOnFocusChangeListener(listener);
            tvAmt.setOnFocusChangeListener(listener);
            tvVat.setOnFocusChangeListener(listener);

            v.findViewById(R.id.btnDelete).setOnClickListener(view -> {
                if (getStringVal(item, "_status").equals("입력")) orderItems.remove(p);
                else item.put("_status", getStringVal(item, "_status").equals("삭제") ? "" : "삭제");
                notifyDataSetChanged();
            });
            return v;
        }
    }
}
