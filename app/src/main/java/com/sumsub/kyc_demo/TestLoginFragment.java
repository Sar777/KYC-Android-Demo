package com.sumsub.kyc_demo;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class TestLoginFragment extends Fragment {

    private EditText loginField;
    private EditText passwordField;
    private View loginButton;
    private View loadingLayout;
    private View actionsLayout;

    private TestNavigationFragment navigationFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_login, container, false);
        bindViews(view);

        //loginField.setText(TestManager.getInstance().getLogin());
        //passwordField.setText(TestManager.getInstance().getPasssword());

        hideProgress();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationFragment = (TestNavigationFragment)getParentFragment();

        if (TestManager.getInstance().getToken() != null) {
            pushNextFragment();
        }
    }

    private void bindViews(View v) {
        loginField = v.findViewById(R.id.test_login);
        passwordField = v.findViewById(R.id.test_password);
        loginButton = v.findViewById(R.id.test_login_button);
        loadingLayout = v.findViewById(R.id.test_loading_layout);
        actionsLayout = v.findViewById(R.id.test_actions_layout);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
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

    private void handleLogin() {
        final String login = loginField.getText().toString();
        final String password = passwordField.getText().toString();

        if (login.length() == 0 || password.length() == 0) {
            return;
        }

        loadToken(login, password);
    }

    private void pushNextFragment() {
        navigationFragment.pushFragment(new TestStartFragment());
    }

    private void loadToken(final String login, final String password) {
        showProgress();
        TestNetworkManager.getInstance().addToRequestQueue(new TestAuthRequest(login, password, new TestRequestListener<String>() {
            @Override
            public void onResult(String result) {
                hideProgress();

                TestManager.getInstance().setLoginAndPass(login, password);
                TestManager.getInstance().setToken(result);

                if (getContext() != null) {
                    pushNextFragment();
                }
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                showErrorAlert("Wrong username or password");
            }
        }));
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
