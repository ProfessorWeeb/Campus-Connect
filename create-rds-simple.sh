#!/bin/bash
# Simple script to create RDS PostgreSQL database using AWS CLI

echo "========================================"
echo "CampusConnect RDS Database Setup"
echo "========================================"
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "ERROR: AWS CLI is not installed"
    echo "Please install AWS CLI: https://aws.amazon.com/cli/"
    exit 1
fi

# Get user input
read -sp "Enter master database password (min 8 chars): " DB_PASSWORD
echo ""
read -p "Enter master username [postgres]: " DB_USERNAME
DB_USERNAME=${DB_USERNAME:-postgres}

read -p "Enter AWS region [us-east-1]: " REGION
REGION=${REGION:-us-east-1}

echo ""
echo "Creating RDS database instance..."
echo "This will take 5-10 minutes..."
echo ""

# Create RDS instance
aws rds create-db-instance \
    --db-instance-identifier campusconnect-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 16.1 \
    --master-username "$DB_USERNAME" \
    --master-user-password "$DB_PASSWORD" \
    --allocated-storage 20 \
    --storage-type gp2 \
    --db-name campusconnect \
    --publicly-accessible \
    --backup-retention-period 7 \
    --storage-encrypted \
    --region "$REGION"

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Failed to create RDS instance"
    echo "Check your AWS credentials and permissions"
    exit 1
fi

echo ""
echo "========================================"
echo "Database creation initiated!"
echo "========================================"
echo ""
echo "Waiting for database to be available..."
echo "This may take 5-10 minutes..."
echo ""

# Wait for database to be available
aws rds wait db-instance-available \
    --db-instance-identifier campusconnect-db \
    --region "$REGION"

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Database creation failed or timed out"
    exit 1
fi

echo ""
echo "========================================"
echo "Database is now available!"
echo "========================================"
echo ""

# Get database endpoint
DB_ENDPOINT=$(aws rds describe-db-instances \
    --db-instance-identifier campusconnect-db \
    --region "$REGION" \
    --query "DBInstances[0].Endpoint.Address" \
    --output text)

DB_PORT=$(aws rds describe-db-instances \
    --db-instance-identifier campusconnect-db \
    --region "$REGION" \
    --query "DBInstances[0].Endpoint.Port" \
    --output text)

echo ""
echo "========================================"
echo "Database Information:"
echo "========================================"
echo "Endpoint: $DB_ENDPOINT"
echo "Port: $DB_PORT"
echo "Database Name: campusconnect"
echo "Username: $DB_USERNAME"
echo ""
echo "IMPORTANT: Save this information!"
echo ""
echo "Next steps:"
echo "1. Update backend/src/main/resources/application-prod.properties"
echo "2. Configure security group to allow backend access"
echo ""

