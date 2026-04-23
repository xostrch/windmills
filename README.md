# Dokumentacja Projektu - Etap I
## System Zarządzania Dziennikami Technicznymi Sieci Turbin Wiatrowych

### 1. Niemutowalność klasy SensorReading
Klasa `SensorReading` została zaprojektowana jako niemutowalna (immutable).
Wszystkie jej pola są oznaczone jako `final`, a klasa nie posiada setterów. 
Jest to kluczowe dla zachowania integralności danych eksploatacyjnych – odczyt z sensora 
jest faktem historycznym, który raz zapisany nie powinien być modyfikowany 
w trakcie działania programu, co zapobiega przypadkowym błędom w obliczeniach analitycznych.

### 2. Atak na niezmiennik w WindTurbine
Metoda `getSensors()` w klasie `WindTurbine` została zabezpieczona przed atakiem na 
niezmiennik poprzez zastosowanie kopii obronnej (`Arrays.copyOf`). Gdyby metoda 
zwracała bezpośrednią referencję do tablicy, zewnętrzny kod mógłby zmodyfikować zestaw sensorów 
turbiny (np. przypisać `null` do elementu tablicy), co naruszyłoby spójność obiektu. 
Dzięki kopii obronnej, wszelkie zmiany na zwróconej tablicy nie wpływają na stan wewnętrzny turbiny.

### 3. Nowy vs istniejący obiekt w metodach filtrujących
Metody filtrujące w klasie `WindFarm` (`filterByTurbine`, `filterByOperator`, itd.) 
zawsze zwracają **nowy obiekt** klasy `WindFarm`. Pozwala to na zachowanie oryginalnego, 
pełnego zbioru danych nienaruszonego, umożliwiając jednoczesne tworzenie wielu 
różnych widoków danych (podzbiorów) do celów statystycznych bez ryzyka utraty 
informacji o pozostałych wpisach.

### 4. Metody statyczne vs instancyjne w FarmAnalytics
W klasie `FarmAnalytics` zastosowano metody instancyjne zamiast statycznych. 
Pozwala to na większą elastyczność systemu – obiekt analityczny może w przyszłości 
przechowywać własny stan (np. cache wyników) lub być częścią większej hierarchii klas (polimorfizm). 
Ułatwia to również testowanie modułu analitycznego z różnymi instancjami farm wiatrowych.

### 5. Struktura danych dla grupowania (bez Map)
Do wyznaczania unikalnych wartości oraz grupowania danych wykorzystano listy `ArrayList` 
oraz tablice, rezygnując z gotowych struktur typu `HashMap`. Unikalność
sprawdzana jest poprzez iterację i porównywanie ciągów znaków metodą `equalsIgnoreCase`. 
Do rankingów (sortowanie typów zdarzeń i alarmów) zaimplementowano algorytm sortowania 
bąbelkowego (Bubble Sort), co zapewnia pełną kontrolę nad procesem porządkowania danych 
bez polegania na zaawansowanych kolekcjach.

### 6. Obsługa braku mocy w computePowerOutput()
Metoda `computePowerOutput()` w klasie `LogEntry` zwraca wartość `-1.0` 
w sytuacji, gdy dana turbina nie posiada sensora o nazwie "POWER". 
Wybrano zwracanie wartości specjalnej zamiast rzucania wyjątku, ponieważ 
brak konkretnego typu sensora jest naturalną cechą konfiguracji niektórych 
urządzeń, a nie błędem krytycznym systemu. Pozwala to na poprawne działanie 
algorytmów uśredniających, które mogą po prostu pominąć takie wpisy.

### 7. Scenariusze graniczne
Aplikacja została zabezpieczona przed następującymi sytuacjami:
* **Błędy typu danych:** W `TurbineApp` zastosowano walidację wejścia – 
wprowadzenie tekstu zamiast liczby nie powoduje błędu `InputMismatchException`, 
lecz wyświetla prośbę o ponowne podanie danych.
* **Puste zbiory:** Metody analityczne sprawdzają liczebność logów i turbin, 
zapobiegając dzieleniu przez zero.
* **Wielkość liter:** Wszystkie filtry oraz mechanizmy unikalności działają 
w trybie *case-insensitive*, co eliminuje duplikaty wynikające z różnego zapisu 
tych samych nazw (np. "ALARM" vs "alarm").
* **Logi poza zakresem:** Filtrowanie dat poprawnie obsługuje 
granice zakresu (włącznie).