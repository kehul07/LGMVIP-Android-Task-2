package com.kehuldroid.facedetection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button cameraButton;
    private final static int REQUEST_IMAGE_CAPTURE = 100;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;
    Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        String gifUrl = "https://www.sectechfield.com/img/face-scan-min.gif";
        Glide.with(this).asGif().load(gifUrl).into(imageView);

        FirebaseApp.initializeApp(this);

        cameraButton = findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(getPackageManager())!= null) {
                            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Error in capturing image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap)extra.get("data");
            detectFace(bitmap);
        }
    }

    private void detectFace(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.1f)
                        .build();

        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);

            detector.detectInImage(image).addOnSuccessListener(faces -> {
                        String resultText = "";
                        int i = 1;
                        for (FirebaseVisionFace face : faces) {
                            resultText = resultText.concat("\nFACE NUMBER. " + i + ": ")
                                    .concat("\nSmile: " + face.getSmilingProbability() * 100 + "%")
                                    .concat("\nleft eye open: " + face.getLeftEyeOpenProbability() * 100 + "%")
                                    .concat("\nright eye open " + face.getRightEyeOpenProbability() * 100 + "%")
                                    .concat("\nFace size: "+face.getBoundingBox().width()).concat(" x "+face.getBoundingBox().height());
                            i++;

                           canvas = new Canvas(bitmap);

                            FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                            FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                            FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                            FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);
                            FirebaseVisionFaceLandmark leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                            FirebaseVisionFaceLandmark bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                            FirebaseVisionFaceLandmark rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);


                            drawCircle(leftEye.getPosition().getX(), leftEye.getPosition().getY());
                            drawCircle(rightEye.getPosition().getX(), rightEye.getPosition().getY());
                            drawCircle2(nose.getPosition().getX(), nose.getPosition().getY());
                            drawCircle2(leftEar.getPosition().getX(), leftEar.getPosition().getY());
                            drawCircle2(rightEar.getPosition().getX(), rightEar.getPosition().getY());
                            drawCircle3(leftMouth.getPosition().getX(), leftMouth.getPosition().getY());
                            drawCircle3(bottomMouth.getPosition().getX(), bottomMouth.getPosition().getY());
                            drawCircle3(rightMouth.getPosition().getX(), rightMouth.getPosition().getY());


                            Rect bounds = face.getBoundingBox();
                            RectF rectF = new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom);
                            Paint paint = new Paint();
                            paint.setColor(Color.GREEN);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(3);
                            canvas.drawRect(rectF, paint);


                        }


                        if (faces.size() == 0) {
                            Toast.makeText(MainActivity.this, "NO FACE DETECT", Toast.LENGTH_SHORT).show();
                        } else {

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            Bundle bundle = new Bundle();
                            bundle.putString(LCOFaceDetection.RESULT_TEXT, resultText);
                            bundle.putByteArray("image",byteArray);

                            DialogFragment resultDialog = new ResultDialog();
                            resultDialog.setArguments(bundle);
                            resultDialog.setCancelable(true);
                            resultDialog.show(getSupportFragmentManager(), LCOFaceDetection.RESULT_DIALOG);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Oops, Something went wrong", Toast.LENGTH_SHORT).show();
                        Log.e("FaceDetection", "Face detection failed", e);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void drawCircle(float x, float y) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, 10, paint);
    }

    private void drawCircle2(float x, float y) {
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, 8, paint);
    }

    private void drawCircle3(float x, float y) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, 8, paint);
    }



}
