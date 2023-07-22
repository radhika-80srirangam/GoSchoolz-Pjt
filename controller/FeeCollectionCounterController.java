package com.iskool.controller;

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

import com.iskool.entity.FeeCollectionCounter;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.FeeCollectionCounterService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StateService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/fee/collection/counter")
@Api(value = "FeeCollectionCounter Rest API", produces = "application/json", consumes = "application/json")
public class FeeCollectionCounterController {

	private static final Logger logger = Logger.getLogger(FeeCollectionCounterController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull FeeCollectionCounterService feeCollectionCounterService;
	private @NonNull StateService stateService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all fee collection counter.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("fee.collection.counter.get"),
					feeCollectionCounterService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch fee collection counter by id.", response = Response.class)
	@GetMapping(value = "/get/{id}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("id") UUID id, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<FeeCollectionCounter> feeCollectionCounter = feeCollectionCounterService.findById(id);
			if (!feeCollectionCounter.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("fee.collection.counter.id.invalid"),
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("fee.collection.counter.get"),
					feeCollectionCounter.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new Fee Collection Counter.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The FeeCollectionCounter request payload") @RequestBody FeeCollectionCounter request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<FeeCollectionCounter> FeeCollectionCounterOptional = feeCollectionCounterService
				.findByName(request.getName());
		if (FeeCollectionCounterOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context,messageSource.getMessage("fee.collection.counter.name", params), HttpStatus.BAD_REQUEST);
		}
		FeeCollectionCounter FeeCollectionCounterObj = new FeeCollectionCounter();
		FeeCollectionCounterObj.setName(request.getName());
		FeeCollectionCounterObj.setShortName(request.getShortName());
		FeeCollectionCounterObj.setStatus(Status.ACTIVE);
		feeCollectionCounterService.saveOrUpdate(FeeCollectionCounterObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("fee.collection.counter.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing FeeCollectionCounter info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The FeeCollectionCounter request payload") @RequestBody FeeCollectionCounter request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<FeeCollectionCounter> FeeCollectionCounterOptional = feeCollectionCounterService.findById(request.getId());
		if (!FeeCollectionCounterOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,HttpStatus.BAD_REQUEST);
		}

		FeeCollectionCounter FeeCollectionCounterObj = FeeCollectionCounterOptional.get();
		if(!request.getName().equals(FeeCollectionCounterObj.getName())) {
			Optional<FeeCollectionCounter> FeeCollectionCounterOpt = feeCollectionCounterService.findByName(request.getName());
			if (FeeCollectionCounterOpt.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context,messageSource.getMessage("fee.collection.counter.name", params), HttpStatus.BAD_REQUEST);
			}
		}
		FeeCollectionCounterObj.setName(request.getName());
		FeeCollectionCounterObj.setShortName(request.getShortName());
		feeCollectionCounterService.saveOrUpdate(FeeCollectionCounterObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("fee.collection.counter.update"),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specfic fee collection counter.", response = Response.class)
	@DeleteMapping(value = "/delete/{id}", produces = "application/json")
	public ResponseEntity<?> delete(@PathVariable("id") UUID id, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		FeeCollectionCounter Obj = feeCollectionCounterService.findById(id).get();

		if (null == Obj) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
					HttpStatus.BAD_REQUEST);
		}
		try {
			feeCollectionCounterService.deleteById(id);
			return responseGenerator.successResponse(context, messageSource.getMessage("fee.collection.counter.delete"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context,
					messageSource.getMessage("fee.collection.counter.invalid.delete"), HttpStatus.BAD_REQUEST);

		}

	}

}
