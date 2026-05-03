package com.example.lottery.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DBUtil {
    public static Timestamp getTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    public static LocalDateTime getLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
