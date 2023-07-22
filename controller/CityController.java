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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.City;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CityService;
import com.iskool.service.MessagePropertyService;
import com.iskool.util.message.ResponseMessage;
import com.sun.istack.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/city")
@Api(value = "City Rest API", produces = "application/json", consumes = "application/json")
public class CityController {

	private static final Logger logger = Logger.getLogger(City.class);

	private @NotNull ResponseGenerator responseGenerator;

	private @NotNull CityService cityService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allowes to create new  type", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The City request Payload") @RequestBody City request,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeaders);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<City> cityObj = cityService.findByName(request.getName());
		if (cityObj.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("city.name", params),
					HttpStatus.BAD_REQUEST);
		}
		City cityObject = new City();

		cityObject.setName(request.getName());
		cityObject.setShortName(request.getShortName());
		cityObject.setStatus(Status.ACTIVE);
		cityObject.setStateObj(request.getStateObj());
		cityService.saveOrUpdate(cityObject);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("city.create"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allowes to update city", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The City request Payload") @RequestBody City request,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeaders);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<City> cityOptional = cityService.findById(request.getId());
		if (!cityOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!cityOptional.get().getName().equals(request.getName())) {
			Optional<City> cityObj = cityService.findByName(request.getName());
			if (cityObj.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("city.name", params),
						HttpStatus.BAD_REQUEST);
		}
		}
			City cityObj = cityOptional.get();
			cityObj.setName(request.getName());
			cityObj.setShortName(request.getShortName());
			cityService.saveOrUpdate(cityObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("city.update"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch all city", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<City>cityList=cityService.findAll();
		cityList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("city.get"),
						cityList, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{cityId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("cityId") UUID cityId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<City> city = cityService.findById(cityId);
			if (!city.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("city.get"), city.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}