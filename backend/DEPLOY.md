# Power Letter Backend Deployment Guide

## Prerequisites

- Google Cloud account with billing enabled
- `gcloud` CLI installed and authenticated
- Anthropic API key

---

## 1. Secret Manager Setup

### Create the secret

```bash
# Set your project
export PROJECT_ID="your-gcp-project-id"
gcloud config set project $PROJECT_ID

# Enable Secret Manager API
gcloud services enable secretmanager.googleapis.com

# Create the secret
echo -n "sk-ant-your-anthropic-api-key" | \
  gcloud secrets create anthropic-api-key \
  --replication-policy="automatic" \
  --data-file=-
```

### Verify the secret

```bash
gcloud secrets versions access latest --secret="anthropic-api-key"
```

---

## 2. Cloud Run Setup

### Enable required APIs

```bash
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com
```

### Create Artifact Registry repository (optional, for custom builds)

```bash
gcloud artifacts repositories create power-letter \
  --repository-format=docker \
  --location=us-central1 \
  --description="Power Letter container images"
```

---

## 3. Deploy to Cloud Run

### Option A: Deploy from source (recommended)

```bash
cd backend

gcloud run deploy power-letter-api \
  --source . \
  --region us-central1 \
  --platform managed \
  --allow-unauthenticated \
  --set-env-vars "ANTHROPIC_SECRET_NAME=anthropic-api-key" \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --concurrency 80 \
  --timeout 60
```

### Option B: Build and deploy manually

```bash
# Build container
gcloud builds submit --tag gcr.io/$PROJECT_ID/power-letter-api

# Deploy
gcloud run deploy power-letter-api \
  --image gcr.io/$PROJECT_ID/power-letter-api \
  --region us-central1 \
  --platform managed \
  --allow-unauthenticated \
  --set-env-vars "ANTHROPIC_SECRET_NAME=anthropic-api-key" \
  --memory 512Mi
```

---

## 4. Grant Secret Access

```bash
# Get the service account used by Cloud Run
SERVICE_ACCOUNT=$(gcloud run services describe power-letter-api \
  --region us-central1 \
  --format 'value(spec.template.spec.serviceAccountName)')

# If empty, it uses the default compute service account
if [ -z "$SERVICE_ACCOUNT" ]; then
  SERVICE_ACCOUNT="$(gcloud projects describe $PROJECT_ID --format='value(projectNumber)')-compute@developer.gserviceaccount.com"
fi

# Grant access to the secret
gcloud secrets add-iam-policy-binding anthropic-api-key \
  --member="serviceAccount:$SERVICE_ACCOUNT" \
  --role="roles/secretmanager.secretAccessor"
```

---

## 5. Get Service URL

```bash
gcloud run services describe power-letter-api \
  --region us-central1 \
  --format 'value(status.url)'
```

---

## 6. Test the Deployment

### Health check

```bash
SERVICE_URL=$(gcloud run services describe power-letter-api --region us-central1 --format 'value(status.url)')

curl $SERVICE_URL/health
```

### Generate a letter

```bash
curl -X POST "$SERVICE_URL/generateLetter" \
  -H "Content-Type: application/json" \
  -d '{
    "letterType": "GYM",
    "companyName": "FitLife Gym",
    "issueDescription": "I cancelled my membership on January 5, 2025, but I was charged $49.99 on February 1 despite the cancellation. I have email confirmation of my cancellation request.",
    "amount": "49.99",
    "transactionDate": "February 1, 2025",
    "accountOrOrderNumber": "MEM-2024-78432"
  }'
```

---

## Local Development

### Install dependencies

```bash
cd backend
npm install
```

### Set environment variable

```bash
export ANTHROPIC_API_KEY="sk-ant-your-key"
```

### Run locally

```bash
npm run dev
```

### Test locally

```bash
curl -X POST "http://localhost:8080/generateLetter" \
  -H "Content-Type: application/json" \
  -d '{
    "letterType": "TELECOM",
    "companyName": "Bell Canada",
    "issueDescription": "I was charged $75 for services I did not authorize. My plan is $50/month but my bill shows $125.",
    "amount": "75.00",
    "transactionDate": "January 15, 2025",
    "accountOrOrderNumber": "ACC-9876543"
  }'
```

---

## Monitoring

### View logs

```bash
gcloud run services logs read power-letter-api --region us-central1 --limit 50
```

### Stream logs

```bash
gcloud run services logs tail power-letter-api --region us-central1
```

---

## Cost Estimate

| Component | Estimate |
|-----------|----------|
| Cloud Run | ~$0.00 (free tier: 2M requests/month) |
| Secret Manager | ~$0.00 (free tier: 6 active secrets) |
| Anthropic API | ~$0.003-0.015 per letter (Claude Sonnet) |

---

## Security Checklist

- [x] API key stored in Secret Manager (not in code)
- [x] Rate limiting enabled (10 req/min per IP)
- [x] Input validation on all fields
- [x] Response validation before returning
- [x] No user content logged
- [x] Helmet.js security headers
- [x] Request body size limited to 10KB
- [ ] Add Cloud Armor for DDoS protection (optional)
- [ ] Add Firebase App Check for app verification (optional)
