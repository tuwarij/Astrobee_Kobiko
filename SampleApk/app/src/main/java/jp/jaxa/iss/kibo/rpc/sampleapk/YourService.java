package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.util.Log;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.core.Mat;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void runPlan1(){
        Log.i(TAG, "start mission");

        // The mission starts.
        api.startMission();

        // Move to a point.
        Point point = new Point(10.9d, -9.92284d, 5.195d);
        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(point, quaternion, false);

        //step in x axis 20cm and reset rotation
        point = new Point(11.1d, -9.92284d, 5.195d);
        api.moveTo(point, quaternion, true);
        // Get a camera image.
        Mat image0 = api.getMatNavCam();
        api.saveMatImage(image0, "file_name0.png");

        //step to 2nd area and rotate to the ceiling
        point = new Point(11.1d, -9.155d, 5.195d);
        Quaternion quaternion_lookUpInXAxis = new Quaternion(0f,  0.707f, 0f,  0.707f);
        api.moveTo(point, quaternion_lookUpInXAxis, true);

        // move up to 2nd image
        point = new Point(11.1d, -9.155d, 4.5d);
        api.moveTo(point, quaternion_lookUpInXAxis,true);

        // Get a camera image.
        Mat image1 = api.getMatNavCam();
        api.saveMatImage(image1, "file_name1.png");

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
        Mat image2 = api.getMatNavCam();
        // Save the image
        api.saveMatImage(image2, "file_name2.png");

        // move to 4th area by move x
        point = new Point(10.7d, -8.055d, 4.5d);
        quaternion = new Quaternion(0f,  0f, 0f,  0f);
        api.moveTo(point, quaternion, true);

        // move to 4th image by move Y
        point = new Point(10.7d, -7.055d, 4.7d);
        quaternion = new Quaternion(0f,  0f, 1f,  0f);
        api.moveTo(point, quaternion, true);

        // Get a camera image.
        Mat image3 = api.getMatNavCam();
        // Save the image
        api.saveMatImage(image3, "file_name3.png");

        /* *********************************************************************** */
        /* Write your code to recognize type and number of items in the each area! */
        /* *********************************************************************** */

        // When you recognize items, let’s set the type and number.
        api.setAreaInfo(1, "item_name", 1);

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

    // You can add your method.
    private String yourMethod(){
        return "your method";
    }
}
