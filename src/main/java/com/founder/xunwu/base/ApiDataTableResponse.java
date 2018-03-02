package com.founder.xunwu.base;

/**
 * @program: xunwu
 * @description: 响应结构
 * @author: YangMing
 * @create: 2018-02-20 20:38
 **/
public class ApiDataTableResponse extends ApiResponse {

    private int draw;
    private long recordsTotal;
    private  long recordsFilterd;

    public ApiDataTableResponse(int draw, long recordsTotal, long recordsFilterd) {
        this.draw = draw;
        this.recordsTotal = recordsTotal;
        this.recordsFilterd = recordsFilterd;
    }

    public ApiDataTableResponse(int code, String message, Object data) {
        super(code, message, data);
    }

    public ApiDataTableResponse(ApiResponse.Status status){
        this(status.getCode(), status.getStandardMessage(), null);
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public long getRecordsFilterd() {
        return recordsFilterd;
    }

    public void setRecordsFilterd(long recordsFilterd) {
        this.recordsFilterd = recordsFilterd;
    }
}
