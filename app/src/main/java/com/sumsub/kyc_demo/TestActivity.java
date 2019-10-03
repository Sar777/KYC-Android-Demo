package com.sumsub.kyc_demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sumsub.kyc.client.KYCManager;
import com.sumsub.kyc.client.ui.base.KYCChatActivity;
import com.sumsub.kyc.core.dataManager.KYCClientData;
import com.sumsub.kyc.core.dataManager.KYCColorConfig;
import com.sumsub.kyc.core.dataManager.KYCIconConfig;
import com.sumsub.kyc.core.dataManager.KYCReviewResult;
import com.sumsub.kyc.core.dataManager.KYCStringConfig;
import com.sumsub.kyc.liveness3d.Liveness3DModule;

import java.util.Collections;

public class TestActivity extends AppCompatActivity {

    private static final int KYC_REQUEST_CODE = 1;

    private TestNavigationFragment navigationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestManager.getInstance().init(getApplicationContext());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_sum_sub_auth);

        navigationFragment = (TestNavigationFragment)getSupportFragmentManager().findFragmentById(R.id.test_navigation_container);
        if (navigationFragment == null) {
            navigationFragment = TestNavigationFragment.newInstance();
            navigationFragment.setInitialFragment(new TestLoginFragment());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.test_navigation_container, navigationFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (!navigationFragment.goBack()) {
            finish();
        }
    }

    public void startKYCModule() {
        KYCColorConfig config = new KYCColorConfig();
        //config.setChatButtonBackgroundColor(Color.parseColor("#aaaaaa"));
        //config.setChatButtonTextColor(Color.BLACK);

        KYCClientData clientData = new KYCClientData(
                BuildConfig.BASE_URL,
                getPackageName(),
                "2.0",
                TestManager.getInstance().getLocale(),
                TestManager.getInstance().getApplicant(),
                "support@sumsub.com",
                config,
                new KYCStringConfig(),
                new KYCIconConfig());


        KYCManager.init(this, clientData, TestManager.getInstance().getKYCTokenUpdater(), Collections.singletonList(new Liveness3DModule()));
        Intent intent = new Intent(this, KYCChatActivity.class);
        startActivityForResult(intent, KYC_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == KYC_REQUEST_CODE) {
            KYCReviewResult kycReviewResult = (KYCReviewResult) data.getSerializableExtra(KYCChatActivity.KYC_VERIFICATION_KEY);
            if (kycReviewResult != null) {
                Log.e("KYC", "KYC Review result: " + kycReviewResult);
            }
        }
    }
}
