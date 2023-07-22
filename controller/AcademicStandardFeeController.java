package com.iskool.controller;

import java.time.LocalDate;
import java.time.ZoneId;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.AcademicStandardFeeDtlComponentDTO;
import com.iskool.dto.AcademicStandardFeeDtlDTO;
import com.iskool.dto.AcademicStandardFeeHdrDTO;
import com.iskool.entity.AcademicStandardFee;
import com.iskool.entity.AcademicStandardFeeComponent;
import com.iskool.entity.AcademicStandardFeeComponentDtl;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.FeesMaster;
import com.iskool.entity.FeesMasterComponent;
import com.iskool.entity.Standard;
import com.iskool.entity.StandardFeeConfigurationDtl;
import com.iskool.entity.StandardFeeConfigurationHdr;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicStandardFeeComponentDtlService;
import com.iskool.service.AcademicStandardFeeComponentService;
import com.iskool.service.AcademicStandardFeeService;
import com.iskool.service.AcademicYearService;
import com.iskool.service.FeesMasterComponentService;
import com.iskool.service.FeesMasterService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StandardFeeConfigurationDtlService;
import com.iskool.service.StandardFeeConfigurationHdrService;
import com.iskool.service.StandardService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/academic/fee")
@Api(value = "Academci fee Rest API", produces = "application/json", consumes = "application/json")
public class AcademicStandardFeeController {

	private static final Logger logger = Logger.getLogger(AcademicStandardFeeController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull AcademicStandardFeeService academicStandardFeeService;

	private @NonNull StandardFeeConfigurationHdrService standardFeeConfigurationHdrService;

	private @NonNull StandardFeeConfigurationDtlService standardFeeConfigurationDtlService;

	private @NonNull StandardService standardService;

	private @NonNull FeesMasterService feesMasterService;

	private @NonNull AcademicYearService academicYearService;

	private @NonNull AcademicStandardFeeComponentService academicStandardFeeComponentService;

	private @NonNull AcademicStandardFeeComponentDtlService academicStandardFeeComponentDtlService;

	private @NonNull FeesMasterService feesService;

	private @NonNull FeesMasterComponentService feeCompService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to update or create existing academic fee info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The academic fee request payload") @RequestBody AcademicStandardFeeHdrDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		for (AcademicStandardFeeDtlDTO dtlObj : request.getAcademicStandardFeeDtlList()) {
			if (dtlObj.getFromDate().equals(dtlObj.getDueDate()) || dtlObj.getFromDate().after(dtlObj.getDueDate())) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("academicFee.invalid.date"),
						HttpStatus.BAD_REQUEST);
			}

			LocalDate date = dtlObj.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (date.isBefore(LocalDate.now())) {
				return responseGenerator.errorResponse(context,
						messageSource.getMessage("academicFee.invalid.from.date"), HttpStatus.BAD_REQUEST);
			}
		}

		if (null == request.getId()) {

			List<AcademicStandardFee> academicStandardFeesList = academicStandardFeeService.findAll();
			for (AcademicStandardFee academicStandardFeeObj : academicStandardFeesList) {
				if (request.getAcademicYearId().equals(academicStandardFeeObj.getYear().getId())
						&& request.getStdId().equals(academicStandardFeeObj.getStandardObj().getId())) {

					return responseGenerator.errorResponse(context, messageSource.getMessage("academicFee.duplicate"),
							HttpStatus.BAD_REQUEST);
				}

			}
			AcademicStandardFee academicStandardFee = new AcademicStandardFee();

			Standard standard = new Standard();
			standard.setId(request.getStdId());
			academicStandardFee.setStandardObj(standard);

			AcademicYear year = new AcademicYear();
			year.setId(request.getAcademicYearId());
			academicStandardFee.setYear(year);

			academicStandardFee.setStatus(Status.ACTIVE);
			academicStandardFeeService.saveOrUpdate(academicStandardFee);

			academicStandardFeeComponentService.saveOrUpdate(academicStandardFee, request);
		}

		else {
			Optional<AcademicStandardFee> academicStdFeeOptional = academicStandardFeeService.findById(request.getId());
			if (academicStdFeeOptional == null) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("academicFee.name"),
						HttpStatus.BAD_REQUEST);
			}

			AcademicStandardFee academicStdFeeObj = academicStdFeeOptional.get();
			Standard standardObj = new Standard();
			standardObj.setId(request.getStdId());
			academicStdFeeObj.setStandardObj(standardObj);

			AcademicYear year = new AcademicYear();
			year.setId(request.getAcademicYearId());
			academicStdFeeObj.setYear(year);
			academicStdFeeObj.setStatus(Status.ACTIVE);
			academicStandardFeeService.saveOrUpdate(academicStdFeeObj);

			academicStandardFeeComponentService.saveOrUpdate(academicStdFeeObj, request);
		}
		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("academicFee.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all academic fee types.", response = Response.class)
	@GetMapping(value = "/get/{stdId}/{academicYearId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("stdId") UUID stdId, @PathVariable("academicYearId") UUID academicYearId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<Standard> standard = standardService.findById(stdId);

		Optional<AcademicYear> year = academicYearService.findById(academicYearId);

		Optional<AcademicStandardFee> academicStandardfeeOptional = academicStandardFeeService
				.findByStandardObjAndYear(standard.get(), year.get());
		try {
			if (academicStandardfeeOptional.isPresent()) {

				AcademicStandardFeeHdrDTO academicStdFeeDTOObj = new AcademicStandardFeeHdrDTO();
				academicStdFeeDTOObj.setAcademicYearId(academicYearId);
				academicStdFeeDTOObj.setStdId(stdId);
				academicStdFeeDTOObj.setId(academicStandardfeeOptional.get().getId());

				List<AcademicStandardFeeComponent> academicStandardFeeComponents = academicStandardFeeComponentService
						.findByAcademicStandardFeeObj(academicStandardfeeOptional.get());

				List<AcademicStandardFeeDtlDTO> list = new ArrayList<AcademicStandardFeeDtlDTO>();
				AcademicStandardFeeDtlDTO academicStdFeeDtlDTOOObj = null;
				for (AcademicStandardFeeComponent academicStandardFeeComponentObj : academicStandardFeeComponents) {
					academicStdFeeDtlDTOOObj = new AcademicStandardFeeDtlDTO();
					academicStdFeeDtlDTOOObj.setId(academicStandardFeeComponentObj.getId());

					Optional<AcademicStandardFeeComponent> feeOptionalObj = academicStandardFeeComponentService
							.findById(academicStandardFeeComponentObj.getId());
					academicStdFeeDtlDTOOObj.setAcademicStdFeeId(feeOptionalObj.get().getId());

					Optional<FeesMaster> fee = feesMasterService
							.findById(academicStandardFeeComponentObj.getFeesMaster().getId());
					academicStdFeeDtlDTOOObj.setFeeId(fee.get().getId());
					academicStdFeeDtlDTOOObj.setFeesName(fee.get().getFeesName());
					academicStdFeeDtlDTOOObj.setFromDate(academicStandardFeeComponentObj.getFromDate());
					academicStdFeeDtlDTOOObj.setDueDate(academicStandardFeeComponentObj.getDueDate());
					list.add(academicStdFeeDtlDTOOObj);

					List<AcademicStandardFeeComponentDtl> academicStandardFeeComponentDtl = academicStandardFeeComponentDtlService
							.findByAcademicStdFeeComponentId(academicStandardFeeComponentObj.getId());

					List<AcademicStandardFeeDtlComponentDTO> academicStandardFeeDtlComponentList = new ArrayList<AcademicStandardFeeDtlComponentDTO>();

					AcademicStandardFeeDtlComponentDTO academicStandardFeeDtlComponentDTOObj = null;
					for (AcademicStandardFeeComponentDtl componentDtlObj : academicStandardFeeComponentDtl) {

						academicStandardFeeDtlComponentDTOObj = new AcademicStandardFeeDtlComponentDTO();

						academicStandardFeeDtlComponentDTOObj
								.setAcademicStandardFeeComponentId(componentDtlObj.getId());
						academicStandardFeeDtlComponentDTOObj.setAmount(componentDtlObj.getAmount());
						academicStandardFeeDtlComponentDTOObj.setId(componentDtlObj.getId());

						Optional<FeesMasterComponent> feeComponent = feeCompService
								.findById(componentDtlObj.getFeesMasterComponentId());

						academicStandardFeeDtlComponentDTOObj.setFeeMasterComponentId(feeComponent.get().getId());
						academicStandardFeeDtlComponentDTOObj.setFeeCompnentName(feeComponent.get().getName());
						academicStandardFeeDtlComponentList.add(academicStandardFeeDtlComponentDTOObj);
					}
					academicStdFeeDtlDTOOObj
							.setAcademicStandardFeeDtlComponentList(academicStandardFeeDtlComponentList);

				}
				academicStdFeeDTOObj.setAcademicStandardFeeDtlList(list);

				return responseGenerator.successGetResponse(context, messageSource.getMessage("academicFee.get"),
						academicStdFeeDTOObj, HttpStatus.OK);

			} else {

				Optional<StandardFeeConfigurationHdr> standardFeeHdr = standardFeeConfigurationHdrService
						.findByStandardObj(standard.get());

				Map<UUID, List<StandardFeeConfigurationDtl>> map = standardFeeHdr.get()
						.getStandardFeeConfigurationDtlList().stream()
						.collect(Collectors.groupingBy(t -> t.getFeesMasterObj().getId()));

				AcademicStandardFeeHdrDTO academicStdFee = new AcademicStandardFeeHdrDTO();
				academicStdFee.setAcademicYearId(academicYearId);
				academicStdFee.setStdId(stdId);

				List<AcademicStandardFeeDtlDTO> list = new ArrayList<AcademicStandardFeeDtlDTO>();
				AcademicStandardFeeDtlDTO feecomponentDtlDtoObj = null;

				for (UUID feeId : map.keySet()) {

					feecomponentDtlDtoObj = new AcademicStandardFeeDtlDTO();

					List<FeesMasterComponent> feeMasterList = feeCompService.findAllByFeeMasterId(feeId);

					Map<UUID, List<FeesMasterComponent>> feeComponentMap = feeMasterList.stream()
							.collect(Collectors.groupingBy(t -> t.getId()));

					List<AcademicStandardFeeDtlComponentDTO> academicStandardFeeDtlComponentList = new ArrayList<AcademicStandardFeeDtlComponentDTO>();
					for (StandardFeeConfigurationDtl feeComponentDtlObj : map.get(feeId)) {

						List<FeesMasterComponent> feecomponentObj = feeComponentMap
								.get(feeComponentDtlObj.getFeesMasterComponentObj().getId());

						Optional<FeesMaster> fee = feesMasterService
								.findById(feeComponentDtlObj.getFeesMasterObj().getId());

						feecomponentDtlDtoObj.setFeeId(fee.get().getId());
						feecomponentDtlDtoObj.setFeesName(fee.get().getFeesName());

						AcademicStandardFeeDtlComponentDTO academicStandardFeeDtlComponentDTOObj = new AcademicStandardFeeDtlComponentDTO();

						academicStandardFeeDtlComponentDTOObj.setFeeCompnentName(feecomponentObj.get(0).getName());
						academicStandardFeeDtlComponentDTOObj.setFeeMasterComponentId(feecomponentObj.get(0).getId());
						academicStandardFeeDtlComponentDTOObj.setAmount(feeComponentDtlObj.getAmount());
						academicStandardFeeDtlComponentList.add(academicStandardFeeDtlComponentDTOObj);

					}
					feecomponentDtlDtoObj.setAcademicStandardFeeDtlComponentList(academicStandardFeeDtlComponentList);

					list.add(feecomponentDtlDtoObj);
				}
				academicStdFee.setAcademicStandardFeeDtlList(list);
				return responseGenerator.successGetResponse(context, messageSource.getMessage("academicFee.get"),
						academicStdFee, HttpStatus.OK);
			}
		} catch (

		Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
}
