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
import android.widget.Button;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🚀 [BarcodeScanActivity] 실시간 카메라 + 갤러리/PDF 문서 바코드 고속 스캐너
 */
public class BarcodeScanActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScan";
    private PreviewView previewView;
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
        Button btnClose = findViewById(R.id.btnClose);
        Button btnGallery = findViewById(R.id.btnGallery);

        // 스캐너 설정 (모든 필수 바코드 규격 수용)
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_CODE_39)
                .build();
        scanner = BarcodeScanning.getClient(options);

        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();

        // 앱 실행 시 다운로드 폴더 미디어 라이브러리 갱신
        triggerMediaScan();

        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {"image/*", "application/pdf"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                galleryLauncher.launch(Intent.createChooser(intent, "바코드 파일 선택"));
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
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera Error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) return;
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        scanImage(image, imageProxy);
    }

    private void handleSelectedUri(Uri uri) {
        try {
            String type = getContentResolver().getType(uri);
            if (type == null) {
                String path = uri.toString().toLowerCase();
                if (path.contains(".pdf")) type = "application/pdf";
            }

            if (type != null && type.contains("pdf")) {
                Bitmap bitmap = pdfToBitmap(uri);
                if (bitmap != null) {
                    scanImage(InputImage.fromBitmap(bitmap, 0), null);
                }
            } else {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                scanImage(InputImage.fromBitmap(bitmap, 0), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "File load error", e);
            Toast.makeText(this, "파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap pdfToBitmap(Uri pdfUri) throws IOException {
        ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
        if (fileDescriptor == null) return null;
        PdfRenderer renderer = new PdfRenderer(fileDescriptor);
        if (renderer.getPageCount() > 0) {
            PdfRenderer.Page page = renderer.openPage(0);
            // 바코드 인식을 위해 해상도를 5배 상향
            float scale = 5f;
            Bitmap bitmap = Bitmap.createBitmap((int)(page.getWidth() * scale), (int)(page.getHeight() * scale), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            renderer.close();
            fileDescriptor.close();
            return bitmap;
        }
        renderer.close();
        fileDescriptor.close();
        return null;
    }

    private void scanImage(InputImage image, ImageProxy imageProxy) {
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes != null && !barcodes.isEmpty()) {
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();
                            if (value != null && !value.trim().isEmpty()) {
                                Intent intent = new Intent();
                                intent.putExtra("BARCODE_VALUE", value.trim());
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                                return;
                            }
                        }
                    } else {
                        if (imageProxy == null) {
                            Toast.makeText(this, "바코드를 인식하지 못했습니다. 더 선명한 자료를 선택해 주세요.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (imageProxy == null) Toast.makeText(this, "분석 오류 발생", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
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
