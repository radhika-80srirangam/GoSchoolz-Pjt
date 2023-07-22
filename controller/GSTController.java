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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.GST;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.GSTService;
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
@RequestMapping("/api/trading/gst")
@Api(value = "Trading: GST Rest API", produces = "application/json", consumes = "application/json")
public class GSTController {
	
	private static final Logger logger = Logger.getLogger(GSTController.class);
	
	private @NonNull ResponseGenerator responseGenerator;
	
	private @NonNull GSTService gstService;
	 
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all GST.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("trading.gst.get"),gstService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{gstId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("gstId") UUID gstId,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<GST> gst = gstService.findById(gstId);
			if(!gst.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT, HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("trading.gst.get"),gst.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	
	@ApiOperation(value = "Allows to create new gst.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The GST request payload") @RequestBody GST request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if(null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT, HttpStatus.BAD_REQUEST);
		}
		GST gstObj=new GST();
		gstObj.setCountryObj(request.getCountryObj());
		gstObj.setDescription(request.getDescription());
		gstObj.setGstPercentage(request.getGstPercentage());
		gstObj.setStatus(Status.ACTIVE);
		gstService.saveOrUpdate(gstObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("trading.gst.create"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing GST info.", response = Response.class)
	@PutMapping(value = "/update",  produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The GST request payload") @RequestBody GST request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if(null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT, HttpStatus.BAD_REQUEST);
		}
		
		Optional<GST> gstOptional = gstService.findById(request.getId());
		if(!gstOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT, HttpStatus.BAD_REQUEST);
		}
		GST gstObj = gstOptional.get();
		gstObj.setCountryObj(request.getCountryObj());
		gstObj.setModifiedBy(request.getModifiedBy());
		gstObj.setDescription(request.getDescription());
		gstObj.setGstPercentage(request.getGstPercentage());
		
		gstService.saveOrUpdate(gstObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("trading.gst.update"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	 
}
