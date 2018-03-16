package com.ronak.viral.adda.api;

import com.ronak.viral.adda.helper.AppConstants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by anish on 15-03-2018.
 */

public interface YouTubeService {
    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl);

    @Streaming
    @GET(AppConstants.WATCH)
    Call<ResponseBody> downloadFile(@Query("v") String v);


}
