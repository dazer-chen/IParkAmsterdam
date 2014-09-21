/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.app.iparkamsterdam;

import java.util.List;

import org.bitpipeline.lib.parkshark.ParkSharkClient;
import org.bitpipeline.lib.parkshark.ParkingAdvice;

import android.os.AsyncTask;

/**
 * 
 * @author mtavares */
public class ParkingAdviceFetcherTask
	extends
		AsyncTask<ParkingAdviceFetcherTask.ParkingAdviceRequest, Integer, ParkingAdviceFetcherTask.ParkingAdviceResponse>{
	static public class ParkingAdviceRequest {
		int day;
		int hour;
		int min;
		int duration;
		float lat;
		float lon;

		public ParkingAdviceRequest (int day, int hour, int min, int duration, float lat, float lon) {
			this.day = day;
			this.hour = hour;
			this.min = min;
			this.duration = duration;
			this.lat = lat;
			this.lon = lon;
		}
	}

	static public class ParkingAdviceResponse {
		List<ParkingAdvice> parkingAdvices = null;
		Exception exception = null;

		public List<ParkingAdvice> getParkingAdvices () {
			return parkingAdvices;
		}

		public void setParkingAdvices (List<ParkingAdvice> parkingAdvices) {
			this.parkingAdvices = parkingAdvices;
		}

		public Exception getException () {
			return exception;
		}

		public void setException (Exception exception) {
			this.exception = exception;
		}
	}

	static public interface ParkingAdviceFetcherListener {
		void setResponse (ParkingAdviceResponse response);
	}

	private ParkingAdviceFetcherListener listener;
	public ParkingAdviceFetcherTask (ParkingAdviceFetcherListener listener) {
		this.listener = listener;
	}

	@Override
	protected ParkingAdviceResponse doInBackground (ParkingAdviceRequest... requests) {
		ParkSharkClient psClient = new ParkSharkClient ();
		ParkingAdviceRequest request = requests[0];
		ParkingAdviceResponse response = new ParkingAdviceResponse ();
		try {
			response.parkingAdvices = psClient.getParkingAdvice (request.day, request.hour, request.min, request.duration, request.lat, request.lon);
		} catch (Exception e) {
			response.exception = e;
		}
		return response;
	}

	@Override
	protected void onPostExecute (ParkingAdviceResponse result) {
		this.listener.setResponse (result);
	}
}
