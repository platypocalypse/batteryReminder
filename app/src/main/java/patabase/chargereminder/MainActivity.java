package patabase.chargereminder;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mCurrentStartTime;
    private Button mStartTimeButton;
    private String mNewStartTime;

    private TextView mCurrentStopTime;
    private Button mStopTimeButton;
    private String mNewStopTime;

    private TextView mCurrentCheckInterval;
    private String mNewCheckInterval;
    private Button mCheckIntervalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentStartTime = (TextView) findViewById(R.id.current_start_time_textview);
        mStartTimeButton = (Button) findViewById(R.id.set_start_time_button);
        mStartTimeButton.setOnClickListener(this);
        mCurrentStopTime = (TextView) findViewById(R.id.current_stop_time_textview);
        mStopTimeButton = (Button) findViewById(R.id.set_stop_time_button);
        mStopTimeButton.setOnClickListener(this);
        mCurrentCheckInterval = (TextView) findViewById(R.id.current_check_interval_textview);
        mCheckIntervalButton = (Button) findViewById(R.id.set_check_interval_button);
        mCheckIntervalButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_start_time_button:
                showTimePickerDialog();
                break;
            case R.id.set_stop_time_button:
                showTimePickerDialog();
                break;
            case R.id.set_check_interval_button:
                showTimePickerDialog();
                break;

        }
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }
}
