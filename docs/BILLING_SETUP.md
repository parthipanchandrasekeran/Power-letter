# Google Play Billing Setup Guide

## Overview

Power Letter uses Google Play Billing for pay-per-letter monetization:
- **Product**: Consumable in-app purchase
- **Price**: $1.99 per letter
- **Free tier**: First letter free (configurable)

---

## 1. Google Play Console Setup

### Create the In-App Product

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app → **Monetize** → **In-app products**
3. Click **Create product**
4. Configure:

| Field | Value |
|-------|-------|
| Product ID | `power_letter_single` |
| Name | Professional Letter |
| Description | Generate one AI-powered complaint letter with Ontario consumer protection references |
| Default price | $1.99 USD |
| Product type | Consumable (managed product) |

5. Click **Save** → **Activate**

### Set Regional Pricing

1. In the product details, click **Manage prices**
2. Set CAD price: $2.79 (or let Google convert)
3. Review other regions as needed
4. Click **Apply**

---

## 2. License Testing Setup

### Add License Testers

1. Go to **Settings** → **License testing**
2. Add your test email addresses
3. These accounts can make purchases without being charged

### Internal Testing Track

1. Go to **Testing** → **Internal testing**
2. Create a release with your signed APK/AAB
3. Add testers via email list
4. Testers get the opt-in link

---

## 3. Android Project Configuration

### build.gradle.kts (app)

```kotlin
dependencies {
    implementation("com.android.billingclient:billing-ktx:6.2.0")
}
```

### Product ID Constant

In `BillingManager.kt`:
```kotlin
companion object {
    const val PRODUCT_ID_LETTER = "power_letter_single"
}
```

**Important**: The product ID must match exactly what you created in Play Console.

---

## 4. Testing Checklist

### Before Testing

- [ ] App is uploaded to internal/closed testing track
- [ ] In-app product is created and **activated**
- [ ] Tester emails are added to license testing
- [ ] Tester has opted into the test track
- [ ] Test device is signed into a tester Google account

### Test Scenarios

| Scenario | Expected Result |
|----------|-----------------|
| First launch | Free letter available |
| Use free letter | Letter generated, free option disappears |
| Purchase letter | Google Play dialog → success → credit added |
| Cancel purchase | Returns to payment screen |
| Use purchased credit | Letter generated, credit decremented |
| Restore purchases | Pending purchases are fulfilled |

### Test Card Numbers (Sandbox)

When using license testers, Google Play provides test payment options:
- **Test card, always approves** - Purchase succeeds
- **Test card, always declines** - Purchase fails
- **Test card, slow** - Purchase delays

---

## 5. Production Checklist

Before releasing to production:

- [ ] Remove debug logging in `ApiClient`
- [ ] Verify product ID matches Play Console
- [ ] Test purchase flow end-to-end
- [ ] Test restore purchases
- [ ] Test edge cases (network errors, cancellation)
- [ ] Review Play billing policy compliance
- [ ] Ensure proper error handling for all billing states

---

## 6. Code Architecture

### Flow Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  FormScreen  │────▶│PaymentScreen │────▶│ ResultScreen │
└──────────────┘     └──────────────┘     └──────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │BillingManager│
                    └──────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │ Google Play  │
                    │   Billing    │
                    └──────────────┘
```

### BillingManager States

```kotlin
sealed class BillingState {
    object Idle          // Initial state
    object Disconnected  // Lost connection
    object Ready         // Can accept purchases
    object Purchasing    // Purchase in progress
    object PurchasePending // Awaiting payment confirmation
    object PurchaseComplete // Purchase succeeded
    object Cancelled     // User cancelled
    class Error(message) // Something went wrong
}
```

### Credit Management

```kotlin
// Check if user can generate
billingManager.canGenerateLetter  // true if free or credits available

// Consume credit (call after successful generation)
billingManager.consumeLetter()    // Deducts 1 credit

// Check counts
billingManager.hasFreeLetterAvailable  // First letter unused
billingManager.availableLetters.value  // Purchased credits
```

---

## 7. Troubleshooting

### "Product not available"

- Verify product ID matches exactly
- Ensure product is **activated** in Play Console
- Check app is uploaded to a testing track
- Wait 15-30 minutes after activation

### Purchase always fails

- Verify tester email is in license testing list
- Ensure device is signed into tester account
- Check app signing matches what's uploaded

### Consumable not consumed

- Check `consumeAsync` is called after purchase
- Verify `ConsumeParams` has correct purchase token
- Check for billing result errors

### Credits not persisting

- Verify SharedPreferences key names
- Check app isn't being killed/reinstalled during testing

---

## 8. Disable Free Letter (Optional)

To disable the free letter option:

```kotlin
// In PaymentViewModel or wherever BillingManager is initialized
billingManager.freeLetterEnabled = false
```

Or remove the free letter UI entirely from `PaymentScreen.kt`.

---

## 9. Price Localization

The billing library automatically handles:
- Currency conversion
- Local price formatting
- Tax calculations (where applicable)

Access the localized price:
```kotlin
billingManager.getFormattedPrice() // Returns "$1.99" or local equivalent
```

---

## 10. Analytics Events (Recommended)

Track these events for business insights:

| Event | When |
|-------|------|
| `payment_screen_viewed` | User reaches payment screen |
| `free_letter_used` | User consumes free letter |
| `purchase_started` | User taps purchase |
| `purchase_completed` | Purchase succeeds |
| `purchase_cancelled` | User cancels |
| `purchase_failed` | Purchase errors |
| `restore_attempted` | User taps restore |
