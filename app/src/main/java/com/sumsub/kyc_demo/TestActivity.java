package com.sumsub.kyc_demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sumsub.kyc.client.ui.base.KYCChatActivity;
import com.sumsub.kyc.core.KYCManager;
import com.sumsub.kyc.core.dataManager.KYCClientData;
import com.sumsub.kyc.core.dataManager.KYCColorConfig;
import com.sumsub.kyc.core.dataManager.KYCIconConfig;
import com.sumsub.kyc.core.dataManager.KYCReviewResult;
import com.sumsub.kyc.core.dataManager.KYCStringConfig;
import com.sumsub.kyc.core.model.KYCLivenessCustomization;
import com.sumsub.kyc.core.model.Liveness3DModule;
import com.sumsub.kyc.liveness3d.Liveness3DResultReceiver;
import com.sumsub.kyc.liveness3d.data.model.KYCLiveness3D;
import com.sumsub.kyc.liveness3d.data.model.KYCLivenessReason;
import com.sumsub.kyc.liveness3d.data.model.KYCLivenessResult;
import com.sumsub.kyc.liveness3d.presentation.KYCLivenessFaceAuthActivity;

import java.util.Collections;
import java.util.Locale;

import timber.log.Timber;

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

        KYCLivenessCustomization livenessConfig = new KYCLivenessCustomization();
        livenessConfig.getFrame().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        livenessConfig.getFrame().setRatio(0.98f);

        KYCClientData clientData = new KYCClientData(
                BuildConfig.BASE_URL,
                getPackageName(),
                "2.0",
                TestManager.getInstance().getLocale(),
                TestManager.getInstance().getApplicant(),
                "support@sumsub.com",
                config,
                new KYCStringConfig(),
                new KYCIconConfig(),
                livenessConfig);


        KYCManager.init(this, clientData, TestManager.getInstance().getKYCTokenUpdater(), Collections.singletonList(new Liveness3DModule()));
        Intent intent = new Intent(this, KYCChatActivity.class);
        startActivityForResult(intent, KYC_REQUEST_CODE);
    }

    public void startKYCLivenessModule() {
        final String apiUrl = "https://test-msdk.sumsub.com";
        String token = TestManager.getInstance().getToken();
        String applicant = TestManager.getInstance().getApplicant();

        Liveness3DResultReceiver result = new Liveness3DResultReceiver(new Handler());
        result.setReceiver(bundle -> Timber.d("Face Auth result: %s", bundle.toString()));

        KYCLivenessCustomization customization = new KYCLivenessCustomization();
        customization.getFrame().setBackgroundColor(ContextCompat.getColor(this, R.color.blueDark));
        customization.getFrame().setRatio(0.98f);
        startActivityForResult(KYCLivenessFaceAuthActivity.Companion.newIntent(this, apiUrl, applicant, token, Locale.getDefault(), customization, result), KYCLiveness3D.REQUEST_CODE_ID_FACE_AUTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if (requestCode == KYCLiveness3D.REQUEST_CODE_ID_FACE_DETECTION) {
            KYCLivenessResult.FaceDetection result = data.getParcelableExtra(KYCLiveness3D.EXTRA_RESULT);
            KYCLivenessReason reason = result.getReason();

            String message;
            if (reason instanceof KYCLivenessReason.InitializationError) {
                message = "KYC Liveness Face Detection initialization is failed. " + ((KYCLivenessReason.InitializationError) reason).getException().getMessage();
            } else {
                message = "KYC Liveness Face Detection result is " + reason;
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else if (requestCode == KYCLiveness3D.REQUEST_CODE_ID_FACE_AUTH) {
            KYCLivenessResult.FaceAuth result = data.getParcelableExtra(KYCLiveness3D.EXTRA_RESULT);
            KYCLivenessReason reason = result.getReason();

            String message;
            if (reason instanceof KYCLivenessReason.InitializationError) {
                message = "KYC Liveness Face Auth initialization is failed. " + ((KYCLivenessReason.InitializationError) reason).getException().getMessage();
            } else {
                message = "KYC Liveness Face Auth result is " + reason;
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else if (requestCode == KYC_REQUEST_CODE) {
            KYCReviewResult kycReviewResult = (KYCReviewResult) data.getSerializableExtra(KYCChatActivity.KYC_VERIFICATION_KEY);
            if (kycReviewResult != null) {
                Timber.e("KYC Review result: %s", kycReviewResult.toString());
            }
        }
    }
}
