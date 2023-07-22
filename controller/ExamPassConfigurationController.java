package com.iskool.controller;
import java.util.Date;
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

import com.iskool.entity.ExamPassConfiguration;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.ExamPassConfigurationService;
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
	@RequestMapping("/api/exampassconfiguration")
	@Api(value = "ExamPassConfigurationController: examPassConfiguration Rest API", produces = "application/json", consumes = "application/json")
	public class ExamPassConfigurationController {
		private static final Logger logger = Logger.getLogger(ExamPassConfigurationController.class);

		private static final UUID UUID = null;

		private @NonNull ResponseGenerator responseGenerator;

		private @NonNull ExamPassConfigurationService examPassConfigurationService;
		@Autowired
		MessagePropertyService messageSource;

		@ApiOperation(value = "Allows to create new examPassConfiguration.", response = Response.class)
		@PostMapping(value = "/create", produces = "application/json")
		public ResponseEntity<?> create(@ApiParam(value = "The ExamPassConfiguration request payload") @RequestBody ExamPassConfiguration request,
				@RequestHeader HttpHeaders httpHeader) throws Exception {
			TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
			if (null == request) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			ExamPassConfiguration examPassConfigurationObj = new ExamPassConfiguration();
			examPassConfigurationObj.setName(request.getName());
			examPassConfigurationObj.setBoardType(request.getBoardType());
			examPassConfigurationObj.setStatus(Status.ACTIVE);
			examPassConfigurationService.save(examPassConfigurationObj);
			try {
				return responseGenerator.successResponse(context, messageSource.getMessage("examPassConfiguration.create"),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		@ApiOperation(value = "Allows to fetch all ExamPassConfiguration.", response = Response.class)
		@GetMapping(value = "/get", produces = "application/json")
		public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
			TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("examPassConfiguration.get"),
						examPassConfigurationService.findAll(), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}

		@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
		@GetMapping(value = "/get/{examPassConfigurationId}", produces = "application/json")
		public ResponseEntity<?> get(@PathVariable("examPassConfigurationId") UUID examPassConfigurationId, @RequestHeader HttpHeaders httpHeader)
				throws Exception {
			TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
			try {
				Optional<ExamPassConfiguration> examPassConfiguration = examPassConfigurationService.findById(examPassConfigurationId);
				if (!examPassConfiguration.isPresent()) {
					return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
							HttpStatus.BAD_REQUEST);
				}
				return responseGenerator.successGetResponse(context, messageSource.getMessage("examPassConfiguration.get"), examPassConfiguration.get(),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		@ApiOperation(value = "Allows to update existing examPassConfiguration info.", response = Response.class)
		@PutMapping(value = "/update", produces = "application/json")
		public ResponseEntity<?> update(@ApiParam(value = "The ExamPassConfiguration request payload") @RequestBody ExamPassConfiguration request,
				@RequestHeader HttpHeaders httpHeader) throws Exception {
			TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
			if (null == request.getId()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}

			Optional<ExamPassConfiguration> examPassConfigurationOptional = examPassConfigurationService.findById(request.getId());
			if (!examPassConfigurationOptional.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			ExamPassConfiguration examPassConfigurationObj = examPassConfigurationOptional.get();
			examPassConfigurationObj.setName(request.getName());
			examPassConfigurationObj.setBoardType(request.getBoardType());
			examPassConfigurationService.saveOrUpdate(examPassConfigurationObj);
			try {
				return responseGenerator.successResponse(context, messageSource.getMessage("examPassConfiguration.update"),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		@ApiOperation(value = " Allows to delete specific examPassConfiguration id.", response = Response.class)
		@DeleteMapping(value = "/delete/{examPassConfigurationId}/{deletedBy}", produces = "application/json")
		public ResponseEntity<?> delete(
		@ApiParam(value = "examPassConfiguration Id to be deleted")
		@PathVariable("examPassConfigurationId") UUID examPassConfigurationId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
			TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
	{ 
		ExamPassConfiguration examPassConfigurationObj = examPassConfigurationService.findById(examPassConfigurationId).get();
		if (null == examPassConfigurationObj) { 
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
					    HttpStatus.BAD_REQUEST);
	} 
		examPassConfigurationObj.setStatus(Status.INACTIVE); 
		examPassConfigurationObj.setDeletedOn(new Date());
		examPassConfigurationObj.setDeletedBy(deletedBy); 
		examPassConfigurationService.saveOrUpdate(examPassConfigurationObj);
		try { 
			return responseGenerator.successResponse(context, messageSource.getMessage("examPassConfiguration.delete"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
						
		}
	}
	}
	}

