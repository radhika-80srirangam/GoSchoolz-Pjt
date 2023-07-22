
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

import com.iskool.entity.Batch;
import com.iskool.entity.Board;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.BatchService;
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
@RequestMapping("/api/batch")
@Api(value = "Batch: batch Rest API", produces = "application/json", consumes = "application/json")
public class BatchController {
	private static final Logger logger = Logger.getLogger(BatchController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull BatchService batchService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new batch.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Batch request payload") @RequestBody Batch request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Batch batchObj = new Batch();
		batchObj.setName(request.getName());
		batchObj.setStatus(Status.ACTIVE);
		batchService.save(batchObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("batch.create"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Batch.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Batch>batchList=batchService.findAll();
		batchList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("batch.get"),
					batchList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{batchId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("batchId") UUID batchId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Batch> batch = batchService.findById(batchId);
			if (!batch.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("batch.get"), batch.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing batch info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Batch request payload") @RequestBody Batch request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Batch> batchOptional = batchService.findById(request.getId());
		if (!batchOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Batch batchObj = batchOptional.get();
		batchObj.setName(request.getName());
		batchService.saveOrUpdate(batchObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("batch.update"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = " Allows to delete specific batch id.", response = Response.class)
	@DeleteMapping(value = "/delete/{batchId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "Batch Id to be deleted") @PathVariable("batchId") UUID batchId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			Batch batchObj = batchService.findById(batchId).get();
			
		
			if (null == batchObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			
			try {
				batchService.deleteById(batchId);
				return responseGenerator.successResponse(context, messageSource.getMessage("batch.delete"),
						HttpStatus.OK);
			}
			catch(Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("batch.invalid.delete"), HttpStatus.BAD_REQUEST);
			}
			
			
		}
	
	}}

