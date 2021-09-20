package com.xinwang.sharecost.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xinwang.sharecost.Event;
import com.xinwang.sharecost.db.DbSchema.EventsTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by xinwang on 12/12/17.
 */

public class EventsRepo {
    public static EventsRepo instance;

    private SQLiteDatabase db;
    private Context context;

    public static EventsRepo get(Context context) {
        if (instance == null) {
            instance = new EventsRepo(context);
        }
        return instance;
    }

    private EventsRepo(Context context) {
        this.context = context;
        this.db = new DbBaseHelper(this.context).getWritableDatabase();
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        try (EventCursorWrapper wrapper = queryEvents(null, null)) {
            wrapper.moveToNext();
            while (!wrapper.isAfterLast()) {
                events.add(wrapper.getEvent());
                wrapper.moveToNext();
            }
        }
//        Event test = new Event(UUID.randomUUID());
//        test.setTitle("this is a test");
//        events.add(test);

        return events;
    }

    public Event getEvent(UUID eventId) {
        String whereClause = EventsTable.Cols.UUID + "=?";
        String[] args = new String[] {eventId.toString()};

        try (EventCursorWrapper wrapper = queryEvents(whereClause, args)) {
            if (wrapper.getCount() == 0) {
                return null;
            }
            wrapper.moveToFirst();
            return wrapper.getEvent();
        }
    }

    public void deleteEvent(UUID eventId) {
        String whereClause = EventsTable.Cols.UUID + "=?";
        String[] args = new String[] {eventId.toString()};
        db.delete(EventsTable.NAME, whereClause, args);
    }

    public void updateEvent(Event event) {
        String uuidString = event.getId().toString();
        ContentValues values = getContentValues(event);

        db.update(EventsTable.NAME, values, EventsTable.Cols.UUID + "=?", new String[] {uuidString});
    }

    public void addEvent(Event event) {
        ContentValues values = getContentValues(event);
        db.insert(EventsTable.NAME, null, values);
    }

    public void deleteEvent(Event event) {
        String whereClause = EventsTable.Cols.UUID + "=?";
        String[] args = new String[] {event.getId().toString()};

        db.delete(EventsTable.NAME, whereClause, args);
    }

    private EventCursorWrapper queryEvents(String whereClause, String[] whereArgs) {
        Cursor cursor = db.query(EventsTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new EventCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Event event) {
        ContentValues values = new ContentValues();
        values.put(EventsTable.Cols.UUID, event.getId().toString());
        values.put(EventsTable.Cols.TITLE, event.getTitle());
        values.put(EventsTable.Cols.DATE, event.getDate().getTime());
        values.put(EventsTable.Cols.NUMBER_OF_PEOPLE, event.getNumberOfPeople());
        values.put(EventsTable.Cols.RESOLVED, event.isResolved() ? 1 : 0);

        return values;
    }
}
