package com.nq.utils.smsUtil;

import com.nq.controller.SmsApiController;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.pay.CmcPayOuterRequestUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class smsUtil {
    private static final Logger log = LoggerFactory.getLogger(SmsApiController.class);

    public String sendSMS(String telephone) {
        String code = RandomStringUtils.randomNumeric(4);
        CmcPayOuterRequestUtil requestUtil = new CmcPayOuterRequestUtil();

        String sign = "【短信签名】";
        String uid = PropertiesUtil.getProperty("dxb.sms.USERNAME");
        String key = PropertiesUtil.getProperty("dxb.sms.PASSWORD");
//        String coding = PropertiesUtil.getProperty("wj.sms.coding");
        String smscontent = sign+"您正在申请手机注册，验证码为：" + code + "，5分钟内有效！";
        //设置签名


        try {
            uid = URLEncoder.encode(uid,"UTF-8");
            smscontent = URLEncoder.encode(smscontent,"UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
//        String url = "http://"+ cod+ing +".api.smschinese.cn/?Uid="+ uid +"&Key="+ key +"&smsMob=" + telephone + "&smsText="+sign+smscontent;
        String url ="https://api.smsbao.com/sms?u="+uid+"&p="+key+"&m="+telephone+"&c="+smscontent;
        log.info("smsurl"+url);
        String result = requestUtil.sendGet(url);
        log.info("smsresult="+result+"==code="+code);
        if (Integer.valueOf(result) < 0) {
            return "";
        } else {
            String keys = "AliyunSmsCode:" + telephone;
            RedisShardedPoolUtils.setEx(keys, code, 5400);
            return code;
        }
    }


    /**
     * 找回密码
     */
    public String sendForgetSms(String telephone) {
        String code = RandomStringUtils.randomNumeric(4);
        CmcPayOuterRequestUtil requestUtil = new CmcPayOuterRequestUtil();

        String sign = "【老虎证券】";
        String uid = PropertiesUtil.getProperty("dxb.sms.USERNAME");
        String key = PropertiesUtil.getProperty("dxb.sms.PASSWORD");
//        String coding = PropertiesUtil.getProperty("wj.sms.coding");
        String smscontent = sign+"您正在申請找回密码，验证码为：" + code + "，5分钟内有效！";



        try {
            uid = URLEncoder.encode(uid,"UTF-8");
            smscontent = URLEncoder.encode(smscontent,"UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
//        String url = "http://"+ cod+ing +".api.smschinese.cn/?Uid="+ uid +"&Key="+ key +"&smsMob=" + telephone + "&smsText="+sign+smscontent;
        String url ="https://api.smsbao.com/sms?u="+uid+"&p="+key+"&m="+telephone+"&c="+smscontent;
        log.info("smsurl"+url);
        String result = requestUtil.sendGet(url);
        log.info("smsresult="+result+"==code="+code);
        if (Integer.valueOf(result) < 0) {
            return "";
        } else {
            String keys = "AliyunSmsCode:" + telephone;
            RedisShardedPoolUtils.setEx(keys, code, 5400);
            return code;
        }
    }
}
