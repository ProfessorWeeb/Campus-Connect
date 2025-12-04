# AWS Deployment Quick Start

This is a condensed version of the full deployment guide. See `AWS_DEPLOYMENT.md` for detailed instructions.

## Prerequisites

```bash
# Install AWS CLI
# Windows: Download from AWS website
# Mac: brew install awscli
# Linux: pip install awscli

# Install EB CLI
pip install awsebcli

# Configure AWS
aws configure
```

## 1. Create RDS Database

1. AWS Console → RDS → Create database
2. PostgreSQL, Free tier
3. Database name: `campusconnect`
4. Save endpoint and password

## 2. Deploy Backend

```bash
cd backend

# Update application-prod.properties with RDS endpoint and password
# Edit: backend/src/main/resources/application-prod.properties

# Build
./mvnw clean package -DskipTests

# Initialize EB
eb init

# Create environment
eb create campusconnect-env

# Get URL
eb status
```

## 3. Deploy Frontend

```bash
cd frontend

# Create .env.production (manually, as it's gitignored)
# REACT_APP_API_URL=https://YOUR_EB_URL/api

# Update all files to use axiosConfig.js instead of direct axios calls
# See UPDATE_FRONTEND_API.md for details

# Build
npm install
npm run build

# Create S3 bucket (via AWS Console or CLI)
aws s3 mb s3://campusconnect-frontend-YOUR-UNIQUE-NAME

# Upload
aws s3 sync build/ s3://campusconnect-frontend-YOUR-UNIQUE-NAME --delete

# Create CloudFront distribution (via AWS Console)
# Origin: S3 bucket
# Error pages: 403, 404 → /index.html (200)
```

## 4. Update Configuration

1. Update `application-prod.properties` CORS with CloudFront URL
2. Rebuild and redeploy backend: `eb deploy`
3. Update `.env.production` with correct backend URL
4. Rebuild and re-upload frontend

## 5. Test

- Backend: `https://YOUR_EB_URL/api/...`
- Frontend: `https://YOUR_CLOUDFRONT_URL`

## Common Commands

```bash
# Backend
eb status          # Check status
eb logs            # View logs
eb deploy          # Redeploy
eb open            # Open in browser

# Frontend
aws s3 sync build/ s3://bucket-name --delete  # Upload
# CloudFront invalidation via AWS Console
```

## Important URLs to Save

- RDS Endpoint: `campusconnect-db.xxxxx.us-east-1.rds.amazonaws.com`
- Backend URL: `http://campusconnect-env.xxxxx.us-east-1.elasticbeanstalk.com`
- Frontend URL: `https://xxxxx.cloudfront.net`

