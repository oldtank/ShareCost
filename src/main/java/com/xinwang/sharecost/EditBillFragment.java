package com.xinwang.sharecost;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.xinwang.sharecost.db.BillsRepo;
import com.xinwang.sharecost.db.EventsRepo;
import com.xinwang.sharecost.db.PeopleRepo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.widget.GridLayout.UNDEFINED;
import static android.widget.GridLayout.spec;
import static com.xinwang.sharecost.utils.Utilities.getCentValueFromString;
import static com.xinwang.sharecost.utils.Utilities.getFormattedCentForDisplay;
import static com.xinwang.sharecost.utils.Utilities.getFormattedDateForDisplay;

/**
 * Created by xinwang on 12/17/17.
 */

public class EditBillFragment extends DialogFragment {
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_UUID = "uuid";

    private static final int REQUEST_DATE = 0;

    private static final String DIALOG_DATE = "DialogDate";

    private boolean isNewBill;

    private Bill bill;

    private TextView descTextView;
    private TextView amountTextView;
    private Spinner paidBySpinner;
    private Button dateButton;
    private Switch forEveryoneSwitch;
    private GridLayout checkBoxGrid;

    public static EditBillFragment newInstance(UUID eventId, UUID billId) {
        EditBillFragment fragment = new EditBillFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);
        args.putSerializable(ARG_UUID, billId);

        fragment.setArguments(args);
        fragment.isNewBill = false;
        return fragment;
    }

    public static EditBillFragment newInstance(UUID eventId) {
        EditBillFragment fragment = EditBillFragment.newInstance(eventId, UUID.randomUUID());
        fragment.isNewBill = true;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_bill, null);

        UUID eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
        List<Person> peopleInEvent = PeopleRepo.get(getActivity()).getPeopleInEvent(eventId);
        final Map<String, UUID> personNameToUUIDMap = new HashMap<>();
        final List<String> forPeople = new ArrayList<>();
        for (Person p : peopleInEvent) {
            personNameToUUIDMap.put(p.getName(), p.getId());
        }

        if (isNewBill) {
            bill = new Bill(eventId);
        } else {
            UUID billId = (UUID) getArguments().getSerializable(ARG_UUID);
            bill = BillsRepo.get(getActivity()).getBill(billId);
        }
        List<Person> currentlyForPeople = BillsRepo.get(getActivity()).getBillForPeople(bill.getId());
        for (Person p : currentlyForPeople) {
            forPeople.add(p.getName());
        }

        final Event event = EventsRepo.get(getActivity()).getEvent(eventId);

        descTextView = v.findViewById(R.id.bill_desc_text_view);
        descTextView.setText(bill.getDesc());
        descTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bill.setDesc(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        amountTextView = v.findViewById(R.id.bill_amount_text_view);
        amountTextView.setText(bill.getAmountCent() == 0 ? "" : getFormattedCentForDisplay(bill.getAmountCent()));
        amountTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    long amount = getCentValueFromString(s.toString());
                    bill.setAmountCent(amount);
                } catch (NumberFormatException e) {
                    bill.setAmountCent(0l);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        paidBySpinner = v.findViewById(R.id.bill_paid_by_spinner);
        ArrayAdapter<Person> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                peopleInEvent);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paidBySpinner.setAdapter(arrayAdapter);
        if (!isNewBill) {
            for (int i=0; i<peopleInEvent.size(); i++) {
                if (peopleInEvent.get(i).getId().compareTo(bill.getPaidBy()) == 0) {
                    paidBySpinner.setSelection(i);
                    break;
                }
            }
        }
        paidBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Person selected = (Person) parent.getItemAtPosition(position);
                bill.setPaidBy(selected.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateButton = v.findViewById(R.id.bill_date_button);
        dateButton.setText(getFormattedDateForDisplay(bill.getDate()));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(bill.getDate());
                dialog.setTargetFragment(EditBillFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        checkBoxGrid = v.findViewById(R.id.checkbox_grid);
        for (Person p : peopleInEvent) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(p.getName());
            checkBox.setChecked(forPeople.contains(p.getName()));
            checkBox.setLayoutParams(new GridLayout.LayoutParams(spec(UNDEFINED), spec(UNDEFINED, 1f)));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        forPeople.add(buttonView.getText().toString());
                    } else {
                        forPeople.remove(buttonView.getText().toString());
                    }
                }
            });
            checkBoxGrid.addView(checkBox);
        }
        checkBoxGrid.setVisibility(bill.isForEveryone() ? View.GONE : View.VISIBLE);
        forEveryoneSwitch = v.findViewById(R.id.bill_for_everyone_switch);
        forEveryoneSwitch.setChecked(bill.isForEveryone());
        forEveryoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBoxGrid.setVisibility(View.GONE);
                    bill.setForEveryone(true);
                } else {
                    bill.setForEveryone(false);
                    checkBoxGrid.setVisibility(View.VISIBLE);
                }
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(isNewBill ? "New bill" : "Edit bill")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<UUID> forPeopleId = new ArrayList<>();
                        for (String personName : forPeople) {
                            forPeopleId.add(personNameToUUIDMap.get(personName));
                        }
                        Log.i("tesT", forPeople.toString());
                        Log.i("test", forPeopleId.toString());
                        if (isNewBill) {
                            BillsRepo.get(getActivity()).addBill(bill);
                            if (!bill.isForEveryone()) {
                                BillsRepo.get(getActivity()).addBillForPeople(bill.getId(), forPeopleId);
                            }
                        } else {
                            BillsRepo.get(getActivity()).updateBill(bill);
                            if (!bill.isForEveryone()) {
                                BillsRepo.get(getActivity()).deleteAndReAddBillForPeople(bill.getId(), forPeopleId);
                            }
                        }
//                        Log.i("checkbox test", personForBillMap.toString());
                        sendResult(Activity.RESULT_OK);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = DatePickerFragment.getDataFromExtra(intent);
            dateButton.setText(getFormattedDateForDisplay(date));
            bill.setDate(date);
        }
    }
}
