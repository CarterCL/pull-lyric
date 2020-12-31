package com.cl.pulllyric.response;


/**
 * @author: CarterCL
 * @date: 2020/12/30 22:40
 * @description:
 */
public class KuGouGetHashResponse {
    private Integer status;
    private String error;
    private KuGouGetHashResponseData data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public KuGouGetHashResponseData getData() {
        return data;
    }

    public void setData(KuGouGetHashResponseData data) {
        this.data = data;
    }
}



