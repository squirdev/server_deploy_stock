package com.nq.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName : SymmetricCryptoUtil  //类名
 * @Description :   //描述
 * @Author :  tf //作者
 * @Date: 2022/10/23  23:31
 */

    public class SymmetricCryptoUtil {
        /**
         * 16字节
         */
        private static final String ENCODE_KEY = PropertiesUtil.getProperty("user.password");
        private static final String IV_KEY = "0000000000000000";

        public static void main(String[] args) {
            String encryptData = encryptFromString("zdm321123.", Mode.CBC, Padding.ZeroPadding);
            System.out.println("加密：" + encryptData);
            String decryptData = decryptFromString(encryptData, Mode.CBC, Padding.ZeroPadding);
            System.out.println("解密：" + decryptData);
        }

        public static String encryptFromString(String data, Mode mode, Padding padding) {
            AES aes;
            if (Mode.CBC == mode) {
                aes = new AES(mode, padding,
                        new SecretKeySpec(ENCODE_KEY.getBytes(), "AES"),
                        new IvParameterSpec(IV_KEY.getBytes()));
            } else {
                aes = new AES(mode, padding,
                        new SecretKeySpec(ENCODE_KEY.getBytes(), "AES"));
            }
            return aes.encryptBase64(data, StandardCharsets.UTF_8);
        }

        public static String decryptFromString(String data, Mode mode, Padding padding) {
            AES aes;
            if (Mode.CBC == mode) {
                aes = new AES(mode, padding,
                        new SecretKeySpec(ENCODE_KEY.getBytes(), "AES"),
                        new IvParameterSpec(IV_KEY.getBytes()));
            } else {
                aes = new AES(mode, padding,
                        new SecretKeySpec(ENCODE_KEY.getBytes(), "AES"));
            }
            byte[] decryptDataBase64 = aes.decrypt(data);
            return new String(decryptDataBase64, StandardCharsets.UTF_8);
        }
    /**
    * @Description:  加密密码
    * @Param:  password
    * @return:  encryptFromString
    * @Author: tf
    * @Date: 2022/10/23
    */
    public static String encryptPassword(String password){
        return encryptFromString(password, Mode.CBC, Padding.ZeroPadding);
    }

    /**
    * @Description:  解密密码
    * @Param:  password
    * @return:  decryptFromString
    * @Author: tf
    * @Date: 2022/10/23
    */
    public static String decryptPassword(String password){
        return decryptFromString(password, Mode.CBC, Padding.ZeroPadding);
    }
}


