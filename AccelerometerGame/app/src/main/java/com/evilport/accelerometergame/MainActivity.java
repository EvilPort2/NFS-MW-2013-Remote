package com.evilport.accelerometergame;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ConnectToServerAsyncTask.Response {

    EditText editIP, editPort, editSensitivity;
    Button btnStream, btnFunction;
    Sensor sensor;
    SensorManager sensorManager;
    float x, y, z;
    double s;
    boolean connected = false;
    ConnectToServerAsyncTask asyncTaskObject = new ConnectToServerAsyncTask(MainActivity.this);
    GlobalVariables globalVariables = GlobalVariables.globalVariables;
    ExtraFunctionsButtonStateThread extraFunctionsButtonStateThread = new ExtraFunctionsButtonStateThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        s = globalVariables.s = 10;
        x = globalVariables.x;
        y = globalVariables.y;
        z = globalVariables.z;
        editIP = (EditText) findViewById(R.id.editTextIP);
        editPort = (EditText) findViewById(R.id.editTextPort);
        btnStream = (Button) findViewById(R.id.buttonStream);
        btnFunction = (Button) findViewById(R.id.buttonFunctions);
        editSensitivity = (EditText) findViewById(R.id.editTextSensitivity);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        sensorManager.registerListener(accelerationListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);

        extraFunctionsButtonStateThread.start();

        editSensitivity.setText(String.format("%.1f", s));
        editSensitivity.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                String str = editSensitivity.getText().toString();
                if (!str.equalsIgnoreCase("")) {
                    try {
                        s = Double.parseDouble(str);
                        if (s < 1 || s > 20) {
                            s = 10;
                            editSensitivity.setText(String.format("%.1f", s));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        s = 1;
                        editSensitivity.setText(String.format("%.1f", s));
                    }
                } else {
                    s = 10;
                    editSensitivity.setText(String.format("%.1f", s));
                }
                globalVariables.s = s;
                return false;
            }
        });


        btnStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = editIP.getText().toString();
                String port = editPort.getText().toString();

                if (!connected) {
                    if (ip.equalsIgnoreCase("")) {
                        Toast.makeText(MainActivity.this, "IP address is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (port.equalsIgnoreCase("")) {
                        Toast.makeText(MainActivity.this, "Port is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    btnStream.setText("Connecting....");
                    asyncTaskObject.execute(ip, port);
                } else {
                    asyncTaskObject.cancel(true);
                    btnStream.setText("Start Streaming");
                    connected = false;
                    asyncTaskObject = new ConnectToServerAsyncTask(MainActivity.this);
                }
            }
        });


        btnFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getApplicationContext())) {
                        startActivity(new Intent(MainActivity.this, FunctionsActivity.class));
                    }
                    else {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }

            }
        });
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
        extraFunctionsButtonStateThread = new ExtraFunctionsButtonStateThread();
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(accelerationListener);
        extraFunctionsButtonStateThread.interrupt();
        super.onStop();
    }

    public void decreaseSensitivity(View view) {
        if (s > 1) {
            s -= .1;
            editSensitivity.setText(String.format("%.1f", s));
        }
        globalVariables.s = s;
    }

    public void increaseSensitivity(View view) {
        if (s < 20) {
            s += .1;
            editSensitivity.setText(String.format("%.1f", s));
        }
        globalVariables.s = s;
    }

    @Override
    public void getResponse(boolean connected, boolean userCancelled, boolean serverDisconnected) {
        if (serverDisconnected) {
            Toast.makeText(this, "Server disconnected", Toast.LENGTH_SHORT).show();
            this.connected = false;
            btnStream.setText("Start Streaming");
            btnStream.setClickable(true);
            GlobalVariables.globalVariables = new GlobalVariables();
            asyncTaskObject = new ConnectToServerAsyncTask(MainActivity.this);
            return;
        }

        if (userCancelled) {
            Toast.makeText(this, "Connection terminated by user", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!connected) {
            Toast.makeText(this, "Could not connect to host. Probably the host is unreachable or has timed-out.", Toast.LENGTH_SHORT).show();
            this.connected = false;
            GlobalVariables.globalVariables = new GlobalVariables();
            btnStream.setText("Start Streaming");
            btnStream.setClickable(true);
            asyncTaskObject = new ConnectToServerAsyncTask(MainActivity.this);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Connection successful", Toast.LENGTH_SHORT).show();
                    btnStream.setClickable(true);
                    btnStream.setText("Stop Streaming");
                    btnFunction.setClickable(true);

                }
            });
            this.connected = true;
        }
    }

    private class ExtraFunctionsButtonStateThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                if (connected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnFunction.setEnabled(true);
                            btnFunction.setClickable(true);
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnFunction.setEnabled(false);
                            btnFunction.setClickable(false);
                        }
                    });
                }
            }
        }
    }
}
