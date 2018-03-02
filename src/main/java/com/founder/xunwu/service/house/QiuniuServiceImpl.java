package com.founder.xunwu.service.house;

import com.founder.xunwu.service.IQiniuService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * @program: xunwu
 * @description: house服务实现类
 * @author: YangMing
 * @create: 2018-02-05 19:30
 **/
@Service
public class QiuniuServiceImpl implements IQiniuService, InitializingBean {

    @Autowired
    private UploadManager uploadManager;
    @Autowired
    private BucketManager bucketManager;
    @Autowired
    private Auth auth;
    @Value("${qiniu.bucket}")
    private String bucket;
    private StringMap putPolicy;

    @Override
    public Response uploadFile(File file) throws QiniuException {
        Response response = this.uploadManager.put(file, null, getUploadToken());
        int reTry=0;
        while(response.needRetry()&&reTry++<3){
            response = this.uploadManager.put(file, null, getUploadToken());
            reTry++;
        }
        return response;
    }

    @Override
    public Response uploadFile(InputStream inputStream) throws QiniuException {
        Response response = this.uploadManager.put(inputStream, null, getUploadToken(),null,null);
        int reTry=0;
        while(response.needRetry()&&reTry++<3){
            response = this.uploadManager.put(inputStream, null, getUploadToken(),null,null);
            reTry++;
        }
        return response;
    }

    @Override
    public Response deleteFile(String key) throws QiniuException {
        Response response = bucketManager.delete(bucket, key);
        int reTry=0;
        while (response.needRetry()&&reTry++<3){
            response = bucketManager.delete(bucket, key);
            reTry++;
        }
        return response;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        this.putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");

    }

    /**
     * 获取上传凭证
     */
    private String getUploadToken() {
        String token = this.auth.uploadToken(bucket, null, 3600, putPolicy);
         return token;
    }
}
