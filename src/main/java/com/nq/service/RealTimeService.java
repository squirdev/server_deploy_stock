package com.nq.service;

import com.nq.common.ServerResponse;

public interface RealTimeService {

    ServerResponse deleteRealTime();

    ServerResponse deleteFuturesRealTime();

    ServerResponse findStock(String paramString);

}
