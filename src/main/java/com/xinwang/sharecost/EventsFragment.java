package com.xinwang.sharecost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xinwang.sharecost.db.BillsRepo;
import com.xinwang.sharecost.db.EventsRepo;
import com.xinwang.sharecost.db.PeopleRepo;

import java.util.List;

import static com.xinwang.sharecost.utils.Utilities.getFormattedCentForDisplay;

/**
 * Created by xinwang on 12/12/17.
 */

public class EventsFragment extends Fragment {
    private static final String TAG = "EventsFragment";

    private RecyclerView eventsRecyclerView;
    private EventsRecyclerViewAdapter viewAdapter;

    private static final int REQUEST_EVENT = 0;
    private static final String DIALOG_EVENT = "DialogEvent";

    public static EventsFragment newInstance() {
        return new EventsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list, container, false);
        eventsRecyclerView = v.findViewById(R.id.events_recycler_view);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        return v;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        updateUI();
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_event:
//                Event event = new Event();
//                EventsRepo.get(getActivity()).addEvent(event);
//                Intent intent = EventActivity.createIntent(getActivity(), event.getId());
//                startActivity(intent);
                FragmentManager fm = getFragmentManager();
                EditEventFragment editEventFragment = EditEventFragment.newInstance();
                editEventFragment.setTargetFragment(EventsFragment.this, REQUEST_EVENT);
                editEventFragment.show(fm, DIALOG_EVENT);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_EVENT) {
            updateUI();
        }
    }

    private void updateUI() {
        EventsRepo eventsRepo = EventsRepo.get(getActivity());
        List<Event> events = eventsRepo.getEvents();

        if (viewAdapter == null) {
            viewAdapter = new EventsRecyclerViewAdapter(events);
            eventsRecyclerView.setAdapter(viewAdapter);
        } else {
            viewAdapter.setEvents(events);
            viewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * ViewHolder of recyclerView in fragment
     */
    private class EventsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView titleTextView;
        private TextView numPeopleTextView;
        private TextView totalAmountTextView;

        private Event event;

        public EventsRecyclerViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_event_list_item, parent, false));
            titleTextView = itemView.findViewById(R.id.event_title_text_view);
            numPeopleTextView = itemView.findViewById(R.id.event_num_people_text_view);
            totalAmountTextView = itemView.findViewById(R.id.event_total_amount_text_view);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        public void bind(Event event, int position) {
            this.event = event;
            List<Person> people = PeopleRepo.get(getActivity()).getPeopleInEvent(event.getId());
            numPeopleTextView.setText("Number of participants: " + people.size());
            totalAmountTextView.setText("Total cost: " + getFormattedCentForDisplay(BillsRepo.get(getActivity()).getTotalAmount(event.getId())));
            titleTextView.setText(event.getTitle());
        }

        @Override
        public void onClick(View v) {
            startActivity(TabbedActivity.createIntent(getActivity(), this.event.getId(), this.event.getTitle()));
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
            MenuItem delete = menu.add(Menu.NONE, 0, 1, "Delete");
            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    EventsRepo.get(getActivity()).deleteEvent(event.getId());
                    updateUI();
                    return true;
                }
            });
        }
    }

    /**
     * ViewAdapter of recyclerView in fragment
     */
    private class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewHolder> {
        private List<Event> events;

        public EventsRecyclerViewAdapter(List<Event> events) {
            this.events = events;
        }

        @Override
        public EventsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new EventsRecyclerViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(EventsRecyclerViewHolder viewHolder, int position) {
            Event event = events.get(position);
            viewHolder.bind(event, position);
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }
    }


}
