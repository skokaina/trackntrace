package com.github.marocraft.trackntrace.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.marocraft.trackntrace.aspect.AnnotationAspect;
import com.github.marocraft.trackntrace.collect.ILogCollector;
import com.github.marocraft.trackntrace.config.IConfigurationTnT;
import com.github.marocraft.trackntrace.context.SpringAOPContext;
import com.github.marocraft.trackntrace.domain.LogLevel;
import com.github.marocraft.trackntrace.domain.LogTraceDefault;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringAOPContext.class)
public class LogCreationTest {

	@Autowired
	@Qualifier("configurationTnTDefault")
	IConfigurationTnT config;

	@Autowired
	ILogBuilder logBuilder;

	@Autowired
	ILogCollector logCollector;

	@Test
	public void test() {
		AnnotationAspect exempleAspect = new AnnotationAspect();
		assertNotNull(exempleAspect);
	}

	@Test
	public void shouldCreateLogTraceClass() {
		LogTraceDefault logTrace = logCollector.collect(null, null, LogLevel.TRIVIAL, 0, "log", "", "","");
		Assert.assertNotNull(logTrace);
	}

	@Test
	public void shouldCreateLogTraceClassWithData() {
		LogTraceDefault logTrace = logCollector.collect("controller", "myMethod", LogLevel.TRIVIAL, 20L, "", "", "","");
		Assert.assertNotNull(logTrace.getClazz());
	}

	@Test
	public void shouldLogHaveCorrectFormat() throws IllegalAccessException {
		config.getFormat();
		LogTraceDefault logTrace = logCollector.collect("controller", "myMethod", LogLevel.TRIVIAL, 20L, "my message", "", "","");

		String log = logBuilder.build(logTrace);
		assertEquals(
				"{\"methodName\": \"myMethod\",\"className\": \"controller\",\"logLevel\": \"TRIVIAL\",\"executionTime\": \"20\",\"logMessage\": \"my message\",\"traceId\": \"\",\"spanId\": \"\",\"timeStamps\": \"\"}",
				log);
	}
}