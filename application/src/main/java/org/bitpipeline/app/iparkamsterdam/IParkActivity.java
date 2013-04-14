package org.bitpipeline.app.iparkamsterdam;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;

public class IParkActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		setProgressBarIndeterminateVisibility(true);
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_ipark);
	}

	@Override
	public boolean onCreateOptionsMenu (com.actionbarsherlock.view.Menu menu) {
		MenuInflater menuInflater = getSupportMenuInflater ();
		menuInflater.inflate (R.menu.ipark, menu);
		return true;
	}

}
