package com.sara.applock.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

import static java.lang.System.exit;

public class CameraCapture extends APictureCapturingService {

    private static final String TAG = CameraCapture.class.getSimpleName();

    private boolean cameraClosed;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    private TreeMap<String, byte[]> picturesTaken;
    private PictureCapturingListener capturingListener;
    private String currentCameraId;
    //camera ids queue;
    private Queue<String> cameraIds;

    /***
     * constructor.
     *
     * @param activity the activity used to get display manager and the application context
     */
    CameraCapture(Activity activity) {
        super(activity);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void startCapturing(PictureCapturingListener listener) {
        this.picturesTaken = new TreeMap<>();
        this.capturingListener = listener;
        this.cameraIds = new LinkedList<>();
        try {

            final String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > 1) {
                this.cameraIds.addAll(Arrays.asList(cameraIds));
                this.currentCameraId = "1";
                openCamera();
            } else {
                capturingListener.onDoneCapturingAllPhotos(picturesTaken);
            }
        } catch ( CameraAccessException e) {
            Log.e(TAG, "Exception occurred while accessing the list of cameras", e);
        }
    }
    public static APictureCapturingService getInstance(final Activity activity) {
        return new CameraCapture(activity);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        Log.d(TAG, "opening camera " + currentCameraId);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(currentCameraId, stateCallback, null);
            }
        } catch ( CameraAccessException e) {
            Log.e(TAG, " exception occurred while opening camera " + currentCameraId, e);
        }
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imReader) {
            final Image image = imReader.acquireLatestImage();
            final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            final byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            try {
                CameraCapture.this.saveImageToDisk(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.close();
        }
    };
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void saveImageToDisk(final byte[] bytes) throws IOException
    {
        String file_path = Environment.getExternalStorageDirectory() +
                "/.Applock";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss",
               java.util.Locale.getDefault()).format(new Date());

        File file = new File(dir,timeStamp + "Intruder.jpg");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            this.picturesTaken.put(file.getPath(), bytes);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        exit(0);
    }



    @SuppressLint("NewApi")
    private void closeCamera() {
        Log.d(TAG, "closing camera " + cameraDevice.getId());
        if (null != cameraDevice && !cameraClosed) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }
    @SuppressLint("NewApi")
    private final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (picturesTaken.lastEntry() != null) {
                capturingListener.onCaptureDone(picturesTaken.lastEntry().getKey(), picturesTaken.lastEntry().getValue());

                Log.i(TAG, "done taking picture from camera " + cameraDevice.getId());
            }
            closeCamera();
        }
    };
    @SuppressLint("NewApi")
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraClosed = false;
            Log.d(TAG, "camera " + camera.getId() + " opened");
            cameraDevice = camera;
            Log.i(TAG, "Taking picture from camera " + camera.getId());
            //Take the picture after some delay. It may resolve getting a black dark photos.
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        takePicture();
                    } catch (CameraAccessException e) {
                        Log.e(TAG, " exception occurred while taking picture from " + currentCameraId, e);
                    }
                }
            }
                    , 500);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, " camera " + camera.getId() + " disconnected");
            if (cameraDevice != null && !cameraClosed) {
                cameraClosed = true;
                cameraDevice.close();
            }
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            cameraClosed = true;
            Log.d(TAG, "camera " + camera.getId() + " closed");
            //once the current camera has been closed, start taking another picture

        }


        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "camera in error, int code " + error);
            if (cameraDevice != null && !cameraClosed) {
                cameraDevice.close();
            }

        }


        public void takePicture() throws CameraAccessException {


            if (null == cameraDevice) {
                Log.e(TAG, "cameraDevice is null");
                return;
            }
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap != null) {
                jpegSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            }
            final boolean jpegSizesNotEmpty = jpegSizes != null && 0 < jpegSizes.length;
            int width = jpegSizesNotEmpty ? jpegSizes[0].getWidth() : 720;
            int height = jpegSizesNotEmpty ? jpegSizes[0].getHeight() : 1280;
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            final List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(reader.getSurface());
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_OFF);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF);
            captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, -20);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,200);
            captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 22000L);


            reader.setOnImageAvailableListener(onImageAvailableListener, null);
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, " exception occurred while accessing " + currentCameraId, e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        }
    };
}


