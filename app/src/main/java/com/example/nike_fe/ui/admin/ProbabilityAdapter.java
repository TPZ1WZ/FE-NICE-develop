package com.example.nike_fe.ui.admin;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Prize;

import java.util.ArrayList;
import java.util.List;

public class ProbabilityAdapter extends RecyclerView.Adapter<ProbabilityAdapter.ViewHolder> {

    private List<Prize> prizes;
    private final Runnable onProbabilityChanged;

    // To prevent infinite loops when updating Views programmatically
    private boolean isBinding = false;

    public ProbabilityAdapter(List<Prize> prizes, Runnable onProbabilityChanged) {
        this.prizes = prizes != null ? prizes : new ArrayList<>();
        this.onProbabilityChanged = onProbabilityChanged;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes != null ? prizes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Prize> getPrizes() {
        return prizes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_probability, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        isBinding = true;
        holder.bind(prizes.get(position));
        isBinding = false;
    }

    @Override
    public int getItemCount() {
        return prizes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        EditText etProbability;
        SeekBar seekBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrizeName);
            etProbability = itemView.findViewById(R.id.etProbability);
            seekBar = itemView.findViewById(R.id.seekBar);

            // Listener for SeekBar
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && !isBinding) {
                        Prize prize = prizes.get(getAdapterPosition());
                        prize.setProbability(progress / 100.0);
                        etProbability.setText(String.valueOf(progress));
                        onProbabilityChanged.run();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            // Listener for EditText
            etProbability.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!isBinding && s.length() > 0) {
                        try {
                            double val = Double.parseDouble(s.toString());
                            if (val > 100)
                                val = 100; // Cap at 100

                            Prize prize = prizes.get(getAdapterPosition());
                            prize.setProbability(val / 100.0);

                            // Don't update EditText here to avoid recursion loop or cursor jump
                            // But update Seekbar
                            seekBar.setProgress((int) val);

                            onProbabilityChanged.run();
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            });
        }

        public void bind(Prize prize) {
            tvName.setText(prize.getName());

            double prob = prize.getProbability() != null ? prize.getProbability() : 0.0;
            int percentage = (int) Math.round(prob * 100);

            etProbability.setText(String.valueOf(percentage));
            seekBar.setProgress(percentage);
        }
    }
}
