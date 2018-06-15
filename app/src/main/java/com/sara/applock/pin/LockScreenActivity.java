package com.sara.applock.pin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static android.content.ContentValues.TAG;

import com.sara.applock.R;
import com.sara.applock.camera.APictureCapturingService;
import com.sara.applock.camera.IntruderDisplayActivity;
import com.sara.applock.camera.CameraCapture;
import com.sara.applock.camera.PictureCapturingListener;
import com.sara.applock.home.Conversion;
import com.sara.applock.home.HomeActivity;
import com.sara.applock.home.LockService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

public class LockScreenActivity extends AppCompatActivity implements View.OnClickListener,PictureCapturingListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    Button btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0,btnclr,btnbksp,btnforget;
    ImageView icon,circle1,circle2,circle3,circle4;
    EditText hiddenEditText;
    TextView message;
    int intrusion=0;
    int oldPin=0,pin =-1, confirmPin =-1;
    SharedPreferences pinSharedPreferences,lockSharedPreference;
    SharedPreferences.Editor editor;
    Boolean newUser, isQuestionRegistered;
    String packageName;
    byte[] iconByteArray;
    boolean isCurrentPackage;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        checkPermissions();
        pinSharedPreferences =getSharedPreferences("PINPREFERENCES", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_lock_screen);
        icon = (ImageView) findViewById(R.id.icon);
        Intent intent=getIntent();
        String pkg=intent.getStringExtra("packageName");
        if(pkg!=null)
        {
            packageName=pkg;
            isCurrentPackage=false;
            iconByteArray=intent.getByteArrayExtra("Icon");
            if(iconByteArray!=null) {
                icon.setImageDrawable(Conversion.convertToDrawable(this,iconByteArray));
            }
        }
        else
        {
            packageName=getApplicationContext().getPackageName();
            isCurrentPackage=true;
            icon.setImageResource(R.mipmap.ic_launcher);
        }
        lockSharedPreference=getSharedPreferences("Lock_Preference",MODE_PRIVATE);
        initialise();
        initializeLockService();
    }
    private void initializeLockService()
    {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Intent LockServiceIntent = new Intent
                    (LockScreenActivity.this, LockService.class);
            PendingIntent pendingIntent = PendingIntent.getService
                    (LockScreenActivity.this, 0, LockServiceIntent, 0);
            AlarmManager alarmManager = (AlarmManager) LockScreenActivity
                    .this.getSystemService(ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            alarmManager.setRepeating
                    (AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 100, pendingIntent);
        }
        else
        {
            /*Since the interval time value in alarmManager will be forced upto 60000 milliseconds as of Android 5.1,
            cannot use alarm manager to run the service every 100 milliseconds*/
            startService(new Intent(this, LockService.class));
        }
    }

    private void initialise() {
        circle1=(ImageView)findViewById(R.id.imageView1);
        circle2=(ImageView)findViewById(R.id.imageView2);
        circle3=(ImageView)findViewById(R.id.imageView3);
        circle4=(ImageView)findViewById(R.id.imageView4);
        message=(TextView)findViewById(R.id.message);
        (btn0=(Button)findViewById(R.id.button0)).setOnClickListener(this);
        (btn1=(Button)findViewById(R.id.button1)).setOnClickListener(this);
        (btn2=(Button)findViewById(R.id.button2)).setOnClickListener(this);
        (btn3=(Button)findViewById(R.id.button3)).setOnClickListener(this);
        (btn4=(Button)findViewById(R.id.button4)).setOnClickListener(this);
        (btn5=(Button)findViewById(R.id.button5)).setOnClickListener(this);
        (btn6=(Button)findViewById(R.id.button6)).setOnClickListener(this);
        (btn7=(Button)findViewById(R.id.button7)).setOnClickListener(this);
        (btn8=(Button)findViewById(R.id.button8)).setOnClickListener(this);
        (btn9=(Button)findViewById(R.id.button9)).setOnClickListener(this);
        (btnclr=(Button)findViewById(R.id.buttonclr)).setOnClickListener(this);
        (btnbksp =(Button)findViewById(R.id.buttonbackspace)).setOnClickListener(this);
        (btnforget=(Button)findViewById(R.id.buttonforgot)).setOnClickListener(this);
        hiddenEditText =(EditText)findViewById(R.id.editText);

        if(pinSharedPreferences.getBoolean("CHANGE_PASSWORD",false))
        {
            message.setText("Enter the Old PIN");
        }
        else if(pinSharedPreferences.getBoolean("FORGOT_PASSWORD",false))
        {
            message.setText("Enter new pin");
        }
        hiddenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int length=charSequence.length();
                int counter;
                ImageView[] circles = {circle1,circle2,circle3,circle4};
                for(counter=0;counter<4;counter++)
                {
                    circles[counter].setBackgroundResource(R.drawable.circle);
                }
                for(counter=0;counter<length;counter++)
                {
                    circles[counter].setBackgroundResource(R.drawable.circle1);
                }
                if(length==4)
                {
                    validatePin();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
        public void onClick (View v)
        {
            if (v == btnclr) {
                hiddenEditText.setText("");
            }
            else if(v == btnforget)
            {
                startActivity(new Intent(this,SecurityQuestionActivity.class));
                finish();
            }
            else if(v == btnbksp && !hiddenEditText.getText().toString().equals(""))
            {
              if(!pinSharedPreferences.getBoolean("CHANGE_PASSWORD",false))
              {
                  if (pin == -1) {
                      pin = Integer.valueOf(hiddenEditText.getText().toString());
                      if (pin > 9) {
                          pin = pin / 10;
                          String s1 = String.valueOf(pin);
                          hiddenEditText.setText(s1);
                      } else {
                          hiddenEditText.setText("");
                      }
                      pin = -1;
                  } else {
                      confirmPin = Integer.valueOf(hiddenEditText.getText().toString());
                      if (confirmPin > 9) {
                          confirmPin = confirmPin / 10;
                          String s1 = String.valueOf(confirmPin);
                          hiddenEditText.setText(s1);
                      } else {
                          hiddenEditText.setText("");
                      }
                      confirmPin = -1;
                  }
              }
              else
              {
                  pin = Integer.valueOf(hiddenEditText.getText().toString());
                  if (oldPin > 9) {
                      oldPin = oldPin / 10;
                      String s1 = String.valueOf(oldPin);
                      hiddenEditText.setText(s1);
                  } else {
                      hiddenEditText.setText("");
                  }
              }
            }
            else
            {
                Button b = (Button) v;
                hiddenEditText.append(b.getText().toString());
            }
        }

    private void validatePin() {
      if(!pinSharedPreferences.getBoolean("CHANGE_PASSWORD",false))
      {
          if (pin == -1) {
              //get the pin
              pin = Integer.valueOf(hiddenEditText.getText().toString());
              newUser = pinSharedPreferences.getBoolean("NEW_USER", true);
              //check whether confirmation pin is needed or not by user type
              checkUser();
          } else {
              //getting confirmation pin
              confirmPin = Integer.valueOf(hiddenEditText.getText().toString());
              //check both pin and confirmation pin
              if (pin == confirmPin) {
                  SharedPreferences.Editor editor = pinSharedPreferences.edit();
                  //store the security pin
                  editor.putBoolean("NEW_USER",false);
                  editor.putInt("Pin", pin);
                  editor.commit();
                  //count=0;
                  Toast.makeText(this, "Pin is set successfully", Toast.LENGTH_SHORT).show();
                  pinSharedPreferences = getSharedPreferences("PINPREFERENCES", Context.MODE_PRIVATE);
                  isQuestionRegistered = pinSharedPreferences.getBoolean("QNS_REGISTERED", false);
                  if (!isQuestionRegistered) {
                      startActivity(new Intent(LockScreenActivity.this, SecurityQuestionActivity.class));
                      finish();
                  } else {
                      startActivity(new Intent(LockScreenActivity.this, HomeActivity.class));
                      finish();
                  }
              } else {
                  hiddenEditText.setText("");
                  message.setText("Confirm your PIN");
                  Toast.makeText(this, "PIN doesn't Match", Toast.LENGTH_SHORT).show();
                  confirmPin = -1;
              }
          }
      }
      else
      {
          oldPin=Integer.valueOf(hiddenEditText.getText().toString());
          if(oldPin ==(pinSharedPreferences.getInt("Pin",-1)))
          {
              message.setText("Enter the new pin");
              hiddenEditText.setText("");
              editor= pinSharedPreferences.edit();
              editor.remove("CHANGE_PASSWORD");
              editor.remove("NEW_USER");
              editor.apply();
          }
          else
          {
              message.setText("Enter the correct old pin");
              hiddenEditText.setText("");
          }
      }
    }
    private void checkUser()
    {
        if(!newUser)
        {
          if(pin ==(pinSharedPreferences.getInt("Pin",-1)))
          {
              lockSharedPreference=getSharedPreferences("Lock_Preference",MODE_PRIVATE);
              editor=lockSharedPreference.edit();
              editor.putString(packageName,"Unlocked");
              editor.apply();
              Log.e(TAG, lockSharedPreference.getString(packageName,"") + " " +  packageName);
              hiddenEditText.setText("");
              launch();
          }
          else
          {
              intrusion++;
              hiddenEditText.setText("");
              pin =-1;
              if(intrusion > 2)
              {
                  APictureCapturingService pictureService = CameraCapture.getInstance(this);
                  //Captures image
                  pictureService.startCapturing(this);
              }
              Toast.makeText(this,"Invalid PIN",Toast.LENGTH_SHORT).show();
          }
        }
        else
        {
            pin =Integer.valueOf(hiddenEditText.getText().toString());
            message.setText("Confirm Your PIN");
            hiddenEditText.setText("");
        }
    }
    public void launch()
    {
        File sourceDirectory=new File(Environment.getExternalStorageDirectory() + "/.Applock");
        File[] imageList=sourceDirectory.listFiles();
        if(imageList!=null && imageList.length>0){
            startActivity(new Intent(this,IntruderDisplayActivity.class).putExtra("packageName",packageName).addFlags
                    (Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        }
        else if(packageName.equals(getApplicationContext().getPackageName()))
        {
            startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        }
        else
        {
            Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            startActivity(LaunchIntent);
            if(android.os.Build.VERSION.SDK_INT >= 21)
            {
                finishAndRemoveTask();
                Log.e(TAG,"finish and remove");
            }
            else
            {
                finish();
                Log.e(TAG,"finish");
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();finish();
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {

    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {

    }
    @SuppressLint("NewApi")
    private void checkPermissions() {
        final String[] requiredPermissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
        };
        final List<String> neededPermissions = new ArrayList<>();
        for (final String p : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    p) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(p);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startHome = new Intent(Intent.ACTION_MAIN);
        startHome.addCategory(Intent.CATEGORY_HOME);
        startHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startHome);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        pinSharedPreferences=getSharedPreferences("PINPREFERENCES",MODE_PRIVATE);
        editor=pinSharedPreferences.edit();
        editor.remove("FORGOT_PASSWORD");
        editor.remove("CHANGE_PASSWORD");
        editor.apply();
    }

}
