package com.founder.xunwu.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;

/**
 * @program: xunwu
 * @description: house服务接口
 * @author: yangming
 * @create: 2018-02-05 19:27
 **/
public interface IQiniuService {

    Response uploadFile(File file) throws QiniuException;

    Response uploadFile(InputStream inputStream) throws QiniuException;

    Response  deleteFile(String key) throws  QiniuException;


}
