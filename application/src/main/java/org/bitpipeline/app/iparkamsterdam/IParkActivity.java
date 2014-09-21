package org.bitpipeline.app.iparkamsterdam;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class IParkActivity extends SherlockFragmentActivity {

	public IParkActivity () {
		// TODO Auto-generated constructor stub
	}

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

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId ()) {
			case R.id.menu_about:
				new AboutDialog (this).show ();
				return true;
		}
		return false;
	}
}
