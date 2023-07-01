package com.pavbatol.gjcc.converter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pavbatol.gjcc.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FileDataLoader {
    private static final String CL_RESET = "\u001B[0m";
    private static final String CL_RED = "\u001B[31m";
    private static final String CL_YELLOW = "\u001B[33m";
    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = ";";
    private static final String RESET_SIGNAL = "----";
    private static final String STOP_SIGNAL = "XXXX";
    private static final String RESET_COMMAND_RECEIVED = "Reset command received";
    private static final String EXIT_COMMAND_RECEIVED = "Exit command received";
    private static final int INITIAL_CAPACITY = 100;
    private static final String OUTPUT_FILE = "output.csv";
    private static final String OUTPUT_DIR = "output";
    public static final String TO_SKIP_FIELD = "-";
    public static final String TO_SKIP_REMAINING_FIELDS = "--";
    public static final String TO_LOAD_REMAINING_FIELDS = "++";
    public static final String TO_LEAVE_AS_IS_FIELD = "";
    public static final String FIELD_LONGITUDE = "longitude";
    public static final String FIELD_LATITUDE = "latitude";
    private final Properties properties = AppConfig.getInstance().getProperty();
    private final String sourceFilePath = properties.getProperty("app.data.file-path");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Scanner scanner = new Scanner(System.in);
    private boolean allFields;
    private boolean specifiedFields;
    private int nextFieldIndex;
    private List<String> csvLineParts;
    private Map<String, Field> fields;
    private Set<String> nodeIds;
    private StringBuilder builder;
    private boolean loadRemainingFields;
    private boolean skipRemainingFields;
    Integer linesLimit;

    /**
     * @param filePaths Paths to json format files from which to read data
     */
    private void loadCities(String... filePaths) {
        Path pathOut = Paths.get(OUTPUT_DIR, OUTPUT_FILE);
        creatDirectoryIfNotExists(pathOut.getParent());

        while (true) {
            builder = new StringBuilder();
            csvLineParts = new ArrayList<>(INITIAL_CAPACITY);
            fields = new HashMap<>((int) (INITIAL_CAPACITY / 0.75) + 1, 0.75f);
            nodeIds = new HashSet<>();
            nextFieldIndex = 0;
            loadRemainingFields = false;
            skipRemainingFields = false;

            exitMenu();

            while (true) {
                entitiesLoadLimitMenu();
                String entitiesLoadLimit = scanner.nextLine().trim();
                if (STOP_SIGNAL.equals(entitiesLoadLimit)) {
                    return;
                } else if (RESET_SIGNAL.equals(entitiesLoadLimit)) {
                    continue;
                }
                try {
                    linesLimit = "".equals(entitiesLoadLimit) ? null : Integer.parseInt(entitiesLoadLimit);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(errorStr() + ": The entered value is not a number");
                }
            }

            allFieldsMenu();
            String allFieldsInput = scanner.nextLine().trim();
            if (STOP_SIGNAL.equals(allFieldsInput)) {
                return;
            } else if (RESET_SIGNAL.equals(allFieldsInput)) {
                continue;
            }
            defineWayOfLoadingFields(allFieldsInput);
            skipRemainingFields = specifiedFields;

            deleteFile(pathOut);
            try (BufferedWriter writer = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE)
            ) {
                ReturnStatus status = null;
                for (String filePath : filePaths) {
                    Path path = Path.of(filePath.trim());
                    log.debug("Path to loud features: {}", path);

                    JsonFactory jsonFactory = objectMapper.getFactory();
                    try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(path.toString()))
                    ) {
                        status = parse(jsonParser);
                        if (status != ReturnStatus.OK) {
                            log.debug(status == ReturnStatus.RESET ? RESET_COMMAND_RECEIVED : EXIT_COMMAND_RECEIVED);
                            break;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (status == ReturnStatus.RESET) {
                    continue;
                } else if (status == ReturnStatus.STOP) {
                    return;
                }

                if (fields.values().size() > 0) {
                    csvLineParts.clear();
                    controlLinePartsSize();
                    fields.values().stream()
                            .filter(Objects::nonNull)
                            .filter(field -> Objects.nonNull(field.getName()))
                            .filter(field -> Objects.nonNull(field.getIndex()))
                            .forEach(field -> csvLineParts.set(field.getIndex(), replaceDelimiter(field.getName())));

                    // Titles-line
                    writer.write((String.join(DELIMITER, csvLineParts) + "\n"));

                    // Body-lines
                    writer.write(builder.toString());
                }

                log.debug("Total number of fields: {}", fields.size());
                log.debug("Total number of selected fields: {}", nextFieldIndex);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ReturnStatus parse(JsonParser jsonParser) throws IOException {
        ReturnStatus status = null;
        while (jsonParser.nextToken() != null) {
            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME && "features".equals(jsonParser.currentName())) {
                int count = 0;
                while (jsonParser.nextToken() != JsonToken.END_ARRAY && (linesLimit == null || count < linesLimit)) {
                    count++;
                    status = parseTarget(jsonParser);
                    if (status != ReturnStatus.OK) {
                        log.debug(status == ReturnStatus.RESET ? RESET_COMMAND_RECEIVED : EXIT_COMMAND_RECEIVED);
                        break;
                    }
                }
                if (status != ReturnStatus.OK) {
                    return status;
                }
                log.debug("Loaded features number: {}", count); // TODO: 01.07.2023 Consider not loading  duplicates
            }
        }
        return ReturnStatus.OK;
    }

    private ReturnStatus parseTarget(JsonParser jsonParser) throws IOException {
        csvLineParts.clear();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            if (jsonParser.currentToken() != JsonToken.FIELD_NAME) {
                continue;
            }
            String featureValue;
            String subFieldName = jsonParser.getCurrentName();
            jsonParser.nextToken();
            switch (subFieldName) {
                case "id":
                    featureValue = jsonParser.getValueAsString();
                    Field field = getFieldOrCreat(subFieldName);

                    // Duplicated ID
                    if (!nodeIds.add(featureValue)) {
                        return ReturnStatus.OK;
                    }

                    setCsvLinePart(field.getIndex(), featureValue);
                    break;
                case "properties":
                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String propsFieldName = jsonParser.getCurrentName();
                        jsonParser.nextToken();
                        featureValue = jsonParser.getValueAsString();

                        if (propsFieldName.startsWith("name:")
                                && !"name:ru".equals(propsFieldName)
                                && !"name:en".equals(propsFieldName)
                                && !"name:".equals(propsFieldName)) {
                            continue;
                        }

                        if (!fields.containsKey(propsFieldName)) {
                            if (allFields || loadRemainingFields) {
                                fields.put(propsFieldName, new Field(propsFieldName, getAndIncrementNextFieldIndex()));
                            } else if (skipRemainingFields) {
                                fields.put(propsFieldName, null);
                            } else {
                                ReturnStatus status = detectedAndKeepField(propsFieldName, featureValue);
                                if (status != ReturnStatus.OK) {
                                    return status;
                                }
                            }
                        }

                        if (fields.get(propsFieldName) != null) {
                            setCsvLinePart(fields.get(propsFieldName).getIndex(), featureValue);
                        }
                    }
                    break;
                case "geometry":
                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String geomFieldName = jsonParser.getCurrentName();
                        jsonParser.nextToken();

                        if ("coordinates".equals(geomFieldName)) {
                            double featureLongitude = .0;
                            double featureLatitude = .0;
                            int i = 0;
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                double coord = jsonParser.getDoubleValue();
                                if (i == 0) {
                                    featureLongitude = coord;
                                } else {
                                    featureLatitude = coord;
                                }
                                i++;
                            }

                            keepField(FIELD_LONGITUDE, String.valueOf(featureLongitude));
                            keepField(FIELD_LATITUDE, String.valueOf(featureLatitude));
                        }
                    }
                    break;
            }
        }

        // String for CSV file
        String newCsvLine = csvLineParts.stream()
                .map(s -> s == null ? TO_LEAVE_AS_IS_FIELD : s)
                .map(this::replaceDelimiter)
                .collect(Collectors.joining(DELIMITER)) + "\n";
        builder.append(newCsvLine);

        return ReturnStatus.OK;
    }

    private Field creatField(final String fieldName) {
        return new Field(fieldName, getAndIncrementNextFieldIndex());
    }

    private Field getFieldOrCreat(final String fieldName) {
        return fields.computeIfAbsent(fieldName, this::creatField);
    }

    private void keepField(final String fieldName, String value) {
        Field field = getFieldOrCreat(fieldName);
        setCsvLinePart(field.getIndex(), value);
    }

    private ReturnStatus detectedAndKeepField(final String propsFieldName, final String featureValueExamole) {
        fieldDetectedMenu(propsFieldName, featureValueExamole);
        String customName = scanner.nextLine().trim();

        switch (customName) {
            case STOP_SIGNAL -> {
                log.debug(EXIT_COMMAND_RECEIVED);
                return ReturnStatus.STOP;
            }
            case RESET_SIGNAL -> {
                log.debug(RESET_COMMAND_RECEIVED);
                return ReturnStatus.RESET;
            }
            case TO_SKIP_REMAINING_FIELDS -> {
                skipRemainingFields = true;
                customName = TO_SKIP_FIELD;
            }
            case TO_LOAD_REMAINING_FIELDS -> {
                loadRemainingFields = true;
                customName = TO_LEAVE_AS_IS_FIELD;
            }
        }

        if (TO_LEAVE_AS_IS_FIELD.equals(customName)) {
            fields.put(propsFieldName, new Field(propsFieldName, getAndIncrementNextFieldIndex()));
        } else if (TO_SKIP_FIELD.equals(customName)) {
            fields.put(propsFieldName, null);
        } else {
            fields.put(propsFieldName, new Field(customName, getAndIncrementNextFieldIndex()));
        }
        return ReturnStatus.OK;
    }

    private int getAndIncrementNextFieldIndex() {
        return nextFieldIndex++;
    }

    private String replaceDelimiter(String str) {
        return str.replace(DELIMITER, DELIMITER_REPLACEMENT);
    }

    private void setCsvLinePart(int index, String part) {
        controlLinePartsSize();
        csvLineParts.set(index, part);
    }

    private void controlLinePartsSize() {
        if (csvLineParts.size() < nextFieldIndex) {
            increaseListSizeBy(nextFieldIndex - csvLineParts.size(), csvLineParts);
        }
    }

    private void increaseListSizeBy(int increase, List<String> list) {
        for (int i = 0; i < increase; i++) {
            list.add(null);
        }
    }

    private void creatDirectoryIfNotExists(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Directory creation error");
            }
        }
    }

    private void deleteFile(Path path) {
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

    private void defineWayOfLoadingFields(String allFieldsInput) {
        switch (allFieldsInput) {
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
                String[] inputFields = allFieldsInput.split(",");
                for (String fieldName : inputFields) {
                    fieldName = fieldName.trim();
                    fields.put(fieldName, new Field(fieldName, getAndIncrementNextFieldIndex()));
                }
            }
        }
    }

    private static void exitMenu() {
        System.out.println("-----------------------");
        System.out.println("At any stage you can enter:");
        System.out.println("\"" + RESET_SIGNAL + "\" : start over");
        System.out.println("\"" + STOP_SIGNAL + "\" : exit");
        System.out.println("-----------------------");
    }

    private void fieldDetectedMenu(final String fieldName, final String examole) {
        System.out.println(noticeStr() + "\nField detected: \"" + fieldName + "\" (value example: " + examole + ")");
        System.out.printf("\tPress Enter to leave this field as is; \n" +
                        "\tOr enter your field name\n" +
                        "\tOr enter \"%s\" to skip the field\n" +
                        "\tOr enter \"%s\" to skip all remaining fields\n" +
                        "\tOr enter \"%s\" to load all remaining fields as is\n",
                TO_SKIP_FIELD, TO_SKIP_REMAINING_FIELDS, TO_LOAD_REMAINING_FIELDS);
    }

    private void allFieldsMenu() {
        System.out.println(noticeStr() + "\nWhich fields to save? (The following fields will always be loaded: id, longitude, latitude)");
        System.out.printf("\t%-11s : %s%n", "Selectively", "0");
        System.out.printf("\t%-11s : %s%n", "All", "1");
        System.out.printf("\t%-11s : %s%n", "Specified", "specify the field names separated by commas " +
                "(take the fields from features[]->properties object from your GEOJSON file)");
    }

    private void entitiesLoadLimitMenu() {
        System.out.println(noticeStr() + "\nHow many features (entities) to load from each source file?");
        System.out.printf("\t%-11s : %s%n", "Limit", "enter number");
        System.out.printf("\t%-11s : %s%n", "All", "press enter");
    }

    private String errorStr() {
        return CL_RED + "Error" + CL_RESET;
    }

    private String noticeStr() {
        return CL_YELLOW + "**" + CL_RESET;
    }

    public void run() {
        loadCities(sourceFilePath.split(","));
    }
}
