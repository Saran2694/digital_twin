package com.datamint.twin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Historical data analytics (6-month data set). */
public class AnalyticsFragment extends Fragment {
    private View root;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        root = inflater.inflate(R.layout.fragment_analytics, container, false);
        Api api = ApiClient.get();

        Net.call(api.overview(), o -> {
            if (!isAdded()) return;
            ((TextView) root.findViewById(R.id.tvKpi)).setText(String.format(Locale.US,
                    "Period: %s → %s\nInspected: %d   Good: %d   Damaged: %d\nYield: %.1f %%   Scrap: %d   Rework: %d\n"
                            + "Avg cycle: %.1f s   Avg pour temp: %.1f °C   Avg pressure: %.1f bar",
                    o.from, o.to, o.total, o.good, o.damaged, o.yieldPct, o.scrap, o.rework, o.avgCycle, o.avgPourTemp, o.avgPressure));
        }, m -> { if (isAdded()) Ui.toast(requireContext(), m); });

        Net.call(api.analyticsTrend("week"), rows -> {
            if (!isAdded()) return;
            List<Entry> total = new ArrayList<>(), dmg = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                total.add(new Entry(i, rows.get(i).total));
                dmg.add(new Entry(i, rows.get(i).damaged));
                labels.add(rows.get(i).period.substring(5));
            }
            LineDataSet a = line(total, "Inspected", 0xFF2F8CFF), b = line(dmg, "Damaged", 0xFFEF4444);
            LineChart chart = root.findViewById(R.id.chartTrend);
            chart.setData(new LineData(a, b));
            style(chart);
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.invalidate();
        }, null);

        Net.call(api.group("defectType"), rows -> {
            if (!isAdded()) return;
            List<BarEntry> e = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) { e.add(new BarEntry(i, rows.get(i).total)); labels.add(rows.get(i).name); }
            bar(R.id.chartDefects, e, labels, "Defects", 0xFFF5A623);
        }, null);

        Net.call(api.group("line"), lines -> Net.call(api.group("material"), mats -> {
            if (!isAdded()) return;
            List<BarEntry> e = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<Models.GroupRow> all = new ArrayList<>(lines);
            all.addAll(mats);
            for (int i = 0; i < all.size(); i++) {
                Models.GroupRow r = all.get(i);
                e.add(new BarEntry(i, r.total == 0 ? 0 : r.damaged * 100f / r.total));
                labels.add(r.name);
            }
            bar(R.id.chartRate, e, labels, "Defect %", 0xFFA855F7);
        }, null), null);
        return root;
    }

    private LineDataSet line(List<Entry> e, String name, int color) {
        LineDataSet ds = new LineDataSet(e, name);
        ds.setColor(color);
        ds.setLineWidth(2f);
        ds.setDrawCircles(false);
        ds.setDrawValues(false);
        return ds;
    }

    private void bar(int chartId, List<BarEntry> e, List<String> labels, String name, int color) {
        BarChart chart = root.findViewById(chartId);
        BarDataSet ds = new BarDataSet(e, name);
        ds.setColor(color);
        ds.setValueTextColor(Color.WHITE);
        chart.setData(new BarData(ds));
        style(chart);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelRotationAngle(-45f);
        chart.getXAxis().setLabelCount(labels.size());
        chart.invalidate();
    }

    static void style(Chart<?> chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        if (chart instanceof BarChart) {
            BarChart c = (BarChart) chart;
            c.getAxisRight().setEnabled(false);
            c.getAxisLeft().setTextColor(Color.WHITE);
            c.getXAxis().setTextColor(Color.WHITE);
            c.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            c.getXAxis().setGranularity(1f);
        } else if (chart instanceof LineChart) {
            LineChart c = (LineChart) chart;
            c.getAxisRight().setEnabled(false);
            c.getAxisLeft().setTextColor(Color.WHITE);
            c.getXAxis().setTextColor(Color.WHITE);
            c.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        }
    }
}
