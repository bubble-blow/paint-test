package com.example.blueskyclouds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

public class SkyCloudView extends View {

    private static final int[] CLOUDS_PER_LAYER = new int[] { 2, 2, 2 };

    private final Paint skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();

    private final List<CloudSpec> clouds = new ArrayList<CloudSpec>();
    private final Random random = new Random(20260418L);

    private int lastW = -1;
    private int lastH = -1;

    public SkyCloudView(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        cloudBodyPaint.setStyle(Paint.Style.FILL);
        cloudShadowPaint.setStyle(Paint.Style.FILL);
        cloudHighlightPaint.setStyle(Paint.Style.FILL);

        cloudBodyPaint.setMaskFilter(new BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL));
        cloudShadowPaint.setMaskFilter(new BlurMaskFilter(22f, BlurMaskFilter.Blur.NORMAL));
        cloudHighlightPaint.setMaskFilter(new BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) {
            return;
        }

        if (w != lastW || h != lastH || clouds.isEmpty()) {
            rebuildClouds(w, h);
            lastW = w;
            lastH = h;
        }

        drawSky(canvas, w, h);

        for (int i = 0; i < clouds.size(); i++) {
            drawCloud(canvas, clouds.get(i));
        }
    }

    private void rebuildClouds(int w, int h) {
        clouds.clear();
        random.setSeed(20260418L + w * 31L + h * 17L);

        for (int layer = 0; layer < CLOUDS_PER_LAYER.length; layer++) {
            int count = CLOUDS_PER_LAYER[layer];
            float depth = (float) layer / (CLOUDS_PER_LAYER.length - 1); // 0远景 1近景

            for (int j = 0; j < count; j++) {
                CloudSpec cloud = new CloudSpec();

                // 每层有自己的随机横向分布，不再和尺寸绑定成单调序列
                float lane = ((float) j + randomRange(0.15f, 0.85f)) / count;
                cloud.cx = w * (0.08f + lane * 0.84f) + randomRange(-w * 0.08f, w * 0.08f);

                // 纵向按层控制范围，再在层内随机扰动
                float topBand = lerp(0.14f, 0.34f, depth);
                float bottomBand = lerp(0.3f, 0.56f, depth);
                cloud.cy = h * randomRange(topBand, bottomBand);

                float baseScale = lerp(0.48f, 1.0f, depth);
                cloud.scale = baseScale * randomRange(0.8f, 1.18f);

                cloud.width = w * lerp(0.16f, 0.28f, depth) * cloud.scale;
                cloud.height = h * lerp(0.07f, 0.12f, depth) * cloud.scale;

                cloud.alphaBody = (int) lerp(150, 235, depth) + (int) randomRange(-12f, 12f);
                cloud.alphaShadow = (int) lerp(24, 78, depth) + (int) randomRange(-8f, 8f);
                cloud.alphaHighlight = (int) lerp(80, 160, depth) + (int) randomRange(-10f, 10f);

                clampAlpha(cloud);

                cloud.softOffsetX = cloud.width * randomRange(0.08f, 0.18f);
                cloud.softOffsetY = cloud.height * randomRange(0.08f, 0.2f);

                clouds.add(cloud);
            }
        }
    }

    private void clampAlpha(CloudSpec cloud) {
        cloud.alphaBody = clamp(cloud.alphaBody, 120, 245);
        cloud.alphaShadow = clamp(cloud.alphaShadow, 12, 95);
        cloud.alphaHighlight = clamp(cloud.alphaHighlight, 40, 180);
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private void drawSky(Canvas canvas, int width, int height) {
        Shader gradient = new LinearGradient(
                0,
                0,
                0,
                height,
                new int[] {
                        Color.rgb(48, 130, 255),
                        Color.rgb(105, 185, 255),
                        Color.rgb(210, 238, 255) },
                new float[] { 0f, 0.62f, 1f },
                Shader.TileMode.CLAMP);
        skyPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, skyPaint);
        skyPaint.setShader(null);
    }

    private void drawCloud(Canvas canvas, CloudSpec cloud) {
        float baseX = cloud.cx;
        float baseY = cloud.cy;
        float cw = cloud.width;
        float ch = cloud.height;

        cloudShadowPaint.setColor(Color.argb(cloud.alphaShadow, 175, 198, 220));
        drawCloudCluster(canvas, baseX + cloud.softOffsetX, baseY + cloud.softOffsetY, cw, ch, cloudShadowPaint, 0.92f);

        cloudBodyPaint.setColor(Color.argb(cloud.alphaBody, 255, 255, 255));
        drawCloudCluster(canvas, baseX, baseY, cw, ch, cloudBodyPaint, 1.0f);

        cloudBodyPaint.setColor(Color.argb((int) (cloud.alphaBody * 0.66f), 246, 252, 255));
        drawCloudCluster(canvas, baseX + cw * 0.05f, baseY + ch * 0.1f, cw * 0.9f, ch * 0.78f, cloudBodyPaint, 0.75f);

        cloudHighlightPaint.setColor(Color.argb(cloud.alphaHighlight, 255, 255, 255));
        drawCloudCluster(canvas, baseX - cw * 0.12f, baseY - ch * 0.12f, cw * 0.62f, ch * 0.58f, cloudHighlightPaint, 0.55f);
    }

    private void drawCloudCluster(Canvas canvas, float x, float y, float w, float h, Paint paint, float density) {
        drawOval(canvas, x - w * 0.56f, y + h * 0.1f, w * 0.46f, h * 0.72f, paint);
        drawOval(canvas, x - w * 0.2f, y - h * 0.1f, w * 0.58f, h * 0.86f, paint);
        drawOval(canvas, x + w * 0.25f, y - h * 0.04f, w * 0.62f, h * 0.88f, paint);
        drawOval(canvas, x + w * 0.66f, y + h * 0.12f, w * 0.42f, h * 0.68f, paint);
        drawOval(canvas, x + w * 0.03f, y + h * 0.28f, w * 0.9f, h * 0.62f, paint);

        int extraCount = (int) (3 * density);
        for (int i = 0; i < extraCount; i++) {
            float rx = randomRange(-w * 0.28f, w * 0.58f);
            float ry = randomRange(-h * 0.06f, h * 0.42f);
            float rw = randomRange(w * 0.2f, w * 0.38f);
            float rh = randomRange(h * 0.26f, h * 0.46f);
            drawOval(canvas, x + rx, y + ry, rw, rh, paint);
        }
    }

    private void drawOval(Canvas canvas, float left, float top, float ovalW, float ovalH, Paint paint) {
        tempRect.set(left, top, left + ovalW, top + ovalH);
        canvas.drawOval(tempRect, paint);
    }

    private float randomRange(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    private float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static class CloudSpec {
        float cx;
        float cy;
        float width;
        float height;
        float scale;
        int alphaBody;
        int alphaShadow;
        int alphaHighlight;
        float softOffsetX;
        float softOffsetY;
    }
}
