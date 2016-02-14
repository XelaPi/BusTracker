package com.blaine.bustracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Alex Vanyo
 */
public class BusAdapter extends ArrayAdapter<Bus> {

	private ArrayList<Integer> mCorrectedPositions;
	private int mLargestRow;
	private School mSchool;

	private Context mContext;

	public BusAdapter(Context context, School school) {
		super(context, R.layout.bus_holder);

		mContext = context;
		mSchool = school;
		mCorrectedPositions = new ArrayList<>();
	}

	private int getColumns() {
		return mSchool.getNumRows();
	}

	@Override
	public int getCount() {
		return mLargestRow * getColumns();
	}

	@Override
	public int getPosition(Bus bus) {
		return mCorrectedPositions.get(super.getPosition(bus));
	}

	@Override
	public Bus getItem(int position) {
		if (mCorrectedPositions.indexOf(position) < 0) {
			return null;
		} else {
			return super.getItem(mCorrectedPositions.indexOf(position));
		}
	}

	private int getRow(int position) {
		return position / getColumns() + 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {

		BusHolder busHolder;
		if (convertView == null) {
			busHolder = (BusHolder) ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bus_holder, parent, false);
		} else {
			busHolder = (BusHolder) convertView;
		}

		TextView busView = (TextView) busHolder.findViewById(R.id.bus_view);
		TextView indexView = (TextView) busHolder.findViewById(R.id.index_view);

		Bus bus = getItem(position);
		if (bus != null) {
			busView.setText(bus.getNumber());
			indexView.setText(String.valueOf(getRow(position)));

			if (mContext.getSharedPreferences(mContext.getString(R.string.shared_pref_user), Context.MODE_PRIVATE)
					.getStringSet(String.format(mContext.getString(R.string.pref_favorite_bus), String.valueOf(mSchool.getID())), new HashSet<String>())
					.contains(bus.getNumber())) {
				busHolder.setFavoriteBus(true);
			} else {
				busHolder.setFavoriteBus(false);
			}

			busHolder.setVisibility(View.VISIBLE);
		} else {
			busHolder.setVisibility(View.INVISIBLE);
		}

		return busHolder;
	}

	public void refreshRowPositions() {
		int[] rowCounts = new int[getColumns()];
		mCorrectedPositions.clear();
		for (int i = 0; i < super.getCount(); i++) {
			mCorrectedPositions.add(rowCounts[super.getItem(i).getRow()] * getColumns() + super.getItem(i).getRow());
			rowCounts[super.getItem(i).getRow()]++;
		}

		mLargestRow = 0;
		for (int rowCount : rowCounts) {
			if (rowCount > mLargestRow) {
				mLargestRow = rowCount;
			}
		}
	}

	@Override
	public void remove(Bus bus) {
		super.remove(bus);

		refreshRowPositions();
	}

	@Override
	public void add(Bus bus) {
		super.add(bus);

		refreshRowPositions();
	}

	@Override
	public void clear() {
		super.clear();

		refreshRowPositions();
	}

	public int searchForBus(String searchText) {
		for (int i = 0; i < super.getCount(); i++) {
			if (super.getItem(i) != null && super.getItem(i).getNumber().equals(searchText)) {
				return mCorrectedPositions.get(i);
			}
		}
		return -1;
	}
}
