<p align="center">
  <img src="app/src/main/res/drawable-nodpi/ic_launcher_art.png" width="180" alt="HyperOS Gestures Activator logo">
</p>

<h1 align="center">HyperOS Gestures Activator</h1>

<p align="center">
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/actions/workflows/android.yml"><img src="https://github.com/GloriousTR/HyperOS-Gestures-Activator/actions/workflows/android.yml/badge.svg" alt="Android Build and Release"></a>
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/tag/v1.2.0"><img src="https://img.shields.io/github/v/release/GloriousTR/HyperOS-Gestures-Activator" alt="Latest release"></a>
</p>

<p align="center">
  HyperOS 3'ün yerel tam ekran hareketlerini üçüncü taraf launcher'larla kullanılabilir
  tutan LSPosed modülü.
</p>

> [!IMPORTANT]
> Bu proje root ve modern libxposed API 102 destekli LSPosed/Vector kurulumu gerektirir.
> Sistem bileşenlerine hook uygular; yalnız uyumlu HyperOS cihazlarda ve geri dönüş
> yöntemi hazırken kullanın.

## v1.2.0 ile gelenler

- **Geri:** Sol veya sağ kenardan içeri kaydırma.
- **Ana ekran:** Alt kenardan hızlıca yukarı kaydırma; seçili üçüncü taraf HOME açılır.
- **Son uygulamalar:** Yukarı kaydırıp bekletme; Xiaomi Overview/Recents yolu açılır.
- **Hızlı uygulama geçişi:** Alt hareket alanında yatay kaydırma; son iki uygun uygulama
  arasında iki yönde geçiş.
- **MTZ Studio tarzı Canlı Tanılama:** Başarılı, başarısız ve bilgi olaylarını canlı
  konsolda görüntüleme; filtreleme, anlık sistem kaydı ve UTF-8 rapor dışa aktarma.
- **Güvenli kapatma:** Önceki gezinme ayarını saklama ve tek dokunuşla geri yükleme.
- **Sistem dili desteği:** İngilizce ve Türkçe dahil 21 Android uygulama dili.
- **Çin ROM desteği:** `com.miui.home` paketli resmi Xiaomi Launcher motoru.
- **Yeni üst menü:** MTZ Studio ile uyumlu üç çizgili, kart tabanlı menü; yinelenen
  ana sayfa araçları kaldırıldı.
- **Tam tema sistemi:** Sistem varsayılanı, AMOLED, Light Mode ve Dark Mode renk
  seçenekleri; bunlardan bağımsız Varsayılan ve Aero Glass içerik tasarımları.
- **Yeni kontrol merkezi:** Ortalanmış hareketle gezinme durum kapsülü, Root yöneticisi
  ve Vector/LSPosed kısayolları, daha kompakt köşe yapısı ve profesyonel kart düzeni.
- **Ayrı durum sayfaları:** Sistem Sağlığı ve Hareketler kartları ayrıntılarını artık
  kendi tema uyumlu sayfalarında gösterir.
- **İletişim:** Canlı Tanılama ve Hakkında ekranlarından Telegram üzerinden
  [@glorioustr](https://t.me/glorioustr) ile doğrudan iletişim.
- **Tek GitHub workflow:** `main` ve pull request gönderilerinde CI; `v*` tag'lerinde
  imzalı APK, SHA-256 özeti ve GitHub Release otomatik oluşturulur.

Uygulama `KEYCODE_HOME` ya da sahte dokunma kullanmaz. Alt ve yan giriş pencereleri
Xiaomi'nin kendi gesture motorunda kalır. Üçüncü taraf HOME'da yatay hareket, yönü
belli olana kadar kısa süre tamponlanır; Xiaomi'nin bu durumda kararsız kalabilen
RecentsAnimation tüketicisi başlatılmadan hedef Android'in gerçek son görev listesinden
çözülür. Sonuç Live Diagnostics'e `quick-switch` kategorisiyle yazılır.

## Uyumluluk

| Bileşen | Durum |
|---|---|
| HyperOS 3 / Android 16 | Desteklenen hedef |
| Xiaomi/POCO Global Launcher (`com.mi.android.globallauncher`) | Desteklenen gesture motoru |
| Xiaomi China Launcher (`com.miui.home`) | v1.1.0 ile desteklenen gesture motoru |
| Smart Launcher | Cihaz üzerinde doğrulandı |
| Diğer üçüncü taraf launcher'lar | Standart Android HOME intent'i kullandıkları sürece tasarım gereği desteklenir; cihaz/firmware testi gerekir |
| libxposed API | 102 |
| Android alt sınırı | API 35 |

v1.0.0 cihaz doğrulaması `2511FPC34G` üzerinde, Xiaomi/POCO Launcher
`RELEASE-6.01.05.2407-06081949` ile yapıldı. Home, Recents, Back ve iki yönlü hızlı
uygulama geçişi çalıştı; launcher sürecinde çökme görülmedi.

Animasyonun görev yüzeyi bölümü HyperOS firmware'ine aittir. Üçüncü taraf launcher
Xiaomi ana ekranındaki uygulama simgesi/hedef koordinatlarını sağlamadığı için,
Xiaomi Launcher'a özel **simgeye kapanma Home animasyonu birebir üretilemez**.
Hızlı geçiş bırakma anında sistem görev animasyonuyla tamamlanır; üçüncü taraf HOME'da
Xiaomi Launcher'ın etkileşimli kart-takip animasyonu kullanılmaz. Donmayı önlemek için
650 ms içindeki aşırı hızlı tekrarlar yok sayılır ve Live Diagnostics'e kaydedilir.

## Kurulum

1. [v1.2.0 sürüm sayfasından](https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/tag/v1.2.0)
   `HyperOS-Gestures-Activator-v1.2.0.apk` dosyasını yükleyin.
2. Uygulamaya `WRITE_SECURE_SETTINGS` iznini bir kez verin:

   ```powershell
   adb shell pm grant dev.glorioustr.hyperosgesturesactivator android.permission.WRITE_SECURE_SETTINGS
   ```

3. Vector/LSPosed içinde modülü etkinleştirin. Sabit kapsamda SystemUI ve cihazınızda
   kurulu olan Xiaomi Launcher paketi seçili olmalıdır:

   - Sistem Arayüzü — `com.android.systemui`
   - POCO/Xiaomi Global Başlatıcı — `com.mi.android.globallauncher`
   - Xiaomi China Başlatıcı — `com.miui.home` (cihazda mevcutsa)

4. Cihazı yeniden başlatın.
5. Uygulamada **Sistem Sağlığı** sayfasında SystemUI ve Xiaomi Launcher motoru
   **Hazır** göründüğünde ana ekrandaki **Hareketle gezinme** kapsülünden durumu açın.
6. Sorun yaşarsanız aynı kapsülden özelliği güvenli biçimde kapatın ve menüden
   **Canlı Tanılama** ekranını kontrol edin veya raporu `@glorioustr` ile paylaşın.

> [!NOTE]
> Debug APK kullanıyorsanız izin komutundaki paket adı
> `dev.glorioustr.hyperosgesturesactivator.debug` olur.

## Hızlı hareketler

| Hareket | Sonuç |
|---|---|
| Sol/sağ kenardan içeri | Geri |
| Alttan hızlı yukarı | Ana ekran |
| Alttan yukarı ve beklet | Son uygulamalar |
| Alt kenarda sola veya sağa | Önceki uygulamaya hızlı geçiş |

## Canlı Tanılama

Tanılama ekranı sol üstteki üç çizgili menüden açılır. Şunları kaydeder:

- SystemUI ve Xiaomi Launcher hook hazırlığı;
- varsayılan HOME bileşeni ve gezinme ayarları;
- navbar/navigation overlay durumu;
- Home ve Recents yönlendirme sonuçları;
- hızlı geçiş hedef görev kimliği, bileşeni ve başarı/hata sonucu;
- hata stack trace'i ile process/thread kaynağı.

MTZ Studio ile uyumlu ekran; durum rozeti, renkli olay konsolu, sonuç filtreleri ve
ayrı tanılama araçları kartlarından oluşur. Ekran performans için son 1000 olayı gösterir. Dışa aktarılan rapor, yerel
device-protected SQLite veritabanındaki tüm olayları içerir. Kayıtlar yalnız kullanıcı
**Temizle** işlemini onayladığında silinir.

## Derleme

Gereksinimler: JDK 17 ve Android SDK 36.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

Çıktılar:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

İmzalı release için depo kökünde Git'e eklenmeyen `keystore.properties` dosyası
kullanılır:

```properties
storeFile=keystore/hyperos-gestures-release.jks
storePassword=...
keyAlias=hga-release
keyPassword=...
```

APK doğrulamasında aşağıdaki libxposed metadatası bulunmalıdır:

```text
META-INF/xposed/java_init.list
META-INF/xposed/module.prop
META-INF/xposed/scope.list
```

GitHub'da tek workflow kullanılır: `.github/workflows/android.yml`. Bir sürüm
oluşturmak için SemVer biçiminde tag gönderilir:

```powershell
git tag v1.2.0
git push origin v1.2.0
```

Tag çalışması, repository Actions secrets içindeki release anahtarıyla APK'yı
imzalar, LSPosed metadata dosyalarını doğrular ve APK ile SHA-256 dosyasını GitHub
Release'e ekler.

Teknik cihaz araştırması ve test matrisi:
[docs/hyperos3-investigation.md](docs/hyperos3-investigation.md)

## Tasarım ve güvenlik ilkeleri

- Statik LSPosed kapsamı yalnız `com.android.systemui` ile resmi Global/POCO
  (`com.mi.android.globallauncher`) ve Çin ROM (`com.miui.home`) Xiaomi Launcher
  paketleriyle sınırlıdır.
- Kullanıcının bağımsız MiuiBackGestureHook kurulumu ve ayarları değiştirilmez.
- Hook bulunamadığında modül mümkün olduğunca zarif biçimde devam eder ve hatayı
  Live Diagnostics'e kaydeder.
- Tanılama yayınları yalnız gerçek SystemUI ile Global veya Çin ROM Xiaomi Launcher
  UID'lerinden kabul edilir.
- Aktivasyon kapatıldığında önceki navigation değeri geri yüklenir.

## Lisans

[Apache License 2.0](LICENSE). İlk LSPosed entegrasyon yaklaşımında
[MiuiBackGestureHook 0.4.0](https://github.com/wxxsfxyzm/MiuiBackGestureHook/tree/0.4.0)
incelenmiştir; atıflar [NOTICE](NOTICE) dosyasındadır.
