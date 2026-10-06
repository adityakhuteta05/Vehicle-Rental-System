# 🚀 DriveSense Platform — Complete Deployment & Database Guide

This guide provides step-by-step instructions for deploying the **DriveSense Smart Car Rental Platform** and its database across different environments:
1. **[Method 1: 1-Command Docker Compose (App + Production MySQL)](#method-1-1-command-docker-compose-recommended)** *(Recommended for local & server production)*
2. **[Method 2: Cloud Deployment (Render / Railway / Koyeb)](#method-2-cloud-deployment-render--railway)** *(Free live URL for demos & portfolios)*
3. **[Method 3: Local Deployment with Standalone MySQL](#method-3-local-deployment-with-standalone-mysql)** *(Traditional MySQL setup on Windows/Mac/Linux)*
4. **[Method 4: Linux VPS / AWS EC2 Deployment](#method-4-linux-vps--aws-ec2-deployment)** *(Production VPS with Nginx & SSL)*
5. **[Method 5: Instant 1-Click Zero-Config Run (Embedded H2)](#method-5-instant-1-click-zero-config-run-embedded-h2)** *(Quick evaluation without installing MySQL)*

---

## 🔑 Pre-Configured Accounts (Auto-Seeded)

Once the application and database boot up, the system automatically detects if the database is fresh and seeds all sample locations, 13 vehicles, and the following demo accounts:

| Role | Email Address | Password | Permissions & Dashboard Access |
| :--- | :--- | :--- | :--- |
| **Fleet Admin** | `admin@drivesense.com` | `admin123` | Full access to `/admin`, Revenue analytics, Fleet CRUD, DCR inspections, XML Fleet Import. |
| **Vehicle Owner** | `owner@drivesense.com` | `owner123` | Access to `/owner/dashboard`, Vehicle listings, Earnings reports, Payouts. |
| **Gold Customer** | `user@example.com` | `user123` | Trust Score 78 (Gold Tier, 3% discount, 50% deposit waiver), past booking `DS-2026-000123`. |
| **Platinum VIP** | `vip@example.com` | `vip123` | Trust Score 92 (Platinum Tier, 6% discount, **100% zero deposit**). |

---

## Method 1: 1-Command Docker Compose (Recommended)

This method spins up a production-grade **MySQL 8.0 container** and the **DriveSense Spring Boot container**, networks them together, verifies MySQL health before launching the app, and persists all data to a Docker volume.

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### Steps to Deploy:
1. Open PowerShell or Terminal in the project root directory:
   ```bash
   cd "Vehical Rental System"
   ```

2. Start the entire application and database stack:
   ```bash
   docker compose up --build -d
   ```

3. Check container status:
   ```bash
   docker compose ps
   ```

4. View live logs:
   ```bash
   docker compose logs -f drivesense
   ```

5. Open your browser:
   👉 **`http://localhost:8080/`**

### Stopping or Resetting:
- To stop the containers:
  ```bash
  docker compose down
  ```
- To stop and completely wipe database data to re-seed clean:
  ```bash
  docker compose down -v
  ```

---

## Method 2: Cloud Deployment (Render / Railway)

### Option A: Railway.app (Easiest Cloud Setup with Free MySQL)

1. **Push your code to GitHub**:
   Ensure your project is pushed to a GitHub repository.

2. **Create a Project in Railway**:
   - Go to [Railway.app](https://railway.app/) and sign in with GitHub.
   - Click **"New Project"** -> **"Provision MySQL"**.
   - Railway will launch a managed MySQL database instance.

3. **Deploy the Spring Boot App**:
   - In the same project canvas, click **"+ New"** -> **"GitHub Repo"** -> select your repository.
   - Go to the deployed service **Variables** tab and add:
     - `SPRING_PROFILES_ACTIVE` = `mysql`
     - `SPRING_DATASOURCE_URL` = `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
     - `SPRING_DATASOURCE_USERNAME` = `${{MySQL.MYSQLUSER}}`
     - `SPRING_DATASOURCE_PASSWORD` = `${{MySQL.MYSQLPASSWORD}}`
     - `PORT` = `8080`

4. **Generate Domain**:
   - Under the service **Settings** -> **Networking**, click **"Generate Domain"**.
   - Your live URL is ready (e.g. `https://drivesense-production.up.railway.app`).

---

### Option B: Render.com (Using Docker Web Service)

1. **Database Setup**:
   - Create a free cloud MySQL database on [Aiven.io](https://aiven.io), [Clever-Cloud.com](https://www.clever-cloud.com), or [TiDB Cloud](https://tidbcloud.com/).
   - Copy the JDBC URL, Username, and Password.
   *(Alternatively: You can run on Render in zero-setup mode using the built-in H2 engine without any external DB).*

2. **Deploy on Render**:
   - Go to [Render Dashboard](https://dashboard.render.com/) -> **New +** -> **Web Service**.
   - Connect your GitHub repository.
   - **Runtime**: Select **Docker** (Render will automatically detect the multi-stage `Dockerfile`).
   - Under **Environment Variables**, add:
     - `PORT` = `8080`
     - `SPRING_PROFILES_ACTIVE` = `mysql`
     - `SPRING_DATASOURCE_URL` = `<Your Cloud JDBC URL>`
     - `SPRING_DATASOURCE_USERNAME` = `<Your Cloud DB User>`
     - `SPRING_DATASOURCE_PASSWORD` = `<Your Cloud DB Password>`
   - Click **Create Web Service**. Render builds the image and deploys.

---

## Method 3: Local Deployment with Standalone MySQL

Use this method if you have MySQL Server (or XAMPP/WAMP) installed directly on your machine.

### Step 1: Create the Database in MySQL
Open MySQL Workbench, phpMyAdmin, or your terminal:
```sql
CREATE DATABASE drivesense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> **Note on Schema & Seed Data:**  
> You do **not** need to manually run SQL scripts! Spring Boot's JPA entity manager automatically generates all tables (`ddl-auto=update`), and `DataInitializer.java` automatically seeds the 13 cars, locations, and demo accounts upon first startup.  
> *(Optional: If you prefer manual SQL scripts, you can execute `src/main/resources/schema.sql` followed by `src/main/resources/data.sql`).*

### Step 2: Build the Production JAR
In your project directory:
```powershell
# Windows
.\mvn.cmd clean package -DskipTests

# Linux / Mac
./mvnw clean package -DskipTests
```
This produces `target/drivesense-platform-1.0.0.jar`.

### Step 3: Launch with MySQL
#### Option 1: Double click the batch file (Windows)
Double-click `start-production-mysql.bat`.

#### Option 2: Run via Command Line
```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="drivesense"
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"

java -jar target/drivesense-platform-1.0.0.jar
```

```bash
# Linux / macOS
export SPRING_PROFILES_ACTIVE=mysql
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=drivesense
export DB_USER=root
export DB_PASSWORD=your_mysql_password

java -jar target/drivesense-platform-1.0.0.jar
```

Open browser at: **`http://localhost:8080/`**.

---

## Method 4: Linux VPS / AWS EC2 Deployment

### 1. Provision Server
Launch an Ubuntu 22.04 / 24.04 LTS instance (AWS EC2 t3.small, DigitalOcean Droplet, Hetzner, etc.).

### 2. Install Docker & Compose
```bash
sudo apt update && sudo apt install -y docker.io docker-compose-plugin git
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
```

### 3. Clone Repository & Run
```bash
git clone https://github.com/your-username/Vehicle-Rental-System.git
cd Vehicle-Rental-System
docker compose up --build -d
```

### 4. Setup Nginx Reverse Proxy with Free SSL (Let's Encrypt)
Install Nginx and Certbot:
```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

Create `/etc/nginx/sites-available/drivesense`:
```nginx
server {
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable site and acquire SSL:
```bash
sudo ln -s /etc/nginx/sites-available/drivesense /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d yourdomain.com
```
Your platform is now securely running at `https://yourdomain.com`.

---

## Method 5: Instant 1-Click Zero-Config Run (Embedded H2)

If you need to quickly test or evaluate without installing Docker or MySQL:

```powershell
# Windows
.\mvn.cmd spring-boot:run
```

```bash
# Linux / Mac
./mvnw spring-boot:run
```

- Accessible at: **`http://localhost:8080/`**
- Embedded H2 Console: **`http://localhost:8080/h2-console`**
  - JDBC URL: `jdbc:h2:mem:drivesensedb`
  - User: `sa`
  - Password: *(blank)*

---

## 🛠️ Verification Checklist After Deployment

- [ ] **Home Page:** Open `/` — verify hero section, live fleet counters, and search bar.
- [ ] **Fleet Catalogue:** Open `/cars` — verify 13 cars with filter chips and real-time availability badges.
- [ ] **Vibe Match Recommender:** Open `/vibe-match` — test wizard recommendation algorithm.
- [ ] **Customer Login:** Log in with `user@example.com` / `user123` — verify Gold tier discount badge and profile details.
- [ ] **Dynamic Pricing:** Open `/book/1` — test date selection; verify AJAX live price calculator.
- [ ] **Booking Flow:** Complete booking reservation — verify invoice view and XML invoice download.
- [ ] **Admin Dashboard:** Log in with `admin@drivesense.com` / `admin123` — open `/admin`, verify Chart.js analytics and Digital Condition Report (DCR).
- [ ] **XML Fleet Import:** Open `/admin/fleet/import-xml` — test uploading `src/main/resources/xml/sample-fleet.xml`.
