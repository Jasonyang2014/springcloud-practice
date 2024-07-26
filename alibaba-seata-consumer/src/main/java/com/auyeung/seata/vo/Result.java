package com.auyeung.seata.vo;

import lombok.Data;

@Data
public class Result<T> {

    private String desc;
    private int code;
    private T data;

    public Result(String desc, int code, T data) {
        this.desc = desc;
        this.code = code;
        this.data = data;
    }

    public static Result success() {
        return Result.success(null);
    }

    public static <T> Result success(T data) {
        return new Result<>("success", 200, data);
    }

    public static <T> Result fail(T data) {
        return new Result<>("fail", 500, data);
    }

    public static Result fail() {
        return new Result<>("fail", 500, null);
    }


}
