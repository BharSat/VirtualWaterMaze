@echo off
gradlew :desktop:createJar
"D:\Program Files\Android Studio\jre\bin\java.exe" -jar D:\bhargav\projects\VWMandroid\desktop\build\libs\desktop-1.0.0.jar