import type { PluginListenerHandle } from '@capacitor/core';

export enum ATTRIBUTION_NETWORK {
  APPLE_SEARCH_ADS = 0,
  ADJUST = 1,
  APPSFLYER = 2,
  BRANCH = 3,
  TENJIN = 4,
  FACEBOOK = 5,
}

export enum PURCHASE_TYPE {
  /**
   * A type of SKU for in-app products.
   */
  INAPP = 'inapp',

  /**
   * A type of SKU for subscriptions.
   */
  SUBS = 'subs',
}

/**
 * Enum for billing features.
 * Currently, these are only relevant for Google Play Android users:
 * https://developer.android.com/reference/com/android/billingclient/api/BillingClient.FeatureType
 */
export enum BILLING_FEATURE {
  /**
   * Purchase/query for subscriptions.
   */
  SUBSCRIPTIONS,

  /**
   * Subscriptions update/replace.
   */
  SUBSCRIPTIONS_UPDATE,

  /**
   * Purchase/query for in-app items on VR.
   */
  IN_APP_ITEMS_ON_VR,

  /**
   * Purchase/query for subscriptions on VR.
   */
  SUBSCRIPTIONS_ON_VR,

  /**
   * Launch a price change confirmation flow.
   */
  PRICE_CHANGE_CONFIRMATION,
}
export enum PRORATION_MODE {
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
  DEFERRED = 4,
}

export enum PACKAGE_TYPE {
  /**
   * A package that was defined with a custom identifier.
   */
  UNKNOWN = 'UNKNOWN',

  /**
   * A package that was defined with a custom identifier.
   */
  CUSTOM = 'CUSTOM',

  /**
   * A package configured with the predefined lifetime identifier.
   */
  LIFETIME = 'LIFETIME',

  /**
   * A package configured with the predefined annual identifier.
   */
  ANNUAL = 'ANNUAL',

  /**
   * A package configured with the predefined six month identifier.
   */
  SIX_MONTH = 'SIX_MONTH',

  /**
   * A package configured with the predefined three month identifier.
   */
  THREE_MONTH = 'THREE_MONTH',

  /**
   * A package configured with the predefined two month identifier.
   */
  TWO_MONTH = 'TWO_MONTH',

  /**
   * A package configured with the predefined monthly identifier.
   */
  MONTHLY = 'MONTHLY',

  /**
   * A package configured with the predefined weekly identifier.
   */
  WEEKLY = 'WEEKLY',
}

export enum INTRO_ELIGIBILITY_STATUS {
  /**
   * doesn't have enough information to determine eligibility.
   */
  INTRO_ELIGIBILITY_STATUS_UNKNOWN = 0,
  /**
   * The user is not eligible for a free trial or intro pricing for this product.
   */
  INTRO_ELIGIBILITY_STATUS_INELIGIBLE,
  /**
   * The user is eligible for a free trial or intro pricing for this product.
   */
  INTRO_ELIGIBILITY_STATUS_ELIGIBLE,
}

export interface Transaction {
  /**
   * Id associated to the transaction.
   */
  readonly transactionId: string;
  /**
   * Receipt data for validation (iOS only - base64 encoded receipt)
   */
  readonly receipt?: string;
  /**
   * An optional obfuscated identifier that uniquely associates the transaction with a user account in your app.
   *
   * PURPOSE:
   * - Fraud detection: Helps platforms detect irregular activity (e.g., many devices purchasing on the same account)
   * - User linking: Links purchases to in-game characters, avatars, or in-app profiles
   *
   * PLATFORM DIFFERENCES:
   * - iOS: Must be a valid UUID format (e.g., "550e8400-e29b-41d4-a716-446655440000")
   *        Apple's StoreKit 2 requires UUID format for the appAccountToken parameter
   * - Android: Can be any obfuscated string (max 64 chars), maps to Google Play's ObfuscatedAccountId
   *           Google recommends using encryption or one-way hash
   *
   * SECURITY REQUIREMENTS (especially for Android):
   * - DO NOT store Personally Identifiable Information (PII) like emails in cleartext
   * - Use encryption or a one-way hash to generate an obfuscated identifier
   * - Maximum length: 64 characters (both platforms)
   * - Storing PII in cleartext will result in purchases being blocked by Google Play
   *
   * IMPLEMENTATION EXAMPLE:
   * ```typescript
   * // For iOS: Generate a deterministic UUID from user ID
   * import { v5 as uuidv5 } from 'uuid';
   * const NAMESPACE = '6ba7b810-9dad-11d1-80b4-00c04fd430c8'; // Your app's namespace UUID
   * const appAccountToken = uuidv5(userId, NAMESPACE);
   *
   * // For Android: Can also use UUID or any hashed value
   * // The same UUID approach works for both platforms
   * ```
   */
  readonly appAccountToken?: string | null;
  /**
   * Product Id associated with the transaction.
   */
  readonly productIdentifier: string;
  /**
   * Purchase date of the transaction in ISO 8601 format.
   */
  readonly purchaseDate: string;
  /**
   * Original purchase date of the transaction in ISO 8601 format (for subscriptions).
   */
  readonly originalPurchaseDate?: string;
  /**
   * Expiration date of the transaction in ISO 8601 format (for subscriptions).
   */
  readonly expirationDate?: string;
  /**
   * Whether the transaction is still active/valid.
   */
  readonly isActive?: boolean;
  /**
   * Whether the subscription will be cancelled at the end of the billing cycle, or null if not cancelled. Only available on iOS.
   */
  readonly willCancel: boolean | null;
  /**
   * Purchase state of the transaction.
   */
  readonly purchaseState?: string;
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
   * Quantity purchased.
   */
  readonly quantity?: number;
  /**
   * Product type (inapp or subs).
   */
  readonly productType?: string;
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
  restorePurchases(): Promise<void>;

  /**
   * Started purchase process for the given product.
   *
   * @param options - The product to purchase
   * @param options.productIdentifier - The product identifier of the product you want to purchase.
   * @param options.productType - Only Android, the type of product, can be inapp or subs. Will use inapp by default.
   * @param options.planIdentifier - Only Android, the identifier of the base plan you want to purchase from Google Play Console. REQUIRED for Android subscriptions, ignored on iOS.
   * @param options.quantity - Only iOS, the number of items you wish to purchase. Will use 1 by default.
   * @param options.appAccountToken - Optional identifier uniquely associated with the user's account in your app.
   *                                  PLATFORM REQUIREMENTS:
   *                                  - iOS: Must be a valid UUID format (StoreKit 2 requirement)
   *                                  - Android: Can be any obfuscated string (max 64 chars), maps to ObfuscatedAccountId
   *                                  SECURITY: DO NOT use PII like emails in cleartext - use UUID or hashed value.
   *                                  RECOMMENDED: Use UUID v5 with deterministic generation for cross-platform compatibility.
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
  getProducts(options: { productIdentifiers: string[]; productType?: PURCHASE_TYPE }): Promise<{ products: Product[] }>;

  /**
   * Gets the product info for a single product identifier.
   *
   * @param options - The product identifier you wish to retrieve information for
   * @param options.productIdentifier - The product identifier
   * @param options.productType - Only Android, the type of product, can be inapp or subs. Will use inapp by default.
   * @returns - The requested product info
   */
  getProduct(options: { productIdentifier: string; productType?: PURCHASE_TYPE }): Promise<{ product: Product }>;

  /**
   * Check if billing is supported for the current device.
   *
   *
   */
  isBillingSupported(): Promise<{ isBillingSupported: boolean }>;
  /**
   * Get the native Capacitor plugin version
   *
   * @returns {Promise<{ id: string }>} an Promise with version for this device
   * @throws An error if the something went wrong
   */
  getPluginVersion(): Promise<{ version: string }>;

  /**
   * Gets all the user's purchases (both in-app purchases and subscriptions).
   * This method queries the platform's purchase history for the current user.
   *
   * @param options - Optional parameters for filtering purchases
   * @param options.productType - Only Android, filter by product type (inapp or subs). If not specified, returns both types.
   * @param options.appAccountToken - Optional filter to restrict results to purchases that used the provided account token.
   *                                   Must be the same identifier used during purchase (UUID format for iOS, any obfuscated string for Android).
   *                                   iOS: UUID format required. Android: Maps to ObfuscatedAccountId.
   * @returns {Promise<{ purchases: Transaction[] }>} Promise that resolves with array of user's purchases
   * @throws An error if the purchase query fails
   * @since 7.2.0
   */
  getPurchases(options?: {
    productType?: PURCHASE_TYPE;
    appAccountToken?: string;
  }): Promise<{ purchases: Transaction[] }>;

  /**
   * Opens the platform's native subscription management page.
   * This allows users to view, modify, or cancel their subscriptions.
   *
   * - iOS: Opens the App Store subscription management page for the current app
   * - Android: Opens the Google Play subscription management page
   *
   * @returns {Promise<void>} Promise that resolves when the management page is opened
   * @throws An error if the subscription management page cannot be opened
   * @since 7.10.0
   */
  manageSubscriptions(): Promise<void>;

  /**
   * Listen for StoreKit transaction updates delivered by Apple's Transaction.updates.
   * Fires on app launch if there are unfinished transactions, and for any updates afterward.
   * iOS only.
   */
  addListener(
    eventName: 'transactionUpdated',
    listenerFunc: (transaction: Transaction) => void,
  ): Promise<PluginListenerHandle>;

  /** Remove all registered listeners */
  removeAllListeners(): Promise<void>;
}
