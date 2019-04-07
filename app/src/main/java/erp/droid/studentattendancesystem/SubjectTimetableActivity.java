package erp.droid.studentattendancesystem;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.view.View.GONE;
import static erp.droid.studentattendancesystem.R.id.tvStudentList;

public class SubjectTimetableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_timetable);

        String subjectId = getIntent().getStringExtra("subjectId");
        getSubjectTimetable(subjectId);
    }

    private void getSubjectTimetable(String subjectId) {
        try {
            SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                    "Config", this.getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("subject_id", subjectId);

            VolleyUtils.makeJsonObjectRequest(this.getApplicationContext(), new ApiRoute().GET_SUBJECT_TIMETABLE, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    finish();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject dataJson = new JSONArray(response.getString("data")).getJSONObject(0);
                            ((TextView) findViewById(R.id.tvTimetableSubjectName)).setText(dataJson.getString("subject_name"));

                            //Get the list of lessons
                            JSONArray results = response.getJSONArray("data");
                            buildTimetable(results);
                        } else {
                            finish();
                        }
                    } catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            });
        }
        catch(Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    private void buildTimetable(JSONArray dataList) {
        try {

            TableLayout tableLayout = (TableLayout)findViewById(R.id.tlSubjectTimetable);

            //loop through 14 weeks
            for (int i = 1; i <= 14; i++) {
                TableRow tableRow = new TableRow(this);
                tableRow.setGravity(Gravity.LEFT);

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.GRAY);
                gd.setStroke(1, Color.BLACK);

                TextView tvWeekNumber = new TextView(this);
                tvWeekNumber.setText("\nWeek " + i + "\n");
                tvWeekNumber.setBackground(gd);
                tvWeekNumber.setPadding(5, 5, 5, 5);
                tvWeekNumber.setTextColor(Color.WHITE);
                tvWeekNumber.setGravity(Gravity.CENTER);

                tableRow.addView(tvWeekNumber);

                //loop through each lesson
                for (int j = 0; j <= dataList.length() - 1; j++) {
                    JSONObject data = dataList.getJSONObject(j);

                    //if lesson is the same week
                    if (data.getInt("week") == i) {
                        TextView tvLesson = new TextView(this);

                        //Convert datetime
                        //Date lessonDateTime=new SimpleDateFormat("d/MM/yyyy h:m a").parse(data.getString("date_time"));

                        //Set TextView style
                        gd = new GradientDrawable();
                        gd.setCornerRadius(5);
                        gd.setStroke(1, Color.BLACK);

                        //if lesson is in the future, paint it white
                        /*if (lessonDateTime.compareTo(new Date()) > 1) {
                            gd.setColor(Color.WHITE);
                        }
                        else */
                        if (data.getString("attended").equals("1")) {
                            gd.setColor(Color.GREEN);
                        }
                        else {
                            gd.setColor(Color.RED);
                        }
                        tvLesson.setBackground(gd);

                        tvLesson.setPadding(5, 5, 5, 5);
                        tvLesson.setText("Date: " + data.getString("date_time").split(" ")[0] + "\n"
                                    + "Time: " + data.getString("date_time").split(" ")[1] + data.getString("date_time").split(" ")[2] + "\n"
                                    + "Lecturer: " + data.getString("staff_name"));

                        tableRow.addView(tvLesson);
                    }
                }

                tableLayout.addView(tableRow);
            }
        }
        catch(Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }
}
