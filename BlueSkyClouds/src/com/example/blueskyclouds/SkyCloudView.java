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

    private static final int CLOUD_COUNT = 6;

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

        for (int i = 0; i < CLOUD_COUNT; i++) {
            CloudSpec cloud = new CloudSpec();

            float depth = (float) i / (CLOUD_COUNT - 1); // 0: 远景, 1: 近景
            cloud.scale = lerp(0.38f, 1.0f, depth) * randomRange(0.92f, 1.08f);
            cloud.cx = w * lerp(0.12f, 0.9f, (i + 0.5f) / CLOUD_COUNT) + randomRange(-w * 0.03f, w * 0.03f);
            cloud.cy = h * lerp(0.16f, 0.5f, depth) + randomRange(-h * 0.025f, h * 0.025f);

            cloud.width = w * lerp(0.18f, 0.31f, depth) * cloud.scale;
            cloud.height = h * lerp(0.075f, 0.13f, depth) * cloud.scale;

            cloud.alphaBody = (int) lerp(145, 235, depth);
            cloud.alphaShadow = (int) lerp(26, 80, depth);
            cloud.alphaHighlight = (int) lerp(85, 165, depth);

            cloud.softOffsetX = cloud.width * randomRange(0.08f, 0.16f);
            cloud.softOffsetY = cloud.height * randomRange(0.1f, 0.2f);

            clouds.add(cloud);
        }
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
