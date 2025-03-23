# Database Manager

Basit ve esnek Java veritabanı yönetim kütüphanesi. SQLite ve MySQL veritabanlarıyla çalışabilir.

## Özellikler

- SQLite ve MySQL desteği
- Akıcı API tasarımı (Fluent API)
- Asenkron veritabanı işlemleri
- Kolay tablo oluşturma
- Güçlü sorgu oluşturucu
- Basit veri ekleme ve sorgulama

## Başlangıç

### Gereksinimler

- Java 8 veya üzeri
- SQLite JDBC (org.sqlite.JDBC)
- MySQL JDBC (com.mysql.cj.jdbc.Driver)

### Kurulum

1. Projenizi oluşturun
2. Gerekli JDBC sürücülerini ekleyin
3. DBManager sınıfını içe aktarın

### Örnek Kullanım

#### SQLite Veritabanı Oluşturma

```java
// SQLite veritabanı oluştur
DBManager db = new DBManager("veritabani.db");
db.connect();
```

#### MySQL Veritabanı Oluşturma

```java
// MySQL veritabanı oluştur
DBManager db = new DBManager("veritabani_adi", "localhost", "kullanici_adi", "sifre");
db.connect();
```

#### Tablo Oluşturma

```java
db.createTable("kullanicilar")
    .addString("kullanici_adi", 50)
    .addString("email", 100)
    .addString("ad")
    .addString("soyad")
    .addIntegerDefault("giris_sayisi", 0)
    .addBooleanDefault("aktif", false)
    .createOrReplace();
```

#### Veri Ekleme

```java
long id = db.insert("kullanicilar")
    .setString("kullanici_adi", "ahmetk")
    .setString("email", "ahmet@ornek.com")
    .setString("ad", "Ahmet")
    .setString("soyad", "Kaya")
    .setInteger("giris_sayisi", 5)
    .setBoolean("aktif", true)
    .execute();
```

#### Asenkron Veri Ekleme

```java
db.insert("kullanicilar")
    .setString("kullanici_adi", "mehmetc")
    .setString("email", "mehmet@ornek.com")
    .setString("ad", "Mehmet")
    .setString("soyad", "Can")
    .executeAsync(id -> {
        System.out.println("Kullanıcı eklendi, ID: " + id);
    });
```

#### Veri Sorgulama

```java
// ID ile sorgulama
if (db.select("kullanicilar", 1)) {
    String kullaniciAdi = db.getString("kullanici_adi", "");
    String email = db.getString("email", "");
    int girisSayisi = db.getInteger("giris_sayisi", 0);
}

// QueryBuilder ile sorgulama
if (db.query("kullanicilar").where("kullanici_adi", "ahmetk").first()) {
    String email = db.getString("email", "");
}
```

#### Çoklu Veri Sorgulama

```java
List<Map<String, Object>> aktifKullanicilar = db.query("kullanicilar")
    .where("aktif", true)
    .orderBy("kullanici_adi", true)
    .get();

for (Map<String, Object> kullanici : aktifKullanicilar) {
    System.out.println(kullanici.get("kullanici_adi"));
}
```

#### Veri Güncelleme

```java
String sql = "UPDATE kullanicilar SET giris_sayisi = giris_sayisi + 1 WHERE id = ?";
int etkilenenSatir = db.executeUpdate(sql, 1);
```

#### Pratik Veri Güncelleme

```java
// ID ile veri güncelleme
db.insert("kullanicilar")  // insert builder'ı güncelleme için de kullanabilirsiniz
    .setString("kullanici_adi", "ahmetk_yeni")
    .setBoolean("aktif", false)
    .setInteger("giris_sayisi", 10)
    .executeUpdate("id = ?", 1);  // koşulu belirtin

// QueryBuilder ile güncelleme
db.query("kullanicilar")
    .where("email", "ahmet@ornek.com")
    .update("aktif", false);

// Birden fazla alanı güncelleme
db.query("kullanicilar")
    .where("id", 1)
    .update(new String[]{"ad", "soyad"}, new Object[]{"Ahmet Yeni", "Kaya Yeni"});
```

#### Asenkron Veri Güncelleme

```java
db.executeUpdateAsync(
    "UPDATE kullanicilar SET giris_sayisi = ? WHERE id = ?",
    sonuc -> {
        System.out.println(sonuc + " kayıt güncellendi");
    },
    15, 1
);

// QueryBuilder ile asenkron güncelleme
db.query("kullanicilar")
    .where("aktif", true)
    .updateAsync("son_giris", new java.util.Date(), sonuc -> {
        System.out.println("Güncellenen kayıt sayısı: " + sonuc);
    });
```

#### Bağlantıyı Kapatma

```java
db.close();
```

## Gelişmiş Kullanım

### Sorgu Oluşturucu (QueryBuilder)

```java
// Çeşitli sorgular
db.query("kullanicilar")
    .where("aktif", true)
    .whereGreaterThan("giris_sayisi", 5)
    .orderBy("ad", true)
    .limit(10)
    .get();

// LIKE sorgusu
db.query("kullanicilar")
    .whereLike("email", "%@gmail.com")
    .get();

// OR koşulu
db.query("kullanicilar")
    .where("aktif", true)
    .orWhere("giris_sayisi", 0)
    .get();
```

### Asenkron İşlemler

Tüm temel veritabanı işlemleri asenkron olarak da gerçekleştirilebilir:

```java
// Asenkron bağlantı
db.connectAsync(basarili -> {
    if (basarili) {
        System.out.println("Bağlantı başarılı!");
    }
});

// Asenkron sorgu
db.query("kullanicilar")
    .where("aktif", true)
    .getAsync(sonuc -> {
        for (Map<String, Object> kullanici : sonuc) {
            System.out.println(kullanici.get("kullanici_adi"));
        }
    });
```




## Kullanım

### Maven ile Kullanım

JitPack üzerinden `DatabaseManager` kütüphanesini Maven projenize eklemek için aşağıdaki adımları takip edebilirsiniz:

1. **JitPack Repository'yi ekleyin:**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://www.jitpack.io</url>
    </repository>
</repositories>
```

2. **Bağımlılığı ekleyin:**

```xml
<dependency>
    <groupId>com.github.exclover</groupId>
    <artifactId>DatabaseManager</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Gradle ile Kullanım

Gradle ile `DatabaseManager` kütüphanesini projeye dahil etmek için şu adımları takip edin:

1. **JitPack Repository'yi ekleyin:**

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

2. **Bağımlılığı ekleyin:**

```gradle
dependencies {
    implementation 'com.github.exclover:DatabaseManager:1.0-SNAPSHOT'
}
```

Her iki yöntemle de projede `DatabaseManager`'ı kullanabilirsiniz. JitPack, projenin her sürümü için otomatik olarak derlemeler sağlar, bu yüzden en güncel sürümle çalışabilirsiniz.



## Lisans

Bu kütüphane MIT lisansı altında lisanslanmıştır. 
