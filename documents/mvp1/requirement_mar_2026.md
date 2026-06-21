# Requirements — March 2026

---

## 1. Document Submission & Delivery Tracking

ระบบจัดการการส่งเอกสาร, ติดตามสถานะ และบริหารที่อยู่จัดส่งของผู้ใช้

### Document Status Flow

```
ตรวจเอกสาร → รอผลจากกรมเจ้าท่า → รอรับเอกสารจากกรมเจ้าท่า → กำลังจัดส่ง → จัดส่งสำเร็จ
```

| Status | ค่า (Enum) | ความหมาย |
|--------|-----------|----------|
| `REVIEWING` | 1 | เจ้าหน้าที่กำลังตรวจเอกสาร |
| `WAITING_AUTHORITY_RESULT` | 2 | รอผลจากกรมเจ้าท่า |
| `WAITING_RECEIVE_FROM_AUTHORITY` | 3 | รอรับเอกสารจากกรมเจ้าท่า |
| `SHIPPING` | 4 | กำลังจัดส่ง |
| `DELIVERED` | 5 | จัดส่งสำเร็จ |

---

### 1.1 การส่งเอกสาร (Document Submission)

**ภาพรวม**
ผู้ใช้ยื่นเอกสารพร้อม application พร้อมระบุที่อยู่จัดส่งเอกสารกลับคืน

**Functional Requirements**
- ผู้ใช้เลือกที่อยู่จัดส่งเอกสาร (ดึงจาก saved addresses หรือกรอกใหม่)
- ระบบสร้าง submission record และเชื่อมกับ application / order
- เจ้าหน้าที่สามารถอัปเดต status และเพิ่ม remark ต่อแต่ละเอกสารได้
- แต่ละเอกสารในการยื่นมี item-level status (`APPROVED` / `REJECTED` / `PENDING`)
- เมื่อ status เปลี่ยน ระบบส่ง FCM notification แจ้งผู้ใช้

**API Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/document-submission` | ยื่นเอกสารพร้อมที่อยู่จัดส่ง |
| GET | `/api/document-submission/{submissionId}` | ดูรายละเอียดการยื่นเอกสาร |
| GET | `/api/document-submission/my` | ดูรายการการยื่นเอกสารของผู้ใช้ |

**Request — ยื่นเอกสาร**
```json
{
  "applicationId": "string",
  "deliveryAddressId": "long (ใช้ที่อยู่ที่บันทึกไว้)",
  "deliveryAddress": {
    "recipientName": "string",
    "phone": "string",
    "addressLine1": "string",
    "addressLine2": "string",
    "subDistrict": "string",
    "district": "string",
    "province": "string",
    "postalCode": "string"
  },
  "documents": [
    {
      "documentType": "string",
      "fileUrl": "string"
    }
  ]
}
```

> หมายเหตุ: ส่ง `deliveryAddressId` หรือ `deliveryAddress` อย่างใดอย่างหนึ่ง

**Response**
```json
{
  "submissionId": "string",
  "status": "REVIEWING",
  "createdAt": "2026-03-07T10:00:00+07:00",
  "documents": [
    {
      "documentType": "string",
      "fileUrl": "string",
      "status": "PENDING",
      "remark": null
    }
  ]
}
```

---

### 1.2 ติดตามสถานะเอกสาร (Document Status Tracking)

**ภาพรวม**
ผู้ใช้สามารถติดตาม status การดำเนินการเอกสารได้แบบ real-time ตั้งแต่ตรวจเอกสารจนถึงจัดส่งสำเร็จ

**Functional Requirements**
- แสดง timeline ของ status พร้อม timestamp แต่ละขั้นตอน
- แสดงรายการเอกสารแต่ละรายการพร้อม item-level status (`APPROVED` / `REJECTED`)
- กรณี `REJECTED` แสดง remark/เหตุผลที่ปฏิเสธ
- กรณี `SHIPPING` แสดง tracking number และ courier ที่ใช้จัดส่ง
- แจ้งเตือน push notification (FCM) เมื่อ status เปลี่ยนทุกขั้นตอน

**API Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/document-submission/{submissionId}/status` | ดูสถานะปัจจุบันและ timeline |
| PUT | `/api/document-submission/{submissionId}/status` | (Admin) อัปเดตสถานะ |
| PUT | `/api/document-submission/{submissionId}/documents/{documentId}` | (Admin) อัปเดต item status พร้อม remark |

**Response — Status Timeline**
```json
{
  "submissionId": "string",
  "currentStatus": "SHIPPING",
  "trackingNumber": "string",
  "courier": "string",
  "timeline": [
    {
      "status": "REVIEWING",
      "label": "ตรวจเอกสาร",
      "occurredAt": "2026-03-07T10:00:00+07:00",
      "remark": "เจ้าหน้าที่กำลังตรวจเอกสาร"
    },
    {
      "status": "WAITING_AUTHORITY_RESULT",
      "label": "รอผลจากกรมเจ้าท่า",
      "occurredAt": "2026-03-08T09:00:00+07:00",
      "remark": null
    }
  ],
  "documents": [
    {
      "documentType": "สำเนาบัตรประชาชน / Passport",
      "fileUrl": "string",
      "status": "APPROVED",
      "remark": null
    },
    {
      "documentType": "ใบรับรองแพทย์",
      "fileUrl": "string",
      "status": "REJECTED",
      "remark": "เอกสารหมดอายุ กรุณาอัปโหลดใหม่"
    }
  ]
}
```

---

### 1.3 ที่อยู่จัดส่งเอกสาร (Delivery Address Management)

**ภาพรวม**
ผู้ใช้สามารถบันทึก/แก้ไข/ลบที่อยู่จัดส่งเอกสารได้หลายที่อยู่ และกำหนดที่อยู่เริ่มต้น

**Functional Requirements**
- ผู้ใช้มีที่อยู่ได้หลายรายการ กำหนด default ได้ 1 รายการ
- รองรับการแก้ไขและลบที่อยู่
- ที่อยู่ที่เคยใช้จัดส่งแล้ว (linked กับ submission) ลบไม่ได้ แต่แก้ไขได้
- Validate postal code ให้เป็นตัวเลข 5 หลัก

**API Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/delivery-address` | ดึงรายการที่อยู่ทั้งหมดของผู้ใช้ |
| POST | `/api/delivery-address` | เพิ่มที่อยู่ใหม่ |
| PUT | `/api/delivery-address/{id}` | แก้ไขที่อยู่ |
| DELETE | `/api/delivery-address/{id}` | ลบที่อยู่ |
| PATCH | `/api/delivery-address/{id}/default` | ตั้งเป็นที่อยู่เริ่มต้น |

**Request — เพิ่ม/แก้ไขที่อยู่**
```json
{
  "recipientName": "string",
  "phone": "string",
  "addressLine1": "string",
  "addressLine2": "string (optional)",
  "subDistrict": "string",
  "district": "string",
  "province": "string",
  "postalCode": "string (5 หลัก)",
  "isDefault": false
}
```

**Response**
```json
{
  "id": 1,
  "recipientName": "string",
  "phone": "string",
  "addressLine1": "string",
  "addressLine2": "string",
  "subDistrict": "string",
  "district": "string",
  "province": "string",
  "postalCode": "string",
  "isDefault": true,
  "createdAt": "2026-03-07T10:00:00+07:00"
}
```

---

### Entities — Document Submission & Delivery

**Entity: DocumentSubmission**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | PK |
| `userId` | Long | FK → User |
| `applicationId` | String | อ้างอิง application / order |
| `status` | Enum | ดูตาราง Document Status Flow |
| `trackingNumber` | String | เลข tracking (กรณีจัดส่ง) |
| `courier` | String | ชื่อบริษัทขนส่ง |
| `deliveryAddressSnapshot` | Text | JSON snapshot ที่อยู่ ณ เวลาที่ยื่น |
| `deliveryAddressId` | Long | FK → DeliveryAddress |
| `createdAt` | DateTime | วันที่ยื่นเอกสาร |
| `updatedAt` | DateTime | วันที่อัปเดตล่าสุด |

**Entity: DocumentSubmissionItem**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | PK |
| `submissionId` | Long | FK → DocumentSubmission |
| `documentType` | String | ประเภทเอกสาร |
| `fileUrl` | String | URL ไฟล์เอกสาร |
| `status` | Enum | `PENDING`, `APPROVED`, `REJECTED` |
| `remark` | String | เหตุผลกรณี REJECTED |
| `reviewedAt` | DateTime | วันที่ตรวจ |

**Entity: DocumentSubmissionStatusHistory**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | PK |
| `submissionId` | Long | FK → DocumentSubmission |
| `status` | Enum | สถานะใหม่ |
| `remark` | String | หมายเหตุ |
| `updatedBy` | Long | FK → User (admin) |
| `occurredAt` | DateTime | เวลาที่เปลี่ยน status |

**Entity: DeliveryAddress**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | PK |
| `userId` | Long | FK → User |
| `recipientName` | String | ชื่อผู้รับ |
| `phone` | String | เบอร์โทร |
| `addressLine1` | String | ที่อยู่บรรทัด 1 |
| `addressLine2` | String | ที่อยู่บรรทัด 2 (optional) |
| `subDistrict` | String | ตำบล/แขวง |
| `district` | String | อำเภอ/เขต |
| `province` | String | จังหวัด |
| `postalCode` | String | รหัสไปรษณีย์ |
| `isDefault` | Boolean | ที่อยู่เริ่มต้น |
| `createdAt` | DateTime | วันที่สร้าง |
| `updatedAt` | DateTime | วันที่อัปเดต |

---

## 2. PromptPay Payment

**ภาพรวม**
ผู้ใช้สามารถชำระเงินผ่าน PromptPay โดยสแกน QR Code ที่ระบบสร้างขึ้น

**Functional Requirements**
- ระบบสร้าง QR Code PromptPay มาตรฐาน EMVCo (Thai QR Payment) ตามจำนวนเงินของ order
- QR Code มีอายุการใช้งานกำหนดได้ (default: 15 นาที)
- เมื่อชำระเงินสำเร็จ ระบบรับ webhook callback และอัปเดตสถานะ order อัตโนมัติ
- แจ้งเตือนผู้ใช้ผ่าน push notification (FCM) เมื่อการชำระเงินสำเร็จหรือหมดอายุ

**API Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/payment/promptpay/qr` | สร้าง QR Code สำหรับ order |
| POST | `/api/payment/promptpay/webhook` | รับ callback จาก payment provider |
| GET | `/api/payment/promptpay/{transactionId}/status` | ตรวจสอบสถานะ transaction |

**Request — สร้าง QR Code**
```json
{
  "orderId": "string",
  "amount": 1500.00,
  "currency": "THB",
  "description": "string"
}
```

**Response**
```json
{
  "transactionId": "string",
  "qrCodeBase64": "string",
  "qrCodeString": "string",
  "amount": 1500.00,
  "expiredAt": "2026-03-07T10:15:00+07:00",
  "status": "PENDING"
}
```

**Payment Status**

| Status | ความหมาย |
|--------|----------|
| `PENDING` | รอการชำระเงิน |
| `PROCESSING` | กำลังประมวลผล |
| `SUCCESS` | ชำระสำเร็จ |
| `FAILED` | ชำระไม่สำเร็จ |
| `EXPIRED` | หมดอายุ |
| `CANCELLED` | ยกเลิกโดยผู้ใช้ |

**Entity: PaymentTransaction**

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | PK |
| `orderId` | String | อ้างอิง order |
| `channel` | Enum | `PROMPTPAY` |
| `amount` | Decimal(10,2) | จำนวนเงิน |
| `currency` | String | สกุลเงิน (default: THB) |
| `status` | Enum | ดูตาราง Payment Status |
| `providerTransactionId` | String | Transaction ID จาก payment provider |
| `providerResponse` | Text | JSON response ดิบจาก provider |
| `expiredAt` | DateTime | วันหมดอายุของ QR |
| `paidAt` | DateTime | วันที่ชำระสำเร็จ |
| `createdAt` | DateTime | วันที่สร้าง record |
| `updatedAt` | DateTime | วันที่อัปเดตล่าสุด |

---

## Task Breakdown

> **สมมติฐาน:** 1 man-day = 8 ชั่วโมง | Backend (Java/Spring Boot) เท่านั้น

**Phase 0 — Foundation & Setup** (พื้นฐานของทุก feature)

| # | Task | รายละเอียด | Man-days |
|---|------|-----------|----------|
| 0.1 | เลือก Payment Provider | ศึกษาและตัดสินใจ provider (Omise, GB Prime Pay, 2C2P ฯลฯ) | 1 |
| 0.2 | สร้าง sandbox account & เชื่อม credentials | ลงทะเบียน sandbox, ตั้งค่า API key ใน application.properties | 0.5 |
| 0.3 | ออกแบบ Database Schema | ออกแบบ `PaymentTransaction` table, index, FK กับ order | 0.5 |
| 0.4 | สร้าง Entity + Repository | `PaymentTransaction`, `PaymentTransactionRepository` (JPA) | 0.5 |
| 0.5 | สร้าง base PaymentService + Exception | interface, custom exceptions, error response structure | 0.5 |
| **Phase 0 รวม** | | | **3** |

**Phase D — Document Submission & Delivery**

| # | Task | รายละเอียด | Man-days |
|---|------|-----------|----------|
| D0.1 | ออกแบบ DB Schema | `DocumentSubmission`, `DocumentSubmissionItem`, `StatusHistory`, `DeliveryAddress` | 0.5 |
| D0.2 | สร้าง Entity + Repository | JPA entities ทั้งหมด | 1 |
| D1.1 | API: ที่อยู่จัดส่ง (CRUD) | GET / POST / PUT / DELETE / PATCH default | 1.5 |
| D2.1 | API: ยื่นเอกสาร | Controller + Service + DTO + validation | 1.5 |
| D2.2 | Snapshot ที่อยู่จัดส่ง | บันทึก address snapshot ณ เวลาที่ยื่น | 0.5 |
| D3.1 | API: ดู status + timeline | GET status + timeline history | 1 |
| D3.2 | API: Admin อัปเดต status | PUT status + บันทึก history | 1 |
| D3.3 | API: Admin อัปเดต item status | PUT document item status + remark | 0.5 |
| D4.1 | FCM notification | แจ้งเตือนทุกครั้งที่ status เปลี่ยน | 0.5 |
| D4.2 | Unit test + Integration test | ครอบคลุม flow ทั้งหมด | 1 |
| **Phase D รวม** | | | **9** |

**Phase 1 — PromptPay**

| # | Task | รายละเอียด | Man-days |
|---|------|-----------|----------|
| 1.1 | Integrate QR Code generation | เรียก provider API สร้าง EMVCo QR, บันทึก transaction | 1.5 |
| 1.2 | API: `POST /promptpay/qr` | Controller + Service + DTO + validation | 1 |
| 1.3 | Webhook handler | รับ callback, ตรวจสอบ signature, อัปเดต status | 1 |
| 1.4 | API: `GET /promptpay/{id}/status` | ตรวจสอบสถานะ transaction | 0.5 |
| 1.5 | FCM notification | แจ้งเตือนเมื่อชำระสำเร็จ / หมดอายุ | 0.5 |
| 1.6 | Unit test + Integration test | ครอบคลุม happy path + edge case | 1 |
| **Phase 1 รวม** | | | **5.5** |

---

**สรุปราคา**

| Phase | รายละเอียด | Man-days | ราคา (1,200 บาท/MD) |
|-------|-----------|----------|------------------|
| Phase 0 | Foundation & Setup | 3 | 3,600 บาท |
| Phase D | Document Submission & Delivery | 9 | 10,800 บาท |
| Phase 1 | PromptPay | 5.5 | 6,600 บาท |
| **รวม** | | **17.5** | **21,000 บาท** |
| **บวก buffer 20%** | | **~21** | **~25,200 บาท** |

---

**Timeline** (~5 สัปดาห์ รวม buffer)

| สัปดาห์ | ช่วงวันที่ | Focus | Tasks | MD |
|---------|----------|-------|-------|----|
| Week 1 | 14–18 มี.ค. | Foundation | Phase 0.1–0.5: Provider, Sandbox, DB Schema, Entity, Base Service | 3 |
| Week 2 | 21–25 มี.ค. | Document Setup | D0.1–D0.2 DB/Entity, D1.1 Address CRUD | 3 |
| Week 3 | 28 มี.ค. – 1 เม.ย. | Document Submission & Tracking | D2.1 Submit API, D2.2 Snapshot, D3.1–D3.3 Status/Timeline/Admin | 4.5 |
| Week 4 | 4–8 เม.ย. | Document Complete + PromptPay | D4.1 FCM, D4.2 Tests, P1.1–P1.3 QR + Webhook | 4 |
| Week 5 | 11–15 เม.ย. | PromptPay Complete + Buffer | P1.4 Status, P1.5 FCM, P1.6 Tests + Buffer | ~6 |
| **รวม** | | | | **~20.5** |

> **Milestone:**
> - ✅ Week 2: Document DB พร้อม, Address CRUD ใช้งานได้
> - ✅ Week 3: ยื่นเอกสารและติดตาม status ได้ครบ
> - ✅ Week 4: Document ส่งมอบครบ, PromptPay QR ใช้งานได้
> - ✅ Week 5: production

---

> ตัวเลขนี้ **ไม่รวม** งาน Frontend / Mobile (UI form, QR display, document upload)
