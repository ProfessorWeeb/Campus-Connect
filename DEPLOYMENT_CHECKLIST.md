# AWS Deployment Checklist

Use this checklist to ensure all steps are completed for deploying CampusConnect to AWS.

## Pre-Deployment

- [ ] AWS Account created and configured
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] EB CLI installed (`pip install awsebcli`)
- [ ] Maven installed (for backend build)
- [ ] Node.js and npm installed (for frontend build)

## Database Setup (RDS)

- [ ] RDS PostgreSQL instance created
- [ ] Database name set to `campusconnect`
- [ ] Master username and password saved securely
- [ ] RDS endpoint copied
- [ ] Security group configured to allow backend access
- [ ] Database connection tested

## Backend Configuration

- [ ] `application-prod.properties` created and configured
- [ ] RDS endpoint updated in `application-prod.properties`
- [ ] RDS password updated in `application-prod.properties`
- [ ] JWT secret changed to secure random string (32+ characters)
- [ ] CORS origins updated in `application-prod.properties`
- [ ] `.ebextensions/environment.config` created
- [ ] `Procfile` created
- [ ] Backend JAR built successfully (`mvnw clean package`)
- [ ] Elastic Beanstalk application initialized (`eb init`)
- [ ] Elastic Beanstalk environment created (`eb create`)
- [ ] Environment variables set (`SPRING_PROFILES_ACTIVE=prod`)
- [ ] Backend URL obtained and saved
- [ ] Backend health check passed

## Frontend Configuration

- [ ] `src/utils/axiosConfig.js` created
- [ ] All API calls updated to use `api` from `axiosConfig.js`
- [ ] `.env.production` created with backend URL
- [ ] `.env.development` created for local development
- [ ] Frontend built successfully (`npm run build`)
- [ ] S3 bucket created for frontend
- [ ] Build files uploaded to S3
- [ ] S3 bucket configured for static website hosting
- [ ] CloudFront distribution created
- [ ] CloudFront error pages configured (403, 404 → index.html)
- [ ] CloudFront distribution URL obtained
- [ ] CORS updated in backend with CloudFront URL
- [ ] Frontend rebuilt and re-uploaded after CORS update

## Security

- [ ] RDS security group restricted (only backend can access)
- [ ] JWT secret is strong and random
- [ ] HTTPS enabled (CloudFront and Elastic Beanstalk)
- [ ] Environment variables secured (not in version control)
- [ ] Database credentials secured

## Testing

- [ ] Backend API accessible via Elastic Beanstalk URL
- [ ] Frontend accessible via CloudFront URL
- [ ] User registration works
- [ ] User login works
- [ ] Database operations work (create groups, send messages, etc.)
- [ ] CORS working correctly
- [ ] Authentication working correctly

## Monitoring & Maintenance

- [ ] CloudWatch alarms configured
- [ ] RDS automated backups enabled
- [ ] Logging configured and accessible
- [ ] Health checks configured

## Post-Deployment

- [ ] Custom domain configured (optional)
- [ ] SSL certificate configured (optional)
- [ ] CI/CD pipeline set up (optional)
- [ ] Documentation updated with production URLs
- [ ] Team notified of deployment

## Rollback Plan

- [ ] Previous version backup available
- [ ] Rollback procedure documented
- [ ] Database backup available

## Notes

- Keep all credentials secure
- Document all URLs and endpoints
- Monitor costs in AWS Billing Dashboard
- Set up billing alerts

