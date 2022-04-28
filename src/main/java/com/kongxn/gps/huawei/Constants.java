package com.kongxn.gps.huawei;

class Constants {

    static final String PLATFORM_NAME = "huawei";

    static final String NOTIFY = "notify";
    static final String HEARTBEAT_CHECK = "heartbeatCheck";
    static final String QUERY_LOCATE_RESULT = "queryLocateResult";

    static final String CLOUD_URL = "https://cloud.huawei.com/";
    static final String NOTIFY_URL = "https://cloud.huawei.com/notify";
    static final String QUERY_LOCATE_URL = "https://cloud.huawei.com/findDevice/queryLocateResult";
    static final String HEART_CHECK_URL = "https://cloud.huawei.com/heartbeatCheck?checkType=1&";
    static final String LOCATE_URL = "https://cloud.huawei.com/findDevice/locate";

    static final int MIN_INTERVAL = 1;
    static final int MAX_INTERVAL = 10;
}
