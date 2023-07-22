
package com.iskool.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

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

import com.iskool.dto.AddressInformationDTO;
import com.iskool.dto.ResultPublishResponceDTO;
import com.iskool.dto.ResultPublishResponceDTO;
import com.iskool.dto.StudentMarkDTO;
import com.iskool.dto.TransportRouteDtlDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.Exam;
import com.iskool.entity.ExamScheduleDtl;
import com.iskool.entity.ExamScheduleHdr;
import com.iskool.entity.Faculty;
import com.iskool.entity.MarkEntryDtl;
import com.iskool.entity.MarkEntryHdr;
import com.iskool.entity.Standard;
import com.iskool.entity.Student;
import com.iskool.entity.StudentMarkPublish;
import com.iskool.entity.SubjectOfferingDTL;
import com.iskool.entity.User;
import com.iskool.enumeration.Status;
import com.iskool.repository.StudentRegistrationRepository;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.ExamScheduleDtlService;
import com.iskool.service.ExamScheduleHdrService;
import com.iskool.service.ExamService;
import com.iskool.service.MarkEntryDtlService;
import com.iskool.service.MarkEntryHdrService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StandardService;
import com.iskool.service.StudentMarkPublishService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.service.SubjectOfferingDTLService;
import com.iskool.service.UserService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/result/myresult")
@Api(value = "ResultPublish: resultPublish Rest API", produces = "application/json", consumes = "application/json")
public class MarkViewController {
	private static final Logger logger = Logger.getLogger(MarkViewController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	@Autowired
	MessagePropertyService messageSource;

	private @NonNull MarkEntryHdrService markEntryHdrService;

	private @NonNull MarkEntryDtlService markEntryDtlService;

	private @NonNull StandardService standardService;

	private @NonNull AcademicYearService academicYearService;
	
	private @NonNull ExamScheduleHdrService examScheduleHdrService;
	
	private @NonNull ExamScheduleDtlService examScheduleDtlService;

	private @NonNull StudentRegistrationService studentRegistrationService;
	
	private @NotNull StudentRegistrationRepository studentRegistrationRepository;

	private @NonNull ExamService examService;

	private @NonNull StudentMarkPublishService studentMarkPublishService;

	private @NonNull UserService userService;

	private @NonNull SubjectOfferingDTLService subjectOfferingDTLService;

	@ApiOperation(value = "Allows to search existing resultPublish info.", response = Response.class)
	@GetMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> myresult(@RequestHeader HttpHeaders httpHeader, Principal principal)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		User userObj = userService.getUserByUserName(principal.getName());
		
		Optional<Student> studentOb = studentRegistrationService.findById(userObj.getReferenceId());

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(studentOb.get().getAcademicYearObj().getId());
		
		Optional<Standard> standardOptional = standardService.findById(studentOb.get().getStandardObj().getId());

		List<StudentMarkPublish> studentResultPublishOptional = studentMarkPublishService
			.findByAcademicYearObjAndStandardObj(academicYearOptional.get(),
					standardOptional.get());
	
	Optional<StudentMarkPublish> studentExam = studentResultPublishOptional.stream().max((o1,o2)->o1.getPublishedOn().compareTo(o2.getPublishedOn()));
		
	
		List<MarkEntryHdr> markEntryHdrOptional = markEntryHdrService.findByExamObjAndAcademicYearObjAndStandardObj(
		studentExam.get().getExamObj(), academicYearOptional.get(), standardOptional.get());
	
	
		if (markEntryHdrOptional.isEmpty()) {

			return responseGenerator.errorResponse(context, messageSource.getMessage("result.publish.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		

		Map<UUID, List<MarkEntryDtl>> markEntryMap = new HashMap<UUID, List<MarkEntryDtl>>();

		for (MarkEntryHdr markEntryOHdrbj : markEntryHdrOptional) {
			for (MarkEntryDtl markEntryDtlObj : markEntryOHdrbj.getMarkEntryDtlList()) {
				if(markEntryDtlObj.getStudentObj().getId().equals(studentOb.get().getId())) {
				List<MarkEntryDtl> dtlList = markEntryMap.get(markEntryDtlObj.getStudentObj().getId());
				if (null == dtlList || dtlList.isEmpty()) {
					dtlList = new ArrayList<MarkEntryDtl>();
				}
				
				dtlList.add(markEntryDtlObj);
				markEntryMap.put(markEntryDtlObj.getStudentObj().getId(), dtlList);
			}
				}
		}
		
		List<ResultPublishResponceDTO> resultPublishList = new ArrayList<ResultPublishResponceDTO>();

		for (UUID studentId : markEntryMap.keySet()) {

			ResultPublishResponceDTO responceDTO = new ResultPublishResponceDTO();
			Integer percentage;
			Double total = 0.0;
			Double studentOverallMark = 0.0;
			Double studentTotalMark = 0.0;
			
			List<StudentMarkDTO> markList = new ArrayList<StudentMarkDTO>();
			for (MarkEntryDtl markEntryDtl : markEntryMap.get(studentId)) {
				StudentMarkDTO studentMarkDTO = new StudentMarkDTO();
				studentMarkDTO.setSubjectName(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getSubjectObj().getName());
				studentMarkDTO.setMarkEntryDtlId(markEntryDtl.getId());
				studentMarkDTO.setPracticalOptaindeMark(markEntryDtl.getPracticalMark());
				studentMarkDTO.setTheroryOptaindeMark(markEntryDtl.getTheoryMark());
				studentMarkDTO.setPracticalTotalMark(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getPracticalMax());
				studentMarkDTO.setSubjectType(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getSubjectObj().getSubjectType());
				studentMarkDTO.setTheroryTotalMark(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getTheoryMax());
				total = (markEntryDtl.getPracticalMark() != null ? markEntryDtl.getPracticalMark() : 0)
						+ (markEntryDtl.getTheoryMark() != null ? markEntryDtl.getTheoryMark() : 0);
				studentMarkDTO.setOptainedMark(total);
				
				markList.add(studentMarkDTO);

				studentOverallMark = studentOverallMark
						+ (markEntryDtl.getPracticalMark() != null ? markEntryDtl.getPracticalMark() : 0)
						+ (markEntryDtl.getTheoryMark() != null ? markEntryDtl.getTheoryMark() : 0);

				studentTotalMark = studentTotalMark
						+ (markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getPracticalMax() != null
								? markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getPracticalMax()
								: 0)
						+ (markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getTheoryMax() != null
								? markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getTheoryMax()
								: 0);
				responceDTO.setName(markEntryDtl.getStudentObj().getConcatFields());
				responceDTO.setRegistrationNumber(markEntryDtl.getStudentObj().getRegistrationNumber());
				responceDTO.setRollNumber(markEntryDtl.getStudentObj().getRollNumber());
				responceDTO.setSectionName(markEntryDtl.getMarkEntryHdrObj().getSectionObj().getName());
				responceDTO.setStandardName(markEntryDtl.getMarkEntryHdrObj().getStandardObj().getName());
				responceDTO.setStudentId(markEntryDtl.getStudentObj().getId());
				percentage = (int) (studentOverallMark / studentTotalMark * 100);
				responceDTO.setPercentage(percentage + "%");
				responceDTO.setExamName(markEntryDtl.getMarkEntryHdrObj().getExamObj().getExamName());
				responceDTO.setSubjectOfferingDtlId(markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getId());
			}

			responceDTO.setOptainedMark(studentOverallMark);
			responceDTO.setTotalMark(studentTotalMark);
			responceDTO.setStudentMarkList(markList);
			resultPublishList.add(responceDTO);

		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("student.exam.result.publish.fetched"),
					resultPublishList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Allows to search existing result info.", response = Response.class)
	@GetMapping(value = "/getAll", produces = "application/json")
	public ResponseEntity<?> MyresultSearch(@RequestHeader HttpHeaders httpHeader,Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		User userObj = userService.getUserByUserName(principal.getName());
		
		Optional<Student> studentOb = studentRegistrationService.findById(userObj.getReferenceId());
		
		AcademicYear academicYear = studentOb.get().getAcademicYearObj();
		
		Standard standard = studentOb.get().getStandardObj();
		
		
	List<StudentMarkPublish> studentResultPublishOptional = studentMarkPublishService
			.findByAcademicYearObjAndStandardObj(academicYear,standard);
	

	if (studentResultPublishOptional.isEmpty()) {
		
		return responseGenerator.errorResponse(context, messageSource.getMessage("result.publish.not.invalid"),
				HttpStatus.BAD_REQUEST);
		}
	
	List<ResultPublishResponceDTO> resultPublish = new ArrayList<ResultPublishResponceDTO>();
	
	for(StudentMarkPublish studentMark:studentResultPublishOptional) {	
		
		List<MarkEntryHdr> markEntryHdrOptional = markEntryHdrService.findByExamObjAndAcademicYearObjAndStandardObj(
				studentMark.getExamObj(),academicYear,standard);
	
	
		if (markEntryHdrOptional.isEmpty()) {

			return responseGenerator.errorResponse(context, messageSource.getMessage("result.publish.invalid"),
					HttpStatus.BAD_REQUEST);
		}
		
	
		Map<UUID, List<MarkEntryDtl>> markEntryMap = new HashMap<UUID, List<MarkEntryDtl>>();

		for (MarkEntryHdr markEntryOHdrbj : markEntryHdrOptional) {
			for (MarkEntryDtl markEntryDtlObj : markEntryOHdrbj.getMarkEntryDtlList()) {
				if(markEntryDtlObj.getStudentObj().getId().equals(studentOb.get().getId())) {
				List<MarkEntryDtl> dtlList = markEntryMap.get(markEntryDtlObj.getStudentObj().getId());
				if (null == dtlList || dtlList.isEmpty()) {
					dtlList = new ArrayList<MarkEntryDtl>();
				}
				
				dtlList.add(markEntryDtlObj);
				markEntryMap.put(markEntryDtlObj.getStudentObj().getId(), dtlList);
			}
				}
		}
	
		
		

		for (UUID studentId : markEntryMap.keySet()) {

			ResultPublishResponceDTO responceDTO = new ResultPublishResponceDTO();
			Integer percentage;
			Double total = 0.0;
			Double studentOverallMark = 0.0;
			Double studentTotalMark = 0.0;
			
			List<StudentMarkDTO> markList = new ArrayList<StudentMarkDTO>();
			for (MarkEntryDtl markEntryDtl : markEntryMap.get(studentId)) {
				StudentMarkDTO studentMarkDTO = new StudentMarkDTO();
				studentMarkDTO.setSubjectName(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getSubjectObj().getName());
				studentMarkDTO.setMarkEntryDtlId(markEntryDtl.getId());
				studentMarkDTO.setPracticalOptaindeMark(markEntryDtl.getPracticalMark());
				studentMarkDTO.setTheroryOptaindeMark(markEntryDtl.getTheoryMark());
				studentMarkDTO.setPracticalTotalMark(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getPracticalMax());
				studentMarkDTO.setSubjectType(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getSubjectObj().getSubjectType());
				studentMarkDTO.setTheroryTotalMark(
						markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getTheoryMax());
				total = (markEntryDtl.getPracticalMark() != null ? markEntryDtl.getPracticalMark() : 0)
						+ (markEntryDtl.getTheoryMark() != null ? markEntryDtl.getTheoryMark() : 0);
				studentMarkDTO.setOptainedMark(total);
				
				markList.add(studentMarkDTO);

				studentOverallMark = studentOverallMark
						+ (markEntryDtl.getPracticalMark() != null ? markEntryDtl.getPracticalMark() : 0)
						+ (markEntryDtl.getTheoryMark() != null ? markEntryDtl.getTheoryMark() : 0);

				studentTotalMark = studentTotalMark
						+ (markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getPracticalMax() != null
								? markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getPracticalMax()
								: 0)
						+ (markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getTheoryMax() != null
								? markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getTheoryMax()
								: 0);
				responceDTO.setName(markEntryDtl.getStudentObj().getConcatFields());
				responceDTO.setRegistrationNumber(markEntryDtl.getStudentObj().getRegistrationNumber());
				responceDTO.setRollNumber(markEntryDtl.getStudentObj().getRollNumber());
				responceDTO.setSectionName(markEntryDtl.getMarkEntryHdrObj().getSectionObj().getName());
				responceDTO.setStandardName(markEntryDtl.getMarkEntryHdrObj().getStandardObj().getName());
				responceDTO.setStudentId(markEntryDtl.getStudentObj().getId());
				percentage = (int) (studentOverallMark / studentTotalMark * 100);
				responceDTO.setPercentage(percentage + "%");
				responceDTO.setExamName(markEntryDtl.getMarkEntryHdrObj().getExamObj().getExamName());
				responceDTO.setSubjectOfferingDtlId(markEntryDtl.getMarkEntryHdrObj().getSubjectOfferingDtlObj().getId());
			}

			responceDTO.setOptainedMark(studentOverallMark);
			responceDTO.setTotalMark(studentTotalMark);
			responceDTO.setStudentMarkList(markList);
			resultPublish.add(responceDTO);
			
		}	
	}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("result.publish.fetched"),
					resultPublish, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
	

	
	