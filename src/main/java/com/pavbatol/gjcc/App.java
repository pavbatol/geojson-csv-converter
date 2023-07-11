package com.pavbatol.gjcc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.pavbatol.gjcc.config.AppConfig;
import com.pavbatol.gjcc.converter.Converter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Slf4j
public class App {

    private static final String LOG_LEVEL = AppConfig.getInstance().getLogLevel();
    private static final String PACKAGE_COM_PAVBATOL = "com.pavbatol";

    public static void main(String[] args) throws IOException {

        final Logger logger = LoggerFactory.getLogger(App.class);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(PACKAGE_COM_PAVBATOL).setLevel(Level.toLevel(LOG_LEVEL));

        logger.info("App starting");

        Converter.run();

        logger.info("The application is completed");
    }
}
