package com.xinwang.sharecost.db;

/**
 * Created by xinwang on 12/12/17.
 */

public class DbSchema {
    public static final class EventsTable {
        public static final String NAME = "events";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String NUMBER_OF_PEOPLE = "number_of_people";
            public static final String RESOLVED = "resolved";
        }
    }

    public static final class PeopleTable {
        public static final String NAME = "people";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String EVENT_ID = "event_id";
            public static final String NAME = "name";
        }
    }

    public static final class BillsTable {
        public static final String NAME = "bills";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String EVENT_ID = "event_id";
            public static final String PAID_BY = "paid_by";
            public static final String DESC = "desc";
            public static final String DATE = "date";
            public static final String AMOUNT = "amount";
            public static final String FOR_EVERYONE = "for_everyone";
        }
    }

    public static final class BillsForPeopleTable {
        public static final String NAME = "fillsforpeople";

        public static final class Cols {
            public static final String BILL_ID = "bill_id";
            public static final String FOR = "for";
        }
    }
}
