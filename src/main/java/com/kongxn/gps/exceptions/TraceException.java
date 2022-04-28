package com.kongxn.gps.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author kxn
 */
@Data
@AllArgsConstructor
public class TraceException extends RuntimeException{

    private String platform;
    private String username;
    private String deviceId;
    private String errorCode;
    private String errorMsg;

    public static TraceException newError(String platform, String username, String deviceId, String errorCode,String errorMsg){
        return new TraceException(platform, username, deviceId, errorCode, errorMsg);
    }

    public static TraceException newError(String platform, String username, String deviceId, String errorMsg){
        return new TraceException(platform, username, deviceId, "", errorMsg);
    }
}
