package com.xinwang.sharecost.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.xinwang.sharecost.Person;
import com.xinwang.sharecost.db.DbSchema.BillsTable;
import com.xinwang.sharecost.db.DbSchema.PeopleTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by xinwang on 12/18/17.
 */

public class PeopleRepo {
    public static PeopleRepo instance;

    private SQLiteDatabase db;
    private Context context;

    private PeopleRepo(Context context) {
        this.context = context;
        this.db = new DbBaseHelper(this.context).getWritableDatabase();
    }

    public static PeopleRepo get(Context context) {
        if (instance == null) {
            instance = new PeopleRepo(context);
        }
        return instance;
    }

    public List<Person> getPeopleInEvent(UUID eventId) {
        List<Person> list= new ArrayList<>();
        String whereClause = PeopleTable.Cols.EVENT_ID + "=?";
        String[] args = new String[] {eventId.toString()};

        try (PersonCursorWrapper wrapper = queryPeople(whereClause, args)) {
            wrapper.moveToNext();
            while (!wrapper.isAfterLast()) {
                list.add(wrapper.getPerson());
                wrapper.moveToNext();
            }
        }

        return list;
    }

    public Map<Person, Long> getPeopleAmountInEvent(UUID eventId) {
        Map<Person, Long> map = new HashMap<>();
        String query = "select " +
                "p." + PeopleTable.Cols.UUID + ", " +
                "p." + PeopleTable.Cols.EVENT_ID + ", " +
                "p." + PeopleTable.Cols.NAME + ", " +
                "sum(b." + BillsTable.Cols.AMOUNT + ") " +
                " from " +
                PeopleTable.NAME + " p left join " +
                BillsTable.NAME + " b on " +
                "p." + PeopleTable.Cols.UUID + " = " +
                "b." + BillsTable.Cols.PAID_BY +
                " where p." + PeopleTable.Cols.EVENT_ID + "=?" +
                " group by p." + PeopleTable.Cols.UUID + ", " +
                "p." + PeopleTable.Cols.EVENT_ID + ", " +
                "p." + PeopleTable.Cols.NAME + " " +
                " order by p." + PeopleTable.Cols.NAME;
        try (Cursor result = db.rawQuery(query, new String[] {eventId.toString()} )) {

            result.moveToNext();
            while (!result.isAfterLast()) {
                Person person = new Person(
                        UUID.fromString(result.getString(0)),
                        UUID.fromString(result.getString(1))
                );
                person.setName(result.getString(2));
                Long amount = result.getLong(3);
                map.put(person, amount);

                result.moveToNext();
            }
        }
        return map;
    }

    public Person getPerson(UUID id) {
        String whereClause = PeopleTable.Cols.UUID + "=?";
        String[] args = new String[] {id.toString()};
        try (PersonCursorWrapper wrapper = queryPeople(whereClause, args)) {
            if (wrapper.getCount() == 0) {
                return null;
            }
            wrapper.moveToFirst();
            return wrapper.getPerson();
        }
    }

    public void addPerson(Person person) {
        ContentValues values = getContentValues(person);
        db.insert(PeopleTable.NAME, null, values);
    }

    public void updatePerson(Person person) {
        String whereClause = PeopleTable.Cols.UUID + "=?";
        String[] args = new String[] {person.getId().toString()};

        db.update(PeopleTable.NAME,
                getContentValues(person),
                whereClause,
                args);
    }

    private PersonCursorWrapper queryPeople(String whereClause, String[] args) {
        Cursor cursor = db.query(PeopleTable.NAME, null, whereClause, args, null, null, null);
        return new PersonCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Person person) {
        ContentValues values = new ContentValues();
        values.put(PeopleTable.Cols.UUID, person.getId().toString());
        values.put(PeopleTable.Cols.EVENT_ID, person.getEventId().toString());
        values.put(PeopleTable.Cols.NAME, person.getName());
        return values;
    }
}
