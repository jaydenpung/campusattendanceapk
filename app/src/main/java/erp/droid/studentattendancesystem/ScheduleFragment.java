package erp.droid.studentattendancesystem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ScheduleFragment extends Fragment{
    String selectedDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Schedule");


        //Set click listener on button
        final Button btnChangeDate = (Button) getView().findViewById(R.id.btnChangeDate);
        btnChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create the datePickerFragment
                AppCompatDialogFragment newFragment = new DatepickerFragment();
                // set the targetFragment to receive the results, specifying the request code
                newFragment.setTargetFragment(ScheduleFragment.this, 666);
                // show the datePicker
                newFragment.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(), "datePicker");
            }
        });

        //Default to all
        ((TextView)getView().findViewById(R.id.tvDateSelected)).setText("Showing all Lessons");
        getSchedule("");
    }

    private void getSchedule(String date) {
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                    "Config", getActivity().getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);

            if (!date.equals("")) {
                requestParameters.put("dateTime", tsToSec8601(date));
            }

            VolleyUtils.makeJsonObjectRequest(getActivity().getApplicationContext(), new ApiRoute().GET_TIMETABLE, requestParameters, new VolleyResponseListener() {
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
                                JSONObject lessonObject = results.getJSONObject(i);
                                HashMap temp = new HashMap();
                                temp.put("1", lessonObject.getString("lesson_id"));

                                temp.put("2", "DateTime: " + lessonObject.getString("date_time"));
                                temp.put("3", "Subject: " + lessonObject.getString("subject_name"));

                                if (authObject.getString("userType").equals("student")) {
                                    temp.put("4", "Lecturer: " + lessonObject.getString("staff_name"));
                                    if (lessonObject.getString("attended").equals("1")) {
                                        temp.put("5", "✔");
                                    }
                                    else {
                                        temp.put("5", "✖");
                                    }
                                }
                                resultList.add(temp);
                            }

                            ListView lview = (ListView) getView().findViewById(R.id.scheduleList);
                            CustomListViewAdapter adapter = new CustomListViewAdapter(getActivity(), resultList);
                            lview.setAdapter(adapter);

                            //On click event to view lesson
                            lview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                                @Override
                                public void onItemClick(AdapterView<?>adapter,View v, int position, long id){
                                    String lessonId = ((HashMap)adapter.getItemAtPosition(position)).get("1").toString();

                                    //Open lesson detail activity
                                    Intent intent = new Intent(getActivity(), LessonActivity.class);
                                    intent.putExtra("lessonId", lessonId);
                                    getActivity().startActivity(intent);
                                }
                            });

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

    //Convert datetime to epoch
    public static Integer tsToSec8601
    (String timestamp) {
        if (timestamp == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dt = sdf.parse(timestamp);
            long epoch = dt.getTime();
            return (int) (epoch / 1000);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check for the results
        if (requestCode == 666 && resultCode == Activity.RESULT_OK) {
            // get date from string
            selectedDate = data.getStringExtra("selectedDate");
            // set the value of the editText
            ((TextView)getView().findViewById(R.id.tvDateSelected)).setText("Showing lessons on: " + selectedDate);
            getSchedule(selectedDate);
        }
    }
}
