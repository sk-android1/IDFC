package com.service.idfcmodule.web_services;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface WebServices {

    @FormUrlEncoded
    @POST("asm/assign-agent")
    Call<JsonObject> saveAgentByAsm(
            @Field("asm_id") String asm_id,
            @Field("bank_id") String bank_id,
            @Field("agents") String agents
            );

    @FormUrlEncoded
    @POST("asm/agent-list")
    Call<JsonObject> agentAssignedList(
            @Field("asm_id") String asm_id
    );

    @FormUrlEncoded
    @POST("retailer/verify-agent")
    Call<JsonObject> verifyAgent(
            @Field("agent_id") String agent_id,
            @Field("pannumber") String pannumber
    );

    @FormUrlEncoded
    @POST("retailer/verify-aadhaar")
    Call<JsonObject> verifyAadhaar(
            @Field("agent_id") String agent_id,
            @Field("revision") String revison,
            @Field("pincode") String pincode,
            @Field("gps_location") String gps_location
    );

    @Multipart
    @POST("retailer/photo-match")
    Call<JsonObject> uploadPhoto(
            @Part("agent_id") RequestBody agent_id,
            @Part("revision") RequestBody revison,
            @Part() MultipartBody.Part agentImage
    );

    @POST("retailer/address-proof/list")
    Call<JsonObject> getAddProofList(

    );

    @FormUrlEncoded
    @POST("retailer/remark-list")
    Call<JsonObject> getRemark(
            @Field("agent_id") String agent_id
    );

    @Multipart
    @POST("retailer/upload-document")
    Call<JsonObject> uploadDocument(
            @Part() MultipartBody.Part pv_doc,
            @Part("agent_id") RequestBody agent_id,
            @Part("address_proof") RequestBody address_proof,
            @Part("revision") RequestBody revison,
            @Part() MultipartBody.Part address_doc
    );

    @Multipart
    @POST("retailer/delivery-status")
    Call<JsonObject> uploadDamagedCard(
            @Part("agent_id") RequestBody agent_id,
            @Part("status") RequestBody revison,
            @Part() MultipartBody.Part agentImage
    );

    @POST("bankList")
    Call<JsonObject> getBankList();

    @FormUrlEncoded
    @POST("retailer/lead-list")
    Call<JsonObject> getLeadList(
            @Field("bank_id") String bank_id,
            @Field("agent_id") String agent_id,
            @Field("status") String status
    );

    @FormUrlEncoded
    @POST("retailer/allocate-lead")
    Call<JsonObject> allocatedLead(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id
    );

    @FormUrlEncoded
    @POST("retailer/start-journey")
    Call<JsonObject> startJourney(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id
    );

    @GET
    Call<JsonObject> verifyUserByUrl(@Url String url);

    @Multipart
    @POST("retailer/upload-data")
    Call<JsonObject> uploadDeliveredDocument(
            @Part("lead_id") RequestBody lead_id,
            @Part("agent_id") RequestBody agent_id,
            @Part("reattempt") RequestBody reattempt,
            @Part("count") RequestBody count,
            @Part MultipartBody.Part[] uploadImagesArr

    );

    @Multipart
    @POST("retailer/upload")
    Call<JsonObject> uploadImage(
            @Part("agent_id") RequestBody agent_id,
            @Part() MultipartBody.Part uploadImages

    );

    @Multipart
    @POST("retailer/upload-data")
    Call<JsonObject> uploadDeliveredCheque(
            @Part("lead_id") RequestBody lead_id,
            @Part("agent_id") RequestBody agent_id,
            @Part("reattempt") RequestBody reattempt,
            @Part("count") RequestBody count,
            @Part("amount") RequestBody amount,
            @Part MultipartBody.Part[] uploadImagesArr

    );

    @FormUrlEncoded
    @POST("retailer/upload-data")
    Call<JsonObject> cashCalculate(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("reattempt") String reattempt,
            @Field("data") String data
    );

    @FormUrlEncoded
    @POST("retailer/branch-list")
    Call<JsonObject> getBranchList(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id
    );

    @FormUrlEncoded
    @POST("retailer/submit-branch")
    Call<JsonObject> submitBranch(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("branch_id") String branch_id
    );

    @FormUrlEncoded
    @POST("retailer/close-sr")
    Call<JsonObject> closeSr(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id
    );

    @FormUrlEncoded
    @POST("retailer/update-lead")
    Call<JsonObject> updateCount(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("count") String count,
          //  @Field("amount") String amount,
            @Field("job_subtype") String job_subtype
    );

    @FormUrlEncoded
    @POST("retailer/update-lead")
    Call<JsonObject> updateAmount(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("amount") String amount,
            @Field("job_subtype") String job_subtype
    );

    @FormUrlEncoded
    @POST("retailer/update-lead")
    Call<JsonObject> updateDocumentCount(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("count") String count,
            @Field("job_subtype") String job_subtype
    );

    @FormUrlEncoded
    @POST("retailer/reached-location")
    Call<JsonObject> reachedLocation(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("geo_code") String geo_code,
            @Field("gps_location") String gps_location
    );

    @FormUrlEncoded
    @POST("retailer/case-enquire")
    Call<JsonObject> caseEnquiry(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id
    );

    @FormUrlEncoded
    @POST("retailer/cancel-request")
    Call<JsonObject> cancelRequest(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id,
            @Field("remark") String remark
    );

    @FormUrlEncoded
    @POST("retailer/bank-list")
    Call<JsonObject> bankList(
            @Field("agent_id") String agent_id
    );

    @FormUrlEncoded
    @POST("retailer/close-sr-enquire")
    Call<JsonObject> closeSrEnq(
            @Field("lead_id") String lead_id,
            @Field("agent_id") String agent_id
    );

}
