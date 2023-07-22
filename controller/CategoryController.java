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

import com.iskool.entity.Category;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.CategoryService;
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
@RequestMapping("/api/category")
@Api(value = "Trading: Section Rest API", produces = "application/json", consumes = "application/json")
public class CategoryController {
	private static final Logger logger = Logger.getLogger(CategoryController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull CategoryService categoryService;
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new category.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The Category request payload") @RequestBody Category request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<Category> categoryObject = categoryService.findByName(request.getName());
		if (categoryObject.isPresent()) {
			String[] params = new String[] { request.getName() };
			return responseGenerator.errorResponse(context, messageSource.getMessage("category.name", params),
					HttpStatus.BAD_REQUEST);
		}
		
		Category categoryObj = new Category();
		categoryObj.setName(request.getName());
		categoryObj.setCreatedBy(request.getCreatedBy());
		categoryObj.setStatus(Status.ACTIVE);
		categoryService.save(categoryObj);
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("category.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Category.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<Category>catList=categoryService.findAll();
		catList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("category.get"),
					catList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value ="Allows to fetch all product types.", response = Response.class)
	@GetMapping(value = "/get/{categoryId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("categoryId") UUID categoryId, @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<Category> category = categoryService.findById(categoryId);
			if (!category.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}
			return responseGenerator.successGetResponse(context, messageSource.getMessage("category.get"),
					category.get(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing category info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(@ApiParam(value = "The Section request payload") @RequestBody Category request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request.getId()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Category> categoryOptional = categoryService.findById(request.getId());
		if (!categoryOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		if(!categoryOptional.get().getName().equals(request.getName())) {
			Optional<Category> category=categoryService.findByName(request.getName());
			if(category.isPresent()) {
				String[] params = new String[] { request.getName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("category.name", params),
						HttpStatus.BAD_REQUEST);
			}
		}
		Category categoryObj = categoryOptional.get();
		categoryObj.setName(request.getName());
		categoryObj.setModifiedBy(request.getModifiedBy());
		categoryService.saveOrUpdate(categoryObj);

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("category.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	
	}
	@ApiOperation(value = "Allows to delete specific category id.", response = Response.class)
	@DeleteMapping(value = "/delete/{categoryId}/{deletedBy}", produces = "application/json")
	public ResponseEntity<?> delete(
	@ApiParam(value = "Category Id to be deleted")
	@PathVariable("categoryId") UUID categoryId, @PathVariable("deletedBy") String deletedBy, @RequestHeader HttpHeaders httpHeader) throws Exception { 
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
{ 
	Category categoryObj = categoryService.findById(categoryId).get();
	if (null == categoryObj) { 
		return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,
				    HttpStatus.BAD_REQUEST);
} 
	try {
		categoryService.deleteById(categoryId);
		return responseGenerator.successResponse(context, messageSource.getMessage("category.delete"),HttpStatus.OK);
	} catch (Exception e) {
		e.printStackTrace();
		logger.error(e.getMessage(), e);
		return responseGenerator.errorResponse(context, messageSource.getMessage("category.invalid.delete"), HttpStatus.BAD_REQUEST);

	}
}
}}