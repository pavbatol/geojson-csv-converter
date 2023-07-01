package com.pavbatol.gjcc;

import com.pavbatol.gjcc.converter.FileDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        final Logger logger = LoggerFactory.getLogger(App.class);
        logger.debug("App starting");

        FileDataLoader fileDataLoader = new FileDataLoader();
        fileDataLoader.run(5);
//        fileDataLoader.run(null);
    }
}
