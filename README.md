# claudeAssist

Ein schlanker, datenschutzfreundlicher **WebView-Wrapper für [claude.ai](https://claude.ai)** für Android.

Inspiriert von und im Stil von [gptAssist](https://github.com/woheller69/gptAssist) und
[geminiAssist](https://github.com/AcideFluorhydrique/geminiAssist) (beide GPLv3).

## Funktionen

- 🔒 **URL-Blocking**: Blockiert standardmäßig alle Domains, die nicht für den Betrieb von
  claude.ai selbst notwendig sind (Tracker, Analytics, Werbenetzwerke Dritter).
- 🔄 **Umschaltbar**: Tippe auf den Button oben rechts, um Blocking an/aus zu schalten,
  falls eine Funktion (z. B. Google-Login) dadurch beeinträchtigt wird.
- 👆 **Ausblendbar**: Den Button nach oben wegwischen, um ihn zu verstecken.
- 🗑️ **Reset**: Langes Drücken auf den Button löscht Cookies, Cache und Website-Daten
  (vollständiger Logout).
- 📎 **Datei-Upload**: Unterstützt das Hochladen von Dateien/Bildern in Chats.
- 🎙️ **Mikrofon-Support**: Für Sprachfunktionen in der Web-UI.
- 🔐 Keine Telemetrie, kein eigenes Tracking, keine Werbung. Reines FOSS.

## Was diese App NICHT ist

Dies ist **kein offizielles Anthropic-Produkt**. Es ist ein reiner WebView-Wrapper um die
öffentliche claude.ai-Weboberfläche — vergleichbar mit einer Progressive-Web-App, nur als
eigenständige APK verpackt. Für Login, Konto und Nutzungsbedingungen gelten die Regeln von
claude.ai / Anthropic.

Für Claude Code (die Coding-CLI) gibt es andere, dedizierte Projekte — dieses Repo betrifft
ausschließlich die **claude.ai Chat-Weboberfläche**.

## Bauen

Voraussetzungen: Android Studio (aktuelle Version) oder Gradle + Android SDK (API 34).

```bash
git clone https://github.com/<dein-user>/claudeAssist.git
cd claudeAssist
./gradlew assembleDebug
```

Die fertige APK liegt danach unter `app/build/outputs/apk/debug/app-debug.apk`.

Für eine Release-APK (unsigniert):

```bash
./gradlew assembleRelease
```

## Bei F-Droid einreichen

F-Droid baut Apps aus dem Quellcode selbst (kein Upload von APKs). Dazu benötigst du:

1. Ein öffentliches Repo mit reproduzierbarem Gradle-Build (bereits vorbereitet).
2. Eine `LICENSE`-Datei mit FOSS-Lizenz (siehe unten, GPLv3).
3. Einen Metadaten-Merge-Request im
   [fdroiddata](https://gitlab.com/fdroid/fdroiddata)-Repo, siehe
   [F-Droid Inclusion-Doku](https://f-droid.org/docs/Inclusion_Policy/).

Eine Beispiel-Metadatendatei liegt unter `metadata/com.claudeassist.app.yml` in diesem Repo,
die du (leicht angepasst) für den F-Droid-Merge-Request verwenden kannst.

## Lizenz

GPLv3 — siehe [LICENSE](LICENSE). Enthält Ideen/Konzepte aus gptAssist und geminiAssist
(beide ebenfalls GPLv3).

## Mitwirken

Issues und Pull Requests sind willkommen. Bitte Android-Version und Gerät bei Bug-Reports
angeben.
