package com.example.blueskyclouds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

public class SkyCloudView extends View {

    private final Paint skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();

    public SkyCloudView(Context context) {
        super(context);
        cloudPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        drawSky(canvas, w, h);

        drawCloud(canvas, w * 0.22f, h * 0.28f, w * 0.16f, h * 0.07f);
        drawCloud(canvas, w * 0.62f, h * 0.2f, w * 0.2f, h * 0.08f);
        drawCloud(canvas, w * 0.45f, h * 0.38f, w * 0.13f, h * 0.06f);
    }

    private void drawSky(Canvas canvas, int width, int height) {
        Shader gradient = new LinearGradient(
                0,
                0,
                0,
                height,
                Color.rgb(64, 170, 255),
                Color.rgb(180, 230, 255),
                Shader.TileMode.CLAMP);
        skyPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, skyPaint);
        skyPaint.setShader(null);
    }

    private void drawCloud(Canvas canvas, float cx, float cy, float cloudW, float cloudH) {
        cloudPaint.setColor(Color.argb(240, 255, 255, 255));

        drawOval(canvas, cx - cloudW * 0.6f, cy, cloudW * 0.55f, cloudH * 0.9f);
        drawOval(canvas, cx, cy - cloudH * 0.2f, cloudW * 0.65f, cloudH * 1.05f);
        drawOval(canvas, cx + cloudW * 0.62f, cy + cloudH * 0.02f, cloudW * 0.52f, cloudH * 0.82f);
        drawOval(canvas, cx + cloudW * 0.2f, cy + cloudH * 0.28f, cloudW * 0.95f, cloudH * 0.75f);

        cloudPaint.setColor(Color.argb(100, 220, 235, 245));
        drawOval(canvas, cx + cloudW * 0.15f, cy + cloudH * 0.45f, cloudW * 0.7f, cloudH * 0.45f);
    }

    private void drawOval(Canvas canvas, float left, float top, float ovalW, float ovalH) {
        tempRect.set(left, top, left + ovalW, top + ovalH);
        canvas.drawOval(tempRect, cloudPaint);
    }
}
