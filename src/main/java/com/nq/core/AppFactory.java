package com.nq.core;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 应用工厂，获取常用的bean其他spring bean和spring配置
 *
 * @date 07/24/2020 19:29
 */
@Component
public class AppFactory implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }


    public static <T> T getBean(Class<T> aClass) {
        Objects.requireNonNull(applicationContext);
        return applicationContext.getBean(aClass);
    }

    public static <T> T getBean(String name, Class<T> aClass) {
        Objects.requireNonNull(applicationContext);
        return applicationContext.getBean(name, aClass);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取配置文件
     *
     * @return
     */
    public static String getConfig(String key) {
        Environment env = applicationContext.getEnvironment();
        return env.getProperty(key);
    }

}