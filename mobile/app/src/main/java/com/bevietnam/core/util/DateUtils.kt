package com.bevietnam.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bộ công cụ tiện ích xử lý định dạng ngày tháng trong ứng dụng BeVietnam.
 */
object DateUtils {
    
    private const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm"
    
    /**
     * Định dạng đối tượng [Date] thành một chuỗi ngày tháng dễ đọc theo định dạng chuẩn Việt Nam.
     *
     * @param date Đối tượng ngày cần định dạng.
     * @param format Chuỗi mẫu định dạng (Mặc định là "dd/MM/yyyy HH:mm").
     * @return Chuỗi ngày tháng đã được định dạng.
     */
    fun formatDate(date: Date, format: String = DEFAULT_DATE_FORMAT): String {
        return try {
            val sdf = SimpleDateFormat(format, Locale("vi", "VN"))
            sdf.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}
