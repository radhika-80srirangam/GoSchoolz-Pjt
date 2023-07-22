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

import com.iskool.entity.Branch;
import com.iskool.entity.CourseType;
import com.iskool.entity.Degree;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.BranchService;
import com.iskool.service.CourseTypeService;
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
@RequestMapping("/api/courseType")
@Api(value = "CourseType: courseType Rest API", produces = "application/json", consumes = "application/json")
public class CourseTypeController {
	private static final Logger logger = Logger.getLogger(BranchController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CourseTypeService courseTypeService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new courseTypeService.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The CourseType request payload") @RequestBody CourseType request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		CourseType courseType = new CourseType();
		courseType.setName(request.getName());
		courseType.setStatus(Status.ACTIVE);
		courseTypeService.save(courseType);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseType.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch all CourseType.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseType.get"),
					courseTypeService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{courseTypeId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("courseTypeId") UUID courseTypeId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<CourseType> branch = courseTypeService.findById(courseTypeId);
			if (!branch.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseType.get"), branch.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to update existing CourseType info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The CourseType request payload") @RequestBody CourseType request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<CourseType> courseTypeOptional = courseTypeService.findById(request.getId());
		if (!courseTypeOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		CourseType courseTypeObj = courseTypeOptional.get();
		courseTypeObj.setName(request.getName());
		courseTypeService.saveOrUpdate(courseTypeObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseType.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = " Allows to delete specific courseType id.", response = Response.class)
	@DeleteMapping(value = "/delete/{courseTypeId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
	@ApiParam(value = "CourseType Id to be deleted")
	@PathVariable("courseTypeId") UUID courseTypeId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
{ 
	CourseType courseTypeObj = courseTypeService.findById(courseTypeId).get();
	if (null == courseTypeObj) { 
		return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
				    HttpStatus.BAD_REQUEST);
} 
	courseTypeObj.setStatus(Status.INACTIVE); 
	courseTypeObj.setDeletedOn(new Date());
	courseTypeObj.setDeletedBy(deletedBy); 
	courseTypeService.saveOrUpdate(courseTypeObj);
	try { 
		return responseGenerator.successResponse(context, messageSource.getMessage("courseType.delete"),
				HttpStatus.OK);
	} catch (Exception e) {
		e.printStackTrace();
		logger.error(e.getMessage(),e);
		return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
					
	}
}
}
}