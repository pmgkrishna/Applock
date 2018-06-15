package com.sara.applock.pin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

import com.sara.applock.R;


public class ImageFragment extends Fragment {
    // the fragment initialization parameters
    private static final String IMAGE = "image";
    private static final String POSITION = "position";
    private static final String LENGTH = "length";


    private int imageId;
    private int position;
    private int length;



    public ImageFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(int imageID,int position,int length) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt(IMAGE, imageID);
        args.putInt(POSITION,position);
        args.putInt(LENGTH,length);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageId = getArguments().getInt(IMAGE);
            position = getArguments().getInt(POSITION);
            length = getArguments().getInt(LENGTH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_image, container, false);
        FrameLayout frameLayout=(FrameLayout) view.findViewById(R.id.frame);
        if(position == length-1) {
            Button button = new Button(getContext());
            final FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.gravity= Gravity.BOTTOM;
            button.setText("Continue");
            button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.parseColor("#2f805e"));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sharedPreference = getContext().getSharedPreferences("userpref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreference.edit();
                    //value is stored in sharedpreference  as false only at first time
                    editor.putBoolean("First_Time_Start",false);
                    editor.commit();
                    Intent intent=new Intent(getActivity(),LockScreenActivity.class);
                    intent.putExtra("packageName",getActivity().getApplicationInfo().packageName);
                    //Toast.makeText(getContext(),getActivity().getApplicationInfo().packageName,Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    getActivity().finish();
                }
            });
            frameLayout.addView(button,params);
        }
        frameLayout.setBackgroundResource(imageId);
        return view;
    }
}
