package com.datamint.twin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Digital factory view: the factory picture with one live marker per machine.
 * Machines A, B and C get the same label cards as in the reference design (drawn over the picture's own labels).
 */
public class FactoryMapView extends FrameLayout {
    public interface Listener { void onMachine(Models.Machine m); }

    // x, y, w, h in % of the picture (same values as the web app)
    private static final float[][] BOX = {{14.1f, 18.8f, 12.3f, 21.7f}, {52.2f, 5.5f, 12.2f, 21.2f}, {80.9f, 31.7f, 12.2f, 17.5f}};
    private static final String[] BOX_CODE = {"A", "B", "C"};

    private final List<Models.Machine> machines = new ArrayList<>();
    private final List<View> dots = new ArrayList<>();
    private final List<TextView> labels = new ArrayList<>();
    private final List<Integer> labelBox = new ArrayList<>();
    private Listener listener;

    public FactoryMapView(Context c) { this(c, null); }

    public FactoryMapView(Context c, AttributeSet a) {
        super(c, a);
        ImageView img = new ImageView(c);
        img.setImageResource(R.drawable.factory);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(img, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setListener(Listener l) { listener = l; }

    public void setMachines(List<Models.Machine> list) {
        for (View v : dots) removeView(v);
        for (View v : labels) removeView(v);
        dots.clear(); labels.clear(); labelBox.clear(); machines.clear();
        if (list == null) return;
        machines.addAll(list);
        float dp = getResources().getDisplayMetrics().density;
        for (Models.Machine m : machines) {
            int color = Ui.statusColor(m.status);
            int box = -1;
            for (int i = 0; i < BOX_CODE.length; i++) if (BOX_CODE[i].equals(m.code)) box = i;
            if (box >= 0) {
                TextView t = new TextView(getContext());
                GradientDrawable bg = Ui.pill(0xF5081428, 8 * dp);
                bg.setStroke((int) (2 * dp), color);
                t.setBackground(bg);
                t.setTextColor(Color.WHITE);
                t.setGravity(Gravity.CENTER_VERTICAL);
                t.setPadding((int) (4 * dp), 0, (int) (2 * dp), 0);
                t.setText("OFFLINE".equals(m.status) ? m.name + "\nOffline"
                        : m.name + "\n" + Ui.f(m.temperature, 0) + "°C\n" + Ui.statusLabel(m.status));
                t.setOnClickListener(v -> { if (listener != null) listener.onMachine(m); });
                addView(t, new LayoutParams(10, 10));
                t.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(getWidth(), 1) * 0.028f);
                labels.add(t);
                labelBox.add(box);
            }
            View dot = new View(getContext());
            GradientDrawable d = Ui.pill(color, 100 * dp);
            d.setStroke((int) (2 * dp), Color.WHITE);
            dot.setBackground(d);
            dot.setOnClickListener(v -> { if (listener != null) listener.onMachine(m); });
            addView(dot, new LayoutParams((int) (16 * dp), (int) (16 * dp)));
            dots.add(dot);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int w = getMeasuredWidth(), h = getMeasuredHeight();
        for (int i = 0; i < labels.size(); i++) {
            float[] bx = BOX[labelBox.get(i)];
            labels.get(i).measure(MeasureSpec.makeMeasureSpec((int) (bx[2] / 100f * w), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) (bx[3] / 100f * h), MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (TextView tv : labels) tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, w * 0.028f);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int w = r - l, h = b - t;
        for (int i = 0; i < labels.size(); i++) {
            float[] bx = BOX[labelBox.get(i)];
            TextView tv = labels.get(i);
            int left = (int) (bx[0] / 100f * w), top = (int) (bx[1] / 100f * h);
            tv.layout(left, top, left + tv.getMeasuredWidth(), top + tv.getMeasuredHeight());
        }
        for (int i = 0; i < dots.size(); i++) {
            Models.Machine m = machines.get(i);
            if (m.posX == null || m.posY == null) continue;
            View d = dots.get(i);
            int cx = (int) (m.posX / 100.0 * w), cy = (int) (m.posY / 100.0 * h), rad = d.getMeasuredWidth() / 2;
            d.layout(cx - rad, cy - rad, cx + rad, cy + rad);
        }
    }
}
