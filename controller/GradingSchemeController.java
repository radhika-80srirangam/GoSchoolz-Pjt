package com.iskool.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.GradingSchemeDtlDTO;
import com.iskool.dto.GradingSchemeHdrDTO;
import com.iskool.entity.GradingSchemeDtl;
import com.iskool.entity.GradingSchemeHdr;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.GradingSchemeDtlService;
import com.iskool.service.GradingSchemeHdrService;
import com.iskool.service.MessagePropertyService;
import com.iskool.util.message.ResponseMessage;
import com.sun.istack.NotNull;

//import io.jsonwebtoken.lang.Collections;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/grading/scheme")
@Api(value = "GradingSchemeHdr: gradingSchemeHdr Rest API", produces = "application/json", consumes = "application/json")
public class GradingSchemeController {
	private static final Logger logger = Logger.getLogger(GradingSchemeController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull GradingSchemeHdrService gradingSchemeHdrService;

	private @NonNull GradingSchemeDtlService gradingSchemeDtlService;

	private @NotNull MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new gradingScheme.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The GradingSchemeHdr request payload") @RequestBody GradingSchemeHdrDTO request,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<String> markList1=new ArrayList<String>();
			
		for(GradingSchemeDtlDTO dtlObj:request.getGradingDtlDTOList()) {
			if(dtlObj.getRangeFrom()>=dtlObj.getRangeTo()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("grade.range.from.small"),HttpStatus.BAD_REQUEST);
			}
				
		}
		
		for(GradingSchemeDtlDTO dtlObj:request.getGradingDtlDTOList()) {
			if(markList1.contains(dtlObj.getGradeName())) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("gradeName.sme"),HttpStatus.BAD_REQUEST);
			}
			else {
				markList1.add(dtlObj.getGradeName());
			}	
		}
		
		List<Double> markList=new ArrayList<Double>();
		
		for(GradingSchemeDtlDTO dto:request.getGradingDtlDTOList()) {
			for(Double i=dto.getRangeFrom();i<=dto.getRangeTo();i++) {
				if(markList.contains(i)) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("scheme.sme"),HttpStatus.BAD_REQUEST);
				}
				else 
					markList.add(i);
			}
		}
	
		Map<UUID, GradingSchemeDtl> existingMap = new HashMap<UUID, GradingSchemeDtl>();
		GradingSchemeHdr gradingSchemeHdrObj = null;
		if (null != request.getId()) {
			Optional<GradingSchemeHdr> gradingSchemeHdr = gradingSchemeHdrService.findById(request.getId());
			if (!gradingSchemeHdr.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,HttpStatus.BAD_REQUEST);
			}

			gradingSchemeHdrObj = gradingSchemeHdr.get();                         

			for (GradingSchemeDtl schemeDtlObj : gradingSchemeHdrObj.getGradingSchemeDtlList()) {
				existingMap.put(schemeDtlObj.getId(), schemeDtlObj);
			}
		}

		else {
			Optional<GradingSchemeHdr> gradingScheme = gradingSchemeHdrService.findBySchemeName(request.getSchemeName());
			if (gradingScheme.isPresent()) {
				String[] params = new String[] { request.getSchemeName() };
				return responseGenerator.errorResponse(context, messageSource.getMessage("scheme.name", params),HttpStatus.BAD_REQUEST);
			}
			gradingSchemeHdrObj = new GradingSchemeHdr();
			gradingSchemeHdrObj.setStatus(Status.ACTIVE); 
		}

		List<GradingSchemeDtl> dtlList = new ArrayList<GradingSchemeDtl>();
		for (GradingSchemeDtlDTO schemeDtlReqObj : request.getGradingDtlDTOList()) {
			GradingSchemeDtl gradingDtlObj = existingMap.remove(schemeDtlReqObj.getId());
			if (null == gradingDtlObj) {
				gradingDtlObj = new GradingSchemeDtl();
				gradingDtlObj.setGradingHdrObj(gradingSchemeHdrObj);
				gradingDtlObj.setStatus(Status.ACTIVE);
				gradingDtlObj.setGradeName(schemeDtlReqObj.getGradeName());
				gradingDtlObj.setRangeFrom(schemeDtlReqObj.getRangeFrom());
				gradingDtlObj.setRangeTo(schemeDtlReqObj.getRangeTo());
				gradingDtlObj.setGradePoints(schemeDtlReqObj.getGradePoints());
				
				
			} else { 
				gradingDtlObj.setGradeName(schemeDtlReqObj.getGradeName());
				gradingDtlObj.setRangeFrom(schemeDtlReqObj.getRangeFrom());
				gradingDtlObj.setRangeTo(schemeDtlReqObj.getRangeTo());
				gradingDtlObj.setGradePoints(schemeDtlReqObj.getGradePoints());
			}
			
			dtlList.add(gradingDtlObj); 
			
		}
		if (!existingMap.isEmpty() && existingMap.size() > 0) {
			for (GradingSchemeDtl gradingSchemeDtl : existingMap.values()) {
				gradingSchemeDtlService.deleteById(gradingSchemeDtl.getId());
			}
		}
		
		gradingSchemeHdrObj.setSchemeName(request.getSchemeName());
		gradingSchemeHdrObj.setSchemeDetails(request.getSchemeDetails());
		gradingSchemeHdrObj.setGradingSchemeDtlList(dtlList);
		gradingSchemeHdrService.saveOrUpdate(gradingSchemeHdrObj);
		
		try {
			return responseGenerator.successResponse(context, null==request.getId()?messageSource.getMessage("grading.scheme.Hdr.create"):messageSource.getMessage("grading.scheme.Hdr.update"),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	
	@ApiOperation(value = "Allows to fetch all TransportRoute.", response = Response.class)
	@GetMapping(value = "/get/{gradeSchemeId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("gradeSchemeId") UUID gradeSchemeId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<GradingSchemeHdr> gradingSchemeHdr = gradingSchemeHdrService.findById(gradeSchemeId);
			if (!gradingSchemeHdr.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}

			GradingSchemeHdr obj = gradingSchemeHdr.get();

			GradingSchemeHdrDTO responseObj = new GradingSchemeHdrDTO();
			responseObj.setId(obj.getId());
			responseObj.setSchemeDetails(obj.getSchemeDetails());
			responseObj.setSchemeName(obj.getSchemeName());
			
			List<GradingSchemeDtlDTO> gradingSchemeDtlList = new ArrayList<GradingSchemeDtlDTO>();
			for (GradingSchemeDtl dltObj : obj.getGradingSchemeDtlList()) {
				GradingSchemeDtlDTO schemeDtlDTO = new GradingSchemeDtlDTO();
				schemeDtlDTO.setId(dltObj.getId());
				schemeDtlDTO.setGradeName(dltObj.getGradeName());
				schemeDtlDTO.setGradePoints(dltObj.getGradePoints());
				schemeDtlDTO.setRangeFrom(dltObj.getRangeFrom());
				schemeDtlDTO.setRangeTo(dltObj.getRangeTo());
				schemeDtlDTO.setCreatedOn(dltObj.getCreatedOn());
				gradingSchemeDtlList.add(schemeDtlDTO);
			}

			//Collections.sort(gradingSchemeDtlList, (i,j)->i.getCreatedOn().compareTo(j.getCreatedOn()));
			Collections.sort(gradingSchemeDtlList, (i,j)->i.getRangeFrom().compareTo(j.getRangeFrom()));
			
			responseObj.setGradingDtlDTOList(gradingSchemeDtlList);

			return responseGenerator.successGetResponse(context, messageSource.getMessage("grading.scheme.Hdr.get"),
					responseObj, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to fetch all TransportRoute.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<GradingSchemeHdrDTO> responseObjList = new ArrayList<GradingSchemeHdrDTO>();
		GradingSchemeHdrDTO responseObj = null;
		for (GradingSchemeHdr hdrObj : gradingSchemeHdrService.findAll()) {
			responseObj = new GradingSchemeHdrDTO();
			responseObj.setId(hdrObj.getId());
			responseObj.setSchemeDetails(hdrObj.getSchemeDetails());
			responseObj.setSchemeName(hdrObj.getSchemeName());
			List<GradingSchemeDtlDTO> gradingSchemeDtlList = new ArrayList<GradingSchemeDtlDTO>();
			List<GradingSchemeDtl>dtlList=hdrObj.getGradingSchemeDtlList();
			
			for (GradingSchemeDtl dltObj : hdrObj.getGradingSchemeDtlList()) {
				GradingSchemeDtlDTO schemeDtlDTO = new GradingSchemeDtlDTO();
				schemeDtlDTO.setId(dltObj.getId());
				schemeDtlDTO.setGradeName(dltObj.getGradeName());
				schemeDtlDTO.setGradePoints(dltObj.getGradePoints());
				schemeDtlDTO.setRangeFrom(dltObj.getRangeFrom());
				schemeDtlDTO.setRangeTo(dltObj.getRangeTo());
				gradingSchemeDtlList.add(schemeDtlDTO);
			}
			gradingSchemeDtlList.sort((o1,o2)->o1.getRangeFrom().compareTo(o2.getRangeFrom()));
			responseObj.setGradingDtlDTOList(gradingSchemeDtlList);
			responseObjList.add(responseObj);
			responseObjList.sort((o1,o2)->o1.getSchemeName().compareToIgnoreCase(o2.getSchemeName()));

		}
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("grading.scheme.Hdr.get"),
					responseObjList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
	
	@ApiOperation(value = " Allows to delete specific grading scheme by id.", response = Response.class)
	@DeleteMapping(value = "/delete/{id}", produces = "application/json")
	public ResponseEntity<?> delete(@ApiParam(value = "Grading scheme id") @PathVariable("id") UUID id,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		GradingSchemeHdr obj = gradingSchemeHdrService.findById(id).get();
		if (null == obj) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_OBJECT_REFERENCE,HttpStatus.BAD_REQUEST);
		}
		
		try {
			return gradingSchemeHdrService.deleteById(id,context);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, messageSource.getMessage("grading.scheme.invalid.delete"),HttpStatus.BAD_REQUEST);
		}

	}
	
}
