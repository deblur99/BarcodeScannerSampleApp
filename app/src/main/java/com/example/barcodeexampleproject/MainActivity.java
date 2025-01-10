package com.example.barcodeexampleproject;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {
    // 카메라 상태 관리
   private ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
        setupViews();

        if (queryCameraPermission() && checkCamera2APISupported()) {
            QRAndBarcodeAnalyzer.getInstance().setContext(this);
            setupCameraWithAnalyzer();
        }
    }

    private void setupActivity() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupViews() {
        TextView scannedBarcodeIdTextView = findViewById(R.id.scanned_barcode_id_text_view);
        Button cameraToggleButton = findViewById(R.id.camera_toggle_button);
        Button clearButton = findViewById(R.id.clear_button);

        scannedBarcodeIdTextView.setText("QR코드 또는 바코드를 스캔하세요");
        cameraToggleButton.setOnClickListener(l -> {
            if (cameraToggleButton.getText().toString().equals("카메라 끄기")) {
                stopCamera();
            } else {
                setupCameraWithAnalyzer();
            }
        });
        clearButton.setOnClickListener(l -> {
            scannedBarcodeIdTextView.setText("QR코드 또는 바코드를 스캔하세요");
        });
    }

    private boolean checkCamera2APISupported() {
        // Camera2 API 지원 여부 확인
        // CameraX는 기본적으로 Camera2 API를 기반으로 동작하므로 Android 5.0(Lollipop, API 21) 이상에서 작동해야 한다.
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                // Camera2 대신 CameraX 사용할 것이므로 INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY 체크
                if (level != null && level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    Log.d("CameraCheck", "Device supports only LEGACY Camera2 API.");
                } else {
                    Log.d("CameraCheck", "Device supports FULL or LIMITED Camera2 API.");
                }
                return true;
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean queryCameraPermission() {
        int CAMERA_PERMISSION_REQUEST_CODE = 1001;

        // 카메라 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    private void setupCameraWithAnalyzer() {
        PreviewView previewView = findViewById(R.id.camera_preview_view);
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        // 카메라 시작 시 배경색 초기화
//        previewView.setBackgroundColor(Color.TRANSPARENT);

        cameraProviderListenableFuture.addListener(() -> {
            try {
                // cameraProvider를 클래스 변수에 할당
                cameraProvider = cameraProviderListenableFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), QRAndBarcodeAnalyzer.getInstance());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();  // 기존 바인딩 해제
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                Button button = findViewById(R.id.camera_toggle_button);
                button.setText("카메라 끄기");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 카메라 중지
    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();

            // PreviewView를 찾아서 검은색 배경으로 설정
//            PreviewView previewView = findViewById(R.id.camera_preview_view);
//            previewView.setBackgroundColor(Color.BLACK);

            // Surface를 제거하여 카메라 프리뷰를 완전히 정리
//            previewView.removeAllViews();

            Button button = findViewById(R.id.camera_toggle_button);
            button.setText("카메라 켜기");
        }
    }
}