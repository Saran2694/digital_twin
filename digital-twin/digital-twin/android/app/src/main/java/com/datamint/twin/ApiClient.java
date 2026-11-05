package com.datamint.twin;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static Api api;
    private static String builtFor;

    private ApiClient() {}

    public static synchronized Api get() {
        String base = Session.baseUrl();
        if (api == null || !base.equals(builtFor)) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder b = original.newBuilder();
                        // Do not attach previous/expired bearer token to login requests
                        boolean isLogin = original.url().encodedPath().contains("auth/login");
                        String token = Session.token();
                        if (token != null && !isLogin) {
                            b.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(b.build());
                    }).build();
            api = new Retrofit.Builder().baseUrl(base).client(client)
                    .addConverterFactory(GsonConverterFactory.create()).build().create(Api.class);
            builtFor = base;
        }
        return api;
    }
}
