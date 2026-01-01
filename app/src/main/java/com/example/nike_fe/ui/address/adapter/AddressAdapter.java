package com.example.nike_fe.ui.address.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<Address> addressList;
    private OnAddressClickListener listener;
    private int selectedPosition = -1;

    public interface OnAddressClickListener {
        void onAddressSelected(Address address);
        void onEditAddress(Address address);
        void onDeleteAddress(Address address);
        void onSetDefault(Address address);
    }

    public AddressAdapter(Context context, List<Address> addressList, OnAddressClickListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
        
        // Find default address position
        for (int i = 0; i < addressList.size(); i++) {
            if (addressList.get(i).isDefault()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        
        holder.tvRecipientName.setText(address.getRecipientName());
        holder.tvPhoneNumber.setText(address.getPhoneNumber());
        holder.tvFullAddress.setText(address.getFullAddress());
        
        // Show/hide default badge
        holder.tvDefaultBadge.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);
        
        // Show/hide delete button (only for non-default addresses)
        holder.tvDelete.setVisibility(address.isDefault() ? View.GONE : View.VISIBLE);
        
        // Set radio button state
        holder.rbSelect.setChecked(selectedPosition == position);
        
        // Click on entire card selects address
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });
        
        // Radio button click
        holder.rbSelect.setOnClickListener(v -> holder.itemView.performClick());
        
        // Edit button
        holder.tvEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditAddress(address);
            }
        });
        
        // Delete button
        holder.tvDelete.setOnClickListener(v -> {
            if (listener != null && !address.isDefault()) {
                listener.onDeleteAddress(address);
            }
        });
        
        // Long press to show options (set default for non-default addresses)
        holder.itemView.setOnLongClickListener(v -> {
            if (!address.isDefault() && listener != null) {
                new android.app.AlertDialog.Builder(context)
                    .setTitle("Đặt làm mặc định")
                    .setMessage("Đặt địa chỉ này làm mặc định?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> listener.onSetDefault(address))
                    .setNegativeButton("Hủy", null)
                    .show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbSelect;
        TextView tvRecipientName;
        TextView tvPhoneNumber;
        TextView tvFullAddress;
        TextView tvDefaultBadge;
        TextView tvEdit;
        TextView tvDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            rbSelect = itemView.findViewById(R.id.rbSelect);
            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvFullAddress = itemView.findViewById(R.id.tvFullAddress);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            tvEdit = itemView.findViewById(R.id.tvEdit);
            tvDelete = itemView.findViewById(R.id.tvDelete);
        }
    }
}
