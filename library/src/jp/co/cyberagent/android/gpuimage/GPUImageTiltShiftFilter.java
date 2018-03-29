package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

/**
 * A simulated tilt shift lens effect
 */
public class GPUImageTiltShiftFilter extends GPUImageTwoInputFilter {
    private static final String TAG = "GPUImageTiltShiftFilter";
    public static final String SHADER = "" +
            " varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " uniform lowp float topFocusLevel;\n" +
            " uniform lowp float bottomFocusLevel;\n" +
            " uniform lowp float mFocusFallOffRate;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 sharpImageColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     lowp vec4 blurredImageColor = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     \n" +
            "     lowp float blurIntensity = 1.0 - smoothstep(topFocusLevel - mFocusFallOffRate, topFocusLevel, textureCoordinate2.y);\n" +
            "     blurIntensity += smoothstep(bottomFocusLevel, bottomFocusLevel + mFocusFallOffRate, textureCoordinate2.y);\n" +
            "     \n" +
            "     gl_FragColor = mix(sharpImageColor, blurredImageColor, blurIntensity);\n" +
            " }\n";

    private int mTopFocusLevelLocation;
    private int mBottomFocusLevelLocation;
    private int mFocusFallOffRate;

    private GPUImageGaussianBlurFilter mBlurFilter;

    private float mTilt = 0.5f;

    public GPUImageTiltShiftFilter(){
        this(0.5f);
    }

    public GPUImageTiltShiftFilter(float tilt){
        super(SHADER);

        mTilt = tilt;

        mBlurFilter = new GPUImageGaussianBlurFilter();
        mBlurFilter.setBlurSize(2);
    }

    @Override
    public void onInit() {
        Log.v(TAG, "onInit >>");

        super.onInit();

        mTopFocusLevelLocation = GLES20.glGetUniformLocation(getProgram(), "topFocusLevel");
        mBottomFocusLevelLocation = GLES20.glGetUniformLocation(getProgram(), "bottomFocusLevel");
        mFocusFallOffRate = GLES20.glGetUniformLocation(getProgram(), "mFocusFallOffRate");

        Log.v(TAG, "onInit <<");
    }

    @Override
    public void onInitialized() {
        Log.v(TAG, "onInitialized >>");

        super.onInitialized();
        setTilt(mTilt);
        setFocusFallOffRate(0.3f);

        Log.v(TAG, "onInitialized <<");
    }

    public void setTilt(final float tilt){
        Log.v(TAG, "setTilt >>");
        Log.v(TAG, "setTilt tilt = " + tilt);
        mTilt = tilt;

        setFloat(mTopFocusLevelLocation, tilt);
        setFloat(mBottomFocusLevelLocation, tilt);
        Log.v(TAG, "setTilt <<");
    }

    public void setFocusFallOffRate(final float rate)
    {
        Log.v(TAG, "setFocusFallOffRate >>");
        Log.v(TAG, "setFocusFallOffRate rate = " + rate);
        setFloat(mFocusFallOffRate, rate);
        Log.v(TAG, "setFocusFallOffRate <<");

    }

    public void setBitmap(Context context, final Bitmap bitmap){
       GPUImage gpuImage = new GPUImage(context);
       gpuImage.setImage(bitmap);
       gpuImage.setFilter(mBlurFilter);

       Bitmap old = super.getBitmap();
       if(old != null && !old.isRecycled()){
           old.recycle();
       }
       super.setBitmap(gpuImage.getBitmapWithFilterApplied());
    }
}
