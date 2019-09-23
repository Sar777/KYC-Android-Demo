package com.sumsub.kyc_demo;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;

public class TestNetworkManager {

    private static volatile TestNetworkManager instance;

    public static TestNetworkManager getInstance() {
        if (instance == null) {
            synchronized (TestManager.class) {
                if (instance == null) {
                    instance = new TestNetworkManager();
                }
            }
        }

        return instance;
    }

    private RequestQueue requestQueue;

    private TestNetworkManager() {
        requestQueue = createRequestQueue();
    }

    private RequestQueue createRequestQueue() {
        RequestQueue queue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack(null, new TLSSocketFactory())));
        queue.start();
        return queue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}
