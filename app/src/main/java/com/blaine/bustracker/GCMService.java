package com.blaine.bustracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class GCMService extends GcmListenerService {
	private static final int NOTIFY_ID = 82696;
	private LocalBroadcastManager mLocalBroadcastManager;

	@Override
	public void onCreate() {
		super.onCreate();

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	public void onMessageReceived(String from, Bundle data) {
		if (!data.isEmpty()) {
			String message = String.valueOf(data.get(getString(R.string.key_message)));

			Intent messageIntent = new Intent(getString(R.string.intent_message));
			messageIntent.putExtra(getString(R.string.key_message), message);
			messageIntent.putExtra(getString(R.string.key_school_id), String.valueOf(data.get(getString(R.string.key_school_id))));

			if (message.equals(getString(R.string.key_add_bus)) || message.equals(getString(R.string.key_remove_bus))) {
				messageIntent.putExtra(getString(R.string.key_bus_row), String.valueOf(data.get(getString(R.string.key_bus_row))));
				messageIntent.putExtra(getString(R.string.key_bus_number), String.valueOf(data.get(getString(R.string.key_bus_number))));

				if (message.equals(getString(R.string.key_add_bus)) &&
						getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE)
								.getStringSet(String.format(getString(R.string.pref_favorite_bus), String.valueOf(data.get(getString(R.string.key_school_id)))), new HashSet<String>())
								.contains(String.valueOf(data.get(getString(R.string.key_bus_number))))) {
					sendNotification(String.valueOf(data.get(getString(R.string.key_school_id))),
							String.valueOf(data.get(getString(R.string.key_bus_row_name))),
							String.valueOf(data.get(getString(R.string.key_bus_number))),
							String.valueOf(data.get(getString(R.string.key_bus_position))));
				}
			}

			mLocalBroadcastManager.sendBroadcast(messageIntent);
		}
		super.onMessageReceived(from, data);
	}

	private void sendNotification(String schoolID, String busRowName, String busNumber, String busPosition) {

		int position = Integer.valueOf(busPosition);
		int mod100 = position % 100;
		int mod10 = position % 10;
		if(mod10 == 1 && mod100 != 11) {
			busPosition += "st";
		} else if(mod10 == 2 && mod100 != 12) {
			busPosition += "nd";
		} else if(mod10 == 3 && mod100 != 13) {
			busPosition += "rd";
		} else {
			busPosition += "th";
		}

		final Intent resultIntent = new Intent(this, ActivityMain.class);
		resultIntent.putExtra(getString(R.string.key_bus_number), busNumber);

		final Notification.Builder notifyBuilder = new Notification.Builder(this)
				.setContentTitle(String.format(getString(R.string.notification_title), busNumber))
				.setContentText(String.format(getString(R.string.notification_detail), busRowName, busPosition))
				.setSmallIcon(R.drawable.ic_directions_bus_white_48dp)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.launcher));

		notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
		notifyBuilder.setAutoCancel(true);

		new HTTPAsyncTask(getResources(), getString(R.string.url_get_school_info), "POST") {
			@Override
			protected void onSuccessInBackground(JSONObject jsonObject) throws JSONException {
				int id = jsonObject.getInt(getString(R.string.key_school_id));
				String name = jsonObject.getString(getString(R.string.key_name));
				int numRows = jsonObject.getInt(getString(R.string.key_rows));

				JSONArray JSONRowNames = jsonObject.getJSONArray(getString(R.string.key_row_names));

				ArrayList<String> rowNames = new ArrayList<>();
				for (int j = 0; j < JSONRowNames.length(); j++) {
					rowNames.add(JSONRowNames.getString(j));
				}

				int defaultRow = jsonObject.getInt(getString(R.string.key_default_row));

				resultIntent.putExtra(getString(R.string.key_school), new School(id, name, numRows, rowNames, defaultRow));
				PendingIntent resultPendingIntent = PendingIntent.getActivity(GCMService.this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
				notifyBuilder.setContentIntent(resultPendingIntent);

				((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_ID, notifyBuilder.build());
			}
		}.execute(getString(R.string.key_school_id), schoolID);
	}

}