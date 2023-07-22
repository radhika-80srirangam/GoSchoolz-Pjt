package com.iskool.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.AcademicYear;
import com.iskool.entity.Term;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.MessagePropertyService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/academic/year")
@Api(value = "AcademicYear Rest API", produces = "application/json", consumes = "application/json")
public class AcademicYearController {

	private static final Logger logger = Logger.getLogger(AcademicYearController.class);
	private @NonNull ResponseGenerator responseGenerator;
	private @NonNull AcademicYearService academicYearService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new academicyear.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")

	public ResponseEntity<?> create(
			@ApiParam(value = "The Academicyear request payload") @RequestBody AcademicYear request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<AcademicYear> yearOptional = academicYearService.findByName(request.getName());
		if (yearOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.name", params),
					HttpStatus.BAD_REQUEST);
		}
		
		Optional<AcademicYear> yearOptional2 = academicYearService.findByShortName(request.getShortName());
		if (yearOptional2.isPresent()) {
			String[] params = new String[] { request.getShortName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.name", params),
					HttpStatus.BAD_REQUEST);
		}
		
		
		
		Optional<AcademicYear> academicYearOptional = academicYearService.findBiggest();
		if(academicYearOptional.isPresent()) {
			if(request.getFromDate().compareTo(academicYearOptional.get().getToDate())<0) {
			
			return responseGenerator.errorResponse(context, messageSource.getMessage("date.invalid"),
					HttpStatus.BAD_REQUEST);
			}
			
		}
		
		
		if (request.getFromDate().equals(request.getToDate()) || request.getFromDate().after(request.getToDate())) {

			return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid"),
					HttpStatus.BAD_REQUEST);
		}
		
//		Optional<AcademicYear> yearOpt = academicYearService.findBiggest(yearObj.getToDate());
//		if(yearObj!=null)
//		{
//		if(request.getFromDate().before(yearOpt)) {
//
//			return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid"),
//					HttpStatus.BAD_REQUEST);
//			
//		}
//		}
		
		AcademicYear yearObj = new AcademicYear();
		yearObj.setName(request.getName());
		yearObj.setShortName(request.getShortName());
		yearObj.setFromDate(request.getFromDate());
		yearObj.setToDate(request.getToDate());
		yearObj.setStatus(Status.ACTIVE);
		academicYearService.saveOrUpdate(yearObj);
		
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("academic.year.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all academicyears.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<AcademicYear>academicYearList=academicYearService.findAll();
		academicYearList.sort((o1,o2)->o1.getFromDate().compareTo(o2.getFromDate()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("academic.year.get"),
					academicYearList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch specific academicyear.", response = Response.class)
	@GetMapping(value = "/get/{yearId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("yearId") UUID yearId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<AcademicYear> yearObj = academicYearService.findById(yearId);
			if (!yearObj.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("academic.year.get"),
					yearObj.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing academicyear.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The Academicyear request payload") @RequestBody AcademicYear request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<AcademicYear> yearOptional = academicYearService.findById(request.getId());
		if (!yearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!yearOptional.get().getName().equals(request.getName())) {
		Optional<AcademicYear> yearOptionalObj = academicYearService.findByName(request.getName());
		if (yearOptionalObj.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.name", params),
					HttpStatus.BAD_REQUEST);
		}
		}
		if(!yearOptional.get().getShortName().equals(request.getShortName())) {
			Optional<AcademicYear> yearOptionalObj = academicYearService.findByShortName(request.getShortName());
			if (yearOptionalObj.isPresent()) {
				String[] params = new String[] { request.getShortName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.name", params),
						HttpStatus.BAD_REQUEST);
			}
			}
		
//		Optional<AcademicYear> academicYearOptional = academicYearService.findBiggest();
//		if(academicYearOptional.isPresent()) {
//			if(request.getFromDate().compareTo(academicYearOptional.get().getToDate())<0) {
//			
//			return responseGenerator.errorResponse(context, messageSource.getMessage("date.invalid"),
//					HttpStatus.BAD_REQUEST);
//			}
//			
//		}
		
		if (request.getFromDate().equals(request.getToDate()) || request.getFromDate().after(request.getToDate())) {

			return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid"),
					HttpStatus.BAD_REQUEST);
		}
			AcademicYear yearObj = yearOptional.get();
	
			yearObj.setName(request.getName());
			yearObj.setShortName(request.getShortName());
			yearObj.setFromDate(request.getFromDate());
			yearObj.setToDate(request.getToDate());
			academicYearService.saveOrUpdate(yearObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("academic.year.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = " Allows to delete specific academicyear.", response = Response.class)
	@DeleteMapping(value = "/delete/{yearId}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "Year Id to be deleted") @PathVariable("yearId") UUID yearId,
			 @RequestHeader HttpHeaders httpHeader,Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			AcademicYear yearObj = academicYearService.findById(yearId).get();
			if (null == yearObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				academicYearService.deleteById(yearId);
				return responseGenerator.successResponse(context, messageSource.getMessage("academic.year.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.invalid.delete"), HttpStatus.BAD_REQUEST);

			}
		}
	}
}
