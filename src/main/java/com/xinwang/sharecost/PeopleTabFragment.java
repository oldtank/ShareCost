package com.xinwang.sharecost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xinwang.sharecost.db.DbSchema;
import com.xinwang.sharecost.db.EventsRepo;
import com.xinwang.sharecost.db.PeopleRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.xinwang.sharecost.utils.Utilities.getFormattedCentForDisplay;

/**
 * Created by xinwang on 12/16/17.
 */

public class PeopleTabFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";

    private static final int REQUEST_PERSON = 0;
    private static final String DIAGLOG_PERSON = "DialogName";

    private RecyclerView peopleRecyclerView;
    private PeopleRecyclerViewAdapter viewAdapter;
    private FloatingActionButton addPersonButton;

    private UUID eventId;

    public static PeopleTabFragment newInstance(UUID eventId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);

        PeopleTabFragment fragment = new PeopleTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        this.eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people_tab, container, false);
        peopleRecyclerView = v.findViewById(R.id.people_recycler_view);
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        addPersonButton = v.findViewById(R.id.add_person_button);
        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                EditPersonFragment editPersonFragment = EditPersonFragment.newInstance(eventId);
                editPersonFragment.setTargetFragment(PeopleTabFragment.this, REQUEST_PERSON);
                editPersonFragment.show(fm, DIAGLOG_PERSON);
            }
        });

        updateUI();
        return v;
    }

    public void updateUI() {
        if (isAdded()) {
            Map<Person, Long> peopleAmountMap = PeopleRepo.get(getActivity()).getPeopleAmountInEvent(eventId);
            if (viewAdapter == null) {
                viewAdapter = new PeopleRecyclerViewAdapter(peopleAmountMap);
                peopleRecyclerView.setAdapter(viewAdapter);
            } else {
                peopleRecyclerView.setAdapter(viewAdapter);
                viewAdapter.setPeopleAmountMap(peopleAmountMap);
                viewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_PERSON) {
            updateUI();
        }
    }

    /**
     * view holder for recycler view
     */
    private class PeopleRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView;
        private TextView amountTextView;
        private Person person;
        private long amount;

        public PeopleRecyclerViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_people_list_item, parent, false));
            nameTextView = itemView.findViewById(R.id.person_name_text_view);
            amountTextView = itemView.findViewById(R.id.person_total_amount_text_view);
            itemView.setOnClickListener(this);
        }

        public void bind(Person person, long amount, int position) {
            this.person = person;
            this.amount = amount;
            nameTextView.setText(this.person.getName());
            amountTextView.setText(getFormattedCentForDisplay(amount));
        }

        @Override
        public void onClick(View view) {
            FragmentManager fm = getFragmentManager();
            EditPersonFragment editPersonFragment = EditPersonFragment.newInstance(eventId, person.getId());
            editPersonFragment.setTargetFragment(PeopleTabFragment.this, REQUEST_PERSON);
            editPersonFragment.show(fm, DIAGLOG_PERSON);
        }
    }

    /**
     * view adapter for recycler view
     */
    private class PeopleRecyclerViewAdapter extends RecyclerView.Adapter<PeopleRecyclerViewHolder> {
        private List<Map.Entry<Person, Long>> entryList;

        public PeopleRecyclerViewAdapter(Map<Person, Long> map) {
            entryList = new ArrayList<>();
            entryList.addAll(map.entrySet());
        }

        @Override
        public PeopleRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new PeopleRecyclerViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(PeopleRecyclerViewHolder viewHolder, int position) {
            Map.Entry<Person, Long> entry = entryList.get(position);
            viewHolder.bind(entry.getKey(), entry.getValue(), position);
        }

        @Override
        public int getItemCount() {
            return entryList.size();
        }

        public void setPeopleAmountMap(Map<Person, Long> map) {
            entryList.clear();
            entryList.addAll(map.entrySet());
        }
    }
}
