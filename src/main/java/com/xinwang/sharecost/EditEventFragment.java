package com.xinwang.sharecost;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xinwang.sharecost.db.EventsRepo;

import java.util.Date;
import java.util.UUID;

import static com.xinwang.sharecost.utils.Utilities.getFormattedDateForDisplay;

/**
 * Created by xinwang on 12/16/17.
 */

public class EditEventFragment extends DialogFragment {
    private static final String ARG_EVENT_ID = "uuid";
    private static final int REQUEST_DATE = 0;
    private static final String DIALOG_DATE = "dialog_date";
    private static final String EXTRA_EVENT = "com.xinwang.sharecost.event";

    private TextView titleTextView;
    private Button dateButton;

    private Event event;
    private boolean isNewEvent;

    public static EditEventFragment newInstance() {
        EditEventFragment fragment = new EditEventFragment();
        fragment.isNewEvent = true;
        return fragment;
    }

    public static EditEventFragment newInstance(UUID id) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, id);
        fragment.setArguments(args);
        fragment.isNewEvent = false;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_event, null);

        if (isNewEvent) {
            event = new Event(UUID.randomUUID());
        } else {
            UUID eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
            event = EventsRepo.get(getActivity()).getEvent(eventId);
        }

        titleTextView = v.findViewById(R.id.event_title_text_view);
        titleTextView.setText(event.getTitle());
        titleTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                event.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dateButton = v.findViewById(R.id.event_date_button);
        dateButton.setText(getFormattedDateForDisplay(event.getDate()));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(event.getDate());
                datePickerFragment.setTargetFragment(EditEventFragment.this, REQUEST_DATE);
                datePickerFragment.show(fm, DIALOG_DATE);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(isNewEvent ? "New event" : "Edit event")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isNewEvent) {
                            EventsRepo.get(getActivity()).addEvent(event);
                        } else {
                            EventsRepo.get(getActivity()).updateEvent(event);
                        }
                        sendResult(Activity.RESULT_OK);
                        dialog.dismiss();
                    }
                })
                .setView(v)
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = DatePickerFragment.getDataFromExtra(intent);
            dateButton.setText(date.toString());
            event.setDate(date);
        }
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_EVENT, event);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
