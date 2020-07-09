package com.example.parstagram.models;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class User extends ParseUser {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PROFILE_IMAGE = "profileImage";

    public ParseFile getProfileImage() {
        return getParseFile(KEY_PROFILE_IMAGE);
    }

    public void setProfileImage(ParseFile parseFile) {
        put(KEY_PROFILE_IMAGE, parseFile);
    }
}
