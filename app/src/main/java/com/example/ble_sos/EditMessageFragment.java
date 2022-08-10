package com.example.ble_sos;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditMessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditMessageFragment extends DialogFragment implements TextView.OnEditorActionListener {

    EditText editMessageEditText;

    public interface EditMessageDialogListener {
        void onFinishEditDialog(String inputText);
    }

    public EditMessageFragment() {
        // Required empty public constructor
    }

    public static EditMessageFragment newInstance(String title) {
        EditMessageFragment fragment = new EditMessageFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_message, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editMessageEditText = view.findViewById(R.id.enterMessageEditText);
        String title = "Enter message";
        if (getArguments()!=null) {
            title = getArguments().getString("title", "Enter message");
        }
        getDialog().setTitle(title);
        editMessageEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        editMessageEditText.setOnEditorActionListener(this);
    }


    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

        if (i == EditorInfo.IME_ACTION_DONE) {
            EditMessageDialogListener listener = (EditMessageDialogListener) getActivity();
            listener.onFinishEditDialog(editMessageEditText.getText().toString());
            dismiss();
            return true;
        }
        return false;
    }
}