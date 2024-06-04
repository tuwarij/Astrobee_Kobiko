package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

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
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

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

    @Override
    protected void runPlan1(){

        Log.i(TAG, "start mission");

        // The mission starts.
        api.startMission();

        // Move to a point.
        Point point = new Point(10.9d, -9.92284d, 5.195d);
        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(point, quaternion, false);

        // step in x axis 20cm and reset rotation
        point = new Point(11.1d, -9.92284d, 5.195d);
        api.moveTo(point, quaternion, true);
        // Get a camera image.
        Mat image1 = api.getMatNavCam();
        api.saveMatImage(image1, "file_name1.png");

        detectObject(image1,1);

        // step to 2nd area and rotate to the ceiling
        point = new Point(11.1d, -9.155d, 5.195d);
        Quaternion quaternion_lookUpInXAxis = new Quaternion(0f,  0.707f, 0f,  0.707f);
        api.moveTo(point, quaternion_lookUpInXAxis, true);

        // move up to 2nd image
        point = new Point(11.1d, -9.155d, 4.5d);
        api.moveTo(point, quaternion_lookUpInXAxis,true);

        // Get a camera image.
        Mat image2 = api.getMatNavCam();
        api.saveMatImage(image2, "file_name2.png");

        detectObject(image2,2);
//        final int LOOP_MAX = 5;
//
//        int loopCounter = 0;
//        while(!result.hasSucceeded() && loopCounter < LOOP_MAX) {
//            result = api.moveTo(point, quaternion_lookUpInXAxis,true);
//            ++loopCounter;
//        }

        // move to 3rd image
        point = new Point(11.1d, -8.055d, 4.5d);
        quaternion_lookUpInXAxis = new Quaternion(0.271f,  0.653f, 0.271f,  0.653f);
        api.moveTo(point, quaternion_lookUpInXAxis, true);

        // Get a camera image.
        Mat image3 = api.getMatNavCam();
        // Save the image
        api.saveMatImage(image3, "file_name3.png");

        detectObject(image3,3);


        // move to 4th area by move x
        point = new Point(10.7d, -8.055d, 4.5d);
        quaternion = new Quaternion(0f,  0f, 0f,  0f);
        api.moveTo(point, quaternion, true);

        // move to 4th image by move Y
        point = new Point(10.7d, -7.055d, 4.7d);
        quaternion = new Quaternion(0f,  0f, 1f,  0f);
        api.moveTo(point, quaternion, true);

        // Get a camera image.
        Mat image4 = api.getMatNavCam();
        // Save the image
        api.saveMatImage(image4, "file_name4.png");

        detectObject(image4,4);

        // move to astronaut
        point = new Point(11.143, -6.7607, 4.9654);
        quaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);
        api.moveTo(point, quaternion, true);


        /* *********************************************************************** */
        /* Write your code to recognize type and number of items in the each area! */
        /* *********************************************************************** */

        // When you recognize items, let’s set the type and number.
//        api.setAreaInfo(1, "item_name", 1);

        /* **************************************************** */
        /* Let's move to the each area and recognize the items. */
        /* **************************************************** */

        // When you move to the front of the astronaut, report the rounding completion.
        api.reportRoundingCompletion();

        /* ********************************************************** */
        /* Write your code to recognize which item the astronaut has. */
        /* ********************************************************** */

        // Let's notify the astronaut when you recognize it.
        api.notifyRecognitionItem();

        /* ******************************************************************************************************* */
        /* Write your code to move Astrobee to the location of the target item (what the astronaut is looking for) */
        /* ******************************************************************************************************* */

        // Take a snapshot of the target item.
        api.takeTargetItemSnapshot();
    }

    @Override
    protected void runPlan2(){
       // write your plan 2 here.
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here.
    }

//    // You can add your method.
//    private String yourMethod(){
//        return "your method";
//    }

    private void detectObject(Mat image,int n) {
        // Detect AR
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
        List<Mat> corners = new ArrayList<>();
        Mat markerIds = new Mat();
        Aruco.detectMarkers(image, dictionary, corners, markerIds);

        if (markerIds.empty()) {
            Log.e(TAG, "No AR markers detected");
        } else {
            Log.i(TAG, "Detected AR markers: " + markerIds.dump());
        }

        // Get camera matrix
        double[][] navCamIntrinsics = api.getNavCamIntrinsics();
        if (navCamIntrinsics.length != 2 || navCamIntrinsics[0].length != 9 || navCamIntrinsics[1].length != 5) {
            Log.e(TAG, "Invalid NavCam intrinsics.");
            return;
        }

        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0, api.getNavCamIntrinsics()[0]);
        //Get lens distortion parameters
        Mat cameraCoefficients = new Mat(1, 5, CvType.CV_64F);
        cameraCoefficients.put(0, 0, api.getNavCamIntrinsics()[1]);
        cameraCoefficients.convertTo(cameraCoefficients, CvType.CV_64F);

        // Undistort image
        Mat undistortImg = new Mat();
        Calib3d.undistort(image, undistortImg, cameraMatrix, cameraCoefficients);
        api.saveMatImage(undistortImg, "undistorted_image" + n + ".png");

        // Convert to grayscale
//        Mat grayImg = new Mat();
//        Imgproc.cvtColor(undistortImg, grayImg, Imgproc.COLOR_BGR2GRAY);

        // Enhance image if it has 3 channels
        Mat enhancedImg = new Mat();
        if (undistortImg.channels() == 3) {
            Imgproc.cvtColor(undistortImg, enhancedImg, Imgproc.COLOR_BGR2GRAY);
        } else {
            enhancedImg = undistortImg;
        }
        Imgproc.equalizeHist(enhancedImg, enhancedImg);

        //Pattern matching using ORB
        //Load template images
        Mat[] templates = loadTemplates();
        if (templates == null) return;

        int[] templateMatchCnt = new int[templates.length];
        ORB orb = ORB.create(500, 1.2f, 8, 31, 0, 2, ORB.HARRIS_SCORE, 31, 20);

        for (int i = 0; i < templates.length; i++) {
            Mat template = templates[i];
            MatOfKeyPoint templateKeyPoints = new MatOfKeyPoint();
            Mat templateDescriptors = new Mat();
            orb.detectAndCompute(template, new Mat(), templateKeyPoints, templateDescriptors);

            MatOfKeyPoint imageKeyPoints = new MatOfKeyPoint();
            Mat imageDescriptors = new Mat();
            orb.detectAndCompute(enhancedImg, new Mat(), imageKeyPoints, imageDescriptors);

            BFMatcher matcher = BFMatcher.create(Core.NORM_HAMMING, true);
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(templateDescriptors, imageDescriptors, matches);

            // Apply ratio test of Lowe
            List<DMatch> goodMatches = new ArrayList<>();
            DMatch[] matchArray = matches.toArray();

            for (int j = 0; j < matchArray.length - 1; j++) {
                DMatch match1 = matchArray[j];
                DMatch match2 = matchArray[j + 1];
                if (match1.distance < 0.75 * match2.distance) {
                    goodMatches.add(match1);
                }
            }

            templateMatchCnt[i] = goodMatches.size();
            Log.i(TAG, "Template: " + TEMPLATE_NAME[i] + ", Matches: " + goodMatches.size());
        }

        int mostMatchTemplateNum = getMaxIndex(templateMatchCnt);
        Log.i(TAG, "Most matched template: " + TEMPLATE_NAME[mostMatchTemplateNum]);
        api.setAreaInfo(n, TEMPLATE_NAME[mostMatchTemplateNum], templateMatchCnt[mostMatchTemplateNum]);
    }

    //Get the maximum value of an array
    private int getMaxIndex(int[] array){
        int max = 0;
        int maxIndex = 0;

        //Find the index of the element with the largest value
        for(int i = 0; i < array.length; i++){
            if(array[i] > max){
                max = array[i];
                maxIndex = i;
            }
        }

        System.out.println("hi");
        return maxIndex;
    }

    private Mat[] loadTemplates() {
        Mat[] templates = new Mat[TEMPLATE_FILE_NAME.length];
        for (int i = 0; i < TEMPLATE_FILE_NAME.length; i++) {
            try {
                InputStream inputStream = getAssets().open(TEMPLATE_FILE_NAME[i]);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    Log.e(TAG, "Failed to decode bitmap for template: " + TEMPLATE_FILE_NAME[i]);
                    continue;
                }
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
                templates[i] = mat;
                inputStream.close();
                Log.i(TAG, "Loaded template: " + TEMPLATE_FILE_NAME[i]);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException while loading template: " + TEMPLATE_FILE_NAME[i]);
            }
        }
        return templates;
    }

}
