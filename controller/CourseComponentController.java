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

import com.iskool.entity.CourseComponent;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CourseComponentService;
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
@RequestMapping("/api/coursecomponent")
@Api(value = "Course component Rest API", produces = "application/json", consumes = "application/json")
public class CourseComponentController {

	private static final Logger logger = Logger.getLogger(CourseComponentController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CourseComponentService courseComponentService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all course component.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseComponent.get"),
					courseComponentService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all course component types.", response = Response.class)
	@GetMapping(value = "/get/{courseComponentId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("courseComponentId") UUID courseComponentId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<CourseComponent> courseComponent = courseComponentService.findById(courseComponentId);
			if (!courseComponent.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseComponent.get"),
					courseComponent.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new course component.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The course component request payload") @RequestBody CourseComponent request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<CourseComponent> courseComponentOptional = courseComponentService.findByName(request.getName());
		if (courseComponentOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("courseComponent.name", params),
					HttpStatus.BAD_REQUEST);
		}
		CourseComponent courseComponentObj = new CourseComponent();
		courseComponentObj.setName(request.getName());
		courseComponentObj.setDescription(request.getDescription());
		courseComponentObj.setStatus(Status.ACTIVE);
		courseComponentService.saveOrUpdate(courseComponentObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseComponent.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing course component info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The course component request payload") @RequestBody CourseComponent request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<CourseComponent> courseComponentOptional = courseComponentService.findById(request.getId());
		if (!courseComponentOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		CourseComponent courseComponentObj = courseComponentOptional.get();
		courseComponentObj.setName(request.getName());
		courseComponentObj.setDescription(request.getDescription());
		courseComponentService.saveOrUpdate(courseComponentObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseComponent.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specific course component id.", response = Response.class)
	@DeleteMapping(value = "/delete/{courseComponentId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Course component Id to be deleted") @PathVariable("courseComponentId") UUID courseComponentId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			CourseComponent courseComponentObj = courseComponentService.findById(courseComponentId).get();

			if (null == courseComponentObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			courseComponentObj.setStatus(Status.INACTIVE);
			courseComponentObj.setDeletedOn(new Date());
			courseComponentObj.setDeletedBy(deletedBy);
			courseComponentService.saveOrUpdate(courseComponentObj);

			try {
				return responseGenerator.successResponse(context, messageSource.getMessage("courseComponent.delete"),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}

	}
}
