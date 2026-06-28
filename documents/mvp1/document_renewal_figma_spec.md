# Document Renewal Request Spec From Figma

> Source: Figma `App_Smart_Seaman`, node `Flow_Renew_Certificated`, plus visible Figma comments and the local flow mock `documents/smart_seaman_full_flow.html`.
> Purpose: define behavior before implementation. This is not implementation code.

## 1. Scope

Build the document renewal request flow for Smart Seaman.

The flow covers:

- Mobile user submits a renewal request for a certificate/document.
- Mobile user uploads required supporting documents.
- Mobile user pays the document renewal fee.
- Admin reviews uploaded documents and can pass, send back for correction, submit to Marine Department, record department result, record pickup, and record delivery.
- User and admin can track request status through the end-to-end lifecycle.

Out of scope for this spec:

- Actual payment provider credential setup.
- Actual Thailand Post API integration details.
- Code implementation details.

## 2. Figma Observations

Visible Figma flow and comments indicate:

- Main mobile flow is `Certificate Renewal` then `Upload Document`.
- There is a 5-step progress display:
  1. ตรวจเอกสาร
  2. รอผลกรมเจ้าท่า
  3. รอรับเอกสารจากกรมเจ้าท่า
  4. กำลังจัดส่ง
  5. จัดส่งสำเร็จ
- Admin/back-office has additional hold/cancel states:
  - รอผู้ยื่นแก้ไข
  - ยกเลิก
- Current status details should show only the current status details, not all future status details.
- Request number format should be `YYMMXXX` or wider running format such as `YYMM` + running digits, e.g. `2605001`.
- Date/time display format should be `DD/MM/YYYY HH:mm`, e.g. `23/05/2026 21:50`.
- After document submission and payment, the user lands in document review status.
- If admin sends documents back, mobile must show a clear correction state explaining that some uploaded documents are incorrect and must be fixed.
- For document rows, Figma comments mention showing a second line as `ดูไฟล์` and a third line as note/remark with a 2-line limit.
- For passed documents, `ดูไฟล์` may be removed in some admin review contexts per comments. Confirm exact rule before UI implementation.
- Delivery status should follow tracking status, and view-file actions should not appear where they are irrelevant.
- If the mobile app is deleted/reinstalled, permission prompts may need to be requested again.

## 3. Actors

| Actor | Role |
|---|---|
| Mobile user / seaman | Select document, upload supporting files, pay fee, track request, correct rejected documents |
| Admin | Review documents, send back, submit to Marine Department, record results, record delivery |
| System | Generate request number, maintain status, receive payment/tracking callbacks, send notifications |
| Payment provider | Process PromptPay/card/banking charges and webhooks |
| Delivery provider | Provides tracking status after shipment |

## 4. Request Status Model

### 4.1 Status Master

Use `m_document_status` as the display master.

| name_th | name_en | css_color | Step | Meaning |
|---|---|---:|---:|---|
| รอตรวจเอกสาร | Pending Document Review | `#ff0000` | 1 | User submitted and paid, waiting for admin document review |
| รอผู้ยื่นแก้ไข | Pending Applicant Correction | `#ff914d` | 1 hold | Admin found document issues; user must upload corrected files |
| รอผลกรมเจ้าท่า | Pending Marine Department Result | `#af87ff` | 2 | Admin has submitted request to Marine Department |
| รอรับเอกสารจากกรม | Pending Department Document Pickup | `#ffde59` | 3 | Marine Department result is ready; admin waits to pick up documents |
| กำลังจัดส่ง | Delivering | `#21e5f8` | 4 | Documents have been shipped to the user |
| จัดส่งสำเร็จ | Delivered | `#00bf63` | 5 | User received documents or delivery provider reports delivered |
| ยกเลิก | Cancelled | `#ff5eb3` | - | Request was cancelled before completion |

### 4.2 Mobile Progress Mapping

Mobile progress bar shows the main 5 statuses only:

| Progress Step | Related Request Status |
|---:|---|
| 1 ตรวจเอกสาร | รอตรวจเอกสาร, รอผู้ยื่นแก้ไข |
| 2 รอผลกรมเจ้าท่า | รอผลกรมเจ้าท่า |
| 3 รอรับเอกสารจากกรมเจ้าท่า | รอรับเอกสารจากกรม |
| 4 กำลังจัดส่ง | กำลังจัดส่ง |
| 5 จัดส่งสำเร็จ | จัดส่งสำเร็จ |

`ยกเลิก` is terminal and should not appear as a normal progress step.

## 5. State Transitions

| Action | From | To | Actor | Required Data |
|---|---|---|---|---|
| CREATE | - | รอตรวจเอกสาร | User/System | document code, uploaded files, amount, payment success |
| SEND_BACK | รอตรวจเอกสาร / รอผลกรมเจ้าท่า | รอผู้ยื่นแก้ไข | Admin | at least one item marked `fix` with note |
| RESUBMIT | รอผู้ยื่นแก้ไข | รอตรวจเอกสาร | User | corrected file(s) |
| CHECK_DOCS | รอตรวจเอกสาร | รอผลกรมเจ้าท่า | Admin | all required items marked `pass` |
| SUBMIT_TO_DEPT | รอผลกรมเจ้าท่า | รอผลกรมเจ้าท่า | Admin | submitted date, submitted by |
| RECORD_DEPT_RESULT | รอผลกรมเจ้าท่า | รอรับเอกสารจากกรม | Admin | available-from date |
| RECEIVE_FROM_DEPT | รอรับเอกสารจากกรม | รอรับเอกสารจากกรม | Admin | received-from-dept date |
| RECORD_DELIVERY | รอรับเอกสารจากกรม | กำลังจัดส่ง | Admin | tracking no, carrier, shipped date |
| DELIVERY_COMPLETE | กำลังจัดส่ง | จัดส่งสำเร็จ | System/Admin | delivered timestamp or tracking delivered event |
| CANCEL | any status before delivery complete | ยกเลิก | Admin/System | cancel reason/note |

Every transition must append a row to `m_document_transaction`.

## 6. Mobile User Flow

### 6.1 Start Renewal

User selects a certificate/document to renew.

Expected screen data:

- Document/certificate name
- Document type/code
- Renewal fee breakdown
- Existing user profile/contact details
- Delivery address or address selection
- Required supporting document list

### 6.2 Upload Documents

User uploads each required supporting document.

Document item examples from the flow mock:

- สำเนาบัตรประชาชน
- รูปถ่ายผู้สมัคร
- หนังสือรับรองบริษัท
- ใบรับรองแพทย์

Rules:

- Each required item must be uploaded before submission.
- File row should show upload state.
- If resubmitting, only corrected file rows need `is_updated = 1`.
- If an item was rejected, show the admin note/remark close to the file row.
- Note/remark display should be limited to 2 lines in compact mobile rows.

### 6.3 Payment

After documents are ready, user pays by supported payment method.

Minimum behavior:

- Create one payment transaction per payment attempt.
- Show QR/redirect/card flow based on payment channel.
- Do not mark request as `รอตรวจเอกสาร` until payment is successful, unless product decides to support unpaid draft requests.
- If payment expires/fails, allow creating a new payment attempt.

### 6.4 Tracking

User sees only current status details plus progress indicator.

Tracking must include:

- Request number
- Current status label and color
- Status timestamp
- Current status detail text
- Supporting document results when relevant
- Delivery tracking when `กำลังจัดส่ง` or `จัดส่งสำเร็จ`

## 7. Admin Flow

### 7.1 List Requests

Admin request list should support:

- Search by Smart Seaman ID
- Search by name
- Search by request no
- Filter tabs by status
- Table columns:
  - Request No.
  - Smart Seaman ID
  - First name
  - Last name
  - Position
  - Document
  - Status
  - Submitted date
  - Amount

### 7.2 Review Documents

For each document item admin can set:

- `pass`
- `fix`
- blank/not reviewed

Rules:

- If result is `fix`, note is required.
- Admin must save document review results before transition buttons are enabled.
- If all required items pass, enable "ยื่นกรมเจ้าท่าแล้ว".
- If any item is `fix`, enable "ส่งกลับให้แก้ไข".
- "ดาวน์โหลดทั้งหมด (.zip)" is available on review screen.

### 7.3 Send Back

When admin sends back:

- Request status changes to `รอผู้ยื่นแก้ไข`.
- User sees correction state in mobile.
- Rejected items show notes.
- After user uploads corrected files, request returns to `รอตรวจเอกสาร`.
- Updated files show badge "อัปเดตใหม่" / `is_updated = 1`.

### 7.4 Marine Department

When all documents pass:

- Admin records submission to Marine Department.
- Status becomes `รอผลกรมเจ้าท่า`.
- Admin can record `available_from_date` when department result is ready.
- Status becomes `รอรับเอกสารจากกรม`.
- Admin can later update `available_from_date` if it changes.
- Admin records actual received-from-dept date.

### 7.5 Delivery

Admin records delivery:

- Tracking No., e.g. `EF123456789TH`
- Carrier, default `Thailand Post`
- Shipped date
- Shipped by

After delivery record:

- Status becomes `กำลังจัดส่ง`.
- Delivery tab shows tracking status from delivery provider or manual update.
- When tracking is delivered, status becomes `จัดส่งสำเร็จ`.

## 8. Data Model Alignment

Use the existing MVP1 table design:

- `m_document_status`: status master with Thai/English names and `css_color`
- `m_document_request`: request header, current status, amount, request no
- `m_document_prices_setting`: fee setup per document
- `m_payment_transaction`: payment attempts/refunds
- `m_document_request_item`: uploaded document items and review result
- `m_document_transaction`: append-only audit timeline
- `m_dept_submission`: Marine Department submission/result/pickup dates
- `m_delivery`: shipping and delivery tracking data

Open design questions before implementation:

- Whether `m_document_request` should store delivery address snapshot directly or via a new table.
- Whether `request_no` should be exactly `YYMMXXX` or support more digits as `YYMM` + running 5 digits.
- Whether user can cancel from mobile, or cancel is admin-only.
- Whether payment success creates request immediately or request can exist as unpaid draft.
- Whether `SUBMIT_TO_DEPT` should be a separate status transition or only a transaction action inside `รอผลกรมเจ้าท่า`.

## 9. API Design Draft

Use `/v1` prefix to match the existing mobile API style.

### Mobile APIs

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/document-renewals/statuses` | Get active status master for display |
| GET | `/v1/document-renewals/prices?documentCode={code}` | Get renewal fee setup |
| POST | `/v1/document-renewals` | Create renewal request after required uploads/payment decision |
| GET | `/v1/document-renewals/my?offSet={n}` | List current user's renewal requests |
| GET | `/v1/document-renewals/{requestId}` | Get request detail |
| GET | `/v1/document-renewals/{requestId}/timeline` | Get status timeline |
| POST | `/v1/document-renewals/{requestId}/items/{itemId}/file` | Upload or replace a supporting document |
| POST | `/v1/document-renewals/{requestId}/resubmit` | Resubmit corrected documents |
| POST | `/v1/document-renewals/{requestId}/payments` | Create payment transaction/charge |
| GET | `/v1/document-renewals/{requestId}/payments/{transactionId}` | Get payment status |

### Admin APIs

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/admin/document-renewals` | Search/filter request list |
| GET | `/v1/admin/document-renewals/{requestId}` | Get admin detail |
| PUT | `/v1/admin/document-renewals/{requestId}/items/review` | Save item review results |
| POST | `/v1/admin/document-renewals/{requestId}/send-back` | Send request back to user |
| POST | `/v1/admin/document-renewals/{requestId}/submit-to-dept` | Record submission to Marine Department |
| POST | `/v1/admin/document-renewals/{requestId}/dept-result` | Record available-from date |
| POST | `/v1/admin/document-renewals/{requestId}/receive-from-dept` | Record actual pickup date |
| POST | `/v1/admin/document-renewals/{requestId}/delivery` | Record delivery tracking info |
| POST | `/v1/admin/document-renewals/{requestId}/cancel` | Cancel request |

### Webhook/System APIs

| Method | Path | Purpose |
|---|---|---|
| POST | `/v1/webhooks/payment/omise` | Receive payment callback and update payment/request |
| POST | `/v1/webhooks/delivery/thailand-post` | Receive delivery callback and update delivery/request |

## 10. Key Response Shapes

### Request Summary

```json
{
  "requestId": "uuid",
  "requestNo": "2605001",
  "documentCode": "BST",
  "documentName": "Basic Safety Training",
  "status": {
    "id": "uuid",
    "nameTh": "รอตรวจเอกสาร",
    "nameEn": "Pending Document Review",
    "cssColor": "#ff0000",
    "step": 1
  },
  "submittedAt": "23/05/2026 21:50",
  "amount": 1500.00,
  "isResubmit": false
}
```

### Request Detail

```json
{
  "requestId": "uuid",
  "requestNo": "2605001",
  "mobileUserUuid": "uuid",
  "documentCode": "BST",
  "documentName": "Basic Safety Training",
  "status": {
    "nameTh": "รอผู้ยื่นแก้ไข",
    "cssColor": "#ff914d",
    "step": 1
  },
  "items": [
    {
      "itemId": "uuid",
      "documentName": "หนังสือรับรองบริษัท",
      "sortOrder": 3,
      "fileUploaded": true,
      "fileUrl": "https://example.invalid/file.pdf",
      "checkResult": "fix",
      "checkNote": "หนังสือหมดอายุ",
      "isUpdated": false
    }
  ],
  "deptSubmission": null,
  "delivery": null,
  "timeline": []
}
```

## 11. Validation Rules

- `request_no` must be unique.
- Request cannot move to `รอผลกรมเจ้าท่า` unless all required items pass.
- Request cannot move to `รอผู้ยื่นแก้ไข` unless at least one item is `fix`.
- `check_note` is required when `check_result = fix`.
- User can resubmit only when current status is `รอผู้ยื่นแก้ไข`.
- Delivery tracking info is required before status `กำลังจัดส่ง`.
- `จัดส่งสำเร็จ` can be set only after `กำลังจัดส่ง`.
- Every status change must create a transaction row.
- Date/time displayed to users/admin must be `DD/MM/YYYY HH:mm`.

## 12. Notification Triggers

Send FCM/in-app notification when:

- Payment succeeds and request enters review.
- Admin sends request back for correction.
- User resubmits corrected documents.
- Admin submits to Marine Department.
- Department result is ready / available-from date recorded.
- Delivery tracking is recorded.
- Delivery is completed.
- Request is cancelled.

## 13. Implementation Readiness Checklist

Before coding, confirm:

- Exact request number format and running digit length.
- Exact mobile copy for each current status detail.
- Whether mobile needs delivery address management in this MVP.
- Supported payment channels for MVP1.
- Required supporting document list per `document_code`.
- File upload storage format: direct multipart, base64, or pre-signed URL.
- Whether admin APIs belong in this mobile API service or a separate back-office service.
- Whether delivery provider webhook is available in MVP1 or manual admin update is enough.
