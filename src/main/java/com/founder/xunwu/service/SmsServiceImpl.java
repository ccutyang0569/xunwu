package com.founder.xunwu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @program: xunwu
 * @description: 手机验证码实现类
 * @author: YangMing
 * @create: 2018-03-26 18:16
 **/
public class SmsServiceImpl implements ISmsServcie {

    @Value("${aliyun.sms.accessKey}")
    private String accessKey;
    @Value("${aliyun.sms.accessKeySecret}")
    private String secretKey;
    @Value("${aliyun.sms.template.code}")
    private String templateCode;
    
    private RedisTemplate<String,String>  redisTemplate;

    @Override
    public ServiceResult<String> sendSms(String telephone) {
        return null;
    }

    @Override
    public ServiceResult<String> getSmsCode(String telephone) {
        return null;
    }

    @Override
    public ServiceResult<String> remove(String telephone) {
        return null;
    }
}
