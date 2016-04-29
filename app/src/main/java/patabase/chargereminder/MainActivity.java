package patabase.chargereminder;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void showTimePickerDialog(View v) {
        mSelectedView = v;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showIntervalPickerDialog(View v) {
//        mSelectedView = v;
//        DialogFragment newFragment = new TimePickerFragment();
//        newFragment.show(getFragmentManager(), "timePicker");
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
                convertStopToMs.set(Calendar.HOUR_OF_DAY, 6);
                convertStopToMs.set(Calendar.MINUTE,0);
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
            Log.d("tag", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBatteryCheckerService = null;
        }
    };
}
