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

    // Static variables to preserve state across dialog reopens
    private static float savedMinPrice = 0f;
    private static float savedMaxPrice = 10000000f;
    private static java.util.List<String> savedSelectedSizes = new java.util.ArrayList<>();
    private static int savedSortOption = -1;

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
        restoreSavedState();
    }

    // Interface for callback
    public interface OnApplyFilterListener {
        void onApplyFilter(float minPrice, float maxPrice, int sortOption, java.util.List<String> selectedSizes);
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

            // Reset static saved state
            savedMinPrice = 0f;
            savedMaxPrice = 10000000f;
            savedSelectedSizes.clear();
            savedSortOption = -1;
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

            // Collect Selected Sizes
            java.util.List<String> selectedSizes = new java.util.ArrayList<>();
            if (cgSize != null) {
                for (int i = 0; i < cgSize.getChildCount(); i++) {
                    View child = cgSize.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (chip.isChecked()) {
                            selectedSizes.add(chip.getText().toString());
                        }
                    }
                }
            }

            // Collect Sort Option
            int sortOption = -1; // -1 means no sort selected
            if (cgSort != null) {
                int checkedId = cgSort.getCheckedChipId();
                if (checkedId != View.NO_ID) {
                    // Map chip position to sort option
                    for (int i = 0; i < cgSort.getChildCount(); i++) {
                        View child = cgSort.getChildAt(i);
                        if (child.getId() == checkedId) {
                            sortOption = i;
                            break;
                        }
                    }
                }
            }

            // Save state for next time
            savedMinPrice = min;
            savedMaxPrice = max;
            savedSelectedSizes = new java.util.ArrayList<>(selectedSizes);
            savedSortOption = sortOption;

            if (listener != null) {
                listener.onApplyFilter(min, max, sortOption, selectedSizes);
            }
            
            // Dismiss dialog after applying
            dismiss();
        });
    }

    private void restoreSavedState() {
        // Restore price range
        if (sliderPrice != null) {
            sliderPrice.setValues(savedMinPrice, savedMaxPrice);
            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###đ");
            if (tvMinPrice != null) {
                tvMinPrice.setText(formatter.format(savedMinPrice));
            }
            if (tvMaxPrice != null) {
                tvMaxPrice.setText(formatter.format(savedMaxPrice));
            }
        }

        // Restore selected sizes
        if (cgSize != null && !savedSelectedSizes.isEmpty()) {
            for (int i = 0; i < cgSize.getChildCount(); i++) {
                View child = cgSize.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (savedSelectedSizes.contains(chip.getText().toString())) {
                        chip.setChecked(true);
                    }
                }
            }
        }

        // Restore sort option
        if (cgSort != null && savedSortOption >= 0 && savedSortOption < cgSort.getChildCount()) {
            View child = cgSort.getChildAt(savedSortOption);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(true);
            }
        }
    }
}
