package com.cl.pulllyric.response;

/**
 * @author: CarterCL
 * @date: 2020/12/30 23:18
 * @description:
 */
public class KuGouGetAccessKeyResponse {
    private Integer errcode;

    private KuGouGetAccessKeyResponseCandidate[] candidates;

    public Integer getErrcode() {
        return errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public KuGouGetAccessKeyResponseCandidate[] getCandidates() {
        return candidates;
    }

    public void setCandidates(KuGouGetAccessKeyResponseCandidate[] candidates) {
        this.candidates = candidates;
    }
}
