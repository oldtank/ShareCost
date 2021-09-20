package com.xinwang.sharecost;

import android.support.annotation.VisibleForTesting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Created by xinwang on 12/13/17.
 */

public class Bill implements Serializable {
    private UUID id;
    private UUID eventId;
    private String desc;
    private Date date;
    private long amountCent;
    private UUID paidBy;
    private boolean forEveryone;

    public Bill(UUID eventId) {
        this(UUID.randomUUID(), eventId);
    }

    public Bill(UUID id, UUID eventId) {
        this.id = id;
        this.date = new Date();
        this.amountCent = 0;
        this.eventId = eventId;
        this.forEveryone = true;
    }

    @VisibleForTesting
    public Bill(long amountCent, UUID paidBy) {
        this.amountCent = amountCent;
        this.paidBy = paidBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getAmountCent() {
        return amountCent;
    }

    public void setAmountCent(long amountCent) {
        this.amountCent = amountCent;
    }

    public UUID getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(UUID paidBy) {
        this.paidBy = paidBy;
    }

    public boolean isForEveryone() {
        return forEveryone;
    }

    public void setForEveryone(boolean forEveryone) {
        this.forEveryone = forEveryone;
    }
}
