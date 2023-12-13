package com.example.alarmapp.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.alarmapp.R;
import com.example.alarmapp.databinding.ActivityRingBinding;
import com.example.alarmapp.model.Alarm;
import com.example.alarmapp.service.AlarmService;
import com.example.alarmapp.viewmodel.AlarmListViewModel;

import java.util.Calendar;
import java.util.Random;

public class RingActivity extends AppCompatActivity {
    Alarm alarm;
    private AlarmListViewModel alarmsListViewModel;
    private ActivityRingBinding ringActivityViewBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ringActivityViewBinding= ActivityRingBinding.inflate(getLayoutInflater());
        setContentView(ringActivityViewBinding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
/*        KeyguardManager keyguardManager=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE) ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(this, null);
            }*/
        alarmsListViewModel = new ViewModelProvider(this).get(AlarmListViewModel.class);
        Bundle bundle=getIntent().getBundleExtra(getString(R.string.bundle_alarm_obj));
        if (bundle!=null)
            alarm =(Alarm)bundle.getSerializable(getString(R.string.arg_alarm_obj));

        double result = 0;

        ringActivityViewBinding.activityRingDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String operator = ringActivityViewBinding.tvDismissOperator.getText().toString();
                String operand1Str = ringActivityViewBinding.tvDismissOperand1.getText().toString();
                String operand2Str = ringActivityViewBinding.tvDismissOperand2.getText().toString();

                int operand1, operand2, result = 0;

                try {
                    operand1 = Integer.parseInt(operand1Str);
                    operand2 = Integer.parseInt(operand2Str);

                    if (operator.equals("+")) {
                        result = operand1 + operand2;
                    } else if (operator.equals("-")) {
                        result = operand1 - operand2;
                    } else if (operator.equals("*")) {
                        result = operand1 * operand2;
                    } else if (operator.equals("/")) {
                        // Check for division by zero
                        if (operand2 != 0) {
                            result = operand1 / operand2;
                        } else {
                            // Handle division by zero error
                            // You may want to show an error message or take appropriate action
                        }
                    } else {
                        // Handle invalid operator error
                        // You may want to show an error message or take appropriate action
                    }
                } catch (NumberFormatException e) {
                    // Handle parsing error (e.g., if operand1Str or operand2Str is not a valid integer)
                    // You may want to show an error message or take appropriate action
                }
                EditText etResult = ringActivityViewBinding.etDismissResult;

// Your previous code for getting operator and operands

// Assuming 'result' is the correct result obtained from your calculations
                int userResult;
                try {
                    userResult = Integer.parseInt(etResult.getText().toString());
                } catch (NumberFormatException e) {
                    // Handle parsing error for user input
                    // You may want to show an error message or take appropriate action
                    return;
                }

                if (userResult == result) {
                    // Call dismissAlarm function
                    dismissAlarm();
                } else {
                    // Handle the case where the user's input does not match the expected result
                }
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false);
            setTurnScreenOn(false);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
    }

    private void dismissAlarm(){
        if(alarm!=null) {
            alarm.setStarted(false);
            alarm.cancelAlarm(getBaseContext());
            alarmsListViewModel.update(alarm);
        }
        Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
        getApplicationContext().stopService(intentService);
        finish();
    }

    private void snoozeAlarm(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 5);

        if(alarm!=null){
            alarm.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            alarm.setMinute(calendar.get(Calendar.MINUTE));
            alarm.setTitle("Snooze "+alarm.getTitle());
        }
        else {
            alarm = new Alarm(
                    new Random().nextInt(Integer.MAX_VALUE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    "Snooze",
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    RingtoneManager.getActualDefaultRingtoneUri(getBaseContext(), RingtoneManager.TYPE_ALARM).toString(),
                    false
            );
        }
        alarm.schedule(getApplicationContext());

        Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
        getApplicationContext().stopService(intentService);
        finish();
    }
}
