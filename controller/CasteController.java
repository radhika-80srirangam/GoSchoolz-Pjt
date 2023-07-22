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

import com.iskool.entity.Caste;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CasteService;
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
@RequestMapping("/api/caste")
@Api(value = "Trading: Section Rest API", produces = "application/json", consumes = "application/json")
public class CasteController  {
	private static final Logger logger = Logger.getLogger(CasteController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CasteService casteService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new section.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Caste request payload") @RequestBody Caste request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<Caste>casteOptional=casteService.findByName(request.getName());
			if(casteOptional.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("caste.name", params),
						HttpStatus.BAD_REQUEST);
			}
		Caste casteObj = new Caste();
		casteObj.setName(request.getName());
		casteObj.setCreatedBy(request.getCreatedBy());
		casteObj.setStatus(Status.ACTIVE);
		casteService.save(casteObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("caste.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Caste.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Caste>castelist=casteService.findAll();
		castelist.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("caste.get"),
					castelist, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{casteId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("casteId") UUID casteId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Caste> caste= casteService.findById(casteId);
			if (!caste.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("caste.get"), caste.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing caste info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Caste request payload") @RequestBody Caste request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Caste> casteOptional = casteService.findById(request.getId());
		if (!casteOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!casteOptional.get().getName().equals(request.getName())) {
			Optional<Caste>casteOptionalObj=casteService.findByName(request.getName());
			if(casteOptionalObj.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("caste.name", params),
						HttpStatus.BAD_REQUEST);
			}
		}
			Caste casteObj = casteOptional.get();
			casteObj.setName(request.getName());
			casteObj.setModifiedBy(request.getModifiedBy());
			casteService.saveOrUpdate(casteObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("caste.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to delete specific caste id.", response = Response.class)
	@DeleteMapping(value = "/delete/{casteId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "Caste Id to be deleted") @PathVariable("casteId") UUID casteId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			Caste casteObj = casteService.findById(casteId).get();
			if (null == casteObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				casteService.deleteById(casteId);
				return responseGenerator.successResponse(context, messageSource.getMessage("caste.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("caste.invalid.delete"), HttpStatus.BAD_REQUEST);

			}
		}
}
	}