package com.blaine.bustracker;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

/**
 * @author Alex Vanyo
 */
public class IIDListenerService extends InstanceIDListenerService {
	@Override
	public void onTokenRefresh() {
		new HTTPAsyncTask(getResources(), getString(R.string.url_add_reg_id), "POST") {
			@Override
			protected Boolean doInBackground(String... arguments) {

				InstanceID instanceID = InstanceID.getInstance(IIDListenerService.this);
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
					Toast.makeText(IIDListenerService.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
					getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_registered), true).apply();
				} else {
					Toast.makeText(IIDListenerService.this, R.string.registration_failed, Toast.LENGTH_SHORT).show();
					getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_registered), false).apply();
				}
			}
		}.execute(getString(R.string.key_reg_id), "");
	}
}
