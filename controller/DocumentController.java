package com.iskool.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.Standard;
import com.iskool.entity.Student;
import com.iskool.entity.StudentAddressInformation;
import com.iskool.entity.StudentAdmissionCancellation;
import com.iskool.enumeration.AddressType;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StandardService;
import com.iskool.service.StudentAddressInformationService;
import com.iskool.service.StudentAdmissionCancellationService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.util.DateUtil;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/document/download")
@Api(value = "Document download Rest API", produces = "application/json", consumes = "application/json")
public class DocumentController {

	private static final Logger logger = Logger.getLogger(StudentFeesReceiptController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull StandardService standardService;

	private @NonNull StudentRegistrationService studentRegistrationService;
	
	private @NonNull StudentAddressInformationService studentAddressService;
	
	private @NonNull StudentAdmissionCancellationService studentAdmissionCancellationService;

	private @NonNull MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch student rollnumber report", response = Response.class)
	@GetMapping(value = "/get/student/rollnumber/report/{stdId}", produces = "application/json")
	public ResponseEntity<?> getDocumentListDownload(@PathVariable("stdId") UUID stdId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		if (null == stdId) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<Standard> standardOptional = standardService.findById(stdId);
		if (!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		List<Student> studentList = studentRegistrationService.findByStandardObjAndStatus(standardOptional.get(),
				Status.ACTIVE);
		
//		if(studentList == null || studentList.isEmpty()) {
//			String[] params=new String[] {standardOptional.get().getName()};
//			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.not.found",params),
//					HttpStatus.BAD_REQUEST);
//		}

		try {
			Resource resource = new ClassPathResource("Student_Rollnumber_Report.xlsx");
			InputStream fileStream = resource.getInputStream();

			XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
			XSSFSheet worksheet = workbook.getSheetAt(0);
			int rowvariable = 1;
			Cell cell = null;
			for (Student studentObj : studentList) {
				Row row = worksheet.createRow(rowvariable);
				
				cell = row.createCell(0);
				cell.setCellValue(studentObj.getRollNumber());

				cell = row.createCell(1);
				cell.setCellValue(studentObj.getRegistrationNumber());

				cell = row.createCell(2);
				cell.setCellValue(studentObj.getFirstName() + (null != studentObj.getMiddleName() ? " " + studentObj.getMiddleName() : "")
						+ " " + studentObj.getLastName());

				cell = row.createCell(3);
				cell.setCellValue(studentObj.getStatus().toString());

				cell = row.createCell(4);
				cell.setCellValue(studentObj.getGender().toString());

				cell = row.createCell(5);
				if(studentObj.getSectionObj()!= null) {
				cell.setCellValue(studentObj.getSectionObj().getName());
				}
				
				cell = row.createCell(6);
				cell.setCellValue(studentObj.getStandardObj().getName());

				rowvariable++;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();
			byte[] barray = bos.toByteArray();
			InputStream excelInputStream = new ByteArrayInputStream(barray);

			return ResponseEntity.ok().header("Access-Control-Expose-Headers", "Content-Disposition")
					.header("Content-Disposition", "attachment; filename=\"Student_Rollnumber_Report.xls\"")
					.body(new InputStreamResource(excelInputStream));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch student rollnumber report", response = Response.class)
	@GetMapping(value = "/get/admission/cancelled/report/{stdId}", produces = "application/json")
	public ResponseEntity<?> getAdmissionCancelledReport(@PathVariable("stdId") UUID stdId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		if (null == stdId) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<Standard> standardOptional = standardService.findById(stdId);
		if (!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		} 
		
		List<Student> studentList = studentRegistrationService.findByStandardObjAndStatus(standardOptional.get(),Status.INACTIVE);
		

//		if(studentList == null || studentList.isEmpty()) {
//			String[] params=new String[] {standardOptional.get().getName()};
//			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.not.found",params),
//					HttpStatus.BAD_REQUEST);
//		}

		try {
			Resource resource = new ClassPathResource("Admission_Cancelled_Report.xlsx");
			InputStream fileStream = resource.getInputStream();

			XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
			XSSFSheet worksheet = workbook.getSheetAt(0);
			int rowvariable = 1;
			Cell cell = null;
			for (Student studentObj : studentList) {
				Row row = worksheet.createRow(rowvariable);

				cell = row.createCell(0);
				cell.setCellValue(studentObj.getRegistrationNumber());

				cell = row.createCell(1);
				cell.setCellValue(studentObj.getRollNumber());
				
				cell = row.createCell(2);
				cell.setCellValue(studentObj.getFirstName() + (null != studentObj.getMiddleName() ? " " + studentObj.getMiddleName() : "")
						+ " " + studentObj.getLastName());

				cell = row.createCell(3);
				cell.setCellValue(studentObj.getStatus().toString());

				cell = row.createCell(4);
				
				cell.setCellValue(studentObj.getStandardObj().getName());
				

				cell = row.createCell(5);
				if(studentObj.getSectionObj()!= null) {
				cell.setCellValue(studentObj.getSectionObj().getName());
				}
				Optional<StudentAdmissionCancellation> studentAdmissionCancellationOptional = studentAdmissionCancellationService.findByRegistrationNumber(studentObj.getRegistrationNumber());
				cell = row.createCell(6);
				cell.setCellValue(studentAdmissionCancellationOptional.get().getRemark());

				cell = row.createCell(7);
				cell.setCellValue(DateUtil.getStrDate(studentObj.getDeletedOn()));

				cell = row.createCell(8);
				cell.setCellValue(studentObj.getDeletedBy());
				
				

				rowvariable++;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();
			byte[] barray = bos.toByteArray();
			InputStream excelInputStream = new ByteArrayInputStream(barray);

			return ResponseEntity.ok().header("Access-Control-Expose-Headers", "Content-Disposition")
					.header("Content-Disposition", "attachment; filename=\"Admission_Cancelled_Report.xlsx\"")
					.body(new InputStreamResource(excelInputStream));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch student rollnumber report", response = Response.class)
	@GetMapping(value ="/get/student/address/report/{stdId}", produces = "application/json")
	public ResponseEntity<?> getDocumentList
	(@PathVariable("stdId") UUID stdId,@RequestHeader HttpHeaders httpHeader )throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		if(null == stdId) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,HttpStatus.BAD_REQUEST);
		}
		Optional<Standard> standardOptional = standardService.findById(stdId);
		if(!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),HttpStatus.BAD_REQUEST);
		}
		
		List<Student> studentList = studentRegistrationService.findByStandardObjAndStatus( standardOptional.get(), Status.ACTIVE);
		

//		if(studentList == null || studentList.isEmpty()) {
//			String[] params=new String[] {standardOptional.get().getName()};
//			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.not.found",params),
//					HttpStatus.BAD_REQUEST);
//		}
		
		try {
			Resource resource = new ClassPathResource("Student_Address_Infomation.xlsx");
			InputStream fileStream = resource.getInputStream();

			XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
			XSSFSheet worksheet = workbook.getSheetAt(0);
			int rowvariable = 1;
			Cell cell = null;
			for (Student dto: studentList) {
				Row row = worksheet.createRow(rowvariable);

				cell = row.createCell(0);
				cell.setCellValue(dto.getFirstName() +( null != dto.getMiddleName() ? " "+dto.getMiddleName() : "" ) + " "+dto.getLastName());
				
				cell = row.createCell(1);
				cell.setCellValue(dto.getRegistrationNumber());
				
				cell = row.createCell(2);
				cell.setCellValue(dto.getRollNumber());
				
				cell = row.createCell(3);
				cell.setCellValue(dto.getStandardObj().getName());
				
				cell = row.createCell(4);
				if(dto.getSectionObj()!= null) {
				cell.setCellValue(dto.getSectionObj().getName());
				}
				
				Optional<StudentAddressInformation>	student = studentAddressService.findByRegistrationIdAndStatusAndAddressType(dto.getId(),Status.ACTIVE,AddressType.Local_Address);

				cell = row.createCell(5);
				cell.setCellValue(student.get().getCityObj().getName());
				
				cell = row.createCell(6);
				cell.setCellValue(student.get().getStateObj().getName());
				
				cell = row.createCell(7);
				cell.setCellValue(student.get().getCountryObj().getName());
				
				cell = row.createCell(8);
				cell.setCellValue(student.get().getPincode());
				
				cell = row.createCell(9);
				cell.setCellValue(student.get().getAddressType().toString());
				
				cell = row.createCell(10);
				cell.setCellValue(student.get().getAddress());
				
				cell = row.createCell(11);
				cell.setCellValue(dto.getMobileNumber());
					
				rowvariable++;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();
			byte[] barray = bos.toByteArray();
			InputStream excelInputStream = new ByteArrayInputStream(barray);

			return ResponseEntity.ok().header("Access-Control-Expose-Headers", "Content-Disposition")
					.header("Content-Disposition", "attachment; filename=\"Student_Address_Infomation.xlsx\"")
					.body(new InputStreamResource(excelInputStream));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}

	

