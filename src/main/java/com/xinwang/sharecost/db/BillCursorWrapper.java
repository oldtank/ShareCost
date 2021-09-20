package com.xinwang.sharecost.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.xinwang.sharecost.Bill;
import com.xinwang.sharecost.db.DbSchema.BillsTable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Created by xinwang on 12/13/17.
 */

public class BillCursorWrapper extends CursorWrapper {
    public BillCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Bill getBill() {
        String uuidString = getString(getColumnIndex(BillsTable.Cols.UUID));
        String eventIdString = getString(getColumnIndex(BillsTable.Cols.EVENT_ID));
        String desc = getString(getColumnIndex(BillsTable.Cols.DESC));
        long date = getLong(getColumnIndex(BillsTable.Cols.DATE));
        long amount = getLong(getColumnIndex(BillsTable.Cols.AMOUNT));
        String paidByString = getString(getColumnIndex(BillsTable.Cols.PAID_BY));
        int forEveryone = getInt(getColumnIndex(BillsTable.Cols.FOR_EVERYONE));

        Bill bill = new Bill(UUID.fromString(uuidString), UUID.fromString(eventIdString));
        bill.setDesc(desc);
        bill.setDate(new Date(date));
        bill.setAmountCent(amount);
        bill.setPaidBy(UUID.fromString(paidByString));
        bill.setForEveryone(forEveryone == 1 ? true : false);

        return bill;
    }
}
