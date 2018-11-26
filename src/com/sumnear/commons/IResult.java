package com.sumnear.commons;

/**
 * <pre>
 * 用于方法的返回值
 * 很多方法除了要返回成功与否，还要返回错误信息和数据
 * isSuccess()得到操作是否成功
 * getMessage()得到错误信息
 * getData()数据
 * </pre>
 */
public interface IResult<T> {

    /**
	 * 是否成功
	 * 
	 * @return
	 */
    boolean isSuccess();

    /**
	 * 得到消息
	 * 
	 * @return
	 */
    String getMessage();

    /**
	 * 得到数据
	 * 
	 * @return
	 */
    T getData();

}
