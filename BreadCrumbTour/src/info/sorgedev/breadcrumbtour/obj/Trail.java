package info.sorgedev.breadcrumbtour.obj;

import java.util.Date;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Trail implements Parcelable {
	
	private Uri imageUri;
	private String name;
	private Date minTime;
	private int rating;
	
	public Trail() {
	}
	
	public Trail(Parcel in) {
		this.name = in.readString();
		this.minTime = new Date(in.readLong());
		this.rating = in.readInt();
		this.imageUri = Uri.parse(in.readString());
	}
	
	public static final Parcelable.Creator<Trail> CREATOR = new Parcelable.Creator<Trail>() {

		@Override
		public Trail createFromParcel(Parcel source) {
			return new Trail(source);
		}

		@Override
		public Trail[] newArray(int size) {
			return new Trail[size];
		}
		
	};

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeLong(minTime.getTime());
		dest.writeInt(rating);
		dest.writeString(imageUri.toString());
	}
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getMinTime() {
		return minTime;
	}
	public void setMinTime(Date minTime) {
		this.minTime = minTime;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public Uri getImageUri() {
		return imageUri;
	}
	public void setImageUri(Uri imageUri) {
		this.imageUri = imageUri;
	}

}
