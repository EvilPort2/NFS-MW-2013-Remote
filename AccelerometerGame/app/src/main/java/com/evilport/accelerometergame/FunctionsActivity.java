package com.evilport.accelerometergame;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class FunctionsActivity extends AppCompatActivity {

    int i = 0, j = 0;
    Button btnControl, btnHandbrake, btnChangeCar, btnMenu, btnStartRace;
    SeekBar seekBarSensitivity;
    TextView txtSensitivity;
    Sensor sensor;
    SensorManager sensorManager;
    GlobalVariables globalVariables = GlobalVariables.globalVariables;
    ChangeControlButtonStateThread changeControlButtonStateThread = new ChangeControlButtonStateThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functions);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        btnControl = (Button) findViewById(R.id.buttonCarControl);
        btnHandbrake = (Button) findViewById(R.id.buttonHandBrake);
        btnChangeCar = (Button) findViewById(R.id.buttonChangeCar);
        btnMenu = (Button) findViewById(R.id.buttonGameMenu);
        btnStartRace = (Button) findViewById(R.id.buttonStartRace);
        txtSensitivity = (TextView) findViewById(R.id.textViewSensitivity);
        seekBarSensitivity = (SeekBar) findViewById(R.id.seekBarSensitivity);
        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d("i", i + "");
                double sensitivity = (double) (i + 5) / 5d;
                Log.d("sensitivity", sensitivity + "");
                if (sensitivity > 20) {
                    sensitivity = 20.0;
                }
                globalVariables.s = sensitivity;
                txtSensitivity.setText(Double.toString(sensitivity));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //changeControlButtonStateThread.start();

        btnHandbrake.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    globalVariables.operation = "Handbrake";
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    globalVariables.operation = "ControlOn";
                }
                return false;
            }
        });

        btnChangeCar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    globalVariables.operation = "ChangeCar";
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    globalVariables.operation = "ControlOn";
                }
                return false;
            }
        });

        btnMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    globalVariables.operation = "Menu";
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    globalVariables.operation = "ControlOn";
                }
                return false;
            }
        });

        btnStartRace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    globalVariables.operation = "StartRace";
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    globalVariables.operation = "ControlOn";
                }
                return false;
            }
        });
    }


    public void screenBrightness(View view) {
        ContentResolver cResolver = getContentResolver();
        Window window = getWindow();
        int brightness;
        try {
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
        brightness = 0;
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        layoutpars.screenBrightness = brightness / (float) 255;
        window.setAttributes(layoutpars);
    }

    public void controlCar(View view) {
        if (globalVariables.operation.equalsIgnoreCase("ControlOff")) {
            globalVariables.operation = "ControlOn";
            btnControl.setText("Stop Controlling Car");
        } else {
            globalVariables.operation = "ControlOff";
            btnControl.setText("Start controlling car");
        }
    }



    SensorEventListener accelerationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            globalVariables.x = Float.parseFloat(String.format("%.3f", sensorEvent.values[0]));
            globalVariables.y = Float.parseFloat(String.format("%.3f", sensorEvent.values[1]));
            globalVariables.z = Float.parseFloat(String.format("%.3f", sensorEvent.values[2]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerationListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        changeControlButtonStateThread = new ChangeControlButtonStateThread();
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(accelerationListener);
        changeControlButtonStateThread.interrupt();
        super.onStop();
    }

    private class ChangeControlButtonStateThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                if (globalVariables.operation.equalsIgnoreCase("ControlOff")) {
                    //globalVariables.operation = "ControlOn";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnControl.setText("Stop Controlling Car");
                        }
                    });

                } else {
                    //globalVariables.operation = "ControlOff";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnControl.setText("Start controlling car");
                        }
                    });
                }
            }
        }
    }
}
