package com.datamint.twin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/** Alert management: filter, acknowledge, resolve. */
public class AlertsFragment extends Fragment {
    private static final String[] FILTERS = {"All", "OPEN", "ACKNOWLEDGED", "RESOLVED"};
    private SimpleAdapter<Models.Alert> adapter;
    private String status = null;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        Spinner sp = root.findViewById(R.id.spFilter);
        sp.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, FILTERS));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                status = pos == 0 ? null : FILTERS[pos];
                load();
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        adapter = new SimpleAdapter<>(R.layout.item_alert, (v, a) -> {
            TextView title = v.findViewById(R.id.tvTitle);
            title.setText(a.title);
            title.setTextColor(Ui.severityColor(a.severity, a.status));
            ((TextView) v.findViewById(R.id.tvMsg)).setText(a.message);
            ((TextView) v.findViewById(R.id.tvMeta)).setText(a.severity + " · " + a.status + " · " + Ui.dateTime(a.createdAt)
                    + (a.acknowledgedBy != null ? " · by " + a.acknowledgedBy : ""));
            Button ack = v.findViewById(R.id.btnAck), res = v.findViewById(R.id.btnResolve);
            ack.setVisibility("OPEN".equals(a.status) ? View.VISIBLE : View.GONE);
            res.setVisibility("RESOLVED".equals(a.status) ? View.GONE : View.VISIBLE);
            ack.setOnClickListener(x -> Net.call(ApiClient.get().acknowledge(a.id), r -> load(), m -> Ui.toast(requireContext(), m)));
            res.setOnClickListener(x -> Net.call(ApiClient.get().resolve(a.id), r -> load(), m -> Ui.toast(requireContext(), m)));
        });
        RecyclerView list = root.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        return root;
    }

    private void load() {
        Net.call(ApiClient.get().alerts(status, 0, 100), page -> { if (isAdded()) adapter.set(page.items); },
                m -> { if (isAdded()) Ui.toast(requireContext(), m); });
    }
}
