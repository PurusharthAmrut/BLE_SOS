package com.example.ble_sos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BlunoLibrary implements EditMessageFragment.EditMessageDialogListener {

    private static final String TAG = "MainActivity.java";

    private String[] bluetooth_permissions;
    private String[] remaining_permissions;

    private Button importContactsButton;
    private Button scanButton;
    private Button setupSMSButton;
    private static List<String> contactNames = new ArrayList<String>();

    ActivityResultLauncher<Intent> getContacts = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()==RESULT_OK) {
                        Intent intent = result.getData();
                        CharSequence[] data = intent.getBundleExtra(Constants.intent_bundle)
                                .getCharSequenceArray(Constants.bundle_contacts);
                        contactNames = Arrays.asList(Arrays.copyOf(data, data.length, String[].class));
//                        for (CharSequence contact : data) {
//                            contactNames.add((String)contact);
//                        }
                        if (!contactNames.isEmpty()) {
                            Log.d(TAG, "contactNames list populated with size " + contactNames.size());
                        } else {
                            Log.d(TAG, "contactsName list not populated");
                        }
                    } else {
                        StringBuilder error = new StringBuilder();
                        error.append("Incorrect result code received from ContactsFragment.java.")
                                .append(" Expected " + RESULT_OK)
                                .append(" but received " + result.getResultCode() + ".");
                        Log.d(TAG, error.toString());
                    }

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreateProcess();
        serialBegin(Constants.baud_rate);


        if (Build.VERSION.SDK_INT >= 30) {
            bluetooth_permissions = new String[] {Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN};
        } else {
            bluetooth_permissions = new String[] {Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN};
        }

        scanButton = findViewById(R.id.scanButton);
        importContactsButton = findViewById(R.id.importContactsButton);
        setupSMSButton = findViewById(R.id.setupSMSButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
//                if(requestRemainingPermissions(MainActivity.this, Constants.RC_BLUETOOTH, bluetooth_permissions)) {
//                    return;
//                }
                buttonScanOnClickProcess();
            }
        });
        setupSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                EditMessageFragment editMessageFragment = EditMessageFragment.
                        newInstance(getResources().getString(R.string.edit_message_dialog_title));
                editMessageFragment.show(fm, "fragment_edit_message");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onImportContactsButtonClicked(View view) {

        if (requestRemainingPermissions(this, Constants.RC_READ_CONTACTS, Manifest.permission.READ_CONTACTS)) {
            return;
        }

        Intent openContactsFragmentIntent = new Intent(this, ContactsActivity.class);
        Bundle contactsBundle = new Bundle();
        contactsBundle.putCharSequenceArray(Constants.bundle_contacts, contactNames.toArray(new String[] {}));
        openContactsFragmentIntent.putExtra(Constants.intent_bundle, contactsBundle);
        openContactsFragmentIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Log.d(TAG, "Sending contactNames intent with list of size " + contactNames.size());
        getContacts.launch(openContactsFragmentIntent);
    }

    @Override
    public void onConectionStateChange(connectionStateEnum connectionState) {

        switch (connectionState) {											//Four connection state
            case isConnected:
                scanButton.setText("Connected");
                break;
            case isConnecting:
                scanButton.setText("Connecting");
                break;
            case isToScan:
                scanButton.setText("Scan");
                break;
            case isScanning:
                scanButton.setText("Scanning");
                break;
            case isDisconnecting:
                scanButton.setText("isDisconnecting");
                break;
            default:
                break;
        }

    }

    @Override
    public void onSerialReceived(String theString) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.RC_READ_CONTACTS:
                for (String permission : permissions) {
                    if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                        Toast.makeText(this, "Please press the import contacts button again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;

            case Constants.RC_BLUETOOTH:
                int counter = 0;
                for (String permission : permissions) {
                    for (String bluetooth_permission : bluetooth_permissions) {
                        if (bluetooth_permission.equals(permission)) {
                            Log.d(TAG, permission + ": permission granted");
                            counter++;
                        }
                    }
                }
                if (counter!= remaining_permissions.length) {
                    Toast.makeText(this, "All bluetooth permissions not granted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "All permissions granted");
                    Toast.makeText(MainActivity.this, "Please press the SCAN button again",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case Constants.RC_SEND_SMS:
                for (String permission : permissions) {
                    if (permission.equals(Manifest.permission.SEND_SMS)) {
                        Toast.makeText(this, "Please press the deploy SMS button again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void deploySOS(View v) {
        if (requestRemainingPermissions(this, Constants.RC_SEND_SMS, Manifest.permission.SEND_SMS)) {
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("9819441364", null, EmergencySMS.getEmergencyMessage(), null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean requestRemainingPermissions(Context context, int requestCode, String... permissions) {
        Log.d(TAG, "Entering requestRemainingPermissions()");
        ArrayList<String> remainingPerms = new ArrayList<>();
        boolean remainingPermFlag = false;
        if (context!=null && permissions!=null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                    continue;
                else if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                    remainingPerms.add(permission);
                    remainingPermFlag = true;
                } else {
                    Log.e(TAG, "ContextCompat.checkSelfPermission returned unexpected value!");
                    remainingPerms.add(permission);
                    remainingPermFlag = true;
                }

            }
            if (!remainingPermFlag) return remainingPermFlag;

            String message, title;
            Resources res = getResources();
            switch (requestCode) {
                case Constants.RC_READ_CONTACTS:
                    title = res.getString(R.string.read_contacts_permission_message);
                    message = res.getString(R.string.read_contacts_permission_message);
                    break;

                case Constants.RC_BLUETOOTH:
                    title = res.getString(R.string.bluetooth_permission_title);
                    message = res.getString(R.string.bluetooth_permission_message);
                    break;

                case Constants.RC_SEND_SMS:
                    title = res.getString(R.string.send_sms_permission_title);
                    message = res.getString(R.string.send_sms_permission_message);
                    break;

                default:
                    Log.e(TAG, "Invalid request code to request permission");
                    return remainingPermFlag;
            }
            remaining_permissions = remainingPerms.toArray(new String[remainingPerms.size()]);
            for (String permission : remainingPerms) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions(remaining_permissions, requestCode);
                                }
                            })
                            .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                } else {
                    requestPermissions(remaining_permissions, requestCode);
                }
            }
        }
        return remainingPermFlag;
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        EmergencySMS.setEmergencyMessage(inputText);
        Log.d(TAG, "SOS message changed to "+EmergencySMS.getEmergencyMessage());
        Toast.makeText(this, "SOS Message has been set", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }
}