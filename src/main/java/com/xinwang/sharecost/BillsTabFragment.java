package com.xinwang.sharecost;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.xinwang.sharecost.db.BillsRepo;
import com.xinwang.sharecost.db.PeopleRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.xinwang.sharecost.utils.Utilities.getFormattedCentForDisplay;

/**
 * Created by xinwang on 12/17/17.
 */

public class BillsTabFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";

    private static final int REQUEST_BILL = 0;

    private static final String DIALOG_BILL = "DialogBill";

    private RecyclerView billsRecyclerView;
    private BillsRecyclerViewAdapter adapter;
    private FloatingActionButton addBillButton;

    private UUID eventId;

    public static BillsTabFragment newInstance(UUID eventId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);

        BillsTabFragment fragment = new BillsTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bills_tab, container, false);
        billsRecyclerView = v.findViewById(R.id.bills_recycler_view);
        billsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        addBillButton = v.findViewById(R.id.add_bill_button);
        addBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                EditBillFragment fragment = EditBillFragment.newInstance(eventId);
                fragment.setTargetFragment(BillsTabFragment.this, REQUEST_BILL);
                fragment.show(fm, DIALOG_BILL);
            }
        });

        updateUI();
        return v;
    }

    public void updateUI() {
        if (isAdded()) {
            BillsRepo billsRepo = BillsRepo.get(getActivity());
            List<Bill> bills = billsRepo.getBills(eventId);

            if (adapter == null) {
                adapter = new BillsRecyclerViewAdapter(bills);
                billsRecyclerView.setAdapter(adapter);
            } else {
                adapter.setBills(bills);
                billsRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_BILL) {
            updateUI();
        }
    }

    /**
     * view holder for recycler view
     */
    private class BillsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private Bill bill;

        private TextView descTextView;
        private TextView amountTextView;
        private TextView paidByTextView;
        private TextView paidForTextView;

        public BillsRecyclerViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_bill_list_item, parent, false));
            descTextView = itemView.findViewById(R.id.bill_desc_text_view);
            amountTextView = itemView.findViewById(R.id.bill_amount_text_view);
            paidByTextView = itemView.findViewById(R.id.bill_paid_by_text_view);
            paidForTextView = itemView.findViewById(R.id.bill_paid_for_text_view);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        public void bind(Bill bill, int position) {
            this.bill = bill;
            descTextView.setText(bill.getDesc());
            amountTextView.setText(getFormattedCentForDisplay(bill.getAmountCent()));
            paidByTextView.setText("Paid by: " + PeopleRepo.get(getActivity()).getPerson(bill.getPaidBy()).getName());
            if (bill.isForEveryone()) {
                paidForTextView.setText("Paid for: everyone");
            } else {
                List<Person> paidFor = BillsRepo.get(getActivity()).getBillForPeople(bill.getId());
                StringBuilder sb = new StringBuilder("Paid for: ");
                for (Person p : paidFor) {
                    sb.append(p.getName()).append(", ");
                }
                paidForTextView.setText(sb.substring(0, sb.length()-2));
            }
        }

        @Override
        public void onClick(View view) {
            FragmentManager fm = getFragmentManager();
            EditBillFragment editBillFragment = EditBillFragment.newInstance(eventId, bill.getId());
            editBillFragment.setTargetFragment(BillsTabFragment.this, REQUEST_BILL);
            editBillFragment.show(fm, DIALOG_BILL);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem delete = menu.add(Menu.NONE, 0, 1, "Delete");
            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    BillsRepo.get(getActivity()).deleteBill(bill.getId());
                    updateUI();
                    return true;
                }
            });
        }
    }


    /**
     * view adapter for recycler view
     */
    private class BillsRecyclerViewAdapter extends RecyclerView.Adapter<BillsRecyclerViewHolder> {
        private List<Bill> bills;

        public BillsRecyclerViewAdapter(List<Bill> bills) {
            this.bills = new ArrayList<>();
            this.bills.addAll(bills);
        }

        public void setBills(List<Bill> bills) {
            this.bills.clear();
            this.bills.addAll(bills);
        }

        @Override
        public BillsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new BillsRecyclerViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(BillsRecyclerViewHolder holder, int position) {
            holder.bind(bills.get(position), position);
        }

        @Override
        public int getItemCount() {
            return bills.size();
        }
    }
}
