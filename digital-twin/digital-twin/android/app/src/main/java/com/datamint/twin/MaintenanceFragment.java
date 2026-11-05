package com.datamint.twin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/** Maintenance management: list, filter, create request, change status. */
public class MaintenanceFragment extends Fragment {
    private static final String[] FILTERS = {"All", "PENDING", "IN_PROGRESS", "COMPLETED"};
    private static final String[] STATUSES = {"PENDING", "IN_PROGRESS", "COMPLETED"};
    private SimpleAdapter<Models.Maint> adapter;
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
        adapter = new SimpleAdapter<>(R.layout.item_maint, (v, m) -> {
            ((TextView) v.findViewById(R.id.tvTitle)).setText(m.title);
            TextView st = v.findViewById(R.id.tvStatus);
            st.setText(Ui.maintLabel(m.status));
            st.setBackground(Ui.pill(Ui.maintColor(m.status), 40f));
            ((TextView) v.findViewById(R.id.tvSub)).setText(m.machineName + (m.description != null ? " – " + m.description : ""));
            ((TextView) v.findViewById(R.id.tvMeta)).setText(m.priority + " · " + Ui.dateTime(m.createdAt) + " · " + m.requestedBy
                    + (m.dueDate != null ? " · due " + m.dueDate : ""));
            v.setOnClickListener(x -> new AlertDialog.Builder(requireContext()).setTitle("Set status")
                    .setItems(new String[]{"Pending", "In Progress", "Completed"}, (d, which) ->
                            Net.call(ApiClient.get().setMaintenanceStatus(m.id, STATUSES[which]), r -> load(),
                                    msg -> Ui.toast(requireContext(), msg))).show());
        });
        RecyclerView list = root.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        View fab = root.findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(x -> showCreateDialog());
        return root;
    }

    private void load() {
        Net.call(ApiClient.get().maintenance(status, 0), list -> { if (isAdded()) adapter.set(list); },
                m -> { if (isAdded()) Ui.toast(requireContext(), m); });
    }

    private void showCreateDialog() {
        Net.call(ApiClient.get().machines(), (List<Models.Machine> machines) -> {
            if (!isAdded()) return;
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_maint, null);
            Spinner spM = v.findViewById(R.id.spMachine), spP = v.findViewById(R.id.spPriority);
            List<String> names = new ArrayList<>();
            for (Models.Machine m : machines) names.add(m.name);
            spM.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names));
            spP.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"LOW", "MEDIUM", "HIGH"}));
            spP.setSelection(1);
            new AlertDialog.Builder(requireContext()).setTitle("New maintenance request").setView(v)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Save", (d, w) -> {
                        Models.MaintForm f = new Models.MaintForm();
                        f.machineId = machines.get(spM.getSelectedItemPosition()).id;
                        f.title = ((EditText) v.findViewById(R.id.etTitle)).getText().toString().trim();
                        f.description = ((EditText) v.findViewById(R.id.etDesc)).getText().toString().trim();
                        f.priority = (String) spP.getSelectedItem();
                        if (f.title.isEmpty()) { Ui.toast(requireContext(), "Title is required"); return; }
                        Net.call(ApiClient.get().createMaintenance(f), r -> load(), m -> Ui.toast(requireContext(), m));
                    }).show();
        }, m -> Ui.toast(requireContext(), m));
    }
}
