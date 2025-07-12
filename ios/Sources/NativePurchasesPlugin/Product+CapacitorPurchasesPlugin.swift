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
            let unitString: String
            switch subscriptionPeriod.unit {
            case .day:
                unitString = "day"
            case .week:
                unitString = "week"
            case .month:
                unitString = "month"
            case .year:
                unitString = "year"
            }
            
            productDict["subscriptionPeriod"] = [
                "numberOfUnits": subscriptionPeriod.value,
                "unit": unitString
            ]
        }
        
        // Add introductory price if available
        if let introOffer = self.subscription?.introductoryOffer {
            let paymentModeString: String
            switch introOffer.paymentMode {
            case .payAsYouGo:
                paymentModeString = "payAsYouGo"
            case .payUpFront:
                paymentModeString = "payUpFront"
            case .freeTrial:
                paymentModeString = "freeTrial"
            default:
                paymentModeString = "Unknown"
            }
            
            let unitString: String
            switch introOffer.period.unit {
            case .day:
                unitString = "day"
            case .week:
                unitString = "week"
            case .month:
                unitString = "month"
            case .year:
                unitString = "year"
            }
            
            productDict["introductoryPrice"] = [
                "identifier": introOffer.id ?? "",
                "type": introOffer.type.rawValue,
                "price": introOffer.price,
                "priceString": introOffer.displayPrice,
                "currencySymbol": currencySymbol,
                "currencyCode": self.priceFormatStyle.currencyCode,
                "paymentMode": paymentModeString,
                "numberOfPeriods": introOffer.periodCount,
                "subscriptionPeriod": [
                    "numberOfUnits": introOffer.period.value,
                    "unit": unitString
                ]
            ]
        } else {
            productDict["introductoryPrice"] = NSNull()
        }
        
        // Add promotional offers (discounts)
        var discounts: [[String: Any]] = []
        if let promotionalOffers = self.subscription?.promotionalOffers {
            for offer in promotionalOffers {
                let paymentModeString: String
                switch offer.paymentMode {
                case .payAsYouGo:
                    paymentModeString = "payAsYouGo"
                case .payUpFront:
                    paymentModeString = "payUpFront"
                case .freeTrial:
                    paymentModeString = "freeTrial"
                default:
                    paymentModeString = "Unknown"
                }
                
                let unitString: String
                switch offer.period.unit {
                case .day:
                    unitString = "day"
                case .week:
                    unitString = "week"
                case .month:
                    unitString = "month"
                case .year:
                    unitString = "year"
                }
                
                discounts.append([
                    "identifier": offer.id,
                    "type": offer.type.rawValue,
                    "price": offer.price,
                    "priceString": offer.displayPrice,
                    "currencySymbol": currencySymbol,
                    "currencyCode": self.priceFormatStyle.currencyCode,
                    "paymentMode": paymentModeString,
                    "numberOfPeriods": offer.periodCount,
                    "subscriptionPeriod": [
                        "numberOfUnits": offer.period.value,
                        "unit": unitString
                    ]
                ])
            }
        }
        productDict["discounts"] = discounts
        
        return productDict
    }
}