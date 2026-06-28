# Non-Prod Smart Seaman DB Schema

Generated: 2026-06-23

Database: `non-prod-smart-seaman`

Total tables: `37`

## Table List

| # | Table |
|---:|---|
| 1 | `m_admin_users` |
| 2 | `m_banners` |
| 3 | `m_certificates` |
| 4 | `m_companys` |
| 5 | `m_configurations` |
| 6 | `m_course_dates` |
| 7 | `m_course_name` |
| 8 | `m_courses` |
| 9 | `m_delivery` |
| 10 | `m_dept_submission` |
| 11 | `m_document_prices_setting` |
| 12 | `m_document_request` |
| 13 | `m_document_request_item` |
| 14 | `m_document_status` |
| 15 | `m_document_transaction` |
| 16 | `m_documents` |
| 17 | `m_fcm_notifications` |
| 18 | `m_forms` |
| 19 | `m_groups` |
| 20 | `m_groups_map_autholist` |
| 21 | `m_menus` |
| 22 | `m_menus_map_permission` |
| 23 | `m_message_code` |
| 24 | `m_mobile_users` |
| 25 | `m_news` |
| 26 | `m_payment_transaction` |
| 27 | `m_permission` |
| 28 | `m_positions` |
| 29 | `m_send_notifications` |
| 30 | `m_send_notifications_backup` |
| 31 | `m_voucher_details` |
| 32 | `m_vouchers` |
| 33 | `t_forgot_password` |
| 34 | `t_session` |
| 35 | `t_transaction_logs` |
| 36 | `t_transaction_logs_offline` |
| 37 | `t_txn_to_other_system` |

## `m_admin_users`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 30 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 32 |
| `CREATE_TIME` | 2026-04-25 08:55:51 |
| `UPDATE_TIME` | 2026-06-17 14:46:15 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `ADMIN_USER_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `ADMIN_UUID` | `varchar(36)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | UUID |
| 3 | `GROUP_ID` | `int` | NO | - | - | - | - | - | Group ID |
| 4 | `USERNAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | User Name = Email |
| 5 | `PASSWORD` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Encrypted Password |
| 6 | `FIRST_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | First Name |
| 7 | `LAST_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Last |
| 8 | `COMPANY_CODE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Company |
| 9 | `POSITIONS` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Position |
| 10 | `EMAIL` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Email |
| 11 | `MOBILE_NUMBER` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Mobile Number |
| 12 | `DISPLAY_TYPE` | `varchar(50)` | NO | NAME | - | - | utf8mb4 | utf8mb4_general_ci | NAME, PICTURE |
| 13 | `DISPLAY_NAME` | `varchar(2)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | ตัวอักษรตัวแรกของชื่อ |
| 14 | `PROFILE_PICTURE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | If have picture,will store filename from S3<br>If don't have picture = null / blank |
| 15 | `USER_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive |
| 16 | `LAST_LOGON` | `timestamp` | YES | - | - | - | - | - | - |
| 17 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 18 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 19 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 20 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `ADMIN_USER_ID` | A | 30 | - | - | - | BTREE | - | - | YES | - |
| `ADMIN_UUID_UNIQUE` | 0 | 1 | `ADMIN_UUID` | A | 30 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_admin_users" (
  "ADMIN_USER_ID" int NOT NULL AUTO_INCREMENT,
  "ADMIN_UUID" varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID',
  "GROUP_ID" int NOT NULL COMMENT 'Group ID',
  "USERNAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'User Name = Email',
  "PASSWORD" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Encrypted Password',
  "FIRST_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'First Name',
  "LAST_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Last',
  "COMPANY_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Company',
  "POSITIONS" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Position',
  "EMAIL" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Email',
  "MOBILE_NUMBER" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Mobile Number',
  "DISPLAY_TYPE" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NAME' COMMENT 'NAME, PICTURE',
  "DISPLAY_NAME" varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ตัวอักษรตัวแรกของชื่อ',
  "PROFILE_PICTURE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'If have picture,will store filename from S3\nIf don''t have picture = null / blank',
  "USER_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive',
  "LAST_LOGON" timestamp NULL DEFAULT NULL,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY ("ADMIN_USER_ID"),
  UNIQUE KEY "ADMIN_UUID_UNIQUE" ("ADMIN_UUID")
);
```


## `m_banners`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 08:55:58 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `BANNER_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `BANNER_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `BANNER_FILE_NAME` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 4 | `BANNER_SEQ` | `varchar(2)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 5 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 6 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 8 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `BANNER_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_banners" (
  "BANNER_ID" int NOT NULL AUTO_INCREMENT,
  "BANNER_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "BANNER_FILE_NAME" varchar(255) DEFAULT NULL,
  "BANNER_SEQ" varchar(2) DEFAULT NULL,
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("BANNER_ID")
);
```


## `m_certificates`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 94 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 95 |
| `CREATE_TIME` | 2026-04-25 08:56:07 |
| `UPDATE_TIME` | 2026-06-22 14:58:49 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `CERT_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `CERT_MOBILE_UUID` | `varchar(36)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `CERT_DOCUMENT_CODE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | DOCUMENT_CODE in m_documents table |
| 4 | `CERT_START_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 5 | `CERT_END_DATE` | `timestamp` | YES | - | - | - | - | - | null = no expire |
| 6 | `CERT_FILE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `ORIGINAL_FILE_NAME` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 8 | `CERT_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, D= Delete |
| 9 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 10 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 11 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 12 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `CERT_ID` | A | 94 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_certificates" (
  "CERT_ID" int NOT NULL AUTO_INCREMENT,
  "CERT_MOBILE_UUID" varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "CERT_DOCUMENT_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'DOCUMENT_CODE in m_documents table',
  "CERT_START_DATE" timestamp NULL DEFAULT NULL,
  "CERT_END_DATE" timestamp NULL DEFAULT NULL COMMENT 'null = no expire',
  "CERT_FILE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "ORIGINAL_FILE_NAME" varchar(255) DEFAULT NULL,
  "CERT_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("CERT_ID")
);
```


## `m_companys`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 39 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 40 |
| `CREATE_TIME` | 2026-04-25 09:12:57 |
| `UPDATE_TIME` | 2026-04-25 09:22:01 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `COMPANY_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `COMPANY_CODE` | `varchar(10)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `COMPANY_NAME_EN` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `COMPANY_NAME_TH` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `COMPANY_FULL_NAME_EN` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `COMPANY_FULL_NAME_TH` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `COMPANY_DESCRIPTION` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 8 | `COMPANY_TYPE` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Training School, Shipping Company, Crew management Company, Department, App Owner |
| 9 | `COMPANY_MOBILE_FLAG` | `varchar(1)` | NO | Y | - | - | utf8mb4 | utf8mb4_general_ci | Flag for showing on mobile app\nY = show\nN = not show |
| 10 | `COMPANY_LOGO` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 11 | `COMPANY_COLOUR` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 12 | `COMPANY_FACEBOOK` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 13 | `COMPANY_LINE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 14 | `COMPANY_PHONE1` | `varchar(20)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 15 | `COMPANY_PHONE2` | `varchar(20)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 16 | `COMPANY_PHONE3` | `varchar(20)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 17 | `COMPANY_SEQ` | `int` | YES | - | - | - | - | - | - |
| 18 | `COMPANY_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive, D= Delete |
| 19 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 20 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 21 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 22 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `COMPANY_ID` | A | 39 | - | - | - | BTREE | - | - | YES | - |
| `COMPANY_CODE_UNIQUE` | 0 | 1 | `COMPANY_CODE` | A | 39 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_companys" (
  "COMPANY_ID" int NOT NULL AUTO_INCREMENT,
  "COMPANY_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COMPANY_NAME_EN" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COMPANY_NAME_TH" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COMPANY_FULL_NAME_EN" varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_FULL_NAME_TH" varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_DESCRIPTION" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_TYPE" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Training School, Shipping Company, Crew management Company, Department, App Owner',
  "COMPANY_MOBILE_FLAG" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Y' COMMENT 'Flag for showing on mobile app\\nY = show\\nN = not show',
  "COMPANY_LOGO" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_COLOUR" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_FACEBOOK" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_LINE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_PHONE1" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_PHONE2" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_PHONE3" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COMPANY_SEQ" int DEFAULT NULL,
  "COMPANY_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("COMPANY_ID"),
  UNIQUE KEY "COMPANY_CODE_UNIQUE" ("COMPANY_CODE")
);
```


## `m_configurations`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:13:04 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `CONFIG_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `CONFIG_KEY` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `CONFIG_VALUE` | `varchar(500)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `DESCRIPTION` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `CONFIG_GROUP` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | ALL, MOBILE, WEB, API |
| 6 | `CONFIG_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive |
| 7 | `CREATE_BY` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 8 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |
| 9 | `UPDATE_BY` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `UPDATE_DATE` | `timestamp` | NO | - | - | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `CONFIG_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_configurations" (
  "CONFIG_ID" int NOT NULL AUTO_INCREMENT,
  "CONFIG_KEY" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "CONFIG_VALUE" varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "DESCRIPTION" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "CONFIG_GROUP" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ALL, MOBILE, WEB, API',
  "CONFIG_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive',
  "CREATE_BY" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  "UPDATE_BY" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "UPDATE_DATE" timestamp NOT NULL,
  PRIMARY KEY ("CONFIG_ID")
);
```


## `m_course_dates`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-04-25 09:13:23 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `COURSE_DATE_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `COURSE_ID` | `int` | NO | - | - | - | - | - | - |
| 3 | `COURSE_DATE_TYPE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | ONLINE, ONSITE |
| 4 | `COURSE_DATE_VALUE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `COURSE_DATE_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_course_dates" (
  "COURSE_DATE_ID" int NOT NULL AUTO_INCREMENT,
  "COURSE_ID" int NOT NULL,
  "COURSE_DATE_TYPE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ONLINE, ONSITE',
  "COURSE_DATE_VALUE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("COURSE_DATE_ID")
);
```


## `m_course_name`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 23 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 24 |
| `CREATE_TIME` | 2026-04-25 09:13:29 |
| `UPDATE_TIME` | 2026-04-25 09:23:10 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `COURSE_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `COURSE_CODE` | `varchar(10)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `COURSE_NAME_EN` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `COURSE_NAME_TH` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `COURSE_DESCRIPTION` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `COURSE_TYPE` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 7 | `COURSE_SEQ` | `int` | YES | - | - | - | - | - | Sequence ID |
| 8 | `COURSE_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive, D= Delete |
| 9 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 10 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 11 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 12 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `COURSE_ID` | A | 23 | - | - | - | BTREE | - | - | YES | - |
| `UNQ_COURSE_CODE` | 0 | 1 | `COURSE_CODE` | A | 23 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_course_name" (
  "COURSE_ID" int NOT NULL AUTO_INCREMENT,
  "COURSE_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_NAME_EN" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_NAME_TH" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_DESCRIPTION" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COURSE_TYPE" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  "COURSE_SEQ" int DEFAULT NULL COMMENT 'Sequence ID',
  "COURSE_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("COURSE_ID"),
  UNIQUE KEY "UNQ_COURSE_CODE" ("COURSE_CODE")
);
```


## `m_courses`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 310 |
| `DATA_LENGTH` | 65536 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 329 |
| `CREATE_TIME` | 2026-04-25 09:13:35 |
| `UPDATE_TIME` | 2026-04-25 09:28:38 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `COURSE_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `COURSE_CODE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `COURSE_COMPANY_CODE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `COURSE_TYPE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | BOTH, ONLINE, ONSITE  |
| 5 | `COURSE_ONLINE_DATE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `COURSE_ONSITE_DATE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `COURSE_TOTAL_DAYS` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 8 | `COURSE_COLOUR` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 9 | `COURSE_PRICE` | `decimal(10,0)` | NO | - | - | - | - | - | - |
| 10 | `COURSE_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive, D= Delete |
| 11 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 12 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 13 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 14 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `COURSE_ID` | A | 310 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_courses" (
  "COURSE_ID" int NOT NULL AUTO_INCREMENT,
  "COURSE_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_COMPANY_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_TYPE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'BOTH, ONLINE, ONSITE ',
  "COURSE_ONLINE_DATE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COURSE_ONSITE_DATE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "COURSE_TOTAL_DAYS" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_COLOUR" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "COURSE_PRICE" decimal(10,0) NOT NULL,
  "COURSE_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("COURSE_ID")
);
```


## `m_delivery`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 49152 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:18 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `request_id` | `char(36)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `tracking_no` | `varchar(50)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `carrier` | `varchar(100)` | NO | Thailand Post | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `shipped_date` | `date` | NO | - | - | - | - | - | - |
| 6 | `delivery_status` | `varchar(20)` | NO | in_transit | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 7 | `shipped_recorded_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `shipped_by` | `char(36)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 9 | `delivered_at` | `datetime` | YES | - | - | - | - | - | - |
| 10 | `created_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 11 | `updated_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_delivery_request` | 0 | 1 | `request_id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_delivery_tracking_no` | 1 | 1 | `tracking_no` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_delivery_status_updated` | 1 | 1 | `delivery_status` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_delivery_status_updated` | 1 | 2 | `updated_at` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| `fk_delivery_request` | `request_id` | `m_document_request`.`id` | NO ACTION | NO ACTION |

### Create SQL

```sql
CREATE TABLE "m_delivery" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "request_id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "tracking_no" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "carrier" varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Thailand Post',
  "shipped_date" date NOT NULL,
  "delivery_status" varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'in_transit',
  "shipped_recorded_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "shipped_by" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "delivered_at" datetime DEFAULT NULL,
  "created_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  UNIQUE KEY "uq_delivery_request" ("request_id"),
  KEY "idx_delivery_tracking_no" ("tracking_no"),
  KEY "idx_delivery_status_updated" ("delivery_status","updated_at"),
  CONSTRAINT "fk_delivery_request" FOREIGN KEY ("request_id") REFERENCES "m_document_request" ("id"),
  CONSTRAINT "chk_delivery_status" CHECK ((`delivery_status` in (_utf8mb4'pending',_utf8mb4'in_transit',_utf8mb4'delivered',_utf8mb4'failed',_utf8mb4'returned')))
);
```


## `m_dept_submission`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 32768 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:17 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `request_id` | `char(36)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `submitted_to_dept_date` | `date` | NO | - | - | MUL | - | - | - |
| 4 | `submitted_by` | `char(36)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `available_from_date` | `date` | YES | - | - | - | - | - | - |
| 6 | `received_from_dept_date` | `date` | YES | - | - | - | - | - | - |
| 7 | `recorded_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `updated_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_deptsubmit_request` | 0 | 1 | `request_id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_deptsubmit_submitted_date` | 1 | 1 | `submitted_to_dept_date` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| `fk_deptsubmit_request` | `request_id` | `m_document_request`.`id` | NO ACTION | NO ACTION |

### Create SQL

```sql
CREATE TABLE "m_dept_submission" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "request_id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "submitted_to_dept_date" date NOT NULL,
  "submitted_by" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "available_from_date" date DEFAULT NULL,
  "received_from_dept_date" date DEFAULT NULL,
  "recorded_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  UNIQUE KEY "uq_deptsubmit_request" ("request_id"),
  KEY "idx_deptsubmit_submitted_date" ("submitted_to_dept_date"),
  CONSTRAINT "fk_deptsubmit_request" FOREIGN KEY ("request_id") REFERENCES "m_document_request" ("id")
);
```


## `m_document_prices_setting`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:14 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `document_code` | `varchar(50)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `document_name_th` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `document_name_en` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `government_fee` | `decimal(10,2)` | NO | 0.00 | - | - | - | - | - |
| 6 | `document_processing_fee` | `decimal(10,2)` | NO | 0.00 | - | - | - | - | - |
| 7 | `shipping_fee` | `decimal(10,2)` | NO | 0.00 | - | - | - | - | - |
| 8 | `shipping_discount` | `decimal(10,2)` | NO | 0.00 | - | - | - | - | - |
| 9 | `service_fee_discount` | `decimal(10,2)` | NO | 0.00 | - | - | - | - | - |
| 10 | `is_active` | `varchar(3)` | NO | YES | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 11 | `created_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 12 | `updated_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_docprice_document_code` | 0 | 1 | `document_code` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_document_prices_setting" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "document_code" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "document_name_th" varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  "document_name_en" varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  "government_fee" decimal(10,2) NOT NULL DEFAULT '0.00',
  "document_processing_fee" decimal(10,2) NOT NULL DEFAULT '0.00',
  "shipping_fee" decimal(10,2) NOT NULL DEFAULT '0.00',
  "shipping_discount" decimal(10,2) NOT NULL DEFAULT '0.00',
  "service_fee_discount" decimal(10,2) NOT NULL DEFAULT '0.00',
  "is_active" varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'YES',
  "created_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  UNIQUE KEY "uq_docprice_document_code" ("document_code"),
  CONSTRAINT "chk_docprice_active" CHECK ((`is_active` in (_utf8mb4'YES',_utf8mb4'NO'))),
  CONSTRAINT "chk_docprice_fee" CHECK (((`government_fee` >= 0) and (`document_processing_fee` >= 0) and (`shipping_fee` >= 0) and (`shipping_discount` >= 0) and (`service_fee_discount` >= 0)))
);
```


## `m_document_request`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 65536 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:14 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `request_no` | `varchar(20)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `mobile_user_uuid` | `varchar(50)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `document_code` | `varchar(50)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `document_status_id` | `char(36)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 6 | `is_resubmit` | `tinyint(1)` | NO | 0 | - | - | - | - | - |
| 7 | `amount` | `decimal(10,2)` | NO | 0.00 | - | - | - | - | - |
| 8 | `submitted_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 9 | `submitted_by` | `char(36)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 10 | `created_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 11 | `updated_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_docreq_request_no` | 0 | 1 | `request_no` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_docreq_mobile_user` | 1 | 1 | `mobile_user_uuid` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_docreq_document_code` | 1 | 1 | `document_code` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_docreq_status_submitted` | 1 | 1 | `document_status_id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_docreq_status_submitted` | 1 | 2 | `submitted_at` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| `fk_docreq_status` | `document_status_id` | `m_document_status`.`id` | NO ACTION | NO ACTION |

### Create SQL

```sql
CREATE TABLE "m_document_request" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "request_no" varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  "mobile_user_uuid" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "document_code" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "document_status_id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "is_resubmit" tinyint(1) NOT NULL DEFAULT '0',
  "amount" decimal(10,2) NOT NULL DEFAULT '0.00',
  "submitted_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "submitted_by" char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "created_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  UNIQUE KEY "uq_docreq_request_no" ("request_no"),
  KEY "idx_docreq_mobile_user" ("mobile_user_uuid"),
  KEY "idx_docreq_document_code" ("document_code"),
  KEY "idx_docreq_status_submitted" ("document_status_id","submitted_at"),
  CONSTRAINT "fk_docreq_status" FOREIGN KEY ("document_status_id") REFERENCES "m_document_status" ("id"),
  CONSTRAINT "chk_docreq_amount" CHECK ((`amount` >= 0)),
  CONSTRAINT "chk_docreq_resubmit" CHECK ((`is_resubmit` in (0,1)))
);
```


## `m_document_request_item`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 32768 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:16 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `request_id` | `char(36)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `document_name` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `sort_order` | `tinyint` | NO | 1 | - | - | - | - | - |
| 5 | `file_uploaded` | `tinyint(1)` | NO | 0 | - | - | - | - | - |
| 6 | `file_path` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 7 | `file_uploaded_at` | `datetime` | YES | - | - | - | - | - | - |
| 8 | `check_result` | `varchar(10)` | YES | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 9 | `check_note` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 10 | `is_updated` | `tinyint(1)` | NO | 0 | - | - | - | - | - |
| 11 | `checked_at` | `datetime` | YES | - | - | - | - | - | - |
| 12 | `checked_by` | `char(36)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 13 | `created_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 14 | `updated_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_reqitem_request_sort` | 1 | 1 | `request_id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_reqitem_request_sort` | 1 | 2 | `sort_order` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_reqitem_check_result` | 1 | 1 | `check_result` | A | 0 | - | - | YES | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| `fk_reqitem_request` | `request_id` | `m_document_request`.`id` | NO ACTION | CASCADE |

### Create SQL

```sql
CREATE TABLE "m_document_request_item" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "request_id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "document_name" varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  "sort_order" tinyint NOT NULL DEFAULT '1',
  "file_uploaded" tinyint(1) NOT NULL DEFAULT '0',
  "file_path" varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "file_uploaded_at" datetime DEFAULT NULL,
  "check_result" varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "check_note" text COLLATE utf8mb4_unicode_ci,
  "is_updated" tinyint(1) NOT NULL DEFAULT '0',
  "checked_at" datetime DEFAULT NULL,
  "checked_by" char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "created_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  KEY "idx_reqitem_request_sort" ("request_id","sort_order"),
  KEY "idx_reqitem_check_result" ("check_result"),
  CONSTRAINT "fk_reqitem_request" FOREIGN KEY ("request_id") REFERENCES "m_document_request" ("id") ON DELETE CASCADE,
  CONSTRAINT "chk_reqitem_check_result" CHECK (((`check_result` is null) or (`check_result` in (_utf8mb4'pass',_utf8mb4'fix')))),
  CONSTRAINT "chk_reqitem_file_uploaded" CHECK ((`file_uploaded` in (0,1))),
  CONSTRAINT "chk_reqitem_fix_note" CHECK (((`check_result` <> _utf8mb4'fix') or (`check_note` is not null))),
  CONSTRAINT "chk_reqitem_is_updated" CHECK ((`is_updated` in (0,1)))
);
```


## `m_document_status`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 32768 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:13 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `name_th` | `varchar(255)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `name_en` | `varchar(255)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `css_color` | `varchar(100)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `is_active` | `varchar(3)` | NO | YES | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 6 | `created_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 7 | - | - | - | BTREE | - | - | YES | - |
| `uq_document_status_name_th` | 0 | 1 | `name_th` | A | 7 | - | - | - | BTREE | - | - | YES | - |
| `uq_document_status_name_en` | 0 | 1 | `name_en` | A | 7 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_document_status" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "name_th" varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  "name_en" varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  "css_color" varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  "is_active" varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'YES',
  "created_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  UNIQUE KEY "uq_document_status_name_th" ("name_th"),
  UNIQUE KEY "uq_document_status_name_en" ("name_en"),
  CONSTRAINT "chk_document_status_active" CHECK ((`is_active` in (_utf8mb4'YES',_utf8mb4'NO')))
);
```


## `m_document_transaction`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 32768 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:16 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `request_id` | `char(36)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `action` | `varchar(50)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `from_status` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `to_status` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 6 | `note` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 7 | `actioned_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `actioned_by` | `char(36)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_doctx_request_actioned` | 1 | 1 | `request_id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_doctx_request_actioned` | 1 | 2 | `actioned_at` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_doctx_action` | 1 | 1 | `action` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| `fk_doctx_request` | `request_id` | `m_document_request`.`id` | NO ACTION | NO ACTION |

### Create SQL

```sql
CREATE TABLE "m_document_transaction" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "request_id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "action" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "from_status" varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "to_status" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "note" text COLLATE utf8mb4_unicode_ci,
  "actioned_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "actioned_by" char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY ("id"),
  KEY "idx_doctx_request_actioned" ("request_id","actioned_at"),
  KEY "idx_doctx_action" ("action"),
  CONSTRAINT "fk_doctx_request" FOREIGN KEY ("request_id") REFERENCES "m_document_request" ("id"),
  CONSTRAINT "chk_doctx_action" CHECK ((`action` in (_utf8mb4'CREATE',_utf8mb4'SEND_BACK',_utf8mb4'RESUBMIT',_utf8mb4'CHECK_DOCS',_utf8mb4'SUBMIT_TO_DEPT',_utf8mb4'RECORD_DEPT_RESULT',_utf8mb4'RECEIVE_FROM_DEPT',_utf8mb4'RECORD_DELIVERY',_utf8mb4'DELIVERY_COMPLETE',_utf8mb4'CANCEL')))
);
```


## `m_documents`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 47 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 48 |
| `CREATE_TIME` | 2026-04-25 09:13:44 |
| `UPDATE_TIME` | 2026-04-25 11:03:00 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `DOCUMENT_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `DOCUMENT_CODE` | `varchar(10)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `DOCUMENT_NAME_EN` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `DOCUMENT_NAME_TH` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `DOCUMENT_COURSE_CODE` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `DOCUMENT_DESCRIPTION` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `DOCUMENT_TYPE` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 8 | `DOCUMENT_DEFAULT_FLAG` | `varchar(1)` | NO | N | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 9 | `DOCUMENT_SEQ` | `int` | YES | - | - | - | - | - | Sequence ID |
| 10 | `DOCUMENT_MOBILE_FLAG` | `varchar(1)` | NO | Y | - | - | utf8mb4 | utf8mb4_general_ci | Flag for showing on mobile app\\nY = show\\nN = not show |
| 11 | `DOCUMENT_COMPANY_CODE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | List of companys that require documents or null for all companys |
| 12 | `DOCUMENT_POSITION_CODE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | List of positions that require documents or null for all positions |
| 13 | `DOCUMENT_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive, D= Delete |
| 14 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 15 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 16 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 17 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `DOCUMENT_ID` | A | 47 | - | - | - | BTREE | - | - | YES | - |
| `UNQ_DOCUMENT_CODE` | 0 | 1 | `DOCUMENT_CODE` | A | 47 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_documents" (
  "DOCUMENT_ID" int NOT NULL AUTO_INCREMENT,
  "DOCUMENT_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "DOCUMENT_NAME_EN" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "DOCUMENT_NAME_TH" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "DOCUMENT_COURSE_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "DOCUMENT_DESCRIPTION" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "DOCUMENT_TYPE" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "DOCUMENT_DEFAULT_FLAG" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'N',
  "DOCUMENT_SEQ" int DEFAULT NULL COMMENT 'Sequence ID',
  "DOCUMENT_MOBILE_FLAG" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Y' COMMENT 'Flag for showing on mobile app\\\\nY = show\\\\nN = not show',
  "DOCUMENT_COMPANY_CODE" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'List of companys that require documents or null for all companys',
  "DOCUMENT_POSITION_CODE" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'List of positions that require documents or null for all positions',
  "DOCUMENT_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("DOCUMENT_ID"),
  UNIQUE KEY "UNQ_DOCUMENT_CODE" ("DOCUMENT_CODE")
);
```


## `m_fcm_notifications`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 2 |
| `CREATE_TIME` | 2026-04-25 09:15:04 |
| `UPDATE_TIME` | 2026-06-22 15:17:36 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `USER_MOBILE_UUID` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `TOKEN_FCM` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `CREATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 5 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_fcm_notifications" (
  "ID" int NOT NULL AUTO_INCREMENT,
  "USER_MOBILE_UUID" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "TOKEN_FCM" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "CREATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  PRIMARY KEY ("ID")
);
```


## `m_forms`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:15:09 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `FORM_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `FORM_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `FORM_FILE_NAME` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 4 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 5 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 7 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `FORM_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_forms" (
  "FORM_ID" int NOT NULL AUTO_INCREMENT,
  "FORM_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "FORM_FILE_NAME" varchar(255) DEFAULT NULL,
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("FORM_ID")
);
```


## `m_groups`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 5 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 6 |
| `CREATE_TIME` | 2026-04-25 09:15:14 |
| `UPDATE_TIME` | 2026-04-25 11:03:19 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `GROUP_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `GROUP_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Group Name |
| 3 | `GROUP_DESC` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Group description |
| 4 | `GROUP_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A = Active, I = Inactive, D = Delete |
| 5 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 6 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 7 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 8 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `GROUP_ID` | A | 5 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_groups" (
  "GROUP_ID" int NOT NULL AUTO_INCREMENT,
  "GROUP_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Group Name',
  "GROUP_DESC" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Group description',
  "GROUP_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A = Active, I = Inactive, D = Delete',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY ("GROUP_ID")
);
```


## `m_groups_map_autholist`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 22 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 26 |
| `CREATE_TIME` | 2026-04-25 09:15:19 |
| `UPDATE_TIME` | 2026-06-17 14:32:07 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `GROUPS_MAP_AUTHOLIST_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `GROUP_ID` | `int` | NO | - | - | MUL | - | - | Group ID from m_groups |
| 3 | `MENU_ID` | `int` | NO | - | - | - | - | - | Menu ID from m_menus |
| 4 | `AUTHOLIST_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Authorlist Status |
| 5 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 6 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 7 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |
| 8 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `GROUPS_MAP_AUTHOLIST_ID` | A | 22 | - | - | - | BTREE | - | - | YES | - |
| `M_GROUPS_MAP_AUTHOLIST_IDX` | 0 | 1 | `GROUP_ID` | A | 5 | - | - | - | BTREE | - | - | YES | - |
| `M_GROUPS_MAP_AUTHOLIST_IDX` | 0 | 2 | `MENU_ID` | A | 22 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_groups_map_autholist" (
  "GROUPS_MAP_AUTHOLIST_ID" int NOT NULL AUTO_INCREMENT,
  "GROUP_ID" int NOT NULL COMMENT 'Group ID from m_groups',
  "MENU_ID" int NOT NULL COMMENT 'Menu ID from m_menus',
  "AUTHOLIST_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Authorlist Status',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  PRIMARY KEY ("GROUPS_MAP_AUTHOLIST_ID"),
  UNIQUE KEY "M_GROUPS_MAP_AUTHOLIST_IDX" ("GROUP_ID","MENU_ID")
);
```


## `m_menus`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 17 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 20 |
| `CREATE_TIME` | 2026-04-25 09:15:24 |
| `UPDATE_TIME` | 2026-06-17 14:45:45 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `MENU_ID` | `int` | NO | - | auto_increment | PRI | - | - | Menu ID |
| 2 | `MENU_CODE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Menu Code |
| 3 | `MENU_NAME_EN` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Menu Name |
| 4 | `MENU_NAME_TH` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `MENU_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | A = Active, I = Inactive |
| 6 | `MENU_URL` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Link Menu |
| 7 | `MENU_SEQ` | `int` | NO | - | - | - | - | - | Display menu order by sequence. |
| 8 | `MENU_PARENT_CODE` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Refer to MENU_ID of Parent menu. |
| 9 | `MENU_ICON` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `MENU_GROUP` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | OPERATION, SYSTEM |
| 11 | `CREATE_BY` | `varchar(50)` | YES | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 12 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 13 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 14 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `MENU_ID` | A | 17 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_menus" (
  "MENU_ID" int NOT NULL AUTO_INCREMENT COMMENT 'Menu ID',
  "MENU_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Menu Code',
  "MENU_NAME_EN" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Menu Name',
  "MENU_NAME_TH" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "MENU_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'A = Active, I = Inactive',
  "MENU_URL" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Link Menu',
  "MENU_SEQ" int NOT NULL COMMENT 'Display menu order by sequence.',
  "MENU_PARENT_CODE" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Refer to MENU_ID of Parent menu.',
  "MENU_ICON" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "MENU_GROUP" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'OPERATION, SYSTEM',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY ("MENU_ID")
);
```


## `m_menus_map_permission`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 58 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 66 |
| `CREATE_TIME` | 2026-04-25 09:15:29 |
| `UPDATE_TIME` | 2026-06-17 14:41:33 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `MENU_MAP_PERMISSION_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `MENU_CODE` | `varchar(10)` | NO | - | - | MUL | utf8mb4 | utf8mb4_general_ci | Menu ID |
| 3 | `PERMISSION_CODE` | `varchar(20)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Permission ID |
| 4 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 5 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 6 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 7 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `MENU_MAP_PERMISSION_ID` | A | 58 | - | - | - | BTREE | - | - | YES | - |
| `M_MENU_MAP_PERMISSION_MENU_ID_IDX` | 0 | 1 | `MENU_CODE` | A | 16 | - | - | - | BTREE | - | - | YES | - |
| `M_MENU_MAP_PERMISSION_MENU_ID_IDX` | 0 | 2 | `PERMISSION_CODE` | A | 58 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_menus_map_permission" (
  "MENU_MAP_PERMISSION_ID" int NOT NULL AUTO_INCREMENT,
  "MENU_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Menu ID',
  "PERMISSION_CODE" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Permission ID',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY ("MENU_MAP_PERMISSION_ID"),
  UNIQUE KEY "M_MENU_MAP_PERMISSION_MENU_ID_IDX" ("MENU_CODE","PERMISSION_CODE")
);
```


## `m_message_code`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 60 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 61 |
| `CREATE_TIME` | 2026-04-25 09:15:36 |
| `UPDATE_TIME` | 2026-04-25 11:08:04 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `MESSAGE_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `MESSAGE_CODE` | `varchar(10)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | MP=mobile app, AP=admin portal, MA=mobile api, WA=web admin api |
| 3 | `MESSAGE_DESCRIPTION_EN` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `MESSAGE_DESCRIPTION_TH` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `REMARK` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `MESSAGE_STATUS` | `varchar(1)` | YES | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive |
| 7 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 9 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 10 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `MESSAGE_ID` | A | 60 | - | - | - | BTREE | - | - | YES | - |
| `UNQ_MESSAGE_CODE` | 0 | 1 | `MESSAGE_CODE` | A | 60 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_message_code" (
  "MESSAGE_ID" int NOT NULL AUTO_INCREMENT,
  "MESSAGE_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'MP=mobile app, AP=admin portal, MA=mobile api, WA=web admin api',
  "MESSAGE_DESCRIPTION_EN" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "MESSAGE_DESCRIPTION_TH" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "REMARK" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "MESSAGE_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("MESSAGE_ID"),
  UNIQUE KEY "UNQ_MESSAGE_CODE" ("MESSAGE_CODE")
);
```


## `m_mobile_users`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 2 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 3 |
| `CREATE_TIME` | 2026-04-25 09:15:45 |
| `UPDATE_TIME` | 2026-06-22 15:17:19 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `MOBILE_USER_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `MOBILE_UUID` | `varchar(36)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | UUID |
| 3 | `SMART_SEAMAN_ID` | `varchar(5)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Smart Seaman ID = running number start with 00001 |
| 4 | `USERNAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | User Name = Email |
| 5 | `PASSWORD` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Encrypted Password |
| 6 | `FIRST_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | First Name |
| 7 | `LAST_NAME` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Last |
| 8 | `DATE_OF_BIRTH` | `date` | YES | - | - | - | - | - | - |
| 9 | `COMPANY_CODE` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Company Name |
| 10 | `POSITION_CODE` | `varchar(10)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Position |
| 11 | `EMAIL` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Email |
| 12 | `MOBILE_NUMBER` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Mobile Number |
| 13 | `DISPLAY_TYPE` | `varchar(50)` | NO | NAME | - | - | utf8mb4 | utf8mb4_general_ci | NAME, PICTURE |
| 14 | `DISPLAY_NAME` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 15 | `PROFILE_PICTURE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | If have picture,will store filename from S3\nIf don't have picture = null / blank |
| 16 | `LAST_LOGON` | `timestamp` | YES | - | - | - | - | - | - |
| 17 | `USER_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status\nA= Active\nI = Inactive\nD= Del |
| 18 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 19 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 20 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 21 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `MOBILE_USER_ID` | A | 2 | - | - | - | BTREE | - | - | YES | - |
| `MOBILE_UUID_UNIQUE` | 0 | 1 | `MOBILE_UUID` | A | 2 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_mobile_users" (
  "MOBILE_USER_ID" int NOT NULL AUTO_INCREMENT,
  "MOBILE_UUID" varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID',
  "SMART_SEAMAN_ID" varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Smart Seaman ID = running number start with 00001',
  "USERNAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'User Name = Email',
  "PASSWORD" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Encrypted Password',
  "FIRST_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'First Name',
  "LAST_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Last',
  "DATE_OF_BIRTH" date DEFAULT NULL,
  "COMPANY_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Company Name',
  "POSITION_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Position',
  "EMAIL" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Email',
  "MOBILE_NUMBER" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Mobile Number',
  "DISPLAY_TYPE" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NAME' COMMENT 'NAME, PICTURE',
  "DISPLAY_NAME" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "PROFILE_PICTURE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'If have picture,will store filename from S3\\nIf don''t have picture = null / blank',
  "LAST_LOGON" timestamp NULL DEFAULT NULL,
  "USER_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status\\nA= Active\\nI = Inactive\\nD= Del',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY ("MOBILE_USER_ID"),
  UNIQUE KEY "MOBILE_UUID_UNIQUE" ("MOBILE_UUID")
);
```


## `m_news`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:15:53 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `NEWS_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `NEWS_TITLE` | `varchar(512)` | NO | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 3 | `NEWS_PICTURE_FILE_NAME` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 4 | `NEWS_TYPE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | GENERAL = ข่าวสารทั่วไป, SHIP = ข่าวสารงานเรือ |
| 5 | `NEWS_DETAILS` | `longtext` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 6 | `NEWS_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_0900_ai_ci | Status A= Publish, P = Pending, D= Delete |
| 7 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 9 | `PUBLISH_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 10 | `PUBLISH_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 11 | `UPDATE_DATE` | `timestamp` | YES | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 12 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `NEWS_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_news" (
  "NEWS_ID" int NOT NULL AUTO_INCREMENT,
  "NEWS_TITLE" varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  "NEWS_PICTURE_FILE_NAME" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  "NEWS_TYPE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'GENERAL = ข่าวสารทั่วไป, SHIP = ข่าวสารงานเรือ',
  "NEWS_DETAILS" longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  "NEWS_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Publish, P = Pending, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "PUBLISH_DATE" timestamp NULL DEFAULT NULL,
  "PUBLISH_BY" varchar(50) DEFAULT NULL,
  "UPDATE_DATE" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("NEWS_ID")
);
```


## `m_payment_transaction`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_unicode_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 114688 |
| `AUTO_INCREMENT` | - |
| `CREATE_TIME` | 2026-06-23 14:00:15 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `id` | `char(36)` | NO | uuid() | DEFAULT_GENERATED | PRI | utf8mb4 | utf8mb4_unicode_ci | - |
| 2 | `request_id` | `char(36)` | NO | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 3 | `parent_transaction_id` | `char(36)` | YES | - | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 4 | `transaction_no` | `varchar(40)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 5 | `transaction_type` | `varchar(10)` | NO | CHARGE | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 6 | `channel` | `varchar(30)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 7 | `payment_method` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 8 | `amount` | `decimal(12,2)` | NO | - | - | - | - | - | - |
| 9 | `currency` | `char(3)` | NO | THB | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 10 | `refunded_amount` | `decimal(12,2)` | NO | 0.00 | - | - | - | - | - |
| 11 | `status` | `varchar(20)` | NO | PENDING | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 12 | `provider` | `varchar(20)` | NO | OMISE | - | MUL | utf8mb4 | utf8mb4_unicode_ci | - |
| 13 | `provider_charge_id` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 14 | `provider_source_id` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 15 | `provider_refund_id` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 16 | `provider_transaction_id` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 17 | `provider_status` | `varchar(30)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 18 | `provider_response` | `json` | YES | - | - | - | - | - | - |
| 19 | `idempotency_key` | `varchar(100)` | NO | - | - | UNI | utf8mb4 | utf8mb4_unicode_ci | - |
| 20 | `description` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 21 | `return_uri` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 22 | `authorize_uri` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 23 | `bank_code` | `varchar(30)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 24 | `card_brand` | `varchar(30)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 25 | `card_last_digits` | `char(4)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 26 | `failure_code` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 27 | `failure_message` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_unicode_ci | - |
| 28 | `is_livemode` | `tinyint(1)` | NO | 0 | - | - | - | - | - |
| 29 | `expires_at` | `datetime` | YES | - | - | - | - | - | - |
| 30 | `paid_at` | `datetime` | YES | - | - | - | - | - | - |
| 31 | `failed_at` | `datetime` | YES | - | - | - | - | - | - |
| 32 | `refunded_at` | `datetime` | YES | - | - | - | - | - | - |
| 33 | `created_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 34 | `updated_at` | `datetime` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_payment_transaction_no` | 0 | 1 | `transaction_no` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_payment_idempotency_key` | 0 | 1 | `idempotency_key` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_payment_provider_charge` | 0 | 1 | `provider` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_payment_provider_charge` | 0 | 2 | `provider_charge_id` | A | 0 | - | - | YES | BTREE | - | - | YES | - |
| `uq_payment_provider_refund` | 0 | 1 | `provider` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `uq_payment_provider_refund` | 0 | 2 | `provider_refund_id` | A | 0 | - | - | YES | BTREE | - | - | YES | - |
| `idx_payment_request_created` | 1 | 1 | `request_id` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_payment_request_created` | 1 | 2 | `created_at` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_payment_status_updated` | 1 | 1 | `status` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_payment_status_updated` | 1 | 2 | `updated_at` | A | 0 | - | - | - | BTREE | - | - | YES | - |
| `idx_payment_parent` | 1 | 1 | `parent_transaction_id` | A | 0 | - | - | YES | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| `fk_payment_parent` | `parent_transaction_id` | `m_payment_transaction`.`id` | NO ACTION | NO ACTION |
| `fk_payment_request` | `request_id` | `m_document_request`.`id` | NO ACTION | NO ACTION |

### Create SQL

```sql
CREATE TABLE "m_payment_transaction" (
  "id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  "request_id" char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  "parent_transaction_id" char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "transaction_no" varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  "transaction_type" varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CHARGE',
  "channel" varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  "payment_method" varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  "amount" decimal(12,2) NOT NULL,
  "currency" char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'THB',
  "refunded_amount" decimal(12,2) NOT NULL DEFAULT '0.00',
  "status" varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  "provider" varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OMISE',
  "provider_charge_id" varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "provider_source_id" varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "provider_refund_id" varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "provider_transaction_id" varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "provider_status" varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "provider_response" json DEFAULT NULL,
  "idempotency_key" varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  "description" varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "return_uri" varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "authorize_uri" varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "bank_code" varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "card_brand" varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "card_last_digits" char(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "failure_code" varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "failure_message" varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  "is_livemode" tinyint(1) NOT NULL DEFAULT '0',
  "expires_at" datetime DEFAULT NULL,
  "paid_at" datetime DEFAULT NULL,
  "failed_at" datetime DEFAULT NULL,
  "refunded_at" datetime DEFAULT NULL,
  "created_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  UNIQUE KEY "uq_payment_transaction_no" ("transaction_no"),
  UNIQUE KEY "uq_payment_idempotency_key" ("idempotency_key"),
  UNIQUE KEY "uq_payment_provider_charge" ("provider","provider_charge_id"),
  UNIQUE KEY "uq_payment_provider_refund" ("provider","provider_refund_id"),
  KEY "idx_payment_request_created" ("request_id","created_at"),
  KEY "idx_payment_status_updated" ("status","updated_at"),
  KEY "idx_payment_parent" ("parent_transaction_id"),
  CONSTRAINT "fk_payment_parent" FOREIGN KEY ("parent_transaction_id") REFERENCES "m_payment_transaction" ("id"),
  CONSTRAINT "fk_payment_request" FOREIGN KEY ("request_id") REFERENCES "m_document_request" ("id"),
  CONSTRAINT "chk_payment_amount" CHECK ((`amount` > 0)),
  CONSTRAINT "chk_payment_channel" CHECK ((`channel` in (_utf8mb4'PROMPTPAY',_utf8mb4'CREDIT_CARD',_utf8mb4'INTERNET_BANKING'))),
  CONSTRAINT "chk_payment_currency" CHECK ((`currency` = _utf8mb4'THB')),
  CONSTRAINT "chk_payment_livemode" CHECK ((`is_livemode` in (0,1))),
  CONSTRAINT "chk_payment_refunded_amount" CHECK (((`refunded_amount` >= 0) and (`refunded_amount` <= `amount`))),
  CONSTRAINT "chk_payment_status" CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'PROCESSING',_utf8mb4'SUCCESS',_utf8mb4'FAILED',_utf8mb4'EXPIRED',_utf8mb4'PARTIALLY_REFUNDED',_utf8mb4'REFUNDED',_utf8mb4'CANCELLED'))),
  CONSTRAINT "chk_payment_type" CHECK ((`transaction_type` in (_utf8mb4'CHARGE',_utf8mb4'REFUND')))
);
```


## `m_permission`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 6 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 7 |
| `CREATE_TIME` | 2026-04-25 09:15:58 |
| `UPDATE_TIME` | 2026-04-25 11:08:20 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `PERMISSION_ID` | `int` | NO | - | auto_increment | PRI | - | - | Permission ID |
| 2 | `PERMISSION_CODE` | `varchar(20)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `PERMISSION_NAME` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Permission Name |
| 4 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 5 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | Create date |
| 6 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 7 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `PERMISSION_ID` | A | 6 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_permission" (
  "PERMISSION_ID" int NOT NULL AUTO_INCREMENT COMMENT 'Permission ID',
  "PERMISSION_CODE" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "PERMISSION_NAME" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Permission Name',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER' COMMENT 'Create by',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  PRIMARY KEY ("PERMISSION_ID")
);
```


## `m_positions`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 19 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 16384 |
| `AUTO_INCREMENT` | 20 |
| `CREATE_TIME` | 2026-04-25 09:16:03 |
| `UPDATE_TIME` | 2026-04-25 11:08:46 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `POSITION_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `POSITION_CODE` | `varchar(10)` | NO | - | - | UNI | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `POSITION_NAME_EN` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `POSITION_NAME_TH` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `POSITION_DESCRIPTION` | `varchar(500)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `POSITION_MOBILE_FLAG` | `varchar(1)` | NO | Y | - | - | utf8mb4 | utf8mb4_general_ci | Flag for showing on mobile app<br>Y = show<br>N = not show |
| 7 | `POSITION_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_general_ci | Status A= Active, I = Inactive, D= Delete |
| 8 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 9 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 11 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `POSITION_ID` | A | 19 | - | - | - | BTREE | - | - | YES | - |
| `UNQ_POSITION_CODE` | 0 | 1 | `POSITION_CODE` | A | 19 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_positions" (
  "POSITION_ID" int NOT NULL AUTO_INCREMENT,
  "POSITION_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "POSITION_NAME_EN" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "POSITION_NAME_TH" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "POSITION_DESCRIPTION" varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "POSITION_MOBILE_FLAG" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Y' COMMENT 'Flag for showing on mobile app\nY = show\nN = not show',
  "POSITION_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Active, I = Inactive, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("POSITION_ID"),
  UNIQUE KEY "UNQ_POSITION_CODE" ("POSITION_CODE")
);
```


## `m_send_notifications`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:17:31 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `READ_STATUS` | `varchar(5)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `NOTI_TYPE` | `varchar(20)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `MOBILE_USER_UUID` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `TERM` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | 18, 12, 6, 3 |
| 6 | `TITLE_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 7 | `BODY_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 8 | `SUCCESS` | `int` | YES | - | - | - | - | - | - |
| 9 | `FAILURE` | `int` | YES | - | - | - | - | - | - |
| 10 | `RESPONSE_BODY_FCM` | `json` | YES | - | - | - | - | - | - |
| 11 | `CREATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 12 | `READ_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 13 | `RETRY` | `int` | YES | - | - | - | - | - | - |
| 14 | `NOTI_DATE` | `date` | YES | - | - | - | - | - | - |
| 15 | `VALUE_ID` | `int` | YES | - | - | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_send_notifications" (
  "ID" int NOT NULL AUTO_INCREMENT,
  "READ_STATUS" varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "NOTI_TYPE" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "MOBILE_USER_UUID" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "TERM" varchar(10) DEFAULT NULL COMMENT '18, 12, 6, 3',
  "TITLE_MESSAGE" text,
  "BODY_MESSAGE" text,
  "SUCCESS" int DEFAULT NULL,
  "FAILURE" int DEFAULT NULL,
  "RESPONSE_BODY_FCM" json DEFAULT NULL,
  "CREATE_DATE" timestamp NULL DEFAULT NULL,
  "READ_DATE" timestamp NULL DEFAULT NULL,
  "RETRY" int DEFAULT NULL,
  "NOTI_DATE" date DEFAULT NULL,
  "VALUE_ID" int DEFAULT NULL,
  PRIMARY KEY ("ID")
);
```


## `m_send_notifications_backup`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:17:36 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `READ_STATUS` | `varchar(5)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `NOTI_TYPE` | `varchar(20)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `MOBILE_USER_UUID` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `TERM` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | 18, 12, 6, 3 |
| 6 | `TITLE_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 7 | `BODY_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | - |
| 8 | `SUCCESS` | `int` | YES | - | - | - | - | - | - |
| 9 | `FAILURE` | `int` | YES | - | - | - | - | - | - |
| 10 | `RESPONSE_BODY_FCM` | `json` | YES | - | - | - | - | - | - |
| 11 | `CREATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 12 | `READ_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 13 | `RETRY` | `int` | YES | - | - | - | - | - | - |
| 14 | `NOTI_DATE` | `date` | YES | - | - | - | - | - | - |
| 15 | `VALUE_ID` | `int` | YES | - | - | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_send_notifications_backup" (
  "ID" int NOT NULL AUTO_INCREMENT,
  "READ_STATUS" varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "NOTI_TYPE" varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "MOBILE_USER_UUID" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "TERM" varchar(10) DEFAULT NULL COMMENT '18, 12, 6, 3',
  "TITLE_MESSAGE" text,
  "BODY_MESSAGE" text,
  "SUCCESS" int DEFAULT NULL,
  "FAILURE" int DEFAULT NULL,
  "RESPONSE_BODY_FCM" json DEFAULT NULL,
  "CREATE_DATE" timestamp NULL DEFAULT NULL,
  "READ_DATE" timestamp NULL DEFAULT NULL,
  "RETRY" int DEFAULT NULL,
  "NOTI_DATE" date DEFAULT NULL,
  "VALUE_ID" int DEFAULT NULL,
  PRIMARY KEY ("ID")
);
```


## `m_voucher_details`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:17:44 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `VOUCHER_DETAIL_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `VOUCHER_ID` | `int` | NO | - | - | - | - | - | - |
| 3 | `SMART_SEAMAN_ID` | `varchar(5)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `VOUCHER_DETAIL_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_voucher_details" (
  "VOUCHER_DETAIL_ID" int NOT NULL AUTO_INCREMENT,
  "VOUCHER_ID" int NOT NULL,
  "SMART_SEAMAN_ID" varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("VOUCHER_DETAIL_ID")
);
```


## `m_vouchers`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:17:52 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `VOUCHER_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `VOUCHER_TITLE` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `VOUCHER_PICTURE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `VOUCHER_DETAILS` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `VOUCHER_TOTAL` | `int` | YES | - | - | - | - | - | - |
| 6 | `VOUCHER_REMAINING` | `int` | YES | - | - | - | - | - | - |
| 7 | `VOUCHER_START_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 8 | `VOUCHER_END_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 9 | `VOUCHER_BARCODE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `VOUCHER_QRCODE` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 11 | `VOUCHER_STATUS` | `varchar(1)` | NO | A | - | - | utf8mb4 | utf8mb4_0900_ai_ci | Status A= Publish, P = Pending, D= Delete |
| 12 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 13 | `CREATE_BY` | `varchar(50)` | NO | SYSTEM_USER | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 14 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 15 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 16 | `VOUCHER_TYPE` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_0900_ai_ci | GLOBAL, PERSONAL |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `VOUCHER_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "m_vouchers" (
  "VOUCHER_ID" int NOT NULL AUTO_INCREMENT,
  "VOUCHER_TITLE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "VOUCHER_PICTURE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "VOUCHER_DETAILS" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  "VOUCHER_TOTAL" int DEFAULT NULL,
  "VOUCHER_REMAINING" int DEFAULT NULL,
  "VOUCHER_START_DATE" timestamp NULL DEFAULT NULL,
  "VOUCHER_END_DATE" timestamp NULL DEFAULT NULL,
  "VOUCHER_BARCODE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "VOUCHER_QRCODE" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "VOUCHER_STATUS" varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'A' COMMENT 'Status A= Publish, P = Pending, D= Delete',
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM_USER',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "VOUCHER_TYPE" varchar(10) DEFAULT NULL COMMENT 'GLOBAL, PERSONAL',
  PRIMARY KEY ("VOUCHER_ID")
);
```


## `t_forgot_password`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:17:58 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `USER_UUID` | `varchar(50)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 3 | `IS_STATUS` | `varchar(5)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 4 | `CREATED_AT` | `timestamp` | YES | - | - | - | - | - | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "t_forgot_password" (
  "ID" int NOT NULL AUTO_INCREMENT,
  "USER_UUID" varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "IS_STATUS" varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "CREATED_AT" timestamp NULL DEFAULT NULL,
  PRIMARY KEY ("ID")
);
```


## `t_session`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 5 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 7 |
| `CREATE_TIME` | 2026-04-25 09:18:06 |
| `UPDATE_TIME` | 2026-06-22 15:17:34 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `SESSION_ID` | `int` | NO | - | auto_increment | PRI | - | - | Session ID |
| 2 | `CLIENT_SESSION_ID` | `varchar(100)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 3 | `USER_ID` | `varchar(36)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Admin ID / Customer ID |
| 4 | `DEVICE_MODEL` | `varchar(255)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `DEVICE_INFO` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `CORRELATIONID` | `varchar(100)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | Corrrelation ID / Trans ID |
| 7 | `TOKEN` | `text` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 8 | `LOGIN_TIME` | `timestamp` | YES | - | - | - | - | - | Login time |
| 9 | `LAST_UPDATE_TIME` | `timestamp` | YES | - | - | - | - | - | Last update time |
| 10 | `EXPIRE_TIME` | `timestamp` | YES | - | - | - | - | - | Expire time |
| 11 | `CREATE_DATE` | `timestamp` | YES | - | - | - | - | - | Create date |
| 12 | `CREATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Create by |
| 13 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | Update date |
| 14 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | Update by |
| 15 | `IS_ONLINE` | `varchar(3)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `SESSION_ID` | A | 5 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "t_session" (
  "SESSION_ID" int NOT NULL AUTO_INCREMENT COMMENT 'Session ID',
  "CLIENT_SESSION_ID" varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "USER_ID" varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Admin ID / Customer ID',
  "DEVICE_MODEL" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "DEVICE_INFO" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "CORRELATIONID" varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Corrrelation ID / Trans ID',
  "TOKEN" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "LOGIN_TIME" timestamp NULL DEFAULT NULL COMMENT 'Login time',
  "LAST_UPDATE_TIME" timestamp NULL DEFAULT NULL COMMENT 'Last update time',
  "EXPIRE_TIME" timestamp NULL DEFAULT NULL COMMENT 'Expire time',
  "CREATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Create date',
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Create by',
  "UPDATE_DATE" timestamp NULL DEFAULT NULL COMMENT 'Update date',
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Update by',
  "IS_ONLINE" varchar(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("SESSION_ID")
);
```


## `t_transaction_logs`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 47 |
| `DATA_LENGTH` | 49152 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 52 |
| `CREATE_TIME` | 2026-04-25 09:18:16 |
| `UPDATE_TIME` | 2026-06-22 15:19:28 |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `IN_LOG_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `TRANS_ID` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | transaction id / correlation id |
| 3 | `CLIENT_SESSION_ID` | `varchar(50)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 4 | `CORRELATION_ID` | `varchar(50)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 5 | `REQUEST_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `SERVICE_NAME` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `REQUEST_DATE_TIME` | `timestamp` | YES | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `LANGUAGE` | `varchar(2)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | TH / EN |
| 9 | `DEVICE_MODEL` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `DEVICE_INFO` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 11 | `TOKEN` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 12 | `REQUEST_DATA` | `json` | YES | - | - | - | - | - | - |
| 13 | `RESPONSE_DATE_TIME` | `timestamp` | YES | - | - | - | - | - | - |
| 14 | `RESPONSE_STATUS_CODE` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 15 | `RESPONSE_STATUS_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 16 | `RESPONSE_DATA` | `json` | YES | - | - | - | - | - | - |
| 17 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 18 | `CREATE_BY` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 19 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 20 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `IN_LOG_ID` | A | 47 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "t_transaction_logs" (
  "IN_LOG_ID" int NOT NULL AUTO_INCREMENT,
  "TRANS_ID" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'transaction id / correlation id',
  "CLIENT_SESSION_ID" varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "CORRELATION_ID" varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "REQUEST_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "SERVICE_NAME" varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "REQUEST_DATE_TIME" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  "LANGUAGE" varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'TH / EN',
  "DEVICE_MODEL" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "DEVICE_INFO" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "TOKEN" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  "REQUEST_DATA" json DEFAULT NULL,
  "RESPONSE_DATE_TIME" timestamp NULL DEFAULT NULL,
  "RESPONSE_STATUS_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "RESPONSE_STATUS_MESSAGE" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  "RESPONSE_DATA" json DEFAULT NULL,
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("IN_LOG_ID")
);
```


## `t_transaction_logs_offline`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:18:34 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `IN_LOG_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `TRANS_ID` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | transaction id / correlation id |
| 3 | `CLIENT_SESSION_ID` | `varchar(50)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 4 | `CORRELATION_ID` | `varchar(50)` | YES | - | - | - | utf8mb3 | utf8mb3_general_ci | - |
| 5 | `REQUEST_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 6 | `SERVICE_NAME` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 7 | `REQUEST_DATE_TIME` | `timestamp` | YES | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 8 | `LANGUAGE` | `varchar(2)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | TH / EN |
| 9 | `DEVICE_MODEL` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `DEVICE_INFO` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 11 | `TOKEN` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 12 | `REQUEST_DATA` | `json` | YES | - | - | - | - | - | - |
| 13 | `RESPONSE_DATE_TIME` | `timestamp` | YES | - | - | - | - | - | - |
| 14 | `RESPONSE_STATUS_CODE` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 15 | `RESPONSE_STATUS_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 16 | `RESPONSE_DATA` | `json` | YES | - | - | - | - | - | - |
| 17 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 18 | `CREATE_BY` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 19 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 20 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `IN_LOG_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "t_transaction_logs_offline" (
  "IN_LOG_ID" int NOT NULL AUTO_INCREMENT,
  "TRANS_ID" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'transaction id / correlation id',
  "CLIENT_SESSION_ID" varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "CORRELATION_ID" varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  "REQUEST_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "SERVICE_NAME" varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "REQUEST_DATE_TIME" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  "LANGUAGE" varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'TH / EN',
  "DEVICE_MODEL" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "DEVICE_INFO" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "TOKEN" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  "REQUEST_DATA" json DEFAULT NULL,
  "RESPONSE_DATE_TIME" timestamp NULL DEFAULT NULL,
  "RESPONSE_STATUS_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "RESPONSE_STATUS_MESSAGE" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  "RESPONSE_DATA" json DEFAULT NULL,
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("IN_LOG_ID")
);
```


## `t_txn_to_other_system`

### Table Info

| Property | Value |
|---|---|
| `ENGINE` | InnoDB |
| `TABLE_COLLATION` | utf8mb4_0900_ai_ci |
| `TABLE_ROWS` | 0 |
| `DATA_LENGTH` | 16384 |
| `INDEX_LENGTH` | 0 |
| `AUTO_INCREMENT` | 1 |
| `CREATE_TIME` | 2026-04-25 09:18:40 |
| `UPDATE_TIME` | - |
| `TABLE_COMMENT` | - |

### Columns

| # | Column | Type | Nullable | Default | Extra | Key | Charset | Collation | Comment |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | `OUT_LOG_ID` | `int` | NO | - | auto_increment | PRI | - | - | - |
| 2 | `TRANS_ID` | `varchar(255)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 3 | `RESPONSE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 4 | `SERVICE_NAME` | `varchar(100)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 5 | `REQUEST_DATE_TIME` | `timestamp` | YES | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 6 | `REQUEST_DATA` | `json` | YES | - | - | - | - | - | - |
| 7 | `RESPONSE_DATE_TIME` | `timestamp` | YES | - | - | - | - | - | - |
| 8 | `RESPONSE_STATUS_CODE` | `varchar(10)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 9 | `RESPONSE_STATUS_MESSAGE` | `text` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 10 | `RESPONSE_DATA` | `json` | YES | - | - | - | - | - | - |
| 11 | `CREATE_DATE` | `timestamp` | NO | CURRENT_TIMESTAMP | DEFAULT_GENERATED | - | - | - | - |
| 12 | `CREATE_BY` | `varchar(50)` | NO | - | - | - | utf8mb4 | utf8mb4_general_ci | - |
| 13 | `UPDATE_DATE` | `timestamp` | YES | - | - | - | - | - | - |
| 14 | `UPDATE_BY` | `varchar(50)` | YES | - | - | - | utf8mb4 | utf8mb4_general_ci | - |

### Indexes

| Key | Non Unique | Seq | Column | Collation | Cardinality | Sub Part | Packed | Null | Type | Comment | Index Comment | Visible | Expression |
|---|---:|---:|---|---|---:|---|---|---|---|---|---|---|---|
| `PRIMARY` | 0 | 1 | `OUT_LOG_ID` | A | 0 | - | - | - | BTREE | - | - | YES | - |

### Foreign Keys

| Constraint | Column | References | On Update | On Delete |
|---|---|---|---|---|
| - | - | - | - | - |

### Create SQL

```sql
CREATE TABLE "t_txn_to_other_system" (
  "OUT_LOG_ID" int NOT NULL AUTO_INCREMENT,
  "TRANS_ID" varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "RESPONSE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "SERVICE_NAME" varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "REQUEST_DATE_TIME" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  "REQUEST_DATA" json DEFAULT NULL,
  "RESPONSE_DATE_TIME" timestamp NULL DEFAULT NULL,
  "RESPONSE_STATUS_CODE" varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  "RESPONSE_STATUS_MESSAGE" text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  "RESPONSE_DATA" json DEFAULT NULL,
  "CREATE_DATE" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "CREATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  "UPDATE_DATE" timestamp NULL DEFAULT NULL,
  "UPDATE_BY" varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY ("OUT_LOG_ID")
);
```


