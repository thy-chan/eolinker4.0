package com.eolinker.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.eolinker.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源配置类
 *
 * @author www.eolinker.com 广州银云信息科技有限公司 2015-2018
 * eoLinker是目前全球领先、国内最大的在线API接口管理平台，提供自动生成API文档、API自动化测试、Mock测试、团队协作等功能，旨在解决由于前后端分离导致的开发效率低下问题。
 * 如在使用的过程中有任何问题，欢迎加入用户讨论群进行反馈，我们将会以最快的速度，最好的服务态度为您解决问题。
 * <p>
 * eoLinker AMS开源版的开源协议遵循GPL V3，如需获取最新的eolinker开源版以及相关资讯，请访问:https://www.eolinker.com/#/os/download
 * <p>
 * 官方网站：https://www.eolinker.com/ 官方博客以及社区：http://blog.eolinker.com/
 * 使用教程以及帮助：http://help.eolinker.com/ 商务合作邮箱：market@eolinker.com
 * 用户讨论QQ群：707530721
 * @name eolinker ams open source，eolinker开源版本
 * @link https://www.eolinker.com/
 * @package eolinker
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**").excludePathPatterns("/Guest/**",
                "/Setting/**", "/Mock/**", "/*.html", "/assets/**", "/scripts/**", "/styles/**", "/vendor/**",
                "/libs/**", "/Install/**", "/error", "/actuator/shutdown", "/Update/**");

    }

    //	@Override
//    public void addCorsMappings(CorsRegistry registry) {  
//        registry.addMapping("/**")  
//                .allowedOrigins("*")  
//                .allowCredentials(true)  
//                .allowedMethods("GET", "POST", "DELETE", "PUT")  
//                .maxAge(3600);  
//    }
//	 @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         //全局支持CORS（跨源请求）
//         registry.addMapping("/**")
//                 //允许任意来源    
//                 .allowedOrigins("*")
//                 .allowedMethods("PUT", "DELETE","OPTIONS", "GET", "POST")
//                 .allowedHeaders("*")
//                 .exposedHeaders("access-control-allow-headers",
//                         "access-control-allow-methods",
//                         "access-control-allow-origin",
//                         "access-control-max-age",
//                         "X-Frame-Options")
//                 .allowCredentials(CrossOrigin.DEFAULT_ALLOW_CREDENTIALS)//允许Cookie跨域
//                 .maxAge(3600);
//     }
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedOrigins("*")
                .allowedMethods("*");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/webapp/");
        try {
            File classPath = new File(ResourceUtils.getURL("classpath:").getPath());
            if (!classPath.exists())
                classPath = new File("");
            File dir = new File(classPath.getAbsolutePath(), "dump");
            if (!dir.exists() || !dir.isDirectory())
                dir.mkdirs();
            String path = dir.getAbsolutePath();
            registry.addResourceHandler("/dump/**").addResourceLocations("file:" + path + "/");
            registry.addResourceHandler("/config/setting.properties")
                    .addResourceLocations(System.getProperty("user.dir") + "/config/setting.properties");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 利用fastjson替换掉jackson，且解决中文乱码问题
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty);
        // 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        fastConverter.setFastJsonConfig(fastJsonConfig);

        // 处理字符串, 避免直接返回字符串的时候被添加了引号
        StringHttpMessageConverter smc = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converters.add(smc);

        converters.add(fastConverter);
    }

}
