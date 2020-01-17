package com.example.lehunt;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class HintCurrentFragment extends Fragment {

    private TextView hint;
    private static HintCurrentFragment hcf;

    public static HintCurrentFragment GetInstance() {
        if(hcf != null)
            return hcf;
        hcf = new HintCurrentFragment();
        return hcf;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.hint_current_fragment, container, false);

        hint = retView.findViewById(R.id.tvHintCurrentHunt);

        Button btnPrevHints = retView.findViewById(R.id.btnShowPrevHints);
        btnPrevHints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HintPreviousFragment hpf = HintPreviousFragment.GetInstance();
                ((GameActivity)getActivity()).replaceFragment(hpf, "hpf");

            }
        });

        return retView;
    }


    public void UpdateHint(String msg){
        hint.setText("Last Hint:\n\n" + msg);
    }
}
