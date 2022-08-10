package com.example.ble_sos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity.java";

    Bundle savedInstanceState;
    private static List<String> contactNames;
    Button doneContactsButton;


    public static void addContact(String contact) {
        if (contactNames == null) {
            Log.d(TAG, "contactNames list not initialized!");
        }
        contactNames.add(contact);
        Log.d(TAG, "New element in contactNames: "+contactNames.get(contactNames.size()-1));
        Log.d(TAG, "List size: "+contactNames.size());
    }

    public static String getContact(int i) {
        if (i >= contactNames.size()) return null;
        return contactNames.get(i);
    }

    public static String removeContact(String contact) {
        contactNames.remove(contact);
        return contact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Log.d(TAG, "onCreate");
        this.savedInstanceState = savedInstanceState;

        // Retrieving contacts list from intent
        CharSequence[] temp = getIntent().getBundleExtra(Constants.intent_bundle)
                .getCharSequenceArray(Constants.bundle_contacts);
        contactNames = Arrays.asList(Arrays.copyOf(temp, temp.length, String[].class));
        contactNames = new ArrayList<>(contactNames);
        if (getIntent()!=null)
        Log.d(TAG, "ContactNames list received through intent with size "+contactNames.size());

        doneContactsButton = (Button) findViewById(R.id.doneContactsButton);


        doneContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent(ContactsActivity.this, MainActivity.class);
                Bundle sendBundle = new Bundle();
                sendBundle.putCharSequenceArray(Constants.bundle_contacts, contactNames.toArray(new String[] {}));
                returnIntent.putExtra(Constants.intent_bundle, sendBundle);
                returnIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Log.d(TAG, "Setting result list of size "+contactNames.size());
                setResult(RESULT_OK, returnIntent);
                startActivity(returnIntent);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}