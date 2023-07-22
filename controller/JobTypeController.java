package com.iskool.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

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

import com.iskool.entity.JobType;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.JobTypeService;
import com.iskool.service.MessagePropertyService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/jobtype")
@Api(value = "JobType Rest Api", produces = "application/json", consumes = "application/json")

public class JobTypeController {

	private static final Logger logger = Logger.getLogger(JobType.class);

	private @NotNull ResponseGenerator responseGenerator;

	private @NotNull JobTypeService jobTypeService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allowes to create new  type", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The JobType request Payload") @RequestBody JobType request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<JobType> jobTypeObj = jobTypeService.findByName(request.getName());
		if (jobTypeObj.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("jobType.name", params),
					HttpStatus.BAD_REQUEST);
		}

		JobType jobTypeObject = new JobType();
		jobTypeObject.setName(request.getName());
		jobTypeObject.setStatus(Status.ACTIVE);
		jobTypeService.saveOrUpdate(jobTypeObject);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("jobType.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all jobType", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> get(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<JobType>jobTypelist=jobTypeService.findAll();
		jobTypelist.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("jobType.get"),
						jobTypelist, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{jobTypeId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("jobTypeId") UUID jobTypeId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{

			try {
				Optional<JobType> JobTypeObj = jobTypeService.findById(jobTypeId);
				if (!JobTypeObj.isPresent()) {
					return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
							HttpStatus.BAD_REQUEST);
				}
				return responseGenerator.successGetResponse(context, messageSource.getMessage("jobType.get"),
						JobTypeObj.get(), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}
	}

	@ApiOperation(value = "Allowes to update JobType", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@RequestBody JobType request, @RequestHeader HttpHeaders httpHeader)
			throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<JobType> JobTypeObj = jobTypeService.findById(request.getId());
		if (!JobTypeObj.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		if (!JobTypeObj.get().getName().equals(request.getName())) {
			Optional<JobType> jobTypeName = jobTypeService.findByName(request.getName());
			if (jobTypeName.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("jobType.name", params),
						HttpStatus.BAD_REQUEST);
			}
		}
		JobType JobTypeObject = JobTypeObj.get();
		JobTypeObject.setName(request.getName());
		// JobTypeObject.setCreatedBy(request.getCreatedBy());

		jobTypeService.saveOrUpdate(JobTypeObject);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("jobType.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to delete specific jobType id.", response = Response.class)
	@DeleteMapping(value = "/delete/{jobTypeId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "jobType Id to be deleted") @PathVariable("jobTypeId") UUID jobTypeId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			JobType jobType = jobTypeService.findById(jobTypeId).get();

			if (null == jobType) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				jobTypeService.deleteById(jobTypeId);
				return responseGenerator.successResponse(context, messageSource.getMessage("jobType.delete"),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("jobType.invalid.delete"),
						HttpStatus.BAD_REQUEST);

			}

		}

	}

}
