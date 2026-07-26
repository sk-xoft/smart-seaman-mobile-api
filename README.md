# Smart Seaman Mobile API

Spring Boot 2.6.2 REST API backend for the Smart Seaman mobile application.

---

## Prerequisites

- Java 25+
- Maven 3.6+ (or use included `./mvnw`)
- MySQL 8.0+ (for local DB option)
- Docker (for container deployment)

---

## Run with System Maven (mvn)

หาก Maven Wrapper (`./mvnw`) ไม่พร้อมใช้งาน ให้ใช้ `mvn` ที่ติดตั้งไว้ในระบบแทน โดย set environment variables ก่อนรัน:

```bash
export DB_URL="jdbc:mysql://<host>:<port>/<database>?autoReconnect=true&useSSL=false"
export DB_USERNAME="<db_user>"
export DB_PASSWORD="<db_password>"
export DO_SPACES_KEY="<digitalocean_spaces_key>"
export DO_SPACES_SECRET="<digitalocean_spaces_secret>"
export ENCRYPT_KEY="<encrypt_key>"
export JWT_SECRET="<jwt_secret>"
export MAIL_PASSWORD="<gmail_app_password>"
export FCM_CREDENTIAL_FILE="<absolute_path_to_firebase_json>"

mvn spring-boot:run
```

หรือรวมเป็นบรรทัดเดียว:

```bash
DB_URL="..." DB_USERNAME="..." DB_PASSWORD="..." \
DO_SPACES_KEY="..." DO_SPACES_SECRET="..." \
ENCRYPT_KEY="..." JWT_SECRET="..." \
MAIL_PASSWORD="..." FCM_CREDENTIAL_FILE="..." \
mvn spring-boot:run
```

> **Note:** ค่า config จริงอยู่ที่ `config/mobile/prod/.env` (ไม่ได้ commit เข้า repo)

API พร้อมใช้งานที่: `http://localhost:8080`
Swagger UI: `http://localhost:8080/smart-seaman-swagger`

---

## Run Locally (Development)

### Option A — ใช้ Dev DB บน DigitalOcean (แนะนำ ไม่ต้องติดตั้ง MySQL)

1. แก้ไขไฟล์ `src/main/resources/application-local.properties`
   Uncomment ส่วน Dev DB และ comment ส่วน Local MySQL:

   ```properties
   #smart.seaman.datasource.url=jdbc:mysql://localhost:3306/smartseaman...
   #smart.seaman.datasource.username=root
   #smart.seaman.datasource.password=P@ssw0rd

   smart.seaman.datasource.url=jdbc:mysql://dev-smartseaman-db-01-do-user-7722588-0.b.db.ondigitalocean.com:25060/dev-seaman?autoReconnect=true&useSSL=false
   smart.seaman.datasource.username=dev-seaman-user
   smart.seaman.datasource.password=
   ```

2. รัน application:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. API พร้อมใช้งานที่: `http://localhost:8081`
   Swagger UI: `http://localhost:8081/smart-seaman-swagger`

---

### Option B — ใช้ Local MySQL

1. สร้าง database ใน MySQL:

   ```sql
   CREATE DATABASE smartseaman CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Import SQL views จาก `src/main/resources/SQL/CreateView.sql`

3. แก้ไขไฟล์ `src/main/resources/application-local.properties`
   ตั้งค่า username/password ให้ตรงกับ local MySQL ของคุณ:

   ```properties
   smart.seaman.datasource.url=jdbc:mysql://localhost:3306/smartseaman?autoreconnect=true
   smart.seaman.datasource.username=root
   smart.seaman.datasource.password=P@ssw0rd
   ```

4. รัน application:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. API พร้อมใช้งานที่: `http://localhost:8081`

---

## Build & Run with Docker

### 1. Build JAR

```bash
./mvnw clean package -DskipTests
```

### 2. Build Docker Image

```bash
docker build -t smart-seaman-mobile-api:latest .
```

### 3. Run Container

```bash

mvn clean package -DskipTests && docker build -t smart-seaman-mobile-api:latest .


```

### Run Docker with configuration

#### Docker build on local

```bash

docker run --name smart-seaman-mobile-api -d \
  --env-file /Users/sk/works/products/smartseaman.com/source-code/config/mobile-api/non-prod/.env \
  -v /Users/sk/works/products/smartseaman.com/source-code/config/mobile-api/non-prod/smart-seaman-firebase.json:/app/firebase.json \
  -e FCM_CREDENTIAL_FILE=/app/firebase.json \
  -it -p 30000:8080/tcp \
  smart-seaman-mobile-api
  

```

#### Docker build on nonprod
```bash

docker run --name smart-seaman-mobile-api -d \
  --env-file /home/ssmuser/apps/config/mobile-api/non-prod/.env \
  -v /home/ssmuser/apps/config/mobile-api/non-prod/smart-seaman-firebase.json:/app/firebase.json \
  -v /home/ssmuser/apps/logs-srv/mobile-api/logs:../logs \
  -e FCM_CREDENTIAL_FILE=/app/firebase.json \
  -it -p 30000:8080/tcp \
  smart-seaman-mobile-api:latest

```


#### Docker build on prod
```bash

docker run --name smart-seaman-mobile-api-0.6 -d \
  --env-file /home/ssmuser/apps/config/mobile/prod/.env \
  -v /home/ssmuser/apps/config/mobile/prod/smart-seaman-firebase.json:/app/firebase.json \
  -v /home/ssmuser/apps-logs-service/smart-seaman-mobile-api/logs:/apps-logs-service/smart-seaman-mobile-api/logs \
  -e FCM_CREDENTIAL_FILE=/app/firebase.json \
  -it -p 30000:8080/tcp \
  xoftspace/smart-seaman-mobile-api:0.6

```

> **Note:** Container ใช้ config จาก `application.properties` (prod profile) โดย default
> Port mapping: host `30000` → container `8080`

### ดู Logs

```bash
docker logs -f smart-seaman-mobile-api
```

### หยุด / ลบ Container

```bash
docker stop smart-seaman-mobile-api
docker rm smart-seaman-mobile-api
```

---

## Environment Profiles

| Profile | คำสั่ง | Database |
| ------- | ------ | -------- |
| `local` | `-Dspring-boot.run.profiles=local` | Local MySQL หรือ Dev DB |
| `prod` (default) | (ไม่ต้องระบุ) | Production DB บน DigitalOcean |

---

## Run Tests

```bash
# รัน tests ทั้งหมด
./mvnw test

# รัน test class เฉพาะ
./mvnw test -Dtest=ClassName
```


## Update JAVA VERSION

### ติดตั้ง Java
```bash

sudo apt update
sudo apt install -y openjdk-17-jdk

```

ตรวจสอบ Java ที่ติดตั้ง:
```bash
ls -la /usr/lib/jvm/
```

จากนั้นเลือก Java 17 เป็นค่าเริ่มต้น:
```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

ตรวจสอบ Java ที่ติดตั้ง:
```bash
ls -la /usr/lib/jvm/
```

เพิ่มลงใน ~/.bashrc:
```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```


# Add Scan code 

```bash

./mvnw clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=sqp_b40e93ebe0c4f5b42bbc30a6d496b05f23ac47e7

```

```bash

mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=smart-seaman-mobile-api \
  -Dsonar.projectName='smart-seaman-mobile-api' \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=sqp_b40e93ebe0c4f5b42bbc30a6d496b05f23ac47e7

```
