
Тестовая служба работы с задачами
======================================

Реализация API по swagger-[описанию](src/main/swagger/api.yml).
                                  


База redis (должен быть запущен на localhost:6379), 
спринговые контроллеры генерятся по swagger-файлу при компиляции.


Запустить на хостинге:

    killall java; git pull && bash gradlew --no-daemon clean build && nohup java -jar -Dserver.port=80 build/libs/mts-task-app.jar &
