package com.example.wifilocalizationtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.WriteAbortedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView wifiText;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private Set<String> totalAPs = new HashSet<String>();
    private Point point;
    private Point nearestPoint;
    private ArrayList<Point> totalPoints = new ArrayList<Point>();
    Map<String, Integer> minLevel = new HashMap<String, Integer>();

    private Point tempPoint;
    int maxSize = 0;
    double minDistance;
    double distance[];
    int mini;

    private Button start;
    private Button calculate;
    private EditText X, Y, times, interval, fileNamEditText;

    private Handler handler = new Handler() {
        /**
         * ?????(1)??????????????е??AP????????Σ???????????????totalPoints?У?
         * (2)??????AP???level????С?????,(3)???????????д???????
         */
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                if (count != timesValue) {// ???д?????{\


                    wifiList = wifiManager.getScanResults();

                    if (maxSize < wifiList.size()) {
                        tempPoint.aps.clear();
                        maxSize = wifiList.size();
                        info += String.valueOf(count) + "\n";
                        // write(String.valueOf(count)+"\n");
                        for (int i = 0; i < wifiList.size(); i++) {
                            String SSID = null;
                            AP ap = new AP();
                            SSID = wifiList.get(i).SSID;

                            if (totalAPs.contains(SSID)) {
                                if (minLevel.get(SSID) > wifiList.get(i).level)
                                    minLevel.put(SSID, wifiList.get(i).level);
                            } else {
                                totalAPs.add(SSID);
                                minLevel.put(SSID, wifiList.get(i).level);
                            }

                            ap.SSID = SSID;
                            ap.level = wifiList.get(i).level;
                            tempPoint.aps.add(ap);
                            info += "BSSID:" + wifiList.get(i).BSSID
                                    + "  level:" + wifiList.get(i).level + " "
                                    + "frequency" + wifiList.get(i).frequency
                                    + "\n";
                            // write("BSSID:" + wifiList.get(i).BSSID +
                            // "  level:"
                            // + wifiList.get(i).level + " ");
                        }
                        // ps.println();
                        info += "\n";
                        // write("\n");
                    } else {
                        info += String.valueOf(count) + "\n";
                        // write(String.valueOf(count)+"\n");
                        for (int i = 0; i < wifiList.size(); i++) {
                            String SSID = null;
                            SSID = wifiList.get(i).SSID;

                            totalAPs.add(SSID);

                            info += "BSSID:" + wifiList.get(i).BSSID
                                    + "  level:" + wifiList.get(i).level + " "
                                    + "frequency" + wifiList.get(i).frequency
                                    + "\n";
                            // write("BSSID:" + wifiList.get(i).BSSID +
                            // "  level:"
                            // + wifiList.get(i).level + " ");
                        }
                        // ps.println();
                        info += "\n";
                        // write("\n");
                    }
                } else {
                    timer.cancel();
                    X.getText().clear();
                    Y.getText().clear();
                    times.getText().clear();
                    interval.getText().clear();
                    count = 0;
                    info += "\n";
                    writeToFile(fileName, info);
                    info = "";

                }

            }
        }
    };

    private int count = 0;
    private int APcount;
    private int timesValue;

    public String fileName;
    PrintStream ps;

    String info = "";

    String path = "";

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        X = (EditText) findViewById(R.id.x);
        Y = (EditText) findViewById(R.id.y);
        times = (EditText) findViewById(R.id.times);
        fileNamEditText = (EditText) findViewById(R.id.fileName);
        interval = (EditText) findViewById(R.id.interval);
        wifiText = (TextView) findViewById(R.id.wifi);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        start = (Button) findViewById(R.id.start);
        calculate = (Button) findViewById(R.id.cal);
        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                timesValue = Integer.parseInt(times.getText().toString()) + 1;

                tempPoint = new Point();

                tempPoint.aps.clear();
                tempPoint.x = -1;
                tempPoint.y = -1;

                tempPoint.x = Integer.valueOf(X.getText().toString());
                tempPoint.y = Integer.valueOf(Y.getText().toString());

                maxSize = 0;
                timer = new Timer();
                APcount = 0;
                fileName = fileNamEditText.getText().toString();

                wifiManager.startScan();
                wifiText.setText("\nStarting Scan...\n");

                info += (new Date().toLocaleString());
                info += " X= " + X.getText().toString() + " Y= "
                        + Y.getText().toString() + " ??????"
                        + times.getText().toString() + " ?????"
                        + interval.getText().toString() + "ms\n";

                timer.schedule(new TimerTask() {//?????

                    @Override
                    public void run() {
                        handler.sendEmptyMessage(0x123);
                        count++;
                        System.out.println(count);
                    }
                }, 0, Integer.parseInt(interval.getText().toString()));

                totalPoints.add(tempPoint);

            }
        });

        calculate.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                int i;
                nearestPoint = calculate(); //??????


                //??????
                StringBuilder sBuilder = new StringBuilder();
                for (i = 0; i < totalPoints.size() - 1; i++) {
                    sBuilder.append("\n");
                    sBuilder.append("Point" + (i + 1) + "X= "
                            + totalPoints.get(i).x + " Y= "
                            + totalPoints.get(i).y + "\n");
                    ArrayList<AP> aps = totalPoints.get(i).aps;
                    for (int j = 0; j < aps.size(); j++) {
                        sBuilder.append(aps.get(j).SSID + " "
                                + aps.get(j).level + "\n");
                    }
                    sBuilder.append("Distance:" + distance[i] + "\n\n");
                }

                sBuilder.append("The measure point: X= " + totalPoints.get(i).x
                        + " Y= " + totalPoints.get(i).y + "\n");
                ArrayList<AP> aps = totalPoints.get(i).aps;
                for (int j = 0; j < aps.size(); j++) {
                    sBuilder.append(aps.get(j).SSID + " " + aps.get(j).level
                            + "\n");
                }

                sBuilder.append("\nSo the nearestPoint is :Point " + (mini + 1)
                        + "\n distance is:" + minDistance);

                wifiText.setText(sBuilder.toString());

            }

        });

    }
    private Point calculate() {
        minDistance = Double.MAX_VALUE;
        mini = -1;
        double tempDistance;
        distance = new double[totalPoints.size() - 1];

        Point endPoint = totalPoints.get(totalPoints.size() - 1);

        for (int i = 0; i < totalPoints.size() - 1; i++) {
            tempDistance = calculate_Distance(endPoint, totalPoints.get(i));
            distance[i] = tempDistance;
            if (tempDistance < minDistance) {
                minDistance = tempDistance;
                mini = i;
            }
        }

        return totalPoints.get(mini);

    }
    private double calculate_Distance(Point point1, Point point2) {
        float result = 0.0f;
        String str;
        Map<String, Integer> tempMap1 = new HashMap<String, Integer>();
        Map<String, Integer> tempMap2 = new HashMap<String, Integer>();

        int i, j;

        for (j = 0; j < point2.aps.size(); j++) {
            tempMap2.put(point2.aps.get(j).SSID, point2.aps.get(j).level);
        }

        for (i = 0; i < point1.aps.size(); i++) {
            tempMap1.put(point1.aps.get(i).SSID, point1.aps.get(i).level);
        }

        Iterator<String> iterator = totalAPs.iterator();
        while (iterator.hasNext()) {
            str = iterator.next();
            if (tempMap1.containsKey(str) && tempMap2.containsKey(str)) {
                result += (tempMap1.get(str) - tempMap2.get(str))
                        * (tempMap1.get(str) - tempMap2.get(str));
            }

            if (tempMap1.containsKey(str) && !tempMap2.containsKey(str)) {
                result += (tempMap1.get(str) - minLevel.get(str))
                        * (tempMap1.get(str) - minLevel.get(str));
            }

            if (!tempMap1.containsKey(str) && tempMap2.containsKey(str)) {
                result += (tempMap2.get(str) - minLevel.get(str))
                        * (tempMap2.get(str) - minLevel.get(str));
            }
        }

        return Math.sqrt(result);
    }
    private void writeToFile(String fileName, String content) {

        /*
         * File targetFile = new File("/download/" + fileName); if
         * (!targetFile.exists()) { // ?????????? Just????
         *
         * targetFile.createNewFile(); } OutputStreamWriter osw = null; osw =
         * new OutputStreamWriter(new FileOutputStream("/download/" + fileName,
         * true)); osw.write(content); System.out.println(content); osw.close();
         */

        try {

            File file = new File("/mnt/sdcard", fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(file, true);
            out.write(content.getBytes());
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        wifiManager.startScan();
        wifiText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
    }
}
