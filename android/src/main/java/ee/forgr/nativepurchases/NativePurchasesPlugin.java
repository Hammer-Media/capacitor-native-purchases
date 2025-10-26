package ee.forgr.nativepurchases;

import android.util.Log;
import androidx.annotation.NonNull;
import com.android.billingclient.api.AccountIdentifiers;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;

@CapacitorPlugin(name = "NativePurchases")
public class NativePurchasesPlugin extends Plugin {

    private final String PLUGIN_VERSION = "7.9.4";
    public static final String TAG = "NativePurchases";
    private static final Phaser semaphoreReady = new Phaser(1);
    private BillingClient billingClient;

    @PluginMethod
    public void isBillingSupported(PluginCall call) {
        Log.d(TAG, "isBillingSupported() called");
        JSObject ret = new JSObject();
        ret.put("isBillingSupported", true);
        Log.d(TAG, "isBillingSupported() returning true");
        call.resolve(ret);
    }

    @Override
    public void load() {
        super.load();
        Log.d(TAG, "Plugin load() called");
        Log.i(NativePurchasesPlugin.TAG, "load");
        semaphoreDown();
        Log.d(TAG, "Plugin load() completed");
    }

    private void semaphoreWait() {
        Log.d(TAG, "semaphoreWait() called with waitTime: " + (Number) 10);
        Log.i(NativePurchasesPlugin.TAG, "semaphoreWait " + (Number) 10);
        try {
            //        Log.i(CapacitorUpdater.TAG, "semaphoreReady count " + CapacitorUpdaterPlugin.this.semaphoreReady.getCount());
            semaphoreReady.awaitAdvanceInterruptibly(semaphoreReady.getPhase(), ((Number) 10).longValue(), TimeUnit.SECONDS);
            //        Log.i(CapacitorUpdater.TAG, "semaphoreReady await " + res);
            Log.i(NativePurchasesPlugin.TAG, "semaphoreReady count " + semaphoreReady.getPhase());
            Log.d(TAG, "semaphoreWait() completed successfully");
        } catch (InterruptedException e) {
            Log.d(TAG, "semaphoreWait() InterruptedException: " + e.getMessage());
            Log.i(NativePurchasesPlugin.TAG, "semaphoreWait InterruptedException");
            e.printStackTrace();
        } catch (TimeoutException e) {
            Log.d(TAG, "semaphoreWait() TimeoutException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void semaphoreUp() {
        Log.d(TAG, "semaphoreUp() called");
        Log.i(NativePurchasesPlugin.TAG, "semaphoreUp");
        semaphoreReady.register();
        Log.d(TAG, "semaphoreUp() completed");
    }

    private void semaphoreDown() {
        Log.d(TAG, "semaphoreDown() called");
        Log.i(NativePurchasesPlugin.TAG, "semaphoreDown");
        Log.i(NativePurchasesPlugin.TAG, "semaphoreDown count " + semaphoreReady.getPhase());
        semaphoreReady.arriveAndDeregister();
        Log.d(TAG, "semaphoreDown() completed");
    }

    private void closeBillingClient() {
        Log.d(TAG, "closeBillingClient() called");
        if (billingClient != null) {
            Log.d(TAG, "Ending billing client connection");
            billingClient.endConnection();
            billingClient = null;
            semaphoreDown();
            Log.d(TAG, "Billing client closed and set to null");
        } else {
            Log.d(TAG, "Billing client was already null");
        }
    }

    private void handlePurchase(Purchase purchase, PluginCall purchaseCall) {
        Log.d(TAG, "handlePurchase() called");
        Log.d(TAG, "Purchase details: " + purchase.toString());
        Log.i(NativePurchasesPlugin.TAG, "handlePurchase" + purchase);
        Log.i(NativePurchasesPlugin.TAG, "getPurchaseState" + purchase.getPurchaseState());
        Log.d(TAG, "Purchase state: " + purchase.getPurchaseState());
        Log.d(TAG, "Purchase token: " + purchase.getPurchaseToken());
        Log.d(TAG, "Is acknowledged: " + purchase.isAcknowledged());

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "Purchase state is PURCHASED");
            // Grant entitlement to the user, then acknowledge the purchase
            //     if sub then acknowledgePurchase
            //      if one time then consumePurchase
            AccountIdentifiers accountIdentifiers = purchase.getAccountIdentifiers();
            String purchaseAccountId = accountIdentifiers != null ? accountIdentifiers.getObfuscatedAccountId() : null;
            Log.d(TAG, "Purchase account identifier present: " + (purchaseAccountId != null ? "[REDACTED]" : "none"));
            if (purchase.isAcknowledged()) {
                Log.d(TAG, "Purchase already acknowledged, consuming...");
                ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.consumeAsync(consumeParams, this::onConsumeResponse);
            } else {
                Log.d(TAG, "Purchase not acknowledged, acknowledging...");
                acknowledgePurchase(purchase.getPurchaseToken());
            }

            JSObject ret = new JSObject();
            ret.put("transactionId", purchase.getPurchaseToken());
            ret.put("productIdentifier", purchase.getProducts().get(0));
            ret.put(
                "purchaseDate",
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(
                    new java.util.Date(purchase.getPurchaseTime())
                )
            );
            ret.put("quantity", purchase.getQuantity());
            ret.put("productType", purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED ? "inapp" : "subs");
            ret.put("orderId", purchase.getOrderId());
            ret.put("purchaseToken", purchase.getPurchaseToken());
            ret.put("isAcknowledged", purchase.isAcknowledged());
            ret.put("purchaseState", String.valueOf(purchase.getPurchaseState()));
            ret.put("appAccountToken", purchaseAccountId);

            // Add cancellation information - ALWAYS set willCancel
            // Note: Android doesn't provide direct cancellation information in the Purchase object
            // This would require additional Google Play API calls to get detailed subscription status
            ret.put("willCancel", null); // Default to null, would need API call to determine actual cancellation date

            // For subscriptions, try to get additional information
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.getProducts().get(0).contains("sub")) {
                // Note: Android doesn't provide direct expiration date in Purchase object
                // This would need to be calculated based on subscription period or fetched from Google Play API
                ret.put("productType", "subs");
                // For subscriptions, we can't get expiration date directly from Purchase object
                // This would require additional Google Play API calls to get subscription details
            }

            Log.d(TAG, "Resolving purchase call with transactionId: " + purchase.getPurchaseToken());
            if (purchaseCall != null) {
                purchaseCall.resolve(ret);
            } else {
                Log.d(TAG, "purchaseCall is null, cannot resolve");
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase state is PENDING");
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that are
            // given to them. You can also choose to remind the user to complete the
            // purchase if you detect that it is still pending.
            if (purchaseCall != null) {
                purchaseCall.reject("Purchase is pending");
            } else {
                Log.d(TAG, "purchaseCall is null for pending purchase");
            }
        } else {
            Log.d(TAG, "Purchase state is OTHER: " + purchase.getPurchaseState());
            // Handle any other error codes.
            if (purchaseCall != null) {
                purchaseCall.reject("Purchase is not purchased");
            } else {
                Log.d(TAG, "purchaseCall is null for failed purchase");
            }
        }
    }

    private void acknowledgePurchase(String purchaseToken) {
        Log.d(TAG, "acknowledgePurchase() called with token: " + purchaseToken);
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build();
        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams,
            new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    // Handle the result of the acknowledge purchase
                    Log.d(TAG, "onAcknowledgePurchaseResponse() called");
                    Log.d(TAG, "Acknowledge result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                    Log.i(NativePurchasesPlugin.TAG, "onAcknowledgePurchaseResponse" + billingResult);
                }
            }
        );
    }

    private void initBillingClient(PluginCall purchaseCall) {
        Log.d(TAG, "initBillingClient() called");
        semaphoreWait();
        closeBillingClient();
        semaphoreUp();
        CountDownLatch semaphoreReady = new CountDownLatch(1);
        Log.d(TAG, "Creating new BillingClient");
        billingClient = BillingClient.newBuilder(getContext())
            .setListener(
                new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
                        Log.d(TAG, "onPurchasesUpdated() called");
                        Log.d(TAG, "Billing result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                        Log.d(TAG, "Purchases count: " + (purchases != null ? purchases.size() : 0));
                        Log.i(NativePurchasesPlugin.TAG, "onPurchasesUpdated" + billingResult);
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                            Log.d(TAG, "Purchase update successful, processing first purchase");
                            //                          for (Purchase purchase : purchases) {
                            //                              handlePurchase(purchase, purchaseCall);
                            //                          }
                            handlePurchase(purchases.get(0), purchaseCall);
                        } else {
                            // Handle any other error codes.
                            Log.d(TAG, "Purchase update failed or purchases is null");
                            Log.i(NativePurchasesPlugin.TAG, "onPurchasesUpdated" + billingResult);
                            if (purchaseCall != null) {
                                purchaseCall.reject("Purchase is not purchased");
                            }
                        }
                        closeBillingClient();
                        return;
                    }
                }
            )
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build();
        Log.d(TAG, "Starting billing client connection");
        billingClient.startConnection(
            new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    Log.d(TAG, "onBillingSetupFinished() called");
                    Log.d(TAG, "Setup result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing setup successful, client is ready");
                        // The BillingClient is ready. You can query purchases here.
                        semaphoreReady.countDown();
                    } else {
                        Log.d(TAG, "Billing setup failed");
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    Log.d(TAG, "onBillingServiceDisconnected() called");
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            }
        );
        try {
            Log.d(TAG, "Waiting for billing client setup to finish");
            semaphoreReady.await();
            Log.d(TAG, "Billing client setup completed");
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException while waiting for billing setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PluginMethod
    public void getPluginVersion(final PluginCall call) {
        Log.d(TAG, "getPluginVersion() called");
        try {
            final JSObject ret = new JSObject();
            ret.put("version", this.PLUGIN_VERSION);
            Log.d(TAG, "Returning plugin version: " + this.PLUGIN_VERSION);
            call.resolve(ret);
        } catch (final Exception e) {
            Log.d(TAG, "Error getting plugin version: " + e.getMessage());
            call.reject("Could not get plugin version", e);
        }
    }

    @PluginMethod
    public void purchaseProduct(PluginCall call) {
        Log.d(TAG, "purchaseProduct() called");
        String productIdentifier = call.getString("productIdentifier");
        String planIdentifier = call.getString("planIdentifier");
        String productType = call.getString("productType", "inapp");
        Number quantity = call.getInt("quantity", 1);
        String appAccountToken = call.getString("appAccountToken");
        final String accountIdentifier = appAccountToken != null && !appAccountToken.isEmpty() ? appAccountToken : null;

        Log.d(TAG, "Product identifier: " + productIdentifier);
        Log.d(TAG, "Plan identifier: " + planIdentifier);
        Log.d(TAG, "Product type: " + productType);
        Log.d(TAG, "Quantity: " + quantity);
        Log.d(TAG, "Account identifier provided: " + (accountIdentifier != null ? "[REDACTED]" : "none"));

        // cannot use quantity, because it's done in native modal
        Log.d("CapacitorPurchases", "purchaseProduct: " + productIdentifier);
        if (productIdentifier == null || productIdentifier.isEmpty()) {
            // Handle error: productIdentifier is empty
            Log.d(TAG, "Error: productIdentifier is empty");
            call.reject("productIdentifier is empty");
            return;
        }
        if (productType == null || productType.isEmpty()) {
            // Handle error: productType is empty
            Log.d(TAG, "Error: productType is empty");
            call.reject("productType is empty");
            return;
        }
        if (productType.equals("subs") && (planIdentifier == null || planIdentifier.isEmpty())) {
            // Handle error: no planIdentifier with productType subs
            Log.d(TAG, "Error: planIdentifier cannot be empty if productType is subs");
            call.reject("planIdentifier cannot be empty if productType is subs");
            return;
        }
        assert quantity != null;
        if (quantity.intValue() < 1) {
            // Handle error: quantity is less than 1
            Log.d(TAG, "Error: quantity is less than 1");
            call.reject("quantity is less than 1");
            return;
        }

        // For subscriptions, always use the productIdentifier (subscription ID) to query
        // The planIdentifier is used later when setting the offer token
        Log.d(TAG, "Using product ID for query: " + productIdentifier);

        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productIdentifier)
                .setProductType(productType.equals("inapp") ? BillingClient.ProductType.INAPP : BillingClient.ProductType.SUBS)
                .build()
        );
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(productList).build();
        Log.d(TAG, "Initializing billing client for purchase");
        this.initBillingClient(call);
        try {
            Log.d(TAG, "Querying product details for purchase");
            billingClient.queryProductDetailsAsync(
                params,
                new ProductDetailsResponseListener() {
                    @Override
                    public void onProductDetailsResponse(
                        @NonNull BillingResult billingResult,
                        @NonNull QueryProductDetailsResult queryProductDetailsResult
                    ) {
                        List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();
                        Log.d(TAG, "onProductDetailsResponse() called for purchase");
                        Log.d(TAG, "Query result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                        Log.d(TAG, "Product details count: " + productDetailsList.size());

                        if (productDetailsList.isEmpty()) {
                            Log.d(TAG, "No products found");
                            closeBillingClient();
                            call.reject("Product not found");
                            return;
                        }
                        // Process the result
                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                        for (ProductDetails productDetailsItem : productDetailsList) {
                            Log.d(TAG, "Processing product: " + productDetailsItem.getProductId());
                            BillingFlowParams.ProductDetailsParams.Builder productDetailsParams =
                                BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetailsItem);
                            if (productType.equals("subs")) {
                                Log.d(TAG, "Processing subscription product");
                                // list the SubscriptionOfferDetails and find the one who match the planIdentifier if not found get the first one
                                ProductDetails.SubscriptionOfferDetails selectedOfferDetails = null;
                                assert productDetailsItem.getSubscriptionOfferDetails() != null;
                                Log.d(TAG, "Available offer details count: " + productDetailsItem.getSubscriptionOfferDetails().size());
                                for (ProductDetails.SubscriptionOfferDetails offerDetails : productDetailsItem.getSubscriptionOfferDetails()) {
                                    Log.d(TAG, "Checking offer: " + offerDetails.getBasePlanId());
                                    if (offerDetails.getBasePlanId().equals(planIdentifier)) {
                                        selectedOfferDetails = offerDetails;
                                        Log.d(TAG, "Found matching plan: " + planIdentifier);
                                        break;
                                    }
                                }
                                if (selectedOfferDetails == null) {
                                    selectedOfferDetails = productDetailsItem.getSubscriptionOfferDetails().get(0);
                                    Log.d(TAG, "Using first available offer: " + selectedOfferDetails.getBasePlanId());
                                }
                                productDetailsParams.setOfferToken(selectedOfferDetails.getOfferToken());
                                Log.d(TAG, "Set offer token: " + selectedOfferDetails.getOfferToken());
                            }
                            productDetailsParamsList.add(productDetailsParams.build());
                        }
                        BillingFlowParams.Builder billingFlowBuilder = BillingFlowParams.newBuilder().setProductDetailsParamsList(
                            productDetailsParamsList
                        );
                        if (accountIdentifier != null && !accountIdentifier.isEmpty()) {
                            billingFlowBuilder.setObfuscatedAccountId(accountIdentifier);
                        }
                        BillingFlowParams billingFlowParams = billingFlowBuilder.build();
                        // Launch the billing flow
                        Log.d(TAG, "Launching billing flow");
                        BillingResult billingResult2 = billingClient.launchBillingFlow(getActivity(), billingFlowParams);
                        Log.d(
                            TAG,
                            "Billing flow launch result: " + billingResult2.getResponseCode() + " - " + billingResult2.getDebugMessage()
                        );
                        Log.i(NativePurchasesPlugin.TAG, "onProductDetailsResponse2" + billingResult2);
                    }
                }
            );
        } catch (Exception e) {
            Log.d(TAG, "Exception during purchase: " + e.getMessage());
            closeBillingClient();
            call.reject(e.getMessage());
        }
    }

    private void processUnfinishedPurchases() {
        Log.d(TAG, "processUnfinishedPurchases() called");
        QueryPurchasesParams queryInAppPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build();
        Log.d(TAG, "Querying unfinished in-app purchases");
        billingClient.queryPurchasesAsync(queryInAppPurchasesParams, this::handlePurchases);

        QueryPurchasesParams querySubscriptionsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build();
        Log.d(TAG, "Querying unfinished subscription purchases");
        billingClient.queryPurchasesAsync(querySubscriptionsParams, this::handlePurchases);
    }

    private void handlePurchases(BillingResult billingResult, List<Purchase> purchases) {
        Log.d(TAG, "handlePurchases() called");
        Log.d(TAG, "Query purchases result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
        Log.d(TAG, "Purchases count: " + (purchases != null ? purchases.size() : 0));

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            assert purchases != null;
            for (Purchase purchase : purchases) {
                Log.d(TAG, "Processing purchase: " + purchase.getOrderId());
                Log.d(TAG, "Purchase state: " + purchase.getPurchaseState());
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    if (purchase.isAcknowledged()) {
                        Log.d(TAG, "Purchase already acknowledged, consuming");
                        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                        billingClient.consumeAsync(consumeParams, this::onConsumeResponse);
                    } else {
                        Log.d(TAG, "Purchase not acknowledged, acknowledging");
                        acknowledgePurchase(purchase.getPurchaseToken());
                    }
                }
            }
        } else {
            Log.d(TAG, "Query purchases failed");
        }
    }

    private void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
        Log.d(TAG, "onConsumeResponse() called");
        Log.d(TAG, "Consume result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
        Log.d(TAG, "Purchase token: " + purchaseToken);

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            // Handle the success of the consume operation.
            // For example, you can update the UI to reflect that the item has been consumed.
            Log.d(TAG, "Consume operation successful");
            Log.i(NativePurchasesPlugin.TAG, "onConsumeResponse OK " + billingResult + purchaseToken);
        } else {
            // Handle error responses.
            Log.d(TAG, "Consume operation failed");
            Log.i(NativePurchasesPlugin.TAG, "onConsumeResponse OTHER " + billingResult + purchaseToken);
        }
    }

    @PluginMethod
    public void restorePurchases(PluginCall call) {
        Log.d(TAG, "restorePurchases() called");
        Log.d(NativePurchasesPlugin.TAG, "restorePurchases");
        this.initBillingClient(null);
        this.processUnfinishedPurchases();
        call.resolve();
        Log.d(TAG, "restorePurchases() completed");
    }

    private void queryProductDetails(List<String> productIdentifiers, String productType, PluginCall call) {
        Log.d(TAG, "queryProductDetails() called");
        Log.d(TAG, "Product identifiers count: " + productIdentifiers.size());
        Log.d(TAG, "Product type: " + productType);
        for (String id : productIdentifiers) {
            Log.d(TAG, "Product ID: " + id);
        }

        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        for (String productIdentifier : productIdentifiers) {
            String productTypeForQuery = productType.equals("inapp") ? BillingClient.ProductType.INAPP : BillingClient.ProductType.SUBS;
            Log.d(TAG, "Creating query product: ID='" + productIdentifier + "', Type='" + productTypeForQuery + "'");
            productList.add(
                QueryProductDetailsParams.Product.newBuilder().setProductId(productIdentifier).setProductType(productTypeForQuery).build()
            );
        }
        Log.d(TAG, "Total products in query list: " + productList.size());
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(productList).build();
        Log.d(TAG, "Initializing billing client for product query");
        this.initBillingClient(call);
        try {
            Log.d(TAG, "Querying product details");
            billingClient.queryProductDetailsAsync(
                params,
                new ProductDetailsResponseListener() {
                    @Override
                    public void onProductDetailsResponse(
                        @NonNull BillingResult billingResult,
                        @NonNull QueryProductDetailsResult queryProductDetailsResult
                    ) {
                        List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();
                        Log.d(TAG, "onProductDetailsResponse() called for query");
                        Log.d(TAG, "Query result: " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                        Log.d(TAG, "Product details count: " + productDetailsList.size());

                        if (productDetailsList.isEmpty()) {
                            Log.d(TAG, "No products found in query");
                            Log.d(TAG, "This usually means:");
                            Log.d(TAG, "1. Product doesn't exist in Google Play Console");
                            Log.d(TAG, "2. Product is not published/active");
                            Log.d(TAG, "3. App is not properly configured for the product type");
                            Log.d(TAG, "4. Wrong product ID or type");
                            closeBillingClient();
                            call.reject("Product not found");
                            return;
                        }
                        JSONArray products = new JSONArray();
                        for (ProductDetails productDetails : productDetailsList) {
                            Log.d(TAG, "Processing product details: " + productDetails.getProductId());
                            JSObject product = new JSObject();
                            product.put("title", productDetails.getName());
                            product.put("description", productDetails.getDescription());
                            Log.d(TAG, "Product title: " + productDetails.getName());
                            Log.d(TAG, "Product description: " + productDetails.getDescription());

                            if (productType.equals("inapp")) {
                                Log.d(TAG, "Processing as in-app product");
                                product.put("identifier", productDetails.getProductId());
                                double price =
                                    Objects.requireNonNull(productDetails.getOneTimePurchaseOfferDetails()).getPriceAmountMicros() /
                                    1000000.0;
                                product.put("price", price);
                                product.put("priceString", productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                product.put("currencyCode", productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode());
                                Log.d(TAG, "Price: " + price);
                                Log.d(TAG, "Formatted price: " + productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                Log.d(TAG, "Currency: " + productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode());
                            } else {
                                Log.d(TAG, "Processing as subscription product");
                                ProductDetails.SubscriptionOfferDetails selectedOfferDetails = productDetails
                                    .getSubscriptionOfferDetails()
                                    .get(0);
                                product.put("planIdentifier", productDetails.getProductId());
                                product.put("identifier", selectedOfferDetails.getBasePlanId());
                                double price =
                                    selectedOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros() / 1000000.0;
                                product.put("price", price);
                                product.put(
                                    "priceString",
                                    selectedOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice()
                                );
                                product.put(
                                    "currencyCode",
                                    selectedOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode()
                                );
                                Log.d(TAG, "Plan identifier: " + productDetails.getProductId());
                                Log.d(TAG, "Base plan ID: " + selectedOfferDetails.getBasePlanId());
                                Log.d(TAG, "Price: " + price);
                                Log.d(
                                    TAG,
                                    "Formatted price: " +
                                        selectedOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice()
                                );
                                Log.d(
                                    TAG,
                                    "Currency: " +
                                        selectedOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode()
                                );
                            }
                            product.put("isFamilyShareable", false);
                            products.put(product);
                        }
                        JSObject ret = new JSObject();
                        ret.put("products", products);
                        Log.d(TAG, "Returning " + products.length() + " products");
                        closeBillingClient();
                        call.resolve(ret);
                    }
                }
            );
        } catch (Exception e) {
            Log.d(TAG, "Exception during product query: " + e.getMessage());
            closeBillingClient();
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void getProducts(PluginCall call) {
        Log.d(TAG, "getProducts() called");
        JSONArray productIdentifiersArray = call.getArray("productIdentifiers");
        String productType = call.getString("productType", "inapp");
        Log.d(TAG, "Product type: " + productType);
        Log.d(TAG, "Raw productIdentifiersArray: " + productIdentifiersArray);
        Log.d(TAG, "productIdentifiersArray length: " + (productIdentifiersArray != null ? productIdentifiersArray.length() : "null"));

        if (productIdentifiersArray == null || productIdentifiersArray.length() == 0) {
            Log.d(TAG, "Error: productIdentifiers array missing or empty");
            call.reject("productIdentifiers array missing");
            return;
        }

        List<String> productIdentifiers = new ArrayList<>();
        for (int i = 0; i < productIdentifiersArray.length(); i++) {
            String productId = productIdentifiersArray.optString(i, "");
            Log.d(TAG, "Array index " + i + ": '" + productId + "'");
            productIdentifiers.add(productId);
            Log.d(TAG, "Added product identifier: " + productId);
        }
        Log.d(TAG, "Final productIdentifiers list: " + productIdentifiers.toString());
        queryProductDetails(productIdentifiers, productType, call);
    }

    @PluginMethod
    public void getProduct(PluginCall call) {
        Log.d(TAG, "getProduct() called");
        String productIdentifier = call.getString("productIdentifier");
        String productType = call.getString("productType", "inapp");
        Log.d(TAG, "Product identifier: " + productIdentifier);
        Log.d(TAG, "Product type: " + productType);

        assert productIdentifier != null;
        if (productIdentifier.isEmpty()) {
            Log.d(TAG, "Error: productIdentifier is empty");
            call.reject("productIdentifier is empty");
            return;
        }
        queryProductDetails(Collections.singletonList(productIdentifier), productType, call);
    }

    @PluginMethod
    public void getPurchases(PluginCall call) {
        Log.d(TAG, "getPurchases() called");
        String productType = call.getString("productType");
        Log.d(TAG, "Product type filter: " + productType);
        String appAccountToken = call.getString("appAccountToken");
        final String accountFilter = appAccountToken != null && !appAccountToken.isEmpty() ? appAccountToken : null;
        final boolean hasAccountFilter = accountFilter != null && !accountFilter.isEmpty();
        Log.d(TAG, "Account filter provided: " + (hasAccountFilter ? "[REDACTED]" : "none"));

        final boolean queryInApp = productType == null || productType.equals("inapp");
        final boolean querySubs = productType == null || productType.equals("subs");

        if (!queryInApp && !querySubs) {
            Log.d(TAG, "Unknown product type filter provided, returning empty result");
            JSObject result = new JSObject();
            result.put("purchases", new JSONArray());
            call.resolve(result);
            return;
        }

        this.initBillingClient(null);

        JSONArray allPurchases = new JSONArray();
        AtomicInteger pendingQueries = new AtomicInteger((queryInApp ? 1 : 0) + (querySubs ? 1 : 0));
        AtomicBoolean finished = new AtomicBoolean(false);

        Runnable maybeFinish = () -> {
            int remaining = pendingQueries.decrementAndGet();
            Log.d(TAG, "Pending purchase queries remaining: " + remaining);
            if (remaining <= 0 && finished.compareAndSet(false, true)) {
                JSObject result = new JSObject();
                result.put("purchases", allPurchases);
                Log.d(TAG, "Returning " + allPurchases.length() + " purchases");
                closeBillingClient();
                call.resolve(result);
            }
        };

        try {
            if (queryInApp) {
                Log.d(TAG, "Querying in-app purchases");
                QueryPurchasesParams queryInAppParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build();

                billingClient.queryPurchasesAsync(queryInAppParams, (billingResult, purchases) -> {
                    try {
                        Log.d(TAG, "In-app purchases query result: " + billingResult.getResponseCode());
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                            for (Purchase purchase : purchases) {
                                Log.d(TAG, "Processing in-app purchase: " + purchase.getOrderId());
                                AccountIdentifiers accountIdentifiers = purchase.getAccountIdentifiers();
                                String purchaseAccountId = accountIdentifiers != null ? accountIdentifiers.getObfuscatedAccountId() : null;
                                if (hasAccountFilter && (purchaseAccountId == null || !purchaseAccountId.equals(accountFilter))) {
                                    Log.d(TAG, "Skipping in-app purchase due to account filter mismatch");
                                    continue;
                                }
                                JSObject purchaseData = new JSObject();
                                purchaseData.put("transactionId", purchase.getPurchaseToken());
                                purchaseData.put(
                                    "productIdentifier",
                                    purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0)
                                );
                                purchaseData.put(
                                    "purchaseDate",
                                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(
                                        new java.util.Date(purchase.getPurchaseTime())
                                    )
                                );
                                purchaseData.put("quantity", purchase.getQuantity());
                                purchaseData.put("productType", "inapp");
                                purchaseData.put("orderId", purchase.getOrderId());
                                purchaseData.put("purchaseToken", purchase.getPurchaseToken());
                                purchaseData.put("isAcknowledged", purchase.isAcknowledged());
                                purchaseData.put("purchaseState", String.valueOf(purchase.getPurchaseState()));
                                purchaseData.put("appAccountToken", purchaseAccountId);
                                purchaseData.put("willCancel", null);
                                synchronized (allPurchases) {
                                    allPurchases.put(purchaseData);
                                }
                            }
                        } else {
                            Log.d(TAG, "In-app purchase query failed: " + billingResult.getDebugMessage());
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "Error processing in-app purchase query: " + ex.getMessage());
                    } finally {
                        maybeFinish.run();
                    }
                });
            }

            if (querySubs) {
                Log.d(TAG, "Querying only subscription purchases");
                QueryPurchasesParams querySubsParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build();

                billingClient.queryPurchasesAsync(querySubsParams, (billingResult, purchases) -> {
                    try {
                        Log.d(TAG, "Subscription purchases query result: " + billingResult.getResponseCode());
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                            for (Purchase purchase : purchases) {
                                Log.d(TAG, "Processing subscription purchase: " + purchase.getOrderId());
                                AccountIdentifiers accountIdentifiers = purchase.getAccountIdentifiers();
                                String purchaseAccountId = accountIdentifiers != null ? accountIdentifiers.getObfuscatedAccountId() : null;
                                if (hasAccountFilter && (purchaseAccountId == null || !purchaseAccountId.equals(accountFilter))) {
                                    Log.d(TAG, "Skipping subscription purchase due to account filter mismatch");
                                    continue;
                                }
                                JSObject purchaseData = new JSObject();
                                purchaseData.put("transactionId", purchase.getPurchaseToken());
                                purchaseData.put(
                                    "productIdentifier",
                                    purchase.getProducts().isEmpty() ? null : purchase.getProducts().get(0)
                                );
                                purchaseData.put(
                                    "purchaseDate",
                                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(
                                        new java.util.Date(purchase.getPurchaseTime())
                                    )
                                );
                                purchaseData.put("quantity", purchase.getQuantity());
                                purchaseData.put("productType", "subs");
                                purchaseData.put("orderId", purchase.getOrderId());
                                purchaseData.put("purchaseToken", purchase.getPurchaseToken());
                                purchaseData.put("isAcknowledged", purchase.isAcknowledged());
                                purchaseData.put("purchaseState", String.valueOf(purchase.getPurchaseState()));
                                purchaseData.put("appAccountToken", purchaseAccountId);
                                purchaseData.put("willCancel", null);
                                synchronized (allPurchases) {
                                    allPurchases.put(purchaseData);
                                }
                            }
                        } else {
                            Log.d(TAG, "Subscription purchase query failed: " + billingResult.getDebugMessage());
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "Error processing subscription purchase query: " + ex.getMessage());
                    } finally {
                        maybeFinish.run();
                    }
                });
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception during getPurchases: " + e.getMessage());
            finished.set(true);
            closeBillingClient();
            call.reject(e.getMessage());
        }
    }
}
