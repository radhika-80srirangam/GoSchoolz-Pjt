package com.iskool.controller;

import java.util.ArrayList;
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

import com.iskool.dto.StateDTO;
import com.iskool.entity.Country;
import com.iskool.entity.State;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CountryService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StateService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/country")
@Api(value = "Country Rest API", produces = "application/json", consumes = "application/json")
public class CountryController {

	private static final Logger logger = Logger.getLogger(CountryController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CountryService countryService;
	private @NonNull StateService stateService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all country.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<Country>countryList=countryService.findAll();
		countryList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("country.get"),
					countryList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{countryId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("countryId") UUID countryId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Country> country = countryService.findById(countryId);
			if (!country.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("country.get"), country.get(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new country.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Country request payload") @RequestBody Country request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Country> countryOptional = countryService.findByName(request.getName());
		if (countryOptional.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("country.name", params),
					HttpStatus.BAD_REQUEST);
		}
		Country countryObj = new Country();
		countryObj.setName(request.getName());
		countryObj.setShortName(request.getShortName());
		countryObj.setStatus(Status.ACTIVE);
		countryService.saveOrUpdate(countryObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("country.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing Country info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Country request payload") @RequestBody Country request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Country> countryOptional = countryService.findById(request.getId());
		if (!countryOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!countryOptional.get().getName().equals(request.getName())) {
			Optional<Country> countryOptionalObj = countryService.findByName(request.getName());
			if (countryOptionalObj.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("country.name", params),
						HttpStatus.BAD_REQUEST);
		}
		}
		
			Country countryObj = countryOptional.get();
			countryObj.setName(request.getName());
			countryObj.setShortName(request.getShortName());
			countryService.saveOrUpdate(countryObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("country.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/state/{countryId}", produces = "application/json")
	public ResponseEntity<?> getAll(@PathVariable("countryId") UUID countryId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		List<State> stateList = stateService.findByCountryId(countryId);

		List<StateDTO> stateDTOList = new ArrayList<StateDTO>();
		StateDTO stateDtoObj = null;
		for (State stateObj : stateList) {
			stateDtoObj = new StateDTO();

			stateDtoObj.setStateId(stateObj.getId());
			stateDtoObj.setStateName(stateObj.getName());
			stateDTOList.add(stateDtoObj);
		}
		stateDTOList.sort((o1,o2)->o1.getStateName().compareToIgnoreCase(o2.getStateName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("state.get"), stateDTOList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}


