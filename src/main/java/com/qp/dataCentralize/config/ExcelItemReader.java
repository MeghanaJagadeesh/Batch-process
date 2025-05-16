package com.qp.dataCentralize.config;

import com.qp.dataCentralize.entity.Datas;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.ItemReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelItemReader implements ItemReader<Datas> {

    private String filePath;
    private Workbook workbook;
    private Sheet sheet;
    private int currentRow = 1;
    private List<String> headers;

    public ExcelItemReader(String filePath) {
        this.filePath = filePath;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            headers = new ArrayList<>();
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        headers.add(cell.getStringCellValue().trim().toLowerCase());
                    } else {
                        headers.add(null);
                    }
                }
            }
        } catch (IOException e) {
            new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Datas read() {
        if (sheet == null || currentRow > sheet.getLastRowNum()) {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                new RuntimeException(e.getMessage());
            }
            return null;
        }

        Row row = sheet.getRow(currentRow++);
        if (row == null) {
            return read();
        }
        Datas data = new Datas();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Cell cell = row.getCell(i);

            if (header != null && cell != null) {
                String cellValue = getCellValueAsString(cell);
				switch (header) {
                    case "name":
                        data.setName(cellValue);
                        break;
                    case "email":
                        data.setEmail(cellValue);
                        break;
                    case "phone number":
                        System.out.println(data);
                        data.setPhoneNumber(cellValue);
                        break;
                    case "category":
                        data.setCategory(cellValue);
                        break;
                    case "designation":
                        data.setDesignation(cellValue);
                        break;
                    case "address":
                        data.setAddress(cellValue);
                        break;
                    case "company name":
                        data.setCompanyName(cellValue);
                        break;
                    case "industry type":
                        data.setIndustryType(cellValue);
                        break;
                }
            }
        }
        return data;
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().toLowerCase();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula().toLowerCase();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
