package com.spatial.learning.jme.android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {
    public AndroidLauncher launcher;
    public View mainView;

    public StartFragment() {
        // Required empty public constructor
    }
    public static StartFragment newInstance(AndroidLauncher launcher) {
        StartFragment fragment = new StartFragment();
        fragment.launcher = launcher;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        System.out.println("\n\n\n\n\n\n\nOn Create View");
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        Button button = view.findViewById(R.id.start_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startJmeGame(v);
            }
        });
        return view;
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
    }

    public void startJmeGame(View view) {
        ConstraintLayout layout = getView().findViewById(R.id.frameLayout);
        EditText filePathEdit = layout.findViewById(R.id.filePathTextEdit);
        EditText playerNameEdit = layout.findViewById(R.id.nameTextEdit);
        if (filePathEdit==null || playerNameEdit==null) {
            throw new RuntimeException("FilePathEdit: "+filePathEdit+" PlayerNameEdit: "+playerNameEdit+" Either is null at StartFragment.StartJmeGame");
        }
        ((AndroidLauncher) requireActivity()).startGame(playerNameEdit.getText().toString(), filePathEdit.getText().toString());
    }
}