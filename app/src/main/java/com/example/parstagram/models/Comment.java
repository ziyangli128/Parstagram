package com.example.parstagram.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {

    public static final String KEY_COMMENT = "comment";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_COMMENTED_POST = "commentedPost";
    public static final String KEY_CREATED_AT = "createdAt";

    public String getComment() {
        return getString(KEY_COMMENT);
    }

    public void setDescription(String comment) {
        put(KEY_COMMENT, comment);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public void setAuthor(ParseUser author) {
        put(KEY_AUTHOR, author);
    }

    public Post getCommentedPost() {
        return (Post) get(KEY_COMMENTED_POST);
    }

    public void setCommentedPost(Post commentedPost) {
        put(KEY_COMMENTED_POST, commentedPost);
    }
}
