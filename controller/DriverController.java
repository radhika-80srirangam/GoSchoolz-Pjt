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

import com.iskool.entity.Driver;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.DriverService;
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
@RequestMapping("/api/driver")
@Api(value = "DriverRest API", produces = "application/json", consumes = "application/json")
public class DriverController {

	private static final Logger logger = Logger.getLogger(DriverController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull DriverService driverService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new driver.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Driver request payload") @RequestBody Driver request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<Driver> driver = driverService.findByLicenseNumber(request.getLicenseNumber());
		if (driver.isPresent()) {
			String[] params = new String[] { request.getLicenseNumber() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("driver.license.exist", params),
					HttpStatus.BAD_REQUEST);
		}
		Optional<Driver> driverOptional = driverService.findByPhoneNumber(request.getPhoneNumber());
		if (driverOptional.isPresent()) {
			String[] params = new String[] { request.getPhoneNumber() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("driver.phone.exist", params),
					HttpStatus.BAD_REQUEST);
		}
		
		Driver driverObj = new Driver();
		driverObj.setName(request.getName());
		driverObj.setAddress(request.getAddress());
		driverObj.setPhoneNumber(request.getPhoneNumber());
		driverObj.setExperience(request.getExperience());
		driverObj.setAge(request.getAge());
		driverObj.setLicenseNumber(request.getLicenseNumber());
		driverObj.setStatus(Status.ACTIVE);
		driverService.saveOrUpdate(driverObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("driver.create"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all driver.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<Driver>driverList=driverService.findAll();
		driverList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("driver.get"),
					driverList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{driverId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("driverId") UUID driverId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Driver> driverObj = driverService.findById(driverId);
			if (!driverObj.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("driver.get"),
					driverObj.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing driver info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Driver request payload") @RequestBody Driver request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Driver> driverOptional = driverService.findById(request.getId());
		if (!driverOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!driverOptional.get().getLicenseNumber().equals(request.getLicenseNumber())) {
		Optional<Driver> driverLicenseOptional = driverService.findByLicenseNumber(request.getLicenseNumber());
		if (driverLicenseOptional.isPresent()) {
			String[] params = new String[] { request.getLicenseNumber() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("driver.license.exist", params),
					HttpStatus.BAD_REQUEST);
		}
		}
		if(!driverOptional.get().getPhoneNumber().equals(request.getPhoneNumber())) {
		Optional<Driver> driverPhoneOptional = driverService.findByPhoneNumber(request.getPhoneNumber());
		if (driverPhoneOptional.isPresent()) {
			String[] params = new String[] { request.getPhoneNumber() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("driver.phone.exist", params),
					HttpStatus.BAD_REQUEST);
		}
		}
		Driver driverObj = driverOptional.get();
		driverObj.setName(request.getName());
		driverObj.setAddress(request.getAddress());
		driverObj.setPhoneNumber(request.getPhoneNumber());
		driverObj.setExperience(request.getExperience());
		driverObj.setAge(request.getAge());
		driverObj.setLicenseNumber(request.getLicenseNumber());
		driverObj.setStatus(Status.ACTIVE);
		driverService.saveOrUpdate(driverObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("driver.update"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = " Allows to delete specific driver id.", response = Response.class)
	@DeleteMapping(value = "/delete/{driverId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Driver Id to be deleted") @PathVariable("driverId") UUID branchId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			Driver driverObj = driverService.findById(branchId).get();
			if (null == driverObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			driverObj.setStatus(Status.INACTIVE);
			driverObj.setDeletedOn(new Date());
			driverObj.setDeletedBy(deletedBy);
			driverService.saveOrUpdate(driverObj);
			try {
				return responseGenerator.successResponse(context, messageSource.getMessage("driver.delete"),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);

			}
		}
	}

}
