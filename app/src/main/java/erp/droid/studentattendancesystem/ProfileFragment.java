package erp.droid.studentattendancesystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Profile");

        //Set focus listener on password field
        final EditText etPassword = (EditText) getView().findViewById(R.id.etPassword);
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    etPassword.setText("");
                } else {
                    if (etPassword.getText().toString().equals("")) {
                        etPassword.setText("********");
                    }
                }
            }
        });

        //Set click listener on button
        final Button btSaveProfile = (Button) getView().findViewById(R.id.btSaveProfile);
        btSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveForm();
            }
        });

        FillForm();
    }

    public void SaveForm() {
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                    "Config", getActivity().getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));


            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);
            requestParameters.put("name", ((EditText)getView().findViewById(R.id.etName)).getText().toString());
            requestParameters.put("age", ((EditText)getView().findViewById(R.id.etAge)).getText().toString());
            requestParameters.put("address", ((EditText)getView().findViewById(R.id.etAddress)).getText().toString());
            requestParameters.put("email", ((EditText)getView().findViewById(R.id.etEmail)).getText().toString());
            requestParameters.put("ic_number", ((EditText)getView().findViewById(R.id.etIcNumber)).getText().toString());
            requestParameters.put("telephone_number", ((EditText)getView().findViewById(R.id.etTelephoneNumber)).getText().toString());

            //only update password if changed
            if (!((EditText)getView().findViewById(R.id.etPassword)).getText().toString().equals("********")) {
                requestParameters.put("password", ((EditText)getView().findViewById(R.id.etPassword)).getText().toString());
            }


            VolleyUtils.makeJsonObjectRequest(getActivity().getApplicationContext(), new ApiRoute().UPDATE_PROFILE, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    ((MainActivity)getActivity()).Logout();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            ((MainActivity)getActivity()).displaySelectedScreen(R.id.nav_profile);
                        } else {
                        }
                    }
                    catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            });
        }
        catch(Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void FillForm() {
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                    "Config", getActivity().getApplicationContext().MODE_PRIVATE);
            final JSONObject authObject = new JSONObject(sharedPref.getString("AuthenticationObject", "default"));

            //Hide staff position if user is not staff
            if (authObject.getString("userType").equals("student")) {
                ((EditText)getView().findViewById(R.id.etStaffPosition)).setVisibility(View.GONE);
                ((TextView)getView().findViewById(R.id.tvStaffPosition)).setVisibility(View.GONE);
            }
            else {
                ((EditText)getView().findViewById(R.id.etStaffPosition)).setVisibility(View.VISIBLE);
                ((TextView)getView().findViewById(R.id.tvStaffPosition)).setVisibility(View.VISIBLE);
            }

            //Make api call
            JSONObject requestParameters = new JSONObject();
            requestParameters.put("authenticationObject", authObject);

            VolleyUtils.makeJsonObjectRequest(getActivity().getApplicationContext(), new ApiRoute().GET_PROFILE, requestParameters, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    ((MainActivity)getActivity()).Logout();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                                    "Config", getActivity().getApplicationContext().MODE_PRIVATE);

                            //Fill form
                            JSONObject dataJson = new JSONObject(response.getString("data"));

                            if (authObject.getString("userType").equals("student")) {
                                ((EditText)getView().findViewById(R.id.etUserId)).setText(dataJson.getString("student_id"));
                            }
                            else {
                                ((EditText)getView().findViewById(R.id.etUserId)).setText(dataJson.getString("staff_id"));
                                ((EditText)getView().findViewById(R.id.etStaffPosition)).setText(dataJson.getString("staff_position"));
                            }

                            ((EditText)getView().findViewById(R.id.etName)).setText(dataJson.getString("name"));
                            ((EditText)getView().findViewById(R.id.etAge)).setText(dataJson.getString("age"));
                            ((EditText)getView().findViewById(R.id.etEmail)).setText(dataJson.getString("email"));
                            ((EditText)getView().findViewById(R.id.etAddress)).setText(dataJson.getString("address"));
                            ((EditText)getView().findViewById(R.id.etIcNumber)).setText(dataJson.getString("ic_number"));
                            ((EditText)getView().findViewById(R.id.etTelephoneNumber)).setText(dataJson.getString("telephone_number"));

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
        catch(Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    public void updateForm() {

    }
}
