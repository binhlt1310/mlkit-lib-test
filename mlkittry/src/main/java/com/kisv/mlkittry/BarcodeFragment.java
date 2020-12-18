package com.kisv.mlkittry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeFragment extends Fragment {

    private final int CAMERA_REQUEST_CODE = 1;

    private PreviewView previewView;
    private TextView noPermissionMessage;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalyzer;
    private Preview preview;

    private ExecutorService cameraExecutor;

    public static BarcodeFragment newInstance() {
        return new BarcodeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewView = view.findViewById(R.id.camera_preview);
        noPermissionMessage = view.findViewById(R.id.noPermissionMessage);
        cameraExecutor = Executors.newSingleThreadExecutor();
        if (hasCameraPermission()) {
            previewView.setVisibility(View.VISIBLE);
            noPermissionMessage.setVisibility(View.GONE);
            bindCameraUsecases();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            previewView.setVisibility(View.VISIBLE);
            noPermissionMessage.setVisibility(View.GONE);
            bindCameraUsecases();
        } else {
            previewView.setVisibility(View.GONE);
            noPermissionMessage.setVisibility(View.VISIBLE);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void bindCameraUsecases() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                imageAnalyzer = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(860, 732))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .setTargetRotation(rotation)
                        .build();
                imageAnalyzer.setAnalyzer(cameraExecutor, new BarcodeProcessor(codeValue ->
                        Toast.makeText(requireContext(), codeValue, Toast.LENGTH_LONG).show()
                ));
                preview = new Preview.Builder()
//                        .setTargetRotation(rotation)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, imageAnalyzer, preview);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }
}