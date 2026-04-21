package org.example.rhcamunda.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.rhcamunda.entity.Conge;
import org.example.rhcamunda.entity.Employe;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    @Transactional(readOnly = true)
    public byte[] exportEmployesToExcel(List<Employe> employes) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employés");

        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Matricule", "Nom", "Prénom", "Email", "Département", "Poste", "Date Embauche"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Employe e : employes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(e.getMatricule());
            row.createCell(1).setCellValue(e.getNom());
            row.createCell(2).setCellValue(e.getPrenom());
            row.createCell(3).setCellValue(e.getEmail());
            row.createCell(4).setCellValue(e.getDepartement() != null ? e.getDepartement().getNom() : "N/A");
            row.createCell(5).setCellValue(e.getPoste() != null ? e.getPoste().getTitre() : "N/A");
            row.createCell(6).setCellValue(e.getDateEmbauche() != null ? e.getDateEmbauche().format(formatter) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    @Transactional(readOnly = true)
    public byte[] exportCongesToExcel(List<Conge> conges) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Congés");

        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Employé", "Type", "Date Début", "Date Fin", "Jours", "Statut"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Conge c : conges) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getEmploye().getNom() + " " + c.getEmploye().getPrenom());
            row.createCell(2).setCellValue(c.getTypeConge());
            row.createCell(3).setCellValue(c.getDateDebut().format(formatter));
            row.createCell(4).setCellValue(c.getDateFin().format(formatter));
            row.createCell(5).setCellValue(c.getNbjours());
            row.createCell(6).setCellValue(c.getStatut().name());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbookToBytes(workbook);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private byte[] workbookToBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}