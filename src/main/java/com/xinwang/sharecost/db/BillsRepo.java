package com.xinwang.sharecost.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xinwang.sharecost.Bill;
import com.xinwang.sharecost.Person;
import com.xinwang.sharecost.db.DbSchema.BillsForPeopleTable;
import com.xinwang.sharecost.db.DbSchema.BillsTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by xinwang on 12/13/17.
 */

public class BillsRepo {
    public static BillsRepo instance;

    private SQLiteDatabase db;
    private Context context;

    public static BillsRepo get(Context context) {
        if (instance == null) {
            instance = new BillsRepo(context);
        }
        return instance;
    }

    private BillsRepo(Context context) {
        this.context = context;
        this.db = new DbBaseHelper(this.context).getWritableDatabase();
    }

    public void addBill(Bill bill) {
        ContentValues values = getContentValues(bill);
        db.insert(BillsTable.NAME, null, values);
    }

    public void deleteBill(UUID billId) {
        String whereClause = BillsTable.Cols.UUID + "=?";
        String[] args = new String[] {billId.toString()};
        db.delete(BillsTable.NAME, whereClause, args);
    }

    public List<Bill> getBills(UUID eventId) {
        List<Bill> bills = new ArrayList<>();

        String whereClause = BillsTable.Cols.EVENT_ID + "=?";
        String[] args = new String[] {eventId.toString()};
        try (BillCursorWrapper wrapper = queryBills(whereClause, args)) {
            wrapper.moveToNext();
            while(!wrapper.isAfterLast()) {
                bills.add(wrapper.getBill());
                wrapper.moveToNext();
            }
        }
        return bills;
    }

    public long getTotalAmount(UUID eventId) {
        String query = "select sum(" + BillsTable.Cols.AMOUNT + ") " +
                " from " + BillsTable.NAME +
                " where " + BillsTable.Cols.EVENT_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[] {eventId.toString()});
        if (cursor.getCount() == 0) {
            return 0;
        } else {
            cursor.moveToFirst();
            return cursor.getLong(0);
        }
    }

    public void updateBill(Bill bill) {
        String uuidString = bill.getId().toString();
        ContentValues values = getContentValues(bill);

        db.update(BillsTable.NAME, values, BillsTable.Cols.UUID + "=?", new String[] {uuidString});
    }

    public Bill getBill(UUID billId) {
        String whereClause = BillsTable.Cols.UUID + "=?";
        String[] args = new String[] {billId.toString()};

        try (BillCursorWrapper wrapper = queryBills(whereClause, args)) {
            if (wrapper.getCount() == 0) {
                return null;
            }
            wrapper.moveToFirst();
            return wrapper.getBill();
        }
    }

    public void deleteAndReAddBillForPeople(UUID billId, List<UUID> people) {
        db.beginTransaction();
        deleteBillForPeople(billId);
        addBillForPeople(billId, people);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteBillForPeople(UUID billId) {
        String whereClause = BillsForPeopleTable.Cols.BILL_ID + "=?";
        String[] args = new String[] {billId.toString()};
        db.delete(BillsForPeopleTable.NAME, whereClause, args);
    }

    public void addBillForPeople(UUID billId, List<UUID> people) {
        for (UUID personId: people) {
            ContentValues values = new ContentValues();
            values.put(BillsForPeopleTable.Cols.BILL_ID, billId.toString());
            values.put(BillsForPeopleTable.Cols.FOR, personId.toString());

            db.insert(BillsForPeopleTable.NAME, null, values);
        }
    }

    public List<Person> getBillForPeople(UUID billId) {
        List<Person> list = new ArrayList<>();
        String query = "select " +
                "b." + BillsForPeopleTable.Cols.FOR +", " +
                "p." + DbSchema.PeopleTable.Cols.NAME +
                " from " +
                BillsForPeopleTable.NAME + " b inner join " +
                DbSchema.PeopleTable.NAME + " p on " +
                "b." + BillsForPeopleTable.Cols.FOR + " = " +
                "p." + DbSchema.PeopleTable.Cols.UUID +
                " where b." + BillsForPeopleTable.Cols.BILL_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[] {billId.toString()});
//        Cursor cursor = db.query(BillsForPeopleTable.NAME, new String[] {BillsForPeopleTable.Cols.FOR},
//                BillsForPeopleTable.Cols.BILL_ID + "=?", new String[] {billId.toString()}, null, null, null);
        cursor.moveToNext();
        while(!cursor.isAfterLast()) {
            Person p = new Person(UUID.fromString(cursor.getString(0)), null);
            p.setName(cursor.getString(1));
            list.add(p);
            cursor.moveToNext();
        }
        return list;
    }

//    public Set<String> getBillPayers(UUID eventId) {
//        String whereClause = BillsTable.Cols.EVENT_ID + "=?";
//        String[] args = new String[] {eventId.toString()};
//        Set<String> payersSet = new HashSet<>();
//        try (BillCursorWrapper wrapper = queryBills(whereClause, args)) {
//            wrapper.moveToNext();
//            while (!wrapper.isAfterLast()) {
//                String payers = wrapper.getBill().getPaidBy();
//                if (payers != null && !payers.isEmpty()) {
//                    String[] payersArray = payers.split(",");
//                    for (String payer : payersArray) {
//                        payersSet.add(payer.trim());
//                    }
//                }
//                wrapper.moveToNext();
//            }
//        }
//        return payersSet;
//    }

    private BillCursorWrapper queryBills(String whereClause, String[] args) {
        Cursor cursor = db.query(BillsTable.NAME, null, whereClause, args, null, null, null);
        return new BillCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Bill bill) {
        ContentValues values = new ContentValues();
        values.put(BillsTable.Cols.UUID, bill.getId().toString());
        values.put(BillsTable.Cols.EVENT_ID, bill.getEventId().toString());
        values.put(BillsTable.Cols.AMOUNT, bill.getAmountCent());
        values.put(BillsTable.Cols.DATE, bill.getDate().getTime());
        values.put(BillsTable.Cols.PAID_BY, bill.getPaidBy().toString());
        values.put(BillsTable.Cols.DESC, bill.getDesc());
        values.put(BillsTable.Cols.FOR_EVERYONE, bill.isForEveryone() ? 1 : 0);

        return values;
    }
}
