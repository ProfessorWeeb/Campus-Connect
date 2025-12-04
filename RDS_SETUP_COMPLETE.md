# RDS Database Setup - Status

## ✅ Completed

- RDS PostgreSQL database created
- Endpoint configured in `application-prod.properties`
- Endpoint: `campusconnect-db.c3eyq2k2y3k1.us-east-2.rds.amazonaws.com`
- Port: `5432`
- Region: `us-east-2`

## ⚠️ Action Required

### 1. Set Database Password

You need to update the password in `backend/src/main/resources/application-prod.properties`:

**Line 8**: Replace `YOUR_RDS_PASSWORD` with the master password you set when creating the RDS instance.

```properties
spring.datasource.password=YOUR_ACTUAL_PASSWORD_HERE
```

### 2. Generate JWT Secret

Generate a secure JWT secret (see `GENERATE_JWT_SECRET.md` for instructions):

**Quick command:**
```bash
# Windows (Git Bash or WSL)
openssl rand -base64 32

# Or using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Then update **Line 20** in `application-prod.properties`:
```properties
jwt.secret=YOUR_GENERATED_SECRET_HERE
```

### 3. Configure Security Group

Your RDS database needs to allow connections from your backend. 

**Option A: Allow from anywhere (for testing only - NOT recommended for production)**
1. Go to AWS Console → EC2 → Security Groups
2. Find the security group attached to your RDS instance
3. Edit Inbound Rules
4. Add rule: Type: PostgreSQL, Port: 5432, Source: 0.0.0.0/0

**Option B: Allow only from Elastic Beanstalk (recommended)**
1. After deploying backend to Elastic Beanstalk, get the security group ID
2. Update RDS security group to allow connections from that security group only

### 4. Verify Database Connection

You can test the connection using psql or pgAdmin:

```bash
# Using psql (if installed)
psql -h campusconnect-db.c3eyq2k2y3k1.us-east-2.rds.amazonaws.com -U postgres -d campusconnect
```

Or use pgAdmin:
- Host: `campusconnect-db.c3eyq2k2y3k1.us-east-2.rds.amazonaws.com`
- Port: `5432`
- Database: `campusconnect`
- Username: `postgres`
- Password: (your master password)

## 📝 Current Configuration

**File**: `backend/src/main/resources/application-prod.properties`

```properties
# Database - ✅ CONFIGURED
spring.datasource.url=jdbc:postgresql://campusconnect-db.c3eyq2k2y3k1.us-east-2.rds.amazonaws.com:5432/campusconnect
spring.datasource.username=postgres
spring.datasource.password=YOUR_RDS_PASSWORD  # ⚠️ UPDATE THIS

# JWT - ⚠️ NEEDS UPDATE
jwt.secret=YOUR_SECURE_RANDOM_SECRET_KEY_MIN_32_CHARACTERS  # ⚠️ UPDATE THIS

# CORS - ⚠️ UPDATE LATER (after frontend deployment)
spring.web.cors.allowed-origins=https://YOUR_CLOUDFRONT_DOMAIN,http://localhost:3000
```

## 🚀 Next Steps

1. ✅ Database endpoint configured
2. ⚠️ Set database password in `application-prod.properties`
3. ⚠️ Generate and set JWT secret
4. ⚠️ Configure security group (allow backend access)
5. ⏭️ Deploy backend to Elastic Beanstalk
6. ⏭️ Deploy frontend to S3 + CloudFront
7. ⏭️ Update CORS with frontend URL

## 📌 Important Notes

- **Region**: Your database is in `us-east-2`. Make sure to deploy your backend and frontend in the same region (or at least a nearby region) for better performance.
- **Security**: Once your backend is deployed, restrict the RDS security group to only allow connections from your Elastic Beanstalk security group.
- **Backup**: Your RDS instance has 7-day backup retention enabled (configured during creation).

## 🔍 Troubleshooting

If you can't connect:
1. Check security group rules (port 5432 must be open)
2. Verify password is correct
3. Check if database is in "available" state (not "creating" or "modifying")
4. Verify the endpoint is correct

