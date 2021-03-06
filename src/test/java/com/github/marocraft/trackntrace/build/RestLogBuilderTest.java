/**
 * 
 */
package com.github.marocraft.trackntrace.build;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import com.github.marocraft.trackntrace.annotation.Trace;
import com.github.marocraft.trackntrace.collect.ILogCollector;
import com.github.marocraft.trackntrace.context.SpringAOPContext;
import com.github.marocraft.trackntrace.domain.LogLevel;
import com.github.marocraft.trackntrace.domain.RestLogTrace;
import com.github.marocraft.trackntrace.http.HttpLog;
import com.github.marocraft.trackntrace.logger.LogCollection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringAOPContext.class)
public class RestLogBuilderTest {

	@Qualifier("restLogBuilder")
	@Autowired
	RestLogBuilder restLogBuilder;
	
	@Autowired
	@Qualifier("restLogCollector")
	ILogCollector logCollector;
	
	LogCollection collection;
	
	JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
	MethodSignature signature = Mockito.mock(MethodSignature.class);
	Method method = PowerMockito.mock(Method.class);
	Trace trace = PowerMockito.mock(Trace.class);
	
	@Before
	public void beforeTest() {
		
		Mockito.when(joinPoint.getSignature()).thenReturn(signature);
		Mockito.when(signature.getName()).thenReturn("clazz");
		Mockito.when(signature.getMethod()).thenReturn(method);
		Mockito.when(trace.level()).thenReturn(LogLevel.ERROR);
		String date = "2019-05-16T13:07:12.123456785";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn");
		LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
		collection = new LogCollection("clazz", joinPoint.getSignature(), new StopWatch(""), dateTime, new HttpLog(),
				LogLevel.ERROR, "my message", "", "", "");
		collection.setLogLevel(LogLevel.ERROR);

	}
	@Test
	public void shouldBuildLogsAsString() throws IllegalAccessException {

		RestLogTrace logTrace = (RestLogTrace) logCollector.collect(collection);
		String timeOffset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).toString();
		if(timeOffset.length()>2) {
			timeOffset=timeOffset.substring(0, 3);
		}
		String log = restLogBuilder.build(logTrace);
		assertEquals(
				"{\"methodName\": \"clazz\",\"className\": \"clazz\",\"logLevel\": \"ERROR\",\"executionTime\": \"0\",\"logMessage\": \"my message\",\"timeStamps\": \"2019-05-16T13:07:12.123456785 " + 
				timeOffset + "\",\"httpVerb\": \"null\",\"httpStatus\": \"null\",\"httpURI\": \"null\",\"traceId\": \"\",\"spanId\": \"\",\"parentId\": \"\",\"ip\": \"null\"}",
				log);
	}

}
