package com.sumsub.kyc_demo;

public interface TestRequestListener<T> {
    void onResult(T result);
    void onError(Exception e);
}
