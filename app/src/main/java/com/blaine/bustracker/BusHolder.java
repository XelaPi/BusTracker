package com.blaine.bustracker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * View class for a bus, as provided by BusAdapter
 *
 * @author Alex Vanyo
 */
public class BusHolder extends LinearLayout implements Checkable {

	private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
	private static final int[] FAVORITE_BUS_STATE_SET= {R.attr.state_favorite_bus};

	private boolean mChecked = false;
	private boolean mIsFavoriteBus = false;

	public BusHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		 setChecked(!mChecked);
	}

	public void setFavoriteBus(boolean isFavoriteBus) {
		mIsFavoriteBus = isFavoriteBus;
		refreshDrawableState();
	}

	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
		refreshDrawableState();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		if (mIsFavoriteBus) {
			mergeDrawableStates(drawableState, FAVORITE_BUS_STATE_SET);
		}
		return drawableState;
	}
}
