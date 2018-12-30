# School project for PJATK SKJ 
# TORrent
***https://github.com/BielinskiLukasz/torrent***

***for English please see below (from the next thick line)***
****

Istrukcja:
-
- Aplikacja umożliwia wymianę plików między dwoma klientami (wersja h2h) jak i wieloma (mh);
- Każda instancja aplikacji ma domyślny folder z pobieranymi/udostępnianymi plikami (możliwa zmiana folderu w konfiguracji aplikacji);
- Ścieżki i zmienne globalne mogą być konfigurowane w pliku config/Config.java;
- Wyświetlanie logów może być konfigurowane w pliku utils/Logger.java;

***API***
- Wszystkie argumenty uruchamianych klientów muszą być oddzielone spacjami;
- Uruchomienie serwera nie wymaga żadnych argumentów;
- Uruchomienie klientów pracujących w systemie mh wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0);
- Uruchomienie pierwszego klienta pracującego w systemie h2h wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0);
- Uruchomienie drugiego klienta pracującego w systemie h2h wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0) oraz numeru pierwszego klienta;
- Wszystkie parametry zapytań muszą być oddzielane spacją;
- Tylko pliki znajdujące się bezpośrednio w domyślnym folderze są widoczne (foldery znajdujące się w domyślnym folderze są pomijane);

**Dostępne zapytania:**

- ### connect TODO jeżeli zostanie zaimplementowane

Łączy klienta z serwerem (wywoływane automatycznie w trakcie utworzenia serwera).
````
connect numer_klienta(int) wiadomość(string)

przykład:
connect 1 Hello all
````

- list

Wyświetla listę dostępnych do pobrania plików wraz z numerem klienta udostępniającego dany plik oraz sumą konstrolną pliku.
````
list

przykład:
list
````

- pull

Pobiera wybrany plik od wskazanego klienta.

*Aktualnie nieobsłużony jest przypadek próby pobrania pliku od niepołączonego klienta*
````
pull numer_klienta_udostępniającego_plik(int) nazwa_pliku(string)

przykład:
pull 2 example.txt
````

- push

Wysyła wybrany (lokalny) plik od wskazanego klienta

*Aktualnie nieobsłużony jest przypadek próby wysłania pliku od niepołączonego klienta*
````
push numer_klienta_odbierającego_plik(int) nazwa_pliku(string)

przykład:
push 2 otherFile.txt
````

- exit

Usuwa numer klienta z bazy serwera. Pliki w domyślnym folderze nie będą udostępniane aż do kolejnego połączenia. Po wywołaniu tej komendy możliwe jest bezpieczne zatrzymanie aplikacji klienta - nie zostanie zakłucone połączenie innych klientów z serwerem oraz między klieantami. 
````
exit

przykład:
exit
````

### TODO API regex
***API regex:***
````
""
````

***Konfiguracja***
- _config/Config.java_ - konfiguracja ścieżek i zmiennych globalnych
- _utils/Logger.java_ - konfiguracja wyświetlania logów

****
### TODO English version update
