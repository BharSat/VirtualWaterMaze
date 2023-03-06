package com.spatial.learning.jme.android;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalibrateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrateFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String FilePath = "param1";
    public static final String PlayerName = "param2";
    Mode mode = Mode.Calibrate;
    VrState vrState = new VrState((AndroidLauncher) this.requireActivity());

    // TODO: Rename and change types of parameters
    private String filePath;
    private String playerName;

    public CalibrateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalibrateFragment.
     */
    public static CalibrateFragment newInstance(String param1, String param2) {
        CalibrateFragment fragment = new CalibrateFragment();
        System.out.println("I repeat, so far, so good");
        Bundle args = new Bundle();
        args.putString(FilePath, param1);
        args.putString(PlayerName, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        if (args != null) {
            filePath = args.getString(FilePath);
            playerName = args.getString(PlayerName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filePath = getArguments().getString(FilePath);
            playerName = getArguments().getString(PlayerName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        System.out.println("\n\n\n\n\n\n\nOn Create View");
        View view;
        view = inflater.inflate(R.layout.calibrate_fragment_layout, container, false);
        Button doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(this::onDoneButtonClicked);

        return view;
    }

    public void onDoneButtonClicked(View button) {
        switch (mode) {
            case Calibrate:
                this.vrState.calibrate();
                mode=Mode.topLeft;
                break;
            case topLeft:
                vrState.position(mode);
                mode=Mode.topRight;
                break;
            case topRight:
                vrState.position(mode);
                mode=Mode.bottomLeft;
                break;
            case bottomLeft:
                vrState.position(mode);
                mode=Mode.topLeft;
                break;
            case bottomRight:
                vrState.position(mode);
                mode=Mode.End;
                break;
        }
    }

    public enum Mode {
        Calibrate,
        topLeft,
        topRight,
        bottomLeft,
        bottomRight,
        End
    }
}