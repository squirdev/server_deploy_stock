package com.nq.common.filter;

import com.nq.common.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ExceptionResolver implements HandlerExceptionResolver {
    private static final Logger log = LoggerFactory.getLogger(ExceptionResolver.class);

    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        log.error("==============================ExceptionResolver=================================");
        log.error("{} Exception by nq", httpServletRequest.getRequestURI(), e);

        e.printStackTrace();
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        modelAndView.addObject("status", Integer.valueOf(ResponseCode.ERROR.getCode()));
        modelAndView.addObject("msg", "當前交易人數過多，請稍後嘗試！");
        modelAndView.addObject("data", e.toString());

        return modelAndView;
    }
}
