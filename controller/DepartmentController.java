package com.iskool.controller;

import java.security.Principal;
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

import com.iskool.entity.Department;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.DepartmentService;
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
@RequestMapping("/api/department")
@Api(value = "Department Rest API", produces = "application/json", consumes = "application/json")
public class DepartmentController {
	
	private static final Logger logger = Logger.getLogger(AcademicYearController.class);
	private @NonNull ResponseGenerator responseGenerator;
	private @NonNull DepartmentService departmentService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new department.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")

	public ResponseEntity<?> create(
			@ApiParam(value = "The Department request payload") @RequestBody Department request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<Department> dptObj = departmentService.findByName(request.getName());
		if (dptObj.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("department.name", params),
					HttpStatus.BAD_REQUEST);
		}
		Department departmentObj = new Department();

		departmentObj.setName(request.getName());
		departmentObj.setShortName(request.getShortName());
		departmentObj.setStatus(Status.ACTIVE);
		departmentService.saveOrUpdate(departmentObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("department.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to fetch all department.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<Department>deptlist=departmentService.findAll();
		deptlist.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("department.get"),
					deptlist, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to fetch specific department.", response = Response.class)
	@GetMapping(value = "/get/{departmentId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("departmentId") UUID departmentId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Department> departmentObj = departmentService.findById(departmentId);
			if (!departmentObj.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("department.get"),
					departmentObj.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to update existing department.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The Department request payload") @RequestBody Department request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Department> dptOptional = departmentService.findById(request.getId());
		if (!dptOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!dptOptional.get().getName().equals(request.getName())) {
			Optional<Department> dptObj = departmentService.findByName(request.getName());
			if (dptObj.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("department.name", params),
						HttpStatus.BAD_REQUEST);
			}
		}
			Department dptObj = dptOptional.get();
	
			dptObj.setName(request.getName());
			dptObj.setShortName(request.getShortName());
			departmentService.saveOrUpdate(dptObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("department.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = " Allows to delete specific department.", response = Response.class)
	@DeleteMapping(value = "/delete/{departmentId}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "Department Id to be deleted") @PathVariable("departmentId") UUID departmentId,
			 @RequestHeader HttpHeaders httpHeader,Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			Department dptObj = departmentService.findById(departmentId).get();
			if (null == dptObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				departmentService.deleteById(departmentId);
				return responseGenerator.successResponse(context, messageSource.getMessage("department.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("department.invalid.delete"), HttpStatus.BAD_REQUEST);

			}
		}
	}

}
