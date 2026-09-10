# Değişiklik günlüğü

## 1.1.0 — 2026-09-10

- Çin ROM Xiaomi Launcher paketi `com.miui.home` için LSPosed kapsamı ve hook
  desteği eklendi.
- Global/POCO Launcher (`com.mi.android.globallauncher`) desteği korunurken iki resmi
  Xiaomi Launcher paketinden gelen tanılama olayları güvenli biçimde kabul ediliyor.
- Ana sayfadaki yinelenen Sistem Araçları kartı kaldırıldı; Live Diagnostics, anlık
  kayıt ve Hakkında seçenekleri MTZ Studio tarzı üst menüde birleştirildi.
- Üç noktalı düğme, sol üstte üç çizgili menü simgesi ve kart tabanlı menüyle değiştirildi.
- MTZ Studio v4 görsel diliyle uyumlu Varsayılan ve Aero Glass arayüz stilleri eklendi;
  sistemin açık/koyu görünümü otomatik izleniyor.
- Aktif gezinme alanına animasyonlu hareket görseli, Sistem Sağlığı ve Hareketler
  bölümlerine renk kodlu iki sütunlu durum kartları eklendi.
- Görünüm seçimi desteklenen 21 uygulama diline yerelleştirildi.

## 1.0.0 — 2026-09-02

- Üçüncü taraf HOME kullanılırken alt kenarda iki yönlü hızlı uygulama geçişi eklendi.
- Yatay ve dikey alt hareketler, yön belirlenene kadar güvenli bir sınıflandırıcıyla
  ayrılıyor; Home/Recents akışı değiştirilmeden korunuyor.
- Yatay harekette kararsız Xiaomi RecentsAnimation başlatılmadan geçiş hedefi
  Android'in son görev listesinden çözümleniyor.
- Hızlı geçiş hedefi, bileşeni ve başarı/hata sonucu Live Diagnostics'e ekleniyor.
- Ardışık uzak animasyonların sistem girişini kilitlemesini önlemek için hızlı geçişte
  650 ms güvenlik aralığı uygulanıyor.
- Home, Recents ve Back yönlendirmeleri korunuyor.
- Live Diagnostics rapor dışa aktarma ve kalıcı olay geçmişi tamamlandı.
- Profesyonel kontrol paneli, Hakkında ekranı, uyumlu uygulama simgesi ve 21 sistem
  dili desteği eklendi.

## 0.2.0

- Üçüncü taraf launcher ile HyperOS gesture motoru aktivasyonu eklendi.
- Home ve Overview fallback yönlendirmeleri ile güvenli aç/kapat akışı eklendi.

## 0.1.0

- Salt okunur HyperOS gezinme tanılaması ve LSPosed logları eklendi.
