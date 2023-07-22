package com.iskool.controller;

import java.util.ArrayList;
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

import com.iskool.entity.FeesMaster;
import com.iskool.entity.FeesMasterComponent;
import com.iskool.entity.State;
import com.iskool.entity.Term;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.FeesMasterService;
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
@RequestMapping("/api/fee")
@Api(value = "FeesMaster: feesmasterclass Rest API", produces = "application/json", consumes = "application/json")
public class FeesMasterController {
	private static final Logger logger = Logger.getLogger(FeesMasterController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;
	
	private @NonNull FeesMasterService feesMasterService;
	
	private @NonNull MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new feesMaster.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The FeesMaster request payload") @RequestBody FeesMaster request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		
		Optional<FeesMaster> feesMaster = feesMasterService.findByFeesName(request.getFeesName());
		if (feesMaster.isPresent()) {
			String[] params = new String[] { request.getFeesName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("feesMaster.name", params),
					HttpStatus.BAD_REQUEST);
		}
		FeesMaster feesMasterObj = new FeesMaster();
		feesMasterObj.setFeesName(request.getFeesName());
		feesMasterObj.setDescription(request.getDescription());
		feesMasterObj.setStatus(Status.ACTIVE);
		List<FeesMasterComponent> list = new ArrayList<>();
		for (FeesMasterComponent component : request.getFeesMasterComponentList()) {
			component.setStatus(Status.ACTIVE);
			component.setFeesMasterObj(feesMasterObj);
			list.add(component);
		}
		feesMasterObj.setFeesMasterComponentList(list);
		feesMasterService.save(feesMasterObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("feesMaster.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all FeesMaster.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			List<FeesMaster>termFeeList=feesMasterService.findAll();
			termFeeList.sort((o1,o2)->o1.getFeesName().compareToIgnoreCase(o2.getFeesName()));
			return responseGenerator.successGetResponse(context, messageSource.getMessage("feesMaster.get"),
					termFeeList	, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all FeesMaster.", response = Response.class)
	@GetMapping(value = "/get/{feesMasterId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("feesMasterId") UUID feesMasterId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<FeesMaster> feeMaster = feesMasterService.findById(feesMasterId);
			if (!feeMaster.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("feesMaster.get"),
					feeMaster.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing feesMaster info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The FeesMaster request payload") @RequestBody FeesMaster request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<FeesMaster> feesMasterOptional = feesMasterService.findById(request.getId());
		if (!feesMasterOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		FeesMaster feesMasterObj = feesMasterOptional.get();
		feesMasterObj.setFeesName(request.getFeesName());
		feesMasterObj.setDescription(request.getDescription());
		feesMasterObj.setStatus(Status.ACTIVE);
		List<FeesMasterComponent> list = new ArrayList<>();
		for (FeesMasterComponent component : request.getFeesMasterComponentList()) {
			component.setStatus(Status.ACTIVE);
			component.setFeesMasterObj(feesMasterObj);
			list.add(component);
		}
		feesMasterObj.setFeesMasterComponentList(list);
		feesMasterService.saveOrUpdate(feesMasterObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("feesMaster.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = " Allows to delete specific feesMaster id.", response = Response.class)
	@DeleteMapping(value = "/delete/{feesMasterId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "feesMasterh Id to be deleted") @PathVariable("feesMasterId") UUID feesMasterId,
		 @PathVariable("deletedBy") String deletedBy,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		FeesMaster feesMasterObj = feesMasterService.findById(feesMasterId).get();
		if (null == feesMasterObj) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
					HttpStatus.BAD_REQUEST);
		}
		
		try {
			
			return feesMasterService.deleteById(feesMasterId,context);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, messageSource.getMessage("feesMaster.delete.invalid"),HttpStatus.BAD_REQUEST);
		}

	}
}
