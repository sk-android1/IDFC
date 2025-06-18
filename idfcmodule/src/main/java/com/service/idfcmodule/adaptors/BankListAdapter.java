package com.service.idfcmodule.adaptors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.service.idfcmodule.lead.LeadListFragment;
import com.service.idfcmodule.databinding.BankListItemBinding;
import com.service.idfcmodule.models.BankModel;
import com.service.idfcmodule.myinterface.BankItemClicked;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.myinterface.LeadItemClicked;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BankListAdapter extends RecyclerView.Adapter<BankListAdapter.MyViewHolder> {

    ArrayList<BankModel> bankList;
    Context context;
    Activity activity;

    BankItemClicked recyclerViewItemClicked;

    Integer selectedItemPosition = null;

    public void MyInterface(BankItemClicked recyclerViewItemClicked) {
        this.recyclerViewItemClicked = recyclerViewItemClicked;
    }

    public BankListAdapter(ArrayList<BankModel> bankList, Context context, Activity activity) {
        this.bankList = bankList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(BankListItemBinding.inflate(LayoutInflater.from(context), parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String bankName = bankList.get(position).getBankName();
        String bankId = bankList.get(position).getBankId();
        String bankLogo = bankList.get(position).getLogoUrl();

        holder.binding.tvBank.setText(bankName);
        Picasso.get().load(bankLogo).into(holder.binding.imgLogo);

//        if (selectedItemPosition != null && selectedItemPosition == position){
//            holder.binding.imgTick.setVisibility(View.VISIBLE);
//        }else {
//            holder.binding.imgTick.setVisibility(View.GONE);
//        }

        holder.binding.mainLayout.setOnClickListener(v -> {

//            Integer previousItemPosition = selectedItemPosition;
//            selectedItemPosition = position;
//            if (previousItemPosition != null){
//                notifyItemChanged(previousItemPosition);
//            }
//            notifyItemChanged(position);
//

            recyclerViewItemClicked.onItemClicked(bankId);

        });

    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        BankListItemBinding binding;

        public MyViewHolder(BankListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
