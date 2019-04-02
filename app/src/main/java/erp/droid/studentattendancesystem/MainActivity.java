package erp.droid.studentattendancesystem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Default to profile page
        displaySelectedScreen(R.id.nav_profile);

        SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                "Config", this.getApplicationContext().MODE_PRIVATE);

        try {
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));
            View headerLayout = navigationView.getHeaderView(0);
            ((TextView) headerLayout.findViewById(R.id.tvUserName)).setText(authObject.getString("userId") + " - " + authObject.getString("name"));

            if(authObject.getString("userType").equals("student")) {
                //Bottom right floating button
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scanQR();
                    }
                });
            }



        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    public void scanQR() {
        try {
            IntentIntegrator qrScan = new IntentIntegrator(this);
            qrScan.initiateScan();
        }
        catch(Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
            }
            else {
                //if qr contains data
                try {
                    String qrKey = result.getContents();
                    attendLesson(qrKey);
                } catch (Exception e) {
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void attendLesson(String qrKey) {
        try {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    "Config", getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("qr_key_string", qrKey);

            VolleyUtils.makeJsonObjectRequest(getApplicationContext(), new ApiRoute().UPDATE_LESSON_ATTENDANCE, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    finish();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(MainActivity.this, "Successfully attended class", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid Lesson", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            });
        }
        catch(Exception ex) {
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        displaySelectedScreen(item.getItemId());
        return true;
    }

    public void displaySelectedScreen(int itemId) {
        //creating fragment object
        Fragment fragment = null;

        // Handle navigation view item clicks here.
        int id = itemId;

        if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();

        } else if (id == R.id.nav_schedule) {
            fragment = new ScheduleFragment();

        } else if (id == R.id.nav_attendance) {
            fragment = new AttendanceFragment();

        } else if (id == R.id.nav_notification) {
            fragment = new NotificationFragment();

        } else if (id == R.id.nav_logout) {
            Logout();
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void Logout() {
        try {
            SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(
                    "Config", this.getApplicationContext().MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear().commit();

            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }
}
