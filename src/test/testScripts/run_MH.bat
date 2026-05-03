cd /d "%~dp0..\..\.."

start java -jar target\torrentServer-bin.jar
start java -jar target\torrentClient-bin.jar 1
start java -jar target\torrentClient-bin.jar 2
start java -jar target\torrentClient-bin.jar 3
