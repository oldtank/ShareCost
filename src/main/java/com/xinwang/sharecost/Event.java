package com.xinwang.sharecost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by xinwang on 12/12/17.
 */

public class Event implements Serializable {
    private UUID id;
    private String title;
    private Date date;
    private int numberOfPeople;
    private boolean resolved;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Event() {
        this(UUID.randomUUID());
    }

    public Event(UUID id) {
        this.id = id;
        this.date = new Date();
        this.numberOfPeople = 2;
    }
}
