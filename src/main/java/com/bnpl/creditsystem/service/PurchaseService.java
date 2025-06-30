package com.bnpl.creditsystem.service;

import com.bnpl.creditsystem.dto.PurchaseRequest;
import com.bnpl.creditsystem.dto.PurchaseResponse;

public interface PurchaseService {

    PurchaseResponse processPurchase(PurchaseRequest request);
}