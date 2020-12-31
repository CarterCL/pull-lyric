package com.cl.pulllyric.response;

/**
 * @author: CarterCL
 * @date: 2020/12/30 22:59
 * @description:
 */
public class KuGouGetLyricResponse {
    private String content;
    private String info;
    private Integer status;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
