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

import com.iskool.entity.BloodGroup;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.BloodGroupService;
import com.iskool.service.MessagePropertyService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/bloodgroup")
@Api(value = "Bloodgroup Rest Api", produces = "application/json", consumes = "application/json")
public class BloodGroupController {

	private static final Logger logger = Logger.getLogger(BloodGroup.class);

	private @NotNull ResponseGenerator responseGenerator;

	private @NotNull BloodGroupService bloodGroupService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allowes to create new  type", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The Blood group request Payload") @RequestBody BloodGroup request,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeaders);

		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<BloodGroup> bloodGroupObj = bloodGroupService.findByName(request.getName());
		if (bloodGroupObj.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("bloodgroup.name", params),
					HttpStatus.BAD_REQUEST);
		}

		BloodGroup bloodGroupoOject = new BloodGroup();
		bloodGroupoOject.setName(request.getName());
		bloodGroupoOject.setStatus(Status.ACTIVE);
		bloodGroupService.saveOrUpdate(bloodGroupoOject);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("bloodgroup.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all bloodgroup", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> get(@RequestHeader HttpHeaders httpHeaders) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeaders);
		
		List<BloodGroup>bloodGroupList=bloodGroupService.findAll();
		bloodGroupList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("bloodgroup.get"),
						bloodGroupList, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{bloodgroupId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("bloodgroupId") UUID bloodgroupId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{

			try {
				Optional<BloodGroup> bloodGroupobj = bloodGroupService.findById(bloodgroupId);
				if (!bloodGroupobj.isPresent()) {
					return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
							HttpStatus.BAD_REQUEST);
				}
				return responseGenerator.successGetResponse(context, messageSource.getMessage("bloodgroup.get"),
						bloodGroupobj.get(), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}
	}

	@ApiOperation(value = "Allowes to update bloodgroup", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update( @RequestBody BloodGroup request,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeaders);
		{
			if (null == request.getId()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			Optional<BloodGroup> bloodGroupobj = bloodGroupService.findById(request.getId());
			if (!bloodGroupobj.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			if(!bloodGroupobj.get().getName().equals(request.getName())) {
			Optional<BloodGroup> bloodGroup = bloodGroupService.findByName(request.getName());
			if(bloodGroup.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("bloodgroup.name", params),
						HttpStatus.BAD_REQUEST);
			}
			}
			
			BloodGroup bloodGroupObject = bloodGroupobj.get();
			bloodGroupObject.setName(request.getName());
			

			bloodGroupService.saveOrUpdate(bloodGroupObject);
		}
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("bloodgroup.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to delete specific bloodgroup id.", response = Response.class)
	@DeleteMapping(value = "/delete/{bloodgroupId}/{deletedBy}",  produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Bloodgroup Id to be deleted") @PathVariable("bloodgroupId") UUID bloodgroupId,
			@PathVariable("deletedBy") String deletedBy,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			BloodGroup bloodGroupObj = bloodGroupService.findById(bloodgroupId).get();

			if (null == bloodGroupObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				bloodGroupService.deleteById(bloodgroupId);
				return responseGenerator.successResponse(context, messageSource.getMessage("bloodgroup.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("bloodgroup.invalid.delete"), HttpStatus.BAD_REQUEST);

			}

		}

	}
}