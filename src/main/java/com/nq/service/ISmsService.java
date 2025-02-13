package com.nq.service;


import com.nq.common.ServerResponse;

public interface ISmsService {
  ServerResponse sendAliyunSMS(String paramString1, String paramString2);
}
