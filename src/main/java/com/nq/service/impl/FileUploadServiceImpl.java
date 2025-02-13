package com.nq.service.impl;

import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.service.IFileUploadService;
import com.nq.utils.FTPUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import com.nq.utils.Md5Utils;
import com.nq.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service("iFileUploadService")

public class FileUploadServiceImpl

        implements IFileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);


    public ServerResponse upload(MultipartFile file, String path) {

        String fileName = file.getOriginalFilename();


        String fileExtentionName = fileName.substring(fileName.lastIndexOf(".") + 1);


        String uploadFileName = UUID.randomUUID() + "." + fileExtentionName;


        File fileDir = new File(path);


        if (!fileDir.exists()) {


            fileDir.setWritable(true);

            fileDir.mkdirs();

        }


        File tartgetFile = new File(path, uploadFileName);

        boolean result = false;

        try {

            file.transferTo(tartgetFile);


            result = FTPUtil.uploadFile(Lists.newArrayList(new File[]{tartgetFile}));


            tartgetFile.delete();

        } catch (Exception e) {

            log.error("上传文件异常 , 错误信息 = {}", e);

            return null;

        }


        if (result) {

            return ServerResponse.createBySuccess(tartgetFile.getName());

        }

        return ServerResponse.createByErrorMsg("上传失败");

    }


    public String downloadImages(String imgurl) {
        String imageUrl = imgurl;
        String folderPath = "temp/images/";
        String fileName = null;
        try {
            fileName = Md5Utils.getMD5(imgurl)  + ".png";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        File folder = new File(folderPath);

        String destinationFile = folderPath + fileName;
        // 检查文件夹是否存在，如果不存在则创建
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(destinationFile);

        if (file.exists()) {
           return PropertiesUtil.getProperty("ftp.server.http.prefix") + fileName;
        }
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                OutputStream outputStream = new FileOutputStream(destinationFile);
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                //上传ftp服务器
                boolean b = FTPUtil.uploadFile(Lists.newArrayList(new File[]{file}));
                if (b){
                    return PropertiesUtil.getProperty("ftp.server.http.prefix") + fileName;
                }
            }
            httpConn.disconnect();
        } catch (IOException e) {
            return null;
        }
        return null;
    }

}
