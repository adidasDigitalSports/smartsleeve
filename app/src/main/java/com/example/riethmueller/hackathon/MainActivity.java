package com.example.riethmueller.hackathon;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.virtualtrainer.*;

public class MainActivity extends AppCompatActivity implements SmartSleeve.SleeveSetupListener, SmartSleeve.SleeveListener {

    private final String TAG = "MainActivity";

    private Button mCalibration1Button;
    private Button mCalibration2Button;
    private Button mStartCalcButton;
    private Button mStopCalcButton;
    private Button mReconnectButton;
    private Button mDisconnectButton;
    private ToggleButton mVibrationToggleButton;
    private TextView mAngleTextView;
    private TextView mStatusTextView;
    private SmartSleeve mSmartSleeve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mStatusTextView = findViewById(R.id.textViewStatus);
        mCalibration1Button = findViewById(R.id.calib1Button);
        mCalibration2Button = findViewById(R.id.calib2Button);
        mStartCalcButton = findViewById(R.id.startCalcButton);
        mStopCalcButton = findViewById(R.id.stopCalcButton);
        mReconnectButton = findViewById(R.id.reconnectButton);
        mDisconnectButton = findViewById(R.id.disconnectButton);
        mVibrationToggleButton = findViewById(R.id.toggleVibration);
        mAngleTextView = findViewById(R.id.angleTextView);


        mCalibration1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.setEnabled(false);
                mSmartSleeve.startCalibrationOne();
            }
        });

        mCalibration2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mSmartSleeve.startCalibrationTwo();
            }
        });

        mStartCalcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mSmartSleeve.startReadingAngle();
                mStopCalcButton.setEnabled(true);
                mDisconnectButton.setEnabled(true);
                mStatusTextView.setText(getResources().getString(R.string.state_receiving));
            }
        });

        mStopCalcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mStartCalcButton.setEnabled(true);
                mStatusTextView.setText(getResources().getString(R.string.state_connected));
                mSmartSleeve.stopReadingAngle();
    }
});



        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mSmartSleeve.disconnect();
            }
        });

        mReconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSmartSleeve.connectWithSleeve();
            }
        });

        mVibrationToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mSmartSleeve.startVibration();
                }
                else
                {
                    mSmartSleeve.stopVibration();
                }
            }
        });

        mCalibration1Button.setEnabled(false);
        mCalibration2Button.setEnabled(false);
        mStartCalcButton.setEnabled(false);
        mStopCalcButton.setEnabled(false);
        mReconnectButton.setEnabled(false);
        mDisconnectButton.setEnabled(false);
        mVibrationToggleButton.setEnabled(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        new SmartSleeve.Builder(this,this, this ).build();
    }

    @Override
    public void onSleeveCreated(SmartSleeve sleeve) {
        Log.d(TAG, "Sleeve created.");
        mSmartSleeve = sleeve;
        mSmartSleeve.initialize();
        mReconnectButton.setEnabled(true);
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "Sleeve connected.");
        mCalibration1Button.setEnabled(true);
        mVibrationToggleButton.setEnabled(true);
        mReconnectButton.setEnabled(false);
        mDisconnectButton.setEnabled(true);
        mStatusTextView.setText(getResources().getString(R.string.state_connected));
        mStatusTextView.setTextColor(Color.GREEN);
    }

    @Override
    public void onDisconnected() {
        mReconnectButton.setEnabled(true);
        mVibrationToggleButton.setEnabled(false);
        mCalibration1Button.setEnabled(false);
        mCalibration2Button.setEnabled(false);
        mStartCalcButton.setEnabled(false);
        mStopCalcButton.setEnabled(false);
        mStatusTextView.setText(getResources().getString(R.string.state_disconnected));
        mStatusTextView.setTextColor(Color.RED);
    }

    @Override
    public void onCalibrationOneFinished() {
        Log.d(TAG, "Calibration 1 finished.");
        mCalibration2Button.setEnabled(true);
        mStatusTextView.setText(getResources().getString(R.string.state_calibrated1));
    }

    @Override
    public void onCalibrationTwoFinished() {
        Log.d(TAG, "Calibration 2 finished.");
        mStatusTextView.setText(getResources().getString(R.string.state_calibrated1));
        mStartCalcButton.setEnabled(true);
}

    @Override
    public void onConnectionError() {
        Log.e(TAG, "Cannot connect to smart sleeve.");
        Toast.makeText(this, "Err: Cannot connect to smart sleeve.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotPairedError() {
        Log.e(TAG, "Smart sleeve not paired.");
    }

    @Override
    public void onAngleValueChanged(float angle, float valgus) {
        updateAngle(angle);
    }


    private void updateAngle(final float angle)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAngleTextView.setText("angle: " + String.valueOf(angle));
            }
        });
    }
}
