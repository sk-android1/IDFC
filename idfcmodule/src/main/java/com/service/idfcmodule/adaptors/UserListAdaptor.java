package com.service.idfcmodule.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.UserListItemBinding;
import com.service.idfcmodule.models.UserModel;

import java.util.ArrayList;

public class UserListAdaptor extends RecyclerView.Adapter<UserListAdaptor.MyViewHolder> {

    ArrayList<UserModel> arrayList;
    Context context;

    public ArrayList<UserModel> selectedList;

    boolean isAssignedList;

    public UserListAdaptor(ArrayList<UserModel> arrayList, Context context, boolean isAssignedList, ArrayList<UserModel> selectedList) {
        this.arrayList = arrayList;
        this.context = context;
        this.isAssignedList = isAssignedList;

        this.selectedList = selectedList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserListItemBinding binding = UserListItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new MyViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String name = arrayList.get(position).getName();
        String userName = arrayList.get(position).getUsername();
        String phone = arrayList.get(position).getPhone();
        String assignedStatus = arrayList.get(position).getAssignedStatus();

        holder.binding.tvName.setText(name);
        holder.binding.tvUserName.setText(userName);
        holder.binding.tvPhone.setText(phone);

        if (isAssignedList) {
            holder.binding.kycLy.setVisibility(View.VISIBLE);
            holder.binding.statusLy.setVisibility(View.VISIBLE);
            holder.binding.adminApprovalLy.setVisibility(View.VISIBLE);

            holder.binding.assignedLy.setVisibility(View.GONE);

            holder.binding.kycView.setVisibility(View.VISIBLE);
            holder.binding.statusView.setVisibility(View.VISIBLE);

            String active = arrayList.get(position).getActive();
            String status = arrayList.get(position).getStatus();
            String adminApproval = arrayList.get(position).getAdminApproval();

            String convertedStatus, convertedAdminApproval, convertedActive;

            if (status.equalsIgnoreCase("1")) {
                convertedStatus = "Complete";
                holder.binding.tvKyc.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                convertedStatus = "Pending";
                holder.binding.tvKyc.setTextColor(context.getResources().getColor(R.color.yellow));
            }
            if (adminApproval.equalsIgnoreCase("1")) {
                convertedAdminApproval = "yes";
            } else {
                convertedAdminApproval = "No";
            }
            if (active.equalsIgnoreCase("1")) {
                convertedActive = "Active";
            } else {
                convertedActive = "InActive";
            }

            holder.binding.tvKyc.setText(convertedStatus);
            holder.binding.tvStatus.setText(convertedActive);
            holder.binding.tvApproval.setText(convertedAdminApproval);

        }

        boolean isChecked2 = arrayList.get(position).isSwitchChecked();
        holder.binding.swIdfc.setChecked(isChecked2);
        if (isChecked2) {
            holder.binding.swIdfc.setText(context.getResources().getString(R.string.yes));
        } else {
            if (assignedStatus != null) {
                if (assignedStatus.equalsIgnoreCase("Assigned")) {
                    holder.binding.swIdfc.setChecked(true);
                    holder.binding.swIdfc.setText("Assigned");
                    holder.binding.swIdfc.setEnabled(false);

//               holder.binding.swIdfc.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.light_grey), PorterDuff.Mode.SRC_IN);
//               holder.binding.swIdfc.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.grey), PorterDuff.Mode.SRC_IN);

                }

            } else {
                holder.binding.swIdfc.setText(context.getResources().getString(R.string.no));
            }

        }

        holder.binding.swIdfc.setOnClickListener(view -> {

            boolean isChecked = holder.binding.swIdfc.isChecked();

            boolean prevIsChecked = !isChecked;
            if (prevIsChecked) {
                holder.binding.swIdfc.setText(context.getResources().getString(R.string.no));
                arrayList.get(position).setSwitchChecked(false);

//                holder.binding.swIdfc.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.dark_grey), PorterDuff.Mode.SRC_IN);
//                holder.binding.swIdfc.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.grey), PorterDuff.Mode.SRC_IN);

                if (!selectedList.isEmpty()) {
                    selectedList.remove(arrayList.get(position));
                }

            } else {
                holder.binding.swIdfc.setText(context.getResources().getString(R.string.yes));
                selectedList.add(arrayList.get(position));

                arrayList.get(position).setSwitchChecked(true);

//                holder.binding.swIdfc.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.sky_blue), PorterDuff.Mode.SRC_IN);
//                holder.binding.swIdfc.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.light_sky_blue), PorterDuff.Mode.SRC_IN);

            }

        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        UserListItemBinding binding;

        public MyViewHolder(UserListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
