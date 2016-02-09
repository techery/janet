package io.techery.janet.sample.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class User implements Parcelable {

    Long id;

    String login;

    @SerializedName("avatar_url")
    String avatar;

    @SerializedName("html_url")
    String url;

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.login);
        dest.writeString(this.avatar);
        dest.writeString(this.url);
    }

    private User(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.login = in.readString();
        this.avatar = in.readString();
        this.url = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
