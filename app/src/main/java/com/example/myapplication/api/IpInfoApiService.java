package com.example.myapplication.api;

import com.example.myapplication.api.responses.IpInfoDirectResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * API Service for direct calls to ipinfo.io
 */
public interface IpInfoApiService {
    
    /**
     * Get IP location information directly from ipinfo.io
     * @return IpInfoDirectResponse with location data
     */
    @GET("json")
    Call<IpInfoDirectResponse> getIpInfo();
}

