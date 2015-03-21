package nl.droptables.psysperience.app.helper;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;

public class CameraHelper {

    public CameraHelper() {

    }

    public Camera openFrontCamera() {
        return openCameraFacing(CameraInfo.CAMERA_FACING_FRONT);
    }

    public Camera openBackCamera() {
        return openCameraFacing(CameraInfo.CAMERA_FACING_BACK);
    }

    public boolean hasFrontCamera() {
        return hasCamera(CameraInfo.CAMERA_FACING_FRONT);
    }

    public boolean hasBackCamera() {
        return hasCamera(CameraInfo.CAMERA_FACING_BACK);
    }

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public Camera openCamera(final int id) {
        return Camera.open(id);
    }

    public Camera openDefaultCamera() {
        return Camera.open(0);
    }

    public boolean hasCamera(final int facing) {
        return getCameraId(facing) != -1;
    }

    public Camera openCameraFacing(final int facing) {
        return Camera.open(getCameraId(facing));
    }

    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        cameraInfo.facing = info.facing;
        cameraInfo.orientation = info.orientation;
    }

    private int getCameraId(final int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo info = new CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }

    public void setCameraDisplayOrientation(final Activity activity,
                                            final int cameraId, final Camera camera) {
        int result = getCameraDisplayOrientation(activity, cameraId);
        camera.setDisplayOrientation(result);
    }

    public int getCameraDisplayOrientation(final Activity activity, final int cameraId) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(cameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static class CameraInfo2 {
        public int facing;
        public int orientation;
    }
}
