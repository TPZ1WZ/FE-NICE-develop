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
    private ChipGroup cgSize, cgSort;
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
        cgSort = view.findViewById(R.id.cgSort);

        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);

        setupListeners();
    }

    // Interface for callback
    public interface OnApplyFilterListener {
        void onApplyFilter(float minPrice, float maxPrice);
    }

    private OnApplyFilterListener listener;

    public void setOnApplyFilterListener(OnApplyFilterListener listener) {
        this.listener = listener;
    }

    private void setupListeners() {
        // Set initial values programmatically to avoid XML inflation issues
        if (sliderPrice != null) {
            sliderPrice.setValues(0f, 10000000f);
            sliderPrice.setLabelFormatter(value -> {
                java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###đ");
                return formatter.format(value);
            });
            sliderPrice.addOnChangeListener(new RangeSlider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                    List<Float> values = slider.getValues();
                    if (values.size() == 2) {
                        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###đ");
                        tvMinPrice.setText(formatter.format(values.get(0)));
                        tvMaxPrice.setText(formatter.format(values.get(1)));
                    }
                }
            });
        }

        btnReset.setOnClickListener(v -> {
            if (cgSize != null)
                cgSize.clearCheck();
            if (cgSort != null)
                cgSort.clearCheck();
            if (sliderPrice != null)
                sliderPrice.setValues(0f, 10000000f);

            // Optional: Auto-apply reset or just wait for Apply click?
            // Usually wait for Apply.
        });

        btnApply.setOnClickListener(v -> {
            // Collect Price
            float min = 0f;
            float max = 10000000f;

            if (sliderPrice != null) {
                List<Float> values = sliderPrice.getValues();
                if (values.size() == 2) {
                    min = values.get(0);
                    max = values.get(1);
                }
            }

            if (listener != null) {
                listener.onApplyFilter(min, max);
            }
            dismiss();
        });
    }
}
