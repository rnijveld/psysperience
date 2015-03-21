package nl.droptables.psysperience.app.filter;

import jp.co.cyberagent.android.gpuimage.*;
import nl.droptables.psysperience.app.cardboard.GPUImage;
import nl.droptables.psysperience.app.helper.GPUImageFilterTools;

import java.util.Random;

public class FilterStack {
    private long mTouches;
    private long mFrequency;
    private GPUImage mImage;
    private Random mRand;

    public FilterStack(GPUImage image) { this(image, 100); }

    public FilterStack(GPUImage image, long frequency) {
        mFrequency = frequency;
        mImage = image;
        mRand = new Random();
    }

    public void touch() {
        mTouches += 1;

        if (mTouches % mFrequency == 0) {
            update();
        }
    }

    public void update() {
        update(mRand.nextInt());
    }

    public void update(int filter) {
        GPUImageFilter f = null;
        switch (Math.abs(filter) % 8) {
            case 0:
                f = new GPUImageContrastFilter(2.0f);
                break;
            case 1:
                f = new GPUImageHueFilter(90.0f);
                break;
            case 2:
                f = new GPUImageGammaFilter(2.0f);
                break;
            case 3:
                f = new GPUImageHueFilter(70.0f);
                break;
            case 4:
                f = new GPUImageSepiaFilter();
                break;
            case 5:
                f = new GPUImageHueFilter(50.0f);
                break;
            case 6:
                f = new GPUImageEmbossFilter();
                break;
            case 7:
                f = new GPUImageSwirlFilter();
                GPUImageFilterTools.FilterAdjuster mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(f);
                mFilterAdjuster.adjust(5);
                break;
            case 8:
                f = new GPUImageSobelEdgeDetection();
                break;
        }
        mImage.setFilter(f);
    }
}
