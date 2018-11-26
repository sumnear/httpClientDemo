package com.sumnear.commons;

public class DefaultResult<T> implements IResult<T> {

    /**
	 * 操作成功
	 */
    private static final SuccessResult SUCCESS = new SuccessResult();
    /**
	 * 操作失败
	 */
    private static final FailResult    FAIL    = new FailResult();

    private boolean                    success;
    private String                     message;
    private T                          data;

    /**
	 * 构造器
	 */
    public DefaultResult() {

    }

    /**
	 * 构造器
	 * 
	 * @param success
	 * @param message
	 */
    public DefaultResult(boolean success, String message) {
        this(success, message, null);
    }

    /**
	 * 构造器
	 * 
	 * @param success
	 * @param data
	 */
    public DefaultResult(boolean success, T data) {
        this(success, null, data);
    }

    /**
	 * 构造器
	 * 
	 * @param success
	 * @param message
	 * @param data
	 */
    public DefaultResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
	 * 操作成功
	 * 
	 * @param success
	 * @param message
	 * @param data
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static <T> DefaultResult<T> successResult() {
        return (DefaultResult<T>) SUCCESS;
    }
    /**
	 * 操作成功
	 * 
	 * @param success
	 * @param message
	 * @param data
	 * @return
	 */
    public static <T> DefaultResult<T> successResult(T data) {
        return new DefaultResult<T>(true, null, data);
    }
    /**
	 * 操作成功
	 * 
	 * @param success
	 * @param message
	 * @param data
	 * @return
	 */
    public static <T> DefaultResult<T> successResult(String message,T data) {
        return new DefaultResult<T>(true, message, data);
    }

    /**
	 * 操作失败
	 * 
	 * @param success
	 * @param message
	 * @param data
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static <T> DefaultResult<T> failResult() {
        return (DefaultResult<T>) FAIL;
    }

    /**
	 * 操作失败
	 * 
	 * @param success
	 * @param message
	 * @param data
	 * @return
	 */
    public static <T> DefaultResult<T> failResult(String message) {
        return new DefaultResult<T>(false, message, null);
    }

    public static <T> DefaultResult<T> failResult(String message,T data) {
        return new DefaultResult<T>(false, message, data);
    }
    /**
	 * 操作信息
	 * 
	 * @param success
	 * @param message
	 * @param data
	 * @return
	 */
    public static <T> DefaultResult<T> result(boolean success, String message, T data) {
        return new DefaultResult<T>(success, message, data);
    }

    /**
	 * 是否成功
	 * 
	 * @return
	 */
    @Override
	public boolean isSuccess() {
        return success;
    }

    /**
	 * 得到消息
	 * 
	 * @return
	 */
    @Override
	public String getMessage() {
        return message;
    }

    /**
	 * 得到数据
	 * 
	 * @return
	 */
    @Override
	public T getData() {
        return data;
    }

    private static class SuccessResult extends DefaultResult<Object> {

        public SuccessResult() {
            super(true, null, null);
        }
    }

    private static class FailResult extends DefaultResult<Object> {

        public FailResult() {
            super(false, null, null);
        }
    }

}
