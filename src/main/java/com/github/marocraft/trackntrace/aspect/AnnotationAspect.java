package com.github.marocraft.trackntrace.aspect;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.github.marocraft.trackntrace.collect.ILogCollector;
import com.github.marocraft.trackntrace.config.IConfigurationTnT;
import com.github.marocraft.trackntrace.domain.CorrelationId;
import com.github.marocraft.trackntrace.domain.LogLevel;
import com.github.marocraft.trackntrace.http.HttpLog;
import com.github.marocraft.trackntrace.logger.LogCollection;
import com.github.marocraft.trackntrace.logger.LogResolver;
import com.github.marocraft.trackntrace.logger.Logger;
import com.github.marocraft.trackntrace.publish.LoggerThread;
import com.github.marocraft.trackntrace.publish.ThreadPoolManager;

/**
 * Annotations Core processing aspect allowing to run the behavior of the
 * framework defined annotations like @Trace
 * 
 * @author Houseine TASSA
 * @author Sallah KOKAINA
 * @author Khalid ELABBADI
 * @since 0.0.3
 * 
 */
@Aspect
@Component
public class AnnotationAspect {

	private static final Object REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";

	@Autowired
	@Qualifier("configurationTnTDefault")
	IConfigurationTnT config;

	@Autowired
	@Qualifier("defaultLogCollector")
	ILogCollector logCollector;

	@Autowired
	ThreadPoolManager threadPoolManager;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	HttpLog httpLog;

	JoinPoint globalJoinpoint;

	StopWatch stopWatch;
	


	@Autowired
	@Qualifier("restLogger")
	private Logger restLogger;

	@Autowired
	@Qualifier("defaultLogger")
	private Logger defaultLogger;

	@Autowired
	CorrelationId correlationId;

	/**
	 * Start multi-threading
	 * 
	 */
	@PostConstruct
	public void postConstruct() {
		threadPoolManager.initialize();
		for (int i = 1; i <= config.getThreadPoolsize(); i++) {
			threadPoolManager.addNewThread(applicationContext.getBean(LoggerThread.class));
		}
	}

	/**
	 * Collect data about annotated methods execution and generate a specific based
	 * template
	 * 
	 * @param joinPoint is a param that contains information about methods and classes
	 * @throws Throwable class is the superclass of all errors and exceptions in the Java language
	 * @return proceed
	 */
	@Around("@annotation(com.github.marocraft.trackntrace.annotation.Trace)")
	public Object whenAnnotatedWithTrace(final ProceedingJoinPoint joinPoint) throws Throwable {
		globalJoinpoint = joinPoint;
		stopWatch = startTimer();
		Object proceed = executeAnnotedMethod(joinPoint);
		stopTimer(stopWatch);
		generateLog(joinPoint, stopWatch, "");

		return proceed;
	}

	/**
	 * Collect and generate logs
	 * 
	 * @param joinPoint
	 * @param stopWatch
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private void generateLog(final JoinPoint joinPoint, StopWatch stopWatch, String exceptionMessage)
			throws IllegalAccessException {
		Clock baseClock = Clock.systemDefaultZone();
		Duration duration = Duration.ofNanos(10);
		Clock clock = Clock.tick(baseClock, duration);
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Object clazz = joinPoint.getTarget();
		String logMessage = logCollector.getMessageFromSignature(signature);
		LogLevel logLevel = logCollector.getLevelFromSignature(signature);
		if (!StringUtils.isEmpty(exceptionMessage)) {
			logMessage = logMessage + " - Exception: " + exceptionMessage;
		}
		LogCollection logCollection = new LogCollection(clazz.getClass().getName(), signature, stopWatch,
				LocalDateTime.now(clock), httpLog, logLevel, logMessage, correlationId.getTraceId(),
				correlationId.getSpanId(), correlationId.getParentId());

		LogResolver resolver = new LogResolver(getLogStrategy(signature));
		resolver.process(logCollection);
	}

	/**
	 * execute Annoted Method
	 * 
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	private Object executeAnnotedMethod(final ProceedingJoinPoint joinPoint) throws Throwable {

		return joinPoint.proceed();
	}

	/**
	 * 
	 * Stop timer to calculate duration
	 * 
	 * @param stopWatch
	 */
	private void stopTimer(StopWatch stopWatch) {
		stopWatch.stop();
	}

	/**
	 * Start Timer to calculate
	 * 
	 * @return
	 */
	private StopWatch startTimer() {
		stopWatch = new StopWatch();
		stopWatch.start();

		return stopWatch;
	}

	/**
	 * Get log Startegy
	 * 
	 * @param joinpointSignature
	 * @return
	 */
	private Logger getLogStrategy(MethodSignature joinpointSignature) {
		if (isRestAnnotated(joinpointSignature)) {
			return restLogger;
		} else {
			return defaultLogger;
		}
	}

	/**
	 * Check if the class is annotated with Rest
	 * 
	 * @param signature
	 * @return
	 */
	private boolean isRestAnnotated(MethodSignature signature) {
		Method method = signature.getMethod();
		Class<?> clazz = method.getDeclaringClass();
		for (Annotation annotation : clazz.getAnnotations()) {
			if (REST_CONTROLLER_ANNOTATION.equals(annotation.annotationType().getName())) {
				return true;
			}
		}

		return false;
	}

	@AfterThrowing(pointcut = "@annotation(com.github.marocraft.trackntrace.annotation.Trace)", throwing = "ex")
	public void logAfterThrowingAllMethods(Exception ex) throws IllegalAccessException {
		if (stopWatch.isRunning()) {
			stopTimer(stopWatch);
		}

		try {
			HTTPException httpEx = (HTTPException) ex;
			httpLog.setHttpStatus("" + httpEx.getStatusCode());
		} catch (Exception exx) {
			httpLog.setHttpStatus("500");
		} finally {
			generateLog(globalJoinpoint, stopWatch, ex.toString());
		}
	}
}