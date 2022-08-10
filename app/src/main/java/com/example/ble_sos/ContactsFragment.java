package com.example.ble_sos;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final String TAG = "ContactsFragment.java";

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    private final static String[] FROM_COLUMNS = {
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            R.id.listItemTextView
    };

    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    //The column indexes will be the same as the order
    // in which they are defined in the above projection
    private static final int CONTACT_ID_INDEX = 0;
    private static final int CONTACT_KEY_INDEX = 1;
    private static final int CONTACT_NAME_INDEX = 2;

    private String searchString = "";
    private static final String SELECTION =
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";

    //Defines the array of values to replace the ? with
    private String[] selectionArgs = { searchString };

    // Define global mutable variables
    // Define a ListView object
    ListView contactsList;

    // Define variables for the contact the user selects
    // The contact's _ID value
    long contactId;

    // The contact's LOOKUP_KEY
    String contactKey;

    //The contact's DISPLAY_NAME_PRIMARY
    String contactName;

    // A content URI for the selected contact
    Uri contactUri;

    private List<String> contactNames = new ArrayList<>();

    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter cursorAdapter;

    public ContactsFragment() {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");
        contactsList = (ListView) getActivity().findViewById(R.id.contactsListView);
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.listitem_contact
                ,null, FROM_COLUMNS, TO_IDS, 0);
        contactsList.setAdapter(cursorAdapter);
        contactsList.setOnItemClickListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemClick");
        final int list_item_highlight_color = getResources().getColor(R.color.teal_200);
        SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapterView.getAdapter();
        Cursor cursor = cursorAdapter.getCursor();
        cursor.moveToPosition(i);
        contactId = cursor.getLong(CONTACT_ID_INDEX);
        contactKey = cursor.getString(CONTACT_KEY_INDEX);
        contactName = cursor.getString(CONTACT_NAME_INDEX);
        contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);
        if (contactName!=null) {
            ContactsActivity.addContact(contactName);
            Object itemAtPosition = adapterView.getItemAtPosition(i);
            TextView listItemTextView = view.findViewById(R.id.listItemTextView);
            listItemTextView.setBackgroundColor(getResources().getColor(R.color.teal_200));
            if (listItemTextView.getHighlightColor()!=list_item_highlight_color) {
                listItemTextView.setHighlightColor(getResources().getColor(R.color.teal_200));
            } else {
                listItemTextView.setHighlightColor(getResources().getColor(R.color.white));
            }
        } else {
            Toast.makeText(getActivity(), "Failed to access the contact", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        Log.d(TAG, "onCreateLoader triggered");
        searchString = "%"+searchString+"%";
        selectionArgs[0] = searchString;
        CursorLoader temp = new CursorLoader(getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArgs,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY+" ASC");

        return temp;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        // Put the result Cursor in the adapter for the ListView
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        //Delete the reference to the existing cursor
        cursorAdapter.swapCursor(null);
    }
}
