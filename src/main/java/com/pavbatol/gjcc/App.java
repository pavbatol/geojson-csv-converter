package com.pavbatol.gjcc;

import com.pavbatol.gjcc.converter.Converter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException {
        final Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("App starting");

        Converter.run();

        logger.info("The application is completed");
    }
}
