package com.xinwang.sharecost.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xinwang.sharecost.BillsTabFragment;
import com.xinwang.sharecost.db.DbSchema.BillsForPeopleTable;
import com.xinwang.sharecost.db.DbSchema.BillsTable;
import com.xinwang.sharecost.db.DbSchema.EventsTable;
import com.xinwang.sharecost.db.DbSchema.PeopleTable;

/**
 * Created by xinwang on 12/12/17.
 */

public class DbBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "sharecost.db";

    public DbBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventsTableSQL = "create table " + EventsTable.NAME + "(" +
                EventsTable.Cols.UUID + " string primary key, " +
                EventsTable.Cols.TITLE + ", " +
                EventsTable.Cols.DATE + ", " +
                EventsTable.Cols.NUMBER_OF_PEOPLE + ", " +
                EventsTable.Cols.RESOLVED + ")";
        String createPeopleTableSQL = "create table " + PeopleTable.NAME + "(" +
                PeopleTable.Cols.UUID + " string primary key, " +
                PeopleTable.Cols.EVENT_ID + ", " +
                PeopleTable.Cols.NAME + " string, " +
                "foreign key (" + PeopleTable.Cols.EVENT_ID + ") references " + EventsTable.NAME + "(" + EventsTable.Cols.UUID + ") on delete cascade, " +
                "unique (" + PeopleTable.Cols.EVENT_ID + ", " + PeopleTable.Cols.NAME + "))";
        String createBillsTableSQL = "create table " + BillsTable.NAME + "(" +
                BillsTable.Cols.UUID + " string primary key, " +
                BillsTable.Cols.DATE + ", " +
                BillsTable.Cols.DESC + ", " +
                BillsTable.Cols.AMOUNT + " integer, " +
                BillsTable.Cols.PAID_BY + ", " +
                BillsTable.Cols.EVENT_ID + ", " +
                BillsTable.Cols.FOR_EVERYONE + " integer, " +
                "foreign key (" + BillsTable.Cols.EVENT_ID + ") references " + EventsTable.NAME + " (" + EventsTable.Cols.UUID + ") on delete cascade, " +
                "foreign key (" + BillsTable.Cols.PAID_BY + ") references " + PeopleTable.NAME + " (" + PeopleTable.Cols.UUID + "))";
        String createBillsForPeopleTable = " create table " + BillsForPeopleTable.NAME + "(" +
                BillsForPeopleTable.Cols.BILL_ID + " string, " +
                BillsForPeopleTable.Cols.FOR + " string, " +
                "foreign key (" + BillsForPeopleTable.Cols.BILL_ID + ") references " + BillsTable.NAME + " (" + BillsTable.Cols.UUID + ") on delete cascade, " +
                "foreign key (" + BillsForPeopleTable.Cols.FOR + ") references " + PeopleTable.NAME + " (" + PeopleTable.Cols.UUID +"), " +
                "unique (" + BillsForPeopleTable.Cols.BILL_ID + ", " + BillsForPeopleTable.Cols.FOR + "))";

        db.execSQL(createEventsTableSQL);
        db.execSQL(createPeopleTableSQL);
        db.execSQL(createBillsTableSQL);
        db.execSQL(createBillsForPeopleTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
