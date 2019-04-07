package erp.droid.studentattendancesystem;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.view.View.GONE;
import static erp.droid.studentattendancesystem.R.id.tvStudentList;

public class AddLessonActivity extends AppCompatActivity {
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);

        LoadAddLesson();

        ((Button)findViewById(R.id.btnSaveLesson)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddLesson();
            }
        });
    }

    private void LoadAddLesson() {
        try {
            SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                    "Config", this.getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);

            VolleyUtils.makeJsonObjectRequest(this.getApplicationContext(), new ApiRoute().GET_ADD_LESSON, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    finish();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject results = response.getJSONObject("data");
                            JSONArray subject_list = results.getJSONArray("subject_list");
                            JSONArray student_list = results.getJSONArray("student_list");

                            //Massage subject_list data
                            ArrayList<String> subjects = new ArrayList<String>();

                            for (int i = 0; i < subject_list.length(); i++) {
                                JSONObject subjectObject = subject_list.getJSONObject(i);
                                String subject = subjectObject.getString("id") + ": " + subjectObject.getString("subject_name");
                                if (!subjects.contains(subject)) {
                                    subjects.add(subject);
                                }
                            }
                            String[] subjectsArray = new String[subjects.size()];
                            subjectsArray = subjects.toArray(subjectsArray);

                            //Load subject list into spinner
                            Spinner subjectSpinner = (Spinner)findViewById(R.id.spinnerSubject);
                            ArrayAdapter aa = new ArrayAdapter(AddLessonActivity.this,android.R.layout.simple_spinner_item, subjectsArray);
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            subjectSpinner.setAdapter(aa);

                            //Date and time picker
                            final Button btnChooseDate = (Button) findViewById(R.id.btnChooseDate);
                            btnChooseDate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Get Current Date
                                    final Calendar c = Calendar.getInstance();
                                    int mYear = c.get(Calendar.YEAR);
                                    int mMonth = c.get(Calendar.MONTH);
                                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddLessonActivity.this,
                                            new DatePickerDialog.OnDateSetListener() {

                                                @Override
                                                public void onDateSet(DatePicker view, int year,
                                                                      int monthOfYear, int dayOfMonth) {

                                                    final Calendar c = Calendar.getInstance();
                                                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                                                    int mMinute = c.get(Calendar.MINUTE);

                                                    //Month start from 0, so + 1
                                                    final String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

                                                    // Launch Time Picker Dialog
                                                    TimePickerDialog timePickerDialog = new TimePickerDialog(AddLessonActivity.this,
                                                            new TimePickerDialog.OnTimeSetListener() {
                                                                @Override
                                                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                                                      int minute) {

                                                                    String selectedTime = hourOfDay + ":" + minute;

                                                                    ((TextView)findViewById(R.id.tvDateSelected)).setText(selectedDate + " " + selectedTime);
                                                                }
                                                            }, mHour, mMinute, false);
                                                    timePickerDialog.show();

                                                }
                                            }, mYear, mMonth, mDay);
                                    datePickerDialog.show();
                                }
                            });

                            //Load student list
                            MultiSelectionSpinner spinner=(MultiSelectionSpinner) findViewById(R.id.mssStudents);

                            List<String> list = new ArrayList<String>();
                            for (int i = 0; i < student_list.length(); i++) {
                                JSONObject studentObject = student_list.getJSONObject(i);
                                String student = studentObject.getString("student_id") + ": " + studentObject.getString("name");
                                if (!list.contains(student)) {
                                    list.add(student);
                                }
                            }
                            spinner.setItems(list);
                        } else {
                            finish();
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

    private void AddLesson(){
        try {
            String subjectId = ((Spinner)findViewById(R.id.spinnerSubject)).getSelectedItem().toString().split(":")[0];
            int dateTime = tsToSec8601(((TextView)findViewById(R.id.tvDateSelected)).getText().toString());
            String [] studentList = ((MultiSelectionSpinner) findViewById(R.id.mssStudents)).getSelectedItemsAsString().split(",");
            String studentIdList = "";

            for (int i = 0; i < studentList.length; i++) {
                studentIdList += "'" + studentList[i].trim().split(":")[0] +"',";
            }
            studentIdList = studentIdList.substring(0, studentIdList.length() - 1);

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    "Config", getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("subjectId", subjectId);
            requestParameters.put("dateTime", dateTime);
            requestParameters.put("studentIdList", studentIdList);

            VolleyUtils.makeJsonObjectRequest(AddLessonActivity.this.getApplicationContext(), new ApiRoute().ADD_LESSON, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    finish();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(getApplicationContext(), "Successfully added lesson!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            finish();
                        }
                    }
                    catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            });

        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    //Convert datetime to epoch
    public static Integer tsToSec8601
    (String timestamp) {
        if (timestamp == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy h:m");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            Date dt = sdf.parse(timestamp);
            long epoch = dt.getTime();
            return (int) (epoch / 1000);
        } catch (ParseException e) {
            return null;
        }
    }
}
