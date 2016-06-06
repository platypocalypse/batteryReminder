package patabase.chargereminder;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.support.v7.app.NotificationCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BatteryCheckerTask extends IntentService {

    public BatteryCheckerTask() {
        super("BatteryCheckerTask");
    }

    public BroadcastReceiver mBatInfoReceiver;
    private BigDecimal mExpectedLevel;

    @Override
    protected void onHandleIntent(Intent intent) {

        mExpectedLevel = new BigDecimal(0);
        final long startTime = intent.getLongExtra("startTime", 0);
        final long stopTime = intent.getLongExtra("stopTime", 0);

        //Set up a BroadcastReceiver that checks battery level and charging status when battery level changes
        mBatInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                long timeBetweenCharges = startTime - stopTime;
                long timeElapsed;
                if (timeBetweenCharges > 0) {
                    timeElapsed = System.currentTimeMillis() -  stopTime;
                    BigDecimal timeElapsedBd = new BigDecimal(timeElapsed);
                    BigDecimal timeBetweenChargesBd = new BigDecimal(timeBetweenCharges);
                    mExpectedLevel = new BigDecimal(1).subtract(timeElapsedBd.divide(timeBetweenChargesBd, 3, RoundingMode.CEILING));
                }
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

                BigDecimal currentBatteryLevel = new BigDecimal(level).divide(new BigDecimal(scale));

                //If battery is not charging, compare level to expected level
                if (!isCharging) {
                    boolean needsCharging = compareToExpectedLevel(currentBatteryLevel);
                    if (needsCharging) {
                        sendNotification();
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
        int expectedLevel = (mExpectedLevel.multiply(new BigDecimal(100))).intValue();
        int levelConverted = (level.multiply(new BigDecimal(100))).intValue();
        if (levelConverted < expectedLevel) {
            return true;
        } else {
            return false;
        }
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

}
