package com.blaine.bustracker;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Alex Vanyo
 */
public class ActivityLoad extends ListActivity {
	private Context mApplicationContext;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ArrayAdapter<School> schoolAdapter = new ArrayAdapter<>(this, R.layout.school_view);

		setListAdapter(schoolAdapter);

		mApplicationContext = getApplicationContext();

		new HTTPAsyncTask(getResources(), getString(R.string.url_get_schools), "GET") {

			private ArrayList<School> mSchools;

			@Override
			protected void onSuccessInBackground(JSONObject jsonObject) throws JSONException {
				JSONArray JSONSchools = jsonObject.getJSONArray(getString(R.string.key_schools));

				mSchools = new ArrayList<>();
				for (int i = 0; i < JSONSchools.length(); i++) {
					JSONObject c = JSONSchools.getJSONObject(i);

					int id = c.getInt(getString(R.string.key_school_id));
					String name = c.getString(getString(R.string.key_name));
					int numRows = c.getInt(getString(R.string.key_rows));

					JSONArray JSONRowNames = c.getJSONArray(getString(R.string.key_row_names));

					ArrayList<String> rowNames = new ArrayList<>();
					for (int j = 0; j < JSONRowNames.length(); j++) {
						rowNames.add(JSONRowNames.getString(j));
					}

					int defaultRow = c.getInt(getString(R.string.key_default_row));

					mSchools.add(new School(id, name, numRows, rowNames, defaultRow));
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				if (success) {
					schoolAdapter.addAll(mSchools);
				}
			}
		}.execute();

		final SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE);

		if (!prefs.getBoolean(getString(R.string.pref_registered), false) && checkPlayServices()) {
			new HTTPAsyncTask(getResources(), getString(R.string.url_add_reg_id), "POST") {
				@Override
				protected Boolean doInBackground(String... arguments) {

					InstanceID instanceID = InstanceID.getInstance(ActivityLoad.this);
					try {
						arguments[1] = instanceID.getToken(getString(R.string.google_project_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
					} catch (IOException e) {
						Log.e(this.getClass().getName(), e.toString());
						return false;
					}

					return super.doInBackground(arguments);
				}

				@Override
				protected void onPostExecute(Boolean success) {
					if (success) {
						Toast.makeText(ActivityLoad.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
						prefs.edit().putBoolean(getString(R.string.pref_registered), true).apply();
					} else {
						Toast.makeText(ActivityLoad.this, R.string.registration_failed, Toast.LENGTH_SHORT).show();
						prefs.edit().putBoolean(getString(R.string.pref_registered), false).apply();
					}
				}
			}.execute(getString(R.string.key_reg_id), "");
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Intent intent = new Intent(mApplicationContext, ActivityMain.class);
		intent.putExtra(getString(R.string.key_school), (School) getListView().getItemAtPosition(position));
		startActivity(intent);
	}

	private boolean checkPlayServices() {
		int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
				GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			return false;
		}
		return true;
	}
}
