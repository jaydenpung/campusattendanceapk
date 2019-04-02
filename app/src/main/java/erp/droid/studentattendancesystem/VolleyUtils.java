package erp.droid.studentattendancesystem;

import android.app.Application;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;

public class VolleyUtils {
    public static void makeJsonObjectRequest(Context context, String url, JSONObject requestParameters, final VolleyResponseListener listener) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, requestParameters, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                listener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error.toString());
            }
        });

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
}
