package org.bitpipeline.lib.parkshark;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkingAdvice implements Parcelable {
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

	public static final Parcelable.Creator<ParkingAdvice> CREATOR = new Parcelable.Creator<ParkingAdvice>() {
		public ParkingAdvice createFromParcel(Parcel in) {
			return new ParkingAdvice(in);
		}

		public ParkingAdvice[] newArray(int size) {
			return new ParkingAdvice[size];
		}
	};
	
	@Override
	public int describeContents () {
		// TODO Auto-generated method stub
		return 0;
	}

	private ParkingAdvice (Parcel in) {
		this.address = in.readString ();
		this.distance = in.readInt ();
		this.id = in.readLong ();
		this.latitude = in.readFloat ();
		this.longitude = in.readFloat ();
		this.price = in.readFloat ();
	}

	@Override
	public void writeToParcel (Parcel dest, int flags) {
		dest.writeString (this.address);
		dest.writeInt (this.distance);
		dest.writeLong (this.id);
		dest.writeFloat (this.latitude);
		dest.writeFloat (this.longitude);
		dest.writeFloat (this.price);
	}
}