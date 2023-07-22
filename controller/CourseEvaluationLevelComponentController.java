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

import com.iskool.entity.CourseEvaluationLevelComponent;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CourseEvaluationLevelComponentService;
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
@RequestMapping("/api/courseevaluationlevelcomponent")
@Api(value = "Course evaluation level component Rest API", produces = "application/json", consumes = "application/json")
public class CourseEvaluationLevelComponentController {

	private static final Logger logger = Logger.getLogger(CourseEvaluationLevelComponentController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CourseEvaluationLevelComponentService courseEvaluationLevelComponentService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all course evaluation level component.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context,
					messageSource.getMessage("courseEvaluationLevelComponent.get"),
					courseEvaluationLevelComponentService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all course evaluation level component types.", response = Response.class)
	@GetMapping(value = "/get/{courseEvaluationLevelComponentId}", produces = "application/json")
	public ResponseEntity<?> get(
			@PathVariable("courseEvaluationLevelComponentId") UUID courseEvaluationLevelComponentId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<CourseEvaluationLevelComponent> courseEvaluationLevelComponent = courseEvaluationLevelComponentService
					.findById(courseEvaluationLevelComponentId);
			if (!courseEvaluationLevelComponent.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context,
					messageSource.getMessage("courseEvaluationLevelComponent.get"),
					courseEvaluationLevelComponent.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new course evaluation level component.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The course evaluation level component request payload") @RequestBody CourseEvaluationLevelComponent request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<CourseEvaluationLevelComponent> courseEvaluationLevelComponentOptional = courseEvaluationLevelComponentService
				.findByName(request.getName());
		if (courseEvaluationLevelComponentOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context,
					messageSource.getMessage("courseEvaluationLevelComponent.name", params), HttpStatus.BAD_REQUEST);
		}
		CourseEvaluationLevelComponent courseEvaluationLevelComponentObj = new CourseEvaluationLevelComponent();
		courseEvaluationLevelComponentObj.setName(request.getName());
		courseEvaluationLevelComponentObj.setDescription(request.getDescription());
		courseEvaluationLevelComponentObj.setStatus(Status.ACTIVE);
		courseEvaluationLevelComponentService.saveOrUpdate(courseEvaluationLevelComponentObj);
		try {
			return responseGenerator.successResponse(context,
					messageSource.getMessage("courseEvaluationLevelComponent.create"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing course evaluation level component info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The course evaluation level component request payload") @RequestBody CourseEvaluationLevelComponent request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<CourseEvaluationLevelComponent> courseEvaluationLevelComponentOptional = courseEvaluationLevelComponentService
				.findById(request.getId());
		if (!courseEvaluationLevelComponentOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		CourseEvaluationLevelComponent courseEvaluationLevelComponentObj = courseEvaluationLevelComponentOptional.get();
		courseEvaluationLevelComponentObj.setName(request.getName());
		courseEvaluationLevelComponentObj.setDescription(request.getDescription());
		courseEvaluationLevelComponentService.saveOrUpdate(courseEvaluationLevelComponentObj);

		try {
			return responseGenerator.successResponse(context,
					messageSource.getMessage("courseEvaluationLevelComponent.update"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specific course evaluation level component id.", response = Response.class)
	@DeleteMapping(value = "/delete/{courseEvaluationLevelComponentId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Course evaluation level component Id to be deleted") @PathVariable("courseEvaluationLevelComponentId") UUID courseEvaluationLevelComponentId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			CourseEvaluationLevelComponent courseEvaluationLevelComponentObj = courseEvaluationLevelComponentService
					.findById(courseEvaluationLevelComponentId).get();

			if (null == courseEvaluationLevelComponentObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			courseEvaluationLevelComponentObj.setStatus(Status.INACTIVE);
			courseEvaluationLevelComponentObj.setDeletedOn(new Date());
			courseEvaluationLevelComponentObj.setDeletedBy(deletedBy);
			courseEvaluationLevelComponentService.saveOrUpdate(courseEvaluationLevelComponentObj);

			try {
				return responseGenerator.successResponse(context,
						messageSource.getMessage("courseEvaluationLevelComponent.delete"), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}

	}
}
