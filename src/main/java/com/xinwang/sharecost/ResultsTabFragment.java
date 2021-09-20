package com.xinwang.sharecost;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xinwang.sharecost.calculation.Accountant;
import com.xinwang.sharecost.db.BillsRepo;
import com.xinwang.sharecost.db.EventsRepo;
import com.xinwang.sharecost.db.PeopleRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.xinwang.sharecost.utils.Utilities.getFormattedCentForDisplay;
import static com.xinwang.sharecost.utils.Utilities.getFormattedDateForDisplay;

/**
 * Created by xinwang on 12/20/17.
 */

public class ResultsTabFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_TITLE = "event_title";

    private RecyclerView resultsRecyclerView;
    private ResultsRecyclerViewAdapter adapter;
    private FloatingActionButton sendReportButton;

    private Map<String, Map<String, Long>> paymentResult;

    private UUID eventId;
    private String eventTitle;

    public static ResultsTabFragment newInstance(UUID eventId, String eventTitle) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);
        args.putSerializable(ARG_EVENT_TITLE, eventTitle);

        ResultsTabFragment fragment = new ResultsTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
        this.eventTitle = getArguments().getString(ARG_EVENT_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result_tab, container, false);
        resultsRecyclerView = v.findViewById(R.id.result_recycler_view);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        sendReportButton = v.findViewById(R.id.send_report_button);
        sendReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, eventTitle);
                intent.putExtra(Intent.EXTRA_TEXT, getReport());
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        updateUI();
        return v;
    }

    public void updateUI() {
        if (isAdded()) {
            List<Person> people = PeopleRepo.get(getActivity()).getPeopleInEvent(eventId);

            Map<UUID, List<Person>> billPaidForMap = new HashMap<>();
            List<Bill> bills = BillsRepo.get(getActivity()).getBills(eventId);
            for (Bill bill : bills) {
                List<Person> paidFor = BillsRepo.get(getActivity()).getBillForPeople(bill.getId());
                billPaidForMap.put(bill.getId(), paidFor);
            }

            Accountant accountant = new Accountant(people, bills, billPaidForMap);
            paymentResult = accountant.splitBill();

            if (adapter == null) {
                adapter = new ResultsRecyclerViewAdapter(paymentResult);
                resultsRecyclerView.setAdapter(adapter);
            } else {
                adapter.setPayments(paymentResult);
                resultsRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * view holder for recycler view
     */

    private class ResultsRecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView debtorTextView;
        private TextView creditorTextView;
        private TextView amountTextView;

        private Payment payment;

        public ResultsRecyclerViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_result_list_item, parent, false));
            debtorTextView = itemView.findViewById(R.id.debtor_text_view);
            creditorTextView = itemView.findViewById(R.id.creditor_text_view);
            amountTextView = itemView.findViewById(R.id.amount_text_view);

        }

        public void bind(Payment payment, int position) {
            this.payment = payment;

            debtorTextView.setText(this.payment.getDebtor());
            creditorTextView.setText(this.payment.getCreditor());
            amountTextView.setText(getFormattedCentForDisplay(this.payment.getAmount()));
        }
    }

    private class ResultsRecyclerViewAdapter extends RecyclerView.Adapter<ResultsRecyclerViewHolder> {
        private List<Payment> payments;

        public ResultsRecyclerViewAdapter(Map<String, Map<String, Long>> map) {
            payments = new ArrayList<>();
            for (Map.Entry<String, Map<String, Long>> debtorEntry : map.entrySet()) {
                for (Map.Entry<String, Long> creditorEntry : debtorEntry.getValue().entrySet()) {
                    Payment payment = new Payment(debtorEntry.getKey(), creditorEntry.getKey(), creditorEntry.getValue());
                    payments.add(payment);
                }
            }
        }

        @Override
        public ResultsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ResultsRecyclerViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(ResultsRecyclerViewHolder viewHolder, int position) {
            viewHolder.bind(payments.get(position), position);
        }

        @Override
        public int getItemCount() {
            return payments.size();
        }

        public void setPayments(Map<String, Map<String, Long>> map) {
            payments.clear();
            for (Map.Entry<String, Map<String, Long>> debtorEntry : map.entrySet()) {
                for (Map.Entry<String, Long> creditorEntry : debtorEntry.getValue().entrySet()) {
                    Payment payment = new Payment(debtorEntry.getKey(), creditorEntry.getKey(), creditorEntry.getValue());
                    payments.add(payment);
                }
            }
        }
    }

    private class Payment {
        private String debtor;
        private String creditor;
        private long amount;

        public String getDebtor() {
            return debtor;
        }

        public void setDebtor(String debtor) {
            this.debtor = debtor;
        }

        public String getCreditor() {
            return creditor;
        }

        public void setCreditor(String creditor) {
            this.creditor = creditor;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public Payment(String debtor, String creditor, long amount) {
            this.debtor = debtor;
            this.creditor = creditor;
            this.amount = amount;
        }
    }

    private String getReport() {
        Event event = EventsRepo.get(getActivity()).getEvent(eventId);
        StringBuilder sb = new StringBuilder();
        sb.append(event.getTitle()).append("\n\n");

        sb.append(">>>>>> Participants:\n");
        List<Person> participants = PeopleRepo.get(getActivity()).getPeopleInEvent(eventId);
        Map<UUID, String> idToNameMap = new HashMap<>();
        for (int i=0; i < participants.size(); i++) {
            idToNameMap.put(participants.get(i).getId(), participants.get(i).getName());
            sb.append(participants.get(i));
            if (i != participants.size()-1) {
                sb.append(", ");
            }
        }

        sb.append("\n\n\n")
                .append(">>>>>> Bills:\n");
        List<Bill> bills = BillsRepo.get(getActivity()).getBills(eventId);
        for (Bill b : bills) {
            sb.append(b.getDesc()).append(" on ").append(getFormattedDateForDisplay(b.getDate())).append("\n");
            sb.append("Amount: ").append(getFormattedCentForDisplay(b.getAmountCent())).append("\n");
            sb.append("Paid by: ").append(idToNameMap.get(b.getPaidBy())).append("\n");
            List<Person> forPeople = BillsRepo.get(getActivity()).getBillForPeople(b.getId());
            sb.append("Paid for: ");
            if (b.isForEveryone()) {
                sb.append("everyone");
            } else {
                for (int i = 0; i < forPeople.size(); i++) {
                    sb.append(forPeople.get(i).getName());
                    if (i != forPeople.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("\n\n");
        }
        sb.append("\n");

        sb.append(">>>>>> Let's share it!\n");
        for (Map.Entry<String, Map<String, Long>> debtorEntry : paymentResult.entrySet()) {
            for (Map.Entry<String, Long> creditorEntry : debtorEntry.getValue().entrySet()) {
                sb.append(debtorEntry.getKey())
                        .append(" pays ")
                        .append(getFormattedCentForDisplay(creditorEntry.getValue()))
                        .append(" to ")
                        .append(creditorEntry.getKey())
                        .append("\n");

            }
        }

        return sb.toString();
    }
}
