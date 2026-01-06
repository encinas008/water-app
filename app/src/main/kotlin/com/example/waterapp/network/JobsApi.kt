package com.example.waterapp.network

import retrofit2.http.*
import java.util.*

interface JobsApi {
    @GET("jobs")
    suspend fun getJobs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("date") date: String? = null
    ): PageResponse<JobOutputDto>

    @GET("jobs/{jobId}/attendance/date/{date}")
    suspend fun getJobAttendance(
        @Path("jobId") jobId: UUID,
        @Path("date") date: String
    ): List<JobAttendanceOutputDto>

    @POST("jobs/{jobId}/attendance/bulk")
    suspend fun recordBulkAttendance(
        @Path("jobId") jobId: UUID,
        @Body attendance: BulkJobAttendanceInputDto
    ): List<JobAttendanceOutputDto>

    @GET("jobs/scheduled-dates")
    suspend fun getScheduledDates(): List<String>
}
