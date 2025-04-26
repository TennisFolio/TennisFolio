package com.tennisfolio.Tennisfolio.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ConversionUtil {
    private ConversionUtil(){}

    public static String timestampToYyyyMMdd(String timestamp){
        if(timestamp == null) return "";
        long seconds = Long.parseLong(timestamp);
        return Instant.ofEpochSecond(seconds)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static Long eurToUsd(Long value, String cur){
        if(!cur.equals("EUR")) return value;

        if(value == 0L || value == null) return 0L;
        Long usd = Math.round(value*0.95);
        return usd;
    }

    public static String secondToMinute(String secondsStr){

        if(secondsStr == null) return "0";

        int totalSeconds = Integer.parseInt(secondsStr);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
