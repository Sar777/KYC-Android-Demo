package com.sumsub.kyc_demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class TestNavigationFragment extends Fragment {

    private Fragment initialFragment;

    public static TestNavigationFragment newInstance() {
        TestNavigationFragment fragment = new TestNavigationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getChildFragmentManager().getBackStackEntryCount() == 0 && initialFragment != null) {
            pushFragment(initialFragment);
        }
    }

    public void pushFragment(Fragment fragment) {
        hideKeyboard();

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            ft.setCustomAnimations(
                    R.anim.right_to_left,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.left_to_right
            );
        }
        ft.replace(R.id.fragment_container, fragment, null).addToBackStack(null).commit();
    }

    public void pop() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            hideKeyboard();
            getChildFragmentManager().popBackStack();
        }
    }

    public boolean goBack() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            pop();
            return true;
        }
        return false;
    }

    public void setInitialFragment(Fragment initialFragment) {
        this.initialFragment = initialFragment;
    }

    public void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    // Private

    public void hideKeyboard() {
        Activity activity = getActivity();

        if (activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
