package com.bevietnam.core.data.remote.api

import com.bevietnam.core.data.remote.api.dto.AuthResponseDto
import com.bevietnam.core.data.remote.api.dto.FeedResponseDto
import com.bevietnam.core.data.remote.api.dto.HealthResponseDto
import com.bevietnam.core.data.remote.api.dto.LoginRequestDto
import com.bevietnam.core.data.remote.api.dto.PlacesResponseDto
import com.bevietnam.core.data.remote.api.dto.RegisterRequestDto
import com.bevietnam.core.data.remote.api.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Interface Retrofit chứa các khai báo gọi API backend.
 */
interface BeVietnamApi {

    /**
     * Endpoint kiểm tra trạng thái hoạt động của Backend.
     */
    @GET("health")
    suspend fun checkHealth(): HealthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    /**
     * Lấy thông tin người dùng đang đăng nhập (dựa vào token đính kèm).
     */
    @GET("auth/me")
    suspend fun getMe(): UserDto

    /**
     * Lấy danh sách địa điểm khám phá.
     */
    @GET("places")
    suspend fun getPlaces(
        @retrofit2.http.Query("category") category: String? = null,
        @retrofit2.http.Query("limit") limit: Int = 10,
        @retrofit2.http.Query("offset") offset: Int = 0
    ): PlacesResponseDto

    /**
     * Lấy bảng tin gợi ý.
     */
    @GET("feed")
    suspend fun getFeed(): FeedResponseDto
}
