package com.crmbank.erp.mobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
 * 🚀 [InboundRegisterActivity] 모바일 통합 바코드 처리 (iogbn 100: 입고, 200: 출고 완벽 통합)
 * 1. orderno: 입고 시 발주번호(balno) / 출고 시 주문번호(ordno) 통합 매핑
 * 2. iogbn: 100(입고) -> 200(출고) 변경만으로 입/출고 전체 프로세스 자동 전환 지원
 */
public class InboundRegisterActivity extends AppCompatActivity {

    private Spinner spWarehouse;
    private TextView tvInboundDate;
    private EditText etOrderNo, etCustomerName, etInboundNo, etRemark;
    private ListView lvRegisterList;
    private Button btnSave;
    private ArrayAdapter<CodeDto> warehouseAdapter;
    private InboundRegisterAdapter listAdapter;
    private final List<Map<String, Object>> detailList = new ArrayList<>();
    private ApiService apiService;

    private String cmpycd = "COIT";
    private String userid = "";
    private String iogbnMode = "100"; // 💡 100: 입고, 200: 출고 (Intent로 "200" 전환 지원)
    private String currentIoym = "";
    private String currentIono = "";
    private String selectedItemCode = "";
    private String selectedItemName = "";

    // 🎯 바코드 스캔 결과 런처
    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String scannedValue = result.getData().getStringExtra("BARCODE_VALUE");
                    handleScannedBarcode(scannedValue);
                }
            });

    // 🎯 카메라 권한 요청 런처
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openBarcodeScanner();
                else Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbound_register);

        // 💡 [핵심] Intent 모드 파라미터 수신 (100: 입고, 200: 출고)
        if (getIntent() != null && getIntent().hasExtra("IOGBN_MODE")) {
            iogbnMode = getIntent().getStringExtra("IOGBN_MODE");
            if (iogbnMode == null || iogbnMode.trim().isEmpty()) iogbnMode = "100";
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        spWarehouse = findViewById(R.id.spWarehouse);
        tvInboundDate = findViewById(R.id.tvInboundDate);
        ImageButton btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        Button btnReset = findViewById(R.id.btnReset);
        etOrderNo = findViewById(R.id.etOrderNo);
        etInboundNo = findViewById(R.id.etInboundNo);
        etCustomerName = findViewById(R.id.etCustomerName);
        etRemark = findViewById(R.id.etRemark);
        lvRegisterList = findViewById(R.id.lvRegisterList);

        // UI 모드 설정 (100: 입고 vs 200: 출고)
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("200".equals(iogbnMode) ? "바코드 출고 처리" : "바코드 입고 처리");
        }

        listAdapter = new InboundRegisterAdapter(this, detailList);
        lvRegisterList.setAdapter(listAdapter);

        // 💡 [선택한 품목 전용 스캔] 터치한 품목 행만 스캔 허용하여 오스캔 100% 방지
        lvRegisterList.setOnItemClickListener((parent, view, position, id) -> {
            if (detailList.size() > position) {
                Map<String, Object> item = detailList.get(position);
                selectedItemCode = getStringValue(item, "itemcd");
                if (selectedItemCode.isEmpty()) selectedItemCode = getStringValue(item, "barcode");
                selectedItemName = getStringValue(item, "itemnm");
                Toast.makeText(this, "스캔 지정 품목: [" + (selectedItemName.isEmpty() ? selectedItemCode : selectedItemName) + "]", Toast.LENGTH_SHORT).show();
            }
        });

        setupWarehouseSpinner();
        setupInboundDatePicker();
        resetFields();

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openBarcodeScanner();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            });
        }

        findViewById(R.id.btnSearchOrder).setOnClickListener(v -> {
            String val = etInboundNo.getText().toString().trim().replace("-", "");
            if (val.length() >= 10) {
                currentIoym = val.substring(0, 6);
                currentIono = val.substring(6);
                loadScannedHistory(currentIoym, currentIono);
            } else {
                showInboundOrderSearchDialog();
            }
        });

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetFields());
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveInboundReceive());
        }
    }

    private void openBarcodeScanner() {
        Intent intent = new Intent(this, BarcodeScanActivity.class);
        if (!currentIoym.isEmpty() && !currentIono.isEmpty()) {
            intent.putExtra("HEADER_BARCODE", currentIoym + currentIono);
        }
        if (!selectedItemCode.isEmpty()) {
            intent.putExtra("TARGET_ITEM_CODE", selectedItemCode);
        }
        barcodeLauncher.launch(intent);
    }

    /** 🚀 [통합 바코드 스캔 핸들러] 1. 의뢰번호 바코드 vs 2. 품목/시리얼 바코드 판별 */
    private void handleScannedBarcode(String value) {
        if (value == null || value.trim().isEmpty()) return;
        final String cleanValue = value.trim();
        String rawVal = cleanValue.replace("-", "");

        // 💡 Case A: 입고/출고 의뢰서 상단 바코드 스캔 (의뢰번호가 아직 미선택 상태일 때만!)
        if ((currentIoym.isEmpty() || currentIono.isEmpty()) && rawVal.length() == 10 && rawVal.startsWith("202")) {
            currentIoym = rawVal.substring(0, 6);
            currentIono = rawVal.substring(6);
            etInboundNo.setText(String.format(Locale.getDefault(), "%s-%s", currentIoym, currentIono));
            loadScannedHistory(currentIoym, currentIono);
            Toast.makeText(this, String.format(Locale.getDefault(), "%s [%s-%s] 선택 완료", ("200".equals(iogbnMode) ? "출고의뢰건" : "입고의뢰건"), currentIoym, currentIono), Toast.LENGTH_SHORT).show();
            return;
        }

        // 💡 Case B: 품목/시리얼 바코드 스캔 (의뢰번호 선택 완료 후 품목 바코드 100% 저장)
        if (currentIoym.isEmpty() || currentIono.isEmpty()) {
            Toast.makeText(this, ("200".equals(iogbnMode) ? "출고의뢰서" : "입고의뢰서") + " 상단 바코드를 먼저 스캔하세요.", Toast.LENGTH_LONG).show();
            return;
        }

        // 🎯 HSIO104T_TBL 품목/시리얼 바코드 실시간 DB 저장 실행!
        saveBarcodeScanToTable(cleanValue);
    }

    /** 🚀 [HSIO104T_TBL 실시간 저장] iogbnMode(100:입고 / 200:출고) 통합 반영 */
    private void saveBarcodeScanToTable(String scannedBarcode) {
        // 💡 스캔한 바코드가 의뢰 상세 품목(itemcd 또는 barcode)과 매칭되는지 정밀 교정
        String matchedItemCd = scannedBarcode;
        for (Map<String, Object> item : detailList) {
            String cd = getStringValue(item, "itemcd");
            String bc = getStringValue(item, "barcode");
            if (scannedBarcode.equalsIgnoreCase(cd) || scannedBarcode.equalsIgnoreCase(bc)) {
                matchedItemCd = cd;
                break;
            }
        }

        Map<String, Object> param = new HashMap<>();
        param.put("cmpycd", cmpycd);
        param.put("iogbn", iogbnMode); // 100: 입고, 200: 출고
        param.put("ioym", currentIoym);
        param.put("iono", currentIono);
        String srowNo = String.format(Locale.getDefault(), "%03d", detailList.size() + 1);
        param.put("srowno", srowNo);
        param.put("itemcd", matchedItemCd);
        param.put("barcode", scannedBarcode);

        if ("200".equals(iogbnMode)) {
            // 💡 출고 모드 (iogbn = 200): 신규 LOT 채번 없음! 입고 시 발행된 기존 LOT 번호 스캔 연결!
            param.put("lotno", scannedBarcode);
        } else {
            // 💡 입고 모드 (iogbn = 100): IOYM + IONO + SROWNO + 일련번호(6) 유일 시리얼/LOT 번호 자동 채번
            String seq6 = String.format(Locale.getDefault(), "%06d", detailList.size() + 1);
            String autoLotNo = String.format(Locale.getDefault(), "%s%s%s%s", currentIoym, currentIono, srowNo, seq6);
            param.put("lotno", scannedBarcode.isEmpty() ? autoLotNo : scannedBarcode);
            if (scannedBarcode.isEmpty()) param.put("barcode", autoLotNo);
        }
        param.put("scan_qty", 1.0);
        param.put("orderno", etOrderNo.getText().toString().trim()); // 💡 통합: 입고=발주번호(balno), 출고=주문번호(ordno)
        param.put("updemp", userid);

        apiService.saveBarcodeScanHistory(param).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    // 💡 [실시간 수량 실시각 표출] 스캔 성공 즉시 메모리 상의 detailList 해당 품목 스캔수량 +1 즉각 증가
                    for (Map<String, Object> item : detailList) {
                        String cd = getStringValue(item, "itemcd");
                        String bc = getStringValue(item, "barcode");
                        if (scannedBarcode.equalsIgnoreCase(cd) || scannedBarcode.equalsIgnoreCase(bc)) {
                            double cur = 0;
                            try { cur = Double.parseDouble(String.valueOf(item.get("scan_qty"))); } catch (Exception ignored) {}
                            item.put("scan_qty", cur + 1.0);
                            break;
                        }
                    }
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();

                    Toast.makeText(InboundRegisterActivity.this, "바코드 [" + scannedBarcode + "] 스캔 완료!", Toast.LENGTH_SHORT).show();
                    loadScannedHistory(currentIoym, currentIono);
                } else {
                    Toast.makeText(InboundRegisterActivity.this, "스캔 저장 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(InboundRegisterActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 🚀 [의뢰 마스터/품목 & 스캔 이력 통합 조회] */
    private void loadScannedHistory(String ioym, String iono) {
        String proc = "200".equals(iogbnMode) ? "HSIO_620S_STR" : "HSIO_215S_STR";

        // 💡 1. 마스터 정보 조회 (거래처명, 입고일자, 비고 자동 채우기)
        Map<String, Object> pMst = new HashMap<>();
        pMst.put("actkind", "S1");
        pMst.put("cmpycd", cmpycd);
        pMst.put("iogbn", iogbnMode);
        pMst.put("ioym", ioym);
        pMst.put("iono", iono);
        pMst.put("whcd", "000");

        apiService.executeHsioProcedure(proc, pMst).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> m = response.body().get(0);
                    String custNm = getStringValue(m, "custnm");
                    if (custNm.isEmpty()) custNm = getStringValue(m, "ccustnm");
                    if (custNm.isEmpty()) custNm = getStringValue(m, "cust_nm");
                    etCustomerName.setText(custNm);

                    String ymd = getStringValue(m, "ioymd");
                    if (ymd.length() == 8) {
                        tvInboundDate.setText(String.format(Locale.getDefault(), "%s-%s-%s", ymd.substring(0, 4), ymd.substring(4, 6), ymd.substring(6, 8)));
                    }
                    if (m.get("remark") != null) etRemark.setText(getStringValue(m, "remark"));
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });

        // 💡 2. 의뢰 상세 품목 목록 조회 및 HSIO104T_TBL 스캔 수량 실시간 병합
        Map<String, Object> pDtl = new HashMap<>(pMst);
        pDtl.put("actkind", "S0");

        apiService.executeHsioProcedure(proc, pDtl).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    detailList.clear();

                    // 💡 [스캔 필수 품목 집중 로드] autoyn == 'Y' 필수 스캔 품목만 필터링하여 목록 표출
                    for (Map<String, Object> item : response.body()) {
                        String autoYn = getStringValue(item, "autoyn");
                        if (autoYn.isEmpty()) autoYn = getStringValue(item, "AUTOYN");
                        if ("Y".equalsIgnoreCase(autoYn)) {
                            detailList.add(item);
                        }
                    }
                    // 만약 autoyn = 'Y' 품목이 별도로 없으면 전체 품목 로드
                    if (detailList.isEmpty()) {
                        detailList.addAll(response.body());
                    }

                    // 거래처명 보정
                    if (etCustomerName.getText().toString().trim().isEmpty()) {
                        Map<String, Object> firstItem = response.body().get(0);
                        String cNm = getStringValue(firstItem, "custnm");
                        if (cNm.isEmpty()) cNm = getStringValue(firstItem, "ccustnm");
                        if (cNm.isEmpty()) cNm = getStringValue(firstItem, "cust_nm");
                        if (!cNm.isEmpty()) etCustomerName.setText(cNm);
                    }

                    // 💡 3. HSIO104T_TBL 실스캔 수량 조회하여 detailList에 합산 병합
                    Map<String, Object> pScan = new HashMap<>();
                    pScan.put("cmpycd", cmpycd);
                    pScan.put("iogbn", iogbnMode);
                    pScan.put("ioym", ioym);
                    pScan.put("iono", iono);

                    apiService.getBarcodeScanHistory(pScan).enqueue(new Callback<List<Map<String, Object>>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Map<String, Object>>> call2, @NonNull Response<List<Map<String, Object>>> response2) {
                            if (response2.isSuccessful() && response2.body() != null) {
                                Map<String, Double> scanMap = new HashMap<>();
                                for (Map<String, Object> scanRow : response2.body()) {
                                    String cd = getStringValue(scanRow, "itemcd").toUpperCase(Locale.ROOT).trim();
                                    String bc = getStringValue(scanRow, "barcode").toUpperCase(Locale.ROOT).trim();
                                    double qty = 0;
                                    try { qty = Double.parseDouble(String.valueOf(scanRow.get("scan_qty"))); } catch (Exception ignored) {}
                                    if (qty <= 0) qty = 1.0;

                                    if (!cd.isEmpty()) {
                                        Double valObj = scanMap.get(cd);
                                        scanMap.put(cd, (valObj != null ? valObj : 0.0) + qty);
                                    }
                                    if (!bc.isEmpty() && !bc.equals(cd)) {
                                        Double valObj = scanMap.get(bc);
                                        scanMap.put(bc, (valObj != null ? valObj : 0.0) + qty);
                                    }
                                }

                                for (Map<String, Object> item : detailList) {
                                    String cd = getStringValue(item, "itemcd").toUpperCase(Locale.ROOT).trim();
                                    String bc = getStringValue(item, "barcode").toUpperCase(Locale.ROOT).trim();
                                    Double qtyByCd = !cd.isEmpty() ? scanMap.get(cd) : null;
                                    Double qtyByBc = !bc.isEmpty() ? scanMap.get(bc) : null;
                                    double scannedTotal = qtyByCd != null ? qtyByCd : (qtyByBc != null ? qtyByBc : 0.0);
                                    item.put("scan_qty", scannedTotal);
                                    item.put("SCAN_QTY", scannedTotal);
                                }
                            }
                            listAdapter.notifyDataSetChanged();
                        }
                        @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call2, @NonNull Throwable t2) {
                            listAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    /** 🚀 [스캔 검수 완료 처리] HSIO104T_TBL 실시간 스캔 저장 내역 확정 완료 */
    private void saveInboundReceive() {
        if (currentIoym.isEmpty() || currentIono.isEmpty() || detailList.isEmpty()) {
            Toast.makeText(this, "스캔 내역이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalScanQty = 0.0;
        int scannedCount = 0;
        for (Map<String, Object> item : detailList) {
            Object scanQtyObj = item.get("scan_qty");
            double scanQty = 0.0;
            if (scanQtyObj != null) {
                try { scanQty = Double.parseDouble(String.valueOf(scanQtyObj)); } catch (Exception ignored) {}
            }
            if (scanQty > 0) {
                totalScanQty += scanQty;
                scannedCount++;
            }
        }

        if (scannedCount == 0) {
            Toast.makeText(this, "스캔된 품목 수량이 없습니다. 바코드를 먼저 스캔하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, String.format(Locale.getDefault(), "%s 완료! (총 %d품목, 스캔수량 %.0f개)",
                ("200".equals(iogbnMode) ? "바코드 출고검수" : "바코드 입고검수"), scannedCount, totalScanQty), Toast.LENGTH_SHORT).show();

        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setAlpha(0.5f);
        }
    }

    /** 🚀 [종료 시 미저장 경고 다이얼로그] HSIO104T_TBL 안전 보존 안내 및 재조회 일괄저장 연동 지원 */
    @Override
    public void onBackPressed() {
        double totalScanQty = 0.0;
        for (Map<String, Object> item : detailList) {
            Object obj = item.get("scan_qty");
            if (obj != null) {
                try { totalScanQty += Double.parseDouble(String.valueOf(obj)); } catch (Exception ignored) {}
            }
        }

        if (totalScanQty > 0 && btnSave != null && btnSave.isEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ 미저장 스캔 내역 안내")
                    .setMessage(String.format(Locale.getDefault(),
                            "아직 [%s]을 진행하지 않은 스캔 내역이 있습니다. (현재 스캔 합계: %.0f개)\n\n지금 저장하지 않고 종료하더라도 이미 스캔하신 내역은 HSIO104T_TBL에 안전하게 보존됩니다.\n나중에 의뢰번호를 다시 조회하여 언제든지 일괄 저장하실 수 있습니다.\n\n종료하시겠습니까?",
                            ("200".equals(iogbnMode) ? "일괄 출고저장" : "일괄 입고저장"), totalScanQty))
                    .setPositiveButton("스캔 계속 진행", (dialog, which) -> dialog.dismiss())
                    .setNegativeButton("나중에 일괄저장 (종료)", (dialog, which) -> finish())
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void resetFields() {
        if (etOrderNo != null) etOrderNo.setText("");
        if (etInboundNo != null) etInboundNo.setText("");
        if (etCustomerName != null) etCustomerName.setText("");
        if (etRemark != null) etRemark.setText("");
        if (tvInboundDate != null) tvInboundDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        currentIoym = ""; currentIono = "";
        detailList.clear();
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(Locale.ROOT));
        return val != null ? String.valueOf(val).trim() : "";
    }

    /** 🚀 [입고/출고 의뢰번호 검색 팝업 다이얼로그] */
    private void showInboundOrderSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_purch_order_search, null);
        String title = "200".equals(iogbnMode) ? "출고의뢰 번호 검색" : "입고의뢰 번호 검색";
        builder.setTitle(title).setView(dialogView);

        TextView tvPopStartDate = dialogView.findViewById(R.id.tvPopStartDate);
        TextView tvPopEndDate = dialogView.findViewById(R.id.tvPopEndDate);
        EditText etPopCustNm = dialogView.findViewById(R.id.etPopCustNm);
        Button btnPopSearch = dialogView.findViewById(R.id.btnPopSearch);
        ListView lv = dialogView.findViewById(R.id.lvPopOrderList);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (tvPopEndDate != null) tvPopEndDate.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        if (tvPopStartDate != null) tvPopStartDate.setText(sdf.format(cal.getTime()));

        List<Map<String, Object>> popList = new ArrayList<>();
        InboundOrderPopAdapter popAdapter = new InboundOrderPopAdapter(popList);
        if (lv != null) lv.setAdapter(popAdapter);

        AlertDialog dialog = builder.create();

        if (btnPopSearch != null) {
            btnPopSearch.setOnClickListener(v -> {
                String proc = "200".equals(iogbnMode) ? "HSIO_620S_STR" : "HSIO_215S_STR";
                Map<String, Object> p = new HashMap<>();
                p.put("actkind", "S1");
                p.put("cmpycd", cmpycd);
                p.put("iogbn", iogbnMode);
                if (tvPopStartDate != null) p.put("fromdt", tvPopStartDate.getText().toString().replace("-", ""));
                if (tvPopEndDate != null) p.put("todt", tvPopEndDate.getText().toString().replace("-", ""));
                if (etPopCustNm != null) p.put("custnm", etPopCustNm.getText().toString().trim());

                apiService.executeHsioProcedure(proc, p).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            popList.clear();
                            popList.addAll(response.body());
                            popAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
                });
            });
        }

        if (lv != null) {
            lv.setOnItemClickListener((parent, view, position, id) -> {
                Map<String, Object> selected = popList.get(position);
                String ym = getStringValue(selected, "ioym");
                String no = getStringValue(selected, "iono");
                if (!ym.isEmpty() && !no.isEmpty()) {
                    currentIoym = ym;
                    currentIono = no;
                    etInboundNo.setText(String.format(Locale.getDefault(), "%s-%s", currentIoym, currentIono));
                    dialog.dismiss();
                    loadScannedHistory(currentIoym, currentIono);
                }
            });
        }

        View btnClose = dialogView.findViewById(R.id.btnPopClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (btnPopSearch != null) btnPopSearch.performClick();
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(Locale.ROOT));
        return val != null ? String.valueOf(val).trim() : "";
    }

    private void setupWarehouseSpinner() {
        warehouseAdapter = new ArrayAdapter<>(this, R.layout.item_popup_list, new ArrayList<>());
        if (spWarehouse != null) spWarehouse.setAdapter(warehouseAdapter);
        apiService.getCommonCode(cmpycd, "KOR", "030").enqueue(new Callback<List<CodeDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<CodeDto>> call, @NonNull Response<List<CodeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    warehouseAdapter.addAll(response.body());
                    warehouseAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<List<CodeDto>> call, @NonNull Throwable t) {}
        });
    }

    private void setupInboundDatePicker() {
        if (tvInboundDate != null) {
            tvInboundDate.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(this, (view, y, m, d) -> 
                    tvInboundDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)), 
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            });
        }
    }

    private class InboundOrderPopAdapter extends BaseAdapter {
        private final List<Map<String, Object>> items;
        public InboundOrderPopAdapter(List<Map<String, Object>> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int p) { return items.get(p); }
        @Override public long getItemId(int p) { return p; }
        @Override public View getView(int p, View v, ViewGroup pr) {
            View rowView = v;
            if (rowView == null) rowView = LayoutInflater.from(pr.getContext()).inflate(R.layout.item_request_search, pr, false);
            Map<String, Object> i = items.get(p);
            String cNm = getStringVal(i, "custnm");
            if (cNm.isEmpty()) cNm = getStringVal(i, "ccustnm");
            String ymd = getStringVal(i, "ioymd");
            String num = String.format(Locale.getDefault(), "%s-%s", getStringVal(i, "ioym"), getStringVal(i, "iono"));

            TextView tvDept = rowView.findViewById(R.id.tvPopDeptNm);
            TextView tvYmd = rowView.findViewById(R.id.tvPopReqYmd);
            TextView tvNo = rowView.findViewById(R.id.tvPopReqNo);

            if (tvDept != null) tvDept.setText(cNm);
            if (tvYmd != null) tvYmd.setText(ymd);
            if (tvNo != null) tvNo.setText(num);

            return rowView;
        }
    }
}
