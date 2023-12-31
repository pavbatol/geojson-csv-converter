[EN](README.md)

### Конвертер: GEOJSON > CSV

Конвертер преобразует файлы с расширением "geojson" в "csv" файл.  
Мне как то понадобились для проекта справочники городов и стран. Конечно, можно найти готовые данные 
для загрузки в проект. Но иногда нужных данных просто нет в открытом доступе. Решил написать этот конвертер. 
Теперь вы можете сами выгрузить и конвертировать в CSV  любой тип географического объекта, который есть в базе 
openstreetmap (например кафе, музеи, и др.). К слову сказать, я был немного расстроен, 
так как не все города, выгруженные из базы сайта openstreetmap.org, содержали поле 'country'. Но это уже другой вопрос. 
Данные в geojson-формате вы можете получить на около-сервисах сайта openstreetmap.org. Однотипные объекты этих данных 
могут содержать разные поля, поэтому количество полей может быть очень большим.  

**Возможности:**

* Конвертер решает вопрос множества полей, взаимодействуя с пользователем через консольное меню. Предоставлены 
разнообразные варианты на каждом шаге конвертации.  

* Конвертер может обрабатывать сразу несколько файлов и выводить данные в один CSV файл. Так как из сервисов порой 
тяжело получить данные сразу, например, для всего мира, приходится их получать выделяя какой-то регион.  

* В приложении есть предустановленные данные. Вы можете посмотреть JSON-структуру этих файлов, поэкспериментировать 
с различными комбинациями этих файлов. С предустановленными файлами приложение работает через переменную, которая 
находится в файле application.properties. Там есть несколько закомментированных вариантов, вы можете добавить свои. 
Если вы запускаете приложение из JAR файла, то вы так же можете указать в команде запуска путь к своему варианту файла 
application.properties. Например:
    ```  
    java -jar -Dproperties.path=путь/к/вашей/директории/application.properties geojson-csv-converter-1.0-SNAPSHOT.jar  
    ```
* Вы так же можете задать только нужные переменные в команде запуска, например, уровень логера:
    ```  
    java -jar -Dlog.level.com.pavbatol=debug  geojson-csv-converter-1.0-SNAPSHOT.jar
    ```
* Приложение имеет достаточно информативное меню с возможностью:  
    * На любом этапе начать заново или выйти из приложения
    * Конвертировать из предустановленных данных в один CSV
    * Конвертировать из директории по умолчанию все файлы с расширением "geojson" в один CSV  
    * Конвертировать из своей директории все файлы с расширением "geojson" в один CSV
    * Установить лимит на количество загружаемых сущностей из каждого файла
    * Загрузить все поля
    * Загрузить выборочно в ручном режиме (пропустить, загрузить, указать свое имя для поля)
    * Загрузить, указав список нужных полей через запятую
    * Загрузить все оставшиеся поля
    * Пропустить все оставшиеся поля
* Конвертер определяет дубликаты по 'id' сущности и исключает их из загрузки
* Конвертер исключает из загрузки многочисленные поля имен сущности на разных языках. Оставляет английский и русский.

***

<details>
  <summary>Сервисы получения данных в geojson и json форматах</summary>

    * https://overpass-turbo.eu
        В этом сервисе есть помощник составления запросов.
        Рекомендую в сгенерированном шаблоне запроса удалить все ненужное (например такие как:   
        way["place"="city"](area.searchArea); relation["place"="city"](area.searchArea);), и оставить только 'node[]'. 
        Иначе файл будет раздут ненужными вам данными, а координаты конвертер считает только первые из длиннющего 
        их списка, которые указывают не на центр, а периметр объекта.

    * https://lz4.overpass-api.de
        Можете сделать например такой запрос (выгрузить данные городов): 
        https://lz4.overpass-api.de/api/interpreter?data=[out:json];node[place=city];out;
        Хочу обратить внимание - этот сервис выдает json формат со структурой, отличающийся от вышеуказанного сервиса.
        Это учтено в конверторе, просто переименуйте расширение на geojson.

    * Наверняка есть и другие сервисы. 
        И если структура выдачи данных отличается от ожидаемых, то конвертер их не обработает.
        Единственное, в коде предусмотренно расширение списка названиий двух основных элементов.
        Вы можете просто добавить их в переменную, которая находится в классе Converter
        Вот она: featurePropertiesListNames = Map.of("features", "properties", "elements", "tags");

</details>

***

**Технологии**
  * Java 17
  * Maven
  * SLF4J + logback-classic
  * Lombok

**Запуск**

  * Собрать проект командой: `mvn clean package`
  * Запустить из каталога, содержащего собранный geojson-csv-converter-1.0-SNAPSHOT.jar:
    ```  
    java -jar geojson-csv-converter-1.0-SNAPSHOT.jar
    ```

