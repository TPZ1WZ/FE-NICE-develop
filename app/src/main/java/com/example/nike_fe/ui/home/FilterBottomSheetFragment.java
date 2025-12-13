package com.example.nike_fe.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nike_fe.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;

import java.util.List;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private RangeSlider sliderPrice;
    private TextView tvMinPrice, tvMaxPrice;
    private ChipGroup cgSize, cgGender, cgRating, cgSort;
    private Button btnReset, btnApply;

    public FilterBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderPrice = view.findViewById(R.id.sliderPrice);
        tvMinPrice = view.findViewById(R.id.tvMinPrice);
        tvMaxPrice = view.findViewById(R.id.tvMaxPrice);

        cgSize = view.findViewById(R.id.cgSize);
        cgGender = view.findViewById(R.id.cgGender);
        cgRating = view.findViewById(R.id.cgRating);
        cgSort = view.findViewById(R.id.cgSort);

        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);

        setupListeners();
    }

    private void setupListeners() {
        // Set initial values programmatically to avoid XML inflation issues
        if (sliderPrice != null) {
            sliderPrice.setValues(0f, 500f);
            sliderPrice.addOnChangeListener(new RangeSlider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                    List<Float> values = slider.getValues();
                    if (values.size() == 2) {
                        tvMinPrice.setText(String.format("$%.0f", values.get(0)));
                        tvMaxPrice.setText(String.format("$%.0f", values.get(1)));
                    }
                }
            });
        }

        btnReset.setOnClickListener(v -> {
            if (cgSize != null)
                cgSize.clearCheck();
            if (cgGender != null)
                cgGender.clearCheck();
            if (cgRating != null)
                cgRating.clearCheck();
            if (cgSort != null)
                cgSort.clearCheck();
            if (sliderPrice != null)
                sliderPrice.setValues(0f, 500f);
        });

        btnApply.setOnClickListener(v -> {
            // Collect data
            String appliedFilters = "Filters Applied:\n";

            // Just for demo, gather some info
            if (cgSort != null && cgSort.getCheckedChipId() != -1) {
                Chip c = cgSort.findViewById(cgSort.getCheckedChipId());
                if (c != null)
                    appliedFilters += "Sort: " + c.getText() + "\n";
            }

            // Collect Price
            if (sliderPrice != null) {
                List<Float> values = sliderPrice.getValues();
                if (values.size() == 2) {
                    appliedFilters += String.format("Price: $%.0f - $%.0f\n", values.get(0), values.get(1));
                }
            }

            // ... collect others ...

            Toast.makeText(getContext(), appliedFilters, Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
