package erp.droid.studentattendancesystem;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateDeviceToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            super.onMessageReceived(remoteMessage);

            final String title = remoteMessage.getNotification().getTitle();
            final String body = remoteMessage.getNotification().getBody();

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    private void updateDeviceToken(final String deviceToken) {
        try {
            final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    "Config", getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("deviceToken", deviceToken);

            VolleyUtils.makeJsonObjectRequest(getApplicationContext(), new ApiRoute().UPDATE_DEVICE_TOKEN, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            //Save to sharedpref if success
                            SharedPreferences.Editor editor = sharedPref.edit();
                            authObject.put("deviceToken", deviceToken);
                            editor.putString("AuthenticationObject", authObject.toString());
                            editor.commit();
                        }
                    } catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            });
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

}
