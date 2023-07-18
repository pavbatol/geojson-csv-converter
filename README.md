[RU](README-RU.md)

### Converter: geojson to csv

The converter converts files with the extension "geojson" to "csv" format.

I needed city and country directories for a project. Of course, you can find ready-made data to download for the project. 
But sometimes the necessary data is not available in open access. So I decided to write this converter. Now you can 
export and convert any type of geographical object from the openstreetmap database to CSV, such as cafes, museums, etc. 
By the way, I was a bit disappointed because not all cities extracted from the openstreetmap.org database had 
the 'country' field. But that's another story. 

You can obtain data in geojson format from the services that extract data from the openstreetmap.org website. 
Objects of the same type in these data may have different fields, so the number of fields can be very large.

**Key Features:**

* The converter solves the issue of multiple fields by interacting with the user through a console menu. 
Various options are provided at each step of the conversion.

* The converter can process multiple files at once and output the data into a single CSV file. 
Since it is sometimes difficult to retrieve data from services for the entire world, for example, you need to extract 
data region by region.

* The application comes with preinstalled data. You can view the JSON structure of these files and experiment with 
different combinations of them. The application works with preinstalled files through a variable located in the 
'application.properties' file. There are several commented options there, and you can add your own. If you run 
the application from a JAR file, you can also specify the path to your version of the 'application.properties' file 
in the startup command. For example:
    ```
    java -jar -Dproperties.path=path/to/your/directory/application.properties geojson-csv-converter-1.0-SNAPSHOT.jar
    ```
    
    You can also specify only the necessary variables in the launch command, for example, the logger level:
    ```
    java -jar -Dlog.level.com.pavbatol=debug geojson-csv-converter-1.0-SNAPSHOT.jar
    ```

* The application has an informative menu with the following options:
  - Start over or exit at any stage
  - Convert from preinstalled data to a single CSV file
  - Convert all files with the "geojson" extension from the default directory to a single CSV file
  - Convert all files with the "geojson" extension from your own directory to a single CSV file
  - Set a limit on the number of entities loaded from each file
  - Load all fields
  - Selectively load fields in manual mode (skip, load, specify your own field name)
  - Load fields by specifying a comma-separated list of required fields
  - Load all remaining fields
  - Skip all remaining fields

* The converter identifies duplicates based on the 'id' field of the entity and excludes them from the loading process.

* The converter excludes multiple name fields in different languages from loading, only English and Russian names are kept.

---

<details>
    <summary>Services for obtaining data in GEOJSON and JSON formats:</summary>

    * https://overpass-turbo.eu
      This service provides a query builder assistant.
      I recommend removing everything unnecessary from the generated query template (for example, like this:
      way["place"="city"](area.searchArea); relation["place"="city"](area.searchArea);) and leaving only 'node[]'.
      Otherwise, the file will be inflated with unnecessary data, and the converter will only consider the first
      coordinates from the long list, which do not indicate the center but the perimeter of the object.

    * https://lz4.overpass-api.de
      You can make a query like this (retrieve city data):
      https://lz4.overpass-api.de/api/interpreter?data=[out:json];node[place=city];out;
      I want to draw your attention - this service provides JSON format with a different structure than the above-mentioned service.
      This is taken into account in the converter; just rename the extension to geojson.

    * There are likely other services as well.
      And if the structure of the output data differs from what is expected, the converter cannot process it.
      However, the code allows for extending the list of names for the two main elements.
      You can simply add them to the variable in the Converter class. 
      Here it is: featurePropertiesListNames = Map.of("features", "properties", "elements", "tags");
</details>

---

**Technologies used:**

- Java 17
- Maven
- SLF4J + logback-classic
- Lombok

**Running the application:**

- Build the project using the command: `mvn clean package`
- Run it from the directory containing the compiled 'geojson-csv-converter-1.0-SNAPSHOT.jar' file:
    ```
    java -jar geojson-csv-converter-1.0-SNAPSHOT.jar
    ```
