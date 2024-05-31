package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log; // Log

import org.opencv.core.Rect;
import org.opencv.core.Size;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService; // providing the API to interact with the Astrobee robot.

import gov.nasa.arc.astrobee.types.Point; // coordinates
import gov.nasa.arc.astrobee.types.Quaternion; // orientations

import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat; //  Matrix data type from OpenCV for handling images
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
            "goggle",
            "hammer",
            "kapton_tape",
            "pipette",
            "screwdriver",
            "thermometer",
            "top",
            "watch",
            "wrench"
    };

    private int areaId = 1;

    @Override
    protected void runPlan1(){
        api.startMission(); // start the mission
        Log.i(TAG, "Start mission");
        Mat[] t = TemplateLoading();

        // Move to a point.
        Point point = new Point(10.9d, -9.92284d, 5.195d);
        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(point, quaternion, true);
        Log.i(TAG, "Moved0.1");

        /// step in x axis 20cm and reset rotation
        point = new Point(11.1d, -9.92284d, 5.195d);
        api.moveTo(point, quaternion, true);
        Log.i(TAG, "Moved0.2");

        // Get a camera image.
        Mat image0 = api.getMatNavCam();
        api.saveMatImage(image0, "file_name0.png");
        Log.i(TAG, "Captured and saved image0");


        crop(image0, t);
        areaId++;
        Log.i(TAG, "next is area2");

        // step to 2nd area and rotate to the ceiling
        point = new Point(11.1d, -9.155d, 5.195d);
        Quaternion quaternion_lookUpInXAxis = new Quaternion(0f,  0.707f, 0f,  0.707f);
        api.moveTo(point, quaternion_lookUpInXAxis, true);
        Log.i(TAG, "Moved1.1");

        // move up to 2nd image
        point = new Point(11.1d, -9.155d, 4.5d);
        api.moveTo(point, quaternion_lookUpInXAxis,true);
        Log.i(TAG, "Moved1.2");

        // Get a camera image.
        Mat image1 = api.getMatNavCam();
        api.saveMatImage(image1, "file_name1.png");
        Log.i(TAG, "Captured and saved image1");

        crop(image1, t);
        areaId++;
        Log.i(TAG, "next is area3");

        // move to 3rd image
        point = new Point(11.1d, -8.055d, 4.5d);
        quaternion_lookUpInXAxis = new Quaternion(0.271f,  0.653f, 0.271f,  0.653f);
        api.moveTo(point, quaternion_lookUpInXAxis, true);
        Log.i(TAG, "Moved2.1");

        // Get a camera image.
        Mat image2 = api.getMatNavCam();
        // Save the image
        api.saveMatImage(image2, "file_name2.png");
        Log.i(TAG, "Captured and saved image2");

        crop(image1, t);
        areaId++;
        Log.i(TAG, "next is area3");

        // move to 4th area by move x
        point = new Point(10.7d, -8.055d, 4.5d);
        quaternion = new Quaternion(0f,  0f, 0f,  0f);
        api.moveTo(point, quaternion, true);
        Log.i(TAG, "Moved3.1");

        // move to 4th image by move Y
        point = new Point(10.7d, -7.055d, 4.7d);
        quaternion = new Quaternion(0f,  0f, 1f,  0f);
        api.moveTo(point, quaternion, true);
        Log.i(TAG, "Moved3.2");


        // Get a camera image.
        Mat image3 = api.getMatNavCam();
        // Save the image
        api.saveMatImage(image3, "file_name3.png");
        Log.i(TAG, "Captured and saved image3");

        crop(image3, t);

        // move to astronaut
//        point = new Point(11.143, -6.7607, 4.9654);
//        quaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);
//        api.moveTo(point, quaternion, true);

        //When you move to the front of the astronaut, report the rounding completion.
        api.reportRoundingCompletion();

//        /* ********************************************************** */
//        /* Write your code to recognize which item the astronaut has. */
//        /* ********************************************************** */

        //Let's notify the astronaut when you recognize it.
        api.notifyRecognitionItem();

        //Take a snapshot of the target item.
        api.takeTargetItemSnapshot();
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

    private Mat CameraCalibration(Mat image){
        //Camera Calibration
        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0,api.getNavCamIntrinsics()[0]);

        Mat cameraCoefficients = new Mat(1, 5, CvType.CV_64F);
        cameraCoefficients.put(0, 0,api.getNavCamIntrinsics()[1]);

        Mat undistortImg = new Mat();
        Calib3d.undistort(image, undistortImg, cameraMatrix, cameraCoefficients);
        Log.i(TAG, "Camera Calibrated");

        return undistortImg;
    }

    private void crop(Mat image, Mat[] templates){
        //Marker Detection
        Mat undistortImg = CameraCalibration(image);
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
        List<Mat> corners = new ArrayList<>(); // To store corners
        Mat markerIds = new Mat(); // To store detected marker IDs
        Aruco.detectMarkers(undistortImg, dictionary, corners, markerIds);
        Log.i(TAG, "Marker Detected");

        // Crop Image
        List<Mat> croppedImages = new ArrayList<>();
        for (Mat corner : corners) {
            if (corner.rows() > 0) {
                // Assuming corner is a 4x2 matrix
                double[] topLeft = corner.get(0, 0);
                double[] topRight = corner.get(0, 1);
                double[] bottomRight = corner.get(0, 2);
                double[] bottomLeft = corner.get(0, 3);

                // Adding
                topLeft[0] -= 50;
                topLeft[1] -= 50;

                topRight[0] += 50;
                topRight[1] -= 50;

                bottomRight[0] += 50;
                bottomRight[1] += 50;

                bottomLeft[0] -= 50;
                bottomLeft[1] += 50;

                // Ensure coordinates are within image boundaries
                topLeft[0] = Math.max(0, Math.min(topLeft[0], image.cols() - 1));
                topLeft[1] = Math.max(0, Math.min(topLeft[1], image.rows() - 1));
                topRight[0] = Math.max(0, Math.min(topRight[0], image.cols() - 1));
                topRight[1] = Math.max(0, Math.min(topRight[1], image.rows() - 1));
                bottomRight[0] = Math.max(0, Math.min(bottomRight[0], image.cols() - 1));
                bottomRight[1] = Math.max(0, Math.min(bottomRight[1], image.rows() - 1));
                bottomLeft[0] = Math.max(0, Math.min(bottomLeft[0], image.cols() - 1));
                bottomLeft[1] = Math.max(0, Math.min(bottomLeft[1], image.rows() - 1));

                // Define the rectangle for cropping
                int x = (int) Math.min(topLeft[0], bottomLeft[0]);
                int y = (int) Math.min(topLeft[1], topRight[1]);
                int width = (int) Math.abs(topRight[0] - topLeft[0]);
                int height = (int) Math.abs(bottomLeft[1] - topLeft[1]);

                // Crop the image
                Rect roi = new Rect(x, y, width, height);
                Mat croppedImage = new Mat(image, roi);


                // Save the cropped image
                String filename = "cropped_image_" + System.currentTimeMillis() + ".png";
                api.saveMatImage(croppedImage, filename);
                Log.i(TAG, "Cropped image saved as: " + filename);
                croppedImages.add(croppedImage);
            }
        }

        Log.i(TAG, "Image Cropped");
        detectObject(croppedImages, templates);
    }

    private void detectObject(List<Mat> croppedImages, Mat[] templates) {
        // Template Matching
        int[] templateMatchCnt = new int[TEMPLATE_FILE_NAME.length]; // Initialize template match count

        for (int tempNum = 0; tempNum < templates.length; tempNum++) { // Loop through templates
            int matchCnt = 0;

            List<org.opencv.core.Point> matches = new ArrayList<>();

            Mat template = templates[tempNum].clone();

            for (Mat targetImg : croppedImages) { // Loop through cropped images
                int widthMin = 20; // Minimum width in pixels
                int widthMax = 100; // Maximum width in pixels
                int changeWidth = 5; // Width increment in pixels
                int changeAngle = 45; // Angle increment in degrees

                for (int i = widthMin; i <= widthMax; i += changeWidth) {
                    for (int j = 0; j <= 360; j += changeAngle) {
                        Mat resizedTemp = resizeImg(template, i);
                        Mat rotResizedTemp = rotImg(resizedTemp, j);

                        Mat result = new Mat();
                        Imgproc.matchTemplate(targetImg, rotResizedTemp, result, Imgproc.TM_CCOEFF_NORMED);

                        double threshold = 0.8;
                        Core.MinMaxLocResult mmlr = Core.minMaxLoc(result);
                        double maxVal = mmlr.maxVal;
                        if (maxVal >= threshold) {
                            Mat thresholdedResult = new Mat();
                            Imgproc.threshold(result, thresholdedResult, threshold, 1.0, Imgproc.THRESH_TOZERO);

                            for (int y = 0; y < thresholdedResult.rows(); y++) {
                                for (int x = 0; x < thresholdedResult.cols(); x++) {
                                    if (thresholdedResult.get(y, x)[0] > 0) {
                                        matches.add(new org.opencv.core.Point(x, y));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            List<org.opencv.core.Point> filteredMatches = removeDuplicates(matches);
            matchCnt += filteredMatches.size();

            templateMatchCnt[tempNum] = matchCnt;

            Log.i(TAG, "Template: " + TEMPLATE_NAME[tempNum] + ", Matches: " + matchCnt);
        }

        //Result Reporting
        //When you recognize items, let’s set the type and number.
        int mostMatchTemplateNum = getMaxIndex(templateMatchCnt);
        Log.i(TAG, "Most matched template: " + TEMPLATE_NAME[mostMatchTemplateNum]);
        api.setAreaInfo(areaId, TEMPLATE_FILE_NAME[mostMatchTemplateNum], templateMatchCnt[mostMatchTemplateNum]);
    }

    // Resize image
    private Mat resizeImg(Mat img, int width){
        int height = (int)(img.rows() * ((double) width / img.cols()));
        Mat resizedImg = new Mat();
        Imgproc.resize(img, resizedImg, new Size(width, height));
        return resizedImg;
    }

    // Rotate image
    private Mat rotImg (Mat img, int angle){
        org.opencv.core.Point center = new org.opencv.core.Point(img.cols() / 2.0, img.rows() / 2.0);
        Mat rotatedMat = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Mat rotatedImg = new Mat();
        Imgproc.warpAffine(img, rotatedImg, rotatedMat, img.size());
        return rotatedImg;
    }

    //    remove multiple detections
    private static List<org.opencv.core.Point> removeDuplicates (List<org.opencv.core.Point> points){
        double length = 10; // [px]
        List<org.opencv.core.Point> filteredList = new ArrayList<>();
        for(org.opencv.core.Point point : points){
            boolean isInclude = false;
            for(org.opencv.core.Point checkPoint : filteredList){
                double distance = calculateDistance(point, checkPoint);
                if(distance <= length){
                    isInclude = true;
                    break;
                }
            }
            if(!isInclude){
                filteredList.add(point);
            }
        }
        return filteredList;
    }

    //Find the distance between two point
    private static double calculateDistance(org.opencv.core.Point p1, org.opencv.core.Point p2){
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    //Get the maximum value of an array
    private int getMaxIndex (int[] array){
        int max = 0;
        int maxIndex = 0;
        for (int i = 0; i < array.length; i++){
            if(array[i] > max){
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
