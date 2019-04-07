package erp.droid.studentattendancesystem;

import android.content.SharedPreferences;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {

    String[] subjectsArray;
    String[] lessonsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        generateSpinners();
    }

    public void generateSpinners() {
        try {

            //get schedule
            SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                    "Config", this.getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);

                            VolleyUtils.makeJsonObjectRequest(this.getApplicationContext(), new ApiRoute().GET_TIMETABLE, requestParameters, new VolleyResponseListener() {
                                @Override
                                public void onError(String message) {
                                    finish();
                                }

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {

                                            final ArrayList<String> subjects = new ArrayList<String>();
                                            final ArrayList<String> lessons = new ArrayList<String>();

                                            JSONArray results = response.getJSONArray("data");
                                            ArrayList<HashMap> resultList = new ArrayList<HashMap>();

                                            //Add default none
                                            lessons.add("Send to student in Lesson");
                                            subjects.add("Send to student with Subject");

                                            for (int i = 0; i < results.length(); i++) {
                                                JSONObject lessonObject = results.getJSONObject(i);
                                                lessons.add(lessonObject.getString("lesson_id") + ": " + lessonObject.getString("subject_name") + " at " + lessonObject.getString("date_time"));
                                                String subject = lessonObject.getString("subject_id") + ": " + lessonObject.getString("subject_name");
                                                if (!subjects.contains(subject)) {
                                                    subjects.add(subject);
                                                }
                                            }

                                            subjectsArray = new String[subjects.size()];
                                            lessonsArray = new String[lessons.size()];

                                            subjectsArray = subjects.toArray(subjectsArray);
                                            lessonsArray = lessons.toArray(lessonsArray);

                            //Put inside spinners
                            final Spinner subjectSpinner = (Spinner) findViewById(R.id.subjectSpinner);
                            final Spinner lessonSpinner = (Spinner) findViewById(R.id.lessonSpinner);

                            subjectSpinner.setTag(R.id.subjectSpinnerPosition, 999);
                            lessonSpinner.setTag(R.id.lessonSpinnerPosition, 999);

                            lessonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                //Performing action onItemSelected and onNothing selected
                                @Override
                                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                                    //Use tag in values/tags to prevent onItemSelectedListener being called when setting value on spinner manually
                                    if((Integer)lessonSpinner.getTag(R.id.lessonSpinnerPosition) != position) {
                                        lessonSpinner.setSelection(position);
                                        subjectSpinner.setTag(R.id.subjectSpinnerPosition, 0);
                                        subjectSpinner.setSelection(0);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub
                                }
                            });

                            subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                //Performing action onItemSelected and onNothing selected
                                @Override
                                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {

                                    //Use tag in values/tags to prevent onItemSelectedListener being called when setting value on spinner manually
                                    if((Integer)(subjectSpinner.getTag(R.id.subjectSpinnerPosition)) != position) {
                                        subjectSpinner.setSelection(position);
                                        lessonSpinner.setTag(R.id.lessonSpinnerPosition, 0);
                                        lessonSpinner.setSelection(0);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub
                                }
                            });

                            //Creating the ArrayAdapter instance
                            ArrayAdapter aa = new ArrayAdapter(MessageActivity.this,android.R.layout.simple_spinner_item, lessonsArray);
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            lessonSpinner.setAdapter(aa);

                            ArrayAdapter aa2 = new ArrayAdapter(MessageActivity.this,android.R.layout.simple_spinner_item, subjectsArray);
                            aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            subjectSpinner.setAdapter(aa2);

                            //Send message on click
                            Button btnSendMessage = (Button)findViewById(R.id.btnSendMessage);
                            btnSendMessage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        //Make api call
                                        JSONObject requestParameters = new JSONObject();
                                        requestParameters.put("authenticationObject", authObject);
                                        requestParameters.put("message", ((EditText) findViewById(R.id.etMessage)).getText().toString());

                                        if (subjectSpinner.getSelectedItemPosition()!= 0) {
                                            requestParameters.put("subject_id", subjectSpinner.getSelectedItem().toString().split(":")[0]);
                                        }
                                        if (lessonSpinner.getSelectedItemPosition()!= 0) {
                                            requestParameters.put("lesson_id", lessonSpinner.getSelectedItem().toString().split(":")[0]);
                                        }

                                        VolleyUtils.makeJsonObjectRequest(getApplicationContext(), new ApiRoute().CREATE_NOTIFICATION, requestParameters, new VolleyResponseListener() {
                                            @Override
                                            public void onError(String message) {
                                                finish();
                                            }

                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    if (response.getBoolean("success")) {
                                                        Toast.makeText(MessageActivity.this, "Successfully sent", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        finish();
                                                    }
                                                } catch (Exception ex) {
                                                    Log.e("Error", ex.getMessage());
                                                }
                                            }
                                        });
                                    }
                                    catch (Exception ex) {

                                    }
                                }
                            });

                        } else {
                            finish();
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
