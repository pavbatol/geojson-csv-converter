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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class FileDataLoader {
    private static final String COMMA = ",";
    private static final String RESET_SIGNAL = "---";
    private static final String STOP_SIGNAL = "XXX";
    public static final String RESET_COMMAND_RECEIVED = "Reset command received";
    public static final String EXIT_COMMAND_RECEIVED = "Exit command received";
    private final String OUTPUT_FILE = "output.csv";
    private final String OUTPUT_DIR = "output";

    private final Properties properties = AppConfig.getInstance().getProperty();
    private final String citiesFilePath = properties.getProperty("app.data.file-path.cities");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean allFields;
    private int nextFieldIndex;
    private List<String> csvLineParts;
    private Map<String, Field> fields;
    private StringBuilder builder;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * @param limit     Number of entries in the file. Set null for all records.
     * @param filePaths Paths to json format files from which to read data
     */
    private void loadCities(final Integer limit, String... filePaths) {
        Path pathOut = Paths.get(OUTPUT_DIR, OUTPUT_FILE);
        creatDirectoryIfNotExists(pathOut.getParent());

        while (true) {
            builder = new StringBuilder();
            fields = new HashMap<>(133, 0.75f);
            csvLineParts = new ArrayList<>(100);
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

            ReturnStatus status = null;
            for (String filePath : filePaths) {
                Path path = Path.of(filePath.trim());
                log.debug("Path to loud features: {}", path);

                JsonFactory jsonFactory = objectMapper.getFactory();
                try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(path.toString()));
                     BufferedWriter writer = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8)
                ) {
                    status = rootParser(jsonParser, limit);
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

//            System.out.println(builder.toString());

//            fields.forEach((s, s2) -> {
//                if (s2 != null) {
//                    System.out.println(s + " = " + s2);
//                }
//            });

        }
    }

    private ReturnStatus rootParser(JsonParser jsonParser, final Integer limit) throws IOException {
        ReturnStatus status = null;
        while (jsonParser.nextToken() != null) {
            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME && "features".equals(jsonParser.currentName())) {
                int count = 0;
                while (jsonParser.nextToken() != JsonToken.END_ARRAY && (limit == null || count < limit)) {
                    count++;
                    status = targetParser(jsonParser);
                    if (status != ReturnStatus.OK) {
                        log.debug(status == ReturnStatus.RESET ? RESET_COMMAND_RECEIVED : EXIT_COMMAND_RECEIVED);
                        break;
                    }
                    if (builder.lastIndexOf(COMMA) == builder.length() - 1) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    builder.append("\n");

                    csvLineParts.forEach(System.out::print); System.out.println();

                }
                if (status != ReturnStatus.OK) {
                    return status;
                }
                log.debug("Loaded features number: {}", count);
            }
        }
        return ReturnStatus.OK;
    }

    private ReturnStatus targetParser(JsonParser jsonParser) throws IOException {

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

                    if (fields.get(subFieldName) != null) {
                        setCsvLinePart(fields.get(subFieldName).getIndex(), jsonParser.getValueAsString() + COMMA);
                    }

                    builder.append("id=").append(featureValue).append(COMMA);
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
                            setCsvLinePart(fields.get(propsFieldName).getIndex(), jsonParser.getValueAsString() + COMMA);
                        }

                        switch (propsFieldName) {
                            case "@id":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("@id=").append(featureValue).append(COMMA);
                                break;
                            case "addr:country":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("country=").append(featureValue).append(COMMA);
                                break;
                            case "addr:region":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("region=").append(featureValue).append(COMMA);
                                break;
                            case "addr:district":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("district=").append(featureValue).append(COMMA);
                                break;
                            case "name":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("name=").append(featureValue).append(COMMA);
                                break;
                            case "official_status":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("official_status=").append(featureValue).append(COMMA);
                                break;
                            case "is_in:country_code":
                                featureValue = jsonParser.getValueAsString();
                                builder.append("is_in:country_code=").append(featureValue).append(COMMA);
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
                                setCsvLinePart(fields.get(longitude).getIndex(), featureLongitude + COMMA);
                            }
                            if (fields.get(latitude) != null) {
                                setCsvLinePart(fields.get(latitude).getIndex(), featureLatitude + COMMA);
                            }

                            builder.append("longitude=").append(featureLongitude).append(COMMA)
                                    .append("latitude=").append(featureLatitude).append(COMMA);
                        }
                    }
                    break;
            }
        }
        return ReturnStatus.OK;
    }

    private void setCsvLinePart(int index, String part) {
        controlListSize();
        csvLineParts.set(index, part);
    }

    private void controlListSize() {
        if (csvLineParts.size() < nextFieldIndex) {
            increaseListSizeBy(nextFieldIndex, csvLineParts);
        }
    }

    private void increaseListSizeBy(int increase, List<String> list) {
        for (int i = 0; i < increase; i++) {
            list.add(COMMA);
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
        loadCities(limit, citiesFilePath.split(","));
    }
}
