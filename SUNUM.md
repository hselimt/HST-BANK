<div align="center">

# HST BANK

**Gerçek bir bankanın temel işlevlerini simüle eden backend API'si. Amaç; bankacılık işlemlerini yazılım olarak modellemek ve temel backend kavramlarını uygulamalı göstermektir.**

</div>

<br>

---

## Özellikler

- Validasyonlu kullanıcı kayıt ve giriş sistemi *(e-posta formatı, şifre uzunluğu ve içerik kontrolü, ad/soyad uzunluk kontrolü)*
- Birden fazla hesap türü *(Checkings, Savings)* ve para birimi *(TRY, USD, EUR, GBP)* desteğiyle banka hesabı oluşturma
- Bakiye ve para birimi kontrolüyle hesaplar arası para transferi
- IBAN ile diğer kullanıcılara **ACID uyumlu** transfer
  
  > 💡 **ACID Nedir?**
  > İşlemler zincirleme gerçekleşir. Zincirin bir halkası koparsa tüm işlem geri alınır **(rollback)**

- Harici kur API'sinden canlı döviz kuru çekme harici olarak kur tutan **cache** + **fallback** mekanizmaları
- Gerçek zamanlı kurlarla exchange işlemleri
- Banka kartı ve kredi kartı yönetimi
- Transaction geçmişi ve işlem türüne göre filtreleme
- Tüm finansal veriyi tek seferde döndüren dashboard endpoint'i

---

## Mimari

**RESTful API Mimarisi** — Client ile server arasındaki iletişimin HTTP methodları `(GET, POST, PUT, DELETE)` üzerinden, belirli kurallara göre yapıldığı bir tasarım yaklaşımıdır.

Katmanlar birbirine **Dependency Injection** ile bağlanır, Spring gerekli bağımlılıkları otomatik olarak sağlar.

<div align="center">

```
┌─────────────────────────────────────────────────────────────────────┐
│  🔵  CONTROLLER KATMANI   │  Dış dünyadan gelen istekleri karşılar  │
├─────────────────────────────────────────────────────────────────────┤
│  🟡  SERVICE KATMANI      │  Tüm iş mantığı burada çalışır          │
├─────────────────────────────────────────────────────────────────────┤
│  🟢  REPOSITORY KATMANI   │  Database ile konuşan tek katman        │
└─────────────────────────────────────────────────────────────────────┘
```

</div>

### 🔵 Controller Katmanı
Uygulamanın dış dünyayla konuştuğu noktadır. Gelen isteği karşılar, Service katmanına iletir ve sonucu **HTTP status** olarak döndürür.

| Bazı Status Kodları | Anlam |
|:---:|---|
| `200` | İşlem başarılı |
| `400` | İstek hatalı |
| `403` | Kullanıcı authorize edilmemiş |
| `404` | Kaynak bulunamadı |
| `500` | Sunucu çöktü |

### 🟡 Service Katmanı
Tüm iş mantığının yürütüldüğü ve kuralların tanımlandığı yerdir.
*"Bakiye yeterli mi?"* — *"Para birimleri uyuşuyor mu?"* — *"Bu e-posta kayıtlı mı?"*
gibi tüm kontroller burada yapılır.

### 🟢 Repository Katmanı
Database ile konuşan tek katmandır. Database işlemleri Repository katmanı üzerinden geçer.

---

## Kullanılan Teknolojiler

| Teknoloji | Ne İşe Yarar? | Neden Tercih Edildi? |
|---|---|---|
| ![Spring](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring&logoColor=white) | Java framework'ü | server, database bağlantısı gibi konfigürasyonları otomatik halleder |
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white) | Relational(İlişkisel) Database | ACID garantisiyle finansal işlemlerde veri tutarlılığını sağlar |
| **Spring Data JPA** | ORM(Object to Relational Mapping) | Java sınıflarını DB tablolarına eşler; SQL yazmak yerine Java metod isimleri kullanılır |
| **Lombok** | Kod üretici | Getter, setter, constructor gibi tekrarlayan kodları otomatik oluşturur |
| **RestTemplate** | HTTP client | Harici döviz kuru API'sine istek atmak için kullanılır |

---

<div align="center">
  
## API Endpoint Listesi

**Uygulamanın dışarıya açık kapılarıdır. `POST` veri gönderir, `GET` veri okur**

---

### Auth
| Metod | Endpoint | Ne Yapar? |
|:---:|---|---|
| `POST` | `/api/auth/register` | Yeni kullanıcı kaydı |
| `POST` | `/api/auth/login` | Kullanıcı girişi |

---

### Hesap
| Metod | Endpoint | Ne Yapar? |
|:---:|---|---|
| `POST` | `/api/accounts` | Banka hesabı oluşturur |

---

### Transfer
| Metod | Endpoint | Ne Yapar? |
|:---:|---|---|
| `POST` | `/api/transfers` | İki hesap arasında para transferi |
| `POST` | `/api/transfers/external` | IBAN ile başka kullanıcıya transfer |
| `GET` | `/api/transfers/history/{id}` | Hesaba ait işlem geçmişi |
| `GET` | `/api/transfers/user/{id}` | Kullanıcının tüm işlemleri |
| `GET` | `/api/transfers/history/{id}/type/{tip}` | İşlemleri türe göre filtreler |

---

### Kart
| Metod | Endpoint | Ne Yapar? |
|:---:|---|---|
| `POST` | `/api/cards` | Debit veya kredi kartı oluşturur |
| `POST` | `/api/cards/{id}/payment` | Kartla ödeme yapar |

---

### Döviz
| Metod | Endpoint | Ne Yapar? |
|:---:|---|---|
| `GET` | `/api/exchange/rates` | Tüm döviz kurlarını listeler |
| `GET` | `/api/exchange/rate` | İki para birimi arasındaki kuru verir |
| `POST` | `/api/exchange` | Hesaplar arası döviz bozdurma |

---

### Dashboard
| Metod | Endpoint | Ne Yapar? |
|:---:|---|---|
| `GET` | `/api/dashboard/user/{id}` | Kullanıcının tüm finansal özetini döner |

</div>
