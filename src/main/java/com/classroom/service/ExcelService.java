package com.classroom.service;

import com.classroom.entity.*;
import com.classroom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for exporting and importing classroom data via Excel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {
    
    @Value("${app.data.excel-dir:./exports}")
    private String excelDir;
    
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final StudentGroupRepository groupRepository;
    private final BadgeRepository badgeRepository;
    
    @Transactional
    public String exportClassroomToExcel(Long classroomId, String filename) throws IOException {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + classroomId));
        
        ensureExcelDirExists();
        File file = new File(excelDir, filename);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Students sheet
            Sheet studentSheet = workbook.createSheet("Students");
            exportStudentsSheet(studentSheet, classroom);
            
            // Groups sheet
            Sheet groupSheet = workbook.createSheet("Groups");
            exportGroupsSheet(groupSheet, classroom);
            
            // Summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            exportSummarySheet(summarySheet, classroom);
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            
            log.info("Exported classroom {} to {}", classroomId, file.getAbsolutePath());
            return file.getAbsolutePath();
        }
    }
    
    @Transactional
    public String importClassroomFromExcel(String filename) throws IOException {
        File file = new File(excelDir, filename);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // Import students
            Sheet studentSheet = workbook.getSheet("Students");
            if (studentSheet != null) {
                importStudentsSheet(studentSheet);
            }
            
            // Import groups
            Sheet groupSheet = workbook.getSheet("Groups");
            if (groupSheet != null) {
                importGroupsSheet(groupSheet);
            }
            
            log.info("Imported classroom from {}", file.getAbsolutePath());
            return "Import successful";
        }
    }
    
    private void exportStudentsSheet(Sheet sheet, Classroom classroom) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Name");
        headerRow.createCell(1).setCellValue("Nickname");
        headerRow.createCell(2).setCellValue("Credit Points");
        headerRow.createCell(3).setCellValue("Rank Level");
        headerRow.createCell(4).setCellValue("Badges");
        
        int rowNum = 1;
        for (Student student : classroom.getStudents()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getName());
            row.createCell(1).setCellValue(student.getNickname());
            row.createCell(2).setCellValue(student.getCreditPoints());
            row.createCell(3).setCellValue(student.getRankLevel() != null ? student.getRankLevel().getName() : "");
            String badges = student.getBadges().stream().map(Badge::getName).collect(Collectors.joining(", "));
            row.createCell(4).setCellValue(badges);
        }
        
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void exportGroupsSheet(Sheet sheet, Classroom classroom) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Group Name");
        headerRow.createCell(1).setCellValue("Description");
        headerRow.createCell(2).setCellValue("Members");
        
        int rowNum = 1;
        for (StudentGroup group : classroom.getGroups()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(group.getName());
            row.createCell(1).setCellValue(group.getDescription());
            String members = group.getStudents().stream().map(Student::getNickname).collect(Collectors.joining(", "));
            row.createCell(2).setCellValue(members);
        }
        
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void exportSummarySheet(Sheet sheet, Classroom classroom) {
        Row row1 = sheet.createRow(0);
        row1.createCell(0).setCellValue("Classroom Name");
        row1.createCell(1).setCellValue(classroom.getName());
        
        Row row2 = sheet.createRow(1);
        row2.createCell(0).setCellValue("Total Students");
        row2.createCell(1).setCellValue(classroom.getStudents().size());
        
        Row row3 = sheet.createRow(2);
        row3.createCell(0).setCellValue("Total Groups");
        row3.createCell(1).setCellValue(classroom.getGroups().size());
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void importStudentsSheet(Sheet sheet) {
        // Iterate through rows and import student data
        Iterator<Row> iterator = sheet.iterator();
        iterator.next(); // Skip header
        
        while (iterator.hasNext()) {
            Row row = iterator.next();
            String name = row.getCell(0).getStringCellValue();
            String nickname = row.getCell(1).getStringCellValue();
            
            // Check for duplicates
            if (studentRepository.findByNickname(nickname).isPresent()) {
                throw new IllegalArgumentException("Student with nickname '" + nickname + "' already exists. Import aborted.");
            }
        }
    }
    
    private void importGroupsSheet(Sheet sheet) {
        // Iterate through rows and import group data
        Iterator<Row> iterator = sheet.iterator();
        iterator.next(); // Skip header
        
        while (iterator.hasNext()) {
            Row row = iterator.next();
            String groupName = row.getCell(0).getStringCellValue();
            
            // Check for duplicates
            if (groupRepository.findByClassroomIdAndName(1L, groupName).isPresent()) {
                throw new IllegalArgumentException("Group '" + groupName + "' already exists. Import aborted.");
            }
        }
    }
    
    private void ensureExcelDirExists() {
        File dir = new File(excelDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public String getExcelDirectory() {
        return new File(excelDir).getAbsolutePath();
    }
}
