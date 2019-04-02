package erp.droid.studentattendancesystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AttendanceFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_attendance, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Attendance");

        getAttendance();
    }

    public void getAttendance() {
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                    "Config", getActivity().getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);

            VolleyUtils.makeJsonObjectRequest(getActivity().getApplicationContext(), new ApiRoute().GET_SUBJECT_ATTENDANCE, requestParameters, new VolleyResponseListener() {
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
                                if (authObject.getString("userType").equals("student")) {
                                    temp.put("2", "Subject: " + jsonObject.getString("subject_name"));
                                    temp.put("3", "Attendance(%): " + jsonObject.getString("attendance_percentage"));
                                }
                                else {
                                    temp.put("2", "Subject: " + jsonObject.getString("subject_name"));
                                    temp.put("3", "Student: " + jsonObject.getString("student_name"));
                                    temp.put("4", "Attendance(%): " + jsonObject.getString("attendance_percentage"));
                                }

                                resultList.add(temp);
                            }

                            ListView lview = (ListView) getView().findViewById(R.id.attendanceList);
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
