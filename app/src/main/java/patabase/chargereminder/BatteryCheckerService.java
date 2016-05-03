package patabase.chargereminder;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

public class BatteryCheckerService extends Service {

    public java.sql.Time mChargeStartTime;
    public java.sql.Time mChargeStopTime;
    public BroadcastReceiver mBatInfoReceiver;
    public long mCheckInterval;
    private long mLastNotificationTime;
    private BigDecimal mExpectedLevel;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mExpectedLevel = new BigDecimal(0);
        mCheckInterval = 30*60*1000;

        Calendar convertStopToMs = Calendar.getInstance();
        convertStopToMs.set(Calendar.HOUR_OF_DAY, 6);
        convertStopToMs.set(Calendar.MINUTE, 0);

        Calendar convertStartToMs = Calendar.getInstance();
        convertStartToMs.set(Calendar.HOUR_OF_DAY, 22);
        convertStartToMs.set(Calendar.MINUTE, 0);

        mChargeStartTime = new java.sql.Time(convertStartToMs.getTimeInMillis());
        mChargeStopTime = new java.sql.Time(convertStopToMs.getTimeInMillis());
        mLastNotificationTime = mChargeStopTime.getTime();

        //Set up a BroadcastReceiver that checks battery level and charging status when battery level changes
        mBatInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                long timeBetweenCharges = mChargeStartTime.getTime() - mChargeStopTime.getTime();
                long timeElapsed;
                if (timeBetweenCharges > 0) {
                    timeElapsed = System.currentTimeMillis() -  mChargeStopTime.getTime();
                    BigDecimal timeElapsedBd = new BigDecimal(timeElapsed);
                    BigDecimal timeBetweenChargesBd = new BigDecimal(timeBetweenCharges);
                    mExpectedLevel = new BigDecimal(1).subtract(timeElapsedBd.divide(timeBetweenChargesBd, 3, RoundingMode.CEILING));
                }
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                BigDecimal currentBatteryLevel = new BigDecimal(level).divide(new BigDecimal(scale));
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

                //If battery is not charging, compare level to expected level
                if (!isCharging) {
                    boolean needsCharging = compareToExpectedLevel(currentBatteryLevel);
                    boolean recentlyNotified = compareToCheckInterval(System.currentTimeMillis());
                    if (needsCharging && !recentlyNotified) {
                        sendNotification();
                        mLastNotificationTime = System.currentTimeMillis();
                    }
                }
            }
        };

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
    }

    public boolean compareToExpectedLevel(BigDecimal level) {
//        int expectedLevel = Math.round(mExpectedLevel);
        int expectedLevel = (mExpectedLevel.multiply(new BigDecimal(100))).intValue();
        int levelConverted = (level.multiply(new BigDecimal(100))).intValue();
        if (levelConverted < expectedLevel) {
            Log.d("tag", "Charge your phone!");
            return true;
        } else {
            Log.d("tag", "Don't charge your phone!");
            return false;
        }
    }

    public boolean compareToCheckInterval(long currentTime) {
        long timeSinceNotification = currentTime - mLastNotificationTime;
        return (timeSinceNotification <= mCheckInterval);
    }

    public void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setContentTitle("Charge your phone!");
        builder.setContentText("Your phone's battery level is below its expected value!");
        builder.setSound(notificationSound);
        //can't find this int...
        final int DEFAULT_SOUND = 2;
        builder.setDefaults(DEFAULT_SOUND);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(1337, builder.build());
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
