package com.sumsub.kyc_demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sumsub.kyc.liveness3d.presentation.KYCLiveness3DActivity;

import java.util.List;
import java.util.Locale;


public class TestStartFragment extends Fragment {

    private View continueButton;
    private View livenessButton;
    private TextView continueText;
    private View startNewButton;
    private View languageButton;
    private TextView languageText;
    private View backButton;

    private View loadingLayout;
    private View actionsLayout;
    private TextView promoText;

    private TestNavigationFragment navigationFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationFragment = (TestNavigationFragment)getParentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_start, container, false);
        bindViews(view);

        updateLanguageButton();

        SpannableString str = new SpannableString("The fastest way to onboard your customers");
        str.setSpan(new StyleSpan(Typeface.BOLD), 4, 11, 0);
        promoText.setText(str);

        hideProgress();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        updateContinueButton();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KYCLiveness3DActivity.REQUEST_RESULT_CODE_ID) {
            Toast.makeText(requireContext(), "Liveness3D status is " + data.getSerializableExtra(KYCLiveness3DActivity.EXTRA_STATUS), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateContinueButton() {
        if (TestManager.getInstance().getApplicant() != null) {
            continueText.setAlpha(1.0f);
            continueButton.setClickable(true);

        } else {
            continueText.setAlpha(0.4f);
            continueButton.setClickable(false);
        }
    }

    private void bindViews(View v) {
        continueButton = v.findViewById(R.id.test_continue_existing);
        livenessButton = v.findViewById(R.id.test_liveness_existing);
        continueText = v.findViewById(R.id.test_continue_text);
        startNewButton = v.findViewById(R.id.test_start_new);
        languageButton = v.findViewById(R.id.test_language_pick);
        languageText = v.findViewById(R.id.test_language_text);
        backButton = v.findViewById(R.id.test_back);
        loadingLayout = v.findViewById(R.id.test_loading_layout);
        actionsLayout = v.findViewById(R.id.test_actions_layout);
        promoText = v.findViewById(R.id.test_promo_text);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationFragment.pop();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueClick();
            }
        });

        startNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newClick();
            }
        });

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languagePickerClick();
            }
        });

        livenessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLiveness3d();
            }
        });

    }

    private String visualTextForLocale(Locale locale) {
        String lang = locale.getDisplayLanguage(locale);
        return lang.substring(0,1).toUpperCase() + lang.substring(1);
    }

    private void updateLanguageButton() {
        languageText.setText(visualTextForLocale(TestManager.getInstance().getLocale()));
    }

    private void showProgress() {
        if (getContext() == null) {
            return;
        }

        loadingLayout.setVisibility(View.VISIBLE);
        actionsLayout.setVisibility(View.GONE);
    }

    private void hideProgress() {
        if (getContext() == null) {
            return;
        }

        loadingLayout.setVisibility(View.GONE);
        actionsLayout.setVisibility(View.VISIBLE);
    }

    private void newClick() {
        if (TestManager.getInstance().getToken() == null) {
            loadToken(new Runnable() {
                @Override
                public void run() {
                    loadApplicant(new Runnable() {
                        @Override
                        public void run() {
                            startKYCModule();
                        }
                    });
                }
            });
        } else {
            loadApplicant(new Runnable() {
                @Override
                public void run() {
                    startKYCModule();
                }
            });
        }
    }

    private void checkLiveness3d() {
        if (TestManager.getInstance().getToken() == null) {
            loadToken(new Runnable() {
                @Override
                public void run() {
                    loadApplicant(new Runnable() {
                        @Override
                        public void run() {
                            startLivenessModule();
                        }
                    });
                }
            });
        } else {
            loadApplicant(new Runnable() {
                @Override
                public void run() {
                    startLivenessModule();
                }
            });
        }
    }

    private void loadToken(final Runnable callback) {
        String login = TestManager.getInstance().getLogin();
        String password = TestManager.getInstance().getPasssword();

        showProgress();
        TestNetworkManager.getInstance().addToRequestQueue(new TestAuthRequest(login, password, new TestRequestListener<String>() {
            @Override
            public void onResult(String result) {
                hideProgress();
                TestManager.getInstance().setToken(result);
                callback.run();
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                showErrorAlert("Connection error");
            }
        }));
    }

    private void loadApplicant(final Runnable callback) {
        final String token = TestManager.getInstance().getToken();

        showProgress();
        TestNetworkManager.getInstance().addToRequestQueue(new TestApplicantRequest(token, new TestRequestListener<String>() {
            @Override
            public void onResult(String result) {
                hideProgress();
                TestManager.getInstance().setApplicant(result);
                callback.run();
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                // Easiest way to handle token expiration on this stage (token exists, applicant not)
                TestManager.getInstance().clearToken();
                showErrorAlert("Connection error");
            }
        }));
    }

    private void continueClick() {
        startKYCModule();
    }

    private void startKYCModule() {
        if (getActivity() == null) {
            return;
        }
        ((TestActivity)getActivity()).startKYCModule();
    }

    private void startLivenessModule() {
        final String apiUrl = "https://test-msdk2.sumsub.com";
        String token = TestManager.getInstance().getToken();
        String applicant = TestManager.getInstance().getApplicant();
        startActivityForResult(KYCLiveness3DActivity.Companion.newIntent(requireContext(), apiUrl, applicant, token, Locale.getDefault()), KYCLiveness3DActivity.REQUEST_RESULT_CODE_ID);
    }

    private void languagePickerClick() {

        final List<Locale> availableLocales = TestManager.getInstance().availableLocales();
        final CharSequence[] availableLocaleNames = new CharSequence[availableLocales.size()];
        for (int i = 0; i < availableLocales.size(); ++i) {
            Locale locale = availableLocales.get(i);
            availableLocaleNames[i] = visualTextForLocale(locale);
        }
        int selectedIndex = availableLocales.indexOf(TestManager.getInstance().getLocale());

        new AlertDialog.Builder(getContext())
                .setTitle("Select language")
                .setSingleChoiceItems(availableLocaleNames, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TestManager.getInstance().setLocale(availableLocales.get(which));
                        updateLanguageButton();
                        dialog.dismiss();

                    }
                })
                .show();
    }

    protected void showErrorAlert(String messageText) {
        if (getContext() == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage(messageText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();
    }
}
