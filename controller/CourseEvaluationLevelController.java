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

import com.iskool.entity.CourseEvaluationLevel;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CourseEvaluationLevelService;
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
@RequestMapping("/api/courseevaluationlevel")
@Api(value = "Course evaluation level Rest API", produces = "application/json", consumes = "application/json")
public class CourseEvaluationLevelController {

	private static final Logger logger = Logger.getLogger(CourseEvaluationLevelController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CourseEvaluationLevelService courseEvaluationLevelService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all course evaluation level.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseEvaluationLevel.get"),
					courseEvaluationLevelService.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all course evaluation level types.", response = Response.class)
	@GetMapping(value = "/get/{courseEvaluationLevelId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("courseEvaluationLevelId") UUID courseEvaluationLevelId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<CourseEvaluationLevel> courseEvaluationLevel = courseEvaluationLevelService
					.findById(courseEvaluationLevelId);
			if (!courseEvaluationLevel.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("courseEvaluationLevel.get"),
					courseEvaluationLevel.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new course evaluation level.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The course evaluation level request payload") @RequestBody CourseEvaluationLevel request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<CourseEvaluationLevel> courseEvaluationLevelOptional = courseEvaluationLevelService
				.findByName(request.getName());
		if (courseEvaluationLevelOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context,
					messageSource.getMessage("courseEvaluationLevel.name", params), HttpStatus.BAD_REQUEST);
		}
		CourseEvaluationLevel courseEvaluationLevelObj = new CourseEvaluationLevel();
		courseEvaluationLevelObj.setName(request.getName());
		courseEvaluationLevelObj.setDescription(request.getDescription());
		courseEvaluationLevelObj.setStatus(Status.ACTIVE);
		courseEvaluationLevelService.saveOrUpdate(courseEvaluationLevelObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseEvaluationLevel.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing course evaluation level info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The course evaluation level request payload") @RequestBody CourseEvaluationLevel request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<CourseEvaluationLevel> courseEvaluationLevelOptional = courseEvaluationLevelService
				.findById(request.getId());
		if (!courseEvaluationLevelOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		CourseEvaluationLevel courseEvaluationLevelObj = courseEvaluationLevelOptional.get();
		courseEvaluationLevelObj.setName(request.getName());
		courseEvaluationLevelObj.setDescription(request.getDescription());
		courseEvaluationLevelService.saveOrUpdate(courseEvaluationLevelObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("courseEvaluationLevel.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specific course evaluation level id.", response = Response.class)
	@DeleteMapping(value = "/delete/{courseEvaluationLevelId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Course evaluation level Id to be deleted") @PathVariable("courseEvaluationLevelId") UUID courseEvaluationLevelId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			CourseEvaluationLevel courseEvaluationLevelObj = courseEvaluationLevelService
					.findById(courseEvaluationLevelId).get();

			if (null == courseEvaluationLevelObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			courseEvaluationLevelObj.setStatus(Status.INACTIVE);
			courseEvaluationLevelObj.setDeletedOn(new Date());
			courseEvaluationLevelObj.setDeletedBy(deletedBy);
			courseEvaluationLevelService.saveOrUpdate(courseEvaluationLevelObj);

			try {
				return responseGenerator.successResponse(context,
						messageSource.getMessage("courseEvaluationLevel.delete"), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}

	}
}
