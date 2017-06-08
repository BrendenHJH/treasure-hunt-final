package org.mixare;


import java.net.URI;

public class TreasureHuntUser {

    private String firebaseKey;
    private String userId;
    private String userName;
    private String userEmail;
    private URI photoURI;
    private double userLatitude;
    private double userLongitude;
    private int onSite;

    public TreasureHuntUser() {
    }

    public TreasureHuntUser(String userId, String userName, double userLatitude, double userLongitude) {
        this.firebaseKey = userId;
        this.userId = userId;
        this.userName = userName;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public URI getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(URI photoURI) {
        this.photoURI = photoURI;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public int getOnSite() {
        return onSite;
    }

    public void setOnSite(int onSite) {
        this.onSite = onSite;
    }
}
