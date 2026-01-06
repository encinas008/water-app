package com.example.waterapp.network

import retrofit2.http.*
import java.util.*

interface MeetingsApi {
    @GET("meetings")
    suspend fun getMeetings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("date") date: String? = null
    ): PageResponse<MeetingOutputDto>

    @GET("meetings/{meetingId}/attendance/date/{date}")
    suspend fun getMeetingAttendance(
        @Path("meetingId") meetingId: UUID,
        @Path("date") date: String
    ): List<MeetingAttendanceOutputDto>

    @POST("meetings/{meetingId}/attendance/bulk")
    suspend fun recordBulkAttendance(
        @Path("meetingId") meetingId: UUID,
        @Body attendance: BulkMeetingAttendanceInputDto
    ): List<MeetingAttendanceOutputDto>

    @GET("meetings/scheduled-dates")
    suspend fun getScheduledDates(): List<String>
}
