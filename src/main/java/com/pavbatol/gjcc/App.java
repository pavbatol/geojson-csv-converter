package com.pavbatol.gjcc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.pavbatol.gjcc.config.Props;
import com.pavbatol.gjcc.converter.Converter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Slf4j
public class App {
    private static final String PACKAGE_COM_PAVBATOL = "com.pavbatol";
    private static final String ROOT = "root";

    public static void main(String[] args) throws IOException {

        final Logger logger = LoggerFactory.getLogger(App.class);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(ROOT).setLevel(Level.toLevel(Props.LOG_LEVEL_ROOT.getValue()));
        loggerContext.getLogger(PACKAGE_COM_PAVBATOL).setLevel(Level.toLevel(Props.LOG_LEVEL_COM_PAVBATOL.getValue()));

        logger.info("App starting");

        Converter.run();

        logger.info("The application is completed");
    }
}
