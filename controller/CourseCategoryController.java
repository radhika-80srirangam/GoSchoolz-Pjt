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
import com.iskool.entity.CourseCategory;
import com.iskool.entity.CourseType;
import com.iskool.entity.Degree;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.BranchService;
import com.iskool.service.CourseCategoryService;
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
@RequestMapping("/api/courseCategory")
@Api(value = "CourseCategory: courseCategory Rest API", produces = "application/json", consumes = "application/json")
public class CourseCategoryController {
	private static final Logger logger = Logger.getLogger(BranchController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CourseCategoryService courseCategoryService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new courseCategoryService.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The CourseCategory request payload") @RequestBody CourseCategory request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		CourseCategory courseCategory = new CourseCategory();
		courseCategory.setName(request.getName());
		courseCategory.setStatus(Status.ACTIVE);
		courseCategoryService.save(courseCategory);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseCategory.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch all CourseCategory.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseCategory.get"),
					courseCategoryService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{courseCategoryId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("courseCategoryId") UUID courseCategoryId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<CourseCategory> courseCategory = courseCategoryService.findById(courseCategoryId);
			if (!courseCategory.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseCategory.get"), courseCategory.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to update existing CourseCategory info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The CourseCategory request payload") @RequestBody CourseCategory request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<CourseCategory> courseCategoryOptional = courseCategoryService.findById(request.getId());
		if (!courseCategoryOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		CourseCategory courseCategoryObj = courseCategoryOptional.get();
		courseCategoryObj.setName(request.getName());
		courseCategoryService.saveOrUpdate(courseCategoryObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseCategory.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = " Allows to delete specific courseCategory id.", response = Response.class)
	@DeleteMapping(value = "/delete/{courseCategoryId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
	@ApiParam(value = "CourseCategory Id to be deleted")
	@PathVariable("courseCategoryId") UUID courseCategoryId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
{ 
	CourseCategory courseCategoryObj = courseCategoryService.findById(courseCategoryId).get();
	if (null == courseCategoryObj) { 
		return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
				    HttpStatus.BAD_REQUEST);
} 
	courseCategoryObj.setStatus(Status.INACTIVE); 
	courseCategoryObj.setDeletedOn(new Date());
	courseCategoryObj.setDeletedBy(deletedBy); 
	courseCategoryService.saveOrUpdate(courseCategoryObj);
	try { 
		return responseGenerator.successResponse(context, messageSource.getMessage("courseCategory.delete"),
				HttpStatus.OK);
	} catch (Exception e) {
		e.printStackTrace();
		logger.error(e.getMessage(),e);
		return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
					
	}
}
}
}