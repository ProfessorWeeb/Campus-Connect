# Step-by-Step AWS Deployment Guide

Follow these steps in order. Check off each step as you complete it.

## ✅ STEP 1: Set Database Password

**What you need**: The master password you set when creating the RDS database

**Action**: 
1. Open `backend/src/main/resources/application-prod.properties`
2. Find line 7: `spring.datasource.password=YOUR_RDS_PASSWORD`
3. Replace `YOUR_RDS_PASSWORD` with your actual database password

**Example**:
```properties
spring.datasource.password=MySecurePassword123!
```

---

## ✅ STEP 2: Generate JWT Secret

**What you need**: A secure random string (32+ characters)

**Action**: Generate a secret using one of these methods:

**Option A - Using OpenSSL (if you have Git Bash or WSL):**
```bash
openssl rand -base64 32
```

**Option B - Using Node.js:**
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

**Option C - Using Python:**
```bash
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

Then:
1. Copy the generated secret
2. Open `backend/src/main/resources/application-prod.properties`
3. Find line 19: `jwt.secret=YOUR_SECURE_RANDOM_SECRET_KEY_MIN_32_CHARACTERS`
4. Replace with your generated secret

---

## ✅ STEP 3: Configure RDS Security Group

**What you need**: Access to AWS Console

**Action**:
1. Go to AWS Console → EC2 → Security Groups
2. Find the security group attached to your RDS instance (look for one with "rds" in the name or description)
3. Click on it → Inbound rules tab → Edit inbound rules
4. Click "Add rule"
5. Configure:
   - Type: PostgreSQL
   - Port: 5432
   - Source: 0.0.0.0/0 (for now - we'll restrict later)
6. Click "Save rules"

**Note**: For production, you'll restrict this to only allow your backend later.

---

## ✅ STEP 4: Build Backend JAR

**What you need**: Maven (mvnw is included in the project)

**Action**: Run this command:
```bash
cd backend
./mvnw clean package -DskipTests
```

**Expected result**: A file `backend/target/campus-connect-1.0.0.jar` should be created

---

## ✅ STEP 5: Install EB CLI (if not installed)

**What you need**: Python and pip

**Action**: 
```bash
pip install awsebcli
```

**Verify**:
```bash
eb --version
```

---

## ✅ STEP 6: Initialize Elastic Beanstalk

**Action**:
```bash
cd backend
eb init
```

**Prompts**:
- Select a region: Choose `us-east-2` (same as your database)
- Select application name: Type `campusconnect` or press Enter
- Select platform: Choose `Java`
- Select platform version: Choose `Java 17 running on 64bit Amazon Linux 2`
- SSH: Type `y` (optional, for debugging)

---

## ✅ STEP 7: Create Elastic Beanstalk Environment

**Action**:
```bash
eb create campusconnect-env
```

**This will**:
- Create an EC2 instance
- Set up a load balancer
- Deploy your application
- Take 5-10 minutes

**Monitor progress**: You can watch in AWS Console → Elastic Beanstalk

---

## ✅ STEP 8: Get Backend URL

**Action**:
```bash
eb status
```

**Or**: Check AWS Console → Elastic Beanstalk → Your Environment → URL

**Save this URL** - you'll need it for the frontend!

---

## ✅ STEP 9: Update Frontend API Calls

**What you need**: Update all frontend files to use the new axiosConfig

**Action**: See `UPDATE_FRONTEND_API.md` for detailed instructions

**Quick summary**:
- Replace `import axios from 'axios'` with `import api from '../utils/axiosConfig'`
- Replace `axios.get('http://localhost:8080/api/...')` with `api.get('/...')`
- Remove `/api` prefix (it's already in baseURL)

---

## ✅ STEP 10: Create Frontend Environment File

**Action**: 
1. Create file `frontend/.env.production` (it's gitignored, so create manually)
2. Add this line (replace with your actual backend URL):
```
REACT_APP_API_URL=https://YOUR_EB_URL/api
```

**Example**:
```
REACT_APP_API_URL=https://campusconnect-env.us-east-2.elasticbeanstalk.com/api
```

---

## ✅ STEP 11: Build Frontend

**Action**:
```bash
cd frontend
npm install
npm run build
```

**Expected result**: A `frontend/build/` directory with your compiled React app

---

## ✅ STEP 12: Create S3 Bucket

**Action**:
1. Go to AWS Console → S3 → Create bucket
2. Configure:
   - Bucket name: `campusconnect-frontend-YOUR-UNIQUE-NAME` (must be globally unique)
   - Region: `us-east-2` (same as database)
   - Uncheck "Block all public access" (we'll use CloudFront)
3. Click Create bucket

---

## ✅ STEP 13: Upload Frontend to S3

**Action**:
```bash
aws s3 sync frontend/build/ s3://YOUR-BUCKET-NAME --delete
```

**Or** via AWS Console:
1. Go to your S3 bucket
2. Click Upload
3. Select all files from `frontend/build/`
4. Click Upload

---

## ✅ STEP 14: Configure S3 for Static Website

**Action**:
1. Go to your S3 bucket → Properties
2. Scroll to "Static website hosting"
3. Click Edit
4. Enable static website hosting
5. Index document: `index.html`
6. Error document: `index.html`
7. Save

---

## ✅ STEP 15: Create CloudFront Distribution

**Action**:
1. Go to AWS Console → CloudFront → Create distribution
2. Configure:
   - Origin domain: Select your S3 bucket
   - Origin access: Select "S3 bucket" (not website endpoint)
   - Viewer protocol policy: Redirect HTTP to HTTPS
   - Default root object: `index.html`
3. Click Create distribution

**Note**: This takes 10-15 minutes to deploy

---

## ✅ STEP 16: Configure CloudFront Error Pages

**Action**:
1. Go to your CloudFront distribution → Error pages tab
2. Create custom error response:
   - HTTP error code: `403`
   - Response page path: `/index.html`
   - HTTP response code: `200`
3. Repeat for `404` error

**Why**: This ensures React Router works correctly

---

## ✅ STEP 17: Update CORS in Backend

**Action**:
1. Get your CloudFront distribution URL (from Step 15)
2. Open `backend/src/main/resources/application-prod.properties`
3. Update line 24:
```properties
spring.web.cors.allowed-origins=https://YOUR_CLOUDFRONT_URL,http://localhost:3000
```
4. Rebuild and redeploy backend:
```bash
cd backend
./mvnw clean package -DskipTests
eb deploy
```

---

## ✅ STEP 18: Update Frontend Environment and Rebuild

**Action**:
1. Update `frontend/.env.production` with correct backend URL (if not already done)
2. Rebuild frontend:
```bash
cd frontend
npm run build
```
3. Re-upload to S3:
```bash
aws s3 sync build/ s3://YOUR-BUCKET-NAME --delete
```

---

## ✅ STEP 19: Test Everything

**Test**:
1. Visit your CloudFront URL
2. Try registering a new user
3. Try logging in
4. Test creating a group
5. Test sending messages

---

## ✅ STEP 20: Secure RDS Security Group (Final Step)

**Action**:
1. Get your Elastic Beanstalk security group ID:
   - Go to EC2 → Security Groups
   - Find the one created by Elastic Beanstalk (look for "awseb" in name)
2. Update RDS security group:
   - Remove the `0.0.0.0/0` rule
   - Add rule: Type PostgreSQL, Port 5432, Source: Your EB security group

---

## 🎉 Done!

Your application should now be fully deployed on AWS!

