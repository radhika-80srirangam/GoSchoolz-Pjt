package com.iskool.controller;

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

import com.iskool.entity.AcadamicBatch;
import com.iskool.entity.Board;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcadamicBatchService;
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
@RequestMapping("/api/acadamicbatch")
@Api(value = "AcademicBatch: academicbatch Rest API", produces = "application/json", consumes = "application/json")
public class AcadamicBatchController {
	private static final Logger logger = Logger.getLogger(AcadamicBatchController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull AcadamicBatchService acadamicBatchService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new academicBatch.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The AcademicBatch request payload") @RequestBody AcadamicBatch request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		AcadamicBatch academicBatchObj = new AcadamicBatch();
		academicBatchObj.setName(request.getName());
		academicBatchObj.setStatus(Status.ACTIVE);
		acadamicBatchService.save(academicBatchObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("academicBatch.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch all academicBatch.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<AcadamicBatch>acaBatchList=acadamicBatchService.findAll();
		acaBatchList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("academicBatch.get"),
					acaBatchList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all academic batch types.", response = Response.class)
	@GetMapping(value = "/get/{acadamicBatchId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("acadamicBatchId") UUID acadamicBatchId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<AcadamicBatch> academicBatch = acadamicBatchService.findById(acadamicBatchId);
			if (!academicBatch.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("academicBatch.get"), academicBatch.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to update existing academicBatch info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The AcademicBatch request payload") @RequestBody AcadamicBatch request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<AcadamicBatch> academicBatchOptional = acadamicBatchService.findById(request.getId());
		if (!academicBatchOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		AcadamicBatch academicBatchObj = academicBatchOptional.get();
		academicBatchObj.setName(request.getName());
		acadamicBatchService.saveOrUpdate(academicBatchObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("academicBatch.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = " Allows to delete specific academicBatch id.", response = Response.class)
	@DeleteMapping(value = "/delete/{acadamicBatchId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
	@ApiParam(value = "AcademicBatch Id to be deleted")
	@PathVariable("acadamicBatchId") UUID acadamicBatchId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
{ 
	AcadamicBatch academicBatchObj = acadamicBatchService.findById(acadamicBatchId).get();
	if (null == academicBatchObj) { 
		return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
				    HttpStatus.BAD_REQUEST);
} 
	try {
		acadamicBatchService.deleteById(acadamicBatchId);
		return responseGenerator.successResponse(context, messageSource.getMessage("acadamicBatch.delete"),HttpStatus.OK);
	} catch (Exception e) {
		e.printStackTrace();
		logger.error(e.getMessage(), e);
		return responseGenerator.errorResponse(context, messageSource.getMessage("acadamicBatch.invalid.delete"), HttpStatus.BAD_REQUEST);

	}
}
}
}

