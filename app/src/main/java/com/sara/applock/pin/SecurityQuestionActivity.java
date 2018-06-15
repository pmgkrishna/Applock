package com.sara.applock.pin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sara.applock.R;
import com.sara.applock.home.HomeActivity;

public class SecurityQuestionActivity extends AppCompatActivity {
    Button submit;
    EditText answer;
    Spinner question;
    Boolean isQuestionRegistered;
    SharedPreferences sharedUserPreferences;
    TextView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);
        message=(TextView)findViewById(R.id.message);
        sharedUserPreferences =getSharedPreferences("PINPREFERENCES", Context.MODE_PRIVATE);
        isQuestionRegistered = sharedUserPreferences.getBoolean("QNS_REGISTERED",false);
        if(!isQuestionRegistered)
        {
            message.setText("Select a security question");
        }
        else
        {
            message.setText("Answer your security question");
        }
        question =(Spinner)findViewById(R.id.spinner);
        answer =(EditText)findViewById(R.id.editText2);
        submit =(Button)findViewById(R.id.button);
        message=(TextView)findViewById(R.id.message);
        ArrayAdapter<CharSequence> questionAdapter=ArrayAdapter.createFromResource(this,
                R.array.spinnerItems, android.R.layout.simple_list_item_1);
        questionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        question.setAdapter(questionAdapter);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Answer= answer.getText().toString();
                isQuestionRegistered = sharedUserPreferences.getBoolean("QNS_REGISTERED",false);
                if(!isQuestionRegistered)
                {
                    answerSubmitting(Answer);
                }
                else
                {
                    answerChecking(Answer);
                }
            }
        });
    }
    public void answerSubmitting(String answer){
        if(!answer.equalsIgnoreCase("")){
            String ques = question.getSelectedItem().toString();
            SharedPreferences.Editor editor= sharedUserPreferences.edit();
            editor.putString(ques,answer);
            editor.putBoolean("QNS_REGISTERED",true);
            editor.apply();
            Toast.makeText(this,"Questions are registered successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        else
        {
            Toast.makeText(this,"Please answer for The Question" ,Toast.LENGTH_SHORT).show();
        }
    }

    public void answerChecking(String s1){
        String Question = question.getSelectedItem().toString();
        if((sharedUserPreferences.getString(Question,"")).equalsIgnoreCase(s1))
        {
            sharedUserPreferences=getSharedPreferences("PINPREFERENCES",MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedUserPreferences.edit();
            //editor.putBoolean("NEW_USER",true);
            editor.putBoolean("FORGOT_PASSWORD",true);
            editor.remove("CHANGE_PASSWORD");
            editor.remove("NEW_USER");
            editor.apply();
            startActivity(new Intent(this, LockScreenActivity.class));
            finish();
        }
        else
        {
            Toast.makeText(this,Question + " " +s1 + " " +(sharedUserPreferences.getString(Question,"")),Toast.LENGTH_SHORT).show();
        }
    }
}
