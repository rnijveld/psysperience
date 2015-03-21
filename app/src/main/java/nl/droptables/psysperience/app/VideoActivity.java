package nl.droptables.psysperience.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.*;
import nl.droptables.psysperience.app.util.AutoFitTextureView;
import nl.droptables.psysperience.app.util.SystemUiHider;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


/**
 * An activity that activates the camera for the input images, feeds them through
 * a pipeline, and displays the resulting video.
 */
public class VideoActivity extends Activity {
    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    /**
     * The logger to use for this class.
     */
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private CameraDevice camera = null;
    private CameraCharacteristics characteristics = null;
    private Size previewSize = null;
    private AutoFitTextureView textureView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        textureView = (AutoFitTextureView)findViewById(R.id.fullscreen_content);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, SystemUiHider.FLAG_HIDE_NAVIGATION);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    @Override
                    public void onVisibilityChange(boolean visible) {
                        if (visible) {
                            // Schedule a hide().
                            delayedHide(3000);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSystemUiHider.show();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Set<String> cameras = new HashSet<>();
        try {
            for(String camera : manager.getCameraIdList()) {
                cameras.add(camera);
            }
        } catch(CameraAccessException cae) {
            new AlertDialog.Builder(this)
                    .setTitle("Camera access failed")
                    .setMessage("Failed to access camera: " + cae.getMessage())
                    .setCancelable(false)
                    .create().show();
            return;
        }

        if(cameras.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Camera access failed")
                    .setMessage("No cameras found on this device.")
                    .setCancelable(false)
                    .create().show();
            return;
        }

        // Open the first camera
        openCamera(cameras.iterator().next());
    }

    public void openCamera(String cameraId) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        final VideoActivity thisActivity = this;

        try {
            characteristics = manager.getCameraCharacteristics(cameraId);

            logger.info("Going to dump " + characteristics.getKeys().size() + " characteristics...");
            for(CameraCharacteristics.Key x : characteristics.getKeys()) {
                logger.info("Camera characteristic '" + x.getName() + "': " + characteristics.get(x).toString());
            }

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    camera = cameraDevice;
                    if (textureView.isAvailable()) {
                        openCameraCaptureSession(textureView.getWidth(), textureView.getHeight());
                    } else {
                        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                            @Override
                            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                                                  int width, int height) {
                                logger.info("Surface texture available!");
                                openCameraCaptureSession(width, height);
                            }

                            @Override
                            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                                    int width, int height) {
                                logger.severe("Surface texture size changed");
                                configureTransform(thisActivity, previewSize, textureView, width, height);
                            }

                            @Override
                            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                                return true;
                            }

                            @Override
                            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                            }
                        });
                    }
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice) {
                    new AlertDialog.Builder(thisActivity)
                            .setTitle("Camera access failed")
                            .setMessage("Camera disconnected")
                            .setCancelable(false)
                            .create().show();
                    return;
                }

                @Override
                public void onError(CameraDevice cameraDevice, int i) {
                    logger.severe("Error on camera: " + i);
                    new AlertDialog.Builder(thisActivity)
                            .setTitle("Camera access failed")
                            .setMessage("Error opening camera: " + i)
                            .create().show();
                    return;
                }
            }, null);
        } catch(CameraAccessException cae) {
            new AlertDialog.Builder(this)
                    .setTitle("Camera access failed")
                    .setMessage("Failed to access camera: " + cae.getMessage())
                    .create().show();
            return;
        }
    }

    private void openCameraCaptureSession(int width, int height) {
        final VideoActivity thisActivity = this;

        // Choose the sizes for camera preview and video recording
        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        previewSize = map.getOutputSizes(SurfaceTexture.class)[0];

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
        } else {
            textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }
        configureTransform(thisActivity, previewSize, textureView, width, height);

        try {
            final CaptureRequest.Builder previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            if(surfaceTexture == null) {
                logger.severe("surfaceTexture is null, abort");
                return;
            }

            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            List<Surface> surfaces = new ArrayList<>();
            Surface surface = new Surface(surfaceTexture);
            surfaces.add(surface);
            previewBuilder.addTarget(surface);

            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try {
                        cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                logger.info("We've captured a frame! " + result.getFrameNumber());
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        new AlertDialog.Builder(thisActivity)
                                .setTitle("Camera access failed")
                                .setMessage("Camera access exception: " + e.getMessage())
                                .create().show();
                        return;
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    logger.severe("Configure camera failed: " + cameraCaptureSession);
                    new AlertDialog.Builder(thisActivity)
                            .setTitle("Camera access failed")
                            .setMessage("Camera session configuration failed")
                            .create().show();
                    return;
                }
            }, null);
        } catch (CameraAccessException e) {
            new AlertDialog.Builder(thisActivity)
                    .setTitle("Camera access failed")
                    .setMessage("Camera access exception: " + e.getMessage())
                    .create().show();
            return;
        }
    }

    @Override
    public void onPause() {
        if (camera != null) {
            camera.close();
            camera = null;
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(Activity activity, Size previewSize, TextureView textureView, int viewWidth, int viewHeight) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }
}