package org.nypl.simplified.offlinequeue;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by aferditamuriqi on 3/22/17.
 */

public class NYPLVolley {

    private static RequestQueue request_queue;

    public static RequestQueue getRequestQueue() {
        if (request_queue != null) {
            return request_queue;
        }
        else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }
    public static void add(Request request)
    {
        getRequestQueue().add(request);
    }

    public static void init(Context context){
        request_queue = Volley.newRequestQueue(context);
    }

}
