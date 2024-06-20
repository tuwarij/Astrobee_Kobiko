package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

public class YourService extends KiboRpcService {

    private final String TAG = this.getClass().getSimpleName();

    private final String[] TEMPLATE_FILE_NAME = {
            "beaker.png",
            "goggle.png",
            "hammer.png",
            "kapton_tape.png",
            "pipette.png",
            "screwdriver.png",
            "thermometer.png",
            "top.png",
            "watch.png",
            "wrench.png"
    };

    private final String[] TEMPLATE_NAME = {
            "beaker",
            "top",
            "goggle",
            "hammer",
            "wrench",
            "kapton_tape",
            "thermometer",
            "watch",
            "pipette",
            "screwdriver"
    };

    private Interpreter tflite;
    private int areaID = 1;


    @Override
    protected void runPlan1() {
        Log.i(TAG, "start mission");
        Mat[] t = TemplateLoading();

        // Initialize TensorFlow Lite interpreter
        try {
            tflite = new Interpreter(loadModelFile("model.tflite"));
            logOutputTensorShapes(tflite);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing TensorFlow Lite model.", e);
        }

        // The mission starts.
        api.startMission();

        // Move to a point.
        Point point = new Point(10.9d, -9.92284d, 5.195d);
        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(point, quaternion, false);

        // step in x axis 20cm and reset rotation
        point = new Point(11.1d, -9.92284d, 5.195d);
        api.moveTo(point, quaternion, true);
        api.flashlightControlFront(0.03f);

        // Get a camera image.
        Mat image1 = api.getMatNavCam();
        image1 = CameraCalibration(image1);
        api.saveMatImage(image1, "undistorted_image1.png");
        crop(image1, t);
        areaID++;
        api.flashlightControlFront(0.0f);

//        detectObject(image1, 1, 300, 50, 0, 0);

        // Run TensorFlow Lite on image1
//        processImageWithTFLite(image1, 1);

        // step to 2nd area and rotate to the ceiling
        point = new Point(11.1d, -9.155d, 5.195d);
        Quaternion quaternion_lookUpInXAxis = new Quaternion(0f,  0.707f, 0f,  0.707f);
        api.moveTo(point, quaternion_lookUpInXAxis, true);

        // move up to 2nd image
        point = new Point(11.1d, -8.955d, 4.5d);
        api.moveTo(point, quaternion_lookUpInXAxis, true);
        api.flashlightControlFront(0.03f);

        // Get a camera image.
        Mat image2 = api.getMatNavCam();
        image2 = CameraCalibration(image2);
        api.saveMatImage(image2, "undistorted_image2.png");
        crop(image2, t);
        areaID++;
        api.flashlightControlFront(0.0f);

//        detectObject(image2, 2, 300, 250, 0, 0);

        // Run TensorFlow Lite on image2
        processImageWithTFLite(image2, 2);

        // move to 3rd image
        point = new Point(11.1d, -8.055d, 4.5d);
        api.moveTo(point, quaternion_lookUpInXAxis, true);
        api.flashlightControlFront(0.03f);

        // Get a camera image.
        Mat image3 = api.getMatNavCam();
        image3 = CameraCalibration(image3);
        api.saveMatImage(image3, "undistorted_image3.png");
        crop(image3, t);
        areaID++;
        api.flashlightControlFront(0.0f);

//        detectObject(image3, 3, 300, 0, 0, 0);

        // Run TensorFlow Lite on image3
        processImageWithTFLite(image3, 3);

        // move to 4th area by move x
        point = new Point(10.7d, -8.055d, 4.5d);
        quaternion = new Quaternion(0f,  0f, 0f,  0f);
        api.moveTo(point, quaternion, true);

        // move to 4th image by move Y
        point = new Point(10.7d, -7.055d, 4.8d);
        quaternion = new Quaternion(0f,  0f, 1f,  0f);
        api.moveTo(point, quaternion, true);
        api.flashlightControlFront(0.03f);

        // Get a camera image.
        Mat image4 = api.getMatNavCam();
        image4 = CameraCalibration(image4);
        api.saveMatImage(image4, "undistorted_image4.png");
        crop(image4, t);
        areaID++;
        api.flashlightControlFront(0.0f);

//        detectObject(image4, 4, 0, 0, 0, 0);

        // Run TensorFlow Lite on image4
        processImageWithTFLite(image4, 4);

        // move to astronaut
        point = new Point(11.143, -6.7607, 4.9654);
        quaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);
        api.moveTo(point, quaternion, true);

        // Report the rounding completion.
        api.reportRoundingCompletion();

        // Notify recognition of item.
        api.notifyRecognitionItem();

        // Take a snapshot of the target item.
        api.takeTargetItemSnapshot();
    }

    @Override
    protected void runPlan2() {
        // write your plan 2 here.
    }

    @Override
    protected void runPlan3() {
        // write your plan 3 here.
    }

    private ByteBuffer loadModelFile(String modelPath) throws IOException {
        try {
            AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd(modelPath);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (FileNotFoundException e) {
            InputStream inputStream = getApplicationContext().getAssets().open(modelPath);
            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            ByteBuffer buffer = ByteBuffer.allocateDirect(modelBytes.length).order(ByteOrder.nativeOrder());
            buffer.put(modelBytes);
            buffer.rewind(); // Ensure the buffer is ready to be read
            return buffer;
        }
    }

    private void processImageWithTFLite(Mat image, int imageNumber) {
        int modelInputSize = 320;
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap, modelInputSize);

        // Update these based on actual tensor shape
        int outputBoxSize = 100 * 4; // 100 boxes with 4 coordinates each
        int outputLabelSize = 100; // 100 class labels
        int outputScoreSize = 100; // 100 confidence scores

        final ByteBuffer outputBoxes = ByteBuffer.allocateDirect(outputBoxSize * 4).order(ByteOrder.nativeOrder());
        final ByteBuffer outputLabels = ByteBuffer.allocateDirect(outputLabelSize * 4).order(ByteOrder.nativeOrder());
        final ByteBuffer outputScores = ByteBuffer.allocateDirect(outputScoreSize * 4).order(ByteOrder.nativeOrder());

        tflite.runForMultipleInputsOutputs(new Object[]{inputBuffer}, new HashMap<Integer, Object>() {{
            put(0, outputBoxes);
            put(1, outputLabels);
            put(2, outputScores);
        }});

        float[] boxes = new float[outputBoxSize];
        float[] labels = new float[outputLabelSize];
        float[] scores = new float[outputScoreSize];

        outputBoxes.rewind();
        outputBoxes.asFloatBuffer().get(boxes);
        outputLabels.rewind();
        outputLabels.asFloatBuffer().get(labels);
        outputScores.rewind();
        outputScores.asFloatBuffer().get(scores);

        Map<String, Integer> detectedItemsCount = new HashMap<>();

        for (int i = 0; i < boxes.length / 4; i++) {
            if (scores[i] > 0.5) {
                int labelIndex = (int) labels[i];
                String detectedLabel = TEMPLATE_NAME[labelIndex];
                Log.i(TAG, "Detected " + detectedLabel + " with confidence " + scores[i]);
                detectedItemsCount.put(detectedLabel, detectedItemsCount.getOrDefault(detectedLabel, 0) + 1);
            }
        }

        // Report each detected item and its count
        for (Map.Entry<String, Integer> entry : detectedItemsCount.entrySet()) {
            String itemName = entry.getKey();
            int quantityOfThatItem = entry.getValue();
            api.setAreaInfo(imageNumber, itemName, quantityOfThatItem);
        }
    }

    private void logOutputTensorShapes(Interpreter interpreter) {
        for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
            int[] shape = interpreter.getOutputTensor(i).shape();
            int numElements = 1;
            for (int dim : shape) {
                numElements *= dim;
            }
            int bytesPerElement = interpreter.getOutputTensor(i).dataType() == DataType.FLOAT32 ? 4 : 2; // Assuming only float or half-float types
            Log.i(TAG, "Output tensor " + i + ": shape=" + Arrays.toString(shape) + ", numElements=" + numElements + ", bytes=" + (numElements * bytesPerElement));
        }
    }

    private Mat CameraCalibration(Mat image){
        Mat undistortImg = new Mat();
        try {
            //Camera Calibration
            Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
            double[] intrinsics = api.getNavCamIntrinsics()[0];
            if (intrinsics != null && intrinsics.length == 9) {
                cameraMatrix.put(0, 0, intrinsics);
            } else {
                Log.e(TAG, "Invalid camera matrix intrinsics");
                return image;
            }

            Mat cameraCoefficients = new Mat(1, 5, CvType.CV_64F);
            double[] coefficients = api.getNavCamIntrinsics()[1];
            if (coefficients != null && coefficients.length == 5) {
                cameraCoefficients.put(0, 0, coefficients);
            } else {
                Log.e(TAG, "Invalid camera coefficients");
                return image;
            }

            Calib3d.undistort(image, undistortImg, cameraMatrix, cameraCoefficients);
            Log.i(TAG, "Camera Calibrated");

        }catch (Exception e) {
            Log.e(TAG, "Error during camera calibration", e);
            return image;
        }
        return undistortImg;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int inputSize) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4).order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.f);
                byteBuffer.putFloat((val & 0xFF) / 255.f);
            }
        }
        return byteBuffer;
    }

    private Mat[] TemplateLoading(){
        // Template loading
        Mat[] templates = new Mat[TEMPLATE_FILE_NAME.length];
        for(int i = 0; i < TEMPLATE_FILE_NAME.length; i++){
            try{
                InputStream inputStream = getAssets().open(TEMPLATE_FILE_NAME[i]);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);

                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

                templates[i] = mat;

                inputStream.close();
                Log.i(TAG, "Successfully loaded template: " + TEMPLATE_FILE_NAME[i]);
            } catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "Failed to load template: " + TEMPLATE_FILE_NAME[i], e);
            }
        }
        return templates;
    }

    public void crop(Mat image, Mat[] templates) {
        try {
            // Marker Detection using Aruco
            Mat undistortImg = CameraCalibration(image);
            Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
            List<Mat> corners = new ArrayList<>(); // To store corners
            Mat markerIds = new Mat(); // To store detected marker IDs
            Aruco.detectMarkers(undistortImg, dictionary, corners, markerIds);

            if (!markerIds.empty()) {
                Log.i(TAG, "Marker Detected");

                // Find bounding box of detected markers
                Rect boundingBox = null;
                for (Mat corner : corners) {
                    MatOfPoint matOfPoint = new MatOfPoint();
                    corner.convertTo(matOfPoint, CvType.CV_32S);
                    Rect rect = Imgproc.boundingRect(matOfPoint);

                    if (boundingBox == null) {
                        boundingBox = rect;
                    } else {
                        boundingBox = unionRect(boundingBox, rect);
                    }
                }

                if (boundingBox != null) {
                    int margin = 200;
                    int x = Math.max(0, boundingBox.x - margin);
                    int y = Math.max(0, boundingBox.y - margin);
                    int w = Math.min(undistortImg.width() - x, boundingBox.width + 2 * margin);
                    int h = Math.min(undistortImg.height() - y, boundingBox.height + 2 * margin);

                    // Crop the image
                    Mat croppedImage = new Mat(undistortImg, new Rect(x, y, w, h));

                    // Save the result
                    String croppedFilename = "cropped_image_" + System.currentTimeMillis() + ".png";
                    api.saveMatImage(croppedImage, croppedFilename);
                    Log.i(TAG, "Cropped image saved as: " + croppedFilename);

                    Log.i(TAG, "Image Cropped");
//                    detectObject(croppedImage, templates);
                    processImageWithTFLite(croppedImage, areaID);
                } else {
                    Log.e(TAG, "No bounding box found");
                }
            } else {
                Log.e(TAG, "No markers detected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during cropping", e);
        }
    }

    private Rect unionRect(Rect r1, Rect r2) {
        int x = Math.min(r1.x, r2.x);
        int y = Math.min(r1.y, r2.y);
        int width = Math.max(r1.x + r1.width, r2.x + r2.width) - x;
        int height = Math.max(r1.y + r1.height, r2.y + r2.height) - y;
        return new Rect(x, y, width, height);
    }

//    private void detectObject(Mat croppedImage, Mat[] templates) {
//        SIFT sift = SIFT.create();
//        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
//
//        Mat descriptorsCropped = new Mat();
//        MatOfKeyPoint keypointsCropped = new MatOfKeyPoint();
//
//        // ตรวจจับและคำนวณคุณลักษณะสำหรับภาพที่ครอบ
//        sift.detectAndCompute(croppedImage, new Mat(), keypointsCropped, descriptorsCropped);
//
//        String bestTemplate = "None";
//        int bestMatchCount = 0;
//
//        for (int i = 0; i < templates.length; i++) {
//            Mat descriptorsTemplate = new Mat();
//            MatOfKeyPoint keypointsTemplate = new MatOfKeyPoint();
//
//            // ตรวจจับและคำนวณคุณลักษณะสำหรับเทมเพลต
//            sift.detectAndCompute(templates[i], new Mat(), keypointsTemplate, descriptorsTemplate);
//
//            // ใช้ KNN สำหรับการจับคู่คุณลักษณะ
//            List<MatOfDMatch> knnMatches = new ArrayList<>();
//            matcher.knnMatch(descriptorsTemplate, descriptorsCropped, knnMatches, 2);
//
//            int goodMatches = 0;
//            double ratioThresh = 0.35; // Adjust the ratio threshold
//
//            // ใช้การกรองคู่ที่ดีด้วย ratio test
//            for (MatOfDMatch matOfDMatch : knnMatches) {
//                DMatch[] dmatchArray = matOfDMatch.toArray();
//                if (dmatchArray.length >= 2) {
//                    if (dmatchArray[0].distance < ratioThresh * dmatchArray[1].distance) {
//                        goodMatches++;
//                    }
//                }
//            }
//
//            if (goodMatches > bestMatchCount) {
//                bestMatchCount = goodMatches;
//                bestTemplate = TEMPLATE_NAME[i];
//            }
//
//            Log.i(TAG, "Template: " + TEMPLATE_NAME[i] + ", Good Matches: " + goodMatches);
//        }
//
//        Log.i(TAG, "Matched Template: " + bestTemplate + ", Count: " + bestMatchCount);
//
//        // รายงานการจับคู่ไปยัง API
//        api.setAreaInfo(areaID, bestTemplate, bestMatchCount);
//    }
}