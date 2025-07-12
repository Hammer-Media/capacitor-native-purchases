import { WebPlugin } from '@capacitor/core';

import type { NativePurchasesPlugin, Product, PURCHASE_TYPE, Transaction } from './definitions';

export class NativePurchasesWeb
  extends WebPlugin
  implements NativePurchasesPlugin
{
  async restorePurchases(): Promise<{ transactions: Transaction[] }> {
    console.error("restorePurchases only mocked in web");
    return { transactions: [] };
  }

  async getProducts(options: { productIdentifiers: string[] }): Promise<{ products: Product[] }> {
    console.error('getProducts only mocked in web ' + options);
    return { products: [] };
  }

  async getProduct(options: { productIdentifier: string }): Promise<{ product: Product }> {
    console.error('getProduct only mocked in web ' + options);
    return { product: {} as any };
  }

  async purchaseProduct(options: {
    productIdentifier: string;
    planIdentifier?: string;
    quantity?: number;
  }): Promise<Transaction> {
    console.error("purchaseProduct only mocked in web" + options);
    return {
      transactionId: "mock-transaction-id",
    };
  }

  async isBillingSupported(): Promise<{ isBillingSupported: boolean }> {
    console.error('isBillingSupported only mocked in web');
    return { isBillingSupported: false };
  }
  
  async getPluginVersion(): Promise<{ version: string }> {
    console.warn('Cannot get plugin version in web');
    return { version: 'default' };
  }

  async getPurchases(options?: {
    productType?: PURCHASE_TYPE;
  }): Promise<{ purchases: Transaction[] }> {
    console.error("getPurchases only mocked in web " + options);
    return { purchases: [] };
  }

  async getLatestSignedTransaction(): Promise<{ jwt: string }> {
    console.error("getLatestSignedTransaction only mocked in web");
    return { jwt: "mock-jwt-token" };
  }

  async showManageSubscriptions(): Promise<void> {
    console.error("showManageSubscriptions only mocked in web");
    // In web, you could redirect to your web subscription management page
    // window.open('https://your-app.com/manage-subscriptions', '_blank');
  }

  async manageSubscriptions(): Promise<void> {
    console.error("manageSubscriptions only mocked in web");
    // In web, you could redirect to your web subscription management page
    // window.open('https://your-app.com/manage-subscriptions', '_blank');
  }
}
