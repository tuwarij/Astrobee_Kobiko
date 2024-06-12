package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log; // Log

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
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
    Mat[] t;

    @Override
    protected void runPlan1(){
        try {
            api.startMission(); // start the mission
            Log.i(TAG, "Start mission");
            t = TemplateLoading();

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
            api.flashlightControlFront(0.6f);
            Mat image0 = api.getMatNavCam();
            api.saveMatImage(image0, "file_name0.png");
            Log.i(TAG, "Captured and saved image0");


            crop(image0, areaId, 300, 50, 0, 0);
            areaId++;
            Log.i(TAG, "next is area2");

            // step to 2nd area and rotate to the ceiling
            point = new Point(11.1d, -9.155d, 5.195d);
            Quaternion quaternion_lookUpInXAxis = new Quaternion(0f,  0.707f, 0f,  0.707f);
            api.moveTo(point, quaternion_lookUpInXAxis, true);
            Log.i(TAG, "Moved1.1");

            // move up to 2nd image
            point = new Point(11.1d, -8.955d, 4.5d);
            api.moveTo(point, quaternion_lookUpInXAxis,true);
            Log.i(TAG, "Moved1.2");

            // Get a camera image.
            api.flashlightControlFront(0.6f);
            Mat image1 = api.getMatNavCam();
            api.saveMatImage(image1, "file_name1.png");
            Log.i(TAG, "Captured and saved image1");

            crop(image1, areaId, 300, 250, 0, 0);
            areaId++;
            Log.i(TAG, "next is area3");

            // move to 3rd image
            point = new Point(11.1d, -8.055d, 4.5d);
//            quaternion_lookUpInXAxis = new Quaternion(0.271f,  0.653f, 0.271f,  0.653f);
            api.moveTo(point, quaternion_lookUpInXAxis, true);
            Log.i(TAG, "Moved2.1");

            // Get a camera image.
            api.flashlightControlFront(0.6f);
            Mat image2 = api.getMatNavCam();
            // Save the image
            api.saveMatImage(image2, "file_name2.png");
            Log.i(TAG, "Captured and saved image2");

            crop(image2, areaId,  300, 0, 0, 0);
            areaId++;
            Log.i(TAG, "next is area3");

            // move to 4th area by move x
            point = new Point(10.7d, -8.055d, 4.5d);
            quaternion = new Quaternion(0f,  0f, 0f,  0f);
            api.moveTo(point, quaternion, true);
            Log.i(TAG, "Moved3.1");

            // move to 4th image by move Y
            point = new Point(10.7d, -7.055d, 4.8d);
            quaternion = new Quaternion(0f,  0f, 1f,  0f);
            api.moveTo(point, quaternion, true);
            Log.i(TAG, "Moved3.2");


            // Get a camera image.
            api.flashlightControlFront(0.6f);
            Mat image3 = api.getMatNavCam();
            // Save the image
            api.saveMatImage(image3, "file_name3.png");
            Log.i(TAG, "Captured and saved image3");

            crop(image3, areaId, 0,0,0,0);

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
        } catch (Exception e) {
            Log.e(TAG, "Error in runPlan1", e);
        }
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

    private void crop(Mat image, int n ,int leftShift, int rightShift, int topShift, int bottomShift) {
        try {
            // Undistort the image
            Mat undistortImg = CameraCalibration(image);
            List<Mat> croppedImages = new ArrayList<>();

            Mat imagecrop = cropImage(undistortImg, leftShift, rightShift, topShift, bottomShift);

            // Convert the image to grayscale if necessary
            Mat grayImage = new Mat();
            if (undistortImg.channels() == 3) {
                Imgproc.cvtColor(imagecrop, grayImage, Imgproc.COLOR_BGR2GRAY);
            } else if (undistortImg.channels() == 4) {
                Imgproc.cvtColor(imagecrop, grayImage, Imgproc.COLOR_BGRA2GRAY);
            } else {
                grayImage = imagecrop.clone();
            }

            // Apply Gaussian blur to the image
            Mat blurredImage = new Mat();
            Imgproc.GaussianBlur(grayImage, blurredImage, new Size(3, 3), 0);

            Mat binary = new Mat(blurredImage.rows(), blurredImage.cols(), blurredImage.type(), new Scalar(0));
            Imgproc.threshold(blurredImage, binary, 20, 200, Imgproc.THRESH_BINARY_INV);

            // Apply Canny edge detection
            Mat edges = new Mat();
            Imgproc.Canny(binary, edges, 50, 150);
//
//            // Save the edges image for debugging
//            String edgesFilename = "edges_image_" + System.currentTimeMillis() + ".png";
//            api.saveMatImage(edges, edgesFilename);
//            Log.i(TAG, "Edges image saved as: " + edgesFilename);

            // Find contours from the edges
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            // Draw contours on the original image for debugging
            Mat contourImage = imagecrop.clone();
            Imgproc.drawContours(contourImage, contours, -1, new Scalar(0, 255, 0), 2,Imgproc.LINE_8, hierarchy, 2);

            String contoursFilename = "contours_image_" + n + ".png";
            api.saveMatImage(contourImage, contoursFilename);
            Log.i(TAG, "Contours image saved as: " + contoursFilename);


            double targetAspectRatio = 27.0 / 15.0;

            MatOfPoint2f bestContour = new MatOfPoint2f();
            double bestAspectRatioDiff = Double.MAX_VALUE;
            double largestArea = 0;  // ตัวแปรสำหรับเก็บพื้นที่ใหญ่ที่สุด

            // Iterate through each contour
            for (MatOfPoint contour : contours) {
                MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                // Check if the approximated contour has 4 points and is convex
                if (approxCurve.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                    Rect boundingRect = Imgproc.boundingRect(new MatOfPoint(approxCurve.toArray()));
                    double aspectRatio = (double) boundingRect.width / boundingRect.height;

                    // Ensure aspectRatio is always greater than 1
                    if (aspectRatio < 1) {
                        aspectRatio = 1.0 / aspectRatio;
                    }

                    double aspectRatioDiff = Math.abs(aspectRatio - targetAspectRatio);
                    double area = Imgproc.contourArea(new MatOfPoint(approxCurve.toArray()));  // คำนวณพื้นที่ของคอนทัวร์

                    // เปรียบเทียบอัตราส่วนและพื้นที่เพื่อหาอันที่ดีที่สุด
                    if (aspectRatioDiff < bestAspectRatioDiff || (aspectRatioDiff == bestAspectRatioDiff && area > largestArea)) {
                        bestAspectRatioDiff = aspectRatioDiff;
                        largestArea = area;  // อัปเดตพื้นที่ใหญ่ที่สุด
                        bestContour = approxCurve;
                    }
                }
            }

            if (bestContour.empty()) {
                Log.i(TAG, "No paper found!");
                return;
            }

            // Prepare source points and destination points for the perspective transform
            Mat srcPoints = new Mat(4, 2, CvType.CV_32F);
            srcPoints.put(0, 0, bestContour.get(0, 0));
            srcPoints.put(1, 0, bestContour.get(1, 0));
            srcPoints.put(2, 0, bestContour.get(2, 0));
            srcPoints.put(3, 0, bestContour.get(3, 0));

            Rect boundingRect = Imgproc.boundingRect(new MatOfPoint(bestContour.toArray()));
            Mat dstPoints = new Mat(4, 2, CvType.CV_32F);
            dstPoints.put(0, 0, new float[]{0, 0});
            dstPoints.put(1, 0, new float[]{boundingRect.width - 1, 0});
            dstPoints.put(2, 0, new float[]{boundingRect.width - 1, boundingRect.height - 1});
            dstPoints.put(3, 0, new float[]{0, boundingRect.height - 1});

            // Compute the perspective transform matrix
            Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);

            // Warp the perspective to get the top-down view of the rectangle
            Mat croppedImage = new Mat();
            Imgproc.warpPerspective(imagecrop, croppedImage, transform, new Size(boundingRect.width, boundingRect.height));

            // Save the cropped image
            String croppedFilename = "cropped_image_" + n + ".png";
            api.saveMatImage(croppedImage, croppedFilename);
            Log.i(TAG, "Cropped image saved as: " + croppedFilename);

            croppedImages.add(croppedImage);

            Log.i(TAG, "Image Cropped");
            detectObject(croppedImages, t);
        } catch (Exception e) {
            Log.e(TAG, "Error during cropping", e);
        }
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

                        double threshold = 0.85;
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

    public static Mat cropImage(Mat image, int leftShift, int rightShift, int topShift, int bottomShift) {
        int x = leftShift;
        int y = topShift;
        int width = image.cols() - leftShift - rightShift;
        int height = image.rows() - topShift - bottomShift;

        // Ensure the dimensions are valid
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid crop dimensions!");
        }

        // Define the region of interest (ROI)
        Rect roi = new Rect(x, y, width, height);

        // Crop the image
        return new Mat(image, roi);
    }


}
