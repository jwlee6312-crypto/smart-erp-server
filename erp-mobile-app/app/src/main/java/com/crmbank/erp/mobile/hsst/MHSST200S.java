package com.crmbank.erp.mobile.hsst;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.hsio.MHSIO650S;
import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.RetrofitClient;
import com.crmbank.erp.mobile.CodeDto;
import com.crmbank.erp.mobile.PopupAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

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

public class MHSST200S extends BaseActivity {

    private static final String TAG = "InventoryStatus";
    private TextView tvBaseDate;
    private Spinner spWarehouse;
    private EditText etItemNm;
    private ListView lvInventoryStatus;
    private ArrayAdapter<CodeDto> warehouseAdapter;
    private InventoryAdapter adapter;
    private List<InventoryItem> inventoryItems;
    private String cmpycd = "";
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhsst200s);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        apiService = RetrofitClient.getApiService();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "coit").trim();

        tvBaseDate = findViewById(R.id.tvBaseDate);
        spWarehouse = findViewById(R.id.spWarehouse);
        etItemNm = findViewById(R.id.etItemNm);
        lvInventoryStatus = findViewById(R.id.lvInventoryStatus);
        Button btnSearch = findViewById(R.id.btnSearch);

        setupDatePicker();
        etItemNm.setFocusable(false);
        etItemNm.setOnClickListener(v -> showItemSearchDialog());

        setupWarehouseSpinner();

        inventoryItems = new ArrayList<>();
        adapter = new InventoryAdapter();
        lvInventoryStatus.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            if (validateInputs()) {
                searchInventory();
            }
        });

        // 🚀 수불 현황 화면으로 이동 (MHSIO650S)
        lvInventoryStatus.setOnItemClickListener((parent, view, position, id) -> {
            InventoryItem item = inventoryItems.get(position);
            CodeDto wh = (CodeDto) spWarehouse.getSelectedItem();
            
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String todt = sdf.format(cal.getTime());
            
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String fromdt = sdf.format(cal.getTime());

            Intent intent = new Intent(this, MHSIO650S.class);
            intent.putExtra("itemcd", item.itemcd);
            intent.putExtra("itemnm", item.itemNm);
            intent.putExtra("whcd", wh.getCodecd());
            intent.putExtra("fromdt", fromdt);
            intent.putExtra("todt", todt);
            intent.putExtra("astkind", "120");
            startActivity(intent);
        });
    }

    private void setupDatePicker() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvBaseDate.setText(sdf.format(cal.getTime()));

        tvBaseDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                tvBaseDate.setText(selectedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private boolean validateInputs() {
        if (spWarehouse.getSelectedItem() == null) {
            Toast.makeText(this, "창고를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupWarehouseSpinner() {
        warehouseAdapter = new ArrayAdapter<CodeDto>(this, R.layout.item_spinner, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                if (getItem(position) != null) tv.setText(getItem(position).getCodenm());
                return tv;
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                if (getItem(position) != null) {
                    tv.setText(getItem(position).getCodenm());
                    tv.setTextColor(Color.BLACK);
                    tv.setBackgroundColor(Color.WHITE);
                }
                return tv;
            }
        };
        spWarehouse.setAdapter(warehouseAdapter);

        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "W0");
        p.put("cmpycd", cmpycd);

        apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CodeDto> codes = new ArrayList<>();

                    // 🚀 '전체' 옵션 추가
                    CodeDto all = new CodeDto();
                    all.codecd = "000";
                    all.codenm = "전체";
                    codes.add(all);

                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringValue(m, "whcd");
                        dto.codenm = getStringValue(m, "whnm");
                        codes.add(dto);
                    }
                    warehouseAdapter.addAll(codes);
                    warehouseAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "창고 로드 실패: " + t.getMessage());
            }
        });
    }

    private void showItemSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_customer_search, null);
        builder.setView(dialogView);
        
        String titleText = "품목 검색";
        builder.setTitle(titleText);

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText(titleText);

        EditText etSearch = dialogView.findViewById(R.id.etSearchQuery);
        RecyclerView rv = dialogView.findViewById(R.id.rvPopupList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        List<Map<String, Object>> list = new ArrayList<>();
        AlertDialog dialog = builder.create();

        PopupAdapter popupAdapter = new PopupAdapter(list, "ITEM", item -> {
            etItemNm.setText(getStringValue(item, "itemnm"));
            dialog.dismiss();
            searchInventory(); // 🚀 선택 즉시 조회
        });
        rv.setAdapter(popupAdapter);

        dialogView.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Map<String, Object> p = new HashMap<>();
            p.put("cmpycd", cmpycd); 
            String keyword = etSearch != null ? etSearch.getText().toString().trim() : "";
            
            p.put("gubun", "I1"); 
            p.put("gbncd", "2"); 
            p.put("code", ""); 
            p.put("codenm", keyword); 
            p.put("etcval", "");
            
            apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> c, @NonNull Response<List<Map<String, Object>>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        list.clear(); list.addAll(r.body()); popupAdapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> c, @NonNull Throwable t) {}
            });
        });

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        dialogView.findViewById(R.id.btnSearch).performClick();
    }

    private void searchInventory() {
        String baseDate = tvBaseDate.getText().toString().replace("-", "");
        CodeDto selectedWh = (CodeDto) spWarehouse.getSelectedItem();
        if (selectedWh == null) {
            Toast.makeText(this, "창고를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("ymd", baseDate);
        params.put("whcd", selectedWh.getCodecd());
        params.put("astkind", "120");
        params.put("itemnm", etItemNm.getText().toString().trim());

        Log.d(TAG, "🔍 조회 파라미터: " + params);

        apiService.executeHsstProcedure("HSST_200S_STR", params).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> list = response.body();
                    inventoryItems.clear();
                    if (list.isEmpty()) {
                        Toast.makeText(MHSST200S.this, "조회된 자료가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (Map<String, Object> map : list) {
                            inventoryItems.add(new InventoryItem(
                                    getStringValue(map, "itemcd"),
                                    getStringValue(map, "itemnm"),
                                    parseToDouble(map.get("stock")),
                                    parseToDouble(map.get("stkqty")),
                                    parseToDouble(map.get("stkamt"))
                            ));
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    String errorMsg = "조회 실패 (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(MHSST200S.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                String errorMsg = "네트워크 오류: " + t.getMessage();
                Toast.makeText(MHSST200S.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorMsg, t);
            }
        });
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val) : "";
    }

    private double parseToDouble(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static class InventoryItem {
        String itemcd, itemNm;
        double properQty, stockQty, stockAmt;

        public InventoryItem(String itemcd, String itemNm, double properQty, double stockQty, double stockAmt) {
            this.itemcd = itemcd;
            this.itemNm = itemNm;
            this.properQty = properQty;
            this.stockQty = stockQty;
            this.stockAmt = stockAmt;
        }
    }

    private class InventoryAdapter extends BaseAdapter {
        @Override public int getCount() { return inventoryItems.size(); }
        @Override public Object getItem(int pos) { return inventoryItems.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MHSST200S.this).inflate(R.layout.item_mhsst200s, parent, false);
            }
            InventoryItem item = inventoryItems.get(position);

            ((TextView) convertView.findViewById(R.id.tvItemNm)).setText(item.itemNm);
            ((TextView) convertView.findViewById(R.id.tvProperQty)).setText(String.format(Locale.getDefault(), "%,d", (int)item.properQty));
            ((TextView) convertView.findViewById(R.id.tvStockQty)).setText(String.format(Locale.getDefault(), "%,d", (int)item.stockQty));
            ((TextView) convertView.findViewById(R.id.tvStockAmt)).setText(String.format(Locale.getDefault(), "%,d", (int)item.stockAmt));

            return convertView;
        }
    }

    @Override protected String getProgramTitle() { return "창고재고현황"; }
    @Override protected String getProgramId() { return "MHSST200S"; }
}
