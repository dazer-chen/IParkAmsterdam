/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.app.iparkamsterdam;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

/**
 * 
 * @author mtavares */
public class TouchOverlay extends Overlay {
	static interface OnMapClickListener {
		void onClick (MapView mapView, GeoPoint geoPoint);
	}

	private boolean isAClick = false;
	private PointerCoords clickDownCoords = new PointerCoords ();
	private PointerCoords clickUpCoords = new PointerCoords ();
	private OnMapClickListener onMapClickListener = null;

	public TouchOverlay (Context ctx) {
		super (ctx);
	}

	@Override
	protected void draw (Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onTouchEvent (MotionEvent event, MapView mapView) {
		switch (event.getAction ()) {
			case MotionEvent.ACTION_DOWN:
				if (event.getPointerCount () > 0) {
					event.getPointerCoords (0, this.clickDownCoords);
					this.isAClick = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (this.onMapClickListener != null && this.isAClick) {
					if (event.getPointerCount () > 0)
						event.getPointerCoords (0, this.clickUpCoords);

					if (Math.abs (this.clickUpCoords.x - this.clickDownCoords.x) < 10 
							&& Math.abs (this.clickUpCoords.y - this.clickDownCoords.y) < 10) {
						IGeoPoint igeoPoint = mapView.getProjection ().fromPixels (event.getX (), event.getY ());
						GeoPoint geoPoint = new GeoPoint (igeoPoint.getLatitudeE6 (), igeoPoint.getLongitudeE6 ());
						this.onMapClickListener.onClick (mapView, geoPoint);
					}
					this.isAClick = false;
				}
				break;
			default:
				break;
		}
		return false;
	}

	public void setOnMapClickListener (OnMapClickListener listener) {
		this.onMapClickListener = listener;
	}
}
