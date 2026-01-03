package com.example.nike_fe.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.UserSpinManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserSpinAdapter extends RecyclerView.Adapter<UserSpinAdapter.UserSpinViewHolder> implements Filterable {

    private List<UserSpinManagement> originalList;
    private List<UserSpinManagement> filteredList;
    private final Consumer<UserSpinManagement> onResetClick;
    private final Consumer<UserSpinManagement> onAddClick;

    public UserSpinAdapter(List<UserSpinManagement> userList, Consumer<UserSpinManagement> onResetClick,
            Consumer<UserSpinManagement> onAddClick) {
        this.originalList = userList != null ? userList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.onResetClick = onResetClick;
        this.onAddClick = onAddClick;
    }

    public void setData(List<UserSpinManagement> userList) {
        this.originalList = userList != null ? userList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserSpinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_spin_management, parent, false);
        return new UserSpinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSpinViewHolder holder, int position) {
        UserSpinManagement user = filteredList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredList = new ArrayList<>(originalList);
                } else {
                    List<UserSpinManagement> filtered = new ArrayList<>();
                    for (UserSpinManagement row : originalList) {
                        if (row.getUsername().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getEmail().toLowerCase().contains(charString.toLowerCase())) {
                            filtered.add(row);
                        }
                    }
                    filteredList = filtered;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<UserSpinManagement>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class UserSpinViewHolder extends RecyclerView.ViewHolder {

        TextView tvAvatar, tvUsername, tvEmail, tvSpinsToday, tvTotalSpins, tvPrizesWon;
        Button btnReset, btnAddSpin;

        public UserSpinViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvSpinsToday = itemView.findViewById(R.id.tvSpinsToday);
            tvTotalSpins = itemView.findViewById(R.id.tvTotalSpins);
            tvPrizesWon = itemView.findViewById(R.id.tvPrizesWon);
            btnReset = itemView.findViewById(R.id.btnReset);
            btnAddSpin = itemView.findViewById(R.id.btnAddSpin);
        }

        public void bind(UserSpinManagement user) {
            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());

            String initial = user.getUsername().isEmpty() ? "?"
                    : String.valueOf(user.getUsername().charAt(0)).toUpperCase();
            tvAvatar.setText(initial);

            tvSpinsToday.setText(String.valueOf(user.getSpinsToday()));
            tvTotalSpins.setText(String.valueOf(user.getTotalSpins()));
            tvPrizesWon.setText(String.valueOf(user.getPrizesWon()));

            btnReset.setOnClickListener(v -> onResetClick.accept(user));
            btnAddSpin.setOnClickListener(v -> onAddClick.accept(user));
        }
    }
}
