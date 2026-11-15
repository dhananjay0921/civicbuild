package com.civicbuild.service;

import com.civicbuild.entity.Payment;
import com.civicbuild.entity.User;
import com.civicbuild.enums.PaymentMethod;

public interface PaymentService {
    Payment getPaymentForApplication(Long applicationId, User user);
    Payment initiatePayment(Long applicationId, PaymentMethod method, User user);
    Payment confirmPayment(Long applicationId, String paymentReference, Boolean mockSuccess, User user);
}
