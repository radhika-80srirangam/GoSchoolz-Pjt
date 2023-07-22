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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.LocationDTO;
import com.iskool.entity.City;
import com.iskool.entity.Location;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CityService;
import com.iskool.service.LocationService;
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
@RequestMapping("/api/location")
@Api(value = "Location Rest API", produces = "application/json", consumes = "application/json")
public class LocationController {

	private static final Logger logger = Logger.getLogger(LocationController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull LocationService locationService;

	private @NonNull CityService cityService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all location.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		List<LocationDTO> list = new ArrayList<>();
		LocationDTO locationDtoObj = null;
		for (Location locationObj : locationService.findAll()) {
			locationDtoObj = new LocationDTO();
			locationDtoObj.setId(locationObj.getId());
			locationDtoObj.setLocationName(locationObj.getName());
			locationDtoObj.setCityId(locationObj.getCityObj().getId());
			locationDtoObj.setCityName(locationObj.getCityObj().getName());
			list.add(locationDtoObj);
		}
		list.sort((o1,o2)->o1.getLocationName().compareToIgnoreCase(o2.getLocationName()));

		try {

			return responseGenerator.successGetResponse(context, messageSource.getMessage("location.get"), list,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all location types.", response = Response.class)
	@GetMapping(value = "/get/{locationId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("locationId") UUID locationId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {

			Optional<Location> location = locationService.findById(locationId);
			if (!location.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			Location locationObj = location.get();
			LocationDTO locationDtoObj = new LocationDTO();
			locationDtoObj.setId(locationObj.getId());
			locationDtoObj.setLocationName(locationObj.getName());
			locationDtoObj.setCityId(locationObj.getCityObj().getId());
			locationDtoObj.setCityName(locationObj.getCityObj().getName());

			return responseGenerator.successGetResponse(context, messageSource.getMessage("location.get"), locationDtoObj,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create new location.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The location request payload") @RequestBody LocationDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Location> locationOptional = locationService.findByName(request.getLocationName());
		if (locationOptional.isPresent()) {
			String[] params = new String[] { request.getLocationName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("location.name", params),
					HttpStatus.BAD_REQUEST);
		}

		Location locationObj = new Location();
		locationObj.setName(request.getLocationName());
		locationObj.setStatus(Status.ACTIVE);

		City cityObj = new City();
		cityObj.setId(request.getCityId());
	    
		locationObj.setCityObj(cityObj);

		locationService.saveOrUpdate(locationObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("location.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing location info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The location request payload") @RequestBody LocationDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Location> locationOptional = locationService.findById(request.getId());
		if (!locationOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		if(!locationOptional.get().getName().equals(request.getLocationName())) {
			Optional<Location> locationOptionalObj = locationService.findByName(request.getLocationName());
			if (locationOptionalObj.isPresent()) {
				String[] params = new String[] { request.getLocationName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("location.name", params),
						HttpStatus.BAD_REQUEST);
			}
		}
		
			City cityObj = new City();
			cityObj.setId(request.getCityId());
			
		    Location locationObj = locationOptional.get();
			locationObj.setName(request.getLocationName());
			locationObj.setCityObj(cityObj);
			locationService.saveOrUpdate(locationObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("location.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to delete specific location id.", response = Response.class)
	@DeleteMapping(value = "/delete/{locationId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Location Id to be deleted") @PathVariable("locationId") UUID locationId,
			@PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			Location locationObj = locationService.findById(locationId).get();

			if (null == locationObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				locationService.deleteById(locationId);
				return responseGenerator.successResponse(context, messageSource.getMessage("location.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("location.invalid.delete"), HttpStatus.BAD_REQUEST);

			}

		}

	}
}
