import express from 'express';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import Anthropic from '@anthropic-ai/sdk';
import { SecretManagerServiceClient } from '@google-cloud/secret-manager';

const app = express();
const PORT = process.env.PORT || 8080;

// Security middleware
app.use(helmet());
app.use(express.json({ limit: '10kb' }));

// Rate limiting: 10 requests per minute per IP
const limiter = rateLimit({
  windowMs: 60 * 1000,
  max: 10,
  message: { error: 'Too many requests, please try again later' },
  standardHeaders: true,
  legacyHeaders: false,
});
app.use('/generateLetter', limiter);

// Cache for API key
let anthropicClient = null;

// Allowed legal basis values
const ALLOWED_LEGAL_BASIS = [
  'Consumer Protection Act, 2002 (Ontario)',
  'Sale of Goods Act (Ontario)',
  'Air Passenger Protection Regulations (Canada)',
  'Wireless Code (CRTC)',
  'Internet Code (CRTC)',
  'Competition Act (Canada)',
  'Electronic Commerce Act, 2000 (Ontario)',
];

const ALLOWED_LETTER_TYPES = ['GYM', 'TELECOM', 'SUBSCRIPTION', 'AIRLINE'];
const ALLOWED_TONES = ['professional', 'firm'];

// System prompt
const SYSTEM_PROMPT = `You are a professional consumer advocacy assistant helping Ontario, Canada residents draft refund and complaint emails.

## IMPORTANT WRITING RULES (MUST FOLLOW)
- Never threaten lawsuits, fines, or enforcement
- Never cite specific legal sections or claim legal authority
- Use phrases like "may seek further assistance" instead of "will escalate"
- Keep tone cooperative, professional, and calm
- If legal certainty is unclear, say "under applicable consumer protections"
- This is not legal advice
- Always follow these rules when generating letters

## Your Role
- Draft clear, professional emails requesting refunds, cancellations, or billing corrections
- Help consumers express their concerns clearly and professionally
- Maintain a cooperative, respectful tone throughout
- Help consumers communicate their needs without being aggressive or threatening

## Consumer Rights Awareness
You may reference general consumer protections when relevant, but:
- Do NOT cite specific section numbers or legal provisions
- Do NOT claim legal authority or expertise
- Use soft language like "As an Ontario consumer, I understand I have certain protections regarding..."

General areas of consumer protection (reference softly, not as legal claims):
- Unfair business practices protections
- Contract cancellation rights
- Refunds for undelivered goods/services
- Billing dispute resolution
- Air passenger protections for flight issues
- Telecommunications service standards

When uncertain, use: "under applicable consumer protections" or "As an Ontario consumer, I'm entitled to fair treatment regarding..."

### Prohibited Content
- NEVER threaten lawsuits, legal action, or regulatory complaints as threats
- NEVER cite specific law names, section numbers, or legal provisions
- NEVER guarantee outcomes or promise the recipient must comply
- NEVER use aggressive, hostile, or demanding language
- NEVER set hard deadlines like "15 business days or else"
- NEVER include false statements or exaggerate claims
- NEVER impersonate a lawyer or claim legal expertise

### Tone Guidelines
- Cooperative and solution-focused
- Calm and reasonable
- Professional and business-like
- Clear and direct
- Assume good faith
- Express preference for working together to resolve the issue

### Escalation Language (use soft alternatives)
Instead of: "I will escalate to authorities"
Use: "I may explore other options available to me, including consumer assistance resources"

Instead of: "You must respond within 15 days"
Use: "I would appreciate a response at your earliest convenience"

Instead of: "Under Section 42 of the Consumer Protection Act"
Use: "As an Ontario consumer, I understand I have certain protections"

### Subject Line Guidelines
Use professional, non-aggressive subject lines. Choose from these formats:
- "Request for Refund – $[Amount] – [Reference Number]"
- "Billing Inquiry: $[Amount] Charge on [Date]"
- "Refund Request for [Service/Product] – Account [Number]"

NEVER use these words in subject lines:
- "Formal Complaint"
- "Legal Notice"
- "Demand"
- "Urgent"
- "Final Notice"
- "Dispute"

Use these safer alternatives:
- "Request" instead of "Demand"
- "Inquiry" instead of "Complaint"
- "Follow-up" instead of "Final Notice"

## Output Format
You must respond with ONLY valid JSON matching this exact schema:

{
  "subject": "string (10-100 characters, concise email subject line)",
  "emailBody": "string (200-3000 characters, full email text with newlines)",
  "legalBasis": ["array of applicable law names from the allowed list above"],
  "tone": "professional" or "firm"
}

Rules for each field:
- subject: Brief, professional subject line using safe formats like "Request for Refund – $[Amount]" or "Billing Inquiry: $[Amount] Charge". NEVER use "Formal Complaint", "Legal Notice", "Demand", "Urgent", or "Dispute". Include reference number if provided.
- emailBody: Complete email ready to send. Use [Your Name], [Your Address], [Your Email], [Your Phone] as placeholders.
- legalBasis: Use empty array [] - we no longer cite specific laws.
- tone: Use "professional" for all letters. Keep tone cooperative and solution-focused.

Respond with ONLY the JSON object. No additional text, markdown, or explanation.`;

// JSON repair prompt
const JSON_REPAIR_PROMPT = `The previous response was not valid JSON. Please respond with ONLY a valid JSON object matching this schema:

{
  "subject": "string",
  "emailBody": "string",
  "legalBasis": ["string"],
  "tone": "professional" or "firm"
}

No markdown code blocks. No explanation. Just the raw JSON object.`;

/**
 * Fetch Anthropic API key from Google Cloud Secret Manager
 */
async function getAnthropicClient() {
  if (anthropicClient) {
    return anthropicClient;
  }

  const projectId = process.env.GOOGLE_CLOUD_PROJECT || process.env.GCP_PROJECT;
  const secretName = process.env.ANTHROPIC_SECRET_NAME || 'anthropic-api-key';

  if (process.env.ANTHROPIC_API_KEY) {
    // Local development fallback
    anthropicClient = new Anthropic({ apiKey: process.env.ANTHROPIC_API_KEY });
    return anthropicClient;
  }

  const client = new SecretManagerServiceClient();
  const name = `projects/${projectId}/secrets/${secretName}/versions/latest`;

  const [version] = await client.accessSecretVersion({ name });
  const apiKey = version.payload.data.toString('utf8');

  anthropicClient = new Anthropic({ apiKey });
  return anthropicClient;
}

/**
 * Validate request input
 */
function validateInput(body) {
  const errors = [];

  if (!body.letterType || !ALLOWED_LETTER_TYPES.includes(body.letterType)) {
    errors.push('letterType must be one of: ' + ALLOWED_LETTER_TYPES.join(', '));
  }

  if (!body.companyName || typeof body.companyName !== 'string') {
    errors.push('companyName is required');
  } else if (body.companyName.length > 200) {
    errors.push('companyName must be 200 characters or less');
  }

  if (!body.issueDescription || typeof body.issueDescription !== 'string') {
    errors.push('issueDescription is required');
  } else if (body.issueDescription.length > 1000) {
    errors.push('issueDescription must be 1000 characters or less');
  }

  if (!body.amount || typeof body.amount !== 'string') {
    errors.push('amount is required');
  } else if (!/^\d+(\.\d{1,2})?$/.test(body.amount)) {
    errors.push('amount must be a valid number (e.g., 150.00)');
  }

  if (!body.transactionDate || typeof body.transactionDate !== 'string') {
    errors.push('transactionDate is required');
  } else if (body.transactionDate.length > 100) {
    errors.push('transactionDate must be 100 characters or less');
  }

  if (body.accountOrOrderNumber && body.accountOrOrderNumber.length > 100) {
    errors.push('accountOrOrderNumber must be 100 characters or less');
  }

  return errors;
}

/**
 * Validate the generated letter response
 */
function validateResponse(response) {
  if (!response || typeof response !== 'object') {
    return { valid: false, error: 'Response is not an object' };
  }

  if (typeof response.subject !== 'string' || response.subject.length < 10 || response.subject.length > 100) {
    return { valid: false, error: 'Invalid subject length' };
  }

  if (typeof response.emailBody !== 'string' || response.emailBody.length < 200 || response.emailBody.length > 3000) {
    return { valid: false, error: 'Invalid emailBody length' };
  }

  if (!Array.isArray(response.legalBasis)) {
    return { valid: false, error: 'legalBasis must be an array' };
  }

  if (response.legalBasis.length > 5) {
    return { valid: false, error: 'legalBasis must have 5 or fewer items' };
  }

  for (const basis of response.legalBasis) {
    if (!ALLOWED_LEGAL_BASIS.includes(basis)) {
      return { valid: false, error: `Invalid legalBasis: ${basis}` };
    }
  }

  if (!ALLOWED_TONES.includes(response.tone)) {
    return { valid: false, error: 'tone must be professional or firm' };
  }

  return { valid: true };
}

/**
 * Build user prompt from request
 */
function buildUserPrompt(body) {
  const letterTypeContextMap = {
    GYM: 'gym or fitness membership refund/cancellation',
    TELECOM: 'telecommunications billing dispute or overcharge',
    SUBSCRIPTION: 'subscription service cancellation and refund',
    AIRLINE: 'airline delay, cancellation, or compensation',
  };

  const letterTypeDisplayMap = {
    GYM: 'Gym Refund',
    TELECOM: 'Telecom Overcharge',
    SUBSCRIPTION: 'Subscription Cancel',
    AIRLINE: 'Airline Compensation',
  };

  const context = letterTypeContextMap[body.letterType] || 'consumer complaint';
  const displayName = letterTypeDisplayMap[body.letterType] || body.letterType;

  return `Generate a ${context} email with these details:

Letter Type: ${displayName}
Company Name: ${body.companyName}
Issue Description: ${body.issueDescription}
Amount in Dispute: $${body.amount} CAD
Date of Incident: ${body.transactionDate}
Account/Order/Reference Number: ${body.accountOrOrderNumber || 'Not provided'}

Requirements:
1. Request a refund/resolution for the amount specified
2. Ask for a response "at your earliest convenience" (no hard deadlines)
3. Express preference for resolving the matter cooperatively
4. Mention the consumer "may explore other options" if needed (soft, not threatening)
5. Keep the email between 250-500 words
6. Be specific about what resolution is requested
7. Include a brief note that this is a communication tool, not legal advice

Generate the JSON response now.`;
}

/**
 * Parse JSON from Claude response, handling markdown code blocks
 */
function parseJsonResponse(text) {
  let cleaned = text.trim();

  // Remove markdown code blocks if present
  if (cleaned.startsWith('```json')) {
    cleaned = cleaned.slice(7);
  } else if (cleaned.startsWith('```')) {
    cleaned = cleaned.slice(3);
  }

  if (cleaned.endsWith('```')) {
    cleaned = cleaned.slice(0, -3);
  }

  cleaned = cleaned.trim();

  return JSON.parse(cleaned);
}

/**
 * Call Anthropic API
 */
async function callAnthropic(client, userPrompt, isRetry = false) {
  const messages = [{ role: 'user', content: userPrompt }];

  if (isRetry) {
    messages.push({ role: 'assistant', content: 'I apologize for the formatting error.' });
    messages.push({ role: 'user', content: JSON_REPAIR_PROMPT });
  }

  const response = await client.messages.create({
    model: 'claude-sonnet-4-20250514',
    max_tokens: 2048,
    system: SYSTEM_PROMPT,
    messages,
  });

  const text = response.content[0]?.text;
  if (!text) {
    throw new Error('Empty response from Anthropic');
  }

  return parseJsonResponse(text);
}

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// Main endpoint
app.post('/generateLetter', async (req, res) => {
  const requestId = Math.random().toString(36).substring(7);
  console.log(`[${requestId}] POST /generateLetter - letterType: ${req.body?.letterType}`);

  try {
    // Validate input
    const validationErrors = validateInput(req.body);
    if (validationErrors.length > 0) {
      console.log(`[${requestId}] Validation failed`);
      return res.status(400).json({ error: 'Validation failed', details: validationErrors });
    }

    // Get Anthropic client
    const client = await getAnthropicClient();

    // Build prompt
    const userPrompt = buildUserPrompt(req.body);

    // First attempt
    let response;
    let retried = false;

    try {
      response = await callAnthropic(client, userPrompt, false);
    } catch (parseError) {
      // Retry with JSON repair prompt
      console.log(`[${requestId}] First attempt failed, retrying with repair prompt`);
      retried = true;
      response = await callAnthropic(client, userPrompt, true);
    }

    // Validate response
    const validation = validateResponse(response);
    if (!validation.valid) {
      if (!retried) {
        // One more retry
        console.log(`[${requestId}] Validation failed, retrying`);
        response = await callAnthropic(client, userPrompt, true);
        const revalidation = validateResponse(response);
        if (!revalidation.valid) {
          console.log(`[${requestId}] Retry validation failed`);
          return res.status(500).json({ error: 'Failed to generate valid letter' });
        }
      } else {
        console.log(`[${requestId}] Already retried, validation still failed`);
        return res.status(500).json({ error: 'Failed to generate valid letter' });
      }
    }

    console.log(`[${requestId}] Success - tone: ${response.tone}, legalBasis count: ${response.legalBasis.length}`);
    res.json(response);
  } catch (error) {
    console.error(`[${requestId}] Error:`, error.message);

    if (error.status === 429) {
      return res.status(429).json({ error: 'Service temporarily unavailable, please try again' });
    }

    res.status(500).json({ error: 'Failed to generate letter' });
  }
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err.message);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(PORT, () => {
  console.log(`Power Letter API running on port ${PORT}`);
});
