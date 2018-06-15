package com.sara.applock.pin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sara.applock.R;

public class IntroActivity extends AppCompatActivity {
    SharedPreferences sharedPreference;
    Boolean firstStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        checkUser();
        ViewPager imageViewPager = (ViewPager) findViewById(R.id.pager);
        imageViewPager.setAdapter(new IntroPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(imageViewPager, true);
    }
    public void checkUser()
    {

        sharedPreference= getSharedPreferences("userpref", Context.MODE_PRIVATE);
        //returns true, if the user launches for the first time
        firstStart=sharedPreference.getBoolean("First_Time_Start",true);
        if(!firstStart)
        {
            //Skips introduction activty
            startActivity(new Intent(this,LockScreenActivity.class));
            finish();
        }
    }
}
