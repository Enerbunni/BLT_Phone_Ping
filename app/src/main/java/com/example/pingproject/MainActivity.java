package com.example.pingproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private Button startButton, stopButton;
    private TextView text, timeText;
    private EditText intervalInput, phoneNumberInput;
    Handler handler = new Handler();

    private int interval;
    private boolean debugMode;
    private String phoneNumber;

    private Runnable updateTime;

    //---Onclick listener for the start button
    private final View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handler.post(updateTime);
        }
    };

    //---Onclick listener for the stop button
    private final View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handler.removeCallbacks(updateTime);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---Toggles debugging for sending SMS
        debugMode = true;

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        //---Disables buttons so the user cant start sending messages without a phone number
        startButton.setEnabled(false);
        stopButton.setEnabled(false);

        timeText = findViewById(R.id.time_Results);
        text = findViewById(R.id.textView);
        intervalInput = findViewById(R.id.interval_Input);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);

        //---requests permission to send SMS at runtime for newer phones
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);

        //---listener for interval change
        intervalInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    //---Converts the seconds entered by the user
                    interval = Math.abs(1000 * Integer.parseInt(v.getText().toString()));
                }
                return false;
            }
        });


        //---listener for phone number changes
        phoneNumberInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    phoneNumber = v.getText().toString();
                    startButton.setEnabled(true);
                    stopButton.setEnabled(true);
                }
                return false;
            }
        });

        startButton.setOnClickListener(startListener);
        stopButton.setOnClickListener(stopListener);

        //---Function to be repeated every 'interval'
        updateTime = new Runnable() {
            @Override
            public void run() {
                //---send time to phone number
                sendMessage(phoneNumber, getStringTime());

                //---prints to time result
                timeText.setText(getStringTime());

                handler.postDelayed(this, interval);
            }
        };
    }

    //---Sends a SMS message to 'number' with the text 'message' (includes debugging)
    private void sendMessage(String number, String message)
    {

        String SENT = "SMS_SENT";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);


        //---when the SMS has been sent
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        text.setText("SMS Sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        text.setText("Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        text.setText("No service");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        SmsManager sms = SmsManager.getDefault();

        if(debugMode)
        {
            sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
        }
        else
        {
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }

    }

    //---Returns the current time in hour:minute:second format
    private String getStringTime()
    {
        return new SimpleDateFormat("hh:mm:ss").format((new Date()));
    }
}