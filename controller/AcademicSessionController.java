package com.iskool.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.AcademicSessionDtlDTO;
import com.iskool.dto.AcademicSessionHdrDTO;
import com.iskool.entity.AcademicSessionDtl;
import com.iskool.entity.AcademicSessionHdr;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.Year;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicSessionHdrService;
import com.iskool.service.AcadenicSessionDtlService;
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
@RequestMapping("/api/academic/session")
@Api(value = "AcademicSession Rest API", produces = "application/json", consumes = "application/json")
public class AcademicSessionController {
	private static final Logger logger = Logger.getLogger(AcademicSessionHdrDTO.class);
	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull AcademicSessionHdrService academicSessionHdrService;
	
	private @NonNull AcadenicSessionDtlService acadenicSessionDtlService;
	
	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new academicSession.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The AcademicSession request payload") @RequestBody AcademicSessionHdrDTO request,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,HttpStatus.BAD_REQUEST);
		}

		if (request.getStartDate().equals(request.getEndDate())|| request.getStartDate().after(request.getEndDate())) {

			return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid"), HttpStatus.BAD_REQUEST);
		}

		for (AcademicSessionDtlDTO dtlDto : request.getAcademicSessionDtlDTO()) {
			if (dtlDto.getStartDate().equals(dtlDto.getEndDate())|| dtlDto.getStartDate().after(dtlDto.getEndDate())) {

				return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid"), HttpStatus.BAD_REQUEST);
			}
		}
		if (request.getId() != null) {
			Optional<AcademicSessionHdr> academicSessionOptional = academicSessionHdrService.findById(request.getId());
			AcademicSessionHdr academicSessionObj = academicSessionOptional.get();
			Map<UUID, AcademicSessionDtl> dtlIdMap = academicSessionOptional.get().getAcedamicSessionDtlList().stream().collect(Collectors.toMap(AcademicSessionDtl::getId, a -> a));

			AcademicYear academicYear = new AcademicYear();
			academicYear.setId(request.getAcademicYearId());
			academicSessionObj.setAcademicYearObj(academicYear);
			academicSessionObj.setStatus(Status.ACTIVE);
			academicSessionObj.setStartDate(request.getStartDate());
			academicSessionObj.setEndDate(request.getEndDate());
			List<AcademicSessionDtl> dtlList = new ArrayList<AcademicSessionDtl>();
			for (AcademicSessionDtlDTO sessiondtlDto : request.getAcademicSessionDtlDTO()) {
				AcademicSessionDtl removedObj = dtlIdMap.remove(sessiondtlDto.getId());
				removedObj.setName(sessiondtlDto.getName());
				removedObj.setShortName(sessiondtlDto.getShortName());
				removedObj.setStartDate(sessiondtlDto.getStartDate());
				removedObj.setEndDate(sessiondtlDto.getEndDate());
				dtlList.add(removedObj);

			}
			academicSessionObj.setAcedamicSessionDtlList(dtlList);
			academicSessionHdrService.saveOrUpdate(academicSessionObj);

			for (UUID dtlId : dtlIdMap.keySet()) {
				acadenicSessionDtlService.deleteById(dtlId);
			}
			
			try {
				return responseGenerator.successResponse(context, messageSource.getMessage("academicSessionHdr.update"), HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}

		}

		else {
			AcademicSessionHdr academicSessionHdr = new AcademicSessionHdr();
			AcademicYear academicYearObj = new AcademicYear();
			academicYearObj.setId(request.getAcademicYearId());
			// year.setStatus(Status.ACTIVE);
			academicSessionHdr.setStatus(Status.ACTIVE);
			academicSessionHdr.setStartDate(request.getStartDate());
			academicSessionHdr.setEndDate(request.getEndDate());

			academicSessionHdr.setAcademicYearObj(academicYearObj);

			List<AcademicSessionDtl> acedamicSessionDtlList = new ArrayList<>();
			AcademicSessionDtl academicSessionDtl = null;
			for (AcademicSessionDtlDTO academicSessionDtlDTO : request.getAcademicSessionDtlDTO()) {
				academicSessionDtl = new AcademicSessionDtl();
				academicSessionDtl.setAcademicSessionObj(academicSessionHdr);
				academicSessionDtl.setName(academicSessionDtlDTO.getName());
				academicSessionDtl.setShortName(academicSessionDtlDTO.getShortName());
				academicSessionDtl.setStartDate(academicSessionDtlDTO.getStartDate());
				academicSessionDtl.setEndDate(academicSessionDtlDTO.getEndDate());
				academicSessionDtl.setStatus(Status.ACTIVE);
				acedamicSessionDtlList.add(academicSessionDtl);
			}

			academicSessionHdr.setAcedamicSessionDtlList(acedamicSessionDtlList);
			academicSessionHdrService.saveOrUpdate(academicSessionHdr);

		}

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("academicSessionHdr.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to fetch all academicSession", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{

			List<AcademicSessionHdrDTO> responseObjList = new ArrayList<AcademicSessionHdrDTO>();
			AcademicSessionHdrDTO responseObj = null;
			for (AcademicSessionHdr hdrObj : academicSessionHdrService.findAll()) {
				responseObj = new AcademicSessionHdrDTO();
				responseObj.setId(hdrObj.getId());
				responseObj.setAcademicYearId(hdrObj.getAcademicYearObj().getId());
				responseObj.setStartDate(hdrObj.getStartDate());
				responseObj.setEndDate(hdrObj.getEndDate());
				responseObj.setAcademicYearName(hdrObj.getAcademicYearObj().getName());

				List<AcademicSessionDtlDTO> academicDtlList = new ArrayList<AcademicSessionDtlDTO>();

				for (AcademicSessionDtl dltObj : hdrObj.getAcedamicSessionDtlList()) {
					AcademicSessionDtlDTO academicDtlDTO = new AcademicSessionDtlDTO();
					academicDtlDTO.setId(dltObj.getId());
					academicDtlDTO.setName(dltObj.getName());
					academicDtlDTO.setShortName(dltObj.getShortName());
					academicDtlDTO.setStartDate(dltObj.getStartDate());
					academicDtlDTO.setEndDate(dltObj.getEndDate());

					academicDtlList.add(academicDtlDTO);
				}
				academicDtlList=academicDtlList.stream().sorted((i,j)->i.getStartDate().compareTo(j.getStartDate())).collect(Collectors.toList());
				responseObj.setAcademicSessionDtlDTO(academicDtlList);
				responseObjList.add(responseObj);

			}

			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("Academicsession.get"),
						responseObjList, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
	}

	@ApiOperation(value = "Allows to fetch all subject configuration", response = Response.class)
	@GetMapping(value = "/get/{academicSessionHdrId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("academicSessionHdrId") UUID academicSessionHdrId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
			Optional<AcademicSessionHdr> academicStandardHdr = academicSessionHdrService.findById(academicSessionHdrId);
			if (!academicStandardHdr.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}

			//Optional<AcademicSessionHdr> academicSessionHdrOptional = academicSessionHdrService.findByAcademicSessionId(academicSessionHdrId);
			AcademicSessionHdr academicObj = academicStandardHdr.get();
			AcademicSessionHdrDTO academicSessionHdrDTO = new AcademicSessionHdrDTO();
			academicSessionHdrDTO.setId(academicObj.getId());
			academicSessionHdrDTO.setAcademicYearId(academicObj.getAcademicYearObj().getId());
			academicSessionHdrDTO.setStartDate(academicObj.getStartDate());
			academicSessionHdrDTO.setEndDate(academicObj.getEndDate());

			AcademicSessionDtlDTO dtlObj = null;
			List<AcademicSessionDtlDTO> dtlList = new ArrayList<AcademicSessionDtlDTO>();
			for (AcademicSessionDtl dtl : academicObj.getAcedamicSessionDtlList()) {
				dtlObj = new AcademicSessionDtlDTO();
				dtlObj.setId(dtl.getId());
				dtlObj.setName(dtl.getName());
				dtlObj.setShortName(dtl.getShortName());
				dtlObj.setStartDate(dtl.getStartDate());
				dtlObj.setEndDate(dtl.getEndDate());
				dtlList.add(dtlObj);
			}
			dtlList=dtlList.stream().sorted((i,j)->i.getStartDate().compareTo(j.getStartDate())).collect(Collectors.toList());
			academicSessionHdrDTO.setAcademicSessionDtlDTO(dtlList);
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("AcademicSessiondtl.get"),
						academicSessionHdrDTO, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
	

}
