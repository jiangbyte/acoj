package io.charlie.app.gateway.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 返回结果封装
 */
@Data
public class Result<T> implements Serializable {

    private String code;

    private T data;

    private String message;

    private Boolean success;

    /**
     * 分页数据内部类
     */
    @Data
    public static class PageData<T> {
        private List<T> records;
        private Long pages;
        private Long total;
        private Long size;
        private Long current;

//        public static <T> PageData<T> from(IPage<T> page) {
//            PageData<T> pageData = new PageData<>();
//            pageData.setRecords(page.getRecords());
//            pageData.setTotal(page.getTotal());
//            pageData.setSize(page.getSize());
//            pageData.setCurrent(page.getCurrent());
//            pageData.setPages(page.getPages());
//            return pageData;
//        }
    }

    private static <T> Result<T> result(String code, T data, String message, Boolean success) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMessage(message);
        result.setSuccess(success);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setData(data);
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setSuccess(true);
        return result;
    }

    /**
     * 创建分页成功结果
     */
//    public static <T> Result<PageData<T>> successForPage(IPage<T> page) {
//        return success(PageData.from(page));
//    }

    public static <T> Result<PageData<T>> successForPageData(PageData<T> pageData) {
        return success(pageData);
    }

    public static <T> Result<T> failure() {
        return result(ResultCode.PARAM_ERROR.getCode(), null, ResultCode.PARAM_ERROR.getMessage(), false);
    }

    public static <T> Result<T> failure(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.PARAM_ERROR.getCode());
        result.setData(data);
        result.setMessage(ResultCode.PARAM_ERROR.getMessage());
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> failure(String message) {
        return result(ResultCode.PARAM_ERROR.getCode(), null, message, false);
    }

    public static <T> Result<T> failure(IResultCode resultCode) {
        return result(resultCode.getCode(), null, resultCode.getMessage(), false);
    }

    public static <T> Result<T> failure(IResultCode resultCode, String message) {
        return result(resultCode.getCode(), null, message, false);
    }

    public static <T> Result<T> status(boolean status) {
        if (status) {
            return success();
        } else {
            return failure();
        }
    }

    public static boolean isSuccess(Result<?> result) {
        return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
    }

    private static final long serialVersionUID = 1L;
}
