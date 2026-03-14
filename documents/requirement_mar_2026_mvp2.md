# Requirements — March 2026 — MVP 2

> **ต่อเนื่องจาก:** [MVP 1](./requirement_mar_2026.md) — PromptPay + Document Submission & Delivery Tracking

---

## MVP 2 — Credit / Debit Card & Internet Banking

| Feature | รายละเอียด |
|---------|-----------|
| Credit / Debit Card | Visa, Mastercard, JCB — token-based, 3D Secure, Refund |
| Internet Banking | SCB, KBank, BBL, BAY, KTB, TTB — redirect flow |
| Cross-cutting Concerns | Idempotency, Retry, Audit logging, Webhook signature, E2E test |

---

### 1. Credit / Debit Card

**ภาพรวม**
ผู้ใช้กรอกข้อมูลบัตรผ่าน secure payment form (hosted fields หรือ tokenization) รองรับ Visa, Mastercard, JCB

**Functional Requirements**
- ไม่จัดเก็บข้อมูลบัตรในระบบ (PCI DSS compliant) — ใช้ token จาก payment provider แทน
- รองรับ 3D Secure (OTP) สำหรับธุรกรรมที่ต้องยืนยันตัวตน
- รองรับการ refund บางส่วน (partial refund) และเต็มจำนวน
- บันทึก masked card number (เช่น `**** **** **** 1234`) และ card brand สำหรับแสดงผล

**API Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/payment/card/charge` | ชำระเงินด้วยบัตร (ส่ง token) |
| POST | `/api/payment/card/refund` | คืนเงิน |
| POST | `/api/payment/card/webhook` | รับ callback จาก payment provider |
| GET | `/api/payment/card/{transactionId}/status` | ตรวจสอบสถานะ transaction |

**Request — ชำระเงิน**
```json
{
  "orderId": "string",
  "amount": 1500.00,
  "currency": "THB",
  "cardToken": "string",
  "description": "string"
}
```

**Response**
```json
{
  "transactionId": "string",
  "status": "SUCCESS | PENDING_3DS | FAILED",
  "redirectUrl": "string (3DS redirect URL, if required)",
  "maskedCard": "**** **** **** 1234",
  "cardBrand": "VISA | MASTERCARD | JCB",
  "amount": 1500.00,
  "paidAt": "2026-03-07T10:00:00+07:00"
}
```

---

### 2. Internet Banking (Direct Bank Transfer)

**ภาพรวม**
ผู้ใช้เลือกธนาคารแล้ว redirect ไปยังหน้า online banking เพื่ออนุมัติการโอนเงิน รองรับ SCB, KBank, BBL, BAY, KTB, TTB

**Functional Requirements**
- แสดงรายการธนาคารที่รองรับให้ผู้ใช้เลือก
- Redirect ผู้ใช้ไปหน้า banking ของธนาคาร และรับ callback เมื่อชำระสำเร็จหรือยกเลิก
- อัปเดตสถานะ order ผ่าน webhook callback อัตโนมัติ
- แจ้งเตือนผู้ใช้ผ่าน push notification (FCM) เมื่อชำระสำเร็จ

**API Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/payment/banking/banks` | ดึงรายการธนาคารที่รองรับ |
| POST | `/api/payment/banking/initiate` | เริ่มต้น banking session และรับ redirect URL |
| POST | `/api/payment/banking/webhook` | รับ callback จากธนาคาร |
| GET | `/api/payment/banking/{transactionId}/status` | ตรวจสอบสถานะ transaction |

**Request — เริ่มต้น session**
```json
{
  "orderId": "string",
  "amount": 1500.00,
  "currency": "THB",
  "bankCode": "SCB | KBANK | BBL | BAY | KTB | TTB",
  "returnUrl": "string",
  "cancelUrl": "string"
}
```

**Response**
```json
{
  "transactionId": "string",
  "redirectUrl": "string",
  "expiredAt": "2026-03-07T10:15:00+07:00",
  "status": "PENDING"
}
```

---

## Payment Status (ใช้ร่วมกันทุก channel)

| Status | ความหมาย |
|--------|----------|
| `PENDING` | รอการชำระเงิน |
| `PROCESSING` | กำลังประมวลผล |
| `SUCCESS` | ชำระสำเร็จ |
| `FAILED` | ชำระไม่สำเร็จ |
| `EXPIRED` | หมดอายุ |
| `REFUNDED` | คืนเงินแล้ว (บางส่วน/เต็มจำนวน) |
| `CANCELLED` | ยกเลิกโดยผู้ใช้ |

---

## Entity: PaymentTransaction

> ใช้ร่วมกับ MVP 1 (PromptPay) — entity เดียวกัน แค่ต่าง `channel`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | PK |
| `orderId` | String | อ้างอิง order |
| `channel` | Enum | `PROMPTPAY`, `CREDIT_CARD`, `INTERNET_BANKING` |
| `amount` | Decimal(10,2) | จำนวนเงิน |
| `currency` | String | สกุลเงิน (default: THB) |
| `status` | Enum | ดูตาราง Payment Status |
| `providerTransactionId` | String | Transaction ID จาก payment provider |
| `providerResponse` | Text | JSON response ดิบจาก provider |
| `bankCode` | String | รหัสธนาคาร (กรณี internet banking) |
| `maskedCard` | String | masked card number (กรณี credit card) |
| `cardBrand` | String | brand ของบัตร (กรณี credit card) |
| `expiredAt` | DateTime | วันหมดอายุของ QR / session |
| `paidAt` | DateTime | วันที่ชำระสำเร็จ |
| `createdAt` | DateTime | วันที่สร้าง record |
| `updatedAt` | DateTime | วันที่อัปเดตล่าสุด |

---

## Non-Functional Requirements

- Webhook endpoint ต้องตรวจสอบ signature / secret key จาก payment provider ทุกครั้ง
- Transaction ทุกรายการต้อง log ทั้ง request และ response เพื่อการ audit
- Retry mechanism สำหรับ webhook ที่ล้มเหลว (max 3 ครั้ง, exponential backoff)
- Idempotency key บน webhook เพื่อป้องกันการประมวลผลซ้ำ
- รองรับ HTTPS เท่านั้นบน callback/webhook URL

---

## Task Breakdown — MVP 2

> **สมมติฐาน:** 1 man-day = 8 ชั่วโมง | Backend (Java/Spring Boot) เท่านั้น

**Phase 2 — Credit / Debit Card**

| # | Task | รายละเอียด | Man-days |
|---|------|-----------|----------|
| 2.1 | Integrate Card Charge API | เรียก provider token-based charge, handle 3DS redirect | 2 |
| 2.2 | API: `POST /card/charge` | Controller + Service + DTO + validation | 1 |
| 2.3 | 3D Secure flow | handle `PENDING_3DS` status, return redirect URL ให้ frontend | 1 |
| 2.4 | Refund API: `POST /card/refund` | partial + full refund, update status | 1 |
| 2.5 | Webhook handler | รับ callback, ตรวจสอบ signature, อัปเดต status | 1 |
| 2.6 | API: `GET /card/{id}/status` | ตรวจสอบสถานะ | 0.5 |
| 2.7 | Unit test + Integration test | ครอบคลุม 3DS, refund, failed scenarios | 1.5 |
| **Phase 2 รวม** | | | **8** |

**Phase 3 — Internet Banking**

| # | Task | รายละเอียด | Man-days |
|---|------|-----------|----------|
| 3.1 | API: `GET /banking/banks` | master data ธนาคารที่รองรับ (static / DB) | 0.5 |
| 3.2 | Integrate Banking Initiate API | เรียก provider สร้าง session + redirect URL | 1.5 |
| 3.3 | API: `POST /banking/initiate` | Controller + Service + DTO | 1 |
| 3.4 | Webhook handler | รับ callback, ตรวจสอบ signature, อัปเดต status | 1 |
| 3.5 | API: `GET /banking/{id}/status` | ตรวจสอบสถานะ | 0.5 |
| 3.6 | FCM notification | แจ้งเตือนเมื่อชำระสำเร็จ | 0.5 |
| 3.7 | Unit test + Integration test | ครอบคลุม success, cancel, timeout | 1 |
| **Phase 3 รวม** | | | **5** |

**Phase 4 — Cross-cutting Concerns** (ครอบคลุมทุก channel)

| # | Task | รายละเอียด | Man-days |
|---|------|-----------|----------|
| 4.1 | Idempotency key | ป้องกัน webhook ซ้ำ (Redis หรือ DB unique key) | 1 |
| 4.2 | Retry mechanism | retry webhook ที่ล้มเหลว (max 3, exponential backoff) | 1 |
| 4.3 | Audit logging | log request/response ทุก transaction ผ่าน Logback | 0.5 |
| 4.4 | Webhook signature validation | centralize signature check ทุก channel | 0.5 |
| 4.5 | Swagger documentation | เพิ่ม annotation ทุก payment endpoint | 0.5 |
| 4.6 | End-to-end test บน sandbox | ทดสอบทุก channel บน sandbox จริง | 2 |
| **Phase 4 รวม** | | | **5.5** |

---

**สรุป MVP 2**

| Phase | รายละเอียด | Man-days | ราคา (1,200 บาท/MD) |
|-------|-----------|----------|------------------|
| Phase 2 | Credit / Debit Card | 8 | 9,600 บาท |
| Phase 3 | Internet Banking | 5 | 6,000 บาท |
| Phase 4 | Cross-cutting Concerns | 5.5 | 6,600 บาท |
| **รวม** | | **18.5** | **22,200 บาท** |
| **บวก buffer 20%** | | **~22** | **~26,400 บาท** |

---

**Timeline — MVP 2** (~4–5 สัปดาห์ รวม buffer | เริ่มหลัง MVP 1 ส่งมอบ)

| สัปดาห์ | Focus | Tasks | MD |
|---------|-------|-------|----|
| Week 1 | Credit Card Integration | P2.1 Card Charge API, P2.2 POST /card/charge, P2.3 3D Secure flow | 4 |
| Week 2 | Credit Card Complete | P2.4 Refund API, P2.5 Webhook, P2.6 Status API, P2.7 Tests | 5.5 |
| Week 3 | Internet Banking | P3.1 Bank list, P3.2 Banking Initiate, P3.3 POST /banking/initiate, P3.4 Webhook | 4 |
| Week 4 | Banking Complete + Cross-cutting | P3.5–P3.7, P4.1–P4.4 Idempotency + Retry + Audit + Signature | 4 |
| Week 5 (buffer) | Finalize + Buffer | P4.5 Swagger, P4.6 E2E test + Buffer | ~4 |
| **รวม** | | | **~21.5** |

> **Milestone:**
> - ✅ Week 2: Credit Card ชำระ + Refund + Webhook ใช้งานได้ครบ
> - ✅ Week 3: Internet Banking redirect flow ใช้งานได้
> - ✅ Week 4: Cross-cutting concerns ครบ (Idempotency, Retry, Audit)
> - ✅ Week 5: MVP 2 ส่งมอบครบ ผ่าน E2E sandbox test

---

> **หมายเหตุ:**
> - ตัวเลขนี้ **ไม่รวม** งาน Frontend / Mobile (UI form, card input, redirect handling)
> - Phase 2 (Credit Card) ใช้เวลามากสุดเพราะ 3DS flow มีความซับซ้อน
> - หาก provider รองรับทั้ง 2 channel ในชุด SDK เดียว (เช่น Omise, 2C2P) จะช่วยลด integration time ได้
