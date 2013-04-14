package org.bitpipeline.lib.parkshark;

import org.json.JSONArray;
import org.json.JSONException;

public class ParkingAdvice {
	private long id;
	private float price;
	private int distance;
	private String address;
	private float latitude;
	private float longitude;
	
	public ParkingAdvice (JSONArray json) throws JSONException {
		this.id = json.getLong (0);
		this.price = (float) json.getDouble (1);
		this.distance = json.getInt (2);
		this.address = json.getString (3);
		this.latitude = (float) json.getDouble (4);
		this.longitude = (float) json.getDouble (5);
	}

	public long getId () {
		return id;
	}

	public float getPrice () {
		return price;
	}

	public int getDistance () {
		return this.distance;
	}

	public String getAddress () {
		return address;
	}

	public float getLatitude () {
		return latitude;
	}

	public float getLongitude () {
		return longitude;
	}
}