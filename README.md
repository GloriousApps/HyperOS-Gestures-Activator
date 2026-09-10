# HyperOS Gestures Activator

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/ic_launcher_art.png" alt="HyperOS Gestures Activator" width="220">
</p>

<p align="center">
  HyperOS 3'ün yerel tam ekran hareketlerini üçüncü taraf başlatıcılarla kullanın.<br>
  Geri, Ana ekran, Son uygulamalar ve hızlı uygulama geçişini Xiaomi'nin hareket motoruyla koruyun.
</p>

<p align="center">
  <strong>🇹🇷 Türkçe</strong> · <a href="readme_en.md">🇬🇧 English</a>
</p>

<p align="center">
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/latest"><img alt="Son sürüm" src="https://img.shields.io/github/v/release/GloriousTR/HyperOS-Gestures-Activator?display_name=tag&style=for-the-badge&color=7357e6"></a>
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/actions/workflows/android.yml"><img alt="Android derlemesi" src="https://img.shields.io/github/actions/workflow/status/GloriousTR/HyperOS-Gestures-Activator/android.yml?branch=main&style=for-the-badge&label=Android"></a>
  <img alt="Android 15+" src="https://img.shields.io/badge/Android-15%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white">
</p>

<p align="center">
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/tag/v1.2.0"><strong>v1.2.0 APK indir</strong></a>
  · <a href="#uyumluluk">Uyumluluk</a>
  · <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/issues">Sorun bildir</a>
  · <a href="https://t.me/glorioustr">Telegram</a>
</p>

> [!IMPORTANT]
> Uygulama root, modern libxposed API 102 destekli Vector/LSPosed ve bir defalık
> `WRITE_SECURE_SETTINGS` yetkisi gerektirir. Sistem bileşenlerine müdahale ettiği
> için yalnız uyumlu HyperOS cihazlarda ve geri dönüş yöntemi hazırken kullanın.

## Neler yapar?

- **Tam ekran hareketleri:** Kenardan Geri, alttan Ana ekran ve yukarıda bekleterek Son uygulamalar hareketlerini üçüncü taraf başlatıcılarda etkin tutar.
- **Hızlı uygulama geçişi:** Alt hareket alanında sağa veya sola kaydırarak son iki uygun uygulama arasında geçiş yapar.
- **Xiaomi hareket motoru:** Sahte dokunma ve `KEYCODE_HOME` yerine SystemUI, WM Shell ve Xiaomi'nin yerel gesture yollarını kullanır.
- **Sistem Sağlığı:** Root yöneticisi, Vector/LSPosed, SystemUI, Xiaomi Launcher motoru ve gerekli izinlerin durumunu gösterir.
- **Live Diagnostics:** Başarı, hata ve bilgi olaylarını canlı kaydeder; filtreleme, sistem anlık görüntüsü ve UTF-8 rapor dışa aktarma sunar.
- **Güvenli kapatma:** Önceki gezinme ayarını saklar ve özellik kapatıldığında geri yükler.
- **Kişiselleştirilebilir arayüz:** Varsayılan veya Aero Glass tasarım; Sistem varsayılanı, AMOLED, Light Mode ve Dark Mode renk seçenekleri sunar.
- **Çoklu dil:** Türkçe ve İngilizce dahil 21 Android sistem dilini destekler.

## Hareketler

| Hareket | Sonuç |
| --- | --- |
| Sol veya sağ kenardan içeri kaydırma | Geri |
| Alt kenardan hızlıca yukarı kaydırma | Varsayılan Ana ekran |
| Alt kenardan yukarı kaydırıp bekletme | Son uygulamalar |
| Alt hareket alanında sağa veya sola kaydırma | Önceki uygulamaya hızlı geçiş |

## Uyumluluk

| Bileşen | Durum |
| --- | --- |
| HyperOS 3 / Android 16 | Desteklenen ve test edilen hedef |
| Xiaomi/POCO Global Launcher (`com.mi.android.globallauncher`) | Desteklenen gesture motoru |
| Xiaomi China Launcher (`com.miui.home`) | Desteklenen gesture motoru |
| Smart Launcher | Gerçek cihazda doğrulandı |
| Diğer üçüncü taraf başlatıcılar | Standart Android HOME intent'i kullandıklarında tasarım gereği desteklenir; ROM ve cihaz testi gerekir |
| Vector/LSPosed | Modern libxposed API 102 gerekli |
| En düşük Android sürümü | Android 15 / API 35 |

v1.0.0 cihaz doğrulaması Xiaomi `2511FPC34G` ve Xiaomi/POCO Launcher
`RELEASE-6.01.05.2407-06081949` üzerinde yapıldı. Geri, Ana ekran, Son uygulamalar
ve iki yönlü hızlı geçiş çalıştı; launcher sürecinde çökme görülmedi.

Xiaomi Launcher'a özel uygulama simgesine kapanma animasyonu, üçüncü taraf
başlatıcının simge koordinatları SystemUI'a verilmediği için birebir üretilemez.
Görev yüzeyi ve hızlı geçiş tamamlanma animasyonları firmware tarafından sağlanır.
Donmayı önlemek amacıyla 650 ms içindeki aşırı hızlı tekrarlar güvenli biçimde yok
sayılır ve Live Diagnostics'e kaydedilir.

## Kurulum

1. [v1.2.0 sürümünden](https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/tag/v1.2.0) `HyperOS-Gestures-Activator-v1.2.0.apk` dosyasını indirin ve kurun.
2. Bilgisayarda ADB ile aşağıdaki izni bir kez verin:

   ```powershell
   adb shell pm grant dev.glorioustr.hyperosgesturesactivator android.permission.WRITE_SECURE_SETTINGS
   ```

3. Vector/LSPosed içinde modülü etkinleştirin. SystemUI ile cihazınızda kurulu resmi Xiaomi Launcher paketinin kapsamda olduğundan emin olun:

   - Sistem Arayüzü — `com.android.systemui`
   - Xiaomi/POCO Global Launcher — `com.mi.android.globallauncher`
   - Xiaomi China Launcher — `com.miui.home`

4. Cihazı yeniden başlatın.
5. **Sistem Sağlığı** sayfasında gerekli bileşenlerin hazır olduğunu doğrulayın.
6. Ana ekrandaki **Hareketle gezinme** kapsülünden özelliği açın.

> [!NOTE]
> Debug APK için izin komutundaki paket adı `dev.glorioustr.hyperosgesturesactivator.debug` olmalıdır.

## Live Diagnostics

Üç çizgili üst menüden açılan Live Diagnostics ekranı SystemUI ve Xiaomi Launcher
hook hazırlığını; HOME bileşenini; navigation ayarlarını; Ana ekran, Son uygulamalar
ve hızlı geçiş sonuçlarını; hedef görev, süreç, thread ve hata ayrıntılarını kaydeder.

Ekran son 1000 olayı gösterir. Dışa aktarılan rapor yerel, device-protected SQLite
veritabanındaki tüm olayları içerir. Kayıtlar yalnız kullanıcı **Temizle** işlemini
onayladığında silinir. Destek için raporu [@glorioustr](https://t.me/glorioustr)
ile paylaşabilirsiniz.

## Geliştirme

Gereksinimler: JDK 17 ve Android SDK API 36.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

APK içinde `META-INF/xposed/java_init.list`, `module.prop` ve `scope.list`
metadata dosyaları bulunmalıdır. Depodaki tek GitHub Actions workflow'u `main` ve
pull request gönderilerinde test derlemesi yapar; `v*` etiketi gönderildiğinde imzalı
APK, SHA-256 özeti ve GitHub Release kaydı üretir.

Teknik cihaz araştırması ve test matrisi:
[HyperOS 3 araştırma notları](docs/hyperos3-investigation.md).

## Güvenlik ve sorumlu kullanım

- Hook bulunamadığında modül işlemi zorlamaz; durumu Live Diagnostics'e kaydeder.
- Kullanıcının bağımsız MiuiBackGestureHook kurulumu ve ayarları değiştirilmez.
- Tanılama yayınları yalnız beklenen sistem ve Xiaomi Launcher süreçlerinden kabul edilir.
- Özellik kapatıldığında önceki navigation değeri geri yüklenir.

Bu bağımsız proje Xiaomi ile bağlantılı değildir ve Xiaomi tarafından desteklenmez.
HyperOS, MIUI ve Xiaomi ilgili sahiplerinin ticari markalarıdır.

## Lisans

[Apache License 2.0](LICENSE). İlk LSPosed entegrasyon yaklaşımında
[MiuiBackGestureHook 0.4.0](https://github.com/wxxsfxyzm/MiuiBackGestureHook/tree/0.4.0)
incelenmiştir; atıflar [NOTICE](NOTICE) dosyasındadır.

<p align="center">
  HyperOS kullanıcıları için <a href="https://github.com/GloriousTR">GloriousTR</a> tarafından geliştirildi.
</p>
