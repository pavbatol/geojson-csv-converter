package com.pavbatol.gjcc;

import com.pavbatol.gjcc.config.LogConfig;
import com.pavbatol.gjcc.converter.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException {
        LogConfig.apply();

        log.info("App starting");

        Converter.run();

        log.info("The application is completed");
    }
}
