package com.sumsub.kyc_demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.sumsub.kyc.core.dataManager.KYCTokenUpdater;
import com.sumsub.kyc.core.utils.ParamCallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class TestManager {

//    public static final String KYC_API_URL = "https://api.sumsub.com/";
    public static final String KYC_API_URL = "https://test-api.sumsub.com/";

    private static final String KEY_LOGIN = "test_login";
    private static final String KEY_PASSWORD = "test_password";
    private static final String KEY_APPLICANT = "test_applicant";
    private static final String KEY_TOKEN = "test_token";
    private static final String KEY_LOCALE = "test_locale";


    private static volatile TestManager instance;

    public static TestManager getInstance() {
        if (instance == null) {
            synchronized (TestManager.class) {
                if (instance == null) {
                    instance = new TestManager();
                }
            }
        }

        return instance;
    }

    private SharedPreferences sp;
    private LinkedHashMap<String, Locale> availableLocales;

    public void init(Context context) {
        sp = context.getSharedPreferences("kyc_test_sp", Context.MODE_PRIVATE);

        availableLocales = new LinkedHashMap<>();
        availableLocales.put("en", new Locale("en", "UK"));
        availableLocales.put("ru", new Locale("ru", "RU"));

        if (getLocale() == null) {
            setLocale(availableLocales.get("en"));
        }
    }

    public List<Locale> availableLocales() {
        return new ArrayList<>(availableLocales.values());
    }

    public void setLocale(Locale locale) {
        sp.edit().putString(KEY_LOCALE, locale.getLanguage()).apply();
    }

    public Locale getLocale() {
        return availableLocales.get(sp.getString(KEY_LOCALE, null));
    }

    public void setLoginAndPass(String login, String password) {
        sp
                .edit()
                .putString(KEY_PASSWORD, password)
                .putString(KEY_LOGIN, login)
                .apply();
    }

    public String getPasssword() {
        return sp.getString(KEY_PASSWORD, null);
    }

    public String getLogin() {
        return sp.getString(KEY_LOGIN, null);
    }

    public void setApplicant(String applicant) {
        sp
                .edit()
                .putString(KEY_APPLICANT, applicant)
                .apply();
    }

    public void setToken(String token) {
        sp
                .edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public void clearToken() {
        sp.edit().remove(KEY_TOKEN).apply();
    }

    public String getApplicant() {
        return sp.getString(KEY_APPLICANT, null);
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public KYCTokenUpdater getKYCTokenUpdater() {
        return new KYCTokenUpdater() {
            @Override
            public void getInitialToken(ParamCallback<String> callback) {
                callback.onResult(getToken());
            }

            @Override
            public void updateExpiredToken(final String expiredToken, final ParamCallback<String> callback) {
                TestNetworkManager.getInstance().addToRequestQueue(new TestAuthRequest(getLogin(), getPasssword(), new TestRequestListener<String>() {
                    @Override
                    public void onResult(String result) {
                        setToken(result);
                        callback.onResult(result);
                    }

                    @Override
                    public void onError(Exception e) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateExpiredToken(expiredToken, callback);
                            }
                        }, 4000);
                    }
                }));
            }
        };
    }
}
