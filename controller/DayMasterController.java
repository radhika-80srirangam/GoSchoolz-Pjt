package com.iskool.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.DayMaster;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.DayMasterService;
import com.iskool.service.MessagePropertyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/daymaster")
@Api(value = "DayMaster Rest Api", produces = "application/json", consumes = "application/json")
public class DayMasterController {
	
	private static final Logger logger = Logger.getLogger(DayMaster.class);

	private @NotNull ResponseGenerator responseGenerator;
	
	private @NotNull DayMasterService dayMasterService;
	
	@Autowired
	MessagePropertyService messageSource;
	
	@ApiOperation(value = "Allows to fetch all dayMaster", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<DayMaster>dayList=dayMasterService.findAll();
		dayList.sort((o1,o2)->o1.getCreatedOn().compareTo(o2.getCreatedOn()));
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("dayMaster.get"),
						dayList, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
	}

}
