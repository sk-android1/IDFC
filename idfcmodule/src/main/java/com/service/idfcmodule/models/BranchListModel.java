package com.service.idfcmodule.models;

public class BranchListModel {
    String branchId, branchName, branchCode,branchAddress;



    public BranchListModel(String branchId,String branchName, String branchCode, String branchAddress) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchCode = branchCode;
        this.branchAddress = branchAddress;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }
}
