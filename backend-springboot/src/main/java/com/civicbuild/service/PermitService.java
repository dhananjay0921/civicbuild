package com.civicbuild.service;

import com.civicbuild.entity.Permit;
import java.io.File;

public interface PermitService {
    Permit createPermitRecord(Long applicationId);
    File generatePermitPdf(Permit permit);
    Permit getPermitById(Long id);
    Permit getPermitByApplicationId(Long applicationId);
}
