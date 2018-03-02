package com.founder.xunwu.web.dto;

/**
 * @program: xunwu
 * @description: 七牛云结果
 * @author: YangMing
 * @create: 2018-02-05 20:13
 **/
public class QINIUPutRet {
    public String key;
    public String hash;
    public String bucket;
    public int width;
    public int height;

    public long fsize;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getFsize() {
        return fsize;
    }

    public void setFsize(long fsize) {
        this.fsize = fsize;
    }

    @Override
    public String toString() {
        return "QINIUPutRet{" +
                "key='" + key + '\'' +
                ", hash='" + hash + '\'' +
                ", bucket='" + bucket + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", fsize=" + fsize +
                '}';
    }
}
