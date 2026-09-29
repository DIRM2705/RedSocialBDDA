package com.redsocial.dto;

public class UserProfileRequest
{
    private String profilePictureURL;
    private String bio;
    private int followersCount;
    private int followingCount;

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public String getBio() {
        return bio;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }
}
