package com.bevietnam.core.data.remote.api

import com.bevietnam.core.data.remote.api.dto.HealthResponseDto
import com.bevietnam.core.data.remote.api.dto.StorylineNextTaskResponseDto
import retrofit2.http.GET

/**
 * Interface Retrofit chứa các khai báo gọi API backend.
 */
interface BeVietnamApi {

    /**
     * Endpoint kiểm tra trạng thái hoạt động của Backend.
     */
    @GET("health")
    suspend fun checkHealth(): HealthResponseDto

    /**
     * Lấy danh sách địa điểm khám phá.
     */
    @GET("places")
    suspend fun getPlaces(
        @retrofit2.http.Query("category") category: String? = null,
        @retrofit2.http.Query("limit") limit: Int = 10,
        @retrofit2.http.Query("offset") offset: Int = 0
    ): com.bevietnam.core.data.remote.api.dto.PlacesResponseDto

    /**
     * Lấy bảng tin gợi ý.
     */
    @GET("feed")
    suspend fun getFeed(): com.bevietnam.core.data.remote.api.dto.FeedResponseDto

    /**
     * Lấy nhiệm vụ văn hóa tiếp theo cho người dùng (khớp endpoint của BE).
     */
    @GET("storyline/next-task")
    suspend fun getNextTask(
        @retrofit2.http.Query("user_id") userId: String
    ): StorylineNextTaskResponseDto
}
