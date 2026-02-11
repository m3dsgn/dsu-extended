# DSU Extended

Android-приложение для установки Dynamic System Updates (GSI) с поддержкой нескольких режимов работы: `Root`, `Shizuku`, `Dhizuku`, `ADB`.

## Важно

Автор поддерживает проект в свободное время, поэтому обновления могут выходить нечасто.

## Версия

Текущая версия: **0.8-beta**

## Что нового в 0.8-beta

- исправлены баги встроенного установщика;
- улучшена логика проверки привилегий перед установкой (Root/Shizuku/Dhizuku);
- исправлены проблемы с отображением прогресса и поведением UI в сценариях установки;
- обновлены настройки шрифтов и поведение MIUIX;
- выполнены общие исправления стабильности и навигации.

## Сборка

Требования:

- JDK 21
- Android SDK
- `adb` в PATH

Команды:

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleMiniDebug
./gradlew :app:assembleRelease
```

## Установка debug APK

```bash
adb devices -l
./gradlew :app:installDebug
```

Если появляется ошибка подписи:

```bash
adb uninstall com.dsu.extended
./gradlew :app:installDebug
```

## Credits

- Original DSU Sideloader idea: https://github.com/VegaBobo/DSU-Sideloader
- MIUIX Compose: https://github.com/compose-miuix-ui/miuix
