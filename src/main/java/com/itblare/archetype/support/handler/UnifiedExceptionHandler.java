package com.itblare.archetype.support.handler;

import com.itblare.archetype.support.exceptions.BaseException;
import com.itblare.archetype.support.result.ResponseDataFactory;
import com.itblare.archetype.support.result.ResponseDataWrapper;
import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一异常处理器
 * 在spring 3.2中，新增了@ControllerAdvice 注解，可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute，并应用到所有@RequestMapping中
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/6 9:56
 */
@RestControllerAdvice // 等价于@ControllerAdvice+@ResponseBody
public class UnifiedExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedExceptionHandler.class);

    /**
     * 处理未捕获的Exception
     *
     * @param ex 异常
     * @return {@link ResponseDataWrapper<Object>}
     * @author Blare
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDataWrapper<Object> handleException(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseDataFactory.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * 处理未捕获的数据异常
     *
     * @param ex 数据异常
     * @return {@link ResponseDataWrapper}
     * @author Blare
     */
    @ExceptionHandler(DataException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDataWrapper<?> handleBaseException(DataException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseDataFactory.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * 处理未捕获的方法参数错误异常
     *
     * @param ex 方法参数非法异常
     * @return {@link ResponseDataWrapper<Object>}
     * @author Blare
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDataWrapper<Object> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        logger.error(ex.getMessage(), ex);
        List<String> errMsgList = new ArrayList<>();        // 从异常对象中拿到ObjectError对象
        assert ex.getBindingResult() != null;
        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            for (ObjectError error : ex.getBindingResult().getAllErrors()) {
                errMsgList.add(error.getDefaultMessage());
            }
        }
        // 然后提取错误提示信息进行返回
        return ResponseDataFactory.wrapper(HttpStatus.BAD_REQUEST, errMsgList);
    }

    /**
     * 处理未捕获的RuntimeException
     *
     * @param ex 运行时异常
     * @return {@link ResponseDataWrapper}
     * @author Blare
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDataWrapper<?> handleRuntimeException(RuntimeException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseDataFactory.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * 处理自定义异常
     *
     * @param ex 自定义异常
     * @return {@link ResponseDataWrapper<Object>}
     * @author Blare
     */
    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDataWrapper<Object> APIExceptionHandler(BaseException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseDataFactory.errorCodeMessage(ex.getStatus(), ex.getMessage());
    }
}