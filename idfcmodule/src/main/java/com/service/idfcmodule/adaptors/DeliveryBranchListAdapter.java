package com.service.idfcmodule.adaptors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.service.idfcmodule.databinding.BranchListItemBinding;
import com.service.idfcmodule.lead.CashCalculateFragment;
import com.service.idfcmodule.lead.DeliveryTrackingFragment;
import com.service.idfcmodule.models.BranchListModel;
import com.service.idfcmodule.myinterface.BranchItemClicked;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;

import java.util.ArrayList;

public class DeliveryBranchListAdapter extends RecyclerView.Adapter<DeliveryBranchListAdapter.MyViewHolder> {

    ArrayList<BranchListModel> arrayList;
    Context context;
    Activity activity;

    BranchItemClicked branchItemClicked;

    public void branchListInterface(BranchItemClicked branchItemClicked) {
        this.branchItemClicked = branchItemClicked;
    }

    public DeliveryBranchListAdapter(ArrayList<BranchListModel> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(BranchListItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String branchId = arrayList.get(position).getBranchId();
        String branchName = arrayList.get(position).getBranchName();
        String branchCode = arrayList.get(position).getBranchCode();
        String branchAddress = arrayList.get(position).getBranchAddress();

        holder.binding.tvBranchName.setText(branchName);
        holder.binding.tvBranchCode.setText(branchCode);
        holder.binding.tvBranchAddress.setText(branchAddress);

        holder.binding.tvSelect.setOnClickListener(v -> {

            branchItemClicked.onItemClicked(branchId, branchName);

        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        BranchListItemBinding binding;

        public MyViewHolder(BranchListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
