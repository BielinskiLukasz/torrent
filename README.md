# School project for PJATK SKJ 
# TORrent
***https://github.com/BielinskiLukasz/torrent***

***for English please see below (from the next thick line)***
****

Instrukcja:
-
- Aplikacja umożliwia wymianę plików zarówno między dwoma (wersja h2h) jak i wieloma klientami (mh);
- Aplikacja pracuje pod nadzorem protokołu TCP;
- Klienci aplikacji realizują polecenia z konsoli jednowątkowo (każdy niezależnie);
- Każda instancja aplikacji ma domyślny folder z pobieranymi/udostępnianymi plikami (możliwa zmiana folderu w konfiguracji aplikacji);
- Zakłada się, że powyższy folder został wcześniej utworzony
- Ścieżki i zmienne globalne mogą być konfigurowane przed skompilowaniem w pliku config/Config.java;
- Wyświetlanie logów może być konfigurowane przed skompilowaniem w pliku utils/Logger.java;
- Maksymalny rozmiar przesyłanych plików to 2047MB


***API***
- Wszystkie argumenty uruchamianych klientów muszą być oddzielone spacjami;
- Nazwy plików można podawać w cudzysłowie lub bez niego;
- Nazwy plików nie mogą zawierać znaku '*';
- Uruchomienie serwera nie wymaga żadnych argumentów;
- Uruchomienie klientów pracujących w trybie mh wymaga podania numeru klienta (unikatowa liczba naturalna większa od 0);
- Uruchomienie pierwszego klienta pracującego w trybie h2h wymaga podania numeru klienta (unikatowa liczba naturalna większa od 0);
- Uruchomienie drugiego klienta pracującego w trybie h2h wymaga podania numeru klienta (unikatowa liczba naturalna większa od 0) oraz numeru pierwszego klienta;
- Wszystkie parametry zapytań muszą być oddzielane spacją;
- Tylko pliki znajdujące się bezpośrednio w domyślnym folderze są widoczne (foldery znajdujące się w domyślnym folderze są pomijane);

**Dostępne zapytania:**

- list

Wyświetla listę dostępnych do pobrania plików wraz z numerem klienta udostępniającego dany plik oraz sumą kontrolną pliku.
````
list
````

- pull

Pobiera wybrany plik od wskazanego klienta. Przed pobraniem sprawdza, czy wskazany klient połączony jest z serwerem i udostępnia wskazany plik. Wznawia pobieranie w przypadku przerwania połączenia oraz wstrzymania/wyłączenia na krótki czas jednego z klientów (ale nie dwóch, w przypadku wyłączenia dwóch klientów pobieranie nie zostanie wznowione). Czas usiłowania nawiązania ponownego połączenia można edytować w pliku konfiguracyjnym.

W przypadku połączenia h2h nie jest wymagane podawanie numeru klienta.
````
Host2host:
pull nazwa_pliku(string)

przykład:
pull exampleFileFromClient2.txt

Mutli host:
pull numer_klienta_udostępniającego_plik(int) nazwa_pliku(string)

przykład:
pull 2 exampleFileFromClient2.txt
````

- push

Wysyła wybrany (lokalny) plik do wskazanego klienta. Przed wysłaniem sprawdza, czy wskazany klient połączony jest z serwerem i czy klient wysyłający udostępnia wskazany plik. Wznawia wysyłanie w przypadku przerwania połączenia oraz wstrzymania/wyłączenia na krótki czas jednego z klientów (ale nie dwóch, w przypadku wyłączenia dwóch klientów wysyłanie nie zostanie wznowione). Czas usiłowania nawiązania ponownego połączeniu można edytować w pliku konfiguracyjnym.

W przypadku połączenia h2h nie jest wymagane podawanie numeru klienta. 
````
Host2host:
push nazwa_pliku(string)

przykład:
push otherFileFromCientEnteringCommand.txt

Mutli host:
push 2 otherFileFromCientEnteringCommand.txt

przykład:
push 2 otherFileFromCientEnteringCommand.txt
````

- multiple_pull

Pobiera wybrany plik od udostępniających go klientów. Przed pobraniem sprawdza, którzy klienci aktualnie udostępniają wskazany plik. Wznawia pobieranie w przypadku przerwania połączenia oraz wstrzymania/wyłączenia na krótki czas jednego z klientów (w przypadku dłuższego czasu oczekiwania zacznie pobierać fragment pliku od innego, połączonego klienta). Czas usiłowania nawiązania ponownego połączeniu można edytować w pliku konfiguracyjnym. Polecenie obsługiwane jedynie w wersji mh.
````
Mutli host:
multiple_pull nazwa_pliku(string)

przykład:
multiple_pull exampleFileFromClients.txt
````

- exit

Usuwa numer klienta z bazy serwera. Pliki w domyślnym folderze nie będą udostępniane aż do kolejnego połączenia. Po wywołaniu tej komendy możliwe jest bezpieczne zatrzymanie aplikacji klienta - nie zostanie zakłócone połączenie innych klientów z serwerem oraz między klientami.
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
