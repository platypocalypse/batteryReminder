package patabase.chargereminder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        NumberPicker.OnValueChangeListener {

    private TextView mCurrentStartTime;
    private TextView mCurrentStopTime;
    private TextView mCurrentCheckInterval;

    private View mSelectedView;

    private BatteryCheckerService mBatteryCheckerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindService(new Intent(this, BatteryCheckerService.class), BatteryCheckerServiceConnection, Context.BIND_AUTO_CREATE);

        mCurrentStartTime = (TextView) findViewById(R.id.current_start_time_textview);
        mCurrentStopTime = (TextView) findViewById(R.id.current_stop_time_textview);
        mCurrentCheckInterval = (TextView) findViewById(R.id.current_check_interval_textview);


    }

    @Override
    protected void onDestroy() {
        unbindService(BatteryCheckerServiceConnection);
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
                mBatteryCheckerService.mCheckInterval = newInterval;
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
                mBatteryCheckerService.mChargeStartTime.setTime(convertStartToMs.getTimeInMillis());
                mCurrentStartTime.setText(text);
                break;
            case R.id.set_stop_time_button:
                Calendar convertStopToMs = Calendar.getInstance();
                convertStopToMs.set(Calendar.HOUR_OF_DAY, hourOfDay);
                convertStopToMs.set(Calendar.MINUTE,minute);
                mBatteryCheckerService.mChargeStopTime.setTime(convertStopToMs.getTimeInMillis());
                mCurrentStopTime.setText(text);
                break;
        }
        mSelectedView = null;
    }

    private ServiceConnection BatteryCheckerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BatteryCheckerService.LocalBinder binder = (BatteryCheckerService.LocalBinder) service;
            mBatteryCheckerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBatteryCheckerService = null;
        }
    };
}
