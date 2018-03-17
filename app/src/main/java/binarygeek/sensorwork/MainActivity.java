package binarygeek.sensorwork;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Activity activity;
    SensorEventListener sensorEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity=this;
        sensorEventListener=this;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);



        FloatingActionButton on = (FloatingActionButton) findViewById(R.id.on);
        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSensorManager.registerListener(sensorEventListener, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        FloatingActionButton off = (FloatingActionButton) findViewById(R.id.off);
        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSensorManager.unregisterListener(sensorEventListener);

            }
        });
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0]< event.sensor.getMaximumRange()) {

            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.screenBrightness = 0;
            getWindow().setAttributes(params);

            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (pm.isScreenOn()) {
                DevicePolicyManager policy = (DevicePolicyManager)
                        getSystemService(Context.DEVICE_POLICY_SERVICE);
                try {
                    policy.lockNow();
                } catch (SecurityException ex) {
                    Toast.makeText(
                            this,
                            "must enable device administrator",
                            Toast.LENGTH_LONG).show();
                    ComponentName admin = new ComponentName(this, ScreenOffAdminReceiver.class);
                    Intent intent = new Intent(
                            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                    startActivity(intent);
                }
            }

            //turnScreenOffAndExit();
            Toast.makeText(this,"NEAR",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,"FAR",Toast.LENGTH_LONG).show();
        }
    }


    static void turnScreenOff(final Context context) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = new ComponentName(context,
                ScreenOffAdminReceiver.class);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            Log.i("GK", "Going to sleep now.");
            policyManager.lockNow();
        } else {
            Log.i("GK", "Not an admin");
            Toast.makeText(context, "NOT WORK ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void turnScreenOffAndExit() {
        // first lock screen
        turnScreenOff(getApplicationContext());

        // then provide feedback
       // ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

        // schedule end of activity
        final Activity activity = this;
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
					/* ignore this */
                }
                activity.finish();
            }
        };
        t.start();
    }
}
