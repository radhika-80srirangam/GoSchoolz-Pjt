package com.iskool.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.MarkEntryDtlDTO;
import com.iskool.dto.MarkEntryHdrDTO;
import com.iskool.dto.MarkEntrySubjectDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.Exam;
import com.iskool.entity.ExamTimeTableDtl;
import com.iskool.entity.ExamTimeTableHdr;
import com.iskool.entity.MarkEntryDtl;
import com.iskool.entity.MarkEntryHdr;
import com.iskool.entity.Section;
import com.iskool.entity.Standard;
import com.iskool.entity.Student;
import com.iskool.entity.SubjectOfferingDTL;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.ExamService;
import com.iskool.service.ExamTimeTableHdrService;
import com.iskool.service.MarkEntryDtlService;
import com.iskool.service.MarkEntryHdrService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.SectionService;
import com.iskool.service.StandardService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.service.SubjectOfferingDTLService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/mark/entry")
@Api(value = "Mark entry Rest API", produces = "application/json", consumes = "application/json")
public class MarkEntryController {

	private static final Logger logger = Logger.getLogger(MarkEntryController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull ExamTimeTableHdrService examTimeTableHdrService;
	private @NonNull MarkEntryHdrService markEntryHdrService;
	private @NonNull MarkEntryDtlService markEntryDtlService;
	private @NonNull StudentRegistrationService studentRegistrationService;

	private @NonNull ExamService examService;
	private @NonNull AcademicYearService academicYearService;
	private @NonNull StandardService standardService;
	private @NonNull SubjectOfferingDTLService subjectOfferingDtlService;
	private @NonNull SectionService sectionService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch subject offering details.", response = Response.class)
	@GetMapping(value = "/search/{academicYearId}/{stdId}/{examId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("academicYearId") UUID academicYearId, @PathVariable("stdId") UUID stdId,
			@PathVariable("examId") UUID examId, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);
		AcademicYear academicYearObj = academicYearOptional.get();
		if (!academicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<Exam> examOptional = examService.findById(examId);
		Exam examObj = examOptional.get();
		if (!examOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<Standard> stdOptional = standardService.findById(stdId);
		Standard stdObj = stdOptional.get();
		if (!stdOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<ExamTimeTableHdr> examTimeTableHdrOptional = examTimeTableHdrService
				.findByAcademicYearObjAndStandardObjAndExamObj(academicYearObj, stdObj, examObj);
		List<ExamTimeTableDtl> examTimeTableDtlList = examTimeTableHdrOptional.get().getExamTimeTableDtlList();

		MarkEntrySubjectDTO subjectDtoObj = null;
		List<MarkEntrySubjectDTO> subjectDtoList = new ArrayList<>();

		for (ExamTimeTableDtl dtlObj : examTimeTableDtlList) {

			subjectDtoObj = new MarkEntrySubjectDTO();
			subjectDtoObj.setSubjectOfferingDtlId(dtlObj.getSubjectObj().getId());
			subjectDtoObj.setSubjectName(dtlObj.getSubjectObj().getSubjectObj().getName());
			subjectDtoObj.setSubjectType(dtlObj.getSubjectObj().getSubjectObj().getSubjectType());

			subjectDtoList.add(subjectDtoObj);
		}
		subjectDtoList.sort((o1, o2) -> o1.getSubjectName().compareToIgnoreCase(o2.getSubjectName()));
		try {
			return responseGenerator.successResponse(context, subjectDtoList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch student mark list based on a particular subject.", response = Response.class)
	@GetMapping(value = "/search/{academicYearId}/{stdId}/{sectionId}/{subOfferingDtlId}/{examId}", produces = "application/json")
	public ResponseEntity<?> get(@PathVariable("academicYearId") UUID academicYearId, @PathVariable("stdId") UUID stdId,
			@PathVariable("sectionId") UUID sectionId, @PathVariable("subOfferingDtlId") UUID subOfferingDtlId,
			@PathVariable("examId") UUID examId, @RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);
		AcademicYear academicYearObj = academicYearOptional.get();
		if (!academicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<Exam> examOptional = examService.findById(examId);
		Exam examObj = examOptional.get();
		if (!examOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<Standard> stdOptional = standardService.findById(stdId);
		Standard stdObj = stdOptional.get();
		if (!stdOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<SubjectOfferingDTL> subOfferingDtlOptional = subjectOfferingDtlService.findById(subOfferingDtlId);
		SubjectOfferingDTL subjectOfferingDtlObj = subOfferingDtlOptional.get();

		if (!subOfferingDtlOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("subject.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<Section> sectionOptional = sectionService.findById(sectionId);
		Section sectionObj = sectionOptional.get();
		if (!sectionOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("section.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		List<Student> studentList = studentRegistrationService
				.findByAcademicYearObjAndStandardObjAndSectionObjAndStatus(academicYearObj, stdObj, sectionObj,
						Status.ACTIVE);
		if (studentList.isEmpty()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("mark.entry.student.not.found"),
					HttpStatus.BAD_REQUEST);
		}
		Optional<MarkEntryHdr> existingMarkEntryHdrOptional = markEntryHdrService
				.findByExamObjAndAcademicYearObjAndStandardObjAndSubjectOfferingDtlObjAndSectionObj(examObj,
						academicYearObj, stdObj, subjectOfferingDtlObj, sectionObj);

		List<MarkEntryDtlDTO> markEntryDtlDtoList = new ArrayList<>();
		MarkEntryHdrDTO markEntryHdrDtoObj = new MarkEntryHdrDTO();

		if (!existingMarkEntryHdrOptional.isPresent()) {
			MarkEntryDtlDTO markEntryDtlDtoObj = null;
			for (Student stuObj : studentList) {

				markEntryDtlDtoObj = new MarkEntryDtlDTO();
				markEntryDtlDtoObj.setStudentId(stuObj.getId());
				markEntryDtlDtoObj.setStudentName(stuObj.getFirstName() + " "
						+ (null != stuObj.getMiddleName() ? stuObj.getMiddleName() + " " : "") + stuObj.getLastName());
				markEntryDtlDtoObj.setRegistrationNumber(stuObj.getRegistrationNumber());
				markEntryDtlDtoObj.setRollNumber(stuObj.getRollNumber());
				markEntryDtlDtoObj.setStandardName(stuObj.getStandardObj().getName());
				markEntryDtlDtoObj.setSectionName(stuObj.getSectionObj().getName());
				markEntryDtlDtoObj.setMaxPracticalMark(subjectOfferingDtlObj.getPracticalMax());
				markEntryDtlDtoObj.setMaxTheoryMark(subjectOfferingDtlObj.getTheoryMax());

				markEntryDtlDtoList.add(markEntryDtlDtoObj);

			}

		} else {
			markEntryHdrDtoObj.setId(existingMarkEntryHdrOptional.get().getId());

			List<MarkEntryDtl> checkedDtlList = existingMarkEntryHdrOptional.get().getMarkEntryDtlList();
			MarkEntryDtlDTO markEntryDtlDtoObj = null;

			for (MarkEntryDtl markEntryDtlObj : checkedDtlList) {

				markEntryDtlDtoObj = new MarkEntryDtlDTO();
				markEntryDtlDtoObj.setId(markEntryDtlObj.getId());
				markEntryDtlDtoObj.setStudentId(markEntryDtlObj.getStudentObj().getId());
				markEntryDtlDtoObj.setStudentName(markEntryDtlObj.getStudentObj().getFirstName() + " "
						+ (null != markEntryDtlObj.getStudentObj().getMiddleName()
								? markEntryDtlObj.getStudentObj().getMiddleName() + " "
								: "")
						+ markEntryDtlObj.getStudentObj().getLastName());
				markEntryDtlDtoObj.setRegistrationNumber(markEntryDtlObj.getStudentObj().getRegistrationNumber());
				markEntryDtlDtoObj.setRollNumber(markEntryDtlObj.getStudentObj().getRollNumber());
				markEntryDtlDtoObj.setStandardName(existingMarkEntryHdrOptional.get().getStandardObj().getName());
				markEntryDtlDtoObj.setSectionName(existingMarkEntryHdrOptional.get().getSectionObj().getName());
				markEntryDtlDtoObj.setPracticalMark(markEntryDtlObj.getPracticalMark());
				markEntryDtlDtoObj.setTheoryMark(markEntryDtlObj.getTheoryMark());
				markEntryDtlDtoObj.setMaxPracticalMark(subjectOfferingDtlObj.getPracticalMax());
				markEntryDtlDtoObj.setMaxTheoryMark(subjectOfferingDtlObj.getTheoryMax());
				markEntryDtlDtoObj.setIsChecked(true);

				markEntryDtlDtoList.add(markEntryDtlDtoObj);

			}

		}
		markEntryDtlDtoList.sort((o1, o2) -> o1.getStudentName().compareToIgnoreCase(o2.getStudentName()));

		markEntryHdrDtoObj.setAcademicYearId(academicYearId);
		markEntryHdrDtoObj.setStandardId(stdId);
		markEntryHdrDtoObj.setSectionId(sectionId);
		markEntryHdrDtoObj.setSubjectOfferingDtlId(subOfferingDtlId);
		markEntryHdrDtoObj.setExamId(examId);
		markEntryHdrDtoObj.setMarkEntryDtlDtoList(markEntryDtlDtoList);

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("mark.entry.get"),
					markEntryHdrDtoObj, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create and update student mark.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The mark entry request payload") @RequestBody MarkEntryHdrDTO request,
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

		Optional<Exam> examOptional = examService.findById(request.getExamId());
		Exam examObj = examOptional.get();
		if (!examOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Standard> stdOptional = standardService.findById(request.getStandardId());
		Standard stdObj = stdOptional.get();
		if (!stdOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<SubjectOfferingDTL> subOfferingDtlOptional = subjectOfferingDtlService
				.findById(request.getSubjectOfferingDtlId());
		SubjectOfferingDTL subjectOfferingDtlObj = subOfferingDtlOptional.get();
		if (!subOfferingDtlOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Optional<Section> sectionOptional = sectionService.findById(request.getSectionId());
		Section sectionObj = sectionOptional.get();
		if (!sectionOptional.isPresent()) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		for (MarkEntryDtlDTO dtoObj : request.getMarkEntryDtlDtoList()) {
			if (!dtoObj.getIsChecked()) {

				return responseGenerator.errorResponse(context, messageSource.getMessage("mark.entry.empty"),
						HttpStatus.BAD_REQUEST);
			}

			if (dtoObj.getMaxPracticalMark() != null && dtoObj.getPracticalMark() == null) {
				return responseGenerator.errorResponse(context,
						messageSource.getMessage("mark.entry.practical.mark.empty"), HttpStatus.BAD_REQUEST);

			}
			if (dtoObj.getTheoryMark() == null && dtoObj.getMaxTheoryMark() != null) {
				return responseGenerator.errorResponse(context,
						messageSource.getMessage("mark.entry.theory.mark.empty"), HttpStatus.BAD_REQUEST);
			}
			if (dtoObj.getPracticalMark() != null && dtoObj.getMaxPracticalMark() != null) {
				if (dtoObj.getPracticalMark() > dtoObj.getMaxPracticalMark()) {

					return responseGenerator.errorResponse(context,
							messageSource.getMessage("mark.entry.greater.practical.mark"), HttpStatus.BAD_REQUEST);
				}
			}

			if (dtoObj.getTheoryMark() != null && dtoObj.getMaxTheoryMark() != null) {
				if (dtoObj.getTheoryMark() > dtoObj.getMaxTheoryMark()) {

					return responseGenerator.errorResponse(context,
							messageSource.getMessage("mark.entry.greater.theory.mark"), HttpStatus.BAD_REQUEST);
				}
			}

			if (dtoObj.getMaxPracticalMark() != null && dtoObj.getMaxTheoryMark() != null) {

				if (dtoObj.getTheoryMark() == null) {
					return responseGenerator.errorResponse(context,
							messageSource.getMessage("mark.entry.theory.mark.empty"), HttpStatus.BAD_REQUEST);
				}
				if (dtoObj.getPracticalMark() == null) {
					return responseGenerator.errorResponse(context,
							messageSource.getMessage("mark.entry.practical.mark.empty"), HttpStatus.BAD_REQUEST);
				}
				if (dtoObj.getPracticalMark() == null && dtoObj.getTheoryMark() == null) {
					return responseGenerator.errorResponse(context,
							messageSource.getMessage("mark.entry.practical.theory.mark.empty"), HttpStatus.BAD_REQUEST);
				}
			}

		}

		List<MarkEntryDtl> markEntryDtlList = new ArrayList<>();

		MarkEntryHdr markEntryHdrObj = null;
		Map<UUID, MarkEntryDtl> dtlIdMap = new HashMap<UUID, MarkEntryDtl>();
		if (request.getId() == null) {

			Optional<MarkEntryHdr> markEntryHdrOptional = markEntryHdrService
					.findByExamObjAndAcademicYearObjAndStandardObjAndSubjectOfferingDtlObjAndSectionObj(examObj,
							academicYearObj, stdObj, subjectOfferingDtlObj, sectionObj);
			if (markEntryHdrOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("mark.entry.dublicate"),
						HttpStatus.BAD_REQUEST);
			}
			markEntryHdrObj = new MarkEntryHdr();

		} else {
			Optional<MarkEntryHdr> markEntryHdrOptional = markEntryHdrService.findById(request.getId());
			if (!markEntryHdrOptional.isPresent()) {
				return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
						HttpStatus.BAD_REQUEST);

			}
			markEntryHdrObj = markEntryHdrOptional.get();
			for (MarkEntryDtl dtlObj : markEntryHdrObj.getMarkEntryDtlList()) {
				dtlIdMap.put(dtlObj.getId(), dtlObj);

			}

		}
		markEntryHdrObj.setExamObj(examObj);
		markEntryHdrObj.setAcademicYearObj(academicYearObj);
		markEntryHdrObj.setStandardObj(stdObj);
		markEntryHdrObj.setSubjectOfferingDtlObj(subjectOfferingDtlObj);
		markEntryHdrObj.setSectionObj(sectionObj);

		for (MarkEntryDtlDTO dtoObj : request.getMarkEntryDtlDtoList()) {

			MarkEntryDtl markEntryDtlObj = dtlIdMap.remove(dtoObj.getId());
			if (markEntryDtlObj == null) {
				markEntryDtlObj = new MarkEntryDtl();
			}

			Optional<Student> stuOptional = studentRegistrationService.findById(dtoObj.getStudentId());
			Student stuObj = null;
			stuObj = stuOptional.get();

			markEntryDtlObj.setMarkEntryHdrObj(markEntryHdrObj);
			markEntryDtlObj.setStudentObj(stuObj);
			if (dtoObj.getMaxPracticalMark() != null) {
				markEntryDtlObj.setPracticalMark(dtoObj.getPracticalMark());
			}
			if (dtoObj.getMaxTheoryMark() != null) {
				markEntryDtlObj.setTheoryMark(dtoObj.getTheoryMark());
			}
			markEntryDtlObj.setStatus(Status.ACTIVE);

			markEntryDtlList.add(markEntryDtlObj);

		}

		markEntryHdrObj.setMarkEntryDtlList(markEntryDtlList);
		markEntryHdrObj.setStatus(Status.ACTIVE);
		markEntryHdrService.saveOrUpdate(markEntryHdrObj);

		for (UUID id : dtlIdMap.keySet()) {
			markEntryDtlService.deleteById(id);
		}

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("mark.entry.update"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}
