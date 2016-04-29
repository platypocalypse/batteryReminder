package patabase.chargereminder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by patbe_000 on 1/18/2016.
 */
public class BatteryCheckerService extends Service {

    private java.sql.Time mChargeStartTime;
    private java.sql.Time mChargeStopTime;
    private int mCheckInterval;
    private float mExpectedLevel;

    @Override
    public void onCreate() {
        super.onCreate();

        //Retrieve charge start and stop times, check interval from sharedpreferences

        //Calculate expected level

        //Set up a BroadcastReceiver that checks battery level and charging status when battery level changes
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent intent) {
                int currentBatteryLevel = intent.getIntExtra("currentBatteryLevel", 0);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

                //If battery is not charging, compare level to expected level
                if (!isCharging) {
                    compareToExpectedLevel(currentBatteryLevel);
                    //if current level < expected level, send a notification to user
                }
            }
        };

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    public void compareToExpectedLevel(int level) {
        int expectedLevel = Math.round(mExpectedLevel);
        if (level < expectedLevel) {
            //Send notification
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
