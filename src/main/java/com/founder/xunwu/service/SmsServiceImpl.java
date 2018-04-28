package com.founder.xunwu.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @program: xunwu
 * @description: 手机验证码实现类
 * @author: YangMing
 * @create: 2018-03-26 18:16
 **/
@Service
public class SmsServiceImpl implements ISmsService,InitializingBean {

    @Value("${aliyun.sms.accsessKey}")
    private String accessKey;
    @Value("${aliyun.sms.accessKeySecret}")
    private String secretKey;
    @Value("${aliyun.sms.template.code}")
    private String templateCode;

    private final String signName="寻屋";

    private static final Logger logger= LoggerFactory.getLogger(SmsServiceImpl.class);

    private static final String[] NUMS={"0","1","2","3","4","5","6","7","8","9",};
    
    private static final Random random = new Random();
    private static final String SMS_CODE_CONTENT_PREFIX="SMS_CODE_CONTENT_PREFIX";
    private IAcsClient acsClient;

    @Autowired
    private RedisTemplate<String,String>  redisTemplate;

    @Override
    public ServiceResult<String> sendSms(String telephone) {

        String gapKey="SMS::CODE::INTERVAL"+telephone;
        String result = redisTemplate.opsForValue().get(gapKey);
        if(result!=null){
            return  new ServiceResult<String>(false,"请求的次数太频繁");

        }
        String code   =generateRandomSmsCode();
        String templateParam = String.format("{\"code\": \"%s\"}", code);
        //组装请求对象

        SendSmsRequest request=new SendSmsRequest();
        request.setTemplateCode(templateCode);
        request.setSignName(signName);
        request.setTemplateParam(templateParam);
        request.setPhoneNumbers(telephone);
        request.setMethod(MethodType.POST);
        //请求
        boolean success=false;
        try {
            SendSmsResponse response = acsClient.getAcsResponse(request);
            if ("OK".equals(response.getCode())){
               success=true;
            } else{
               logger.error("telephone:"+telephone+" is sending code failed :"+ response.getMessage());
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }

        if (success){
            redisTemplate.opsForValue().set(gapKey, code, 60, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(SMS_CODE_CONTENT_PREFIX + telephone, code, 5, TimeUnit.MINUTES);
            return  ServiceResult.of(code);
        }else{
            return  new ServiceResult<>(false,"服务器忙，请稍后重试！");
        }



    }

    /**
     * 6位验证码生成器
     * @return
     */
    private String generateRandomSmsCode() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <6 ; i++) {
            int index = random.nextInt(10);
            sb.append(NUMS[index]);

        }
        return sb.toString();
    }

    @Override
    public String getSmsCode(String telephone) {

        return redisTemplate.opsForValue().get(SMS_CODE_CONTENT_PREFIX+telephone);
    }

    @Override
    public void remove(String telephone) {
        redisTemplate.delete(SMS_CODE_CONTENT_PREFIX+telephone);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("sun.net.client.defaultConnectTimeout","10000");

        System.setProperty("sun.net.client.defaultReadTimeOut","10000");

        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secretKey);

        String product = "Dysmsapi";

        String domain = "dysmsapi.aliyuncs.com";

        DefaultProfile.addEndpoint("cn-hangzhou","cn-hangzhou",product,domain);

        this.acsClient = new DefaultAcsClient(profile);


    }
}
