package com.iskool.controller;

import java.util.Date;
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



import com.iskool.entity.AdmissionCategory;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AdmissionCategoryService;
import com.iskool.service.MessagePropertyService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/admissioncategory")
@Api(value = "AdmissionCategory Rest Api", produces = "application/json", consumes = "application/json")
public class AdmissionCategoryController {

	private static final Logger logger = Logger.getLogger(AdmissionCategory.class);

	private @NotNull ResponseGenerator responseGenerator;

	private @NotNull AdmissionCategoryService admissionCategoryService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allowes to create new  type", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The AdmissionCategory request Payload") @RequestBody AdmissionCategory request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if(null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT, HttpStatus.BAD_REQUEST);
		}
		Optional<AdmissionCategory> admissionCategoryObj = admissionCategoryService.findByName(request.getName());
		if (admissionCategoryObj.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("admissionCategory.name", params),
					HttpStatus.BAD_REQUEST);
		}


		AdmissionCategory admissionCategory = new AdmissionCategory();
		admissionCategory.setName(request.getName());
		admissionCategory.setStatus(Status.ACTIVE);
		admissionCategoryService.saveOrUpdate(admissionCategory);

		

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("admissionCategory.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all admissionCategory", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> get(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<AdmissionCategory>admCatList=admissionCategoryService.findAll();
		admCatList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		{
			
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("admissionCategory.get"),
						admCatList, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
	}

	@ApiOperation(value = "Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{admissionCategoryId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("admissionCategoryId") UUID admissionCategoryId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{

			try {
				Optional<AdmissionCategory> admissionCategoryObj = admissionCategoryService.findById(admissionCategoryId);
				if (!admissionCategoryObj.isPresent()) {
					return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
							HttpStatus.BAD_REQUEST);
				}
				return responseGenerator.successGetResponse(context, messageSource.getMessage("admissionCategory.get"),
						admissionCategoryObj.get(), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}
	}

	@ApiOperation(value = "Allowes to update admissionCategory", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@RequestBody AdmissionCategory request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<AdmissionCategory> admissionCategoryObj = admissionCategoryService.findById(request.getId());
		if (!admissionCategoryObj.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!admissionCategoryObj.get().getName().equals(request.getName())) {
		Optional<AdmissionCategory> admissionCategory= admissionCategoryService.findByName(request.getName());
		if (admissionCategory.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("admissionCategory.name", params),
					HttpStatus.BAD_REQUEST);
		}
		}
		
			AdmissionCategory admissionCategoryObject = admissionCategoryObj.get();
			admissionCategoryObject.setName(request.getName());
			admissionCategoryService.saveOrUpdate(admissionCategoryObject);
		
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("admissionCategory.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to delete specific admissionCategory id.", response = Response.class)
	@DeleteMapping(value = "/delete/{admissionCategoryId}/{deletedBy}",  produces = "application/json")
	public ResponseEntity<?> delete(
			@ApiParam(value = "AdmissionCategory Id to be deleted") @PathVariable("admissionCategoryId") UUID admissionCategoryId,
			@PathVariable("deletedBy") String deletedBy,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			AdmissionCategory admissionCategoryObj = admissionCategoryService.findById(admissionCategoryId).get();

			if (null == admissionCategoryObj) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
						HttpStatus.BAD_REQUEST);
			}
			try {
				admissionCategoryService.deleteById(admissionCategoryId);
				return responseGenerator.successResponse(context, messageSource.getMessage("admissionCategory.delete"),HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, messageSource.getMessage("admissionCategory.invalid.delete"), HttpStatus.BAD_REQUEST);

			}
		}

	}
}
