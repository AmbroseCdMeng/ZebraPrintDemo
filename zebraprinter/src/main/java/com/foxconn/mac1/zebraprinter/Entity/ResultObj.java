package com.foxconn.mac1.zebraprinter.Entity;

/**
 * Result Object
 */
public class ResultObj {

    /**
     * 状态
     */
    private boolean status;
    /**
     * 信息
     */
    private String message;

    public ResultObj() {
        this.status = false;
    }

    public ResultObj(boolean status) {
        this.status = status;
    }

    public ResultObj(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
