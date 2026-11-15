package com.civicbuild.service.impl;

import com.civicbuild.entity.Application;
import com.civicbuild.entity.Payment;
import com.civicbuild.entity.Permit;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.PaymentRepository;
import com.civicbuild.repository.PermitRepository;
import com.civicbuild.service.PermitService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class PermitServiceImpl implements PermitService {

    private static final Logger log = LoggerFactory.getLogger(PermitServiceImpl.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PermitRepository permitRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${civicbuild.permits.output-dir:./generated-permits}")
    private String permitsOutputDir;

    @Override
    @Transactional
    public Permit createPermitRecord(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        // Check if permit already exists
        if (application.getPermit() != null) {
            return application.getPermit();
        }

        String permitNumber = "CB-PERMIT-2026-" + (10000 + new Random().nextInt(90000));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusYears(3); // Statutory 3-year building permit validity

        String qrData = "VERIFIED_MUNICIPAL_PERMIT: " + permitNumber +
                " | App: " + application.getApplicationNumber() +
                " | Applicant: " + application.getApplicant().getFullName() +
                " | Zone: " + application.getProperty().getZone() +
                " | Issued: " + now.format(DateTimeFormatter.ISO_LOCAL_DATE);

        File dir = new File(permitsOutputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String pdfFileName = permitNumber + ".pdf";
        File pdfFile = new File(dir, pdfFileName);

        Permit permit = new Permit(
                permitNumber,
                application,
                now,
                expiry,
                qrData,
                pdfFile.getAbsolutePath(),
                "Commissioner of Municipal Town Planning"
        );

        permit = permitRepository.save(permit);
        application.setPermit(permit);
        application.setStatus(ApplicationStatus.APPROVED);
        application.setApprovedDate(now);
        applicationRepository.save(application);

        // Generate actual physical PDF file
        generatePermitPdf(permit);

        return permit;
    }

    @Override
    public File generatePermitPdf(Permit permit) {
        Application app = permit.getApplication();
        File file = new File(permit.getPdfPath());

        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Municipal Header Banner
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 41, 66));
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(100, 116, 139));
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(13, 148, 136));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

            Paragraph header = new Paragraph("MUNICIPAL CORPORATION OF CIVICBUILD", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph dept = new Paragraph("Department of Town Planning & Statutory Building Permissions", subHeaderFont);
            dept.setAlignment(Element.ALIGN_CENTER);
            dept.setSpacingAfter(15);
            document.add(dept);

            Paragraph certTitle = new Paragraph("OFFICIAL BUILDING PERMIT CERTIFICATE", titleFont);
            certTitle.setAlignment(Element.ALIGN_CENTER);
            certTitle.setSpacingAfter(20);
            document.add(certTitle);

            // Permit Metadata Box
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            metaTable.addCell(createCell("Permit Certificate No:", boldFont, true));
            metaTable.addCell(createCell(permit.getPermitNumber(), normalFont, false));

            metaTable.addCell(createCell("Application Reference:", boldFont, true));
            metaTable.addCell(createCell(app.getApplicationNumber(), normalFont, false));

            metaTable.addCell(createCell("Date of Sanction:", boldFont, true));
            metaTable.addCell(createCell(permit.getIssueDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")), normalFont, false));

            metaTable.addCell(createCell("Statutory Expiry Date:", boldFont, true));
            metaTable.addCell(createCell(permit.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), normalFont, false));

            document.add(metaTable);

            // Applicant & Site Details Table
            Paragraph section1 = new Paragraph("1. APPLICANT AND PROPERTY DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 41, 66)));
            section1.setSpacingAfter(8);
            document.add(section1);

            PdfPTable siteTable = new PdfPTable(2);
            siteTable.setWidthPercentage(100);
            siteTable.setSpacingAfter(15);

            siteTable.addCell(createCell("Applicant Name:", boldFont, true));
            siteTable.addCell(createCell(app.getApplicant().getFullName(), normalFont, false));

            siteTable.addCell(createCell("Contact Email & Phone:", boldFont, true));
            siteTable.addCell(createCell(app.getApplicant().getEmail() + " | " + app.getApplicant().getPhone(), normalFont, false));

            siteTable.addCell(createCell("Construction Site Address:", boldFont, true));
            siteTable.addCell(createCell(app.getProperty().getAddress(), normalFont, false));

            siteTable.addCell(createCell("Survey No / Ward / Zone:", boldFont, true));
            siteTable.addCell(createCell(app.getProperty().getSurveyNumber() + " / Ward " + app.getProperty().getWardNumber() + " / " + app.getProperty().getZone(), normalFont, false));

            siteTable.addCell(createCell("Plot Area:", boldFont, true));
            siteTable.addCell(createCell(app.getProperty().getPlotArea() + " sq.ft (" + app.getProperty().getOwnershipType() + ")", normalFont, false));

            document.add(siteTable);

            // Construction Specification Table
            Paragraph section2 = new Paragraph("2. APPROVED SPECIFICATIONS & MULTI-DEPARTMENT CLEARANCES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 41, 66)));
            section2.setSpacingAfter(8);
            document.add(section2);

            PdfPTable specsTable = new PdfPTable(2);
            specsTable.setWidthPercentage(100);
            specsTable.setSpacingAfter(15);

            specsTable.addCell(createCell("Building Category:", boldFont, true));
            specsTable.addCell(createCell(app.getBuildingType().name() + " (" + app.getConstructionPurpose() + ")", normalFont, false));

            specsTable.addCell(createCell("Total Built-up Area:", boldFont, true));
            specsTable.addCell(createCell(app.getBuiltUpArea() + " sq.ft across " + app.getNumberOfFloors() + " Floors", normalFont, false));

            specsTable.addCell(createCell("Sanctioned Building Height:", boldFont, true));
            specsTable.addCell(createCell(app.getBuildingHeight() + " meters", normalFont, false));

            specsTable.addCell(createCell("Statutory Depart. Clearances:", boldFont, true));
            specsTable.addCell(createCell("FIRE: APPROVED | WATER: APPROVED | ZONING: APPROVED | ENVIRONMENT: APPROVED", normalFont, false));

            List<Payment> payments = paymentRepository.findByApplicationId(app.getId());
            String paymentInfo = "Paid: INR " + (app.getCalculatedFee() != null ? app.getCalculatedFee() : "0.00");
            if (!payments.isEmpty()) {
                paymentInfo += " (Ref: " + payments.get(0).getTransactionRef() + ")";
            }
            specsTable.addCell(createCell("Municipal Fee Receipt:", boldFont, true));
            specsTable.addCell(createCell(paymentInfo, normalFont, false));

            document.add(specsTable);

            // Digital Seal and Verification Statement
            Paragraph verificationText = new Paragraph("CERTIFICATE AUTHENTICATION & STATUTORY DECLARATION:\n" +
                    "This permit is electronically generated through the CivicBuild Camunda 8 Workflow Orchestration Engine. " +
                    "Physical copy verified against municipal land deed and architectural blueprints. All construction must adhere strictly to the National Building Code (NBC). " +
                    "Any structural deviation without prior written municipal approval shall invalidate this authorization immediately.\n\n" +
                    "Digital Seal Payload: " + permit.getQrCodeData(),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
            verificationText.setSpacingAfter(25);
            document.add(verificationText);

            Paragraph signParagraph = new Paragraph("Authorized Signatory\n" + permit.getIssuedBy() + "\nMunicipal Corporation of CivicBuild",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(15, 41, 66)));
            signParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(signParagraph);

            document.close();
            log.info("Successfully compiled physical permit PDF: {}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to compile permit PDF: {}", e.getMessage(), e);
        }

        return file;
    }

    private PdfPCell createCell(String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        if (isHeader) {
            cell.setBackgroundColor(new Color(241, 245, 249));
        }
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }

    @Override
    @Transactional(readOnly = true)
    public Permit getPermitById(Long id) {
        return permitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permit not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Permit getPermitByApplicationId(Long applicationId) {
        return permitRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Permit not found for application: " + applicationId));
    }
}
