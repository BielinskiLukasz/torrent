# School project for PJATK SKJ 
# TORrent
***https://github.com/BielinskiLukasz/torrent***

***for English please see below (from the next thick line)***
****

Istrukcja:
-
- Aplikacja umożliwia wymianę plików między dwoma klientami (wersja h2h) jak i wieloma (mh);
- Każda instancja aplikacji ma domyślny folder z pobieranymi/udostępnianymi plikami (możliwa zmiana folderu w konfiguracji aplikacji);
- Zakłada się, że powyższy folder (foldery) został wcześniej utworzony
- Ścieżki i zmienne globalne mogą być konfigurowane w pliku config/Config.java;
- Wyświetlanie logów może być konfigurowane w pliku utils/Logger.java;

***API***
- Wszystkie argumenty uruchamianych klientów muszą być oddzielone spacjami;
- Nazwy plików można podawać w cudzysłowie lub bez niego;
- Nazwy plików nie mogą zawierać znaku '*';
- Uruchomienie serwera nie wymaga żadnych argumentów;
- Uruchomienie klientów pracujących w systemie mh wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0);
- Uruchomienie pierwszego klienta pracującego w systemie h2h wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0);
- Uruchomienie drugiego klienta pracującego w systemie h2h wymaga podania mumeru klienta (unikatowa liczba naturalna większa od 0) oraz numeru pierwszego klienta;
- Wszystkie parametry zapytań muszą być oddzielane spacją;
- Tylko pliki znajdujące się bezpośrednio w domyślnym folderze są widoczne (foldery znajdujące się w domyślnym folderze są pomijane);

**Dostępne zapytania:**

- list

Wyświetla listę dostępnych do pobrania plików wraz z numerem klienta udostępniającego dany plik oraz sumą konstrolną pliku.
````
list
````

- pull

Pobiera wybrany plik od wskazanego klienta. Przed pobraniem sprawdza, czy wskazany klient połączony jest z serwerem i udostępnia wskazany plik. Wznawia pobieranie w przypadku przerwania połączenia oraz wstrzymania/wyłączenia na krótki czas jednego z klientów (ale nie dwóch, w przypadku wyłączenia dwóch klientów pobieranie nie zostanie wznowione). Czas usiłowania nawiązania ponownego połączeniu można edytować w pliku konfiguracyjnym.

````
pull numer_klienta_udostępniającego_plik(int) nazwa_pliku(string)

przykład:
pull 2 exampleFileFromClient2.txt
````

- push

Wysyła wybrany (lokalny) plik do wskazanego klienta. Przed wysłaniem sprawdza, czy wskazany klient połączony jest z serwerem i czy klient wysyłający udostępnia wskazany plik. Wznawia wysyłanie w przypadku przerwania połączenia oraz wstrzymania/wyłączenia na krótki czas jednego z klientów (ale nie dwóch, w przypadku wyłączenia dwóch klientów wysyłanie nie zostanie wznowione). Czas usiłowania nawiązania ponownego połączeniu można edytować w pliku konfiguracyjnym.

W przypadku połączenia h2h nie jest wymagane podawanie numeru klienta. 
````
push numer_klienta_odbierającego_plik(int) nazwa_pliku(string)

przykład MH:
push 2 otherFileFromCientEnteringCommand.txt

przykład H2H:
push otherFileFromCientEnteringCommand.txt
````

- exit

Usuwa numer klienta z bazy serwera. Pliki w domyślnym folderze nie będą udostępniane aż do kolejnego połączenia. Po wywołaniu tej komendy możliwe jest bezpieczne zatrzymanie aplikacji klienta - nie zostanie zakłucone połączenie innych klientów z serwerem oraz między klieantami. 
````
exit
````

### TODO API regex
***API regex:***
````
"not implemented yet"
````

***Konfiguracja***
- _config/Config.java_ - konfiguracja ścieżek i zmiennych globalnych
- _utils/Logger.java_ - konfiguracja wyświetlania logów

****
### TODO English version update
