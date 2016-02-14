package com.blaine.bustracker;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity to display a school's rows and buses
 * Must be passed an Intent with School extra
 * Can be passed an Intent with Bus extra to search for bus on load
 *
 * @author Alex Vanyo
 */
public class ActivityMain extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private EditText searchField;
	private BusAdapter mBusAdapter;
	private GridView mBusGridView;
	private LinearLayout mAddBusLayout;

	private BroadcastReceiver mBusUpdateReceiver;

	private School mSchool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mSchool = getIntent().getParcelableExtra(getString(R.string.key_school));
		mBusAdapter = new BusAdapter(ActivityMain.this, mSchool);

		getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);

		setTitle(mSchool.getName());

		searchField = (EditText) this.findViewById(R.id.search_field);
		searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchForBus(view);
					handled = true;
				}
				return handled;
			}
		});

		LinearLayout rowLabelLayout = (LinearLayout) this.findViewById(R.id.row_label_layout);
		mAddBusLayout = (LinearLayout) this.findViewById(R.id.add_bus_layout);

		// Add row labels and add bus sections for each row in school
		// TODO: Hide row label layout if schools has only one row
		for (final String rowName : mSchool.getRowNames()) {
			TextView rowLabel = (TextView) getLayoutInflater().inflate(R.layout.row_label, rowLabelLayout, false);
			rowLabel.setText(rowName);
			rowLabelLayout.addView(rowLabel);

			LinearLayout addBusView = (LinearLayout) getLayoutInflater().inflate(R.layout.add_bus_view, mAddBusLayout, false);
			final EditText busNumberField = (EditText) addBusView.findViewById(R.id.bus_number_field);
			final Button addBus = (Button) addBusView.findViewById(R.id.add_bus);

			busNumberField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					boolean handled = false;
					if (actionId == EditorInfo.IME_ACTION_SEND) {
						addBus.callOnClick();
						handled = true;
					}
					return handled;
				}
			});

			addBus.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

					if (busNumberField.getText().length() == 0) {
						// Check if add bus is empty
						Toast.makeText(ActivityMain.this, R.string.empty_bus_number, Toast.LENGTH_SHORT).show();
					} else if (mBusAdapter.searchForBus(busNumberField.getText().toString()) >= 0) {
						// Check if bus already exists
						Toast.makeText(ActivityMain.this, R.string.duplicate_bus_number, Toast.LENGTH_SHORT).show();
					} else {
						// Make HTTP request to add bus
						addBus.setEnabled(false);

						new HTTPAsyncTask(getResources(), getString(R.string.url_add_bus), "POST") {
							@Override
							protected void onPostExecute(Boolean success) {
								if (success) {
									Toast.makeText(ActivityMain.this, R.string.add_bus_success, Toast.LENGTH_SHORT).show();
									busNumberField.setText("");
								} else {
									Toast.makeText(ActivityMain.this, R.string.add_bus_failure, Toast.LENGTH_SHORT).show();
								}

								addBus.setEnabled(true);
							}
						}.execute(getResources().getString(R.string.key_school_id), String.valueOf(mSchool.getID()),
								getString(R.string.key_password), getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE)
										.getString(String.format(getString(R.string.pref_password), mSchool.getID()), ""),
								getString(R.string.key_bus_number), busNumberField.getText().toString(),
								getString(R.string.key_bus_row), String.valueOf(mSchool.getRowNames().indexOf(rowName)));
					}
				}
			});
			mAddBusLayout.addView(addBusView);
		}
		// Hide layout if not in administrator mode
		mAddBusLayout.setVisibility(getSharedPreferences(getString(R.string.shared_pref_user), Context.MODE_PRIVATE).getString(String.format(getString(R.string.pref_password), mSchool.getID()), "").isEmpty() ? View.GONE : View.VISIBLE);

		mBusGridView = (GridView) this.findViewById(R.id.buses_grid_view);
		mBusGridView.setNumColumns(mSchool.getNumRows());
		mBusGridView.setAdapter(mBusAdapter);

		mBusUpdateReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
		        int schoolID = Integer.parseInt(intent.getStringExtra(getString(R.string.key_school_id)));

		        if (schoolID == mSchool.getID()) {
			        String message = intent.getStringExtra(getString(R.string.key_message));

			        if (message.equals(getString(R.string.key_add_bus))) {
				        final int row = Integer.parseInt(intent.getStringExtra(getString(R.string.key_bus_row)));
				        final String busNumber = intent.getStringExtra(getString(R.string.key_bus_number));
				        runOnUiThread(new Runnable() {
					        public void run() {
						        mBusAdapter.add(new Bus(row, busNumber));
					        }
				        });
			        } else if (message.equals(getString(R.string.key_remove_bus))) {
				        final int row = Integer.parseInt(intent.getStringExtra(getString(R.string.key_bus_row)));
				        final String busNumber = intent.getStringExtra(getString(R.string.key_bus_number));
				        runOnUiThread(new Runnable() {
					        public void run() {
						        mBusAdapter.remove(new Bus(row, busNumber));
					        }
				        });
			        } else if (message.equals(getString(R.string.key_remove_all_buses))) {
				        runOnUiThread(new Runnable() {
					        public void run() {
						        mBusAdapter.clear();
					        }
				        });
			        }
		        }
	        }
	    };

		// Initial HTTP request to get buses
		// TODO: Add loading screen/spinner
		new HTTPAsyncTask(getResources(), getString(R.string.url_get_buses), "POST") {
			@Override
			protected void onSuccessInBackground(JSONObject jsonObject) throws JSONException {
				JSONArray JSONBuses = jsonObject.getJSONArray(getString(R.string.key_buses));
				for (int i = 0; i < JSONBuses.length(); i++) {
					JSONObject c = JSONBuses.getJSONObject(i);

					final String number = c.getString(getString(R.string.key_bus_number));
					final int row = c.getInt(getString(R.string.key_bus_row));

					runOnUiThread(new Runnable() {
						public void run() {
							mBusAdapter.add(new Bus(row, number));
						}
					});
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				if (success) {
					if (getIntent().hasExtra(getString(R.string.key_bus_number))) {
						searchField.setText(getIntent().getStringExtra(getString(R.string.key_bus_number)));
						searchForBus(searchField);
					}
				}
			}
		}.execute(getString(R.string.key_school_id), String.valueOf(mSchool.getID()));

		LocalBroadcastManager.getInstance(this).registerReceiver((mBusUpdateReceiver),
				new IntentFilter(getString(R.string.intent_message))
		);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.hasExtra(getString(R.string.key_bus_number))) {
			searchField.setText(intent.getStringExtra(getString(R.string.key_bus_number)));
			searchForBus(searchField);
		}
	}

	@Override
	protected void onDestroy() {
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(mBusUpdateReceiver);
	    super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_activity_main, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    // Launch login activity
	        case R.id.action_login:
		        Intent intent = new Intent(this, ActivityLogin.class);
		        intent.putExtra(getString(R.string.key_school_id), mSchool.getID());
		        startActivity(intent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void searchForBus(View view) {
		// Hide keyboard from search
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		int position = mBusAdapter.searchForBus(searchField.getText().toString());

		if (position != -1) {
			mBusGridView.setItemChecked(position, true);
			mBusGridView.smoothScrollToPosition(position);
		} else {
			Toast.makeText(this, R.string.search_failed, Toast.LENGTH_SHORT).show();
		}
	}

	public void busClick(View view) {
		TextView busView = (TextView) view.findViewById(R.id.bus_view);

		int position = mBusAdapter.searchForBus(busView.getText().toString());
		Bus bus = mBusAdapter.getItem(position);

		Intent intent = new Intent(this, BusDialog.class);
		intent.putExtra(getString(R.string.key_bus), bus);
		intent.putExtra(getString(R.string.key_school), mSchool);
		startActivity(intent);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(String.format(getString(R.string.pref_favorite_bus), mSchool.getID()))) {
			mBusAdapter.notifyDataSetChanged();
		} else if (key.equals(String.format(getString(R.string.pref_password), mSchool.getID()))) {
			mAddBusLayout.setVisibility(sharedPreferences.getString(key, "").isEmpty() ? View.GONE : View.VISIBLE);
		}
	}
}
