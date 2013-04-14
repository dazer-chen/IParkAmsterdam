/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.app.iparkamsterdam;

import java.util.Locale;

import org.bitpipeline.lib.parkshark.ParkingAdvice;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * 
 * @author mtavares */
public class ParkingAdviceOverlayItem extends OverlayItem {
	final ParkingAdvice advice; 
	public ParkingAdviceOverlayItem (ParkingAdvice advice) {
		super (
				String.format (Locale.getDefault (), "%.2f €", advice.getPrice ()),
				advice.getAddress (),
				new GeoPoint (advice.getLatitude (), advice.getLongitude ()));
		this.advice = advice;
	}

	public ParkingAdvice getParkingAdvice () {
		return this.advice;
	}
}
