package com.datamint.twin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUser;
    private EditText etPass;
    private EditText etServer;
    private TextView tvError;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Only skip login if there is an active, unexpired session token
        if (Session.isLoggedIn()) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        etServer = findViewById(R.id.etServer);
        tvError = findViewById(R.id.tvError);
        btnLogin = findViewById(R.id.btnLogin);

        etServer.setText(Session.baseUrl());

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        tvError.setText("");

        String username = etUser.getText() != null ? etUser.getText().toString().trim() : "";
        String password = etPass.getText() != null ? etPass.getText().toString() : "";
        String server = etServer.getText() != null ? etServer.getText().toString().trim() : "";

        // 1. Validation for empty fields
        if (TextUtils.isEmpty(username)) {
            etUser.setError("Username is required");
            etUser.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPass.setError("Password is required");
            etPass.requestFocus();
            return;
        }

        // 2. Validate and persist server URL
        try {
            Session.setBaseUrl(server);
        } catch (Exception e) {
            tvError.setText("Invalid server URL format");
            return;
        }

        // 3. Loading state prevents repeated login submissions
        setLoading(true);

        try {
            Api api = ApiClient.get();
            Net.call(api.login(new Models.LoginRequest(username, password)),
                    res -> {
                        setLoading(false);
                        if (res != null && res.token != null) {
                            Session.save(res);
                            openMain();
                        } else {
                            tvError.setText("Login failed: empty response from server");
                        }
                    },
                    msg -> {
                        setLoading(false);
                        tvError.setText(msg);
                    });
        } catch (Exception e) {
            setLoading(false);
            tvError.setText("Error: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Signing in…" : "Sign In");
        etUser.setEnabled(!loading);
        etPass.setEnabled(!loading);
        etServer.setEnabled(!loading);
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
