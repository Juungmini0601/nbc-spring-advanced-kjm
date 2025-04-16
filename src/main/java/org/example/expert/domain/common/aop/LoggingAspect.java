package org.example.expert.domain.common.aop;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author    : kimjungmin
 * Created on : 2025. 4. 16.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {
	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void restController() {
	}

	@Around("restController()")
	public Object loggingAround(ProceedingJoinPoint joinPoint) throws Throwable {
		HttpServletRequest request = getCurrentHttpRequest();
		String ip = request.getRemoteAddr();
		String url = request.getRequestURI();
		String method = request.getMethod();

		// 요청 바디 로깅 (Method 파라미터 기준)
		Object[] args = joinPoint.getArgs();
		String requestBody = Arrays.stream(args)
			.map(Object::toString)
			.collect(Collectors.joining(", "));

		log.info("[{}] {}  [IP: {}], Body: {}", method, url, ip, requestBody);

		Object result = joinPoint.proceed();

		// 응답 바디 로깅
		log.info("[{}] {} - Response: {}", method, url, result);

		return result;
	}

	private HttpServletRequest getCurrentHttpRequest() {
		ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		return attr.getRequest();
	}
}
