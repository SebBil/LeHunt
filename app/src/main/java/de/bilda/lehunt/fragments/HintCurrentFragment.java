package de.bilda.lehunt.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.bilda.lehunt.R;
import de.bilda.lehunt.activities.GameActivity;

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
        btnPrevHints.setOnClickListener(v -> {
            HintPreviousFragment hpf = HintPreviousFragment.GetInstance();
            ((GameActivity)getActivity()).replaceFragment(hpf, "hpf");

        });

        return retView;
    }


    public void UpdateHint(String msg){
        hint.setText(String.format("Last Hint:\n\n%s", msg));
    }
}
