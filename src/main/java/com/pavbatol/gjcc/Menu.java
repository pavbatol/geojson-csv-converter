package com.pavbatol.gjcc;

import com.pavbatol.gjcc.converter.ReturnArrayData;
import com.pavbatol.gjcc.converter.ReturnIntegerData;
import com.pavbatol.gjcc.converter.ReturnLoadingFildsWayData;
import com.pavbatol.gjcc.converter.ReturnStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;
import java.util.Scanner;

import static com.pavbatol.gjcc.converter.Utils.fileExistsByExtension;
import static com.pavbatol.gjcc.converter.Utils.getFilePathArrayByExtension;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Menu {
    private static final String CL_RESET = "\u001B[0m";
    private static final String CL_RED = "\u001B[31m";
    private static final String CL_YELLOW = "\u001B[33m";
    private static final String RESET_SIGNAL = "----";
    private static final String STOP_SIGNAL = "XXXX";
    private static final String GEOJSON_EXTENSION = "GEOJSON";

    public static void exit() {
        exitMenu();
    }

    public static ReturnArrayData directory(@NonNull Scanner scanner, String[] initialFilePaths) {
        while (true) {
            directoryMenu();
            String input = scanner.nextLine().trim();
            if (STOP_SIGNAL.equals(input)) {
                return new ReturnArrayData(ReturnStatus.STOP, null);
            } else if (RESET_SIGNAL.equals(input)) {
                return new ReturnArrayData(ReturnStatus.RESET, null);
            }
            try {
                String[] filePaths = "".equals(input) ? initialFilePaths : getFilePathArrayByExtension(input, GEOJSON_EXTENSION);

                if (filePaths == initialFilePaths && !fileExistsByExtension(initialFilePaths, GEOJSON_EXTENSION)) {
                    System.out.printf("%s: No files with the %s extension found in the environment variable\n", warnStr(), GEOJSON_EXTENSION);
                } else if (filePaths.length == 0) {
                    System.out.printf("%s: No files with the %s extension found in the directory: %s\n", warnStr(), GEOJSON_EXTENSION, input);
                } else {
                    System.out.println("Found files: " + filePaths.length);
                    for (String filePath : filePaths) {
                        System.out.println(filePath);
                    }

                    return new ReturnArrayData(ReturnStatus.OK, filePaths);
                }
            } catch (IOException e) {
                System.out.println(errorStr() + ": Failed to access the directory: " + input);
            }
        }
    }

    public static ReturnIntegerData limit(@NonNull Scanner scanner) {
        while (true) {
            entitiesLoadLimitMenu();
            String input = scanner.nextLine().trim();
            if (STOP_SIGNAL.equals(input)) {
                return new ReturnIntegerData(ReturnStatus.STOP, null);
            } else if (RESET_SIGNAL.equals(input)) {
                return new ReturnIntegerData(ReturnStatus.RESET, null);
            }
            try {
                Integer linesLimit = "".equals(input) ? null : Integer.parseInt(input);
                return new ReturnIntegerData(ReturnStatus.OK, linesLimit);
            } catch (NumberFormatException e) {
                System.out.println(errorStr() + ": The entered value is not a number");
            }
        }
    }

    public static ReturnLoadingFildsWayData fields(@NonNull Scanner scanner) {
        chooseFieldsMenu();
        String input = scanner.nextLine().trim();
        if (STOP_SIGNAL.equals(input)) {
            return ReturnLoadingFildsWayData.of(ReturnStatus.STOP);
        } else if (RESET_SIGNAL.equals(input)) {
            return ReturnLoadingFildsWayData.of(ReturnStatus.RESET);
        }

        boolean allFields;
        boolean specifiedFields;
        String[] inputFields = null;
        switch (input) {
            case "0" -> {
                allFields = false;
                specifiedFields = false;
            }
            case "1" -> {
                allFields = true;
                specifiedFields = false;
            }
            default -> {
                allFields = false;
                specifiedFields = true;
                inputFields = input.split(",");
            }
        }
        return new ReturnLoadingFildsWayData(ReturnStatus.OK, allFields, specifiedFields, inputFields);
    }

    private static void exitMenu() {
        System.out.println("-----------------------");
        System.out.println("At any stage you can enter:");
        System.out.println("\"" + RESET_SIGNAL + "\" : start over");
        System.out.println("\"" + STOP_SIGNAL + "\" : exit");
        System.out.println("-----------------------");
    }

    private static void directoryMenu() {
        System.out.println(noticeStr() + "\nIn which directory are the source files located?");
        System.out.printf("\t%-11s : %s%n", "In project", "press enter (contained in the variable by getProperty(\"app.data.file-path\"))");
        System.out.printf("\t%-11s : %s%n", "In custom ", "enter your absolute path to directory");
    }

    private static void entitiesLoadLimitMenu() {
        System.out.println(noticeStr() + "\nHow many features (entities) to load from each source file?");
        System.out.printf("\t%-11s : %s%n", "Limit", "enter number");
        System.out.printf("\t%-11s : %s%n", "All", "press enter");
    }

    private static void chooseFieldsMenu() {
        System.out.println(noticeStr() + "\nWhich fields to save? (The following fields will always be loaded: id, longitude, latitude)");
        System.out.printf("\t%-11s : %s%n", "Selectively", "0");
        System.out.printf("\t%-11s : %s%n", "All", "1 (notice, there can be a lot of fields)");
        System.out.printf("\t%-11s : %s%n", "Specified", "specify the field names, separated by commas " +
                "(take the fields from features[]->properties object from your " + GEOJSON_EXTENSION + " file)");
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
