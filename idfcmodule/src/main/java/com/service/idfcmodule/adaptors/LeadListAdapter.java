package com.service.idfcmodule.adaptors;

import static com.service.idfcmodule.IdfcMainActivity.comType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.service.idfcmodule.R;
import com.service.idfcmodule.databinding.LeadListItemBinding;
import com.service.idfcmodule.lead.CaseEnquiryFragment;
import com.service.idfcmodule.lead.CashCalculateFragment;
import com.service.idfcmodule.lead.CloseSrAcceptanceFragment;
import com.service.idfcmodule.lead.DeliveredChequeUploadFragment;
import com.service.idfcmodule.lead.DeliveredChequeUploadFragmentNew;
import com.service.idfcmodule.lead.DeliveredChequeUploadFragmentNew2;
import com.service.idfcmodule.lead.DeliveredDocumentUploadFragment;
import com.service.idfcmodule.lead.DeliveredDocumentUploadFragmentNew;
import com.service.idfcmodule.lead.DeliveryBranchListFragment;
import com.service.idfcmodule.lead.DeliveryTrackingFragment;
import com.service.idfcmodule.models.LeadModel;
import com.service.idfcmodule.myinterface.LeadItemClicked;
import com.service.idfcmodule.utils.ConverterUtils;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.ReplaceFragmentUtils;

import java.util.ArrayList;

public class LeadListAdapter extends RecyclerView.Adapter<LeadListAdapter.MyViewHolder> {

    ArrayList<LeadModel> arrayList;
    Context context;
    Activity activity;
    LeadItemClicked recyclerViewItemClicked;

    Integer selectedItemPosition = null;

    public void MyInterface(LeadItemClicked recyclerViewItemClicked) {
        this.recyclerViewItemClicked = recyclerViewItemClicked;
    }

    public LeadListAdapter(ArrayList<LeadModel> arrayList, Context context, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LeadListItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String leadId = arrayList.get(position).getLeadId();
        String jobId = arrayList.get(position).getJobId();
        String srNo = arrayList.get(position).getSrNo();
        String jobType = arrayList.get(position).getJobType();
        String jobSubType = arrayList.get(position).getJobSubType();
        String address = arrayList.get(position).getPickUpAddress();
        String date = arrayList.get(position).getDate();
        String timeFrom = arrayList.get(position).getTimeFrom();
        String timeTo = arrayList.get(position).getTimeTo();
        String count = arrayList.get(position).getCount();
        String status = arrayList.get(position).getStatus();
        String amount = arrayList.get(position).getAmount();
        String documentType = arrayList.get(position).getDocumentType();
        String stage = arrayList.get(position).getStage();
        String mobile = arrayList.get(position).getMobile();
        String branchMobile = arrayList.get(position).getBranchMobile();
        String branchName = arrayList.get(position).getBranchName();
        String distance = arrayList.get(position).getDistance();
        String ifscCode = arrayList.get(position).getIfscCode();
        String delivery = arrayList.get(position).getDelivery();
        String dropAddress = arrayList.get(position).getDropAddress();

        String leadStatus = arrayList.get(position).getLeadStatus();               //TODO  use this for bypass click event on completed lead

        String lat = arrayList.get(position).getLat();
        String longi = arrayList.get(position).getLongi();

        String currentDate = arrayList.get(position).getCurrentDate();

//        if (currentDate.equalsIgnoreCase(date)) {                        // for restrict allocate lead
//            holder.binding.imgDropdown.setVisibility(View.VISIBLE);
//        }
//        else {
//            holder.binding.imgDropdown.setVisibility(View.GONE);
//        }

        if (comType.equalsIgnoreCase("Vidcom")) {
            holder.binding.imgDropdown.setImageResource(R.drawable.dropdown2);
            holder.binding.imgDropup.setImageResource(R.drawable.dropup2);
        }

        if (status.equalsIgnoreCase("0")) {
            holder.binding.acceptDeclineLy.setVisibility(View.VISIBLE);
            holder.binding.tvStatus.setText("Pending");
        } else if (status.equalsIgnoreCase("1")) {
            holder.binding.tvProceed.setVisibility(View.VISIBLE);
         //   holder.binding.tvCancelReq.setVisibility(View.VISIBLE);
            holder.binding.tvStatus.setText("Allocated");
            if (!stage.equalsIgnoreCase("0")) {
                holder.binding.imgDropdown.setVisibility(View.GONE);
            }
        } else {
            holder.binding.acceptDeclineLy.setVisibility(View.GONE);
            holder.binding.tvProceed.setVisibility(View.GONE);
            holder.binding.tvStatus.setText("Completed");
            holder.binding.tvStatus.setBackgroundResource(R.drawable.rounded_green_back);
            holder.binding.imgDropdown.setVisibility(View.GONE);
        }

        if (jobSubType.equalsIgnoreCase("cash")) {
            holder.binding.textDocumentType.setText("Amount");
            holder.binding.tvCount.setText("₹ " + amount);
        } else if (jobSubType.equalsIgnoreCase("cheque")) {
            holder.binding.textDocumentType.setText("No. of count");
            holder.binding.tvCount.setText(count);
        } else {
            holder.binding.textDocumentType.setText("Document Type");
            holder.binding.tvCount.setText(documentType);
        }

        String[] convertedDateArray = date.split(" ");
      //  String convertedDate = convertedDateArray[1] + " " + convertedDateArray[0];
        String convertedDate = convertedDateArray[0]+" " + convertedDateArray[1]+" " + convertedDateArray[2];

        holder.binding.tvJobId.setText("SR - "+srNo);
        holder.binding.tvJobId2.setText("SR - "+srNo);
        holder.binding.tvJobType.setText(ConverterUtils.capitaliseString(jobSubType) + " " + ConverterUtils.capitaliseString(jobType));
        holder.binding.tvJobType2.setText(ConverterUtils.capitaliseString(jobSubType) + " " + ConverterUtils.capitaliseString(jobType));
        holder.binding.tvAddress.setText(address);
        holder.binding.tvAddress2.setText(address);
        holder.binding.tvDateTime.setText(convertedDate + "\n" + timeFrom + " to " + timeTo);
        holder.binding.tvDateTime2.setText(convertedDate + "\n" + timeFrom + " to " + timeTo);
        holder.binding.tvDateTime3.setText( timeFrom + " to " + timeTo );
        holder.binding.tvBranch.setText(branchName+"\n"+distance);

        if (selectedItemPosition != null && selectedItemPosition == position) {
            holder.binding.layout2.setVisibility(View.VISIBLE);
            holder.binding.layout1.setVisibility(View.GONE);
        } else {
            holder.binding.layout2.setVisibility(View.GONE);
            holder.binding.layout1.setVisibility(View.VISIBLE);
        }

        holder.binding.layout1.setOnClickListener(v -> {

            if (stage.equalsIgnoreCase("0")) {
                Integer previousItemPosition = selectedItemPosition;
                selectedItemPosition = position;
                if (previousItemPosition != null) {
                    notifyItemChanged(previousItemPosition);                          //  working as usual
                }
                notifyItemChanged(position);

                holder.binding.layout2.setVisibility(View.VISIBLE);
                holder.binding.layout1.setVisibility(View.GONE);
            } else if (stage.equalsIgnoreCase("1")) {                              // direct  go to pickup tracking fragment
                recyclerViewItemClicked.onItemClicked("proceedToJourney", leadId, stage, amount, count);
            } else if (stage.equalsIgnoreCase("2")) {

                if (jobSubType.equalsIgnoreCase("cheque")) {
                    Bundle bundle = new Bundle();
                    bundle.putString(MyConstantKey.COUNT, count);
                    bundle.putString(MyConstantKey.AMOUNT, amount);
                    bundle.putString(MyConstantKey.JOB_ID, jobId);
                    bundle.putString(MyConstantKey.SR_NO, srNo);
                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                    bundle.putString(MyConstantKey.REATTEMPT, "0");
                //    ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragmentNew(), bundle, (AppCompatActivity) activity);  // direct  go to Delivered Document Upload Fragment
                 //   ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragment(), bundle, (AppCompatActivity) activity);  // direct  go to Delivered Document Upload Fragment
                    ReplaceFragmentUtils.replaceFragment(new DeliveredChequeUploadFragmentNew2(), bundle, (AppCompatActivity) activity);  // direct  go to Delivered Document Upload Fragment

                } else if (jobSubType.equalsIgnoreCase("cash")) {
                    Bundle bundle = new Bundle();
                    bundle.putString(MyConstantKey.AMOUNT, amount);
                    bundle.putString(MyConstantKey.JOB_ID, jobId);
                    bundle.putString(MyConstantKey.SR_NO, srNo);
                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                    bundle.putString(MyConstantKey.REATTEMPT, "0");
                    ReplaceFragmentUtils.replaceFragment(new CashCalculateFragment(), bundle, (AppCompatActivity) activity);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(MyConstantKey.COUNT, count);
                    bundle.putString(MyConstantKey.JOB_ID, jobId);
                    bundle.putString(MyConstantKey.SR_NO, srNo);
                    bundle.putString(MyConstantKey.LEAD_ID, leadId);
                    bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                    bundle.putString(MyConstantKey.REATTEMPT, "0");
                    ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragment(), bundle, (AppCompatActivity) activity);  // direct  go to Delivered Document Upload Fragment
                 //   ReplaceFragmentUtils.replaceFragment(new DeliveredDocumentUploadFragmentNew(), bundle, (AppCompatActivity) activity);  // direct  go to Delivered Document Upload Fragment

                 //   Toast.makeText(context, "it is not job subtype cheque or cash", Toast.LENGTH_SHORT).show();
                }

            }else if (stage.equalsIgnoreCase("3")){
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.COUNT, count);
                bundle.putString(MyConstantKey.AMOUNT, amount);
                bundle.putString(MyConstantKey.JOB_ID, jobId);
                bundle.putString(MyConstantKey.SR_NO, srNo);
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                ReplaceFragmentUtils.replaceFragment(new CaseEnquiryFragment(), bundle, (AppCompatActivity) activity);  // direct  go to Delivered Document Upload Fragment

            } else if (stage.equalsIgnoreCase("4")) {
                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.SR_NO, srNo);
                ReplaceFragmentUtils.replaceFragment(new DeliveryBranchListFragment(), bundle, (AppCompatActivity) activity);
            }
            else if (stage.equalsIgnoreCase("5")) {

                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.MOBILE_NO, branchMobile);
                bundle.putString(MyConstantKey.BRANCH_NAME, branchName);
                bundle.putString(MyConstantKey.IFSC_CODE, ifscCode);
                bundle.putString(MyConstantKey.JOB_TYPE, jobType);
                bundle.putString(MyConstantKey.JOB_SUBTYPE, jobSubType);
                bundle.putString(MyConstantKey.DELIVERY, delivery);
                bundle.putString(MyConstantKey.DROP_ADDRESS, dropAddress);

                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.SR_NO, srNo);
                ReplaceFragmentUtils.replaceFragment(new DeliveryTrackingFragment(), bundle, (AppCompatActivity) activity);
            }
            else if (stage.equalsIgnoreCase("6")) {

                Bundle bundle = new Bundle();
                bundle.putString(MyConstantKey.LEAD_ID, leadId);
                bundle.putString(MyConstantKey.SR_NO, srNo);

                ReplaceFragmentUtils.replaceFragment(new CloseSrAcceptanceFragment(), bundle, (AppCompatActivity) activity);
            }

        });

        holder.binding.dropUpLayout.setOnClickListener(v -> {
            holder.binding.layout2.setVisibility(View.GONE);
            holder.binding.layout1.setVisibility(View.VISIBLE);
        });

        holder.binding.tvAccept.setOnClickListener(v -> {
            showConfirmDialog(leadId, "", "", "", "accept");
        });

        holder.binding.tvDecline.setOnClickListener(v -> {
            holder.binding.layout2.setVisibility(View.GONE);
            holder.binding.layout1.setVisibility(View.VISIBLE);
        });

        holder.binding.tvProceed.setOnClickListener(v -> {
            //   recyclerViewItemClicked.onItemClicked("proceedToJourney", leadId, stage, amount, count);
            //    showConfirmDialog(leadId, stage, amount, count,"proceedToJourney");

            try {
                String leadDate = date + " " + timeFrom;
                String dateFormat = "dd MMM yyyy hh:mm a";

                long allocateTime = ConverterUtils.dateToMilliSeconds(leadDate, dateFormat);
                long currentTime = System.currentTimeMillis();

                //   if (currentTime >= allocateTime){
                showConfirmDialog(leadId, stage, amount, count, "proceedToJourney");
//                }
//                else {
//                    String waitingTime = ConverterUtils.waitingTime(leadDate, dateFormat);
//                    showWaitingDialog(waitingTime );
//                }

            } catch (Exception ignored) {
            }

        });

        holder.binding.tvCancelReq.setOnClickListener(v -> {
            recyclerViewItemClicked.onItemClicked("cancelRequest",leadId,"","","");
        });

    }

    private void showWaitingDialog(String waitingTime) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.estimate_time_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_back);

        TextView tvWaiting = convertView.findViewById(R.id.tvWaitingTime);

        tvWaiting.setText(waitingTime);

        alertDialog.setView(convertView);

        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showConfirmDialog(String leadId, String stage, String amount, String count, String whichButtonClicked) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.lead_confirm_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_back);

        TextView textHeading = convertView.findViewById(R.id.textHeading);
        TextView textMessage = convertView.findViewById(R.id.textMessage);
        TextView tvCancel = convertView.findViewById(R.id.tvCancel);

        TextView tvYes = convertView.findViewById(R.id.tvYes);

        if (whichButtonClicked.equalsIgnoreCase("accept")) {

            textHeading.setText("Are you sure to accept the lead");
            textMessage.setText("Are you sure that you want to accept this lead ");
        }
        tvCancel.setOnClickListener(v -> alertDialog.dismiss());
        tvYes.setOnClickListener(v -> {
            alertDialog.dismiss();

            if (whichButtonClicked.equalsIgnoreCase("accept")) {
                recyclerViewItemClicked.onItemClicked("accept", leadId, stage, amount, count);   // not need of stage , amount and count
            } else {
                recyclerViewItemClicked.onItemClicked("proceedToJourney", leadId, stage, amount, count);
            }

        });

        alertDialog.setCancelable(false);
        alertDialog.setView(convertView);

        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
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
        LeadListItemBinding binding;

        public MyViewHolder(LeadListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}