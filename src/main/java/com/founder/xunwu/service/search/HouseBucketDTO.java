package com.founder.xunwu.service.search;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-05-01 01:54
 **/
public class HouseBucketDTO {


    /**
     * bucket key
     */
    public String  key;

    /**
     *聚合结果值
     */
    public  long count;

    public HouseBucketDTO(String key, long count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
