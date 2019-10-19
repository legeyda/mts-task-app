
База redis
Спринговые контроллеры генерятся по swagger-файлу.

запустить на хостинге:

    killall java; git pull && bash gradlew --no-daemon clean build && nohup java -jar -Dserver.port=80 build/libs/mts-task-app.jar &
