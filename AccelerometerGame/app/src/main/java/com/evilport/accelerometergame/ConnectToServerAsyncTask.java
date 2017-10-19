package com.evilport.accelerometergame;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by dibak on 10/14/2017.
 */

class ConnectToServerAsyncTask extends AsyncTask<String, Void, String>{
    
    private ConnectToServerAsyncTask.Response delegate = null;
    private boolean userCancelled = false;
    private boolean connected = false;
    private boolean serverDisconnected = false;
    GlobalVariables globalVariables = GlobalVariables.globalVariables;

    ConnectToServerAsyncTask(Response delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... strings) {
        String ip = strings[0];
        int port = Integer.parseInt(strings[1]);

        try {
            InetAddress servAddr = InetAddress.getByName(ip);
            Socket socket = new Socket(servAddr, port);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String incomingMessage;
            connected = true;
            delegate.getResponse(true, userCancelled, serverDisconnected);
            while (!isCancelled()) {
                float x = globalVariables.x;
                float y = globalVariables.y;
                float z = globalVariables.z;
                double s = globalVariables.s;
                String operation = globalVariables.operation;

                printWriter.printf("%.3f %.3f %.3f %.1f %s\n", x, y, z, s, operation);
                incomingMessage = bufferedReader.readLine();
                globalVariables.numLockState = incomingMessage;
                printWriter.flush();
                if (incomingMessage == null) {
                    serverDisconnected = true;
                    break;
                }
            }
            socket.close();
            printWriter.close();

        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        delegate.getResponse(connected, userCancelled, serverDisconnected);
    }

    @Override
    protected void onCancelled() {
        delegate.getResponse(connected, true, serverDisconnected);
    }

    interface Response {
        void getResponse(boolean connected, boolean userCancelled, boolean serverDisconnected);
    }

}
