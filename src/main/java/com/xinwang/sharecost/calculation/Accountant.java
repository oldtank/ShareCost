package com.xinwang.sharecost.calculation;

import com.xinwang.sharecost.Bill;
import com.xinwang.sharecost.Person;
import com.xinwang.sharecost.db.BillsRepo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by xinwang on 12/14/17.
 */

public class Accountant {
    private static final String TAG = "Accountant";
    private List<Person> people;
    private Map<Person, Long> personAmountMap;
    private List<Bill> bills;
    private Map<UUID, String> personIdNameMap;
    private Map<UUID, List<Person>> billPaidForMap;

    public Accountant(List<Person> people, List<Bill> bills, Map<UUID, List<Person>> billPaidForMap) {
        this.people = people;
        this.bills = bills;

        personIdNameMap = new HashMap<>();
        for (Person p : people) {
            personIdNameMap.put(p.getId(), p.getName());
        }

        this.billPaidForMap = billPaidForMap;
    }

    /**
     * calculate how to split the bill
     *
     * @return A map from debtor to the list of creditors he needs to pay to.
     */
    public Map<String, Map<String, Long>> splitBill() {
        Map<String, Map<String, Long>> result = new HashMap<>();

        Map<String, Long> payerNetAmountMap = getPayerNetAmountMap();

        TreeMap<Long, List<String>> creditors = new TreeMap<>();
        TreeMap<Long, List<String>> debtors = new TreeMap<>();
        getAmountToPayerMap(payerNetAmountMap, creditors, debtors);

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            // remove largest debtor(s) from map
            Map.Entry<Long, List<String>> largestDebtors = debtors.pollLastEntry();
            long debtAmount = largestDebtors.getKey();

            if (creditors.containsKey(debtAmount)) {
                // there is an exactly matched creditor. remove the creditor(s) from map
                List<String> creditorList = creditors.remove(debtAmount);
//                System.out.println(largestDebtors.getValue().get(0) + " pays " + creditorList.get(0) + " amount " + debtAmount);
                addPayment(largestDebtors.getValue().get(0), creditorList.get(0), debtAmount, result);

                // debtor fully paid the amount, removing from debtor list of that debt amount
                largestDebtors.getValue().remove(0);
                if (!largestDebtors.getValue().isEmpty()) {
                    // add the remaining debtors, if any, back to map
                    debtors.put(debtAmount, largestDebtors.getValue());
                }

                // remove fully paid creditor from list
                creditorList.remove(0);
                if (!creditorList.isEmpty()) {
                    // add the remaining creditors, if any
                    creditors.put(debtAmount, creditorList);
                }
            } else {
                // remove the largest creditors from map
                Map.Entry<Long, List<String>> largestCreditors = creditors.pollLastEntry();
                long creditAmount = largestCreditors.getKey();

                if (creditAmount > debtAmount) {
                    // debtor will pay his full owed amount
//                    System.out.println( largestDebtors.getValue().get(0) + " pays " + largestCreditors.getValue().get(0) + " amount " + debtAmount);
                    addPayment(largestDebtors.getValue().get(0), largestCreditors.getValue().get(0), debtAmount, result);

                    // remove debtor from list, since he has fully paid his amount
                    largestDebtors.getValue().remove(0);
                    if (!largestDebtors.getValue().isEmpty()) {
                        // add back remaining debtors if any, to map
                        debtors.put(debtAmount, largestDebtors.getValue());
                    }

                    // the creditor who took the money from debtor, hence his credit amount is reduced
                    String partiallyPaidCreditor = largestCreditors.getValue().get(0);
                    long remaining = creditAmount - debtAmount;

                    // add the remaining amount information back to map
                    if (creditors.containsKey(remaining)) {
                        creditors.get(remaining).add(partiallyPaidCreditor);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(partiallyPaidCreditor);
                        creditors.put(remaining, list);
                    }

                    // remove creditor from its original creditor list, since his amount has changed
                    largestCreditors.getValue().remove(0);
                    if (!largestCreditors.getValue().isEmpty()) {
                        // add remaining creditors back to map, if any
                        creditors.put(creditAmount, largestCreditors.getValue());
                    }
                } else {
//                    System.out.println(largestDebtors.getValue().get(0) + " pays " + largestCreditors.getValue().get(0) + " amount " + creditAmount);
                    addPayment(largestDebtors.getValue().get(0), largestCreditors.getValue().get(0), creditAmount, result);

                    largestCreditors.getValue().remove(0);
                    if (!largestCreditors.getValue().isEmpty()) {
                        creditors.put(creditAmount, largestCreditors.getValue());
                    }
                    String partiallyPaidDebtor = largestDebtors.getValue().get(0);
                    long remaining = debtAmount - creditAmount;
                    if (debtors.containsKey(remaining)) {
                        debtors.get(remaining).add(partiallyPaidDebtor);
                    } else {
                        List<String> list =new ArrayList<>();
                        list.add(partiallyPaidDebtor);
                        debtors.put(remaining, list);
                    }
                    largestDebtors.getValue().remove(0);
                    if (!largestDebtors.getValue().isEmpty()) {
                        debtors.put(debtAmount, largestDebtors.getValue());
                    }
                }
            }
        }
        printResult(result);
        return result;
    }

    private void printResult(Map<String, Map<String, Long>> result) {
        for (Map.Entry<String, Map<String, Long>> entry : result.entrySet()) {
//            System.out.println(entry.getKey() + " pays to :");
            for (Map.Entry<String, Long> creditorEntry : entry.getValue().entrySet()) {
//                System.out.println("\t" + creditorEntry.getKey() + " amount: " + creditorEntry.getValue());
            }
        }
    }

    private void addPayment(String debtor, String creditor, Long payment, Map<String, Map<String, Long>> result) {
        if (result.containsKey(debtor)) {
            Map<String, Long> creditors = result.get(debtor);
            if (creditors.containsKey(creditor)) {
                creditors.put(creditor, payment + creditors.get(creditor));
            } else {
                creditors.put(creditor, payment);
            }
        } else {
            Map<String, Long> creditors = new HashMap<>();
            creditors.put(creditor, payment);
            result.put(debtor, creditors);
        }
    }

    private Map<String, Long> getPayerNetAmountMap() {
        Map<String, Long> result = new HashMap<>();
        for (Person p : people) {
            result.put(p.getName(), Long.valueOf(0l));
        }
        for (Bill bill : bills) {
            String payer = personIdNameMap.get(bill.getPaidBy());
            result.put(payer, result.get(payer) + bill.getAmountCent());
            if (bill.isForEveryone()) {
                for (Person p : people) {
                    result.put(p.getName(),
                            Math.round(result.get(p.getName()) - (double) bill.getAmountCent() / people.size()));
                }
            } else {
                for (Person paidFor : billPaidForMap.get(bill.getId())) {
                    String paidForName = personIdNameMap.get(paidFor.getId());
                    double newAmount = result.get(paidForName) - (double) (bill.getAmountCent()) / billPaidForMap.get(bill.getId()).size();
                    result.put(paidForName, Math.round(newAmount));
                }
            }
        }
        return result;
    }

    private void getAmountToPayerMap(Map<String, Long> payerToAmountMap, TreeMap<Long, List<String>> creditors, TreeMap<Long, List<String>> debtors) {
        for (String payer : payerToAmountMap.keySet()) {
            Long amount = payerToAmountMap.get(payer);
            if (amount > 0) {
                addToTreeMap(amount, payer, creditors);
            } else {
                addToTreeMap(-1 * amount, payer, debtors);
            }
        }
    }

    private void addToTreeMap(Long amount, String payer, TreeMap<Long, List<String>> treeMap) {
        if (treeMap.containsKey(amount)) {
            treeMap.get(amount).add(payer);
        } else {
            List<String> list = new ArrayList<>();
            list.add(payer);
            treeMap.put(amount, list);
        }
    }

    private class ReverseComparator implements Comparator<Long> {
        @Override
        public int compare(Long l1, Long l2) {
            return l2.compareTo(l1);
        }
    }
}
