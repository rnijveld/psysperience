package nl.droptables.psysperience.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import jp.co.cyberagent.android.gpuimage.*;
import nl.droptables.psysperience.app.helper.CameraHelper;
import nl.droptables.psysperience.app.helper.GPUImageFilterTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import nl.droptables.psysperience.app.cardboard.GPUImage;
import nl.droptables.psysperience.app.helper.CameraHelper;
import nl.droptables.psysperience.app.helper.GPUImageFilterTools;

import javax.microedition.khronos.egl.EGLConfig;

public class GpuImageActivity extends Activity implements View.OnClickListener {

    private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private int numberOfFilters = 11;
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler;
    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.gpu_image_activity);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.surfaceView);
        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(cardboardView);

        mCameraHelper = new CameraHelper();
        mCamera = new CameraLoader();

        View cameraSwitchView = findViewById(R.id.img_switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!mCameraHelper.hasFrontCamera() || !mCameraHelper.hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
            new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        showSystemUi();
                    } else {
                        hideSystemUi();
                    }
                }
            }
        );
        applyFilters();
    }

    private void setupGestureDetector() {
        mGestureDetector = new GestureDetector(this,
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    hideSystemUi();
                    return true;
                }
            }
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            this.hideSystemUi();
        } else {
            this.showSystemUi();
        }
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.onResume();
        setupGestureDetector();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.img_switch_camera:
                mCamera.switchCamera();
                break;
        }
    }

    public void applyFilters() {
        applyRandomFilter(-1);
    }

    public void applyRandomFilter(int rand) {
        if (rand == -1) {
            rand = MediaActivity.randInt(0, numberOfFilters);
        }

        switch (rand) {
            case 0:
                switchFilterTo(new GPUImageContrastFilter(2.0f));
                break;
            case 1:
                switchFilterTo(new GPUImageHueFilter(90.0f));
                break;
            case 2:
                switchFilterTo(new GPUImageGammaFilter(2.0f));
                break;
            case 3:
                switchFilterTo(new GPUImageBrightnessFilter(1.5f));
                break;
            case 4:
                switchFilterTo(new GPUImageSepiaFilter());
                break;
            case 5:
                switchFilterTo(new GPUImageSobelEdgeDetection());
                break;
            case 6:
                switchFilterTo(new GPUImageEmbossFilter());
                break;
            case 7:
                switchFilterTo(new GPUImageSaturationFilter(1.0f));
                break;
            case 8:
                switchFilterTo(new GPUImageExposureFilter(0.0f));
                break;
            case 9:
                switchFilterTo(new GPUImageMonochromeFilter(1.0f, new float[]{0.6f, 0.45f, 0.3f, 1.0f}));
                break;
            case 10:
                switchFilterTo(new GPUImageSwirlFilter());
                mFilterAdjuster.adjust(5);
                break;
            case 11:
                switchFilterTo(new GPUImageColorBalanceFilter());
                break;
        }
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    private class CameraLoader {

        private int mCurrentCameraId = 0;
        private Camera mCameraInstance;

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Camera.Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCameraInstance.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    GpuImageActivity.this, mCurrentCameraId);
            CameraHelper.CameraInfo2 cameraInfo = new CameraHelper.CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /** A safe way to get an instance of the Camera object. */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }

    }
}
