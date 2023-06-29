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
    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = ";";
    private static final String RESET_SIGNAL = "---";
    private static final String STOP_SIGNAL = "XXX";
    private static final String RESET_COMMAND_RECEIVED = "Reset command received";
    private static final String EXIT_COMMAND_RECEIVED = "Exit command received";
    private static final int INITIAL_CAPACITY = 100;
    private static final String OUTPUT_FILE = "output.csv";
    private static final String OUTPUT_DIR = "output";
    private final Properties properties = AppConfig.getInstance().getProperty();
    private final String sourceFilePath = properties.getProperty("app.data.file-path");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Scanner scanner = new Scanner(System.in);
    private boolean allFields;
    private int nextFieldIndex;
    private List<String> csvLineParts;
    private Map<String, Field> fields;
    private Set<String> nodeIds;
    private StringBuilder builder;

    /**
     * @param limit     Number of entries in the file. Set null for all records.
     * @param filePaths Paths to json format files from which to read data
     */
    private void loadCities(final Integer limit, String... filePaths) {
        Path pathOut = Paths.get(OUTPUT_DIR, OUTPUT_FILE);
        creatDirectoryIfNotExists(pathOut.getParent());

        while (true) {
            builder = new StringBuilder();
            csvLineParts = new ArrayList<>(INITIAL_CAPACITY);
            fields = new HashMap<>((int) (INITIAL_CAPACITY / 0.75) + 1, 0.75f);
            nodeIds = new HashSet<>();
            nextFieldIndex = 0;

            exitMenu();
            allFieldsMenu();
            String allFieldsInput = scanner.nextLine().trim();
            if (STOP_SIGNAL.equals(allFieldsInput)) {
                return;
            } else if (RESET_SIGNAL.equals(allFieldsInput)) {
                continue;
            }
            allFields = !"0".equals(allFieldsInput);

            deleteFile(pathOut);
            try (BufferedWriter writer = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE)
            ) {

//                deleteFile(pathOut);
                ReturnStatus status = null;
                for (String filePath : filePaths) {
                    Path path = Path.of(filePath.trim());
                    log.debug("Path to loud features: {}", path);

                    JsonFactory jsonFactory = objectMapper.getFactory();
                    try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(path.toString()))
//                     ;
//                     BufferedWriter writer = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8,
//                             StandardOpenOption.APPEND, StandardOpenOption.CREATE)
                    ) {
                        status = parse(jsonParser, writer, limit);
                        if (status != ReturnStatus.OK) {
                            log.debug(status == ReturnStatus.RESET ? RESET_COMMAND_RECEIVED : EXIT_COMMAND_RECEIVED);
                            break;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    builder.append("\n");

                }
                if (status == ReturnStatus.RESET) {
                    continue;
                } else if (status == ReturnStatus.STOP) {
                    return;
                }

                // Titles
                if (fields.values().size() > 0) {
                    csvLineParts.clear();
                    controlLinePartsSize();
                    fields.values().stream()
                            .filter(Objects::nonNull)
                            .filter(field -> Objects.nonNull(field.getName()))
                            .filter(field -> Objects.nonNull(field.getIndex()))
                            .forEach(field -> csvLineParts.set(field.getIndex(), replaceDelimiter(field.getName())));

                    writer.write(String.join(DELIMITER, csvLineParts));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//            System.out.println(builder.toString());

//            csvLineParts.forEach(System.out::println);

//            fields.forEach((s, s2) -> {
//                if (s2 != null) {
//                    System.out.println(s + " = " + s2);
//                }
//            });

//            System.out.println("fields.size() = " + fields.size());
//            System.out.println("csvLineParts.size() = " + csvLineParts.size());
//            System.out.println("nextFieldIndex = " +nextFieldIndex);

//            System.out.println();
//            csvLineParts.forEach(System.out::println);

        }
    }

    private ReturnStatus parse(JsonParser jsonParser, BufferedWriter writer, final Integer limit) throws IOException {
        ReturnStatus status = null;
        while (jsonParser.nextToken() != null) {
            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME && "features".equals(jsonParser.currentName())) {
                int count = 0;
                while (jsonParser.nextToken() != JsonToken.END_ARRAY && (limit == null || count < limit)) {
                    count++;
                    status = parseTarget(jsonParser, writer);
                    if (status != ReturnStatus.OK) {
                        log.debug(status == ReturnStatus.RESET ? RESET_COMMAND_RECEIVED : EXIT_COMMAND_RECEIVED);
                        break;
                    }
                    if (builder.lastIndexOf(DELIMITER) == builder.length() - 1) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    builder.append("\n");

                }
                if (status != ReturnStatus.OK) {
                    return status;
                }
                log.debug("Loaded features number: {}", count);
            }
        }
        return ReturnStatus.OK;
    }

    private ReturnStatus parseTarget(JsonParser jsonParser, BufferedWriter writer) throws IOException {

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
                    if (!fields.containsKey(subFieldName)) {
                        fields.put(subFieldName, new Field(subFieldName, nextFieldIndex++));
                    }

                    // Duplicated ID
                    if (!nodeIds.add(featureValue)) {
                        return ReturnStatus.OK;
                    }

                    if (fields.get(subFieldName) != null) {
                        setCsvLinePart(fields.get(subFieldName).getIndex(), jsonParser.getValueAsString());
                    }

                    builder.append("id=").append(featureValue).append(DELIMITER);
                    break;
                case "properties":
                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String propsFieldName = jsonParser.getCurrentName();
                        jsonParser.nextToken();

                        if (propsFieldName.startsWith("name:")
                                && !"name:ru".equals(propsFieldName)
                                && !"name:en".equals(propsFieldName)
                                && !"name:".equals(propsFieldName)) {
                            continue;
                        }

                        if (!fields.containsKey(propsFieldName)) {
                            if (allFields) {
                                fields.put(propsFieldName, new Field(propsFieldName, nextFieldIndex++));
                            } else {
                                fieldDetectedMenu(propsFieldName, jsonParser.getValueAsString());
                                String customName = scanner.nextLine().trim();

                                if (STOP_SIGNAL.equals(customName)) {
                                    log.debug(EXIT_COMMAND_RECEIVED);
                                    return ReturnStatus.STOP;
                                } else if (RESET_SIGNAL.equals(customName)) {
                                    log.debug(RESET_COMMAND_RECEIVED);
                                    return ReturnStatus.RESET;
                                }

                                if ("".equals(customName)) {
                                    fields.put(propsFieldName, new Field(propsFieldName, nextFieldIndex++));
                                } else if ("-".equals(customName)) {
                                    fields.put(propsFieldName, null);
                                } else {
                                    fields.put(propsFieldName, new Field(customName, nextFieldIndex++));
                                }
                            }
                        }

                        if (fields.get(propsFieldName) != null) {
                            setCsvLinePart(fields.get(propsFieldName).getIndex(), jsonParser.getValueAsString());
                        }

                        switch (propsFieldName) {
                            case "@id":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("@id=").append(featureValue).append(DELIMITER);
                                break;
                            case "addr:country":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("country=").append(featureValue).append(DELIMITER);
                                break;
                            case "addr:region":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("region=").append(featureValue).append(DELIMITER);
                                break;
                            case "addr:district":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("district=").append(featureValue).append(DELIMITER);
                                break;
                            case "name":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("name=").append(featureValue).append(DELIMITER);
                                break;
                            case "official_status":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("official_status=").append(featureValue).append(DELIMITER);
                                break;
                            case "is_in:country_code":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("is_in:country_code=").append(featureValue).append(DELIMITER);
                                break;
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
                            String longitude = "longitude";
                            String latitude = "latitude";
                            if (!fields.containsKey(longitude)) {
                                fields.put(longitude, new Field(longitude, nextFieldIndex++));
                            }
                            if (!fields.containsKey(latitude)) {
                                fields.put(latitude, new Field(latitude, nextFieldIndex++));
                            }

                            if (fields.get(longitude) != null) {
                                setCsvLinePart(fields.get(longitude).getIndex(), String.valueOf(featureLongitude));
                            }
                            if (fields.get(latitude) != null) {
                                setCsvLinePart(fields.get(latitude).getIndex(), String.valueOf(featureLatitude));
                            }

                            builder.append("longitude=").append(featureLongitude).append(DELIMITER)
                                    .append("latitude=").append(featureLatitude).append(DELIMITER);
                        }
                    }
                    break;
            }
        }

        System.out.println(csvLineParts.stream()
                .map(s -> s == null ? "" : s)
                .map(this::replaceDelimiter)
                .collect(Collectors.joining(DELIMITER)));
//        csvLineParts.forEach(System.out::print); System.out.println();

        // String of CSV
        if (csvLineParts.size() > 0) {
            writer.write(csvLineParts.stream()
                    .map(s -> s == null ? "" : s)
                    .map(this::replaceDelimiter)
                    .collect(Collectors.joining(DELIMITER)) + "\n");
        }

        return ReturnStatus.OK;
    }

    private String replaceDelimiter(String str) {
        return str.replace(DELIMITER, DELIMITER_REPLACEMENT);
    }

    private void setCsvLinePart(int index, String part) {
        controlLinePartsSize();
        csvLineParts.set(index, part);
    }

    private void controlLinePartsSize() {
        if (csvLineParts.size() < fields.size()) {
            increaseListSizeBy(fields.size() - csvLineParts.size(), csvLineParts);
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

    private static void exitMenu() {
        System.out.println("-----------------------");
        System.out.println("At any stage, you can:");
        System.out.println("Enter " + RESET_SIGNAL + " to start over");
        System.out.println("Enter " + STOP_SIGNAL + " to exit");
        System.out.println("-----------------------");
    }

    private void fieldDetectedMenu(String fieldName, String examole) throws IOException {
        System.out.println("Field detected: " + fieldName + " (example of a value: " + examole + ")");
        System.out.println("Press Enter to leave as is; Or enter your name; Or type \"-\" to skip; Or enter \"all\" to load all fields as is");
    }

    private void allFieldsMenu() {
        System.out.println("Which fields to save?");
        System.out.println("\tSelectively: 0");
        System.out.println("\tAll: 1 (or any other)");
    }

    public void run(Integer limit) {
        loadCities(limit, sourceFilePath.split(","));
    }
}
