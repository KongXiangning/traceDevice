package com.kongxn.gps.webclient;

import com.kongxn.gps.entity.AccountEntity;

public interface ServiceRequestManager {


    <T extends AbstractWebClient> T getWebClient(AccountEntity accountEntity, Class<T> clz) throws Exception;
}
