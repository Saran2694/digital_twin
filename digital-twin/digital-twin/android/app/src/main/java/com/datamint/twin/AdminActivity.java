package com.datamint.twin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Map;

/** Admin dashboard (mobile): system counters and user management. Excel import / machine editing is on the web app. */
public class AdminActivity extends AppCompatActivity {
    private SimpleAdapter<Models.User> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        if (getSupportActionBar() != null) { getSupportActionBar().setTitle("Admin Dashboard"); getSupportActionBar().setDisplayHomeAsUpEnabled(true); }
        adapter = new SimpleAdapter<>(R.layout.item_user, (v, u) -> {
            ((TextView) v.findViewById(R.id.tvName)).setText(u.username + (u.fullName != null ? " – " + u.fullName : ""));
            ((TextView) v.findViewById(R.id.tvSub)).setText(u.role + (u.enabled ? "" : " · disabled"));
            v.findViewById(R.id.btnDelete).setOnClickListener(x -> new AlertDialog.Builder(this)
                    .setMessage("Delete " + u.username + "?").setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (d, w) -> Net.call(ApiClient.get().deleteUser(u.id), r -> load(), m -> Ui.toast(this, m))).show());
        });
        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        findViewById(R.id.fab).setOnClickListener(v -> showCreate());
        load();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void load() {
        Net.call(ApiClient.get().stats(), (Map<String, Long> s) -> ((TextView) findViewById(R.id.tvStats)).setText(
                "Inspection records: " + s.get("components") + "\nMachines: " + s.get("machines") + "\nAlerts: " + s.get("alerts")
                        + "\nMaintenance requests: " + s.get("maintenance") + "\nUsers: " + s.get("users")), m -> Ui.toast(this, m));
        Net.call(ApiClient.get().users(), list -> adapter.set(list), m -> Ui.toast(this, m));
    }

    private void showCreate() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_user, null);
        Spinner role = v.findViewById(R.id.spRole);
        role.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"OPERATOR", "ENGINEER", "ADMIN"}));
        new AlertDialog.Builder(this).setTitle("New user").setView(v).setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (d, w) -> {
                    Models.UserForm f = new Models.UserForm();
                    f.username = ((EditText) v.findViewById(R.id.etUsername)).getText().toString().trim();
                    f.password = ((EditText) v.findViewById(R.id.etPassword)).getText().toString();
                    f.fullName = ((EditText) v.findViewById(R.id.etFullName)).getText().toString().trim();
                    f.role = (String) role.getSelectedItem();
                    Net.call(ApiClient.get().createUser(f), r -> load(), m -> Ui.toast(this, m));
                }).show();
    }
}
