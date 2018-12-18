package ma.craft.trackntrace.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import ma.craft.trackntrace.collect.LogCollector;
import ma.craft.trackntrace.domain.LogLevel;
import ma.craft.trackntrace.domain.LogTrace;
import ma.craft.trackntrace.domain.Template;
import ma.craft.trackntrace.generate.LogBuilder;
import ma.craft.trackntrace.generate.LogPublisher;

@Aspect
@Component
public class AnnotationAspect {

	private final LogPublisher logPublisher = LogPublisher.instance();
	@Autowired
	LogBuilder logBuilder;

	@Autowired

	Template template;

	/**
	 * Business Log aspect Collect data about annotated methods Generate a log
	 * message based on a specific template
	 * 
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around(value = "@annotation(ma.craft.trackntrace.annotation.BusinessLog)")
	public Object whenAnnotatedWithBusinessLog(final ProceedingJoinPoint joinPoint) throws Throwable {
		StopWatch stopWatch = startTimer();
		Object proceed = executeAnnotedMethod(joinPoint);
		stopTimer(stopWatch);
		collectAndGenerateLog(joinPoint, stopWatch);
		return proceed;
	}

	/**
	 * Rest Log aspect Collect data about annotated methods having also a @Path
	 * annotation Generate a log message containing URI call information based on a
	 * specific template
	 * 
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around(value = "@annotation(ma.craft.trackntrace.annotation.RestLog)")
	public Object whenAnnotatedWithRestLog(final ProceedingJoinPoint joinPoint) throws Throwable {
		// 0. check if method is annotated with @Path

		StopWatch stopWatch = startTimer();
		Object proceed = executeAnnotedMethod(joinPoint);
		stopTimer(stopWatch);

		// 1.collect rest data from @Path annotation and http dispatcher
		// non blocking

		collectAndGenerateLog(joinPoint, stopWatch);
		return proceed;
	}

	private void collectAndGenerateLog(final JoinPoint joinPoint, StopWatch stopWatch)
			throws IllegalAccessException, IOException {
		LogCollector collector = new LogCollector();
		LogLevel LogLevel = collector.collectLogLevel(joinPoint);
		String logMessage = collector.logMessage(joinPoint);
		Signature methodSignature = joinPoint.getSignature();
		Object clazz = joinPoint.getTarget();
		LogTrace logTrace = collector.collect(clazz.getClass().getName(), methodSignature.getName(), LogLevel,
				stopWatch.getTotalTimeMillis(), logMessage);

		String log = logBuilder.build(logTrace);
		logPublisher.publish(log);
		LogPublisher.instance().exportFile(template.getLogsPath(), LogPublisher.instance().getLogs());

	}

	private Object executeAnnotedMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
		return joinPoint.proceed();
	}

	private void stopTimer(StopWatch stopWatch) {
		stopWatch.stop();
	}

	private StopWatch startTimer() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		return stopWatch;
	}
}
