package com.datamint.twin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

/** Dashboard: KPI cards, digital factory view, recent alerts and maintenance requests (auto-refresh every 10 s). */
public class DashboardFragment extends Fragment {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View root;
    private final Runnable tick = new Runnable() {
        @Override public void run() { load(); handler.postDelayed(this, 10000); }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ((FactoryMapView) root.findViewById(R.id.map)).setListener(m -> {
            Intent i = new Intent(requireContext(), MachineDetailActivity.class);
            i.putExtra("id", m.id);
            startActivity(i);
        });
        return root;
    }

    @Override public void onResume() { super.onResume(); handler.post(tick); }
    @Override public void onPause() { super.onPause(); handler.removeCallbacks(tick); }

    private void load() {
        Api api = ApiClient.get();
        Net.call(api.summary(), s -> {
            if (!isAdded()) return;
            ((TextView) root.findViewById(R.id.tvTotal)).setText(String.valueOf(s.total));
            ((TextView) root.findViewById(R.id.tvRunning)).setText(String.valueOf(s.running));
            ((TextView) root.findViewById(R.id.tvWarning)).setText(String.valueOf(s.warning));
            ((TextView) root.findViewById(R.id.tvOffline)).setText(String.valueOf(s.offline));
            ((TextView) root.findViewById(R.id.tvAlerts)).setText(String.valueOf(s.activeAlerts));
        }, m -> { if (isAdded()) Ui.toast(requireContext(), m); });

        Net.call(api.machines(), list -> { if (isAdded()) ((FactoryMapView) root.findViewById(R.id.map)).setMachines(list); }, null);

        Net.call(api.recentAlerts(3), list -> {
            if (!isAdded()) return;
            LinearLayout box = root.findViewById(R.id.recentAlerts);
            box.removeAllViews();
            for (Models.Alert a : list) {
                View row = LayoutInflater.from(getContext()).inflate(R.layout.item_row, box, false);
                row.findViewById(R.id.vColor).setBackgroundColor(Ui.severityColor(a.severity, a.status));
                ((TextView) row.findViewById(R.id.tvTitle)).setText(a.title);
                ((TextView) row.findViewById(R.id.tvSub)).setText(a.message);
                ((TextView) row.findViewById(R.id.tvRight)).setText(Ui.time(a.createdAt));
                box.addView(row);
            }
        }, null);

        Net.call(api.maintenance(null, 3), (List<Models.Maint> list) -> {
            if (!isAdded()) return;
            LinearLayout box = root.findViewById(R.id.recentMaint);
            box.removeAllViews();
            for (Models.Maint m : list) {
                View row = LayoutInflater.from(getContext()).inflate(R.layout.item_row, box, false);
                row.findViewById(R.id.vColor).setBackgroundColor(Ui.maintColor(m.status));
                ((TextView) row.findViewById(R.id.tvTitle)).setText(m.title);
                ((TextView) row.findViewById(R.id.tvSub)).setText(m.machineName);
                ((TextView) row.findViewById(R.id.tvRight)).setText(Ui.maintLabel(m.status));
                box.addView(row);
            }
        }, null);
    }
}
