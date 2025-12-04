# AWS Deployment Guide for CampusConnect

This guide will walk you through deploying CampusConnect to AWS, including the database, backend, and frontend.

## Related Documents

- **Quick Start**: See `AWS_DEPLOYMENT_QUICK_START.md` for a condensed version
- **Checklist**: Use `DEPLOYMENT_CHECKLIST.md` to track your progress
- **Frontend Updates**: See `UPDATE_FRONTEND_API.md` for updating API calls

## Architecture Overview

- **Database**: AWS RDS (PostgreSQL)
- **Backend**: AWS Elastic Beanstalk (Spring Boot)
- **Frontend**: AWS S3 + CloudFront (React)

## Prerequisites

1. AWS Account with appropriate permissions
2. AWS CLI installed and configured
3. Maven installed (for building backend)
4. Node.js and npm installed (for building frontend)
5. Git installed

## Step 1: Set Up AWS RDS PostgreSQL Database

### 1.1 Create RDS Instance

1. Go to AWS Console → RDS → Databases → Create database
2. Choose **PostgreSQL** as the engine
3. Select **Free tier** template (or appropriate tier for your needs)
4. Configure:
   - **DB instance identifier**: `campusconnect-db`
   - **Master username**: `postgres` (or your preferred username)
   - **Master password**: Create a strong password (save this!)
   - **DB instance class**: `db.t3.micro` (free tier eligible)
   - **Storage**: 20 GB (free tier)
   - **VPC**: Default VPC (or create new)
   - **Public access**: **Yes** (for initial setup, can restrict later)
   - **Security group**: Create new or use existing
   - **Database name**: `campusconnect`
5. Click **Create database**

### 1.2 Configure Security Group

1. Go to EC2 → Security Groups
2. Find the security group attached to your RDS instance
3. Edit **Inbound rules**:
   - Add rule: Type: **PostgreSQL**, Port: **5432**, Source: Your backend's security group (or `0.0.0.0/0` for testing - **restrict later**)

### 1.3 Get Database Endpoint

1. In RDS console, click on your database instance
2. Copy the **Endpoint** (e.g., `campusconnect-db.xxxxx.us-east-1.rds.amazonaws.com`)
3. Note the **Port** (usually 5432)

## Step 2: Prepare Backend for Deployment

### 2.1 Update Application Properties

Create a production profile configuration file:

**File**: `backend/src/main/resources/application-prod.properties`

```properties
# Server Configuration
server.port=5000

# Database Configuration - AWS RDS PostgreSQL
spring.datasource.url=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/campusconnect
spring.datasource.username=postgres
spring.datasource.password=YOUR_RDS_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# JWT Configuration - CHANGE THIS IN PRODUCTION!
jwt.secret=YOUR_SECURE_RANDOM_SECRET_KEY_MIN_32_CHARACTERS
jwt.expiration=86400000

# CORS Configuration - Update with your frontend URL
spring.web.cors.allowed-origins=https://YOUR_CLOUDFRONT_DOMAIN,http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*

# Logging
logging.level.com.campusconnect=INFO
logging.level.org.springframework.security=WARN
```

**Important**: Replace:
- `YOUR_RDS_ENDPOINT` with your RDS endpoint
- `YOUR_RDS_PASSWORD` with your RDS master password
- `YOUR_SECURE_RANDOM_SECRET_KEY_MIN_32_CHARACTERS` with a secure random string
- `YOUR_CLOUDFRONT_DOMAIN` with your CloudFront distribution domain (after Step 4)

### 2.2 Create .ebextensions Configuration

Create directory: `backend/.ebextensions/`

**File**: `backend/.ebextensions/environment.config`

```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: prod
  aws:elasticbeanstalk:container:java:
    JVM Options: '-Xmx512m -Xms256m'
```

### 2.3 Create Procfile for Elastic Beanstalk

**File**: `backend/Procfile`

```
web: java -jar target/campus-connect-1.0.0.jar --spring.profiles.active=prod
```

### 2.4 Build JAR File

```bash
cd backend
./mvnw clean package -DskipTests
```

This creates `backend/target/campus-connect-1.0.0.jar`

## Step 3: Deploy Backend to AWS Elastic Beanstalk

### 3.1 Install EB CLI

```bash
pip install awsebcli
```

### 3.2 Initialize Elastic Beanstalk

```bash
cd backend
eb init
```

Follow prompts:
- Select region (e.g., `us-east-1`)
- Select application name: `campusconnect`
- Select platform: **Java**
- Select platform version: **Java 17 running on 64bit Amazon Linux 2**
- Select SSH: Yes (optional, for debugging)

### 3.3 Create Environment

```bash
eb create campusconnect-env
```

This will:
- Create EC2 instance
- Set up load balancer
- Deploy your application

**Note**: This takes 5-10 minutes. You can monitor progress in the AWS Console.

### 3.4 Configure Environment Variables

After deployment, set environment variables:

```bash
eb setenv SPRING_PROFILES_ACTIVE=prod
```

Or via AWS Console:
1. Go to Elastic Beanstalk → Your Environment → Configuration
2. Software → Environment properties
3. Add: `SPRING_PROFILES_ACTIVE` = `prod`

### 3.5 Get Backend URL

```bash
eb status
```

Or check AWS Console → Elastic Beanstalk → Your Environment → URL

Example: `http://campusconnect-env.xxxxx.us-east-1.elasticbeanstalk.com`

### 3.6 Update CORS in application-prod.properties

Update the CORS allowed origins with your actual backend URL and future frontend URL.

## Step 4: Deploy Frontend to AWS S3 + CloudFront

### 4.1 Build React Application

First, update the frontend to use environment variables for API URL.

**File**: `frontend/.env.production` (create this file manually, it's gitignored)

```
REACT_APP_API_URL=https://YOUR_EB_URL/api
```

**Note**: The file `frontend/src/utils/axiosConfig.js` has already been created. You now need to update all frontend files to use `api` from this config instead of direct `axios` calls. See `UPDATE_FRONTEND_API.md` for detailed instructions.

### 4.2 Update Frontend API Calls

**Important**: The `frontend/src/utils/axiosConfig.js` file has already been created. You now need to:

1. Update all frontend files to import and use `api` from `axiosConfig.js` instead of direct `axios` calls
2. Replace `axios.get('http://localhost:8080/api/...')` with `api.get('/...')` (note: remove `/api` prefix as it's in baseURL)
3. See `UPDATE_FRONTEND_API.md` for detailed step-by-step instructions and examples

### 4.3 Build Frontend

```bash
cd frontend
npm install
npm run build
```

This creates the `frontend/build/` directory.

### 4.4 Create S3 Bucket

1. Go to AWS Console → S3 → Create bucket
2. Configure:
   - **Bucket name**: `campusconnect-frontend` (must be globally unique)
   - **Region**: Same as your backend
   - **Block Public Access**: Uncheck (we'll use CloudFront)
   - **Bucket Versioning**: Optional
3. Click **Create bucket**

### 4.5 Upload Build Files

**Option A: Using AWS Console**
1. Go to your S3 bucket
2. Click **Upload**
3. Select all files from `frontend/build/`
4. Click **Upload**

**Option B: Using AWS CLI**
```bash
aws s3 sync frontend/build/ s3://campusconnect-frontend --delete
```

### 4.6 Configure S3 Bucket for Static Website Hosting

1. Go to your S3 bucket → Properties
2. Scroll to **Static website hosting**
3. Click **Edit**
4. Enable static website hosting
5. Index document: `index.html`
6. Error document: `index.html` (for React Router)
7. Save

### 4.7 Create CloudFront Distribution

1. Go to AWS Console → CloudFront → Create distribution
2. Configure:
   - **Origin domain**: Select your S3 bucket
   - **Origin access**: Select "S3 bucket" (not website endpoint)
   - **Viewer protocol policy**: Redirect HTTP to HTTPS
   - **Allowed HTTP methods**: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE
   - **Cache policy**: CachingDisabled (or create custom)
   - **Default root object**: `index.html`
3. Click **Create distribution**

**Note**: Distribution creation takes 10-15 minutes.

### 4.8 Configure CloudFront Error Pages

1. Go to your CloudFront distribution → Error pages
2. Create custom error response:
   - HTTP error code: `403`
   - Response page path: `/index.html`
   - HTTP response code: `200`
3. Repeat for `404` error

This ensures React Router works correctly.

### 4.9 Update Frontend Environment Variable

Update `frontend/.env.production` with your actual backend URL:

```
REACT_APP_API_URL=https://YOUR_EB_URL/api
```

Rebuild and re-upload:
```bash
cd frontend
npm run build
aws s3 sync build/ s3://campusconnect-frontend --delete
```

## Step 5: Configure Security

### 5.1 Restrict RDS Security Group

1. Go to EC2 → Security Groups
2. Find your RDS security group
3. Edit inbound rules:
   - Remove `0.0.0.0/0`
   - Add rule allowing only from your Elastic Beanstalk security group

### 5.2 Update JWT Secret

Ensure your JWT secret in `application-prod.properties` is a strong, random string (at least 32 characters).

### 5.3 Enable HTTPS

Elastic Beanstalk and CloudFront both support HTTPS. Consider:
- Using AWS Certificate Manager (ACM) for SSL certificates
- Configuring custom domains

## Step 6: Database Migration

### 6.1 Connect to RDS and Create Database

Using psql or pgAdmin:
```sql
CREATE DATABASE campusconnect;
```

### 6.2 Run Application

The first time your Spring Boot app connects, it will create tables automatically (due to `spring.jpa.hibernate.ddl-auto=update`).

Alternatively, you can:
1. Export schema from local database
2. Import to RDS

## Step 7: Testing

1. **Test Backend**: 
   - Visit: `https://YOUR_EB_URL/api/health` (if you have a health endpoint)
   - Or test with Postman/curl

2. **Test Frontend**:
   - Visit your CloudFront distribution URL
   - Verify API calls work

3. **Test Database**:
   - Verify data persists
   - Check RDS monitoring

## Step 8: Monitoring and Maintenance

### 8.1 Set Up CloudWatch Alarms

- Monitor RDS CPU, memory, connections
- Monitor Elastic Beanstalk health
- Monitor CloudFront requests

### 8.2 Set Up Logging

- Elastic Beanstalk logs are available in CloudWatch
- RDS logs are available in CloudWatch Logs

### 8.3 Backup Strategy

- Enable automated RDS snapshots
- Configure backup retention period

## Cost Estimation (Approximate)

- **RDS (db.t3.micro)**: ~$15/month (free tier eligible for first year)
- **Elastic Beanstalk (t3.small)**: ~$15/month
- **S3 Storage**: ~$0.023/GB/month (minimal for static site)
- **CloudFront**: ~$0.085/GB data transfer (first 10TB)
- **Total**: ~$30-50/month (after free tier)

## Troubleshooting

### Backend Issues

- **Check logs**: `eb logs`
- **Check environment**: `eb health`
- **Verify database connection**: Check RDS security group

### Frontend Issues

- **Clear CloudFront cache**: Distribution → Invalidations → Create invalidation → `/*`
- **Check S3 bucket permissions**
- **Verify environment variables**: Check `.env.production`

### Database Issues

- **Check RDS security group**: Ensure backend can connect
- **Verify endpoint**: Check RDS console for correct endpoint
- **Check credentials**: Verify username/password

## Next Steps

1. Set up custom domains
2. Configure CI/CD pipeline (CodePipeline, GitHub Actions)
3. Set up monitoring and alerting
4. Implement automated backups
5. Scale resources as needed

## Alternative Deployment Options

### Backend Alternatives:
- **AWS ECS/Fargate**: Containerized deployment
- **AWS EC2**: Direct EC2 instance
- **AWS Lambda**: Serverless (requires refactoring)

### Frontend Alternatives:
- **AWS Amplify**: Simplified React deployment
- **AWS App Runner**: Containerized frontend

## Support

For issues:
1. Check AWS CloudWatch logs
2. Review Elastic Beanstalk health dashboard
3. Check RDS monitoring
4. Review CloudFront access logs

