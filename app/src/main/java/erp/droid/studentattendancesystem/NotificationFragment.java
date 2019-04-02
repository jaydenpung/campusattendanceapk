package erp.droid.studentattendancesystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Notification");

        //Set click listener on button
        final Button btnNewMessage = (Button) getView().findViewById(R.id.btnNewMessage);
        btnNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getNotification();
    }

    public void getNotification() {
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                    "Config", getActivity().getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Only allow create new message if user is staff
            if (authObject.getString("userType").equals("staff")) {
                ((Button)getView().findViewById(R.id.btnNewMessage)).setVisibility(View.VISIBLE);
            }
            else {
                ((Button)getView().findViewById(R.id.btnNewMessage)).setVisibility(View.GONE);
            }

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);

            VolleyUtils.makeJsonObjectRequest(getActivity().getApplicationContext(), new ApiRoute().GET_NOTIFICATION_LIST, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    ((MainActivity)getActivity()).Logout();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {

                            JSONArray results = response.getJSONArray("data");
                            ArrayList<HashMap> resultList = new ArrayList<HashMap>();

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject jsonObject = results.getJSONObject(i);
                                HashMap temp = new HashMap();

                                temp.put("1", "");
                                temp.put("2", "Date Time: " + jsonObject.getString("date_time"));
                                temp.put("3", "Sender: " + jsonObject.getString("sender_name"));
                                temp.put("4", "Message: " + jsonObject.getString("message"));

                                resultList.add(temp);
                            }

                            ListView lview = (ListView) getView().findViewById(R.id.notificationList);
                            CustomListViewAdapter adapter = new CustomListViewAdapter(getActivity(), resultList);
                            lview.setAdapter(adapter);
                        } else {
                            ((MainActivity)getActivity()).Logout();
                        }
                    }
                    catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            });
        }
        catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }
}
