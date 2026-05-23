package com.example.app.dto.response;

public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<T>();
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().code(200).message("success").data(data).build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder().code(200).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder().code(code).message(message).data(null).build();
    }

    public static <T> ApiResponse<T> error(String message, Integer code) {
        return ApiResponse.<T>builder().code(code).message(message).data(null).build();
    }

    public static class ApiResponseBuilder<T> {
        private Integer code;
        private String message;
        private T data;

        public ApiResponseBuilder<T> code(Integer code) { this.code = code; return this; }
        public ApiResponseBuilder<T> message(String message) { this.message = message; return this; }
        public ApiResponseBuilder<T> data(T data) { this.data = data; return this; }

        public ApiResponse<T> build() {
            return new ApiResponse<T>(code, message, data);
        }
    }
}
