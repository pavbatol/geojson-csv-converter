package com.pavbatol.gjcc.converter;

import com.pavbatol.gjcc.config.Props;
import com.pavbatol.gjcc.field.FieldAction;
import com.pavbatol.gjcc.returns.*;
import com.pavbatol.gjcc.returns.impl.ReturnArrayData;
import com.pavbatol.gjcc.returns.impl.ReturnDetectedFieldData;
import com.pavbatol.gjcc.returns.impl.ReturnIntegerData;
import com.pavbatol.gjcc.returns.impl.ReturnLoadingFieldsWayData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import static com.pavbatol.gjcc.converter.Utils.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Menu {
    private static final String CL_RESET = "\u001B[0m";
    private static final String CL_RED = "\u001B[31m";
    private static final String CL_YELLOW = "\u001B[33m";
    private static final String RESET_SIGNAL = "----";
    private static final String STOP_SIGNAL = "!!!!";
    private static final String TO_SKIP_FIELD = "-";
    private static final String TO_LEAVE_AS_IS_FIELD = "";
    private static final String TO_SKIP_REMAINING_FIELDS = "--";
    private static final String TO_LOAD_REMAINING_FIELDS = "++";
    private static final String TO_EMPTY = "";
    private static final String TO_ZERO = "0";
    private static final String TO_ONE = "1";
    private static final String GEOJSON_EXTENSION = "GEOJSON";
    private static final String OUTPUT_DIR = Props.DATA_DIRECTORY_INPUT_DEFAULT.getValue();
    private static final String INPUT_GENERATED_DIR = Props.DATA_DIRECTORY_INPUT_GENERATED.getValue();

    public static void exit() {
        exitMenu();
    }

    public static ReturnArrayData directory(@NonNull Scanner scanner, @NonNull String[] initialFilePaths) {
        while (true) {
            directoryMenu();
            final String input = scanner.nextLine().trim();
            if (STOP_SIGNAL.equals(input)) {
                return ReturnArrayData.of(ReturnStatus.STOP);
            } else if (RESET_SIGNAL.equals(input)) {
                return ReturnArrayData.of(ReturnStatus.RESET);
            }

            final String prefix = getResourcePathPrefix();
            String inputDir;
            switch (input) {
                case TO_EMPTY:
                    if (isLaunchedFromJAR()) {
                        log.debug("The application is launched from the JAR archive");

                        inputDir = INPUT_GENERATED_DIR;
                        creatDirectoryIfNotExists(Path.of(inputDir));

                        for (String initialFilePath : initialFilePaths) {
                            if (!initialFilePath.startsWith(prefix)) {
                                continue;
                            }
                            final String outputDir = inputDir;
                            final String inputFileName = initialFilePath.substring(prefix.length());
                            final String outputFileName = Path.of(outputDir, Path.of(inputFileName).getFileName().toString()).toString();
                            copyResource(inputFileName, outputFileName);
                        }
                    } else {
                        log.debug("The application is launched from the development environment");
                        inputDir = null;
                    }
                    break;
                case TO_ZERO:
                    inputDir = creatIfNotAndGetInputDefaultDir();
                    break;
                default:
                    inputDir = input;
            }

            try {
                final String[] filePaths = inputDir == null
                        ? getExistingFiles(relativePathToAbsolute(initialFilePaths))
                        : getFilePathArrayByExtension(inputDir, GEOJSON_EXTENSION);

                if (inputDir == null && !fileExistsByExtension(filePaths, GEOJSON_EXTENSION)) {
                    System.out.printf("%s: No files with the %s extension found in the environment variable\n", warnStr(), GEOJSON_EXTENSION);
                } else if (filePaths.length == 0) {
                    System.out.printf("%s: No files with the %s extension found in the directory: %s\n", warnStr(), GEOJSON_EXTENSION, inputDir);
                } else {
                    System.out.println("Found files: " + filePaths.length + ":");
                    for (String filePath : filePaths) {
                        System.out.println(filePath);
                    }

                    return new ReturnArrayData(ReturnStatus.OK, filePaths);
                }
            } catch (IOException e) {
                System.out.println(errorStr() + ": Failed to access the directory: " + inputDir);
            }
        }
    }

    public static ReturnIntegerData limit(@NonNull Scanner scanner) {
        while (true) {
            entitiesLoadLimitMenu();
            String input = scanner.nextLine().trim();
            if (STOP_SIGNAL.equals(input)) {
                return ReturnIntegerData.of(ReturnStatus.STOP);
            } else if (RESET_SIGNAL.equals(input)) {
                return ReturnIntegerData.of(ReturnStatus.RESET);
            }
            try {
                Integer linesLimit = "".equals(input) ? null : Integer.parseInt(input);
                return new ReturnIntegerData(ReturnStatus.OK, linesLimit);
            } catch (NumberFormatException e) {
                System.out.println(errorStr() + ": The entered value is not a number");
            }
        }
    }

    public static ReturnLoadingFieldsWayData fields(@NonNull Scanner scanner) {
        chooseFieldsMenu();
        String input = scanner.nextLine().trim();
        if (STOP_SIGNAL.equals(input)) {
            return ReturnLoadingFieldsWayData.of(ReturnStatus.STOP);
        } else if (RESET_SIGNAL.equals(input)) {
            return ReturnLoadingFieldsWayData.of(ReturnStatus.RESET);
        }

        boolean allFields;
        boolean specifiedFields;
        String[] inputFields = null;
        switch (input) {
            case TO_ZERO -> {
                allFields = false;
                specifiedFields = false;
            }
            case TO_ONE -> {
                allFields = true;
                specifiedFields = false;
            }
            default -> {
                allFields = false;
                specifiedFields = true;
                inputFields = input.split(",");
            }
        }
        return new ReturnLoadingFieldsWayData(ReturnStatus.OK, allFields, specifiedFields, inputFields);
    }

    public static ReturnDetectedFieldData solveField(@NonNull Scanner scanner,
                                                     final String propsFieldName,
                                                     final String featureValueExamole) {
        fieldDetectedMenu(propsFieldName, featureValueExamole);
        String input = scanner.nextLine().trim();

        switch (input) {
            case STOP_SIGNAL -> {
                return ReturnDetectedFieldData.of(ReturnStatus.STOP);
            }
            case RESET_SIGNAL -> {
                return ReturnDetectedFieldData.of(ReturnStatus.RESET);
            }
            case TO_SKIP_REMAINING_FIELDS -> {
                return new ReturnDetectedFieldData(ReturnStatus.OK, FieldAction.SKIP_FIELD,
                        true, null);
            }
            case TO_LOAD_REMAINING_FIELDS -> {
                return new ReturnDetectedFieldData(ReturnStatus.OK, FieldAction.AS_IS_NAME.setName(propsFieldName),
                        null, true);
            }
            case TO_SKIP_FIELD -> {
                return new ReturnDetectedFieldData(ReturnStatus.OK, FieldAction.SKIP_FIELD,
                        null, null);
            }
            case TO_LEAVE_AS_IS_FIELD -> {
                return new ReturnDetectedFieldData(ReturnStatus.OK, FieldAction.AS_IS_NAME.setName(propsFieldName),
                        null, null);
            }
            default -> {
                return new ReturnDetectedFieldData(ReturnStatus.OK, FieldAction.CUSTOM_NAME.setName(input),
                        null, null);
            }
        }
    }

    private static void exitMenu() {
        System.out.println("-----------------------");
        System.out.println("At any stage you can:");
        System.out.printf("%-10s : %s%n", "start over", "\"" + RESET_SIGNAL + "\"");
        System.out.printf("%-10s : %s%n", "exit", "\"" + STOP_SIGNAL + "\"");
        System.out.println("-----------------------");
    }

    private static void directoryMenu() {
        System.out.println(noticeStr() + "\nIn which directory are the source files located?");
        System.out.printf("\t%-14s : %s%n", "In preset data", "press enter (contained in the application.properties " +
                "in the variable \"" + Props.DATA_PRESET_FILE_PATHS.getKey() + "\")");
        System.out.printf("\t%-14s : %s%n", "In default dir", "enter " + TO_ZERO + " (The directory is located " +
                "next to the application file: " + OUTPUT_DIR);
        System.out.printf("\t%-14s : %s%n", "In custom dir", "enter your absolute path to directory");
    }

    private static void entitiesLoadLimitMenu() {
        System.out.println(noticeStr() + "\nHow many features (entities) to load from each source file?");
        System.out.printf("\t%-14s : %s%n", "Limit", "enter number");
        System.out.printf("\t%-14s : %s%n", "All", "press enter");
    }

    private static void chooseFieldsMenu() {
        System.out.println(noticeStr() + "\nWhich fields to save? (The following fields will always be loaded: id, longitude, latitude)");
        System.out.printf("\t%-14s : %s%n", "Selectively", TO_ZERO);
        System.out.printf("\t%-14s : %s%n", "All", TO_ONE + " (notice, there can be a lot of fields)");
        System.out.printf("\t%-14s : %s%n", "Specified", "specify the field names, separated by commas " +
                "(take the fields from features[]->properties object from your " + GEOJSON_EXTENSION + " file)");
    }

    private static void fieldDetectedMenu(final String fieldName, final String examole) {
        System.out.println(noticeStr() + "\nField detected: \"" + fieldName + "\" (value example: " + examole + ")");
        System.out.printf("\tPress Enter to leave this field as is; \n" +
                        "\tOr enter your field name\n" +
                        "\tOr enter \"%s\" to skip the field\n" +
                        "\tOr enter \"%s\" to skip all remaining fields\n" +
                        "\tOr enter \"%s\" to load all remaining fields as is\n",
                TO_SKIP_FIELD, TO_SKIP_REMAINING_FIELDS, TO_LOAD_REMAINING_FIELDS);
    }

    private static String errorStr() {
        return CL_RED + "Error" + CL_RESET;
    }

    private static String noticeStr() {
        return CL_YELLOW + "**" + CL_RESET;
    }

    private static String warnStr() {
        return CL_YELLOW + "Warn" + CL_RESET;
    }
}
