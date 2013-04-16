/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.app.iparkamsterdam;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;

/**
 * 
 * @author mtavares */
public class TouchOverlay extends SimpleLocationOverlay {
	static interface OnTargetClickListener {
		void onClick (GeoPoint location);
	}

	private boolean isAClick = false;
	private long clickDownTime = Long.MAX_VALUE;
	private float[] clickDownCoords = new float[] {Float.NaN, Float.NaN};
	private float[] clickUpCoords = new float[] {Float.NaN, Float.NaN};
	private OnTargetClickListener onTargetClickListener = null;

	private int clickTargetToleranceHeight;
	private int clickTargetToleranceWidth;

	private boolean lockPosition = false;

	public TouchOverlay (Context ctx) {
		super (ctx);
		this.clickTargetToleranceWidth = this.PERSON_ICON.getWidth () / 2;
		this.clickTargetToleranceHeight = this.PERSON_ICON.getHeight () / 2;
	}

	@Override
	public boolean onDown (MotionEvent event, MapView mapView) {
		if (event.getPointerCount () > 0) {
			this.clickDownCoords[0] = event.getX ();
			this.clickDownCoords[1] = event.getY ();
			this.clickDownTime = event.getEventTime ();
			this.isAClick = true;
			return false;
		}
		return super.onDown (event, mapView);
	}

	private boolean onUp (MotionEvent event, MapView mapView) {
		if (this.onTargetClickListener != null && this.isAClick) {
			this.clickUpCoords[0] = event.getX ();
			this.clickUpCoords[1] = event.getY ();

			if (Math.abs (this.clickUpCoords[0] - this.clickDownCoords[0]) < 10 
					&& Math.abs (this.clickUpCoords[1] - this.clickDownCoords[1]) < 10) {
				IGeoPoint igeoPoint = mapView.getProjection ().fromPixels (event.getX (), event.getY ());
				GeoPoint geoPoint = new GeoPoint (igeoPoint.getLatitudeE6 (), igeoPoint.getLongitudeE6 ());
				if (event.getEventTime () - this.clickDownTime < android.view.ViewConfiguration.getLongPressTimeout ()
						&& isEventOnTarget (event, mapView)) {
					this.lockPosition = true;
					this.onTargetClickListener.onClick (getMyLocation ());
					return true;
				} else if (this.lockPosition == false
						&& event.getEventTime () - this.clickDownTime >= android.view.ViewConfiguration.getLongPressTimeout ()) {
					setLocation (geoPoint);
					mapView.invalidate ();
					return true;
				}
			}
			this.isAClick = false;
		}
		return false;
	}

	public void setLockPosition (boolean lock) {
		this.lockPosition = lock;
	}
	
	private Point myLocationPixels = new Point ();

	private boolean isEventOnTarget (MotionEvent e, MapView mapView) {
		GeoPoint myLocationGP = getMyLocation ();
		if (myLocationGP == null)
			return false;

		this.myLocationPixels = pointFromGeoPoint (myLocationGP, mapView);
		if (this.myLocationPixels == null) { // out of the screen
			return false;
		}

		int eX = (int) e.getX ();
		int eY = (int) e.getY ();
		if (eX >= this.myLocationPixels.x - this.clickTargetToleranceWidth
				&& eX <= this.myLocationPixels.x + this.clickTargetToleranceWidth
				&& eY >= this.myLocationPixels.y - this.clickTargetToleranceHeight
				&& eY <= this.myLocationPixels.y + this.clickTargetToleranceHeight) {
			this.onTargetClickListener.onClick (myLocationGP);
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent (MotionEvent event, MapView mapView) {
		switch (event.getAction ()) {
			case MotionEvent.ACTION_DOWN:
				return onDown (event, mapView);
			case MotionEvent.ACTION_UP:
				return onUp (event, mapView);
			default:
				this.clickUpCoords[0] = event.getX ();
				this.clickUpCoords[1] = event.getY ();
				
				if (Math.abs (this.clickUpCoords[0] - this.clickDownCoords[0]) >= 10 
						|| Math.abs (this.clickUpCoords[1] - this.clickDownCoords[1]) >= 10) {
					this.isAClick = false;
				}
				break;
		}
		return false;
	}

	public void setOnMapClickListener (OnTargetClickListener listener) {
		this.onTargetClickListener = listener;
	}

	private Point pointFromGeoPoint (GeoPoint gp, MapView mapView) {
		Point viewPoint = new Point ();
		Projection projection = mapView.getProjection ();
		projection.toPixels (gp, viewPoint);
		// Get the top left GeoPoint
		GeoPoint geoPointTopLeft = (GeoPoint) projection.fromPixels (0, 0);
		Point topLeftPoint = new Point ();
		// Get the top left Point (includes osmdroid offsets)
		projection.toPixels (geoPointTopLeft, topLeftPoint);
		viewPoint.x -= topLeftPoint.x; // remove offsets
		viewPoint.y -= topLeftPoint.y;
		if (viewPoint.x > mapView.getWidth () || viewPoint.y > mapView.getHeight () || viewPoint.x < 0 || viewPoint.y < 0) {
			return null; // gp must be off the screen
		}
		return viewPoint;
	}
}
