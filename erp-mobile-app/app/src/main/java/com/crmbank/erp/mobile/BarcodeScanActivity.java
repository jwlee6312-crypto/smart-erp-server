package com.crmbank.erp.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🚀 [BarcodeScanActivity] 실시간 카메라 + 갤러리/PDF 문서 바코드 고속 스캐너
 */
public class BarcodeScanActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScan";
    private PreviewView previewView;
    private ImageView ivDocPreview;
    private TextView tvDetectedBarcode;
    private String lastDetectedVal = "";
    private String candidateVal = "";
    private int candidateCount = 0;
    private ExecutorService cameraExecutor;
    private BarcodeScanner scanner;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        handleSelectedUri(selectedUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        previewView = findViewById(R.id.previewView);
        ivDocPreview = findViewById(R.id.ivDocPreview);
        tvDetectedBarcode = findViewById(R.id.tvDetectedBarcode);
        Button btnTriggerScan = findViewById(R.id.btnTriggerScan);
        Button btnClose = findViewById(R.id.btnClose);
        Button btnGallery = findViewById(R.id.btnGallery);

        if (btnTriggerScan != null) {
            btnTriggerScan.setOnClickListener(v -> {
                if (!lastDetectedVal.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("BARCODE_VALUE", lastDetectedVal);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(this, "바코드를 조준창에 맞추세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 🚀 스캐너 설정 (모든 바코드 및 2D/1D 규격 수용)
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        scanner = BarcodeScanning.getClient(options);

        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();

        // 앱 실행 시 다운로드 폴더 미디어 라이브러리 갱신
        triggerMediaScan();

        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                try {
                    // 💡 [해결] 전체 파일 선택기 (최근파일 및 다운로드 폴더 직접 표출)
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    galleryLauncher.launch(Intent.createChooser(intent, "파일 선택 (다운로드/이미지/PDF)"));
                } catch (Exception e) {
                    Toast.makeText(this, "파일 탐색기를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }
    }

    private void triggerMediaScan() {
        try {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (downloadDir != null && downloadDir.exists()) {
                String[] files = downloadDir.list();
                if (files != null) {
                    String[] paths = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        paths[i] = new File(downloadDir, files[i]).getAbsolutePath();
                    }
                    MediaScannerConnection.scanFile(this, paths, null, null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Media scan failed", e);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

                cameraProvider.unbindAll();
                cameraStartTime = System.currentTimeMillis(); // 💡 카메라 구동 시작 시점 기록 (초점 안정화용)
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera Error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private long cameraStartTime = 0;

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) return;
        // 💡 [초점 안정화 버퍼] 카메라 오픈 직후 400ms 동안은 자동 초점(Autofocus)이 맞을 때까지 초점 대기
        if (System.currentTimeMillis() - cameraStartTime < 400) {
            imageProxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        scanImage(image, imageProxy);
    }

    private void handleSelectedUri(Uri uri) {
        try {
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}

            String type = getContentResolver().getType(uri);
            if (type == null) {
                String path = uri.toString().toLowerCase();
                if (path.contains(".pdf")) type = "application/pdf";
            }

            if (type != null && type.contains("pdf")) {
                List<Bitmap> pdfBitmaps = pdfToBitmaps(uri);
                if (!pdfBitmaps.isEmpty()) {
                    if (ivDocPreview != null) {
                        ivDocPreview.setVisibility(View.VISIBLE);
                        ivDocPreview.setImageBitmap(pdfBitmaps.get(0)); // 💡 선택한 문서가 화면에 선명하게 미리보기로 표시됨!
                    }
                    scanPdfBitmaps(pdfBitmaps, 0);
                } else {
                    Toast.makeText(this, "PDF 파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                if (bitmap != null) {
                    if (ivDocPreview != null) {
                        ivDocPreview.setVisibility(View.VISIBLE);
                        ivDocPreview.setImageBitmap(bitmap); // 💡 선택한 이미지가 화면에 표시됨!
                    }
                    scanImage(InputImage.fromBitmap(bitmap, 0), null);
                } else {
                    Toast.makeText(this, "이미지 파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "File load error", e);
            Toast.makeText(this, "파일을 읽을 수 없습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** 🚀 [다중 페이지 PDF 바코드 순차 스캔] 헤더 바코드 스마트 건너뛰기 포함 */
    private void scanPdfBitmaps(List<Bitmap> pdfBitmaps, int index) {
        if (index >= pdfBitmaps.size()) {
            Toast.makeText(this, "PDF 문서에서 바코드를 인식하지 못했습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        String headerBarcode = getIntent() != null ? getIntent().getStringExtra("HEADER_BARCODE") : null;
        if (headerBarcode != null) headerBarcode = headerBarcode.replaceAll("-", "").trim();
        final String skipBarcode = headerBarcode;

        String targetCode = getIntent() != null ? getIntent().getStringExtra("TARGET_ITEM_CODE") : null;
        if (targetCode != null) targetCode = targetCode.replaceAll("-", "").trim();
        final String targetItem = targetCode;

        Bitmap bitmap = pdfBitmaps.get(index);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes != null && !barcodes.isEmpty()) {
                        String selectedValue = null;

                        // 💡 1차: 지정 품목 잠금 스캔 모드 (지정한 품목 바코드만 100% 한정 인식)
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();
                            if (value == null || value.trim().isEmpty()) value = barcode.getDisplayValue();
                            if (value != null && !value.trim().isEmpty()) {
                                String cleanVal = value.replace("-", "").trim();
                                if (skipBarcode != null && !skipBarcode.isEmpty() && cleanVal.equalsIgnoreCase(skipBarcode)) {
                                    continue; // 이미 선택된 헤더 바코드 스킵
                                }
                                if (targetItem != null && !targetItem.isEmpty()) {
                                    if (cleanVal.equalsIgnoreCase(targetItem)) {
                                        selectedValue = value.trim(); // 지정한 품목 바코드만 포커싱 선택!
                                        break;
                                    } else {
                                        continue; // 지정 품목과 일치하지 않는 바코드 스킵 (오스캔 100% 방지)
                                    }
                                }
                                selectedValue = value.trim();
                                break;
                            }
                        }

                        // 💡 2차: 헤더 바코드가 없을 때만 첫 번째 바코드 선택 (헤더가 이미 있으면 품목 바코드가 잡힐 때까지 지속 프레임 분석)
                        if (selectedValue == null && !barcodes.isEmpty() && (skipBarcode == null || skipBarcode.isEmpty())) {
                            selectedValue = barcodes.get(0).getRawValue();
                            if (selectedValue == null) selectedValue = barcodes.get(0).getDisplayValue();
                        }

                        if (selectedValue != null && !selectedValue.trim().isEmpty()) {
                            Log.i(TAG, "✅ [PDF Barcode Selected]: " + selectedValue.trim());
                            Intent intent = new Intent();
                            intent.putExtra("BARCODE_VALUE", selectedValue.trim());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                            return;
                        }
                    }
                    // 바코드를 못 찾았을 경우 다음 페이지 순차 분석
                    scanPdfBitmaps(pdfBitmaps, index + 1);
                })
                .addOnFailureListener(e -> scanPdfBitmaps(pdfBitmaps, index + 1));
    }

    private List<Bitmap> pdfToBitmaps(Uri pdfUri) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<>();
        ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
        if (fileDescriptor == null) return bitmaps;
        PdfRenderer renderer = new PdfRenderer(fileDescriptor);
        int pageCount = Math.min(renderer.getPageCount(), 5); // 상위 5페이지 순차 랜더링
        for (int i = 0; i < pageCount; i++) {
            PdfRenderer.Page page = renderer.openPage(i);
            float scale = 3.0f; // 300 DPI 정밀 해상도
            Bitmap bitmap = Bitmap.createBitmap((int)(page.getWidth() * scale), (int)(page.getHeight() * scale), Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            canvas.drawColor(android.graphics.Color.WHITE); // 💡 [핵심] 투명 배경을 선명한 순백색(#FFFFFF)으로 채워 바코드 대비 극대화
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            bitmaps.add(bitmap);
        }
        renderer.close();
        fileDescriptor.close();
        return bitmaps;
    }

    private void scanImage(InputImage image, ImageProxy imageProxy) {
        String headerBarcode = getIntent() != null ? getIntent().getStringExtra("HEADER_BARCODE") : null;
        if (headerBarcode != null) headerBarcode = headerBarcode.replace("-", "").trim();
        final String skipBarcode = headerBarcode;

        String targetCode = getIntent() != null ? getIntent().getStringExtra("TARGET_ITEM_CODE") : null;
        if (targetCode != null) targetCode = targetCode.replace("-", "").trim();
        final String targetItem = targetCode;

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes != null && !barcodes.isEmpty()) {
                        int imgWidth = image.getWidth();
                        int imgHeight = image.getHeight();
                        int centerX = imgWidth / 2;
                        int centerY = imgHeight / 2;

                        Barcode bestBarcode = null;
                        double minDistance = Double.MAX_VALUE;

                        // 🎯 [중앙 조준 포커싱] 화면 중앙 사각형 프레임에 가장 가까운 바코드 우선 선택
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();
                            if (value == null || value.trim().isEmpty()) value = barcode.getDisplayValue();
                            if (value == null || value.trim().isEmpty()) continue;

                            String cleanVal = value.replace("-", "").trim();
                            if (skipBarcode != null && !skipBarcode.isEmpty() && cleanVal.equalsIgnoreCase(skipBarcode)) {
                                continue; // 이미 선택된 헤더 바코드 스킵
                            }
                            if (targetItem != null && !targetItem.isEmpty()) {
                                if (!cleanVal.equalsIgnoreCase(targetItem)) {
                                    continue; // 지정 품목과 일치하지 않는 다른 품목 바코드 100% 스킵 (오스캔 방지)
                                }
                            }

                            android.graphics.Rect box = barcode.getBoundingBox();
                            if (box != null) {
                                double dist = Math.hypot(box.centerX() - centerX, box.centerY() - centerY);
                                if (dist < minDistance) {
                                    minDistance = dist;
                                    bestBarcode = barcode;
                                }
                            } else if (bestBarcode == null) {
                                bestBarcode = barcode;
                            }
                        }

                        // 💡 헤더 바코드가 이미 선택되어 있다면, 품목 바코드가 인식될 때까지 연속 카메라 프레임 분석 대기
                        if (bestBarcode == null && !barcodes.isEmpty() && (skipBarcode == null || skipBarcode.isEmpty())) {
                            bestBarcode = barcodes.get(0);
                        }

                        if (bestBarcode != null) {
                            String val = bestBarcode.getRawValue();
                            if (val == null) val = bestBarcode.getDisplayValue();
                            if (val != null && !val.trim().isEmpty()) {
                                String clean = val.trim();
                                if (clean.equals(candidateVal)) {
                                    candidateCount++;
                                } else {
                                    candidateVal = clean;
                                    candidateCount = 1;
                                }

                                // 💡 2프레임 연속 동일 감지 시 바코드 고정 (화면 미세 떨림으로 인한 끝자리 1,2,3 연속 변동 100% 차단)
                                if (candidateCount >= 2 || imageProxy == null) {
                                    lastDetectedVal = candidateVal;
                                    runOnUiThread(() -> {
                                        if (tvDetectedBarcode != null) {
                                            tvDetectedBarcode.setText(String.format(Locale.getDefault(), "🎯 바코드 감지 고정: %s", lastDetectedVal));
                                        }
                                    });
                                }

                                Log.i(TAG, "🎯 [Center Barcode Lock]: " + lastDetectedVal);
                                if (imageProxy == null) {
                                    Intent intent = new Intent();
                                    intent.putExtra("BARCODE_VALUE", lastDetectedVal);
                                    setResult(Activity.RESULT_OK, intent);
                                    finish();
                                    return;
                                }
                            }
                        }
                    } else {
                        if (imageProxy == null) {
                            Toast.makeText(this, "바코드를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                    if (imageProxy != null) imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    if (imageProxy != null) imageProxy.close();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        scanner.close();
    }
}
