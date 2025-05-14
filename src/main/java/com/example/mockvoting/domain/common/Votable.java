package com.example.mockvoting.domain.common;

public interface Votable {
    int getUpvotes();
    int getDownvotes();
    void setUpvotes(int upvotes);
    void setDownvotes(int downvotes);
}
