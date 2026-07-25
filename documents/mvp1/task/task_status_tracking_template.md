# Task Status Tracking Template

ใช้ template นี้สำหรับสร้างเอกสาร task ใต้ `documents/.../task/` เพื่อให้ตอบได้ทันทีว่า:

- ตอนนี้ทำถึงไหนแล้ว
- อะไรเสร็จจริง และมีหลักฐานอะไร
- อะไรยังคงเหลือ, blocked หรือรอยืนยัน

รูปแบบนี้ต่อยอดจาก `task_mobile_document_renewal_api.md` โดยเพิ่มความชัดเรื่อง progress, remaining work, owner และ acceptance criteria

## Document Header Template

```md
# <Feature / Task Group Name>

อ้างอิง:
- Requirement: `<path หรือ link>`
- Design/API Spec: `<path หรือ link>`
- DB Script: `<path หรือ link ถ้ามี>`

อัปเดตล่าสุด: YYYY-MM-DD
ผู้รับผิดชอบ: <name/team>
สถานะรวม: <Not Started | In Progress | Blocked | Done>
Progress: <done>/<total> tasks
```

กติกา:

- `Progress` นับเฉพาะ task detail ที่มี status เป็น `[x]` เทียบกับ task ทั้งหมด
- `สถานะรวม` ต้องสอดคล้องกับ task detail และ remaining work
- ถ้ามี task อย่างน้อย 1 รายการเป็น `[!]` ให้พิจารณา `สถานะรวม: Blocked` เว้นแต่ blocker ไม่กระทบ critical path

## Status Legend

```md
## Status Legend

- [x] Done: ทำครบ + test ผ่าน + มีหลักฐาน
- [~] In Progress: ทำบางส่วน แต่ยังไม่ครบ acceptance criteria
- [ ] Todo: ยังไม่ได้เริ่ม
- [!] Blocked: ติด dependency / decision / environment
- [?] Unknown: มีข้อมูลบางส่วน แต่ยังยืนยันไม่ได้
```

Definition:

- `[x] Done` ใช้ได้เมื่อครบ Definition of Done ของเอกสารนี้เท่านั้น
- `[~] In Progress` ต้องมี `Remaining` เป็น action ที่ทำต่อได้ทันที
- `[!] Blocked` ต้องระบุ blocker ว่าใครหรืออะไรต้องปลดล็อก
- `[?] Unknown` ใช้เมื่อมีหลักฐานบางส่วน แต่ยังยืนยันผลจริงไม่ได้ เช่น มี SQL script แต่ยังไม่รู้ว่า deploy แล้วหรือไม่

## Current Status Template

```md
## Current Status

| # | Status | Task | Owner | Evidence | Remaining |
|---:|:---:|---|---|---|---|
| 1 | [x] | Create renewal request API | Backend | `DocumentRenewalControllerTest`, `POST /v1/document-renewals` | - |
| 2 | [~] | Payment webhook | Backend | มี service แล้ว: `DocumentRenewalPaymentService` | เพิ่ม signature validation และ duplicate-event test |
| 3 | [!] | Production price config | Admin/DBA | มี schema: `05_seed_document_price_settings.sql` | รอข้อมูลราคาจริงจาก Product/Admin |
```

กติกา:

- `Evidence` ต้องเป็นไฟล์, test, endpoint, cURL, SQL script หรือผลตรวจที่อ้างอิงได้
- `Remaining` ต้องเขียนเป็น action ที่ทำต่อได้ทันที ไม่ใช้คำกว้าง ๆ เช่น `เก็บงาน`, `เช็คเพิ่ม`, `ทำต่อ`
- ถ้าเป็น `[x]` แล้ว `Remaining` ต้องเป็น `-`
- ถ้าเป็น `[!]` แล้ว `Remaining` ต้องเริ่มจากสิ่งที่ทำได้หลัง blocker ถูกปลดล็อก

## Task Detail Template

```md
### TASK-ID: <Task Name>

Status: [ ] Todo
Owner: <name/team>
Estimate: <n> MD
Priority: <High | Medium | Low>

Goal:
- <ผลลัพธ์ที่ task นี้ต้องทำให้เกิด>

Scope:
- <สิ่งที่ต้องทำ>
- <สิ่งที่รวมอยู่ใน task นี้>

Out of scope:
- <สิ่งที่ไม่ทำใน task นี้ เพื่อกัน scope creep>

Implementation checklist:
- [ ] Controller / endpoint
- [ ] Service logic
- [ ] Repository / SQL
- [ ] Request/response DTO
- [ ] Validation
- [ ] Error handling
- [ ] Transaction / concurrency handling ถ้าเกี่ยวข้อง
- [ ] Swagger / API doc
- [ ] Unit test
- [ ] Integration/focused test
- [ ] cURL example

Acceptance criteria:
- <เงื่อนไขที่ใช้ตัดสินว่าเสร็จจริง>
- <expected behavior สำคัญ>
- <negative case สำคัญ>

Evidence when done:
- Test command: `./mvnw test -Dtest=<TestClass>`
- API example: `<cURL หรือ endpoint>`
- Files changed: `<ไฟล์หลัก 2-5 ไฟล์>`
```

คำแนะนำ:

- ลบ checklist item ที่ไม่เกี่ยวข้องได้ แต่ต้องไม่ลบ item สำคัญของ public API เช่น validation, error handling, API doc และ test
- ถ้า task เป็น internal foundation และไม่มี public endpoint ให้ระบุใน `Evidence when done` ว่าไม่มี cURL โดยตรง และอ้าง API หรือ test ที่พิสูจน์ behavior
- Acceptance criteria ต้องมีทั้ง positive case และ negative case สำหรับ behavior สำคัญ

## Remaining Work Template

ให้แยกงานคงเหลือออกจากรายละเอียด task เป็น section รวมท้ายเอกสารเสมอ

```md
## Remaining Work

| Priority | Task | Why it remains | Next action | Blocker |
|:---:|---|---|---|---|
| High | Payment webhook signature | ยังไม่ได้ validate provider callback | เพิ่ม signature verifier และ focused tests | รอ secret จาก provider |
| Medium | Swagger examples | endpoint ทำแล้วแต่ doc ยังไม่ครบ | เพิ่ม annotation และ cURL example | - |
```

กติกา:

- `Next action` ต้องเป็นคำสั่งงานต่อได้ใน 1 step
- `Blocker` ต้องระบุว่าใครหรืออะไรต้องปลดล็อก
- งานที่ไม่มี blocker ต้องมี `Next action` เสมอ
- งานที่เสร็จแล้วไม่ต้องอยู่ใน `Remaining Work`
- ถ้าไม่มีงานคงเหลือ ให้เขียนตารางว่างพร้อมข้อความ `ไม่มีงานคงเหลือที่ทราบ ณ วันที่ YYYY-MM-DD`

## Definition Of Done

task จะเปลี่ยนเป็น `[x]` ได้เมื่อครบทั้งหมดนี้:

- implementation ครบตาม scope
- acceptance criteria ผ่านครบ
- มี test อย่างน้อย focused test สำหรับ behavior สำคัญ
- ไม่มี known blocker
- update API doc หรือ cURL ถ้าเป็น public endpoint
- ระบุ evidence ชัดเจน เช่น test class, endpoint, SQL script หรือไฟล์ที่เกี่ยวข้อง

## Complete Example

```md
# Mobile Document Renewal API Tasks

อ้างอิง:
- Requirement: `documents/mvp1/document_renewal_figma_spec.md`
- Design/API Spec: `documents/mvp1/document_service_flow.md`
- DB Script: `documents/mvp1/script/90_03_migrate_document_renewal_price_effective_period.sql`

อัปเดตล่าสุด: 2026-07-25
ผู้รับผิดชอบ: Backend
สถานะรวม: In Progress
Progress: 1/3 tasks

## Status Legend

- [x] Done: ทำครบ + test ผ่าน + มีหลักฐาน
- [~] In Progress: ทำบางส่วน แต่ยังไม่ครบ acceptance criteria
- [ ] Todo: ยังไม่ได้เริ่ม
- [!] Blocked: ติด dependency / decision / environment
- [?] Unknown: มีข้อมูลบางส่วน แต่ยังยืนยันไม่ได้

## Current Status

| # | Status | Task | Owner | Evidence | Remaining |
|---:|:---:|---|---|---|---|
| 1 | [x] | Create renewal request API | Backend | `DocumentRenewalCreateControllerTest`, `POST /v1/document-renewals` | - |
| 2 | [~] | Payment webhook | Backend | `DocumentRenewalPaymentService` | เพิ่ม signature validation และ duplicate-event focused test |
| 3 | [!] | Production price config | Admin/DBA | schema มี `m_document_prices_setting` | รอ Product/Admin ยืนยันราคาจริง |

## Task Breakdown

### MR-MOB-01: Create Renewal Request API

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- mobile user สร้าง renewal request ได้โดย server เป็นผู้กำหนดราคา, owner และ initial status

Scope:
- เพิ่ม endpoint `POST /v1/document-renewals`
- สร้าง request header, request items และ timeline ใน transaction เดียวกัน

Out of scope:
- payment provider callback
- admin approval workflow

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling ถ้าเกี่ยวข้อง
- [x] Swagger / API doc
- [x] Unit test
- [x] Integration/focused test
- [x] cURL example

Acceptance criteria:
- user สร้าง request ได้เฉพาะ document ที่เปิด renewal
- server ไม่เชื่อถือราคา, owner หรือ status จาก request body
- rollback ทั้ง request/items/timeline เมื่อสร้างรายการใดรายการหนึ่งไม่สำเร็จ

Evidence when done:
- Test command: `./mvnw test -Dtest=DocumentRenewalCreateControllerTest`
- API example: `POST /v1/document-renewals`
- Files changed: `DocumentRenewalController`, `DocumentRenewalService`, `DocumentRenewalRepository`

## Remaining Work

| Priority | Task | Why it remains | Next action | Blocker |
|:---:|---|---|---|---|
| High | Payment webhook | ยังขาด signature validation | เพิ่ม verifier และ duplicate-event test | รอ webhook secret จาก provider |
| High | Production price config | ยังไม่มีราคาจริงสำหรับ production | เพิ่ม production seed script หลัง Product/Admin ยืนยันราคา | Product/Admin ต้องยืนยันราคา |
```

## Assumptions

- ใช้ Markdown เป็นหลัก และเก็บเอกสาร task ใต้ `documents/.../task/`
- ไม่ต้องสร้าง backend หรือ database สำหรับ task tracking ในตอนนี้
- ใช้ status แบบ checklist เพราะเข้ากับเอกสารปัจจุบันของ repo และอ่านง่ายสำหรับ backend, mobile, PM และ QA
