# CIVICBUILD – FEEL (Friendly Enough Expression Language) Expression Dictionary

This dictionary documents all FEEL expressions utilized across the Camunda 8 BPMN process definitions, their execution context, variable inputs, expected output data types, and evaluation rules.

---

## 1. Gateway Routing & Approval Conditions

### Expression 1: All Department Reviews Approved
- **BPMN Element**: `Gateway_AllReviewsApproved` -> Sequence Flow `Flow_AllApproved`
- **FEEL Expression**:
  ```feel
  = every review in departmentReviews satisfies review.status = "APPROVED"
  ```
- **Context**: Evaluated immediately after the parallel Multi-Instance departmental review sub-process finishes all 4 concurrent instances.
- **Input Variables**:
  - `departmentReviews`: List of objects, each containing `{ "department": String, "status": String, "remarks": String }`.
- **Output**: `Boolean` (`true` if Fire, Water, Zoning, and Environmental all issued `APPROVED`).
- **Behavior**: If `true`, the workflow proceeds to `CallActivity_MunicipalFeeCalc`. If `false`, the default flow `Flow_AnyReviewRejected` triggers application rejection.

---

### Expression 2: Department Decision Routing
- **BPMN Element**: `Gateway_DeptDecision` -> Sequence Flow `Flow_DecisionRevision`
- **FEEL Expression**:
  ```feel
  = reviewDecision = "REVISION_REQUESTED"
  ```
- **Context**: Evaluated upon completion of a department officer user review task.
- **Input Variables**: `reviewDecision` (String: `"APPROVED"`, `"REJECTED"`, or `"REVISION_REQUESTED"`).
- **Behavior**: Diverts the sub-process instance into the embedded `BlueprintRevisionProcess` sub-process.

- **BPMN Element**: `Gateway_DeptDecision` -> Sequence Flow `Flow_DecisionRejected`
- **FEEL Expression**:
  ```feel
  = reviewDecision = "REVISION_REQUESTED"
  ```
- **Context**: Evaluated for immediate rejection by a department.
- **Output**: `Boolean`.

---

### Expression 3: Revision Cycle Counter Threshold
- **BPMN Element**: `Gateway_RevisionLimit` -> Sequence Flow `Flow_RevLimitValid`
- **FEEL Expression**:
  ```feel
  = revisionCount <= 3
  ```
- **Context**: Evaluated inside `BlueprintRevisionProcess` after the applicant uploads a corrected blueprint.
- **Input Variables**: `revisionCount` (Integer: 1, 2, 3, ...).
- **Behavior**: Permits up to 3 revision iterations. If `revisionCount > 3`, flow routes to `End_RevisionExceeded`, ending the application in rejection due to non-compliance.

---

### Expression 4: Payment Confirmation Gateway
- **BPMN Element**: `Gateway_PaymentSuccess` -> Sequence Flow `Flow_PaymentSuccess`
- **FEEL Expression**:
  ```feel
  = paymentStatus = "SUCCESS"
  ```
- **Context**: Evaluated after the `confirm-payment` service task validates the payment gateway token.
- **Input Variables**: `paymentStatus` (String: `"SUCCESS"`, `"FAILED"`, `"PENDING"`).
- **Behavior**: Directs the workflow to `Task_GeneratePermit`. If failed, diverts to `Flow_PaymentRetry` to re-prompt the applicant.

---

## 2. Multi-Instance Iteration & Collection FEEL Expressions

### Expression 5: Parallel Review Department Collection
- **BPMN Element**: `SubProcess_DepartmentReviews` (Multi-Instance Loop Characteristics)
- **Input Collection**:
  ```feel
  = departmentList
  ```
- **Input Element Variable**: `currentDept`
- **Value**: `["FIRE", "WATER", "ZONING", "ENVIRONMENTAL"]`
- **Output Collection**: `departmentReviews`
- **Output Element Expression**:
  ```feel
  = deptReviewResult
  ```

---

### Expression 6: Sequential Document Checklist Collection
- **BPMN Element**: `Task_VerifyDocumentSeq` in `DocumentVerificationProcess`
- **Input Collection**:
  ```feel
  = documents
  ```
- **Input Element Variable**: `doc`
- **Output Collection**: `documentResults`
- **Output Element Expression**:
  ```feel
  = docVerificationResult
  ```

---

## 3. Fee Calculation Rules (Illustrative Academic Demonstration)

The statutory fee calculation service implements the following municipal fee calculation formula:

```feel
totalFee = baseFee + (builtUpArea * ratePerSqFt) + (numberOfFloors * floorSurcharge)
```

Where parameter matrix:
- **Residential**:
  - `baseFee`: 5,000 INR
  - `ratePerSqFt`: 2.50 INR / sq.ft
  - `floorSurcharge`: 1,500 INR / floor
- **Commercial**:
  - `baseFee`: 15,000 INR
  - `ratePerSqFt`: 6.00 INR / sq.ft
  - `floorSurcharge`: 3,500 INR / floor
- **Industrial**:
  - `baseFee`: 25,000 INR
  - `ratePerSqFt`: 8.00 INR / sq.ft
  - `floorSurcharge`: 5,000 INR / floor
- **Institutional**:
  - `baseFee`: 10,000 INR
  - `ratePerSqFt`: 4.00 INR / sq.ft
  - `floorSurcharge`: 2,000 INR / floor
