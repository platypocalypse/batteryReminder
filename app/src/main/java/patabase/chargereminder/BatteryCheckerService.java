package patabase.chargereminder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BatteryCheckerService extends Service {

    public java.sql.Time mChargeStartTime;
    public java.sql.Time mChargeStopTime;
    public int mCheckInterval;
    private float mExpectedLevel;

    public Calendar mCalendar;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mChargeStartTime = new java.sql.Time(System.currentTimeMillis());
        mChargeStopTime = new java.sql.Time(System.currentTimeMillis());

        //Calculate expected level

        //Set up a BroadcastReceiver that checks battery level and charging status when battery level changes
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                long timeBetweenCharges = mChargeStopTime.getTime() - mChargeStartTime.getTime();
                if (timeBetweenCharges > 0) {
                    long timeElapsed =  timeBetweenCharges - System.currentTimeMillis();
                    mExpectedLevel = 1 - (timeElapsed / timeBetweenCharges);
                }
                int currentBatteryLevel = intent.getIntExtra("currentBatteryLevel", 0);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

                //If battery is not charging, compare level to expected level
//                if (!isCharging) {
                    boolean needsCharging = compareToExpectedLevel(currentBatteryLevel);
                    if (needsCharging) {
                        Log.d("tag", "Charge your phone!");
                        //send a notification to user
                    }
//                }
            }
        };

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    public boolean compareToExpectedLevel(int level) {
        int expectedLevel = Math.round(mExpectedLevel);
        if (level < expectedLevel) {
            //Send notification
            return true;
        } else {
            return false;
        }
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BatteryCheckerService getService() {
            return BatteryCheckerService.this;
        }
    }
}
