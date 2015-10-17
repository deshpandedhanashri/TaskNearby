package app.tasknearby.yashcreations.com.tasknearby.service;

/**
 * Created by Yash on 13/10/15.
 */
public class Constants {
    public Constants() {
    }

    public static long ActDetectionInterval_ms = 500;


    public static long UPDATE_INTERVAL = 5000;
    public static long FATEST_INTERVAL = 3000;
    public static int DISPLACEMENT = 1;
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static String ReceiverIntentExtra="detectedActivities";
    public static String INTENT_FILTER="com.commando.taskNearby"+ReceiverIntentExtra;


}
