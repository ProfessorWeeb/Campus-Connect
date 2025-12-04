# Generating a Secure JWT Secret

For production deployment, you need a strong, random JWT secret (at least 32 characters).

## Option 1: Using OpenSSL (Recommended)

**Windows (Git Bash or WSL):**
```bash
openssl rand -base64 32
```

**Mac/Linux:**
```bash
openssl rand -base64 32
```

## Option 2: Using Node.js

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

## Option 3: Using Python

```bash
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

## Option 4: Online Generator

Use a secure random string generator (ensure it's from a trusted source):
- https://www.random.org/strings/
- Generate at least 32 characters

## Usage

Copy the generated secret and paste it into:
- `backend/src/main/resources/application-prod.properties`
- Replace `YOUR_SECURE_RANDOM_SECRET_KEY_MIN_32_CHARACTERS` with your generated secret

## Security Notes

- **Never commit secrets to version control**
- Store secrets in environment variables or AWS Secrets Manager for production
- Use different secrets for different environments (dev, staging, prod)
- Rotate secrets periodically

