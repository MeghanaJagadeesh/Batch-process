//package com.qp.dataCentralize.config;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.NonTransientResourceException;
//import org.springframework.batch.item.ParseException;
//import org.springframework.batch.item.UnexpectedInputException;
//
//import com.qp.dataCentralize.entity.Datas;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class ExcelItemReader implements ItemReader<Datas> {
//
//	private String filePath;
//	private Workbook workbook;
//	private Sheet sheet;
//	private int currentRow = 1; 
//	private List<String> headers; 
//
//	public ExcelItemReader(String filePath) {
//		this.filePath = filePath;
//		System.err.println(filePath);
//		try (FileInputStream fis = new FileInputStream(filePath)) {
//			workbook = new XSSFWorkbook(fis); // Use XSSFWorkbook for .xlsx files
//			sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
//
//			Row headerRow = sheet.getRow(0);
//			System.out.println("header row "+headerRow);
//			headers = new ArrayList<>();
//			System.out.println("header row num "+headerRow.getLastCellNum());
//			if (headerRow != null) {
//				for (int i = 0; i < headerRow.getLastCellNum(); i++) {
//					Cell cell = headerRow.getCell(i);
//					System.err.println("cell "+cell);
//					if (cell != null) {
//						headers.add(cell.getStringCellValue().trim().toLowerCase()); // Store headers in lowercase
//					} else {
//						headers.add(null); // Handle empty header cells
//					}
//				}
//			}
//			System.err.println(headers);
//			System.out.println("after loop");
//			log.info("Excel Headers: {}", headers);
//
//		} catch (IOException e) {
//			log.error("Error opening Excel file: {}", filePath, e);
//			throw new IllegalStateException("Error opening Excel file: " + filePath, e); // Stop the job
//		}
//	}
//
//	@Override
//	public Datas read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//		System.out.println("read file");
//		if (sheet == null || currentRow > sheet.getLastRowNum()) {
//			try {
//				if (workbook != null) {
//					workbook.close();
//				}
//			} catch (IOException e) {
//				log.error("Error closing workbook", e);
//			}
//			return null; 
//		}
//
//		Row row = sheet.getRow(currentRow++);
//		if (row == null) {
//			return read();
//		}
//
//		Datas data = new Datas();
//		for (int i = 0; i < headers.size(); i++) {
//			String header = headers.get(i);
//			Cell cell = row.getCell(i);
//
//			if (header != null && cell != null) {
//				String cellValue = getCellValueAsString(cell); 
//				System.out.println(header);
//				switch (header) {
//				case "name":
//					data.setName(cellValue);
//					break;
//				case "email":
//					data.setEmail(cellValue);
//					break;
//				case "phone number":
//					data.setPhoneNumber(cellValue);
//					break;
//				case "category":
//					data.setCategory(cellValue);
//					break;
//				case "designation":
//					data.setDesignation(cellValue);
//					break;
//				case "address":
//					data.setAddress(cellValue);
//					break;
//				case "company name":
//					data.setCompanyName(cellValue);
//					break;
//				case "industry type":
//					data.setIndustryType(cellValue);
//					break;
//
//				}
//			}
//		}
//
//		log.debug("Read data: {}", data);
//		return data;
//	}
//
//	private String getCellValueAsString(Cell cell) {
//		switch (cell.getCellType()) {
//		case STRING:
//			return cell.getStringCellValue();
//		case NUMERIC:
//			return String.valueOf(cell.getNumericCellValue()); // Convert numeric to string
//		case BOOLEAN:
//			return String.valueOf(cell.getBooleanCellValue());
//		case FORMULA:
//			return cell.getCellFormula();
//		case BLANK:
//			return ""; // or null, depending on your needs
//		default:
//			return ""; // or null, depending on your needs
//		}
//	}
//}

package com.qp.dataCentralize.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.ItemReader;

import com.qp.dataCentralize.entity.Datas;


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
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		case BLANK:
			return "";
		default:
			return "";
		}
	}
}
