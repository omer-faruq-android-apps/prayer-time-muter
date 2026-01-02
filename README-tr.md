# Namaz Vakti Sessizleştirici

Bu depo, telefonunuzu namaz vakitlerinde otomatik olarak sessize almanızı sağlar. Vakitler https://prayertimes.api.abdus.dev/ API'sinden çekilir, şehir/ülke seçimini yapabilir, her vakit için ayrı öncesi/sonrası sessize alma süreleri tanımlayabilir, Cuma öğle vaktine özel süreler verebilir ve sessiz modu titreşimsiz veya titreşimli olarak belirleyebilirsiniz. Çalışma, izin ve pil optimizasyonu rehberi için aşağıdaki notlara bakın.

## API
- Konum arama (`search`), listeleme (`locations`) ve `location_id` ile namaz vakitlerini çekme
- Diyanet uyumlu vakitler: https://prayertimes.api.abdus.dev/

## Temel Özellikler
- Konum seçimi (ülke/şehir/ilçe)
- Her vakit için öncesi/sonrası sessize alma süresi
- Cuma öğle vakti için ayrı süreler
- Sessiz modu tercihi: titreşimsiz sessiz veya sessiz+titreşim
- Otomatik izin kontrolü ve alarm planlama

## Çalışma Notları / İzinler
- DND (Rahatsız Etmeyin) izni gerekir; servis başlatırken kullanıcıdan istenir.
- Exact alarm izinleri (SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM) ve WAKE_LOCK manifest’te tanımlıdır.
- Ağ izinleri (INTERNET, ACCESS_NETWORK_STATE) gereklidir.
- Android 13+ için bildirim izni (POST_NOTIFICATIONS) runtime istenir.
- Pil optimizasyonu sorunları için: https://dontkillmyapp.com/

## Build (Docker)
```bash
docker run --rm -v "${PWD}:/app" prayer-time-muter
```

### Tek seferlik kullanılan yardımcı komutlar
Gradle wrapper’ı jdk17 ile oluşturmak ve imajı build etmek için:
```bash
docker run --rm -v "${PWD}:/work" -w /work gradle:8.2-jdk17 gradle wrapper --gradle-version 8.2
docker build -t prayer-time-muter .
```

Çıktı APK: `app/build/outputs/apk/debug/app-debug.apk`

## Lisans
AGPL-3.0. Ayrıntılar için [LICENSE](LICENSE).
