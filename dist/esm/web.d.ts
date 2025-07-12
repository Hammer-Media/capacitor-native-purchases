import { WebPlugin } from "@capacitor/core";
import type { NativePurchasesPlugin, Product, Transaction } from "./definitions";
export declare class NativePurchasesWeb extends WebPlugin implements NativePurchasesPlugin {
    restorePurchases(): Promise<{
        transactions: Transaction[];
    }>;
    getProducts(options: {
        productIdentifiers: string[];
    }): Promise<{
        products: Product[];
    }>;
    getProduct(options: {
        productIdentifier: string;
    }): Promise<{
        product: Product;
    }>;
    purchaseProduct(options: {
        productIdentifier: string;
        planIdentifier?: string;
        quantity?: number;
    }): Promise<Transaction>;
    isBillingSupported(): Promise<{
        isBillingSupported: boolean;
    }>;
    getPluginVersion(): Promise<{
        version: string;
    }>;
    getLatestSignedTransaction(): Promise<{
        jwt: string;
    }>;
    showManageSubscriptions(): Promise<void>;
}
