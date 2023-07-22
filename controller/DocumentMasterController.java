package com.iskool.controller;

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

import com.iskool.entity.DocumentMaster;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.DocumentMasterService;
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
@RequestMapping("/api/document")
@Api(value = "DocumentMaster Rest API", produces = "application/json", consumes = "application/json")
public class DocumentMasterController {

	private static final Logger logger = Logger.getLogger(DocumentMasterController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull DocumentMasterService documentMasterService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all document master.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<DocumentMaster> docList = documentMasterService.findAll();
		docList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("documentMaster.get"),
					docList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all documentMaster types.", response = Response.class)
	@GetMapping(value = "/get/{documentMasterId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("documentMasterId") UUID documentMasterId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<DocumentMaster> documentMaster = documentMasterService.findById(documentMasterId);
			if (!documentMaster.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("documentMaster.get"),
					documentMaster.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new documentMaster.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The DocumentMaster request payload") @RequestBody DocumentMaster request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<DocumentMaster> documentMasterOptional = documentMasterService.findByName(request.getName());
		if (documentMasterOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("documentMaster.name", params),
					HttpStatus.BAD_REQUEST);
		}
		DocumentMaster documentMasterObj = new DocumentMaster();
		documentMasterObj.setName(request.getName());
		documentMasterObj.setStatus(Status.ACTIVE);
		documentMasterService.saveOrUpdate(documentMasterObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("documentMaster.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing DocumentMaster info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The DocumentMaster request payload") @RequestBody DocumentMaster request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getDocumentId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<DocumentMaster> documentMasterOptional = documentMasterService.findById(request.getDocumentId());
		if (!documentMasterOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		if (!documentMasterOptional.get().getName().equals(request.getName())) {

			Optional<DocumentMaster> doucumentNameOptional = documentMasterService.findByName(request.getName());
			if (doucumentNameOptional.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("documentMaster.name", params),
						HttpStatus.BAD_REQUEST);
			}
		}
		DocumentMaster documentMasterObj = documentMasterOptional.get();
		documentMasterObj.setName(request.getName());
		documentMasterService.saveOrUpdate(documentMasterObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("documentMaster.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specific DocumentMaster id.", response = Response.class)
	@DeleteMapping(value = "/delete/{documentMasterId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "DocumentMaster Id to be deleted") @PathVariable("documentMasterId") UUID documentMasterId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			DocumentMaster documentMasterObj = documentMasterService.findById(documentMasterId).get();

			if (null == documentMasterObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				documentMasterService.deleteById(documentMasterId);
				return responseGenerator.successResponse(context, messageSource.getMessage("documentMaster.delete"),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context,
						messageSource.getMessage("documentMaster.invalid.delete"), HttpStatus.BAD_REQUEST);

			}

		}

	}
}
