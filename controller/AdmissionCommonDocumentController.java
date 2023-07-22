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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.AdmissionCommonDocument;
import com.iskool.entity.DocumentMaster;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AdmissionCommonDocumentService;
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
@RequestMapping("/api/admission/common/document")
@Api(value = "DocumentMaster Rest API", produces = "application/json", consumes = "application/json")
public class AdmissionCommonDocumentController {

	private static final Logger logger = Logger.getLogger(AdmissionCommonDocumentController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull AdmissionCommonDocumentService admissionCommonDocumentService;

	private @NonNull MessagePropertyService messageSource;

	private @NonNull DocumentMasterService documentMasterService;

	@ApiOperation(value = "Allows to fetch all document master.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<AdmissionCommonDocument> docList = admissionCommonDocumentService.findAll();
		docList.sort((o1, o2) -> o1.getDocumentName().compareToIgnoreCase(o2.getDocumentName()));

		try {
			return responseGenerator.successGetResponse(context,
					messageSource.getMessage("admission.common.document.get"), docList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create admission common document.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The DocumentMaster request payload") @RequestBody AdmissionCommonDocument request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request || null == request.getDocumentId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		DocumentMaster documentMaster = new DocumentMaster();
		documentMaster.setDocumentId(request.getDocumentId());

		Optional<DocumentMaster> documentOptional = documentMasterService.findById(request.getDocumentId());
		if (!documentOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("document.invalid.id"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<AdmissionCommonDocument> admissionCommonDocumentOptional = admissionCommonDocumentService
				.findByDocumentMasterObj(documentMaster);
		if (admissionCommonDocumentOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("document.id.exist"),
					HttpStatus.BAD_REQUEST);
		}

		AdmissionCommonDocument obj = new AdmissionCommonDocument();
		obj.setDocumentMasterObj(documentOptional.get());
		obj.setStatus(Status.ACTIVE);
		admissionCommonDocumentService.saveOrUpdate(obj);

		try {
			return responseGenerator.successResponse(context,
					messageSource.getMessage("admission.common.document.create"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specific admission common document id.", response = Response.class)
	@DeleteMapping(value = "/delete/{id}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "admission common document id") @PathVariable("id") UUID id,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		AdmissionCommonDocument obj = admissionCommonDocumentService.findById(id).get();

		if (null == obj) {
			return responseGenerator.errorResponse(context,
					messageSource.getMessage("admission.common.document.id.invalid"), HttpStatus.BAD_REQUEST);
		}
		try {
			admissionCommonDocumentService.deleteById(id);
			return responseGenerator.successResponse(context,
					messageSource.getMessage("admission.common.document.delete"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context,
					messageSource.getMessage("admission.common.document.id.invalid"), HttpStatus.BAD_REQUEST);

		}

	}
}
