package com.blaine.bustracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Alex Vanyo
 */
public class ActivityLogin extends Activity {
    private int mSchoolID;
    private EditText mPasswordField;
    private TextView mLoginStatus;
    private Button mRemoveAllBuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.login);

        mSchoolID = getIntent().getIntExtra(getString(R.string.key_school_id), -1);

        mPasswordField = (EditText) findViewById(R.id.password_field);
        mLoginStatus = (TextView) findViewById(R.id.status);
        mRemoveAllBuses = (Button) findViewById(R.id.remove_all_buses);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE);
        String password = prefs.getString(String.format(getString(R.string.pref_password), mSchoolID), "");
        if (!password.isEmpty()) {
            mPasswordField.setText(password);
            login(null);
        }
    }

    public void login(View view) {
        new HTTPAsyncTask(getResources(), getString(R.string.url_check_login), "POST") {
            @Override
            protected void onPostExecute(Boolean success) {
                SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if (success) {
                    editor.putString(String.format(getString(R.string.pref_password), mSchoolID), mPasswordField.getText().toString());

                    mLoginStatus.setText(R.string.status_logged_in);
                    mRemoveAllBuses.setVisibility(View.VISIBLE);

                } else {
                    editor.putString(String.format(getString(R.string.pref_password), mSchoolID), "");

                    mLoginStatus.setText(R.string.status_incorrect_password);
                    mRemoveAllBuses.setVisibility(View.INVISIBLE);
                }

                editor.apply();
            }
        }.execute(getString(R.string.key_school_id), String.valueOf(mSchoolID),
                getString(R.string.key_password), mPasswordField.getText().toString());
    }

    public void removeAllBuses(View view) {
        new HTTPAsyncTask(getResources(), getString(R.string.url_remove_all_buses), "POST") {
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(ActivityLogin.this, R.string.remove_all_buses_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityLogin.this, R.string.remove_all_buses_failure, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(getString(R.string.key_school_id), String.valueOf(mSchoolID),
                getString(R.string.key_password), mPasswordField.getText().toString());
    }
}
