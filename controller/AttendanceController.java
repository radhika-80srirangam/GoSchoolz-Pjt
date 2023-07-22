package com.iskool.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.hamcrest.collection.IsEmptyCollection;
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iskool.dto.AttendanceDTO;
import com.iskool.dto.CalenderRequestDTO;
import com.iskool.dto.CalenderResponseDTO;
import com.iskool.dto.DateRequestDto;
import com.iskool.dto.ExamTimeTableDtlDTO;
import com.iskool.dto.ExamTimeTableHdrDTO;
import com.iskool.dto.ManageAttendanceDetailsDto;
import com.iskool.dto.ManageAttendanceDto;
import com.iskool.dto.ResultPublishResponceDTO;
import com.iskool.dto.StudentMarkDTO;
import com.iskool.dto.StudentsAttendanceDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.Attendance;
import com.iskool.entity.Batch;
import com.iskool.entity.City;
import com.iskool.entity.DayMaster;
import com.iskool.entity.Exam;
import com.iskool.entity.ExamTimeTableDtl;
import com.iskool.entity.ExamTimeTableHdr;
import com.iskool.entity.Faculty;
import com.iskool.entity.FacultyMappingDTL;
import com.iskool.entity.FacultyMappingHDR;
import com.iskool.entity.MarkEntryDtl;
import com.iskool.entity.MarkEntryHdr;
import com.iskool.entity.Section;
import com.iskool.entity.Standard;
import com.iskool.entity.Student;
import com.iskool.entity.StudentMarkPublish;
import com.iskool.entity.Subject;
import com.iskool.entity.SubjectOfferingHDR;
import com.iskool.entity.User;
import com.iskool.enumeration.AttendanceType;
import com.iskool.enumeration.Status;
import com.iskool.repository.AttendanceRepository;
import com.iskool.repository.StudentRegistrationRepository;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicStandardFeeService;
import com.iskool.service.AcademicYearService;
import com.iskool.service.AttendanceService;
import com.iskool.service.DayMasterService;
import com.iskool.service.FacultyMappingDTLService;
import com.iskool.service.FacultyMappingHDRService;
import com.iskool.service.FacultyService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.SectionService;
import com.iskool.service.StandardService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.service.SubjectService;
import com.iskool.service.UserService;
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
@RequestMapping("/api/attendance")
@Api(value = "Attendance: attendance Rest API", produces = "application/json", consumes = "application/json")
public class AttendanceController {

	private static final Logger logger = Logger.getLogger(AttendanceController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull StudentRegistrationService studentRegistrationService;

	private @NonNull AcademicStandardFeeService academicStandardFeeService;

	private @NonNull StandardService standardService;

	private @NonNull SectionService sectionService;
	

	private @NonNull FacultyMappingHDRService facultyMappingHDRService;
	
	private @NotNull StudentRegistrationRepository studentRegistrationRepository;

	private @NonNull FacultyMappingDTLService facultyMappingDTLService;

	private @NonNull AttendanceService attendanceService;

	private @NonNull AcademicYearService academicYearService;

	private @NonNull FacultyService facultyService;

	private @NonNull DayMasterService dayMasterService;
	
	private @NonNull SubjectService subjectService;
	private @NonNull UserService userService;

	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all fee declaration by standard id", response = Response.class)
	@PutMapping(value = "/get", produces = "application/json")
	public ResponseEntity<?> getAttendance(
			@ApiParam(value = "The Attendance request payload") @RequestBody CalenderResponseDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(request.getAcademicYearId());
		if (!academicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.academic.year"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<Standard> standardOptional = standardService.findById(request.getStdId());

		if (!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<Section> sectionOptional = sectionService.findById(request.getSectionId());
		if (!sectionOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.section"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<Faculty> facultyOptional = facultyService.findById(request.getFacultyId());
		if (!sectionOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.faculty"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<DayMaster> dayMasterOptional = dayMasterService.findById(request.getDayId());
		if (!dayMasterOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.faculty"),
					HttpStatus.BAD_REQUEST);
		}
		
		Optional<Subject> subjectOptional = subjectService.findById(request.getSubjectId());

		Optional<FacultyMappingHDR> facultyMappingHDRs = facultyMappingHDRService
				.findByStatusAndSubjectObjAndAcademicYearObjAndStandardObjAndSectionObjAndFacultyObj(Status.ACTIVE,
						subjectOptional.get(),academicYearOptional.get(), standardOptional.get(), sectionOptional.get(),
						facultyOptional.get());
		if (!facultyMappingHDRs.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("faculty.mapping.hdr.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		Map<UUID, List<FacultyMappingDTL>> facultyDtlMap = new HashMap<UUID, List<FacultyMappingDTL>>();
		
		for (FacultyMappingDTL dtlObj : facultyMappingHDRs.get().getFacultyMappingDtlList()) {
			List<FacultyMappingDTL> dtlList = facultyDtlMap.get(dtlObj.getId());
			if (null == dtlList || dtlList.isEmpty()) {
				dtlList = new ArrayList<FacultyMappingDTL>();
			}
			dtlList.add(dtlObj);
			facultyDtlMap.put(dtlObj.getId(), dtlList);
		}
		
		AttendanceDTO dtoObj = null;
		dtoObj = new AttendanceDTO();
		for (UUID dtlId : facultyDtlMap.keySet()) {

			List<FacultyMappingDTL> dtlList = facultyDtlMap.get(dtlId);
			for (FacultyMappingDTL facultyDtl : dtlList) {
			
				dtoObj.setFacultyId(facultyDtl.getFacultyMappingHDRObj().getFacultyObj().getFacultyId());
				dtoObj.setFacultyName(facultyDtl.getFacultyMappingHDRObj().getFacultyObj().getConcatFields());
				dtoObj.setSubjectId(facultyDtl.getFacultyMappingHDRObj().getSubjectObj().getId());
				dtoObj.setSubjectName(facultyDtl.getFacultyMappingHDRObj().getSubjectObj().getName());
				dtoObj.setStdId(facultyDtl.getFacultyMappingHDRObj().getStandardObj().getId());
				dtoObj.setStdName(facultyDtl.getFacultyMappingHDRObj().getStandardObj().getName());
				dtoObj.setStartTime(request.getStartTime());
				dtoObj.setEndTime(request.getEndTime());
				dtoObj.setAcademicYearId(facultyDtl.getFacultyMappingHDRObj().getAcademicYearObj().getId());
				dtoObj.setDayId(request.getDayId());
				dtoObj.setDayName(request.getDayName());
				dtoObj.setFromDate(request.getStartDate());
				dtoObj.setToDate(request.getEndDate());
				dtoObj.setSectionId(facultyDtl.getFacultyMappingHDRObj().getSectionObj().getId());

				List<StudentsAttendanceDTO> list = new ArrayList<StudentsAttendanceDTO>();
				List<Student> students = studentRegistrationService
						.findByStatusAndAcademicYearObjAndStandardObjAndSectionObj(Status.ACTIVE,
								academicYearOptional.get(), standardOptional.get(), sectionOptional.get());
				if (students.isEmpty()) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("student.invalid.id"),
							HttpStatus.BAD_REQUEST);
				}
				List<Attendance> attendance = attendanceService
						.findByStatusAndYearObjAndStandardObjAndDayMasterObjAndStartTimeAndEndTimeAndDate(Status.ACTIVE,
								academicYearOptional.get(), standardOptional.get(), dayMasterOptional.get(),
								request.getStartTime(), request.getEndTime(),request.getStartDate());
				if (attendance.isEmpty()) {
					
					StudentsAttendanceDTO listObj = null;
					for (Student studObj : students) {
						listObj = new StudentsAttendanceDTO();
						listObj.setStudentId(studObj.getId());
						listObj.setStudentName(studObj.getConcatFields());
						listObj.setRegistrationNumber(studObj.getRegistrationNumber());
						listObj.setStdName(studObj.getStandardObj().getName());
						listObj.setSectionName(studObj.getSectionObj().getName());
						listObj.setAttendance(AttendanceType.PRESENT);
						list.add(listObj);
				
					}
					dtoObj.setStudentsAttendanceList(list);
				}else {
							
					StudentsAttendanceDTO tableObj = null;
					for (Student stuObj : students) {	
						Attendance attendObj=attendanceService
								.findByStatusAndYearObjAndStandardObjAndStartTimeAndEndTimeAndStudentObjAndDate(Status.ACTIVE,
										academicYearOptional.get(), standardOptional.get(),
										request.getStartTime(), request.getEndTime(),stuObj,request.getStartDate());
						tableObj = new StudentsAttendanceDTO();	
						tableObj.setAttendance(attendObj.getAttendanceType());
						tableObj.setStudentId(attendObj.getStudentObj().getId());
						tableObj.setRegistrationNumber(stuObj.getRegistrationNumber());
						tableObj.setSectionName(stuObj.getSectionObj().getName());
						tableObj.setStdName(stuObj.getStandardObj().getName());
						tableObj.setStudentId(stuObj.getId());
						tableObj.setStudentName(stuObj.getConcatFields());
						list.add(tableObj);
			}
					dtoObj.setStudentsAttendanceList(list);	
		}
	}
		}
					try {
						return responseGenerator.successGetResponse(context,
								messageSource.getMessage("students.attendance.list"), dtoObj, HttpStatus.OK);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e.getMessage(), e);
						return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);		
		}
}

	@ApiOperation(value = "Allows to update existing Attendance info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "The Attendance request payload") @RequestBody AttendanceDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		 DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		 
		 String date = LocalDate.now().format(format).toString();
		 
		 String date1 = LocalDate.now().minusDays(1).format(format).toString();
		 
		if (request.getFromDate().equals(date)||request.getFromDate().equals(date1)) {
			Optional<AcademicYear> academicYearOptional = academicYearService.findById(request.getAcademicYearId());
			if (!academicYearOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.academic.year"),
						HttpStatus.BAD_REQUEST);
			}

			Optional<Standard> standardOptional = standardService.findById(request.getStdId());

			if (!standardOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
						HttpStatus.BAD_REQUEST);
			}
			Optional<DayMaster> dayMasterOptional = dayMasterService.findById(request.getDayId());
			if (!dayMasterOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.faculty"),
						HttpStatus.BAD_REQUEST);
			}

			List<Attendance> attendance = attendanceService
					.findByStatusAndYearObjAndStandardObjAndDayMasterObjAndStartTimeAndEndTimeAndDate(Status.ACTIVE,
							academicYearOptional.get(), standardOptional.get(), dayMasterOptional.get(),
							request.getStartTime(), request.getEndTime(),request.getFromDate());
			if (attendance.isEmpty()) {

				Attendance attendanceObj = null;
				for (StudentsAttendanceDTO obj : request.getStudentsAttendanceList()) {
					attendanceObj = new Attendance();

					attendanceObj.setAttendanceType(obj.getAttendance());
					Student studentObj = new Student();
					studentObj.setId(obj.getStudentId());
					attendanceObj.setStudentObj(studentObj);
					attendanceObj.setAttendanceType(obj.getAttendance());

					AcademicYear academicYearObj = new AcademicYear();
					academicYearObj.setId(request.getAcademicYearId());
					attendanceObj.setYearObj(academicYearObj);

					Standard stdObj = new Standard();
					stdObj.setId(request.getStdId());
					attendanceObj.setStandardObj(stdObj);
					attendanceObj.setStatus(Status.ACTIVE);
					attendanceObj.setStartTime(request.getStartTime());
					attendanceObj.setEndTime(request.getEndTime());

					Subject subjectObj = new Subject();
					subjectObj.setId(request.getSubjectId());
					attendanceObj.setSubjectObj(subjectObj);
					
					DayMaster dayMasterObj = new DayMaster();
					dayMasterObj.setId(request.getDayId());
					attendanceObj.setDayMasterObj(dayMasterObj);
					
					attendanceObj.setDate(request.getFromDate());
					
					attendanceService.saveOrUpdate(attendanceObj);				
					
				}
				try {
					return responseGenerator.successResponse(context, messageSource.getMessage("attendance.create"),
							HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
					return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
				}
			} else {
				
					for (StudentsAttendanceDTO obj : request.getStudentsAttendanceList()) {	
						
						Student studentObj = new Student();
						studentObj.setId(obj.getStudentId());
						Attendance attendObj=attendanceService
								.findByStatusAndYearObjAndStandardObjAndStartTimeAndEndTimeAndStudentObjAndDate(Status.ACTIVE,
										academicYearOptional.get(), standardOptional.get(),
										request.getStartTime(), request.getEndTime(),studentObj,request.getFromDate());
						
						attendObj.setStudentObj(studentObj);
						attendObj.setAttendanceType(obj.getAttendance());

						AcademicYear academicYearObj = new AcademicYear();
						academicYearObj.setId(request.getAcademicYearId());
						attendObj.setYearObj(academicYearObj);

						Standard stdObj = new Standard();
						stdObj.setId(request.getStdId());
						attendObj.setStandardObj(stdObj);
						attendObj.setStatus(Status.ACTIVE);
						attendObj.setStartTime(request.getStartTime());
						attendObj.setEndTime(request.getEndTime());
						
						Subject subjectObj = new Subject();
						subjectObj.setId(request.getSubjectId());
						attendObj.setSubjectObj(subjectObj);

						DayMaster dayMasterObj = new DayMaster();
						dayMasterObj.setId(request.getDayId());
						attendObj.setDayMasterObj(dayMasterObj);
						
						attendObj.setDate(request.getFromDate());
						attendanceService.saveOrUpdate(attendObj);				
				}
			
			}
		}else {
			return responseGenerator.errorResponse(context, messageSource.getMessage("attendance.error"),
					HttpStatus.BAD_REQUEST);
		}

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("attendance.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}	
	
}