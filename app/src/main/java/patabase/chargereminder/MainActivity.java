package patabase.chargereminder;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.math.BigDecimal;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        NumberPicker.OnValueChangeListener {

    private TextView mCurrentStartTime;
    private TextView mCurrentStopTime;
    private TextView mCurrentCheckInterval;

    private View mSelectedView;

    private java.sql.Time mStartTime;
    private java.sql.Time mStopTime;
    private long mInterval;
    private AlarmManager mAlarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentStartTime = (TextView) findViewById(R.id.current_start_time_textview);
        mCurrentStopTime = (TextView) findViewById(R.id.current_stop_time_textview);
        mCurrentCheckInterval = (TextView) findViewById(R.id.current_check_interval_textview);

        //Initialize values for first launch
        Calendar convertStopToMs = Calendar.getInstance();
        convertStopToMs.set(Calendar.HOUR_OF_DAY, 6);
        convertStopToMs.set(Calendar.MINUTE, 0);
        mStopTime = new java.sql.Time(convertStopToMs.getTimeInMillis());
        Calendar convertStartToMs = Calendar.getInstance();
        convertStartToMs.set(Calendar.HOUR_OF_DAY, 22);
        convertStartToMs.set(Calendar.MINUTE, 0);
        mStartTime = new java.sql.Time(convertStartToMs.getTimeInMillis());
        mInterval = 30 * 60 * 1000;

        //Set first alarm
        updateAlarmParameters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAlarmParameters();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showIntervalPickerDialog(View v) {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("NumberPicker");
        dialog.setContentView(R.layout.dialog_number_picker);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        Button setButton = (Button) dialog.findViewById(R.id.button_set);
        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(60);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener(this);

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        setButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String text = String.valueOf(numberPicker.getValue()) + " minutes";
                mCurrentCheckInterval.setText(text);
                int newInterval = numberPicker.getValue() * 60 * 1000;
                mInterval = newInterval;
                updateAlarmParameters();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //auto generated stub
    }

    public void showTimePickerDialog(View v) {
        mSelectedView = v;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d("tag", "onTimeSet");
        String hour;
        String minutes;
        String amOrPm;
        if (hourOfDay < 13) {
            hour = String.format("%d", hourOfDay);
            amOrPm = " AM";
        } else {
            hour = String.format("%d", (hourOfDay - 12));
            amOrPm = " PM";

        }
        minutes = String.format("%02d", minute);
        String text = hour + ":" + minutes + amOrPm;
        Log.d("tag", mSelectedView.toString());
        switch (mSelectedView.getId()) {
            case R.id.set_start_time_button:
                Calendar convertStartToMs = Calendar.getInstance();
                convertStartToMs.setTimeInMillis(System.currentTimeMillis());
                convertStartToMs.set(Calendar.HOUR_OF_DAY, hourOfDay);
                convertStartToMs.set(Calendar.MINUTE, minute);
                mStartTime.setTime(convertStartToMs.getTimeInMillis());
                mCurrentStartTime.setText(text);
                updateAlarmParameters();
                break;
            case R.id.set_stop_time_button:
                Calendar convertStopToMs = Calendar.getInstance();
                convertStopToMs.set(Calendar.HOUR_OF_DAY, hourOfDay);
                convertStopToMs.set(Calendar.MINUTE,minute);
                mStopTime.setTime(convertStopToMs.getTimeInMillis());
                mCurrentStopTime.setText(text);
                updateAlarmParameters();
                break;
        }
        mSelectedView = null;
    }

    private void updateAlarmParameters() {

        Intent checkBatteryLevel = new Intent(getApplicationContext(), BatteryCheckerTask.class);
        checkBatteryLevel.putExtra("startTime", mStartTime.getTime());
        checkBatteryLevel.putExtra("stopTime" ,mStopTime.getTime());
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),  0, checkBatteryLevel, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), mInterval, pendingIntent);

    }


}
