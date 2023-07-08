package com.pavbatol.gjcc.converter;

import com.pavbatol.gjcc.App;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

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

//    public static boolean fileExistsByExtension(String[] filePaths, String extension) {
//        if (filePaths != null) {
//            for (String filePath : filePaths) {
//                if (filePath.toLowerCase().endsWith("." + extension.toLowerCase())) {
//                    if (Files.exists(Paths.get(filePath))) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    public static String[] splitWithTrim(String delimiter, @NonNull String source) {
        String[] parts = source.split(delimiter);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

//    public static String solveClasspath(String filePath) {
//        String prefix = "classpath:";
//        if (filePath.startsWith(prefix)) {
//            String subStr = filePath.substring(prefix.length());
//            URL resource = App.class.getClassLoader().getResource(subStr);
//
//            return resource == null ? null : resource.getPath();
//        }
//        return filePath;
//    }

//    public static String[] solveClasspath(String[] filePaths) {
//        String[] newFilePaths = new String[filePaths.length];
//        for (int i = 0; i < filePaths.length; i++) {
//            newFilePaths[i] = solveClasspath(filePaths[i]);
//        }
//        return newFilePaths;
//    }

    public static String getProjectLaunchSourcePath() {
        return App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public static String getProjectLaunchSourceName() {
        return Paths.get(getProjectLaunchSourcePath()).getFileName().toString();
    }

    public static boolean isLaunchedFromJAR() {
        return getProjectLaunchSourceName().toLowerCase().endsWith(".jar");
    }
}
