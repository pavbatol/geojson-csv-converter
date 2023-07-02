package com.pavbatol.gjcc;

import com.pavbatol.gjcc.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        final Logger logger = LoggerFactory.getLogger(App.class);
        logger.debug("App starting");

        Converter converter = new Converter();
        converter.run();
    }
}
