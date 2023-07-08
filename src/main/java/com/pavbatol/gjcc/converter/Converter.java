package com.pavbatol.gjcc.converter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pavbatol.gjcc.Menu;
import com.pavbatol.gjcc.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static com.pavbatol.gjcc.converter.Utils.*;

@Slf4j
public class Converter {
    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = ";";
    private static final String RESET_COMMAND_RECEIVED = "Reset command received";
    private static final String EXIT_COMMAND_RECEIVED = "Exit command received";
    private static final int INITIAL_CAPACITY = 100;
    private static final String OUTPUT_FILE = "output.csv";
    private static final String OUTPUT_DIR = AppConfig.getInstance().getProperty("app.data.directory.output");
    private static final String FIELD_LONGITUDE = "longitude";
    private static final String FIELD_LATITUDE = "latitude";
    private final String sourceFilePath = AppConfig.getInstance().getProperty("app.data.file-path");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Scanner scanner = new Scanner(System.in);
    private boolean allFields;
    private int nextFieldIndex;
    private List<String> csvLineParts;
    private Map<String, Field> fields;
    private Set<String> nodeIds;
    private StringBuilder builder;
    private boolean loadRemainingFields;
    private boolean skipRemainingFields;
    Integer linesLimit;

    private void convertToCsv() {
        Path pathOut = Paths.get(OUTPUT_DIR, OUTPUT_FILE);
        creatDirectoryIfNotExists(pathOut.getParent());
        creatIfNotAndGetInputDefaultDir();

        while (true) {
            builder = new StringBuilder();
            csvLineParts = new ArrayList<>(INITIAL_CAPACITY);
            fields = new HashMap<>((int) (INITIAL_CAPACITY / 0.75) + 1, 0.75f);
            nodeIds = new HashSet<>();
            nextFieldIndex = 0;
            loadRemainingFields = false;
            skipRemainingFields = false;
            String[] filePaths = null;

            //---
            Menu.exit();

            //---
            String[] initialFilePaths = sourceFilePath == null ? null : splitWithTrim(",", sourceFilePath);
            ReturnArrayData arrayData = Menu.directory(scanner, initialFilePaths);
            if (arrayData.getStatus() == ReturnStatus.STOP) {
                return;
            } else if (arrayData.getStatus() == ReturnStatus.RESET) {
                continue;
            }
            filePaths = arrayData.getValues();

            //---
            ReturnIntegerData integerData = Menu.limit(scanner);
            if (integerData.getStatus() == ReturnStatus.STOP) {
                return;
            } else if (integerData.getStatus() == ReturnStatus.RESET) {
                continue;
            }
            linesLimit = integerData.getValue();

            //---
            ReturnLoadingFildsWayData loadingFieldsWayData = Menu.fields(scanner);
            if (loadingFieldsWayData.getStatus() == ReturnStatus.STOP) {
                return;
            } else if (loadingFieldsWayData.getStatus() == ReturnStatus.RESET) {
                continue;
            }
            allFields = loadingFieldsWayData.getAllFields();
            skipRemainingFields = loadingFieldsWayData.getSpecifiedFields();
            if (loadingFieldsWayData.getInputFields() != null) {
                Arrays.stream(loadingFieldsWayData.getInputFields())
                        .map(String::trim)
                        .forEach(fieldName -> fields.put(fieldName, creatField(fieldName)));
            }

            //---
            deleteFile(pathOut);
            try (BufferedWriter writer = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE)
            ) {
                ReturnStatus status = null;
                for (String filePath : filePaths) {
                    Path path = Path.of(filePath.trim()); // TODO: 05.07.2023 filePaths can be NULL or not exists
                    log.info("Path to loud features: {}", path);

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

                log.info("Total number of fields: {}", fields.size());
                log.info("Total number of selected fields: {}", nextFieldIndex);

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
                log.info("Loaded features number: {}", count); // TODO: 01.07.2023 Consider not loading  duplicates
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

                        if (excludedField(propsFieldName)) {
                            continue;
                        }

                        if (!fields.containsKey(propsFieldName)) {
                            if (allFields || loadRemainingFields) {
                                fields.put(propsFieldName, creatField(propsFieldName));
                            } else if (skipRemainingFields) {
                                fields.put(propsFieldName, null);
                            } else {
                                ReturnDetectedFieldData solveFieldData = Menu.solveField(scanner, propsFieldName, featureValue);
                                ReturnStatus status = solveFieldData.getStatus();
                                if (status != ReturnStatus.OK) {
                                    log.debug(status == ReturnStatus.RESET ? RESET_COMMAND_RECEIVED : EXIT_COMMAND_RECEIVED);
                                    return status;
                                }
                                skipRemainingFields = getBoolean(skipRemainingFields, solveFieldData.getSkipRemainingFields());
                                loadRemainingFields = getBoolean(loadRemainingFields, solveFieldData.getLoadRemainingFields());
                                FieldAction action = solveFieldData.getFieldAction();
                                fields.put(propsFieldName, action == FieldAction.SKIP_FIELD ? null : creatField(action.getName()));
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
                .map(s -> s == null ? "" : s)
                .map(this::replaceDelimiter)
                .collect(Collectors.joining(DELIMITER)) + "\n";
        builder.append(newCsvLine);

        return ReturnStatus.OK;
    }

    private boolean getBoolean(boolean target, Boolean source) {
        return source == null ? target : source;
    }

    private boolean excludedField(String fieldName) {
        return (fieldName.startsWith("name:")
                && !"name:ru".equals(fieldName)
                && !"name:en".equals(fieldName)
                && !"name:".equals(fieldName));
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

    public void run() {
        convertToCsv();
    }
}
