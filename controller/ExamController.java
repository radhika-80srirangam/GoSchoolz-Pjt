package com.iskool.controller;

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

import com.iskool.entity.Exam;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.ExamService;
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
@RequestMapping("/api/exam")
@Api(value = "Exam Rest API", produces = "application/json", consumes = "application/json")
public class ExamController {
	
	private static final Logger logger = Logger.getLogger(ExamController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull ExamService examService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new exam.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Exam request payload") @RequestBody Exam request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Exam> examOptional = examService.findByName(request.getExamName());
		if (examOptional.isPresent()) {
			String[] params = new String[] { request.getExamName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.name", params),
					HttpStatus.BAD_REQUEST);
		}
		Exam examObj = new Exam();
		examObj.setExamName(request.getExamName());
		examObj.setShortName(request.getShortName());
		examObj.setDescription(request.getDescription());
		examObj.setStatus(Status.ACTIVE);
		examService.saveOrUpdate(examObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("exam.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to fetch all exam.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<Exam>examList=examService.findAll();
		examList.sort((o1,o2)->o1.getExamName().compareToIgnoreCase(o2.getExamName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("exam.get"),
					examList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch specific exam.", response = Response.class)
	@GetMapping(value = "/get/{examId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("examId") UUID examId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Exam> exam = examService.findById(examId);
			if (!exam.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("exam.get"), exam.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to update existing exam info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Exam request payload") @RequestBody Exam request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Exam> examOpt = examService.findById(request.getId());
		if (!examOpt.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!examOpt.get().getExamName().equals(request.getExamName())) {
		Optional<Exam> examOptional = examService.findByName(request.getExamName());
		if (examOptional.isPresent()) {
			String[] params = new String[] { request.getExamName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.name", params),
					HttpStatus.BAD_REQUEST);
		}
		}
			Exam examObj = examOpt.get();
			examObj.setExamName(request.getExamName());
			examObj.setShortName(request.getShortName());
			examObj.setDescription(request.getDescription());
			examService.saveOrUpdate(examObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("exam.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to delete specific exam id.", response = Response.class)
	@DeleteMapping(value = "/delete/{examId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "Exam Id to be deleted") @PathVariable("examId") UUID examId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			Exam examObj = examService.findById(examId).get();

			if (null == examObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				examService.deleteById(examId);
				return responseGenerator.successResponse(context, messageSource.getMessage("exam.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("exam.invalid.delete"), HttpStatus.BAD_REQUEST);

			}

		}

	}

}
