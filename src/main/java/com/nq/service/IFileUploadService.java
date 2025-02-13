package com.nq.service;


import com.nq.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IFileUploadService {
  ServerResponse upload(MultipartFile paramMultipartFile, String paramString);

  String downloadImages(String url);
}