package com.datamint.twin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/** Digital factory view + live equipment monitoring list. */
public class FactoryFragment extends Fragment {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View root;
    private SimpleAdapter<Models.Machine> adapter;
    private final Runnable tick = new Runnable() {
        @Override public void run() { load(); handler.postDelayed(this, 10000); }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        root = inflater.inflate(R.layout.fragment_factory, container, false);
        FactoryMapView map = root.findViewById(R.id.map);
        map.setListener(this::open);
        adapter = new SimpleAdapter<>(R.layout.item_machine, (v, m) -> {
            ((TextView) v.findViewById(R.id.tvName)).setText(m.name);
            TextView st = v.findViewById(R.id.tvStatus);
            st.setText(Ui.statusLabel(m.status));
            st.setBackground(Ui.pill(Ui.statusColor(m.status), 40f));
            ((TextView) v.findViewById(R.id.tvLine)).setText(m.line + " · " + m.material + " · updated " + Ui.dateTime(m.lastUpdated));
            ((TextView) v.findViewById(R.id.tvValues)).setText("Temp " + Ui.f(m.temperature, 1) + " °C   Pressure " + Ui.f(m.pressure, 1)
                    + " bar\nRun " + Ui.f(m.runningHours, 1) + " h   Defects " + Ui.f(m.defectRate, 1) + " %   Alerts " + m.openAlerts);
            ((TextView) v.findViewById(R.id.tvReason)).setText(m.statusReason);
            v.setOnClickListener(x -> open(m));
        });
        RecyclerView list = root.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        return root;
    }

    private void open(Models.Machine m) {
        Intent i = new Intent(requireContext(), MachineDetailActivity.class);
        i.putExtra("id", m.id);
        startActivity(i);
    }

    @Override public void onResume() { super.onResume(); handler.post(tick); }
    @Override public void onPause() { super.onPause(); handler.removeCallbacks(tick); }

    private void load() {
        Net.call(ApiClient.get().machines(), list -> {
            if (!isAdded()) return;
            ((FactoryMapView) root.findViewById(R.id.map)).setMachines(list);
            adapter.set(list);
        }, m -> { if (isAdded()) Ui.toast(requireContext(), m); });
    }
}
