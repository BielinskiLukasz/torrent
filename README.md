# School project for PJATK SKJ 
# TORrent
***https://github.com/BielinskiLukasz/torrent***

***for English please see below (from the next thick line)***
****

Istrukcja:
-
- Każda instancja aplikacji ma domyślny folder z pobieranymi/udostępnianymi plikami (możliwa zmiana folderu w konfiguracji aplikacji)
- Ścieżki i zmienne globalne mogą być konfigurowane w pliku config/Config.java
- Wyświetlanie logów może być konfigurowane w pliku utils/Logger.java

***API***
- wszystkie parametry zapytań muszą być oddzielane spacjami
- nazwy plików zawierające spacje powinny być podawane w cudzysłowie
- tylko pliki znajdujące się bezpośrednio w domyślnym folderze są widoczne (foldery znajdujące się w domyślnym folderze są pomijane)

**Dostępne zapytania:**

- list

wyświetla listę dostępnych do pobrania plików jako listę zawierającą: numer klienta udostępniającego dany plik, nazwę pliku, sumę md5 pliku

- pull

pobiera wybrany plik

Parametry zapytań:
- TODO

***API regex:***
````
"TODO"
````
np.
- list
- TODO

***CONFIG***
- _config/Config.java_ - konfiguracja ścieżek i zmiennych globalnych
- _utils/Logger.java_ - konfiguracja wyświetlania logów

****

Instructions:
-
- Each instance of application has it's own default download folders, which can be changed in application configuration
- Paths and globals can be configured in config/Config.java
- Log display can be configured in utils/Logger.java

***API***
- all API query parameters have to be splitted with space
- files whose names contain spaces should be written in quotes
- only files in default download folders are visible (inner dirs are omitted)

**Possible queries:**
- list
- TODO

query parameters:
- TODO

***API Request regex:***
````
"TODO"
````
e.g.
- list
- TODO

***CONFIG***
- _config/Config.java_ - specifies all env variables
- _utils/Logger.java_ - turn on/off logs

