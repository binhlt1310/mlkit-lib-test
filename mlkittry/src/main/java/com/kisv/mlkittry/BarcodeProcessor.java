package com.kisv.mlkittry;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

public class BarcodeProcessor implements ImageAnalysis.Analyzer {

    private OnDetectSuccessListener listener;

    ObjectDetector objectDetector;

    public BarcodeProcessor(OnDetectSuccessListener listener) {
        this.listener = listener;
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableClassification()  // Optional
                        .build();
        objectDetector = ObjectDetection.getClient(options);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
//            BarcodeScanner scanner = BarcodeScanning.getClient();
            objectDetector.process(inputImage)
                    .addOnSuccessListener(results -> {
//                       if (barcodes.size() > 0) {
//                           StringBuilder rawValue = new StringBuilder();
//                           for (Barcode barcode : barcodes) {
//                               rawValue.append(barcode.getRawValue());
//                           }
//                           listener.callback(rawValue.toString());
//                       }
                        for (DetectedObject detectedObject : results) {
                            Rect boundingBox = detectedObject.getBoundingBox();
                            Integer trackingId = detectedObject.getTrackingId();
                            listener.callback(detectedObject.getBoundingBox().toString());
                            for (DetectedObject.Label label : detectedObject.getLabels()) {
                                String text = label.getText();
                                if (PredefinedCategory.FOOD.equals(text)) {
                                }
                                int index = label.getIndex();
                                if (PredefinedCategory.FOOD_INDEX == index) {
                                }
                                float confidence = label.getConfidence();
                            }
                        }
                    })
                    .addOnFailureListener(exception -> {
                    })
                    .addOnCompleteListener(results -> image.close());
        }
    }

    public interface OnDetectSuccessListener {
        void callback(String codeValue);
    }
}
