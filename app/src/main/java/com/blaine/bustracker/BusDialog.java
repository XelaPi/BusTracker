package com.blaine.bustracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

/**
 * Popup dialog displaying device information and options for it
 *
 * @author Alex Vanyo
 */
public class BusDialog extends Activity {

	private Bus mBus;
	private School mSchool;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.bus_dialog);

		mBus = (Bus) getIntent().getExtras().get(getString(R.string.key_bus));
		mSchool = (School) getIntent().getExtras().get(getString(R.string.key_school));

		((TextView) findViewById(R.id.bus_number)).setText(String.format(getResources().getString(R.string.bus_number_format), mBus.getNumber()));

		// Hide "Remove" option if not in administrator mode
		if (!getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).getString(String.format(getString(R.string.pref_password), mSchool.getID()), "").isEmpty()) {
			findViewById(R.id.remove_bus).setVisibility(View.VISIBLE);
		}

		// Update favorites button based on status
		if ((getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).getStringSet(String.format(getString(R.string.pref_favorite_bus), mSchool.getID()), new HashSet<String>())).contains(mBus.getNumber())) {
			((Button) findViewById(R.id.toggle_favorite_bus)).setText(R.string.remove_favorite_bus);
		}
	}

	public void toggleFavorites(View view) {
		SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE);

		HashSet<String> favoriteBuses = (HashSet<String>) sharedPreferences.getStringSet(String.format(getString(R.string.pref_favorite_bus), mSchool.getID()), new HashSet<String>());

		if (favoriteBuses.contains(mBus.getNumber())) {
			favoriteBuses.remove(mBus.getNumber());

			((Button) findViewById(R.id.toggle_favorite_bus)).setText(R.string.add_favorite_bus);
		} else {
			favoriteBuses.add(mBus.getNumber());

			((Button) findViewById(R.id.toggle_favorite_bus)).setText(R.string.remove_favorite_bus);
		}

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(String.format(getString(R.string.pref_favorite_bus), mSchool.getID())).apply();
		editor.putStringSet(String.format(getString(R.string.pref_favorite_bus), mSchool.getID()), favoriteBuses).apply();
	}

	public void remove(View view) {
		findViewById(R.id.remove_bus).setEnabled(false);
		new HTTPAsyncTask(getResources(), getString(R.string.url_remove_bus), "POST") {
			@Override
			protected void onPostExecute(Boolean success) {
				if (success) {
					Toast.makeText(BusDialog.this, R.string.remove_bus_success, Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(BusDialog.this, R.string.remove_bus_failure, Toast.LENGTH_SHORT).show();
				}
				findViewById(R.id.remove_bus).setEnabled(true);
			}
		}.execute(getString(R.string.key_school_id), String.valueOf(mSchool.getID()),
				getString(R.string.key_password), getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).getString(String.format(getString(R.string.pref_password), mSchool.getID()), ""),
				getString(R.string.key_bus_number), String.valueOf(mBus.getNumber()),
				getString(R.string.key_bus_row), String.valueOf(mBus.getRow()));
	}

	public void close(View view) {
		finish();
	}
}
