/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.app.iparkamsterdam;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.bitpipeline.app.iparkamsterdam.ParkingAdviceFetcherTask.ParkingAdviceFetcherListener;
import org.bitpipeline.app.iparkamsterdam.ParkingAdviceFetcherTask.ParkingAdviceResponse;
import org.bitpipeline.app.iparkamsterdam.TouchOverlay.OnMapClickListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * 
 * @author mtavares */
public class MapFragment extends SherlockFragment implements OnMapClickListener, OnDateSetListener, OnTimeSetListener {
	static final private String LOG_TAG = "MapFragment";

	static final private BoundingBoxE6 MAP_BOUNDS = new BoundingBoxE6 (
			52.291, 4.756,
			52.436, 5018);
	static final private GeoPoint START_CENTER = new GeoPoint (52.373056, 4.894222);
	static final private int START_ZOOM = 14;

	private GeoPoint mapCenter = MapFragment.START_CENTER;
	private int mapZoom = MapFragment.START_ZOOM;

	private java.text.DateFormat dateFormat = null;
	private java.text.DateFormat hourFormat = null;
	
	private MapView mapView = null;
	private Context context = null;

	private MenuItem dateItem = null;
	private MenuItem hourItem = null;
	private MenuItem durationItem = null;

	private Calendar cal = null;
	private int duration = 1; // in hours

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setHasOptionsMenu (true);
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.context = inflater.getContext ();
		this.mapView = new MapView (this.context, 256);

		this.mapView.setUseSafeCanvas (true);
		setHardwareAccelerationOff ();

		this.mapView.setBuiltInZoomControls (true);
		this.mapView.setMultiTouchControls (true);

		this.mapView.getController ().setZoom (this.mapZoom);
		this.mapView.getController ().setCenter (this.mapCenter);

		TouchOverlay touchOverlay = new TouchOverlay (this.context);
		touchOverlay.setOnMapClickListener (this);
		this.mapView.getOverlays ().add (0, touchOverlay);
		return this.mapView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setHardwareAccelerationOff() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			this.mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu (menu, inflater);
		inflater.inflate (R.menu.map_fragment, menu);

		this.context = getActivity ();
		this.dateFormat = DateFormat.getDateFormat (this.context);
		this.hourFormat = DateFormat.getTimeFormat (this.context);
		this.cal = new GregorianCalendar (Locale.getDefault ());

		this.dateItem = menu.findItem (R.id.map_fragment_menu_date);
		this.hourItem = menu.findItem (R.id.map_fragment_menu_hour);
		this.durationItem = menu.findItem (R.id.map_fragment_menu_duration);

		updateMenuEntries ();
	}

	private void updateMenuEntries () {
		this.dateItem.setTitle (this.dateFormat.format (this.cal.getTime ()));
		this.hourItem.setTitle (this.hourFormat.format (this.cal.getTime ()));
		this.durationItem.setTitle (
				String.format (
						Locale.getDefault (),
						"%d H",
						this.duration));
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId ()) {
			case R.id.map_fragment_menu_date:
				new DatePickerDialog (
						this.context,
						this,
						this.cal.get (Calendar.YEAR),
						this.cal.get (Calendar.MONTH),
						this.cal.get (Calendar.DAY_OF_MONTH)).show ();
				break;
			case R.id.map_fragment_menu_hour:
				new TimePickerDialog (this.context, this, 0, 0, true).show ();
				System.out.println ("menu hour");
				break;
			case R.id.map_fragment_menu_duration:
				System.out.println ("menu duration");
				break;
		}
		return super.onOptionsItemSelected (item);
	}

	@Override
	public void onClick (MapView mapView, GeoPoint geoPoint) {
		float lat = (float) (geoPoint.getLatitudeE6 () / 1E6);
		float lon = (float) (geoPoint.getLongitudeE6 () / 1E6);
		Toast.makeText (this.context,
				String.format (Locale.getDefault (), "Fetching parking info to park at %.2f, %.2f",
						lat, lon),
						Toast.LENGTH_SHORT).show ();
		new ParkingAdviceFetcherTask (new ParkingAdviceFetcherListener() {
			@Override
			public void setResponse (ParkingAdviceResponse response) {
				if (response.exception != null) {
					Log.e (MapFragment.LOG_TAG, "Failed to get the parking advice", response.exception);
					return;
				}
				if (response.parkingAdvices == null) { 
					System.out.println ("Null answers...");
					return;
				}
				System.out.println ("Found advices: " + response.parkingAdvices.size ());
				
			}
		}).execute (new ParkingAdviceFetcherTask.ParkingAdviceRequest (
				this.cal.get (Calendar.DAY_OF_WEEK)-1,
				this.cal.get (Calendar.HOUR_OF_DAY),
				this.cal.get (Calendar.MINUTE),
				this.duration,
				lat,
				lon));
	}

	@Override
	public void onDateSet (DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		this.cal.set (year, monthOfYear, dayOfMonth);
		updateMenuEntries ();
	}

	@Override
	public void onTimeSet (TimePicker view, int hourOfDay, int minute) {
		this.cal.set (Calendar.HOUR_OF_DAY, hourOfDay);
		this.cal.set (Calendar.MINUTE, minute);
		updateMenuEntries ();
	}

}
