# Options for Setting Up RDS Database

You have three options to create your RDS database:

## Option 1: AWS Console (Easiest - No CLI needed)

**Best for**: First-time users, visual learners

1. Go to [AWS Console](https://console.aws.amazon.com)
2. Navigate to RDS → Databases → Create database
3. Follow the steps in `AWS_DEPLOYMENT.md` Section 1.1
4. Takes about 5-10 minutes

**Pros**: Visual, no CLI needed, easy to see all options
**Cons**: Manual, can't automate

---

## Option 2: AWS CLI Script (Automated)

**Best for**: Users with AWS CLI installed, want automation

### Prerequisites:
- AWS CLI installed: https://aws.amazon.com/cli/
- AWS credentials configured: `aws configure`

### Windows:
```bash
create-rds-database.bat
```

### Mac/Linux:
```bash
chmod +x create-rds-simple.sh
./create-rds-simple.sh
```

The script will:
- Prompt for password and username
- Create the RDS instance
- Wait for it to be available
- Display the endpoint information

**Pros**: Automated, saves endpoint info
**Cons**: Requires AWS CLI installation

---

## Option 3: CloudFormation Template (Infrastructure as Code)

**Best for**: Advanced users, want version control, repeatable deployments

### Prerequisites:
- AWS CLI installed and configured

### Deploy:
```bash
aws cloudformation create-stack \
    --stack-name campusconnect-rds \
    --template-body file://aws-rds-setup.yaml \
    --parameters \
        ParameterKey=MasterUserPassword,ParameterValue=YOUR_PASSWORD \
        ParameterKey=MasterUsername,ParameterValue=postgres \
    --region us-east-1
```

### Check status:
```bash
aws cloudformation describe-stacks --stack-name campusconnect-rds
```

### Get outputs (endpoint):
```bash
aws cloudformation describe-stacks \
    --stack-name campusconnect-rds \
    --query "Stacks[0].Outputs"
```

**Pros**: Version controlled, repeatable, professional
**Cons**: More complex, requires understanding of CloudFormation

---

## Recommendation

- **First time**: Use **Option 1** (AWS Console) - it's the easiest and you'll learn the interface
- **If you have AWS CLI**: Use **Option 2** (Script) - it's faster and automated
- **For production/team**: Use **Option 3** (CloudFormation) - best practices

---

## After Database Creation

Regardless of which option you choose, you'll need to:

1. **Save the endpoint** (e.g., `campusconnect-db.xxxxx.us-east-1.rds.amazonaws.com`)
2. **Save the password** you set
3. **Update** `backend/src/main/resources/application-prod.properties`:
   - Replace `YOUR_RDS_ENDPOINT` with your endpoint
   - Replace `YOUR_RDS_PASSWORD` with your password
4. **Configure security group** (see `AWS_DEPLOYMENT.md` Section 1.2)

---

## Troubleshooting

### "AWS CLI not found"
- Install AWS CLI: https://aws.amazon.com/cli/
- Windows: Download MSI installer
- Mac: `brew install awscli`
- Linux: `pip install awscli`

### "Credentials not configured"
- Run: `aws configure`
- Enter your AWS Access Key ID
- Enter your AWS Secret Access Key
- Enter default region (e.g., `us-east-1`)
- Enter output format: `json`

### "Insufficient permissions"
- Your AWS user needs `rds:*` permissions
- Or use an admin account for initial setup

