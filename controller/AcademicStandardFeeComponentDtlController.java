//package com.iskool.controller;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.iskool.dto.StandardFeeDtlComponentDTO;
//import com.iskool.dto.StandardFeeDtlDTO;
//import com.iskool.dto.StandardFeeHdrDTO;
//import com.iskool.entity.FeesMaster;
//import com.iskool.entity.FeesMasterComponent;
//import com.iskool.entity.Standard;
//import com.iskool.entity.StandardFeeConfigurationDtl;
//import com.iskool.entity.StandardFeeConfigurationHdr;
//import com.iskool.enumeration.Status;
//import com.iskool.response.Response;
//import com.iskool.response.ResponseGenerator;
//import com.iskool.response.TransactionContext;
//import com.iskool.service.AcademicStandardFeeComponentService;
//import com.iskool.service.AcademicStandardFeeService;
//import com.iskool.service.AcademicYearService;
//import com.iskool.service.FeesMasterComponentService;
//import com.iskool.service.FeesMasterService;
//import com.iskool.service.MessagePropertyService;
//import com.iskool.service.StandardFeeConfigurationDtlService;
//import com.iskool.service.StandardFeeConfigurationHdrService;
//import com.iskool.service.StandardService;
//import com.iskool.util.message.ResponseMessage;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.AllArgsConstructor;
//import lombok.NonNull;
//
//@CrossOrigin(origins = "*", maxAge = 3600)
//@RestController
//@AllArgsConstructor(onConstructor_ = { @Autowired })
//@RequestMapping("/api/academic/fee/component")
//@Api(value = "Academci fee Rest API", produces = "application/json", consumes = "application/json")
//public class AcademicStandardFeeComponentDtlController {
//
//	private static final Logger logger = Logger.getLogger(AcademicStandardFeeComponentDtlController.class);
//
//	private @NonNull ResponseGenerator responseGenerator;
//
//	private @NonNull AcademicStandardFeeService academicStandardFeeService;
//
//	private @NonNull StandardFeeConfigurationHdrService standardFeeConfigurationHdrService;
//
//	private @NonNull StandardFeeConfigurationDtlService standardFeeConfigurationDtlService;
//
//	private @NonNull StandardService standardService;
//
//	private @NonNull FeesMasterService feesMasterService;
//
//	private @NonNull AcademicYearService academicYearService;
//
//	private @NonNull AcademicStandardFeeComponentService academicStandardFeeComponentService;
//
//	private @NonNull FeesMasterService feesService;
//
//	private @NonNull FeesMasterComponentService feeCompService;
//
//	@Autowired
//	MessagePropertyService messageSource;
//
//	
//	@ApiOperation(value = "Allows to fetch all fee declaration by standard id", response = Response.class)
//	@GetMapping(value = "/get/{stdId}", produces = "application/json")
//	public ResponseEntity<?> getAllFeesByStandardId(@PathVariable("stdId") UUID stdId,
//			@RequestHeader HttpHeaders httpHeader) throws Exception {
//		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
//
//		Standard std = new Standard();
//		std.setId(stdId);
//
//		Optional<StandardFeeConfigurationHdr> standardFeeConfigurationHdrObj = standardFeeConfigurationHdrService
//				.findByStandardObj(std);
//
//		if (!standardFeeConfigurationHdrObj.isPresent()) {
//
//			return responseGenerator.errorResponse(context,
//					messageSource.getMessage("standard.Fee"), HttpStatus.BAD_REQUEST);
//		}
//
//		Map<UUID, List<StandardFeeConfigurationDtl>> feeDtlMap = new HashMap<UUID, List<StandardFeeConfigurationDtl>>();
//		for (StandardFeeConfigurationDtl dtlObj : standardFeeConfigurationHdrObj.get()
//				.getStandardFeeConfigurationDtlList()) {
//			List<StandardFeeConfigurationDtl> dtlList = feeDtlMap.get(dtlObj.getFeesMasterObj().getId());
//			if (null == dtlList || dtlList.isEmpty()) {
//				dtlList = new ArrayList<StandardFeeConfigurationDtl>();
//			}
//			dtlList.add(dtlObj);
//			feeDtlMap.put(dtlObj.getFeesMasterObj().getId(), dtlList);
//		}
//
//		// Map<UUID, List<StandardFeeConfigurationDtl>> feeDtlMap =
//		// standardFeeConfigurationHdrObj.get().getStandardFeeConfigurationDtlList().stream().collect(Collectors.groupingBy(d->d.getFeesMasterObj().getId()));
//		List<StandardFeeDtlDTO> feeDtlList = new ArrayList<StandardFeeDtlDTO>();
//
//		for (UUID feeId : feeDtlMap.keySet()) {
//
//			List<StandardFeeConfigurationDtl> dtlList = feeDtlMap.get(feeId);
//
//			List<StandardFeeDtlComponentDTO> feeCompList = new ArrayList<StandardFeeDtlComponentDTO>();
//
//			Double total = 0.0;
//			for (StandardFeeConfigurationDtl standardFeeConfigurationDtl : dtlList) {
//				StandardFeeDtlComponentDTO componentDTO = new StandardFeeDtlComponentDTO();
//				componentDTO.setAmount(standardFeeConfigurationDtl.getAmount());
//				componentDTO.setId(standardFeeConfigurationDtl.getFeesMasterComponentObj().getId());
//				componentDTO.setName(standardFeeConfigurationDtl.getFeesMasterComponentObj().getName());
//				total = total + componentDTO.getAmount();
//				feeCompList.add(componentDTO);
//			}
//			StandardFeeDtlDTO feeDtlDTO = new StandardFeeDtlDTO();
//			feeDtlDTO.setFeeCompList(feeCompList);
//			feeDtlDTO.setFeeId(feeId);
//			Optional<FeesMaster> feeObj = feesService.findById(feeId);
//			feeDtlDTO.setFeesName(feeObj.get().getFeesName());
//			feeDtlDTO.setTotal(total);
//
//			feeDtlList.add(feeDtlDTO);
//		}
//
//		StandardFeeHdrDTO responseObj = new StandardFeeHdrDTO();
//		responseObj.setStdId(stdId);
//		responseObj.setFeeDtlList(feeDtlList);
//
//		try {
//			return responseGenerator.successGetResponse(context,
//					messageSource.getMessage("standard.Fee.Configuration.Hdr.fetch"), responseObj, HttpStatus.OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
//		}
//	}
//	
//	@ApiOperation(value = "Allows to create new standard fee.", response = Response.class)
//	@PostMapping(value = "/create", produces = "application/json")
//	public ResponseEntity<?> update(
//			@ApiParam(value = "The StandardFeeHdrDTO request payload") @RequestBody StandardFeeHdrDTO request,
//			@RequestHeader HttpHeaders httpHeader) throws Exception {
//		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
//		if (null == request) {
//			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
//					HttpStatus.BAD_REQUEST);
//		}
//		Optional<Standard> standardObj = standardService.findById(request.getStdId());
//		if (!standardObj.isPresent()) {
//			return responseGenerator.errorResponse(context, messageSource.getMessage("standard.fetch"),
//					HttpStatus.BAD_REQUEST);
//		}
//		Optional<StandardFeeConfigurationHdr> standardFeeConfigurationHdrObjOptional = standardFeeConfigurationHdrService
//				.findByStandardObj(standardObj.get());
//		Map<String, StandardFeeConfigurationDtl> existingMap = new HashMap<String, StandardFeeConfigurationDtl>();
//		StandardFeeConfigurationHdr standardFeeConfigurationHdrObj = null;
//		if (standardFeeConfigurationHdrObjOptional.isPresent()) {
//			for (StandardFeeConfigurationDtl feeDtlObj : standardFeeConfigurationHdrObjOptional.get()
//					.getStandardFeeConfigurationDtlList()) {
//				existingMap.put(feeDtlObj.getFeesMasterObj().getId() + "_" + feeDtlObj.getFeesMasterComponentObj().getId(),
//						feeDtlObj);
//			}
//
//			standardFeeConfigurationHdrObj = standardFeeConfigurationHdrObjOptional.get();
//		} else {
//			standardFeeConfigurationHdrObj = new StandardFeeConfigurationHdr();
//
//		}
//
//		standardFeeConfigurationHdrObj.setStandardObj(standardObj.get());
//		standardFeeConfigurationHdrObj.setStatus(Status.ACTIVE);
//		List<StandardFeeConfigurationDtl> dtlList = new ArrayList<StandardFeeConfigurationDtl>();
//
//		for (StandardFeeDtlDTO feeDtlReqObj : request.getFeeDtlList()) {
//
//			for (StandardFeeDtlComponentDTO feeDtlCompReqObj : feeDtlReqObj.getFeeCompList()) {
//				Optional<FeesMaster> feeObj = feesService.findById(feeDtlReqObj.getFeeId());
//				if (!feeObj.isPresent()) {
//					continue;
//				}
//				Optional<FeesMasterComponent> feeCompObj = feeCompService.findById(feeDtlCompReqObj.getId());
//				if (!feeCompObj.isPresent()) {
//					continue;
//				}
//				StandardFeeConfigurationDtl feeDtlObj = existingMap
//						.remove(feeDtlReqObj.getFeeId() + "_" + feeDtlCompReqObj.getId());
//
//				if (null != feeDtlObj) {
//					feeDtlObj.setAmount(feeDtlCompReqObj.getAmount());
//					dtlList.add(feeDtlObj);
//
//				} else {
//					feeDtlObj = new StandardFeeConfigurationDtl();
//					feeDtlObj.setAmount(feeDtlCompReqObj.getAmount());
//					feeDtlObj.setFeesMasterObj(feeObj.get());
//					feeDtlObj.setFeesMasterComponentObj(feeCompObj.get());
//					feeDtlObj.setStandardFeeConfigurationHdrObj(standardFeeConfigurationHdrObj);
//					feeDtlObj.setStatus(Status.ACTIVE);
//					dtlList.add(feeDtlObj);
//				}
//			}
//		}
//		if (null != existingMap && !existingMap.isEmpty()) {
//			for (StandardFeeConfigurationDtl standardFeeConfigurationDtl : existingMap.values()) {
//				standardFeeConfigurationDtlService.deleteById(standardFeeConfigurationDtl.getId());
//			}
//		}
//		standardFeeConfigurationHdrObj.setStandardFeeConfigurationDtlList(dtlList);
//		standardFeeConfigurationHdrService.saveOrUpdate(standardFeeConfigurationHdrObj);
//		try {
//			return responseGenerator.successResponse(context, messageSource.getMessage("standardFeeConfigurationHdr.create"),
//					HttpStatus.OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
//		}
//	}
//	
//	}
//
//	
