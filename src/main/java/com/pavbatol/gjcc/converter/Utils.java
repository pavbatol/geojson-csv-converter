package com.pavbatol.gjcc.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static void creatDirectoryIfNotExists(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Directory creation error");
            }
        }
    }

    public static void deleteFile(Path path) {
        if (Files.exists(path)) {
            try {
                Files.delete(path);
                log.debug("The file was successfully deleted: {}", path);
            } catch (NoSuchFileException e) {
                System.err.println("File not found: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error deleting a file: " + e.getMessage());
            }
        }
    }

//    public static List<Path> getFilePathsByExtension(String directoryPath, String extension) {
//        List<Path> filePaths = new ArrayList<>();
//        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
//            for (Path path : directoryStream) {
//                if (Files.isRegularFile(path)) {
//
////                    String fileName = path.getFileName().toString();
////                    String extension = "";
////                    int dotIndex = fileName.lastIndexOf(".");
////                    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
////                        extension = fileName.substring(dotIndex + 1);
////
////                        if (GEOJSON.equalsIgnoreCase(extension)) {
////                            filePaths.add(fileName);
////                        }
////
////                    }
//
//                    if (path.getFileName().toString().endsWith("." + extension)) {
//                        filePaths.add(path);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return filePaths;
//    }

    public static List<String> getFilePathsByExtension(String directoryPath, String extension) throws IOException {
        List<String> filePaths = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {

            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString();
                    if (fileName.toLowerCase().endsWith("." + extension.toLowerCase())) {
                        filePaths.add(path.toString());
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
        return filePaths;
    }

    public static String[] getFilePathArrayByExtension(String directoryPath, String extension) throws IOException {
        return getFilePathsByExtension(directoryPath, extension).toArray(new String[0]);
    }
}
