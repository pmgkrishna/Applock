package com.sara.applock.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sara.applock.R;
import com.sara.applock.home.HomeActivity;

import java.io.File;

public class IntruderDisplayActivity extends AppCompatActivity {
    ImageView imageView;
    Button save, delete;
    String packageName;
    int intrudersImageCount;
    File[] intrudersImageList;
    File sourceDirectory,saveDirectory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_display);
        Intent intent=getIntent();
        packageName=intent.getStringExtra("packageName");
        save =(Button)findViewById(R.id.button) ;
        delete =(Button)findViewById(R.id.button10) ;
        imageView =(ImageView)findViewById(R.id.imageView) ;
        sourceDirectory=new File(Environment.getExternalStorageDirectory() + "/.Applock");
        saveDirectory=new File(Environment.getExternalStorageDirectory() + "/Applock");
        intrudersImageList=sourceDirectory.listFiles();
        intrudersImageCount=intrudersImageList.length;
        setImage();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!saveDirectory.exists()) {
                    saveDirectory.mkdirs();
                }
                try {
                    File source=new File(sourceDirectory + "/" + intrudersImageList[intrudersImageCount-1].getName());
                    File destination=new File(saveDirectory + "/" + intrudersImageList[intrudersImageCount-1].getName());
                    Log.d("Test","Testing camera\n " + source + "\n" + destination + " \n"+intrudersImageList[intrudersImageCount-1] );
                    if(source.renameTo(destination)) {
                        Toast.makeText(getApplicationContext(), "Saved to " + saveDirectory, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                launch();
            }

        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File source=new File(sourceDirectory + "/" + intrudersImageList[intrudersImageCount-1].getName());
                Log.d("Test","Testing camera" + source + intrudersImageList[intrudersImageCount-1].getName() );
                boolean delete=source.delete();
                if(delete)
                    Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(),"not Deleted",Toast.LENGTH_LONG).show();
                launch();
            }
        });
    }
    public void setImage()
    {
        Bitmap bitmap;
        String imageFile=intrudersImageList[intrudersImageCount-1].getName();
        if(imageFile.endsWith("Intruder.jpg") || imageFile.endsWith("Intruder.png") || imageFile.endsWith("Intruder")) {
            bitmap = BitmapFactory.decodeFile(sourceDirectory + "/" + intrudersImageList[intrudersImageCount - 1].getName());
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            launch();
        }
    }
    public void launch()
    {
        if(--intrudersImageCount > 0)
        {
            setImage();
        }
        else {
            if (packageName.equals(getApplicationContext().getPackageName())) {
                startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                finish();
            } else {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(LaunchIntent);
                finish();
            }
        }
    }

}
