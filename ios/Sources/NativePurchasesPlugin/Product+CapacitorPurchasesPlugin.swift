//
//  Extensions.swift
//  CapgoCapacitorPurchases
//
//  Created by Martin DONADIEU on 2023-08-08.
//

import Foundation
import StoreKit

@available(iOS 15.0, *)
extension Product {

    var dictionary: [String: Any] {
        // Get currency symbol from the price formatter
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale.current
        let currencySymbol = formatter.currencySymbol ?? ""
        
        var productDict: [String: Any] = [
            "identifier": self.id,
            "description": self.description,
            "title": self.displayName,
            "price": self.price,
            "priceString": self.displayPrice,
            "currencyCode": self.priceFormatStyle.currencyCode,
            "currencySymbol": currencySymbol,
            "isFamilyShareable": self.isFamilyShareable
        ]
        
        // Add subscription group identifier if available
        if let subscriptionGroupID = self.subscription?.subscriptionGroupID {
            productDict["subscriptionGroupIdentifier"] = subscriptionGroupID
        } else {
            productDict["subscriptionGroupIdentifier"] = ""
        }
        
        // Add subscription period if available
        if let subscriptionPeriod = self.subscription?.subscriptionPeriod {
            productDict["subscriptionPeriod"] = [
                "numberOfUnits": subscriptionPeriod.value,
                "unit": mapSubscriptionPeriodUnit(subscriptionPeriod.unit)
            ]
        }
        
        // Add introductory price if available
        if let introOffer = self.subscription?.introductoryOffer {
            productDict["introductoryPrice"] = [
                "identifier": introOffer.id ?? "",
                "type": introOffer.type.rawValue,
                "price": introOffer.price,
                "priceString": introOffer.displayPrice,
                "currencySymbol": currencySymbol,
                "currencyCode": self.priceFormatStyle.currencyCode,
                "paymentMode": mapPaymentMode(introOffer.paymentMode),
                "numberOfPeriods": introOffer.periodCount,
                "subscriptionPeriod": [
                    "numberOfUnits": introOffer.period.value,
                    "unit": mapSubscriptionPeriodUnit(introOffer.period.unit)
                ]
            ]
        } else {
            productDict["introductoryPrice"] = NSNull()
        }
        
        // Add promotional offers (discounts)
        var discounts: [[String: Any]] = []
        if let promotionalOffers = self.subscription?.promotionalOffers {
            for offer in promotionalOffers {
                discounts.append([
                    "identifier": offer.id,
                    "type": offer.type.rawValue,
                    "price": offer.price,
                    "priceString": offer.displayPrice,
                    "currencySymbol": currencySymbol,
                    "currencyCode": self.priceFormatStyle.currencyCode,
                    "paymentMode": mapPaymentMode(offer.paymentMode),
                    "numberOfPeriods": offer.periodCount,
                    "subscriptionPeriod": [
                        "numberOfUnits": offer.period.value,
                        "unit": mapSubscriptionPeriodUnit(offer.period.unit)
                    ]
                ])
            }
        }
        productDict["discounts"] = discounts
        
        return productDict
    }
    
    // MARK: - Helper Methods for Legacy Format Conversion
    
    /// Maps StoreKit 2 subscription period unit to legacy integer format
    /// - Parameter unit: The StoreKit 2 period unit
    /// - Returns: Integer representation (0=day, 1=week, 2=month, 3=year)
    private func mapSubscriptionPeriodUnit(_ unit: Product.SubscriptionPeriod.Unit) -> Int {
        switch unit {
        case .day: return 0
        case .week: return 1
        case .month: return 2
        case .year: return 3
        @unknown default: return -1
        }
    }
    
    /// Maps StoreKit 2 payment mode to legacy integer format
    /// - Parameter mode: The StoreKit 2 payment mode
    /// - Returns: Integer representation (0=payAsYouGo, 1=payUpFront, 2=freeTrial)
    private func mapPaymentMode(_ mode: Product.SubscriptionOffer.PaymentMode) -> Int {
        switch mode {
        case .payAsYouGo: return 0
        case .payUpFront: return 1
        case .freeTrial: return 2
        @unknown default: return -1
        }
    }
}