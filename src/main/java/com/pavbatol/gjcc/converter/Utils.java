package com.pavbatol.gjcc.converter;

import com.pavbatol.gjcc.App;
import com.pavbatol.gjcc.config.Props;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {
    private static final String INPUT_DEFAULT_DIR = Props.DATA_DIRECTORY_INPUT_DEFAULT.getValue();
    private static final String CLASSPATH = "classpath:";
    private static final String USER_DIR = "user.dir";

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
                log.debug("File not found: {}", e.getMessage());
            } catch (IOException e) {
                log.error("Error deleting a file: {}", e.getMessage());
            }
        }
    }

    public static String creatIfNotAndGetInputDefaultDir() {
        String dir = INPUT_DEFAULT_DIR;
        creatDirectoryIfNotExists(Path.of(dir));
        return dir;
    }

    public static boolean checkDuplicateFile(@NonNull Path inputFileName, @NonNull Path outputFileName) throws IOException {
        if (Files.exists(outputFileName)) {
            return Files.size(inputFileName) == Files.size(outputFileName);
        }
        return false;
    }

    public static void copyResource(final String inputFileName, final String outputFileName) {
        final String zipFilePath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(Paths.get(zipFilePath), (ClassLoader) null)) {
            final Path entryFile = zipFileSystem.getPath(inputFileName);

            if (Files.exists(entryFile)) {
                Path outputFile = Paths.get(outputFileName);

                if (checkDuplicateFile(entryFile, outputFile)) {
                    log.debug("The file {} already exists and has the same size as {}", outputFileName, inputFileName);
                    return;
                }

                try (InputStream inputStream = Files.newInputStream(entryFile);
                     OutputStream outputStream = Files.newOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                log.debug("Successfully copied from resource: {}, to file: {}", entryFile, outputFileName);
            } else {
                log.info("File not found inside the JAR archive.");
            }
        } catch (IOException e) {
            log.warn("Error copying the file: {}", e.getMessage());
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

    public static boolean fileExistsByExtension(String[] filePaths, String extension) {
        if (filePaths != null) {
            for (String filePath : filePaths) {
                if (filePath.toLowerCase().endsWith("." + extension.toLowerCase())) {
                    if (Files.exists(Paths.get(filePath))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String[] splitWithTrim(String delimiter, @NonNull String source) {
        String[] parts = source.split(delimiter);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    public static Optional<String> relativePathToAbsolute(@NonNull String filePath) {
        String prefix = getResourcePathPrefix();
        if (filePath.startsWith(prefix)) {
            String withoutPrefix = filePath.substring(prefix.length());
            URL resource = App.class.getClassLoader().getResource(withoutPrefix);
            return resource == null ? Optional.empty() : Optional.of(resource.getPath());
        } else if (!Path.of(filePath).isAbsolute()) {
            String userDir = System.getProperty(USER_DIR);
            return userDir == null ? Optional.empty() : Optional.of(Path.of(userDir, filePath).toString());
        } else {
            return Optional.of(filePath);
        }
    }

    public static String getResourcePathPrefix() {
        return CLASSPATH;
    }

    public static String[] relativePathToAbsolute(String[] filePaths) {
        String[] newFilePaths = new String[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            if (filePaths[i] != null) {
                int finalI = i;
                relativePathToAbsolute(filePaths[i]).ifPresentOrElse(
                        newFilePath -> newFilePaths[finalI] = newFilePath,
                        () -> log.debug("Failed to process the resource: " + filePaths[finalI])
                );
            } else {
                log.debug("Ignoring element with null value");
            }
        }
        return newFilePaths;
    }

    public static String[] getExistingFiles(String[] filePaths) {
        return Arrays.stream(filePaths)
                .filter(filePath -> Files.isRegularFile(Path.of(filePath)))
                .filter(filePath -> Files.exists(Path.of(filePath)))
                .toList().toArray(new String[]{});
    }

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
