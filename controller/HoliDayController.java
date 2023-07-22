package com.iskool.controller;

import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.HolidayDtlDTO;
import com.iskool.dto.HolidayHdrDTO;
import com.iskool.dto.HolidayLeaveDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.DayMaster;
import com.iskool.entity.HolidayDtl;
import com.iskool.entity.HolidayHdr;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.DayMasterService;
import com.iskool.service.HolidayDtlService;
import com.iskool.service.HolidayHdrService;
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
@RequestMapping("/api/holiday")
@Api(value = "Holiday Rest API", produces = "application/json", consumes = "application/json")
public class HoliDayController {
	private static final Logger logger = Logger.getLogger(HoliDayController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull HolidayHdrService holidayHdrService;
	private @NonNull HolidayDtlService holidayDtlService;
	private @NonNull DayMasterService dayMasterService;
	private @NonNull AcademicYearService academicYearService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to create new holiday.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The holiday request payload") @RequestBody HolidayHdrDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Optional<AcademicYear> academicYearOptional = academicYearService.findById(request.getAcademicYearId());
		AcademicYear academicYearObj = academicYearOptional.get();
		if (!academicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		List<String> nameList = new ArrayList<String>();
		List<Date> dateList = new ArrayList<Date>();

		List<HolidayDtl> holidayDtlList = new ArrayList<HolidayDtl>();

		HolidayHdr holidayHdrObj = null;
		Map<UUID, HolidayDtl> dtlIdMap = new HashMap<UUID, HolidayDtl>();
		if (request.getId() == null) {
			
			Optional<HolidayHdr> holidayHdrOptional = holidayHdrService.findByAcademicYearObj(academicYearOptional.get());
			if (holidayHdrOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("holiday.year.error"),
						HttpStatus.BAD_REQUEST);
			}
			holidayHdrObj = new HolidayHdr();
		}

		else {
			Optional<HolidayHdr> optional = holidayHdrService.findById(request.getId());
			if (!optional.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);

			}
			holidayHdrObj = optional.get();
			for (HolidayDtl obj : holidayHdrObj.getHolidayDtlList()) {
				dtlIdMap.put(obj.getId(), obj);

			}

		}

		AcademicYear academicYearObject = new AcademicYear();

		academicYearObject.setId(academicYearOptional.get().getId());
		academicYearObject.setName(academicYearOptional.get().getName());
		holidayHdrObj.setAcademicYearObj(academicYearObject);

		for (HolidayDtlDTO dtoObj : request.getHolidayDtlDtoList()) {
			HolidayDtl holidayDtlObj = dtlIdMap.remove(dtoObj.getId());
			if (holidayDtlObj == null) {
				holidayDtlObj = new HolidayDtl();
			}

			if (nameList.contains(dtoObj.getName())) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("holiday.name"),
						HttpStatus.BAD_REQUEST);
			} else {
				nameList.add(dtoObj.getName());
			}

			if (dateList.contains(dtoObj.getDate())) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("holiday.date"),
						HttpStatus.BAD_REQUEST);
			} else {
				dateList.add(dtoObj.getDate());
			}
			
			if (dtoObj.getDate().after(academicYearObj.getToDate())|| dtoObj.getDate().before(academicYearObj.getFromDate())) {

				return responseGenerator.errorResponse(context, messageSource.getMessage("holiday.date.valid"), HttpStatus.BAD_REQUEST);
			}
			holidayDtlObj.setHolidayHdrObj(holidayHdrObj);
			holidayDtlObj.setName(dtoObj.getName());
			holidayDtlObj.setDate(dtoObj.getDate());
			holidayDtlObj.setDescription(dtoObj.getDescription());

			DayMaster dayMasterObj = new DayMaster();
			dayMasterObj.setId(dtoObj.getDayId());
			holidayDtlObj.setDayMasterObj(dayMasterObj);
			holidayDtlObj.setStatus(Status.ACTIVE);

			holidayDtlList.add(holidayDtlObj);
		}
		holidayHdrObj.setHolidayDtlList(holidayDtlList);
		holidayHdrObj.setStatus(Status.ACTIVE);
		holidayHdrService.saveOrUpdate(holidayHdrObj);

		for (UUID id : dtlIdMap.keySet()) {
			holidayDtlService.deleteById(id);
		}

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("holiday.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Holiday.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAll(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		List<HolidayHdrDTO> holidayResponseList = new ArrayList<HolidayHdrDTO>();
		HolidayHdrDTO holidayResponseObj = null;
		for (HolidayHdr hdrObj : holidayHdrService.findAll()) {
			holidayResponseObj = new HolidayHdrDTO();

			holidayResponseObj.setId(hdrObj.getId());
			holidayResponseObj.setAcademicYearId(hdrObj.getAcademicYearObj().getId());
			holidayResponseObj.setAcademicYearName(hdrObj.getAcademicYearObj().getName());

			List<HolidayDtlDTO> holidayDtlDtoList = new ArrayList<HolidayDtlDTO>();
			for (HolidayDtl dltObj : hdrObj.getHolidayDtlList()) {

				HolidayDtlDTO holidayDtlDTO = new HolidayDtlDTO();
				holidayDtlDTO.setId(dltObj.getId());
				holidayDtlDTO.setName(dltObj.getName());
				holidayDtlDTO.setDate(dltObj.getDate());
				holidayDtlDTO.setDayId(dltObj.getDayMasterObj().getId());
				holidayDtlDTO.setDayName(dltObj.getDayMasterObj().getName());
				holidayDtlDTO.setDescription(dltObj.getDescription());
				holidayDtlDtoList.add(holidayDtlDTO);
			}
			holidayResponseObj.setHolidayDtlDtoList(holidayDtlDtoList);
			holidayResponseList.add(holidayResponseObj);

		}
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("holiday.get"),
					holidayResponseList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to fetch holiday by particular academic year.", response = Response.class)
	@GetMapping(value = "/get/{id}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("id") UUID id, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			Optional<HolidayHdr> holidayHdr = holidayHdrService.findById(id);
			if (!holidayHdr.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);
			}

			HolidayHdr holidayHdrobj = holidayHdr.get();

			HolidayHdrDTO holidayresponseObj = new HolidayHdrDTO();
			holidayresponseObj.setId(holidayHdrobj.getId());
			holidayresponseObj.setAcademicYearId(holidayHdrobj.getAcademicYearObj().getId());
			holidayresponseObj.setAcademicYearName(holidayHdrobj.getAcademicYearObj().getName());

			List<HolidayDtlDTO> holidayDtlDtoList = new ArrayList<HolidayDtlDTO>();
			for (HolidayDtl dltObj : holidayHdrobj.getHolidayDtlList()) {

				HolidayDtlDTO holidayDtlDTO = new HolidayDtlDTO();
				holidayDtlDTO.setId(dltObj.getId());
				holidayDtlDTO.setName(dltObj.getName());
				holidayDtlDTO.setDate(dltObj.getDate());
				holidayDtlDTO.setDayId(dltObj.getDayMasterObj().getId());
				holidayDtlDTO.setDayName(dltObj.getDayMasterObj().getName());
				holidayDtlDTO.setDescription(dltObj.getDescription());
				holidayDtlDtoList.add(holidayDtlDTO);
			}
			holidayresponseObj.setHolidayDtlDtoList(holidayDtlDtoList);

			return responseGenerator.successGetResponse(context, messageSource.getMessage("holiday.get"),
					holidayresponseObj, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@ApiOperation(value = "Allows to fetch holiday by particular academic year.", response = Response.class)
	@PutMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> get(@RequestBody HolidayLeaveDTO request, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if(null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,HttpStatus.BAD_REQUEST);
		}
		 
		List<HolidayDtl> holidayDtlList = holidayDtlService.findByDateBetween(request.getFromDate(), request.getToDate());
		
		HolidayDtlDTO holidayDtlDTO = null;
		List<HolidayDtlDTO> holidayDtlDTOList = new ArrayList<>();
		for(HolidayDtl holidayDtlObj : holidayDtlList) {
			holidayDtlDTO = new HolidayDtlDTO();
			holidayDtlDTO.setId(holidayDtlObj.getId());
			holidayDtlDTO.setDayId(holidayDtlObj.getDayMasterObj().getId());
			holidayDtlDTO.setDayName(holidayDtlObj.getDayMasterObj().getName());
			holidayDtlDTO.setName(holidayDtlObj.getName());
			holidayDtlDTO.setDate(holidayDtlObj.getDate());
			holidayDtlDTO.setDescription(holidayDtlObj.getDescription());
			holidayDtlDTOList.add(holidayDtlDTO);
		}
		
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("holiday.get"),
					holidayDtlDTOList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		
		
		
}
}