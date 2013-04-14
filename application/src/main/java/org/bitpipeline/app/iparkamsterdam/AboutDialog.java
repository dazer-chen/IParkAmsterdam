/**
 * 
 */
package org.bitpipeline.app.iparkamsterdam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * @author mtavares */
public class AboutDialog extends AlertDialog {
	static final public int ID = 101;
	
	/**
	 * @param context */
	public AboutDialog (Context context) {
		super (context);
		init ();
	}

	/**
	 * @param context
	 * @param theme */
	public AboutDialog (Context context, int theme) {
		super (context, theme);
		init ();
	}

	private void init () {
		setTitle (R.string.dialog_about_title);
		setIcon (R.drawable.ic_launcher);

		Resources resources = getContext ().getResources ();

		String vName = "";
		String vCode = "";

		try {
			PackageInfo packageInfo = getContext ().getPackageManager ().getPackageInfo (getContext ().getPackageName (), 0);
			vName = packageInfo.versionName;
			vCode = Integer.toString (packageInfo.versionCode);
		} catch (NameNotFoundException e) {
		}

		String aboutMessage = resources.getString (R.string.dialog_about_projects,
				resources.getString (R.string.app_name), vName, vCode);

		TextView messageView = new TextView (getContext());
		messageView.setTextAppearance (getContext (), android.R.style.TextAppearance_DialogWindowTitle);
		messageView.setText (Html.fromHtml (aboutMessage));
		messageView.setLinksClickable (true);
		messageView.setMovementMethod (LinkMovementMethod.getInstance ());

		ScrollView rootView = new ScrollView (getContext());
		rootView.setScrollbarFadingEnabled (false);
		rootView.addView (messageView);
		setView (rootView, 10, 10, 10, 10);
		
		setButton (BUTTON_NEUTRAL, "Close", new OnClickListener() {
			@Override
			public void onClick (DialogInterface dialog, int which) {
				// nothing to do.. just dismiss the dialog
			}
		});
	}
}
