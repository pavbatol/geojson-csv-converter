package com.pavbatol.gjcc.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class LogConfig {
    private static final String PACKAGE_COM_PAVBATOL = "com.pavbatol";
    private static final String ROOT = "root";

    public static void apply() {
        new LogConfig().setLevel();
    }

    private void setLevel() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(ROOT).setLevel(Level.toLevel(Props.LOG_LEVEL_ROOT.getValue()));
        loggerContext.getLogger(PACKAGE_COM_PAVBATOL).setLevel(Level.toLevel(Props.LOG_LEVEL_COM_PAVBATOL.getValue()));

    }
}
