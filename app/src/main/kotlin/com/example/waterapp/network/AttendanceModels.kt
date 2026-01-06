package com.example.waterapp.network

import java.util.*

// Jobs
data class JobOutputDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val startDate: String, // Corrected from date
    val active: Boolean
)

data class JobAttendanceOutputDto(
    val id: UUID,
    val jobId: UUID,
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val attendanceDate: String,
    val present: Boolean,
    val checkInTime: String?,
    val checkOutTime: String?,
    val observation: String?
)

data class BulkJobAttendanceInputDto(
    val jobId: UUID,
    val attendanceDate: String,
    val attendances: List<JobAttendanceItemInputDto>
)

data class JobAttendanceItemInputDto(
    val partnerId: UUID,
    val present: Boolean,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val observation: String? = null
)

// Meetings
data class MeetingOutputDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val meetingDate: String,
    val active: Boolean
)

data class MeetingAttendanceOutputDto(
    val id: UUID,
    val meetingId: UUID,
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val attendanceDate: String,
    val present: Boolean,
    val checkInTime: String?,
    val checkOutTime: String?,
    val observation: String?
)

data class BulkMeetingAttendanceInputDto(
    val meetingId: UUID,
    val attendanceDate: String,
    val attendances: List<MeetingAttendanceItemInputDto>
)

data class MeetingAttendanceItemInputDto(
    val partnerId: UUID,
    val present: Boolean,
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val observation: String? = null
)


data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val number: Int
)
