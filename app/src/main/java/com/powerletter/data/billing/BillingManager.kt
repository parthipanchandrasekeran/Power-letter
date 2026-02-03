package com.powerletter.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.powerletter.data.subscription.ProStatusManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        // This must match the product ID in Google Play Console
        const val PRODUCT_ID_PRO = "power_letter_pro_2months"
    }

    private val proStatusManager = ProStatusManager(context)

    @Suppress("DEPRECATION")
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _isPro = MutableStateFlow(proStatusManager.isPro)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private var pendingPurchaseCallback: ((Boolean) -> Unit)? = null

    fun initialize() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryExistingPurchases()
                } else {
                    _billingState.value = BillingState.Error("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PRO)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList.firstOrNull()
                _billingState.value = BillingState.Ready
            } else {
                _billingState.value = BillingState.Error("Failed to load products")
            }
        }
    }

    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.products.contains(PRODUCT_ID_PRO)
                }

                if (hasActiveSub) {
                    // User has active subscription - ensure Pro is activated
                    if (!proStatusManager.isPro) {
                        proStatusManager.activatePro()
                    }
                    _isPro.value = true
                }

                // Acknowledge any unacknowledged purchases
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, onResult: (Boolean) -> Unit) {
        val details = _productDetails.value
        if (details == null) {
            _billingState.value = BillingState.Error("Product not available. Please try again.")
            onResult(false)
            return
        }

        // Get the subscription offer
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            _billingState.value = BillingState.Error("Subscription offer not available")
            onResult(false)
            return
        }

        pendingPurchaseCallback = onResult
        _billingState.value = BillingState.Purchasing

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _billingState.value = BillingState.Error("Failed to launch purchase: ${result.debugMessage}")
            pendingPurchaseCallback?.invoke(false)
            pendingPurchaseCallback = null
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Cancelled
                pendingPurchaseCallback?.invoke(false)
                pendingPurchaseCallback = null
            }
            else -> {
                _billingState.value = BillingState.Error("Purchase failed: ${billingResult.debugMessage}")
                pendingPurchaseCallback?.invoke(false)
                pendingPurchaseCallback = null
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                // Acknowledge the purchase
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }

                // Activate Pro
                proStatusManager.activatePro()
                _isPro.value = true
                _billingState.value = BillingState.PurchaseComplete
                pendingPurchaseCallback?.invoke(true)
                pendingPurchaseCallback = null
            }
            Purchase.PurchaseState.PENDING -> {
                _billingState.value = BillingState.PurchasePending
                pendingPurchaseCallback?.invoke(false)
                pendingPurchaseCallback = null
            }
            else -> {
                pendingPurchaseCallback?.invoke(false)
                pendingPurchaseCallback = null
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                // Log error but don't fail - purchase is still valid
            }
        }
    }

    fun getFormattedPrice(): String {
        return _productDetails.value
            ?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
            ?: "$2.99"
    }

    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.products.contains(PRODUCT_ID_PRO)
                }

                if (hasActiveSub) {
                    proStatusManager.activatePro()
                    _isPro.value = true
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun resetBillingState() {
        _billingState.value = BillingState.Ready
    }

    fun refreshProStatus() {
        _isPro.value = proStatusManager.isPro
    }

    fun disconnect() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

sealed class BillingState {
    data object Idle : BillingState()
    data object Disconnected : BillingState()
    data object Ready : BillingState()
    data object Purchasing : BillingState()
    data object PurchasePending : BillingState()
    data object PurchaseComplete : BillingState()
    data object Cancelled : BillingState()
    data class Error(val message: String) : BillingState()
}
