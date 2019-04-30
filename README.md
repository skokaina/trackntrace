# <img src="src/docs/images/trackntrace.png" width="96" height="96"> TracknTrace Framework

## Project description

Spring Module for Log generation through AOP
TnT is a java framework that offers centralization and aggregation of the format of logs . with a simple annotation you can generate technical logs of your method

- Smouth exchange between DEVs and OPS
- Standardize project logs
- Save time in communication between development, deployment
  
## How to use

1. Add the following dependency to your pom.xml file

    ``` xml
    <dependency>
        <groupId>com.github.marocraft.trackntrace</groupId>
        <artifactId>tnt-core</artifactId>
        <version>0.0.3-SNAPSHOT</version>
    </dependency>
    ```

2. Configure your application.yml or application.properties file to costumize the template format :

        application.properties:

    ``` json
    tnt.logging.format={"methodName": "{{methodName}}","className": "{{className}}","logLevel": "{{logLevel}}","executionTime": "{{executionTime}}","logMessage": "{{logMessage}}","traceId": "{{traceId}}","spanId": "{{spanId}}"}
    tnt.logging.output=json
    tnt.multithread.poolsize=1
    ```

        application.yml:

    ```json
    tnt:
    multithread:
    poolsize: 1
    logging:
    format: {"methodName": "{{methodName}}","className": "{{className}}","logLevel": "{{logLevel}}","executionTime": "{{executionTime}}","logMessage": "{{logMessage}}","traceId": "{{traceId}}","spanId": "{{spanId}}"}
    output: json
    ```

    - tnt.logging.format: is the log format that will be diplsayed or written

    - tnt.logging.output: is the output type forinstance the onely disponible type is json

    - tnt.multithread.poolsize: is the number of thread that will be started

3. Add the annotation @EnableTracknTrace to your main class:

    ``` java
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.context.annotation.ComponentScan;

    import com.github.marocraft.trackntrace.annotation.EnableTracknTrace;

    @SpringBootApplication
    @EnableTracknTrace
    @ComponentScan(basePackages = { "com.bnpparibas" })
    public class DemoTnt {
        public static void main(String[] args) {
            SpringApplication.run(DemoTnt.class, args);
        }
    }
    ```

4. Add the annotation Trace to your method

    ``` java
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RestController;

    import com.github.marocraft.trackntrace.annotation.Trace;
    import com.github.marocraft.trackntrace.domain.LogLevel;


    @RestController
    public class RestControllerTnT {
        @Trace(message="my message",level=LogLevel.IMPORTANT)
        @GetMapping(value = "/hello")
        public String getTntLogs() {
            return "works";
        }
    }
    ```

     - Parameters message and level are optional

5. Add logback.xml file to your src/main/resources:

    ``` xml
    <configuration>
        <appender name="FILE-TNT" class="ch.qos.logback.core.FileAppender">
                <file>C:/logstnt/demo-TnT-logs.txt</file>

                <encoder>
                    <Pattern>
                        %msg%n
                    </Pattern>
                </encoder>
        </appender>
        <logger name="com.github.marocraft.trackntrace" level="debug"
            additivity="false">
            <appender-ref ref="FILE-TNT" />
        </logger>
    </configuration>

    ```

    - Specify the pattern and TnT logs will be de included via variable %msg
    - Specify a new logger with this name: name="com.github.marocraft.trackntrace"

6. Result

    ``` json
    {"methodName": "getTntLogs","className": "com.bnpparibas.raiserframework.demotnt.RestControllerTnT","logLevel": "IMPORTANT","executionTime": "4","logMessage": "","traceId": "d769eada-8f61-496f-be3e-d623790dca59","spanId": "31d79f00-9ba8-4de8-9cad-ff84dfbf230d"}
    ```

## Features

    - [X] Business logging: Automatic Logger for Business Layer
    - [X] Rest logging: Automatic Logger for REST Layer
    - [X] Data logging: Automatic Logger for Data Layer

## Maintainers

- Sallah KOKAINA
- Houseine TASSA
- Khalid ELABBADI
- Badr Eddine ZINOUN
- Sanaa HSAKOU
- Hassane EL GHACHTOUL