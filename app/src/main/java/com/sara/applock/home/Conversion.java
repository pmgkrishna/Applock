package com.sara.applock.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

/**
 * Created by Hariharan on 14-06-2017.
 */

public class Conversion {
    public static byte[] convertToByteArray(Drawable icon) {
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();
        return bitmapData;
    }
    public static Drawable convertToDrawable(Context context,byte[] bitmapData)
    {
        if(bitmapData!=null) {
            Bitmap iconBitMap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
            return new BitmapDrawable(context.getResources(), iconBitMap);
        }
        return null;
    }
}
