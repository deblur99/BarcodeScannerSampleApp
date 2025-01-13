package com.example.barcodeexampleproject.fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.barcodeexampleproject.QRAndBarcodeAnalyzer;
import com.example.barcodeexampleproject.R;
import com.example.barcodeexampleproject.databinding.FragmentCodeAnalysisBinding;
import com.google.common.util.concurrent.ListenableFuture;

public class CodeAnalysisFragment extends Fragment {
    private FragmentCodeAnalysisBinding binding;

    // 카메라 상태 관리
    private Preview preview;
    private ProcessCameraProvider cameraProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (queryCameraPermission() && checkCamera2APISupported()) {
            QRAndBarcodeAnalyzer.getInstance().setContext(this.getContext());
            startCamera();
        }
    }

    // Fragment 서브뷰가 생성되는 시점의 생명주기 메서드
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCodeAnalysisBinding.inflate(inflater, container, false);
        setupViews();
        return binding.getRoot();   // 바인딩 객체의 최상위 계층을 리턴
    }

    private void setupViews() {
        binding.scannedBarcodeIdTextView.setText("QR코드 또는 바코드를 스캔하세요");
        binding.cameraToggleButton.setOnClickListener(l -> {
            if (binding.cameraToggleButton.getText().toString().equals("카메라 끄기")) {
                stopCamera();
            } else {
                startCamera();
            }
        });
        binding.clearButton.setOnClickListener(l -> {
            binding.scannedBarcodeIdTextView.setText("QR코드 또는 바코드를 스캔하세요");
        });
    }

    private boolean checkCamera2APISupported() {
        // Camera2 API 지원 여부 확인
        // CameraX는 기본적으로 Camera2 API를 기반으로 동작하므로 Android 5.0(Lollipop, API 21) 이상에서 작동해야 한다.
        // - Fragment에서는 requireContext()로 액티비티의 컨텍스트를 가져오고, 여기에 getSystemService()를 호출해야 한다.
        CameraManager cameraManager = (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
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
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this.getContext());
        cameraProviderListenableFuture.addListener(() -> {
            try {
                // cameraProvider를 클래스 변수에 할당
                cameraProvider = cameraProviderListenableFuture.get();
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.cameraPreviewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this.getContext()), QRAndBarcodeAnalyzer.getInstance());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();  // 기존 바인딩 해제
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                binding.cameraToggleButton.setText("카메라 끄기");
                binding.overlayView.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this.getContext()));
    }

    // 카메라 중지
    private void stopCamera() {
        if (preview == null || cameraProvider == null) {
            return;
        }
        cameraProvider.unbindAll();
        preview.setSurfaceProvider(null);

        // 프리뷰 자체에는 배경화면 설정하는 기능이 사실상 없음
        // - 따라서 카메라 바인딩, surface 해제하고, 그 위에 투명하게 겹쳐진 검은 화면 뷰를 불투명하게 해서 프리뷰를 가려야 한다.
        binding.overlayView.setVisibility(View.VISIBLE);
        binding.cameraToggleButton.setText("카메라 켜기");
    }
}