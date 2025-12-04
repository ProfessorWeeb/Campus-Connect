# Deploy Backend to Elastic Beanstalk via AWS Console

Since we're having Java 25 compatibility issues building locally, we'll deploy the source code and let Elastic Beanstalk build it using Java 17.

## Step 1: Create Application Archive

Even though we can't build the JAR locally, we can create a ZIP file with the source code and Elastic Beanstalk will build it.

### Option A: Create ZIP manually

1. Navigate to the `backend` folder
2. Select all files EXCEPT:
   - `target/` folder (if it exists)
   - `.git/` folder (if it exists)
   - Any IDE files
3. Right-click → Send to → Compressed (zipped) folder
4. Name it `campusconnect-backend.zip`

### Option B: Use command line

```cmd
cd backend
powershell Compress-Archive -Path pom.xml,src,mvnw,mvnw.cmd,.ebextensions,Procfile -DestinationPath campusconnect-backend.zip -Force
```

## Step 2: Deploy via AWS Console

1. **Go to AWS Console** → Elastic Beanstalk

2. **Click "Create Application"**

3. **Configure Application**:
   - Application name: `campusconnect`
   - Description: `Campus Connect Backend` (optional)

4. **Configure Environment**:
   - Environment name: `campusconnect-env`
   - Domain: Leave default (or choose custom)
   - Platform: **Java**
   - Platform branch: **Java 17 running on 64bit Amazon Linux 2**
   - Platform version: Latest
   - Application code: **Upload your code**
     - Click "Choose file"
     - Select `campusconnect-backend.zip`
     - Click "Upload"

5. **Configure Service Access**:
   - Service role: Create new (or use existing)
   - EC2 key pair: Optional (for SSH access)
   - EC2 instance profile: Create new (or use existing)

6. **Set Up Networking, Database, and Tags**:
   - VPC: Default VPC
   - Instance subnets: Default
   - Load balancer: Application Load Balancer (default)
   - Database: None (we're using RDS separately)

7. **Configure Instance Traffic and Scaling**:
   - Instance type: `t3.small` or `t3.micro` (free tier eligible)
   - Capacity: 1 instance (for now)

8. **Configure Updates, Monitoring, and Logging**:
   - Rolling updates: Default
   - Health reporting: Enhanced
   - Logging: Enable (recommended)

9. **Review and Submit**:
   - Review all settings
   - Click "Submit"

## Step 3: Configure Environment Variables

After the environment is created:

1. Go to your environment → Configuration
2. Click "Edit" on "Software"
3. Under "Environment properties", add:
   - Key: `SPRING_PROFILES_ACTIVE`
   - Value: `prod`
4. Click "Apply"

## Step 4: Wait for Deployment

- This takes 5-10 minutes
- Watch the events log for progress
- Status will change from "Launching" to "Ok" when ready

## Step 5: Get Your Backend URL

1. Once deployment is complete, go to your environment
2. The URL will be displayed at the top (e.g., `campusconnect-env.us-east-2.elasticbeanstalk.com`)
3. **Save this URL** - you'll need it for the frontend!

## Step 6: Test the Backend

Try accessing:
- `http://YOUR_EB_URL/api/health` (if you have a health endpoint)
- Or test with Postman/curl

## Troubleshooting

### If deployment fails:

1. **Check Logs**:
   - Go to your environment → Logs
   - Request logs to see build errors

2. **Common Issues**:
   - **Build timeout**: Increase timeout in configuration
   - **Out of memory**: Increase instance size
   - **Database connection**: Check security group allows EB to connect to RDS

3. **View Full Logs**:
   - Go to Logs → Request full logs
   - Download and check for errors

### If you need to update:

1. Make changes to your code
2. Create a new ZIP file
3. Go to your environment → Upload and deploy
4. Select the new ZIP file
5. Deploy

## Alternative: Fix Java 25 Issue

If you want to build locally first, you could:

1. Install Java 17 alongside Java 25
2. Set JAVA_HOME to Java 17 for Maven builds
3. Or use a Docker container with Java 17

But deploying source code to EB is often easier and lets AWS handle the build environment!

