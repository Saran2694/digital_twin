package com.datamint.twin;

import android.content.Intent;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Small helper around Retrofit callbacks (runs on the main thread). */
public final class Net {
    private Net() {}

    public interface Ok<T> { void run(T body); }
    public interface Fail { void run(String message); }

    public static <T> void call(Call<T> call, Ok<T> ok, Fail fail) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) {
                if (r.isSuccessful()) {
                    if (ok != null) ok.run(r.body());
                } else {
                    boolean isLogin = c.request().url().encodedPath().contains("auth/login");
                    if (r.code() == 401 && !isLogin && Session.token() != null) {
                        Session.clear();
                        Intent i = new Intent(TwinApp.ctx(), LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        TwinApp.ctx().startActivity(i);
                    } else if (fail != null) {
                        fail.run(message(r));
                    }
                }
            }
            @Override public void onFailure(Call<T> c, Throwable t) {
                if (fail != null) {
                    String msg = t != null ? t.getMessage() : "Unknown error";
                    if (msg == null || msg.isEmpty()) {
                        msg = t != null ? t.getClass().getSimpleName() : "Network failure";
                    }
                    fail.run("Cannot reach server: " + msg);
                }
            }
        });
    }

    private static String message(Response<?> r) {
        try {
            if (r.errorBody() != null) {
                String body = r.errorBody().string();
                if (!body.isEmpty()) {
                    JSONObject o = new JSONObject(body);
                    if (o.has("message") && !o.isNull("message")) {
                        return o.getString("message");
                    }
                    if (o.has("error") && !o.isNull("error")) {
                        return o.getString("error");
                    }
                }
            }
        } catch (Exception ignored) { }
        if (r.code() == 401) {
            return "Invalid username or password";
        }
        return "Error " + r.code();
    }
}
