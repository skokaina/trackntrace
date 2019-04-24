package com.github.marocraft.trackntrace.context;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.github.marocraft.trackntrace.annotation.EnableTracknTrace;

@Configuration
@EnableAspectJAutoProxy
@EnableTracknTrace
public class SpringAOPContext {

}