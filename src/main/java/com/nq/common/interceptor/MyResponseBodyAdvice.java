//package com.nq.common.interceptor;
////import com.example.demo.base.vo.Result;
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
//import com.nq.common.ServerResponse;
//import com.nq.utils.translate.GoogleTranslateUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.util.ArrayList;
//
//import static cn.hutool.core.lang.Validator.isChinese;
//
///**
//     * 全局拦截返回值
//     * @author DaenMax
//     */
//    @ControllerAdvice
//    @Slf4j
//    public class MyResponseBodyAdvice implements ResponseBodyAdvice {
//        @Override
//        public boolean supports(MethodParameter returnType, Class converterType) {
//            return true;
//        }
//
//        @Override
//        public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
//            //形如：/love/user/test2
//            String requestPath = request.getURI().getPath();
//            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//            HttpSession httpSession = httpServletRequest.getSession(true);
//            String lang = request.getHeaders().getFirst("lang");
////            if (lang != null && !lang.equals("zh-CN") && !lang.equals("")) {
////                //不做处理
////                return body;
////            }
//            //可以在此处修改body，实现全局拦截返回结果修改后返回
//            ServerResponse result = (ServerResponse) body;
//            //result转换为json
//            String resultJson = JSONObject.toJSONString(result.getData());
//            log.info("翻以前"+resultJson);
//            //resultJson去除所有中文标点
////            resultJson = StringUtils.replaceChars(resultJson, "，。：；？！”“‘’《》〈〉「」『』〖〗【】（）〔〕｛｝｟｠﹃﹄﹁﹂﹙﹚﹛﹜﹝﹞", "");
//            //resultJson去除影响翻译中文的标点
//            resultJson = StringUtils.replaceChars(resultJson, "，。：；？！”“‘’《》〈〉「」『』〖〗【】（）〔〕｛｝｟｠﹃﹄﹁﹂﹙﹚﹛﹜﹝﹞", "");
//
////            resultJson = StringUtils.replaceChars(resultJson, "，。；：！”“‘’《》【】", "");
//            log.info("去除后"+resultJson);
//            GoogleTranslateUtil transan = new GoogleTranslateUtil();
//            StringBuilder translate = new StringBuilder();
//            //统计resultJson中的中文字符个数
//            int count = 0;
//            for (int i = 0; i < resultJson.length(); i++) {
//                String temp = resultJson.substring(i, i + 1);
//                if (isChinese(temp)) {
//                    count++;
//                }
//            }
//
//            log.info("resultJson中的中文字符个数为{}", count);
//            //如果resultJson中的中文字符个数大于2000分割中文翻译
//
//            if (lang != null && !lang.equals("zh-CN") && !lang.equals("")) {
//                if (count > 1000) {
//                    ArrayList<String> list = new ArrayList<>();
//                    int size = resultJson.length() / 1000;
//                    for (int i = 0; i < size; i++) {
//                        //以中文字符为单位分割
//
//                        String substring = resultJson.substring(i * 1000, (i + 1) * 1000);
//                        list.add(substring);
//                    }
//                    String substring = resultJson.substring(size * 1000);
//                    list.add(substring);
//                    for (String s : list) {
//                        try {
//                            translate.append(transan.translate("zh", lang, s));
//                        } catch (Exception e) {
//                            log.error("翻译异常", e);
//                            return body;
//                        }
//
//                    }
//                } else {
//                    try {
//                        translate.append(transan.translate("zh", lang, resultJson));
//                    } catch (Exception e) {
//                        log.error("翻译异常", e);
//                        return body;
//                    }
//                }
//
//                log.info("翻译结果为{}", translate);
//                //去除\"
//                String s = translate.toString().replaceAll("\\\\\"", "");
//                 JSONObject data = JSONObject.parseObject(s);
//
//                result.setData(data);
//                //放到缓存里，以便于可以在HandlerInterceptor拦截里取出并打印出返回结果
////            httpSession.setAttribute("body", body);
//                return body;
//            }
//
//            return  body;
//        }
//    }