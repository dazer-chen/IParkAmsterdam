/**
 * Copyright 2012 
 *         J. Miguel P. Tavares <mtavares@bitpipeline.eu>
 *         BitPipeline
 */
package org.bitpipeline.lib.parkshark;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 
 * @author mtavares */
public class ParkSharkClient {
	static private String JSON_ADVICE_ELEMENT = "advice";
	private HttpClient httpClient;

	public ParkSharkClient () {
		this.httpClient = new DefaultHttpClient ();
	}

	public ParkSharkClient (HttpClient httpClient) {
		if (httpClient == null)
			throw new IllegalArgumentException ("Can't construct a ParkShark with a null HttpClient");
		this.httpClient = httpClient;
	}

	public List<ParkingAdvice> getParkingAdvice (int day, int hour, int min, int duration, float lat, float lon) throws JSONException, IOException {
		// day=5&hr=8&min=30&duration=3&lat=52.377&lon=4.9104&methods=cash,pin
		JSONObject queryResponse = doQuery (
				String.format (Locale.ENGLISH,
						"day=%d&hr=%d&min=%d&duration=%d&lat=%f&lon=%f&methods=cash,pin",
						day, hour, min, duration, lat, lon)
				);
		JSONArray jsonAdvices = queryResponse.getJSONArray (ParkSharkClient.JSON_ADVICE_ELEMENT);
		if (jsonAdvices == null || jsonAdvices.length () == 0)
			return Collections.emptyList ();
		List<ParkingAdvice> advices = new ArrayList<ParkingAdvice> (jsonAdvices.length ());
		for (int i = 0; i<jsonAdvices.length (); i++) {
			JSONArray jsonAdvice = jsonAdvices.getJSONArray (i);
			try {
				advices.add (new ParkingAdvice (jsonAdvice));
			} catch (JSONException ignore) {
				Log.e ("ParkSharkClient", "can't parse", ignore);
				System.out.println (jsonAdvice.toString (4));
			}
		}
		return advices;
	}

	private JSONObject doQuery (String apiParameters) throws JSONException, IOException {
		String responseBody = null;
		String queryString = "http://api.parkshark.nl/psapi/api.jsp?" + apiParameters;
		System.out.println (queryString);
		HttpGet httpget = new HttpGet (queryString);

		HttpResponse response = this.httpClient.execute (httpget);
		InputStream contentStream = null;
		try {
			StatusLine statusLine = response.getStatusLine ();
			if (statusLine == null) {
				throw new IOException (
						String.format ("Unable to get a response from ParkShark server"));
			}
			int statusCode = statusLine.getStatusCode ();
			if (statusCode < 200 && statusCode >= 300) {
				throw new IOException (
						String.format ("ParkShark server responded with status code %d: %s", statusCode, statusLine));
			}
			/* Read the response content */
			HttpEntity responseEntity = response.getEntity ();
			contentStream = responseEntity.getContent ();
			Reader isReader = new InputStreamReader (contentStream);
			int contentSize = (int) responseEntity.getContentLength ();
			if (contentSize < 0)
				contentSize = 8*1024;
			StringWriter strWriter = new StringWriter (contentSize);
			char[] buffer = new char[8*1024];
			int n = 0;
			while ((n = isReader.read(buffer)) != -1) {
					strWriter.write(buffer, 0, n);
			}
			responseBody = strWriter.toString ();
			contentStream.close ();
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException re) {
			httpget.abort ();
			throw re;
		} finally {
			if (contentStream != null)
				contentStream.close ();
		}
		return new JSONObject (responseBody);
	}

}
