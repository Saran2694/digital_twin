package com.datamint.twin;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface Api {
    @POST("auth/login") Call<Models.LoginResponse> login(@Body Models.LoginRequest r);

    @GET("dashboard/summary") Call<Models.Summary> summary();
    @GET("machines") Call<List<Models.Machine>> machines();
    @GET("machines/{id}") Call<Models.Machine> machine(@Path("id") long id);
    @GET("machines/{id}/trend") Call<List<Models.TrendPoint>> trend(@Path("id") long id, @Query("range") String range);

    @GET("alerts") Call<Models.PageResult<Models.Alert>> alerts(@Query("status") String status, @Query("page") int page, @Query("size") int size);
    @GET("alerts/recent") Call<List<Models.Alert>> recentAlerts(@Query("limit") int limit);
    @PUT("alerts/{id}/acknowledge") Call<Models.Alert> acknowledge(@Path("id") long id);
    @PUT("alerts/{id}/resolve") Call<Models.Alert> resolve(@Path("id") long id);

    @GET("maintenance") Call<List<Models.Maint>> maintenance(@Query("status") String status, @Query("limit") int limit);
    @POST("maintenance") Call<Models.Maint> createMaintenance(@Body Models.MaintForm form);
    @PUT("maintenance/{id}/status") Call<Models.Maint> setMaintenanceStatus(@Path("id") long id, @Query("status") String status);

    @GET("analytics/overview") Call<Models.Overview> overview();
    @GET("analytics/group") Call<List<Models.GroupRow>> group(@Query("by") String by);
    @GET("analytics/trend") Call<List<Models.TrendRow>> analyticsTrend(@Query("granularity") String granularity);

    @GET("admin/users") Call<List<Models.User>> users();
    @POST("admin/users") Call<Models.User> createUser(@Body Models.UserForm form);
    @DELETE("admin/users/{id}") Call<Void> deleteUser(@Path("id") long id);
    @GET("admin/stats") Call<Map<String, Long>> stats();
}
