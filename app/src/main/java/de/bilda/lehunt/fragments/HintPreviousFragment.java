package de.bilda.lehunt.fragments;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.TreeMap;

import de.bilda.lehunt.R;
import de.bilda.lehunt.activities.GameActivity;

@TargetApi(24)
public class HintPreviousFragment extends Fragment {

    private ScrollView scrollView;
    private LinearLayout allPrevHints;
    private static HintPreviousFragment hpf;


    public static HintPreviousFragment GetInstance() {
        if(hpf != null)
            return hpf;
        hpf = new HintPreviousFragment();
        return hpf;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hint_previous_fragment, container, false);

        scrollView = view.findViewById(R.id.scrollView);

        Button btnBackToHunt = view.findViewById(R.id.btnBackToHunt);
        btnBackToHunt.setOnClickListener(v -> {
            HintCurrentFragment hcf = HintCurrentFragment.GetInstance();
            ((GameActivity)getActivity()).replaceFragment(hcf, "hcf");
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void UpdateHintList(TreeMap<Integer, String> hints){

        if(scrollView.getChildCount() == 0) {
            ScrollView.LayoutParams params = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
            allPrevHints = new LinearLayout(getContext());
            allPrevHints.setLayoutParams(params);
            allPrevHints.setOrientation(LinearLayout.VERTICAL);
            hints.forEach((key, value) -> {
                addTV("Hint " + (key + 1) + ":");
                addTV(value);
            });
            scrollView.addView(allPrevHints);
        }
    }

    private void addTV(String s){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tv=new TextView(getContext());
        tv.setLayoutParams(lp);
        tv.setPadding(0,10,0, 10);
        tv.setText(s);
        allPrevHints.addView(tv);
    }

}
