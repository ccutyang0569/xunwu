package com.founder.xunwu.service;

/**
 * @program: xunwu
 * @description: 手机短信验证码服务
 * @author: yangming
 * @create: 2018-03-26 18:09
 **/
public interface ISmsServcie {

    /**
     * 向用户手机发送验证码
     * @param telephone
     * @return
     */
     ServiceResult<String>  sendSms(String telephone);

    /**
     * 从缓存中获取验证码
     * @param telephone
     * @return
     */
     ServiceResult<String> getSmsCode(String telephone);

    /**
     *
     * @param telephone
     * @return
     */
     ServiceResult<String> remove(String telephone);
}
