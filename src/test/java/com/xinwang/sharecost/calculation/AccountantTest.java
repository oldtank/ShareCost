package com.xinwang.sharecost.calculation;

import com.xinwang.sharecost.Bill;
import com.xinwang.sharecost.Person;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by xinwang on 12/15/17.
 */

public class AccountantTest {
    private Accountant accountant;

    @Before
    public void setup() {


    }

    @Test
    public void test() {
        List<Person> people = new ArrayList<>();
        Person p1 = new Person(UUID.randomUUID());
        p1.setName("Xin");
        Person p2 = new Person(UUID.randomUUID());
        p2.setName("Youyou");
        Person p3 = new Person(UUID.randomUUID());
        p3.setName("random guy");

        people.add(p1);
        people.add(p2);
        people.add(p3);

        Map<Person, Long> map = new HashMap<>();
        map.put(p1, 200l);
        map.put(p2, 1000l);

        accountant = new Accountant(people, map);
        accountant.splitBill();
    }

}
