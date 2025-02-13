package com.nq.common.interceptor;


import com.google.common.collect.Maps;
import com.nq.pojo.SiteAdmin;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.ip.IpUtils;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

@Component
public class ApiAdminAuthorityInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ApiAdminAuthorityInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {

//        //表示接受任意域名的请求,也可以指定域名
//        httpServletResponse.setHeader("Access-Control-Allow-Origin",httpServletRequest.getHeader("origin"));
//        //该字段可选，是个布尔值，表示是否可以携带cookie
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
//        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT,PATCH, DELETE, OPTIONS");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "admintoken");
        if (HttpMethod.OPTIONS.toString().equals(httpServletRequest.getMethod())){
            return true;
        }

        SiteAdmin siteAdmin = null;
        String loginToken = httpServletRequest.getHeader(PropertiesUtil.getProperty("admin.cookie.name"));
//        log.info("loginToken:{}",loginToken);
        if (StringUtils.isNotEmpty(loginToken)) {
            String adminJsonStr = RedisShardedPoolUtils.get(loginToken);

            if (adminJsonStr==null || "".equals(adminJsonStr)) {
                httpServletResponse.setCharacterEncoding("UTF-8");
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                PrintWriter writer = httpServletResponse.getWriter();
                Map map = Maps.newHashMap();
                map.put("success", Boolean.valueOf(false));
                map.put("msg", "請先登錄，無權限訪問admin");
                writer.print(JsonUtil.obj2String(map));
                writer.flush();
                writer.close();
                return false;
            }else{
                siteAdmin = (SiteAdmin) JsonUtil.string2Obj(adminJsonStr, SiteAdmin.class);
            }
        }
        if (null == siteAdmin) {
//            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = httpServletResponse.getWriter();
            Map map = Maps.newHashMap();
            map.put("success", Boolean.valueOf(false));
            map.put("msg", "請先登錄，無權限訪問admin");
            writer.print(JsonUtil.obj2String(map));
            writer.flush();
            writer.close();
            return false;
        }

//        194.26.73.150, 172.70.34.195
//        String ip = IpUtils.getIp(httpServletRequest);
//        String[] split = ip.split(", ");
//        for (String s : split) {
//            if (s.equals("118.140.35.45")){
//                return true;
//            }
//            if (s.equals("118.140.35.50")){
//                return true;
//            }
//            if (s.equals("58.152.85.239")){
//                return true;
//            }
//            if (s.equals("66.249.77.78")){
//                return true;
//            }
//            if (s.equals("220.133.13.177")){
//                return true;
//            }
//            if (s.equals("111.241.195.2")){
//                return true;
//            }
//            if (s.equals("194.26.73.150")){
//                return true;
//            }
//            if (s.equals("160.16.103.240")){
//                return true;
//            }
//            if (s.equals("194.26.73.150")){
//                return true;
//            }
//            if (s.equals("101.24.91.83")){
//                return true;
//            }  if (s.equals("111.90.140.138")){
//                return true;
//            }  if (s.equals("103.233.2.196")){
//                return true;
//            }
//        }

//        httpServletResponse.setCharacterEncoding("UTF-8");
//        httpServletResponse.setContentType("application/json;charset=UTF-8");
//        PrintWriter writer = httpServletResponse.getWriter();
//        Map map = Maps.newHashMap();
//        map.put("success", Boolean.valueOf(false));
//        map.put("msg", "請先登錄，無權限訪問admin");
//        writer.print(JsonUtil.obj2String(map));
//        writer.flush();
//        writer.close();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, ModelAndView modelAndView) throws Exception {
    }
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception e) throws Exception {
    }
}
