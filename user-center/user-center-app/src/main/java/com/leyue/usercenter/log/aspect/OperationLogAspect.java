package com.leyue.usercenter.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.usercenter.api.annotation.OperationLog;
import com.leyue.usercenter.domain.log.gateway.OperationLogGateway;
import com.leyue.usercenter.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 用于自动记录带有@OperationLog注解的方法调用
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogGateway operationLogGateway;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 定义切入点：所有使用@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.leyue.usercenter.api.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 环绕通知：记录方法执行前后的操作日志
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean isSuccess = true;
        String errorMessage = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            isSuccess = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            try {
                // 记录操作日志
                recordOperationLog(joinPoint, result, isSuccess, errorMessage, System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                logger.error("记录操作日志失败", e);
            }
        }
    }

    /**
     * 异常通知：记录方法执行异常的操作日志
     *
     * @param joinPoint 连接点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "operationLogPointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e) {
        // 异常已在环绕通知中处理，这里不需要额外处理
    }

    /**
     * 记录操作日志
     *
     * @param joinPoint     连接点
     * @param result        方法执行结果
     * @param isSuccess     是否执行成功
     * @param errorMessage  错误信息
     * @param executionTime 执行时间(毫秒)
     */
    private void recordOperationLog(JoinPoint joinPoint, Object result, boolean isSuccess, String errorMessage, long executionTime) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                logger.warn("无法获取当前用户信息，操作日志记录失败");
                return;
            }

            User currentUser = null;
            if (authentication.getPrincipal() instanceof User) {
                currentUser = (User) authentication.getPrincipal();
            } else {
                logger.warn("当前用户信息类型不匹配，操作日志记录失败: {}", authentication.getPrincipal());
                return;
            }

            // 获取方法签名和注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);

            // 获取请求信息
            HttpServletRequest request = null;
            String ipAddress = "unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    request = attributes.getRequest();
                    ipAddress = getIpAddress(request);
                }
            } catch (Exception e) {
                logger.warn("获取请求信息失败", e);
            }

            // 创建操作日志对象
            com.leyue.usercenter.domain.log.OperationLog operationLog = new com.leyue.usercenter.domain.log.OperationLog();
            operationLog.setUserId(currentUser.getId());
            operationLog.setUsername(currentUser.getUsername());
            operationLog.setOperationType(operationLogAnnotation.operationType());
            operationLog.setModule(operationLogAnnotation.module());
            operationLog.setDescription(operationLogAnnotation.description());
            operationLog.setSeverityLevel(operationLogAnnotation.severityLevel());
            operationLog.setIsSensitive(operationLogAnnotation.isSensitive() ? 1 : 0);
            operationLog.setIpAddress(ipAddress);
            operationLog.setOperationResult(isSuccess ? "成功" : "失败");
            operationLog.setCreatedAt(new Date());

            // 解析SpEL表达式获取目标ID
            if (!operationLogAnnotation.targetId().isEmpty()) {
                Object targetId = parseSpEL(operationLogAnnotation.targetId(), joinPoint);
                if (targetId != null) {
                    if (targetId instanceof Number) {
                        operationLog.setTargetId(((Number) targetId).longValue());
                    } else {
                        operationLog.setTargetId(Long.parseLong(targetId.toString()));
                    }
                }
            }

            // 构建操作内容
            Map<String, Object> contentMap = new HashMap<>();

            // 解析SpEL表达式获取操作内容
            if (!operationLogAnnotation.content().isEmpty()) {
                Object content = parseSpEL(operationLogAnnotation.content(), joinPoint);
                if (content != null) {
                    contentMap.put("customContent", content);
                }
            }

            // 保存请求参数
            if (operationLogAnnotation.saveParams()) {
                Object[] args = joinPoint.getArgs();
                // 过滤掉请求对象和敏感信息
                Map<String, Object> filteredArgs = new HashMap<>();
                String[] paramNames = signature.getParameterNames();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null && !(args[i] instanceof HttpServletRequest)) {
                        filteredArgs.put(paramNames[i], sanitizeObject(args[i]));
                    }
                }
                contentMap.put("params", filteredArgs);
            }

            // 保存返回结果
            if (operationLogAnnotation.saveResult() && result != null) {
                contentMap.put("result", sanitizeObject(result));
            }

            // 保存执行时间
            contentMap.put("executionTime", executionTime + "ms");

            // 保存错误信息
            if (!isSuccess && errorMessage != null) {
                contentMap.put("errorMessage", errorMessage);
            }

            operationLog.setOperationContent(contentMap);

            // 处理审批信息
            if (operationLogAnnotation.needApproval()) {
                Map<String, Object> approvalInfo = new HashMap<>();
                approvalInfo.put("needApproval", true);
                approvalInfo.put("approvalStatus", "待审批");
                approvalInfo.put("createTime", new Date());
                operationLog.setApprovalInfo(approvalInfo);
            }

            // 保存操作日志
            operationLogGateway.save(operationLog);

        } catch (Exception e) {
            logger.error("记录操作日志过程中发生异常", e);
        }
    }

    /**
     * 解析SpEL表达式
     *
     * @param spEL      SpEL表达式
     * @param joinPoint 连接点
     * @return 解析结果
     */
    private Object parseSpEL(String spEL, JoinPoint joinPoint) {
        if (spEL == null || spEL.isEmpty()) {
            return null;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            String[] paramNames = signature.getParameterNames();

            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            Expression expression = expressionParser.parseExpression(spEL);
            return expression.getValue(context);
        } catch (Exception e) {
            logger.warn("解析SpEL表达式失败: " + spEL, e);
            return null;
        }
    }

    /**
     * 处理对象，去除敏感信息
     *
     * @param obj 原对象
     * @return 处理后的对象
     */
    private Object sanitizeObject(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            // 这里可以根据需要实现对敏感信息的过滤
            // 例如，将密码等信息替换为***
            // 为简化实现，这里直接将对象转为字符串再返回
            String json = objectMapper.writeValueAsString(obj);
            // 在实际项目中，可以在这里对json进行处理，过滤敏感信息
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.warn("处理对象失败", e);
            return obj.toString();
        }
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request 请求对象
     * @return IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP地址
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
