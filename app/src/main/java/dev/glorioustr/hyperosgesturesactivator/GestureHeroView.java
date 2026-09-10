package dev.glorioustr.hyperosgesturesactivator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/** A lightweight animated illustration for the gesture-navigation status card. */
final class GestureHeroView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();
    private final RectF orbit = new RectF();
    private final RectF phone = new RectF();
    private final RectF navigationPill = new RectF();
    private ValueAnimator animator;
    private Shader orbitShader;
    private Shader swipeShader;
    private float progress;
    private float centerX;
    private float swipeBottom;
    private float swipeTop;
    private boolean active;
    private boolean aeroGlass;
    private boolean darkMode;

    GestureHeroView(Context context, boolean aeroGlass, boolean darkMode) {
        super(context);
        this.aeroGlass = aeroGlass;
        this.darkMode = darkMode;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        if (getWidth() > 0 && getHeight() > 0) {
            updateShaders(getWidth(), getHeight());
        }
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    private void startAnimation() {
        if (animator != null) {
            return;
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1800L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(value -> {
            progress = (float) value.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        centerX = width * 0.5f;
        swipeBottom = height * 0.79f;
        swipeTop = height * 0.29f;
        orbit.set(width * 0.12f, height * 0.14f, width * 0.88f, height * 0.92f);
        phone.set(width * 0.32f, height * 0.05f, width * 0.68f, height * 0.92f);
        navigationPill.set(centerX - dp(24), height * 0.83f,
                centerX + dp(24), height * 0.86f);
        updateShaders(width, height);
    }

    private void updateShaders(int width, int height) {
        int blue = active ? Color.rgb(32, 186, 255) : Color.rgb(128, 145, 180);
        int purple = active ? Color.rgb(145, 92, 255) : Color.rgb(147, 151, 170);
        int warm = active ? Color.rgb(255, 94, 143) : Color.rgb(175, 154, 168);
        orbitShader = new LinearGradient(0, 0, width, height,
                new int[]{blue, purple, warm}, null, Shader.TileMode.CLAMP);
        swipeShader = new LinearGradient(centerX, swipeBottom, centerX, swipeTop,
                new int[]{blue, purple, warm}, null, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float cx = centerX;

        int blue = active ? Color.rgb(32, 186, 255) : Color.rgb(128, 145, 180);
        int purple = active ? Color.rgb(145, 92, 255) : Color.rgb(147, 151, 170);
        int warm = active ? Color.rgb(255, 94, 143) : Color.rgb(175, 154, 168);
        int phoneEdge = darkMode ? Color.argb(220, 224, 237, 255) : Color.rgb(81, 95, 124);
        int phoneFill = darkMode ? Color.argb(aeroGlass ? 150 : 220, 6, 17, 38)
                : (aeroGlass ? Color.argb(155, 242, 248, 255) : Color.rgb(245, 248, 255));

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(8));
        paint.setAlpha(aeroGlass ? 150 : 92);
        paint.setShader(orbitShader);
        canvas.drawArc(orbit, 196, 276, false, paint);
        paint.setShader(null);
        paint.setAlpha(255);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(phoneFill);
        canvas.drawRoundRect(phone, dp(22), dp(22), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(phoneEdge);
        canvas.drawRoundRect(phone, dp(22), dp(22), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(darkMode ? Color.argb(215, 210, 224, 255) : Color.rgb(70, 82, 108));
        canvas.drawRoundRect(navigationPill, dp(8), dp(8), paint);

        float bottom = swipeBottom;
        float top = swipeTop;
        float y = bottom - ((bottom - top) * progress);
        float glow = 0.35f + (0.65f * (1f - Math.abs(0.5f - progress) * 2f));
        paint.setShader(swipeShader);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setAlpha(Math.round(170 * glow));
        canvas.drawLine(cx, bottom - dp(8), cx, top + dp(8), paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        canvas.drawCircle(cx, y, dp(7), paint);
        paint.setShader(null);

        arrow.reset();
        arrow.moveTo(cx, top - dp(7));
        arrow.lineTo(cx - dp(12), top + dp(7));
        arrow.lineTo(cx - dp(5), top + dp(7));
        arrow.lineTo(cx, top + dp(1));
        arrow.lineTo(cx + dp(5), top + dp(7));
        arrow.lineTo(cx + dp(12), top + dp(7));
        arrow.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? warm : purple);
        canvas.drawPath(arrow, paint);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
