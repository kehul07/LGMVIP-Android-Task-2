package com.kehuldroid.facedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class displayPhoto extends AppCompatActivity {
    ImageView photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_photo);

        photo = findViewById(R.id.imageView2);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        photo.setImageBitmap(bitmap);

// Now you have the Bitmap in 'bitmap' variable

    }
}