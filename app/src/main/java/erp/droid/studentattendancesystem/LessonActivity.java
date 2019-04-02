package erp.droid.studentattendancesystem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.R.attr.bitmap;
import static android.view.View.GONE;
import static erp.droid.studentattendancesystem.R.id.tvStudentList;

public class LessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        String lessonId = getIntent().getStringExtra("lessonId");
        getLesson(lessonId);
    }

    public void getLesson(final String lessonId) {
        try {
            SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                    "Config", this.getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Only allow generate qr code if user is staff
            if (authObject.getString("userType").equals("staff")) {
                ((Button) this.findViewById(R.id.btnGenerateQrCode)).setVisibility(View.VISIBLE);
                ((ListView) this.findViewById(R.id.lvStudentInLesson)).setVisibility(View.VISIBLE);
                ((EditText) this.findViewById(R.id.etLecturer)).setVisibility(GONE);
                ((TextView) this.findViewById(R.id.tvLecturer)).setVisibility(GONE);
                ((TextView) this.findViewById(tvStudentList)).setVisibility(View.VISIBLE);

                ((Button) this.findViewById(R.id.btnGenerateQrCode)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        generateQrCode(lessonId);
                    }
                });
            } else {
                ((Button) this.findViewById(R.id.btnGenerateQrCode)).setVisibility(GONE);
                ((ListView) this.findViewById(R.id.lvStudentInLesson)).setVisibility(GONE);
                ((EditText) this.findViewById(R.id.etLecturer)).setVisibility(View.VISIBLE);
                ((TextView) this.findViewById(R.id.tvLecturer)).setVisibility(View.VISIBLE);
                ((TextView) this.findViewById(tvStudentList)).setVisibility(GONE);
            }

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("lessonId", lessonId);

            VolleyUtils.makeJsonObjectRequest(this.getApplicationContext(), new ApiRoute().GET_LESSON, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    finish();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject dataJson = new JSONArray(response.getString("data")).getJSONObject(0);
                            ((EditText) findViewById(R.id.etDateTime)).setText(dataJson.getString("date_time"));
                            ((EditText) findViewById(R.id.etSubject)).setText(dataJson.getString("subject_name"));

                            if (authObject.getString("userType").equals("student")) {
                                ((EditText) findViewById(R.id.etLecturer)).setText(dataJson.getString("staff_name"));

                                if (dataJson.getString("attended").equals("1")) {
                                    ((ImageView) findViewById(R.id.ivAttended)).setVisibility(View.VISIBLE);
                                } else {
                                    ((ImageView) findViewById(R.id.ivAttended)).setVisibility(GONE);
                                }
                            } else {
                                //for staff, list student in the class
                                JSONArray results = response.getJSONArray("data");
                                ArrayList<HashMap> resultList = new ArrayList<HashMap>();
                                boolean noStudent = true;

                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject jsonObject = results.getJSONObject(i);
                                    HashMap temp = new HashMap();

                                    temp.put("1", "");
                                    temp.put("2", "Student: " + jsonObject.getString("student_name"));

                                    if (jsonObject.getString("attended").equals("1")) {
                                        temp.put("3", "Attended: Yes");
                                    } else {
                                        temp.put("3", "Attended: No");
                                    }

                                    if (!jsonObject.getString("student_name").equals("null")) {
                                        noStudent = false;
                                    }

                                    resultList.add(temp);
                                }

                                ListView lview = (ListView) findViewById(R.id.lvStudentInLesson);
                                if (noStudent) {
                                    lview.setVisibility(GONE);
                                    ((TextView) findViewById(R.id.tvStudentList)).setVisibility(GONE);
                                } else {
                                    CustomListViewAdapter adapter = new CustomListViewAdapter(LessonActivity.this, resultList);
                                    lview.setAdapter(adapter);
                                }
                            }
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

    public void generateQrCode(String lessonId) {
        try {

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    "Config", getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("lesson_id", lessonId);

            VolleyUtils.makeJsonObjectRequest(getApplicationContext(), new ApiRoute().GENERATE_LESSON_ATTENDANCE_QR_CODE, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    finish();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {

                            JSONObject dataJson = new JSONObject(response.getString("data"));
                            String qrKey = dataJson.getString("qr_key_string");

                            Intent intent = new Intent(LessonActivity.this, DisplayQrActivity.class);
                            intent.putExtra("qrKey", qrKey);
                            LessonActivity.this.startActivity(intent);

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
}

