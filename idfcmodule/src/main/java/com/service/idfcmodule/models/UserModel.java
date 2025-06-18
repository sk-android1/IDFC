package com.service.idfcmodule.models;


import java.io.Serializable;

public class UserModel implements Serializable {
    String name, username, phone, assignedStatus, pannumber, email;
    String status, adminApproval, active;

    boolean isSwitchChecked;

    public UserModel() {
    }

    public UserModel(String name, String username, String phone, String pannumber,String email, String assignedStatus) {
        this.name = name;
        this.username = username;
        this.phone = phone;
        this.pannumber = pannumber;
        this.email = email;
        this.assignedStatus = assignedStatus;

    }

    public UserModel(String name, String username, String phone, String pannumber, String email) {
        this.name = name;
        this.username = username;
        this.phone = phone;
        this.pannumber = pannumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAssignedStatus() {
        return assignedStatus;
    }

    public void setAssignedStatus(String assignedStatus) {
        this.assignedStatus = assignedStatus;
    }

    public String getPannumber() {
        return pannumber;
    }

    public void setPannumber(String pannumber) {
        this.pannumber = pannumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminApproval() {
        return adminApproval;
    }

    public void setAdminApproval(String adminApproval) {
        this.adminApproval = adminApproval;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public boolean isSwitchChecked() {
        return isSwitchChecked;
    }

    public void setSwitchChecked(boolean switchChecked) {
        isSwitchChecked = switchChecked;
    }
}
