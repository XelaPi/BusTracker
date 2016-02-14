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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Alex Vanyo
 */
public class ActivityLoad extends ListActivity {
	private GoogleCloudMessaging mGCM;
	private Context mApplicationContext;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ArrayAdapter<School> schoolAdapter = new ArrayAdapter<>(this, R.layout.school_view);

		setListAdapter(schoolAdapter);

		mApplicationContext = getApplicationContext();

		SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE);
		String registrationId = prefs.getString(getString(R.string.key_reg_id), "");

		if (registrationId.isEmpty()) {
			new HTTPAsyncTask(getResources(), getString(R.string.url_add_reg_id), "POST") {
				@Override
				protected Boolean doInBackground(String... arguments) {
					if (checkPlayServices()) {
						try {
							if (mGCM == null) {
								mGCM = GoogleCloudMessaging.getInstance(mApplicationContext);
							}
							// TODO: Update deprecated method
							arguments[1] = mGCM.register(getString(R.string.google_project_id));

						} catch (IOException ex) {
							Log.e(this.getClass().getName(), ex.getMessage());
						}

						if (!arguments[1].isEmpty()) {

							SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(getString(R.string.key_reg_id), arguments[1]);
							editor.apply();

							return super.doInBackground(arguments);
						}
					}
					return false;
				}

				@Override
				protected void onPostExecute(Boolean success) {
					if (success) {
						Toast.makeText(ActivityLoad.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ActivityLoad.this, R.string.registration_failed, Toast.LENGTH_SHORT).show();
					}
				}
			}.execute(getString(R.string.key_reg_id), "");
		}

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
