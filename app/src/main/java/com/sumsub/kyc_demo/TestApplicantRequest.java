package com.sumsub.kyc_demo;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TestApplicantRequest extends JsonObjectRequest {

    private String token;
    private TestRequestListener<String> listener;

    public TestApplicantRequest(String token, TestRequestListener<String> listener) {
        super(Method.POST, TestManager.KYC_API_URL + "resources/applicants", mapJSONBody(), null, null);

        this.token = token;
        this.listener = listener;
    }

    public static JSONObject mapJSONBody() {

        String body = "{\n" +
                "  \"info\": {\n" +
                "    \n" +
                "  },\n" +
                "  \"requiredIdDocs\": {\n" +
                "    \"docSets\": [\n" +
                "      {\n" +
                "        \"idDocSetType\": \"IDENTITY\",\n" +
                "        \"types\": [\n" +
                "          \"ID_CARD\",\n" +
                "          \"PASSPORT\",\n" +
                "          \"DRIVERS\"\n" +
                "        ],\n" +
                "        \"subTypes\": [\n" +
                "          \"FRONT_SIDE\",\n" +
                "          \"BACK_SIDE\"\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"idDocSetType\": \"SELFIE\",\n" +
                "        \"types\": [\n" +
                "          \"SELFIE\"\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"idDocSetType\": \"PROOF_OF_RESIDENCE\",\n" +
                "        \"types\": [\n" +
                "          \"UTILITY_BILL\"\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"includedCountries\": [\n" +
                "      \"RUS\",\n" +
                "      \"VIR\",\n" +
                "      \"GBR\"\n" +
                "    ],\n" +
                "    \"excludedCountries\": [\n" +
                "      \n" +
                "    ]\n" +
                "  }\n" +
                "}";

        try {
            return new JSONObject(body);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    @Override
    public void deliverError(VolleyError error) {
        listener.onError(error);
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        String id = response.optString("id");
        listener.onResult(id);
    }
}
