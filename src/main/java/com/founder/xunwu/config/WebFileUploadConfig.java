package com.founder.xunwu.config;

import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

/**
 * @program: xunwu
 * @description: 图片上传配置类
 * @author: YangMing
 * @create: 2018-02-04 23:31
 **/
@Configuration
@ConditionalOnClass({Servlet.class, StandardServletMultipartResolver.class, MultipartConfigElement.class})
@ConditionalOnProperty(prefix="spring.http.multipart",name="enable",matchIfMissing = true)
@EnableConfigurationProperties(MultipartProperties.class)
public class WebFileUploadConfig {

    private final MultipartProperties multipartProperties;

    public WebFileUploadConfig(MultipartProperties multipartProperties) {
        this.multipartProperties = multipartProperties;
    }

    /**
     * 上传配置
     */
    @Bean
    @ConditionalOnMissingBean
     public MultipartConfigElement multipartConfigElement(){

         return this.multipartProperties.createMultipartConfig();
     }
    /**
     * 注册解析器
     */
    @Bean
    @ConditionalOnMissingBean(MultipartResolver.class)
    public StandardServletMultipartResolver multipartResolver(){
        StandardServletMultipartResolver multipartResolver=new StandardServletMultipartResolver();
        multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
        return multipartResolver;
    }
    /**
     * 华东机房（七牛云）
     */
    @Bean
    public com.qiniu.storage.Configuration qiniuConifg(){
        return new com.qiniu.storage.Configuration(Zone.zone0());
    }
    /**
     * 构建七牛云上传图片工具实例
     */
    @Bean
    public UploadManager uploadManager(){
        return new UploadManager(qiniuConifg());
    }

    @Value("${qiniu.accessKey}")
    private String accessKey;
    @Value("${qiniu.secretKey}")
    private String secretKey;

    /**
     * /认证信息实例
     */

    @Bean
    public Auth auth(){
        return  Auth.create(accessKey,secretKey);
    }

    /**
     * 构建七牛空间管理
     * @return
     */
    @Bean
    public BucketManager bucketManager(){
        return new BucketManager(auth(),qiniuConifg());
    }
    @Bean
    public Gson gson(){
        return new Gson();
    }
}
