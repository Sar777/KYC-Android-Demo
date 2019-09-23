package com.sumsub.kyc_demo;

import android.util.Base64;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TestAuthRequest extends JsonObjectRequest {

    private String login;
    private String password;

    private TestRequestListener<String> listener;

    public TestAuthRequest(String login, String password, TestRequestListener<String> listener) {
        super(Method.POST, TestManager.KYC_API_URL + "resources/auth/login", null, null, null);

        this.login = login;
        this.password = password;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type", "application/json");

        try {
            String authString = login + ":" + password;
            String headerValue = Base64.encodeToString(authString.getBytes("UTF-8"), 0);

            headers.put("Authorization", "Basic " + headerValue);
        } catch (Throwable ignored) {
        }

        return headers;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        String status = response.optString("status");
        String token = response.optString("payload");

        if (status.equals("ok")) {
            listener.onResult(token);
        } else {
            listener.onError(new Exception("Auth status is not ok"));
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        listener.onError(error);
    }
}
