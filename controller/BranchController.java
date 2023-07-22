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

import com.iskool.entity.Branch;
import com.iskool.entity.Degree;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.BranchService;
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
@RequestMapping("/api/branch")
@Api(value = "Branch: branch Rest API", produces = "application/json", consumes = "application/json")
public class BranchController {
	private static final Logger logger = Logger.getLogger(BranchController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull BranchService branchService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new branch.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Branch request payload") @RequestBody Branch request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Branch branchObj = new Branch();
		branchObj.setLongName(request.getLongName());
		branchObj.setShortName(request.getShortName());
		branchObj.setDegreeObj(request.getDegreeObj());
		branchObj.setStatus(Status.ACTIVE);
		branchService.save(branchObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("branch.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch all Branch.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Branch>branchList=branchService.findAll();
		branchList.sort((o1,o2)->o1.getLongName().compareToIgnoreCase(o2.getLongName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("branch.get"),
					branchList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{branchId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("branchId") UUID branchId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Branch> branch = branchService.findById(branchId);
			if (!branch.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("branch.get"), branch.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to update existing branch info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Branch request payload") @RequestBody Branch request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Branch> branchOptional = branchService.findById(request.getId());
		if (!branchOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Branch brancheObj = branchOptional.get();
		brancheObj.setLongName(request.getLongName());
		brancheObj.setShortName(request.getShortName());
		branchService.saveOrUpdate(brancheObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("branch.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = " Allows to delete specific branch id.", response = Response.class)
	@DeleteMapping(value = "/delete/{branchId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
	@ApiParam(value = "Branch Id to be deleted")
	@PathVariable("branchId") UUID branchId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
{ 
	Branch branchObj = branchService.findById(branchId).get();
	if (null == branchObj) { 
		return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
				    HttpStatus.BAD_REQUEST);
} 
	try {
		branchService.deleteById(branchId);
		return responseGenerator.successResponse(context, messageSource.getMessage("branch.delete"),HttpStatus.OK);
	} catch (Exception e) {
		e.printStackTrace();
		logger.error(e.getMessage(), e);
		return responseGenerator.errorResponse(context, messageSource.getMessage("branch.invalid.delete"), HttpStatus.BAD_REQUEST);

	}
}
}
}