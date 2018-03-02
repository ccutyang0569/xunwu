package com.founder.xunwu.service.search;

/**
 * @program: xunwu
 * @description:
 * @author: YangMing
 * @create: 2018-03-02 23:06
 **/
public class HouseSuggest {
    private String input;
    //默认权重
    private int weight = 10;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
