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

import com.iskool.entity.CourseElectiveGroup;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CourseElectiveGroupService;
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
@RequestMapping("/api/courseElectiveGroup")
@Api(value = "CourseElectiveGroup: courseElectiveGroup Rest API", produces = "application/json", consumes = "application/json")
public class CourseElectiveGroupController {
	private static final Logger logger = Logger.getLogger(CourseElectiveGroupController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CourseElectiveGroupService courseElectiveGroupService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new courseElectiveGroup.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The CourseElectiveGroup request payload") @RequestBody CourseElectiveGroup request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		CourseElectiveGroup courseElectiveGroupObj = new CourseElectiveGroup();
		courseElectiveGroupObj.setName(request.getName());
		courseElectiveGroupObj.setDescription(request.getDescription());
		courseElectiveGroupObj.setMinimumSelected(request.getMinimumSelected());
		courseElectiveGroupObj.setMaximumSelected(request.getMaximumSelected());
		courseElectiveGroupObj.setStatus(Status.ACTIVE);
		courseElectiveGroupService.save(courseElectiveGroupObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseElectiveGroup.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch all CourseElectiveGroup.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseElectiveGroup.get"),
					courseElectiveGroupService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{courseElectiveGroupId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("courseElectiveGroupId") UUID courseElectiveGroupId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<CourseElectiveGroup> courseElectiveGroup = courseElectiveGroupService.findById(courseElectiveGroupId);
			if (!courseElectiveGroup.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseElectiveGroup.get"), courseElectiveGroup.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to update existing courseElectiveGroup info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The CourseElectiveGroup request payload") @RequestBody CourseElectiveGroup request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<CourseElectiveGroup> courseElectiveGroupOptional = courseElectiveGroupService.findById(request.getId());
		if (!courseElectiveGroupOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		CourseElectiveGroup courseElectiveGroupObj = courseElectiveGroupOptional.get();
		courseElectiveGroupObj.setName(request.getName());
		courseElectiveGroupObj.setDescription(request.getDescription());
		courseElectiveGroupObj.setMinimumSelected(request.getMinimumSelected());
		courseElectiveGroupObj.setMaximumSelected(request.getMaximumSelected());
		courseElectiveGroupService.saveOrUpdate(courseElectiveGroupObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseElectiveGroup.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = " Allows to delete specific courseElectiveGroup id.", response = Response.class)
	@DeleteMapping(value = "/delete/{courseElectiveGroupId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
	@ApiParam(value = "CourseElectiveGroup Id to be deleted")
	@PathVariable("courseElectiveGroupId") UUID courseElectiveGroupId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
{ 
	CourseElectiveGroup courseElectiveGroupObj = courseElectiveGroupService.findById(courseElectiveGroupId).get();
	if (null == courseElectiveGroupObj) { 
		return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
				    HttpStatus.BAD_REQUEST);
} 
	courseElectiveGroupObj.setStatus(Status.INACTIVE); 
	courseElectiveGroupObj.setDeletedOn(new Date());
	courseElectiveGroupObj.setDeletedBy(deletedBy); 
	courseElectiveGroupService.saveOrUpdate(courseElectiveGroupObj);
	try { 
		return responseGenerator.successResponse(context, messageSource.getMessage("courseElectiveGroup.delete"),
				HttpStatus.OK);
	} catch (Exception e) {
		e.printStackTrace();
		logger.error(e.getMessage(),e);
		return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
					
	}
}
}
}