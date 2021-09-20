package com.xinwang.sharecost.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.xinwang.sharecost.Event;
import com.xinwang.sharecost.db.DbSchema.EventsTable;

import java.util.Date;
import java.util.UUID;

/**
 * Created by xinwang on 12/12/17.
 */

public class EventCursorWrapper extends CursorWrapper {
    public EventCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Event getEvent() {
        String uuidString = getString(getColumnIndex(EventsTable.Cols.UUID));
        String title = getString(getColumnIndex(EventsTable.Cols.TITLE));
        long date = getLong(getColumnIndex(EventsTable.Cols.DATE));
        int number_of_people = getInt(getColumnIndex(EventsTable.Cols.NUMBER_OF_PEOPLE));
        int isResolved = getInt(getColumnIndex(EventsTable.Cols.RESOLVED));

        Event event = new Event(UUID.fromString(uuidString));
        event.setTitle(title);
        event.setDate(new Date(date));
        event.setNumberOfPeople(number_of_people);
        event.setResolved(isResolved != 0);

        return event;
    }
}
