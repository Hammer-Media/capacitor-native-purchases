import type { PluginListenerHandle } from "@capacitor/core";
export declare enum ATTRIBUTION_NETWORK {
    APPLE_SEARCH_ADS = 0,
    ADJUST = 1,
    APPSFLYER = 2,
    BRANCH = 3,
    TENJIN = 4,
    FACEBOOK = 5
}
export declare enum PURCHASE_TYPE {
    /**
     * A type of SKU for in-app products.
     */
    INAPP = "inapp",
    /**
     * A type of SKU for subscriptions.
     */
    SUBS = "subs"
}
/**
 * Enum for billing features.
 * Currently, these are only relevant for Google Play Android users:
 * https://developer.android.com/reference/com/android/billingclient/api/BillingClient.FeatureType
 */
export declare enum BILLING_FEATURE {
    /**
     * Purchase/query for subscriptions.
     */
    SUBSCRIPTIONS = 0,
    /**
     * Subscriptions update/replace.
     */
    SUBSCRIPTIONS_UPDATE = 1,
    /**
     * Purchase/query for in-app items on VR.
     */
    IN_APP_ITEMS_ON_VR = 2,
    /**
     * Purchase/query for subscriptions on VR.
     */
    SUBSCRIPTIONS_ON_VR = 3,
    /**
     * Launch a price change confirmation flow.
     */
    PRICE_CHANGE_CONFIRMATION = 4
}
export declare enum PRORATION_MODE {
    UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY = 0,
    /**
     * Replacement takes effect immediately, and the remaining time will be
     * prorated and credited to the user. This is the current default behavior.
     */
    IMMEDIATE_WITH_TIME_PRORATION = 1,
    /**
     * Replacement takes effect immediately, and the billing cycle remains the
     * same. The price for the remaining period will be charged. This option is
     * only available for subscription upgrade.
     */
    IMMEDIATE_AND_CHARGE_PRORATED_PRICE = 2,
    /**
     * Replacement takes effect immediately, and the new price will be charged on
     * next recurrence time. The billing cycle stays the same.
     */
    IMMEDIATE_WITHOUT_PRORATION = 3,
    /**
     * Replacement takes effect when the old plan expires, and the new price will
     * be charged at the same time.
     */
    DEFERRED = 4
}
export declare enum PACKAGE_TYPE {
    /**
     * A package that was defined with a custom identifier.
     */
    UNKNOWN = "UNKNOWN",
    /**
     * A package that was defined with a custom identifier.
     */
    CUSTOM = "CUSTOM",
    /**
     * A package configured with the predefined lifetime identifier.
     */
    LIFETIME = "LIFETIME",
    /**
     * A package configured with the predefined annual identifier.
     */
    ANNUAL = "ANNUAL",
    /**
     * A package configured with the predefined six month identifier.
     */
    SIX_MONTH = "SIX_MONTH",
    /**
     * A package configured with the predefined three month identifier.
     */
    THREE_MONTH = "THREE_MONTH",
    /**
     * A package configured with the predefined two month identifier.
     */
    TWO_MONTH = "TWO_MONTH",
    /**
     * A package configured with the predefined monthly identifier.
     */
    MONTHLY = "MONTHLY",
    /**
     * A package configured with the predefined weekly identifier.
     */
    WEEKLY = "WEEKLY"
}
export declare enum INTRO_ELIGIBILITY_STATUS {
    /**
     * RevenueCat doesn't have enough information to determine eligibility.
     */
    INTRO_ELIGIBILITY_STATUS_UNKNOWN = 0,
    /**
     * The user is not eligible for a free trial or intro pricing for this product.
     */
    INTRO_ELIGIBILITY_STATUS_INELIGIBLE = 1,
    /**
     * The user is eligible for a free trial or intro pricing for this product.
     */
    INTRO_ELIGIBILITY_STATUS_ELIGIBLE = 2
}
/**
 * Subscription period unit types.
 * Maps to StoreKit 2 Product.SubscriptionPeriod.Unit
 */
export declare enum SUBSCRIPTION_PERIOD_UNIT {
    /**
     * Day unit
     */
    DAY = 0,
    /**
     * Week unit
     */
    WEEK = 1,
    /**
     * Month unit
     */
    MONTH = 2,
    /**
     * Year unit
     */
    YEAR = 3
}
/**
 * Payment mode for subscription offers.
 * Maps to StoreKit 2 Product.SubscriptionOffer.PaymentMode
 */
export declare enum OFFER_PAYMENT_MODE {
    /**
     * Pay as you go - price charged each billing period
     */
    PAY_AS_YOU_GO = 0,
    /**
     * Pay up front - total price charged at the beginning
     */
    PAY_UP_FRONT = 1,
    /**
     * Free trial - no charge during the offer period
     */
    FREE_TRIAL = 2
}
export interface Transaction {
    /**
     * The unique transaction identifier.
     */
    readonly transactionId: string;
    /**
     * The original transaction identifier (for renewals). iOS only.
     */
    readonly originalTransactionId?: string;
    /**
     * Receipt data for validation (iOS only - base64 encoded receipt)
     */
    readonly receipt?: string;
    /**
     * Product Id associated with the transaction.
     */
    readonly productIdentifier?: string;
    /**
     * Product identifier associated with the transaction. iOS only.
     */
    readonly productId?: string;
    /**
     * Purchase quantity. iOS only.
     */
    readonly quantity?: number;
    /**
     * Purchase date (string in ISO 8601 format for Android, number in milliseconds since epoch for iOS).
     */
    readonly purchaseDate?: string | number;
    /**
     * Original purchase date (string in ISO 8601 format for Android, number in milliseconds since epoch for iOS).
     */
    readonly originalPurchaseDate?: string | number;
    /**
     * Transaction signed date in milliseconds since epoch. iOS only.
     */
    readonly signedDate?: number;
    /**
     * Expiration date of the transaction in ISO 8601 format (for subscriptions).
     */
    readonly expirationDate?: string;
    /**
     * Expiration date for subscriptions (in milliseconds since epoch). iOS only.
     */
    readonly expiresDate?: number;
    /**
     * Whether the transaction is still active/valid.
     */
    readonly isActive?: boolean;
    /**
     * Whether the subscription will be cancelled at the end of the billing cycle, or null if not cancelled. Only available on iOS.
     */
    readonly willCancel?: boolean | null;
    /**
     * Purchase state of the transaction.
     */
    readonly purchaseState?: string;
    /**
     * Transaction reason (PURCHASE, RENEWAL, etc.). iOS only.
     */
    readonly transactionReason?: string;
    /**
     * App Store environment (Sandbox/Production). iOS only.
     */
    readonly environment?: string;
    /**
     * App Store storefront. iOS only.
     */
    readonly storefront?: string;
    /**
     * App Store storefront identifier. iOS only.
     */
    readonly storefrontId?: string;
    /**
     * Transaction price. iOS only.
     */
    readonly price?: number;
    /**
     * Currency code. iOS only.
     */
    readonly currency?: string;
    /**
     * Subscription group identifier (for subscriptions). iOS only.
     */
    readonly subscriptionGroupId?: string;
    /**
     * Web order line item identifier. iOS only.
     */
    readonly webOrderLineItemId?: string;
    /**
     * App transaction identifier. iOS only.
     */
    readonly appTransactionId?: string;
    /**
     * App bundle identifier. iOS only.
     */
    readonly bundleId?: string;
    /**
     * Device verification data. iOS only.
     */
    readonly deviceVerification?: string;
    /**
     * Device verification nonce. iOS only.
     */
    readonly deviceVerificationNonce?: string;
    /**
     * In-app ownership type (PURCHASED, FAMILY_SHARED, etc.). iOS only.
     */
    readonly inAppOwnershipType?: string;
    /**
     * Signed transaction JWT token. iOS only.
     */
    readonly jwt?: string;
    /**
     * Order ID associated with the transaction (Android).
     */
    readonly orderId?: string;
    /**
     * Purchase token associated with the transaction (Android).
     */
    readonly purchaseToken?: string;
    /**
     * Whether the purchase has been acknowledged (Android).
     */
    readonly isAcknowledged?: boolean;
    /**
     * Product type (inapp or subs).
     */
    readonly productType?: string;
    /**
     * Product type (Auto-Renewable Subscription, Consumable, etc.). iOS only.
     */
    readonly type?: string;
    /**
     * Whether the transaction is a trial period.
     */
    readonly isTrialPeriod?: boolean;
    /**
     * Whether the transaction is in intro price period.
     */
    readonly isInIntroPricePeriod?: boolean;
    /**
     * Whether the transaction is in grace period.
     */
    readonly isInGracePeriod?: boolean;
}
export interface SubscriptionPeriod {
    /**
     * The Subscription Period number of unit.
     */
    readonly numberOfUnits: number;
    /**
     * The Subscription Period unit.
     */
    readonly unit: number;
}
export interface SKProductDiscount {
    /**
     * The Product discount identifier.
     */
    readonly identifier: string;
    /**
     * The Product discount type.
     */
    readonly type: number;
    /**
     * The Product discount price.
     */
    readonly price: number;
    /**
     * Formatted price of the item, including its currency sign, such as €3.99.
     */
    readonly priceString: string;
    /**
     * The Product discount currency symbol.
     */
    readonly currencySymbol: string;
    /**
     * The Product discount currency code.
     */
    readonly currencyCode: string;
    /**
     * The Product discount paymentMode.
     */
    readonly paymentMode: number;
    /**
     * The Product discount number Of Periods.
     */
    readonly numberOfPeriods: number;
    /**
     * The Product discount subscription period.
     */
    readonly subscriptionPeriod: SubscriptionPeriod;
}
export interface Product {
    /**
     * Product Id.
     */
    readonly identifier: string;
    /**
     * Description of the product.
     */
    readonly description: string;
    /**
     * Title of the product.
     */
    readonly title: string;
    /**
     * Price of the product in the local currency.
     */
    readonly price: number;
    /**
     * Formatted price of the item, including its currency sign, such as €3.99.
     */
    readonly priceString: string;
    /**
     * Currency code for price and original price.
     */
    readonly currencyCode: string;
    /**
     * Currency symbol for price and original price.
     */
    readonly currencySymbol: string;
    /**
     * Boolean indicating if the product is sharable with family
     */
    readonly isFamilyShareable: boolean;
    /**
     * Group identifier for the product.
     */
    readonly subscriptionGroupIdentifier: string;
    /**
     * The Product subcription group identifier.
     */
    readonly subscriptionPeriod: SubscriptionPeriod;
    /**
     * The Product introductory Price.
     */
    readonly introductoryPrice: SKProductDiscount | null;
    /**
     * The Product discounts list.
     */
    readonly discounts: SKProductDiscount[];
}
export interface NativePurchasesPlugin {
    /**
     * Restores a user's previous  and links their appUserIDs to any user's also using those .
     */
    restorePurchases(): Promise<{
        transactions: Transaction[];
    }>;
    /**
     * Started purchase process for the given product.
     *
     * @param options - The product to purchase
     * @param options.productIdentifier - The product identifier of the product you want to purchase.
     * @param options.productType - Only Android, the type of product, can be inapp or subs. Will use inapp by default.
     * @param options.planIdentifier - Only Android, the identifier of the plan you want to purchase, require for for subs.
     * @param options.quantity - Only iOS, the number of items you wish to purchase. Will use 1 by default.
     * @param options.appAccountToken - Only iOS, UUID for the user's account. Used to link purchases to the user account for App Store Server Notifications.
     */
    purchaseProduct(options: {
        productIdentifier: string;
        planIdentifier?: string;
        productType?: PURCHASE_TYPE;
        quantity?: number;
        appAccountToken?: string;
    }): Promise<Transaction>;
    /**
     * Gets the product info associated with a list of product identifiers.
     *
     * @param options - The product identifiers you wish to retrieve information for
     * @param options.productIdentifiers - Array of product identifiers
     * @param options.productType - Only Android, the type of product, can be inapp or subs. Will use inapp by default.
     * @returns - The requested product info
     */
    getProducts(options: {
        productIdentifiers: string[];
        productType?: PURCHASE_TYPE;
    }): Promise<{
        products: Product[];
    }>;
    /**
     * Gets the product info for a single product identifier.
     *
     * @param options - The product identifier you wish to retrieve information for
     * @param options.productIdentifier - The product identifier
     * @param options.productType - Only Android, the type of product, can be inapp or subs. Will use inapp by default.
     * @returns - The requested product info
     */
    getProduct(options: {
        productIdentifier: string;
        productType?: PURCHASE_TYPE;
    }): Promise<{
        product: Product;
    }>;
    /**
     * Check if billing is supported for the current device.
     *
     *
     */
    isBillingSupported(): Promise<{
        isBillingSupported: boolean;
    }>;
    /**
     * Get the native Capacitor plugin version
     *
     * @returns {Promise<{ id: string }>} an Promise with version for this device
     * @throws An error if the something went wrong
     */
    getPluginVersion(): Promise<{
        version: string;
    }>;
    /**
     * Gets all the user's purchases (both in-app purchases and subscriptions).
     * This method queries the platform's purchase history for the current user.
     *
     * @param options - Optional parameters for filtering purchases
     * @param options.productType - Only Android, filter by product type (inapp or subs). If not specified, returns both types.
     * @returns {Promise<{ purchases: Transaction[] }>} Promise that resolves with array of user's purchases
     * @throws An error if the purchase query fails
     * @since 7.2.0
     */
    getPurchases(options?: {
        productType?: PURCHASE_TYPE;
    }): Promise<{
        purchases: Transaction[];
    }>;
    /**
     * Get the latest signed transaction JWT token. iOS only.
     *
     * @returns {Promise<{ jwt: string }>} Promise with the JWT token
     * @throws An error if no transactions found or StoreKit 2 not available
     */
    getLatestSignedTransaction(): Promise<{
        jwt: string;
    }>;
    /**
     * Opens the native subscription management interface for the user.
     */
    showManageSubscriptions(): Promise<void>;
    /**
     * Listen for StoreKit transaction updates delivered by Apple's Transaction.updates.
     * Fires on app launch if there are unfinished transactions, and for any updates afterward.
     * iOS only.
     */
    addListener(eventName: "transactionUpdated", listenerFunc: (transaction: Transaction) => void): Promise<PluginListenerHandle>;
    /** Remove all registered listeners */
    removeAllListeners(): Promise<void>;
}
