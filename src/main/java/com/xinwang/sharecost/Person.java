package com.xinwang.sharecost;

import java.util.UUID;

/**
 * Created by xinwang on 12/18/17.
 */

public class Person implements Comparable<Person> {
    private UUID id;
    private UUID eventId;
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person(UUID eventId) {
        this(UUID.randomUUID(), eventId);
    }

    public Person(UUID id, UUID eventId) {
        this.id = id;
        this.eventId = eventId;
        name = "";
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(Person p) {
        return this.id.compareTo(p.getId());
    }
}
