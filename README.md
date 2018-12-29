# School project for PJATK SKJ 
# TORrent
***https://github.com/BielinskiLukasz/torrent***

***for English please see below (from the next thick line)***
****

Istrukcja:
-
- Aplikacja umożliwia wymianę plików między dwoma klientami (wersja h2h) jak i wieloma (mh)
- Każda instancja aplikacji ma domyślny folder z pobieranymi/udostępnianymi plikami (możliwa zmiana folderu w konfiguracji aplikacji)
- Ścieżki i zmienne globalne mogą być konfigurowane w pliku config/Config.java
- Wyświetlanie logów może być konfigurowane w pliku utils/Logger.java

***API***
- wszystkie argumenty uruchamianych klientów muszą być oddzielone spacjami
- uruchomienie serwera nie wymaga żadnych argumentów
- uruchomienie klientów pracujących w systemie mh wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0)
- uruchomienie pierwszego klienta pracującego w systemie h2h wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0)
- uruchomienie drugiego klienta pracującego w systemie h2h wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0) oraz numeru pierwszego klienta
- wszystkie parametry zapytań muszą być oddzielane znakiem *
- tylko pliki znajdujące się bezpośrednio w domyślnym folderze są widoczne (foldery znajdujące się w domyślnym folderze są pomijane)

**Dostępne zapytania:**

- ### connect TODO jeżeli zostanie zaimplementowane

łączy klienta z serwerem (wywoływane automatycznie w trakcie utworzenia serwera)

- list

wyświetla listę dostępnych do pobrania plików jako listę zawierającą: numer klienta udostępniającego dany plik, nazwę pliku, sumę md5 pliku

- pull

pobiera wybrany plik od wybranego klienta

- push

wysyła wybrany (lokalny) plik od wybranego klienta

Parametry zapytań:
- connect*numer klienta(int)*wiadomość(string)
- list
- pull*numer klienta od którego pobierany będzie plik(int)*nazwa pliku(string)
- push*numer klienta do którego wysyłany będzie plik(int)*nazwa pliku(string)
- ### TODO podać parametry

### TODO API regex
***API regex:***
````
""
````
np.
- connect*1*Hello all
- list
- pull*2*example.txt
- push*2*otherFile.txt
- ### TODO

***CONFIG***
- _config/Config.java_ - konfiguracja ścieżek i zmiennych globalnych
- _utils/Logger.java_ - konfiguracja wyświetlania logów

****
### TODO English version update

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


query parameters:
- ... 

***API Request regex:***
````
"TODO"
````
e.g.
- list

***CONFIG***
- _config/Config.java_ - specifies all env variables
- _utils/Logger.java_ - turn on/off logs

