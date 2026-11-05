package com.datamint.twin;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;

/** Machine details + live sensor trend (temperature / pressure). */
public class MachineDetailActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long id;
    private String range = "1h";
    private final Runnable tick = new Runnable() {
        @Override public void run() { load(); handler.postDelayed(this, 10000); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_detail);
        if (getSupportActionBar() != null) { getSupportActionBar().setTitle("Machine Details"); getSupportActionBar().setDisplayHomeAsUpEnabled(true); }
        id = getIntent().getLongExtra("id", 0);
        int[] ids = {R.id.r1h, R.id.r6h, R.id.r24h, R.id.r7d};
        String[] ranges = {"1h", "6h", "24h", "7d"};
        for (int i = 0; i < ids.length; i++) {
            final String r = ranges[i];
            findViewById(ids[i]).setOnClickListener(v -> { range = r; loadTrend(); });
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
    @Override protected void onResume() { super.onResume(); handler.post(tick); }
    @Override protected void onPause() { super.onPause(); handler.removeCallbacks(tick); }

    private void load() {
        Net.call(ApiClient.get().machine(id), m -> {
            ((TextView) findViewById(R.id.tvName)).setText(m.name);
            ((TextView) findViewById(R.id.tvType)).setText(m.machineType + " · " + m.line + " · " + m.material);
            TextView st = findViewById(R.id.tvStatus);
            st.setText(Ui.statusLabel(m.status));
            st.setBackground(Ui.pill(Ui.statusColor(m.status), 60f));
            ((TextView) findViewById(R.id.tvReason)).setText(m.statusReason);
            ((TextView) findViewById(R.id.tvValues)).setText("Temperature   " + Ui.f(m.temperature, 1) + " °C\nMold temp   " + Ui.f(m.moldTemp, 1)
                    + " °C\nPressure   " + Ui.f(m.pressure, 1) + " bar\nRunning hours   " + Ui.f(m.runningHours, 1)
                    + " hrs\nDefect rate (7d)   " + Ui.f(m.defectRate, 1) + " %\nActive alerts   " + m.openAlerts
                    + "\nLast updated   " + Ui.dateTime(m.lastUpdated));
        }, msg -> Ui.toast(this, msg));
        loadTrend();
    }

    private void loadTrend() {
        Net.call(ApiClient.get().trend(id, range), pts -> {
            List<Entry> t = new ArrayList<>(), p = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < pts.size(); i++) {
                t.add(new Entry(i, (float) pts.get(i).temperature));
                p.add(new Entry(i, (float) pts.get(i).pressure));
                labels.add(pts.get(i).label);
            }
            LineDataSet temp = new LineDataSet(t, "Temperature °C");
            temp.setColor(0xFF2F8CFF); temp.setLineWidth(2f); temp.setDrawCircles(false); temp.setDrawValues(false);
            LineDataSet pres = new LineDataSet(p, "Pressure bar");
            pres.setColor(0xFFF59E0B); pres.setLineWidth(2f); pres.setDrawCircles(false); pres.setDrawValues(false);
            pres.setAxisDependency(YAxis.AxisDependency.RIGHT);

            LineChart chart = findViewById(R.id.chart);
            chart.setData(new LineData(temp, pres));
            chart.getDescription().setEnabled(false);
            chart.getLegend().setTextColor(Color.WHITE);
            chart.getAxisLeft().setTextColor(0xFF2F8CFF);
            chart.getAxisRight().setTextColor(0xFFF59E0B);
            XAxis x = chart.getXAxis();
            x.setTextColor(Color.WHITE);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.invalidate();
        }, null);
    }
}
