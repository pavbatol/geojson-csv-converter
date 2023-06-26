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
    private static final String CLEAR_SIGNAL = "---";
    private static final String STOP_SIGNAL = "XXX";
    private final String OUTPUT_PATH = "output.csv";

    private final Properties properties = AppConfig.getInstance().getProperty();
    private final String countriesFilePath = properties.getProperty("app.data.file-path.countries");
    private final String highSchoolsFilePath = properties.getProperty("app.data.file-path.high-schools");
    private final String citiesFilePath = properties.getProperty("app.data.file-path.cities");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean stop;
    private boolean allFields;
    private Map<String, String> fields;
    private StringBuilder builder;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * @param limit     Number of entries in the file. Set null for all records.
     * @param filePaths Paths to json format files from which to read data
     */
    public void loadCities_OLD(final Integer limit, String... filePaths) {

        Scanner scanner = new Scanner(System.in);
        Map<String, String> fields = new HashMap<>();

        allFieldsMenu();
        String allFieldsInput = scanner.nextLine();
        final boolean allFields = "1".equals(allFieldsInput);

        List<String> collect = Arrays.stream(filePaths)
                .map(filePath -> {
                    Path path = Path.of(filePath.trim());
                    log.debug("Path to loud Cities: {}", path);

                    JsonFactory jsonFactory = objectMapper.getFactory();
                    try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(path.toString()))) {

                        StringBuilder builder = new StringBuilder();

                        while (jsonParser.nextToken() != null) {
                            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME && "features".equals(jsonParser.currentName())) {
                                String featureId;
                                String featureCountry;
                                String featureRegion;
                                String featureDistrict;
                                String featureName;
                                String featureOfficialStatus;
                                String featureIsInCountryCode;
                                double featureLongitude = .0;
                                double featureLatitude = .0;

                                int count = 0;
                                while (jsonParser.nextToken() != JsonToken.END_ARRAY && (limit == null || count < limit)) {
                                    count++;
                                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                        if (jsonParser.currentToken() != JsonToken.FIELD_NAME) {
                                            continue;
                                        }
                                        String subFieldName = jsonParser.getCurrentName();
                                        jsonParser.nextToken();
                                        switch (subFieldName) {
                                            case "id":
                                                featureId = jsonParser.getValueAsString();
                                                builder.append("id=").append(featureId).append(COMMA);
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
                                                            fields.put(propsFieldName, propsFieldName);
                                                        } else {
                                                            fieldDetectedMenu(propsFieldName, jsonParser.getValueAsString());
                                                            String customName = scanner.nextLine().trim().replace("\n", "");
                                                            if ("".equals(customName)) {
                                                                fields.put(propsFieldName, propsFieldName);
                                                            } else if ("-".equals(customName)) {
                                                                fields.put(propsFieldName, null);
                                                            } else {
                                                                fields.put(propsFieldName, customName);
                                                            }
                                                        }
                                                    }

                                                    switch (propsFieldName) {
                                                        case "@id":
                                                            featureId = jsonParser.getValueAsString();
                                                            builder.append("@id=").append(featureId).append(COMMA);
                                                            break;
                                                        case "addr:country":
                                                            featureCountry = jsonParser.getValueAsString();
                                                            builder.append("country=").append(featureCountry).append(COMMA);
                                                            break;
                                                        case "addr:region":
                                                            featureRegion = jsonParser.getValueAsString();
                                                            builder.append("region=").append(featureRegion).append(COMMA);
                                                            break;
                                                        case "addr:district":
                                                            featureDistrict = jsonParser.getValueAsString();
                                                            builder.append("district=").append(featureDistrict).append(COMMA);
                                                            break;
                                                        case "name":
                                                            featureName = jsonParser.getValueAsString();
                                                            builder.append("name=").append(featureName).append(COMMA);
                                                            break;
                                                        case "official_status":
                                                            featureOfficialStatus = jsonParser.getValueAsString();
                                                            builder.append("official_status=").append(featureOfficialStatus).append(COMMA);
                                                            break;
                                                        case "is_in:country_code":
                                                            featureIsInCountryCode = jsonParser.getValueAsString();
                                                            builder.append("is_in:country_code=").append(featureIsInCountryCode).append(COMMA);
                                                            break;
                                                    }
                                                }
                                                break;
                                            case "geometry":
                                                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                                    String geomFieldName = jsonParser.getCurrentName();
                                                    jsonParser.nextToken();
                                                    if ("coordinates".equals(geomFieldName)) {
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
                                                        builder.append("longitude=").append(featureLongitude).append(COMMA)
                                                                .append("latitude=").append(featureLatitude).append(COMMA);
                                                    }
                                                }
                                                break;
                                        }

                                    }

                                    if (builder.lastIndexOf(COMMA) == builder.length() - 1) {
                                        builder.deleteCharAt(builder.length() - 1);
                                    }
                                    builder.append("\n");

                                }
                                log.debug("Loaded cities number: {} from: {}", count, path);
                            }
                        }

                        return builder.toString();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        collect.forEach(System.out::println);

        System.out.println();
        fields.forEach((s, s2) -> {
            if (s2 != null) {
                System.out.println(s + " = " + s2);
            }
        });
    }

    /**
     * @param limit     Number of entries in the file. Set null for all records.
     * @param filePaths Paths to json format files from which to read data
     */
    private void loadCities(final Integer limit, String... filePaths) {
        Path pathOut = Paths.get(OUTPUT_PATH);


        while (!stop) {
            builder = new StringBuilder();
            fields = new HashMap<>();
//            allFields = false;

            exitMenu();
            allFieldsMenu();
            String allFieldsInput = scanner.nextLine();
            if (STOP_SIGNAL.equals(allFieldsInput)) {
                return;
            }
            if (CLEAR_SIGNAL.equals(allFieldsInput)) {
                continue;
            }
            allFields = !"0".equals(allFieldsInput);

            for (String filePath : filePaths) {
                Path path = Path.of(filePath.trim());
                log.debug("Path to loud Cities: {}", path);

                JsonFactory jsonFactory = objectMapper.getFactory();
                try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(path.toString()));
                     BufferedWriter writer = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8)
                ) {

                    rootParser(jsonParser, limit, path);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                builder.append("\n");

            }

            System.out.println(builder.toString());

            fields.forEach((s, s2) -> {
                if (s2 != null) {
                    System.out.println(s + " = " + s2);
                }
            });

        }
    }

    private void rootParser(JsonParser jsonParser, final Integer limit, Path path) throws IOException {
        while (jsonParser.nextToken() != null) {
            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME && "features".equals(jsonParser.currentName())) {

                int count = 0;
                while (jsonParser.nextToken() != JsonToken.END_ARRAY && (limit == null || count < limit)) {
                    count++;

                    targetParser(jsonParser);

                    if (builder.lastIndexOf(COMMA) == builder.length() - 1) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    builder.append("\n");

                }

                log.debug("Loaded cities number: {} from: {}", count, path);

            }
        }
    }

    private void targetParser(JsonParser jsonParser) throws IOException {
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
                                fields.put(propsFieldName, propsFieldName);
                            } else {
                                fieldDetectedMenu(propsFieldName, jsonParser.getValueAsString());
                                String customName = scanner.nextLine().trim().replace("\n", "");
                                if ("".equals(customName)) {
                                    fields.put(propsFieldName, propsFieldName);
                                } else if ("-".equals(customName)) {
                                    fields.put(propsFieldName, null);
                                } else {
                                    fields.put(propsFieldName, customName);
                                }
                            }
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
                            builder.append("longitude=").append(featureLongitude).append(COMMA)
                                    .append("latitude=").append(featureLatitude).append(COMMA);
                        }
                    }
                    break;
            }

        }
    }

    private static void exitMenu() {
        System.out.println("-----------------------");
        System.out.println("At any stage, you can:");
        System.out.println("Enter " + CLEAR_SIGNAL + " to start over");
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
