package com.example.barcodeexampleproject;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class QRAndBarcodeAnalyzer implements ImageAnalysis.Analyzer {
    private static QRAndBarcodeAnalyzer instance;
    public Context currentContext;

    private QRAndBarcodeAnalyzer() {}

    public static QRAndBarcodeAnalyzer getInstance() {
        if (instance == null) {
            instance = new QRAndBarcodeAnalyzer();
        }
        return instance;
    }

    synchronized public void setContext(Context context) {
        currentContext = context;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        try {
            // 카메라에서 가져오는 ImageProxy 객체로부터 이미지 가져오기
            Image mediaImage = imageProxy.getImage();
            if (mediaImage == null) {
                return;
            }

            // 회전 각도 반영한 이미지로 변환하고, BarcodeScanner 가져오기
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            BarcodeScanner scanner = BarcodeScanning.getClient();

            // 바코드 이미지 인식 처리
            Task<List<Barcode>> processed = scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (barcodes.isEmpty()) {
                            imageProxy.close();
                            return;
                        }

                        Barcode lastBarcode = barcodes.get(barcodes.size() - 1);
                        // 안드로이드의 Context 객체는 액티비티 객체로 타입캐스팅이 가능함
                        if (lastBarcode != null && currentContext instanceof MainActivity) {
                            ((MainActivity) currentContext).runOnUiThread(() -> {
                                // 맨 마지막으로 인식한 바코드의 raw string을 텍스트뷰에 적용
                                TextView barcodeTextView = ((MainActivity) currentContext).findViewById(R.id.scanned_barcode_id_text_view);
                                barcodeTextView.setText(lastBarcode.getRawValue());
                            });
                        }

                        // CameraX API를 사용하고 있으므로 ImageProxy 객체를 닫는다.
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}