package com.civicbuild.controller;

import com.civicbuild.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mock")
public class MockServicesController {

    @GetMapping("/land-registry/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyLandTitle(@RequestParam String surveyNumber) {
        Map<String, Object> response = new HashMap<>();
        response.put("surveyNumber", surveyNumber);
        response.put("isTitleClear", true);
        response.put("cadastralZone", "Urban Standard Cadastre 4");
        response.put("encumbranceStatus", "NIL");
        response.put("registryOffice", "Central Municipal Land Records Bureau");
        response.put("verifiedTimestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success("Land records verified against municipal cadastre", response));
    }

    @PostMapping("/payment-gateway/process")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processPayment(
            @RequestBody Map<String, Object> paymentRequest) {

        boolean isSuccess = !Boolean.FALSE.equals(paymentRequest.get("mockSuccess"));
        Map<String, Object> gatewayResp = new HashMap<>();
        gatewayResp.put("transactionId", "GATEWAY-TX-" + System.currentTimeMillis());
        gatewayResp.put("amount", paymentRequest.get("amount"));
        gatewayResp.put("status", isSuccess ? "AUTHORIZED" : "DECLINED");
        gatewayResp.put("authCode", isSuccess ? "AUTH-OK-9821" : "ERR-DECLINED-402");
        gatewayResp.put("gatewayTimestamp", LocalDateTime.now());

        if (isSuccess) {
            return ResponseEntity.ok(ApiResponse.success("Gateway payment authorized", gatewayResp));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Transaction declined by gateway issuer"));
        }
    }
}
