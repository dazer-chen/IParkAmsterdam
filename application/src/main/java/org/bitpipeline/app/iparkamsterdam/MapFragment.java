/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.app.iparkamsterdam;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.bitpipeline.app.iparkamsterdam.TouchOverlay.OnTargetClickListener;
import org.bitpipeline.lib.parkshark.ParkingAdvice;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

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
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * 
 * @author mtavares */
public class MapFragment
		extends SherlockFragment
		implements OnTargetClickListener, OnDateSetListener, OnTimeSetListener, NumberPickerDialog.OnNumberSetListener {
	static final String LOG_TAG = "MapFragment";

	static final private BoundingBoxE6 MAP_BOUNDS = new BoundingBoxE6 (
			52.436, 5.00,
			52.289, 4.778);
	static final private int START_ZOOM = 14;

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

	private TouchOverlay touchOverlay = null;
	private ItemizedIconOverlay<ParkingAdviceOverlayItem> advicesOverlay = null;

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
		this.mapView.getController ().setCenter (MapFragment.MAP_BOUNDS.getCenter ());
		this.mapView.setMinZoomLevel (Integer.valueOf (12));
		this.mapView.setMaxZoomLevel (null);
		this.mapView.setScrollableAreaLimit (MapFragment.MAP_BOUNDS);

		this.touchOverlay = new TouchOverlay (this.context);
		this.touchOverlay.setOnMapClickListener (this);

		this.advicesOverlay = new ItemizedIconOverlay<ParkingAdviceOverlayItem> (
				this.context,
				new ArrayList<ParkingAdviceOverlayItem> (),
				new org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<ParkingAdviceOverlayItem> () {
					@Override
					public boolean onItemSingleTapUp (int index, ParkingAdviceOverlayItem item) {
						System.out.println ("Click on item " + index);
						ParkingAdvice advice = item.getParkingAdvice ();
						Toast.makeText (MapFragment.this.context,
								String.format ("%s\n%.2f €", advice.getAddress (), advice.getPrice ()),
								Toast.LENGTH_SHORT).show ();
						return true;
					}

					@Override
					public boolean onItemLongPress (int index, ParkingAdviceOverlayItem item) {
						System.out.println ("LongPress on item " + index);
						ParkingAdvice advice = item.getParkingAdvice ();
						Toast.makeText (MapFragment.this.context,
								String.format ("%s\n%.2f €", advice.getAddress (), advice.getPrice ()),
								Toast.LENGTH_LONG).show ();
						return true;
					}});
		this.mapView.getOverlays ().add (this.touchOverlay);
		this.mapView.getOverlays ().add (this.advicesOverlay);
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
				new TimePickerDialog (
						this.context,
						this,
						this.cal.get (Calendar.HOUR_OF_DAY),
						this.cal.get (Calendar.MINUTE),
						true).show ();
				break;
			case R.id.map_fragment_menu_duration:
				new NumberPickerDialog (
						this.context,
						this,
						1,
						24,
						1)
				.setPickerTitle (R.string.map_fragment_dialog_duration_title)
				.show ();
				break;
		}
		return super.onOptionsItemSelected (item);
	}

	private boolean fetching = false;
	@Override
	public void onClick (GeoPoint geoPoint) {
		if (this.fetching) {
			Log.i (MapFragment.LOG_TAG, "Attempted to fetch parking advice when a query is already running");
			return;
		}
		this.fetching = true;
		
		float lat = (float) (geoPoint.getLatitudeE6 () / 1E6);
		float lon = (float) (geoPoint.getLongitudeE6 () / 1E6);

		Log.i (MapFragment.LOG_TAG,
				String.format (Locale.UK,
						"Fetching parking info to park at %.2f, %.2f", lat, lon));
		Toast.makeText (this.context,
				this.context.getResources ().getString (R.string.map_fragment_toast_fetch),
				Toast.LENGTH_SHORT).show ();

		new ParkingAdviceFetcherTask (new ParkingAdviceFetcherTask.ParkingAdviceFetcherListener () {
			@Override
			public void setResponse (ParkingAdviceFetcherTask.ParkingAdviceResponse response) {
				MapFragment.this.touchOverlay.setLockPosition (false);
				if (response.exception != null) {
					Log.e (MapFragment.LOG_TAG, "Failed to get the parking advice", response.exception);
					return;
				}
				if (response.parkingAdvices == null) { 
					Toast.makeText (MapFragment.this.context,
							MapFragment.this.context.getResources ().getString (R.string.map_fragment_toast_no_advice),
							Toast.LENGTH_SHORT).show ();
					return;
				}
				showAdvices (response.parkingAdvices);
			}
		}).execute (new ParkingAdviceFetcherTask.ParkingAdviceRequest (
				this.cal.get (Calendar.DAY_OF_WEEK)-1,
				this.cal.get (Calendar.HOUR_OF_DAY),
				this.cal.get (Calendar.MINUTE),
				this.duration,
				lat,
				lon));
	}

	private void showAdvices (List<ParkingAdvice> advices) {
		this.advicesOverlay.removeAllItems ();
		for (ParkingAdvice advice : advices)
			this.advicesOverlay.addItem (new ParkingAdviceOverlayItem (advice));
		this.mapView.invalidate ();
		this.fetching = false;
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

	@Override
	public void onNumberSet (NumberPicker view, int value) {
		this.duration = value;
		updateMenuEntries ();
	}

}
