package nl.droptables.psysperience.app.cardboard;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import jp.co.cyberagent.android.gpuimage.*;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.GPUImage.ScaleType;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

@TargetApi(11)
public class GPUImageRenderer implements GLSurfaceView.Renderer, Camera.PreviewCallback, CardboardView.StereoRenderer {
    public static final int NO_IMAGE = -1;
    static final float[] CUBE = new float[]{-1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F};
    private GPUImageFilter mFilter;
    public final Object mSurfaceChangedWaiter = new Object();
    private int mGLTextureId = -1;
    private SurfaceTexture mSurfaceTexture = null;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private IntBuffer mGLRgbBuffer;
    private int mOutputWidth;
    private int mOutputHeight;
    private int mImageWidth;
    private int mImageHeight;
    private int mAddedPadding;
    private final Queue<Runnable> mRunOnDraw;
    private final Queue<Runnable> mRunOnDrawEnd;
    private Rotation mRotation;
    private boolean mFlipHorizontal;
    private boolean mFlipVertical;
    private GPUImage.ScaleType mScaleType;

    public GPUImageRenderer(GPUImageFilter filter) {
        this.mScaleType = GPUImage.ScaleType.CENTER_CROP;
        this.mFilter = filter;
        this.mRunOnDraw = new LinkedList();
        this.mRunOnDrawEnd = new LinkedList();
        this.mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mGLCubeBuffer.put(CUBE).position(0);
        this.mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.setRotation(Rotation.NORMAL, false, false);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glDisable(2929);
        this.mFilter.init();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(this.mFilter.getProgram());
        this.mFilter.onOutputSizeChanged(width, height);
        this.adjustImageScaling();
        Object var4 = this.mSurfaceChangedWaiter;
        synchronized(this.mSurfaceChangedWaiter) {
            this.mSurfaceChangedWaiter.notifyAll();
        }
    }

    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(16640);
        this.runAll(this.mRunOnDraw);
        this.mFilter.onDraw(this.mGLTextureId, this.mGLCubeBuffer, this.mGLTextureBuffer);
        this.runAll(this.mRunOnDrawEnd);
        if(this.mSurfaceTexture != null) {
            this.mSurfaceTexture.updateTexImage();
        }

    }

    private void runAll(Queue<Runnable> queue) {
        synchronized(queue) {
            while(!queue.isEmpty()) {
                ((Runnable)queue.poll()).run();
            }

        }
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        final Camera.Size previewSize = camera.getParameters().getPreviewSize();
        if(this.mGLRgbBuffer == null) {
            this.mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
        }

        if(this.mRunOnDraw.isEmpty()) {
            this.runOnDraw(new Runnable() {
                public void run() {
                    GPUImageNativeLibrary.YUVtoRBGA(data, previewSize.width, previewSize.height, GPUImageRenderer.this.mGLRgbBuffer.array());
                    GPUImageRenderer.this.mGLTextureId = OpenGlUtils.loadTexture(GPUImageRenderer.this.mGLRgbBuffer, previewSize, GPUImageRenderer.this.mGLTextureId);
                    camera.addCallbackBuffer(data);
                    if(GPUImageRenderer.this.mImageWidth != previewSize.width) {
                        GPUImageRenderer.this.mImageWidth = previewSize.width;
                        GPUImageRenderer.this.mImageHeight = previewSize.height;
                        GPUImageRenderer.this.adjustImageScaling();
                    }

                }
            });
        }

    }

    public void setUpSurfaceTexture(final Camera camera) {
        this.runOnDraw(new Runnable() {
            public void run() {
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                GPUImageRenderer.this.mSurfaceTexture = new SurfaceTexture(textures[0]);

                try {
                    camera.setPreviewTexture(GPUImageRenderer.this.mSurfaceTexture);
                    camera.setPreviewCallback(GPUImageRenderer.this);
                    camera.startPreview();
                } catch (IOException var3) {
                    var3.printStackTrace();
                }

            }
        });
    }

    public void setFilter(final GPUImageFilter filter) {
        this.runOnDraw(new Runnable() {
            public void run() {
                GPUImageFilter oldFilter = GPUImageRenderer.this.mFilter;
                GPUImageRenderer.this.mFilter = filter;
                if(oldFilter != null) {
                    oldFilter.destroy();
                }

                GPUImageRenderer.this.mFilter.init();
                GLES20.glUseProgram(GPUImageRenderer.this.mFilter.getProgram());
                GPUImageRenderer.this.mFilter.onOutputSizeChanged(GPUImageRenderer.this.mOutputWidth, GPUImageRenderer.this.mOutputHeight);
            }
        });
    }

    public void deleteImage() {
        this.runOnDraw(new Runnable() {
            public void run() {
                GLES20.glDeleteTextures(1, new int[]{GPUImageRenderer.this.mGLTextureId}, 0);
                GPUImageRenderer.this.mGLTextureId = -1;
            }
        });
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if(bitmap != null) {
            this.runOnDraw(new Runnable() {
                public void run() {
                    Bitmap resizedBitmap = null;
                    if(bitmap.getWidth() % 2 == 1) {
                        resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas can = new Canvas(resizedBitmap);
                        can.drawARGB(0, 0, 0, 0);
                        can.drawBitmap(bitmap, 0.0F, 0.0F, (Paint)null);
                        GPUImageRenderer.this.mAddedPadding = 1;
                    } else {
                        GPUImageRenderer.this.mAddedPadding = 0;
                    }

                    GPUImageRenderer.this.mGLTextureId = OpenGlUtils.loadTexture(resizedBitmap != null?resizedBitmap:bitmap, GPUImageRenderer.this.mGLTextureId, recycle);
                    if(resizedBitmap != null) {
                        resizedBitmap.recycle();
                    }

                    GPUImageRenderer.this.mImageWidth = bitmap.getWidth();
                    GPUImageRenderer.this.mImageHeight = bitmap.getHeight();
                    GPUImageRenderer.this.adjustImageScaling();
                }
            });
        }
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        this.mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return this.mOutputWidth;
    }

    protected int getFrameHeight() {
        return this.mOutputHeight;
    }

    private void adjustImageScaling() {
        float outputWidth = (float)this.mOutputWidth;
        float outputHeight = (float)this.mOutputHeight;
        if(this.mRotation == Rotation.ROTATION_270 || this.mRotation == Rotation.ROTATION_90) {
            outputWidth = (float)this.mOutputHeight;
            outputHeight = (float)this.mOutputWidth;
        }

        float ratio1 = outputWidth / (float)this.mImageWidth;
        float ratio2 = outputHeight / (float)this.mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round((float)this.mImageWidth * ratioMax);
        int imageHeightNew = Math.round((float)this.mImageHeight * ratioMax);
        float ratioWidth = (float)imageWidthNew / outputWidth;
        float ratioHeight = (float)imageHeightNew / outputHeight;
        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(this.mRotation, this.mFlipHorizontal, this.mFlipVertical);
        if(this.mScaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1.0F - 1.0F / ratioWidth) / 2.0F;
            float distVertical = (1.0F - 1.0F / ratioHeight) / 2.0F;
            textureCords = new float[]{this.addDistance(textureCords[0], distHorizontal), this.addDistance(textureCords[1], distVertical), this.addDistance(textureCords[2], distHorizontal), this.addDistance(textureCords[3], distVertical), this.addDistance(textureCords[4], distHorizontal), this.addDistance(textureCords[5], distVertical), this.addDistance(textureCords[6], distHorizontal), this.addDistance(textureCords[7], distVertical)};
        } else {
            cube = new float[]{CUBE[0] / ratioHeight, CUBE[1] / ratioWidth, CUBE[2] / ratioHeight, CUBE[3] / ratioWidth, CUBE[4] / ratioHeight, CUBE[5] / ratioWidth, CUBE[6] / ratioHeight, CUBE[7] / ratioWidth};
        }

        this.mGLCubeBuffer.clear();
        this.mGLCubeBuffer.put(cube).position(0);
        this.mGLTextureBuffer.clear();
        this.mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0F?distance:1.0F - distance;
    }

    public void setRotationCamera(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        this.setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setRotation(Rotation rotation) {
        this.mRotation = rotation;
        this.adjustImageScaling();
    }

    public void setRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        this.mFlipHorizontal = flipHorizontal;
        this.mFlipVertical = flipVertical;
        this.setRotation(rotation);
    }

    public Rotation getRotation() {
        return this.mRotation;
    }

    public boolean isFlippedHorizontally() {
        return this.mFlipHorizontal;
    }

    public boolean isFlippedVertically() {
        return this.mFlipVertical;
    }

    protected void runOnDraw(Runnable runnable) {
        Queue var2 = this.mRunOnDraw;
        synchronized(this.mRunOnDraw) {
            this.mRunOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(Runnable runnable) {
        Queue var2 = this.mRunOnDrawEnd;
        synchronized(this.mRunOnDrawEnd) {
            this.mRunOnDrawEnd.add(runnable);
        }
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(16640);
        this.runAll(this.mRunOnDraw);
        this.mFilter.onDraw(this.mGLTextureId, this.mGLCubeBuffer, this.mGLTextureBuffer);
        this.runAll(this.mRunOnDrawEnd);
        if(this.mSurfaceTexture != null) {
            this.mSurfaceTexture.updateTexImage();
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(this.mFilter.getProgram());
        this.mFilter.onOutputSizeChanged(width, height);
        this.adjustImageScaling();
        Object var4 = this.mSurfaceChangedWaiter;
        synchronized(this.mSurfaceChangedWaiter) {
            this.mSurfaceChangedWaiter.notifyAll();
        }
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glDisable(2929);
        this.mFilter.init();
    }

    @Override
    public void onRendererShutdown() {

    }
}
