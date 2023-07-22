package com.iskool.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

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
import com.iskool.dto.AcadmicRecordDtlDTO;
import com.iskool.dto.ExamScheduleDtlDTO;
import com.iskool.dto.ExamScheduleHdrDTO;
import com.iskool.dto.ExamTimeTableDtlDTO;
import com.iskool.dto.SectionAllotmentDTO;
import com.iskool.entity.AcademicSessionDtl;
import com.iskool.entity.AcademicSessionHdr;
import com.iskool.entity.AcademicStandardFee;
import com.iskool.entity.AcademicYear;

import com.iskool.entity.Exam;
import com.iskool.entity.ExamScheduleDtl;
import com.iskool.entity.ExamScheduleHdr;
import com.iskool.entity.Standard;
import com.iskool.entity.State;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.ExamScheduleDtlService;
import com.iskool.service.ExamScheduleHdrService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StandardService;
import com.iskool.util.IskoolUtil;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/exam/schedule")
@Api(value = "Exam Schedule Rest Api", produces = "application/json", consumes = "application/json")
public class ExamScheduleController {

	private static final Logger logger = Logger.getLogger(ExamScheduleHdrDTO.class);

	private @NotNull ResponseGenerator responseGenerator;

	private @NonNull StandardService standardService;

	private @NonNull AcademicYearService academicYearService;

	private @NonNull ExamScheduleDtlService examScheduleDtlService;

	private @NonNull ExamScheduleHdrService examScheduleHdrService;

	private @NonNull MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all exam schedule.", response = Response.class)
	@GetMapping(value = "/get/{stdId}/{academicYearId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("stdId") UUID stdId, @PathVariable("academicYearId") UUID academicYearId,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<Standard> standardOptional = standardService.findById(stdId);
		if (!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);
		if (!academicYearOptional.isPresent()) {

			return responseGenerator.errorResponse(context, messageSource.getMessage("create.current.academic.year"),
					HttpStatus.BAD_REQUEST);
		}
		ExamScheduleHdrDTO examScheduleHdrDTOObj = new ExamScheduleHdrDTO();
		examScheduleHdrDTOObj.setStdId(stdId);
		examScheduleHdrDTOObj.setAcademicYearId(academicYearId);
		Optional<ExamScheduleHdr> examScheduleHdrOptional = examScheduleHdrService
				.findByStandardObjAndAcademicYearObj(standardOptional.get(), academicYearOptional.get());
		if (!examScheduleHdrOptional.isPresent()) {
			examScheduleHdrDTOObj = new ExamScheduleHdrDTO();
			List<ExamScheduleDtlDTO> examScheduleDtlDTOList = new ArrayList<>();
			examScheduleHdrDTOObj.setExamScheduleDtlDTOList(examScheduleDtlDTOList);
		}

		else {
			examScheduleHdrDTOObj.setId(examScheduleHdrOptional.get().getId());
			ExamScheduleDtlDTO examScheduleDtlDTOObj = null;
			List<ExamScheduleDtlDTO> examScheduleDtlDTOList = new ArrayList<>();
			for (ExamScheduleDtl examScheduleDtlObj : examScheduleHdrOptional.get().getExamScheduleDtlList()) {
				examScheduleDtlDTOObj = new ExamScheduleDtlDTO();
				examScheduleDtlDTOObj.setId(examScheduleDtlObj.getId());
				examScheduleDtlDTOObj.setExam(examScheduleDtlObj.getExamObject().getExamName());
				examScheduleDtlDTOObj.setExamId(examScheduleDtlObj.getExamObject().getId());
				examScheduleDtlDTOObj.setStartDate(examScheduleDtlObj.getStartDate());
				examScheduleDtlDTOObj.setEndDate(examScheduleDtlObj.getEndDate());
				examScheduleDtlDTOList.add(examScheduleDtlDTOObj);
			}
			examScheduleHdrDTOObj.setExamScheduleDtlDTOList(examScheduleDtlDTOList);
		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("exam.schedule.get"),
					examScheduleHdrDTOObj, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create or update exam schedule.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The Exam Schedule request payload") @RequestBody ExamScheduleHdrDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		for (ExamScheduleDtlDTO dtlDto : request.getExamScheduleDtlDTOList()) {
			if (dtlDto.getStartDate().equals(dtlDto.getEndDate()) || dtlDto.getStartDate().after(dtlDto.getEndDate())) {
				return responseGenerator.errorResponse(context,
						messageSource.getMessage("start.date.and.end.date.valid"), HttpStatus.BAD_REQUEST);
			}
		}

		if (request.getId() != null) {
			Optional<ExamScheduleHdr> examScheduleHdrOptional = examScheduleHdrService.findById(request.getId());
			ExamScheduleHdr examScheduleHdrObj = examScheduleHdrOptional.get();
			Map<UUID, ExamScheduleDtl> dtlIdMap = examScheduleHdrOptional.get().getExamScheduleDtlList().stream()
					.collect(Collectors.toMap(ExamScheduleDtl::getId, Function.identity()));
			Optional<AcademicYear> academicYearOptional = academicYearService.findById(request.getAcademicYearId());
			if (!academicYearOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.id.invalid"),
						HttpStatus.BAD_REQUEST);
			}
			examScheduleHdrObj.setAcademicYearObj(academicYearOptional.get());
			Optional<Standard> stdOptional = standardService.findById(request.getStdId());
			if (!stdOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
						HttpStatus.BAD_REQUEST);
			}

			examScheduleHdrObj.setStandardObj(stdOptional.get());
			examScheduleHdrObj.setStatus(Status.ACTIVE);
			List<UUID> examIdList = new ArrayList<UUID>();
			List<ExamScheduleDtl> examScheduleDtlList = new ArrayList<ExamScheduleDtl>();
			for (ExamScheduleDtlDTO examScheduleDtlDTO : request.getExamScheduleDtlDTOList()) {
				ExamScheduleDtl examScheduleDtlObj = dtlIdMap.remove(examScheduleDtlDTO.getId());
				if (examIdList.contains(examScheduleDtlDTO.getExamId())) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("exam.already.declare"),
							HttpStatus.BAD_REQUEST);
				} else {
					examIdList.add(examScheduleDtlDTO.getExamId());
				}
				if (examScheduleDtlObj == null) {
					examScheduleDtlObj = new ExamScheduleDtl();
					Exam exam = new Exam();
					exam.setId(examScheduleDtlDTO.getExamId());
					examScheduleDtlObj.setExamScheduleHdrObject(examScheduleHdrObj);
					examScheduleDtlObj.setExamObject(exam);
					examScheduleDtlObj.setStartDate(examScheduleDtlDTO.getStartDate());
					examScheduleDtlObj.setEndDate(examScheduleDtlDTO.getEndDate());
					examScheduleDtlObj.setStatus(Status.ACTIVE);
					examScheduleDtlList.add(examScheduleDtlObj);

				} else {
					Exam exam = new Exam();
					exam.setId(examScheduleDtlDTO.getExamId());
					examScheduleDtlObj.setExamScheduleHdrObject(examScheduleHdrObj);
					examScheduleDtlObj.setExamObject(exam);
					examScheduleDtlObj.setStartDate(examScheduleDtlDTO.getStartDate());
					examScheduleDtlObj.setEndDate(examScheduleDtlDTO.getEndDate());
					examScheduleDtlList.add(examScheduleDtlObj);
				}

			}
			examScheduleHdrObj.setDtlIdMap(dtlIdMap);
			examScheduleHdrObj.setExamScheduleDtlList(examScheduleDtlList);
			examScheduleHdrService.saveOrUpdate(examScheduleHdrObj);

		}

		else {

			for (ExamScheduleDtlDTO dtlDto : request.getExamScheduleDtlDTOList()) {
				if (dtlDto.getStartDate().equals(dtlDto.getEndDate())
						|| dtlDto.getStartDate().after(dtlDto.getEndDate())) {
					return responseGenerator.errorResponse(context,
							messageSource.getMessage("start.date.and.end.date.valid"), HttpStatus.BAD_REQUEST);
				}

				LocalDate date = dtlDto.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if (date.isBefore(LocalDate.now())) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid.start.date"),
							HttpStatus.BAD_REQUEST);
				}

			}

			ExamScheduleHdr examScheduleHdr = new ExamScheduleHdr();
			AcademicYear academicYearObj = new AcademicYear();
			academicYearObj.setId(request.getAcademicYearId());
			examScheduleHdr.setAcademicYearObj(academicYearObj);
			Standard standardObj = new Standard();
			standardObj.setId(request.getStdId());
			examScheduleHdr.setStandardObj(standardObj);
			examScheduleHdr.setStatus(Status.ACTIVE);

			List<ExamScheduleDtl> examScheduleDtlList = new ArrayList<ExamScheduleDtl>();
			ExamScheduleDtl examScheduleDtlObj = null;
			List<UUID> examIdList = new ArrayList<UUID>();
			for (ExamScheduleDtlDTO examScheduleDtlDTO : request.getExamScheduleDtlDTOList()) {
				examScheduleDtlObj = new ExamScheduleDtl();

				if (examIdList.contains(examScheduleDtlDTO.getExamId())) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("exam.already.declare"),
							HttpStatus.BAD_REQUEST);
				} else {
					examIdList.add(examScheduleDtlDTO.getExamId());
				}
				Exam examObj = new Exam();
				examObj.setId(examScheduleDtlDTO.getExamId());
				examScheduleDtlObj.setExamObject(examObj);
				examScheduleDtlObj.setStartDate(examScheduleDtlDTO.getStartDate());
				examScheduleDtlObj.setEndDate(examScheduleDtlDTO.getEndDate());
				examScheduleDtlObj.setStatus(Status.ACTIVE);
				examScheduleDtlObj.setExamScheduleHdrObject(examScheduleHdr);
				examScheduleDtlList.add(examScheduleDtlObj);

			}
			examScheduleHdr.setExamScheduleDtlList(examScheduleDtlList);
			examScheduleHdrService.saveOrUpdate(examScheduleHdr);
		}

		try {
			return responseGenerator.successResponse(context,
					request.getId() != null ? messageSource.getMessage("exam.schedule.update")
							: messageSource.getMessage("exam.schedule.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

}