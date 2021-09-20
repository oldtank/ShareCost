package com.xinwang.sharecost;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xinwang.sharecost.db.EventsRepo;
import com.xinwang.sharecost.db.PeopleRepo;

import java.util.List;
import java.util.UUID;

/**
 * Created by xinwang on 12/17/17.
 */

public class EditPersonFragment extends DialogFragment {
    private static final String ARG_EVENT_ID = "uuid";
    private static final String ARG_PERSON_ID = "id";

    private Person person;

    private boolean isNewPerson;
    private List<Person> currentPeopleList;
    private TextView nameTextView;

    public static EditPersonFragment newInstance(UUID eventId, UUID id) {
        EditPersonFragment fragment = new EditPersonFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);
        args.putSerializable(ARG_PERSON_ID, id);

        fragment.setArguments(args);
        fragment.isNewPerson = false;
        return fragment;
    }

    public static EditPersonFragment newInstance(UUID eventId) {
        EditPersonFragment fragment = new EditPersonFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        fragment.isNewPerson = true;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_person, null);

        UUID eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
        currentPeopleList = PeopleRepo.get(getActivity()).getPeopleInEvent(eventId);

        nameTextView = v.findViewById(R.id.person_name_text_view);
        if (!isNewPerson) {
            UUID personId = (UUID) getArguments().getSerializable(ARG_PERSON_ID);
            person = PeopleRepo.get(getActivity()).getPerson(personId);
        } else {
            person = new Person(UUID.randomUUID(), eventId);
        }
        nameTextView.setText(person.getName());
        nameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                person.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(isNewPerson ? "New person" : "Edit person")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isNewPerson) {
                            PeopleRepo.get(getActivity()).addPerson(person);
                        } else {
                            PeopleRepo.get(getActivity()).updatePerson(person);
                        }
                        sendResult(Activity.RESULT_OK);
                        dialog.dismiss();
                    }
                })
                .setView(v)
                .create();
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
//        intent.putExtra()
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
