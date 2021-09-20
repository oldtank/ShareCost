package com.xinwang.sharecost.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.xinwang.sharecost.Person;
import com.xinwang.sharecost.db.DbSchema.PeopleTable;

import java.util.UUID;

/**
 * Created by xinwang on 12/18/17.
 */

public class PersonCursorWrapper extends CursorWrapper {
    public PersonCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Person getPerson() {
        String name = getString(getColumnIndex(PeopleTable.Cols.NAME));
        String uuidString = getString(getColumnIndex(PeopleTable.Cols.UUID));
        String eventIdString = getString(getColumnIndex(PeopleTable.Cols.EVENT_ID));

        Person person = new Person(UUID.fromString(uuidString), UUID.fromString(eventIdString));
        person.setName(name);
        return person;
    }
}
