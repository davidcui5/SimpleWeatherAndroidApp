package com.example.david2.weatherapp;

// David Cui B00788648 Nov 2018

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

//RequestQueueSingleton provides singleton access to the Volley RequestQueue
// Singleton is used because only 1 instance of RequestQueue is needed
// Volley RequestQueue takes care of the HTTP request to the API
// RequestQueue manages worker threads for network connection
public class RequestQueueSingleton {

    private static RequestQueueSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    //constructor is private so only 1 RequestQueueSingleton and 1 RequestQueue is created
    private RequestQueueSingleton(Context context) {
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    //this is used to access the singleton by other classes
    public static synchronized RequestQueueSingleton getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new RequestQueueSingleton(context.getApplicationContext());
        }
        return mInstance;
    }

    //make sure we only create 1 RequestQueue
    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    //allows Volley request objects to be added to queue, the queue takes care of everything
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
