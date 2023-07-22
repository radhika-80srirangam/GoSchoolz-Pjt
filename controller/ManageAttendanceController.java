package com.iskool.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

import com.iskool.dto.DateRequestDto;
import com.iskool.dto.ManageAttendanceDetailsDto;
import com.iskool.dto.ManageAttendanceDto;
import com.iskool.dto.StudentsAttendanceDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.Attendance;
import com.iskool.entity.DayMaster;
import com.iskool.entity.Faculty;
import com.iskool.entity.FacultyMappingDTL;
import com.iskool.entity.FacultyMappingHDR;
import com.iskool.entity.Section;
import com.iskool.entity.Standard;
import com.iskool.entity.Student;
import com.iskool.entity.Subject;
import com.iskool.entity.User;
import com.iskool.enumeration.AttendanceType;
import com.iskool.enumeration.Status;
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
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/manage/attendance")
@Api(value = "Manage Attendance: manage attendance Rest API", produces = "application/json", consumes = "application/json")
public class ManageAttendanceController {

	private static final Logger logger = Logger.getLogger(ManageAttendanceController.class);

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

	@ApiOperation(value = "Allows to fetch current Academic", response = Response.class)
	@GetMapping(value = "/academic", produces = "application/json")
	public ResponseEntity<?> currentAcademic(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYear = academicYearService.findByCurrentAcademicYear();

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("academic.get"), academicYear,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all subject.", response = Response.class)
	@GetMapping(value = "/subject", produces = "application/json")
	public ResponseEntity<?> getSubject(@RequestHeader HttpHeaders httpHeader, Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYear = academicYearService.findByCurrentAcademicYear();

		User userObj = userService.getUserByUserName(principal.getName());

		Faculty faculty = facultyService.findById(userObj.getReferenceId()).get();

		List<Subject> subjectList = subjectService.findBySubjectList(faculty.getFacultyId(),
				academicYear.get().getId());

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("subject.get"), subjectList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all subject.", response = Response.class)
	@GetMapping(value = "/standard", produces = "application/json")
	public ResponseEntity<?> getStandard(@RequestHeader HttpHeaders httpHeader, Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYear = academicYearService.findByCurrentAcademicYear();

		User userObj = userService.getUserByUserName(principal.getName());

		Faculty faculty = facultyService.findById(userObj.getReferenceId()).get();

		List<Standard> standardList = standardService.findByStandardList(faculty.getFacultyId(),
				academicYear.get().getId());

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("std.get"), standardList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all subject.", response = Response.class)
	@GetMapping(value = "/section", produces = "application/json")
	public ResponseEntity<?> getSection(@RequestHeader HttpHeaders httpHeader, Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYear = academicYearService.findByCurrentAcademicYear();

		User userObj = userService.getUserByUserName(principal.getName());

		Faculty faculty = facultyService.findById(userObj.getReferenceId()).get();

		List<Section> sectionList = sectionService.findBySectionList(faculty.getFacultyId(),
				academicYear.get().getId());

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("section.get"), sectionList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch the class schedules.", response = Response.class)
	@PutMapping(value = "/get/{academicYearId}/{stdId}/{sectionId}/{subjectId}", produces = "application/json")
	public ResponseEntity<?> getDetails(@PathVariable("academicYearId") UUID academicYearId,
			@PathVariable("stdId") UUID stdId, @PathVariable("sectionId") UUID sectionId,
			@PathVariable("subjectId") UUID subjectId, @RequestBody DateRequestDto request,
			@RequestHeader HttpHeaders httpHeader, Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		String findDate = request.getFromDate().format(format).toString();

		LocalDate date = LocalDate.now();

		LocalDate date1 = LocalDate.now().minusDays(1);

		if (request.getFromDate().equals(date) || request.getFromDate().equals(date1)) {

			User userObj = userService.getUserByUserName(principal.getName());

			Faculty faculty = facultyService.findById(userObj.getReferenceId()).get();

			Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);

			Optional<Standard> standardOptional = standardService.findById(stdId);

			Optional<Section> sectionOptional = sectionService.findById(sectionId);

			Optional<Faculty> facultyOptional = facultyService.findById(faculty.getFacultyId());

			Optional<Subject> subjectOptional = subjectService.findById(subjectId);

			Optional<FacultyMappingHDR> facultyMappingHDR = facultyMappingHDRService
					.findByStatusAndSubjectObjAndAcademicYearObjAndStandardObjAndSectionObjAndFacultyObj(Status.ACTIVE,
							subjectOptional.get(), academicYearOptional.get(), standardOptional.get(),
							sectionOptional.get(), facultyOptional.get());

			String day = request.getFromDate().getDayOfWeek().toString();

			Optional<DayMaster> dayMasterOptional = dayMasterService.findByName(day);

			List<FacultyMappingDTL> facultyMappingDTL = facultyMappingDTLService
					.findByFacultyMappingHDRObjAndDayMasterObj(facultyMappingHDR.get(), dayMasterOptional.get());

			if (facultyMappingDTL.isEmpty()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("subject.id.invalid"),
						HttpStatus.BAD_REQUEST);
			}
			List<ManageAttendanceDto> manage = new ArrayList<ManageAttendanceDto>();

			for (FacultyMappingDTL facultyDtl : facultyMappingDTL) {
				ManageAttendanceDto manageAtt = new ManageAttendanceDto();

				List<Attendance> attendance = attendanceService.findByDateAndStartTimeAndEndTime(findDate,
						facultyDtl.getStartTime(), facultyDtl.getEndTime());

				if (attendance.isEmpty()) {
					manageAtt.setStatus(Status.PENDING);
					manageAtt.setSubjectName(subjectOptional.get().getName());
					manageAtt.setSubjectId(subjectId);
					manageAtt.setClassSchedule(facultyDtl.getStartTime() + " To " + facultyDtl.getEndTime());
					manageAtt.setAcademicYearId(academicYearId);
					manageAtt.setStdId(stdId);
					manageAtt.setDayId(dayMasterOptional.get().getId());
					manageAtt.setFacultyId(facultyOptional.get().getFacultyId());
					manageAtt.setStartTime(facultyDtl.getStartTime());
					manageAtt.setEndTime(facultyDtl.getEndTime());
					manageAtt.setSectionId(sectionId);
					manageAtt.setDate(request.getFromDate().format(format).toString());
					manage.add(manageAtt);
				} else {
					manageAtt.setStatus(Status.COMPLETED);
					manageAtt.setSubjectName(subjectOptional.get().getName());
					manageAtt.setSubjectId(subjectId);
					manageAtt.setClassSchedule(facultyDtl.getStartTime() + " To " + facultyDtl.getEndTime());
					manageAtt.setTakenBy(attendance.get(0).getCreatedBy());
					manageAtt.setTakenTime(attendance.get(0).getCreatedOn().toString());
					manageAtt.setAcademicYearId(academicYearId);
					manageAtt.setStdId(stdId);
					manageAtt.setDayId(dayMasterOptional.get().getId());
					manageAtt.setFacultyId(facultyOptional.get().getFacultyId());
					manageAtt.setStartTime(facultyDtl.getStartTime());
					manageAtt.setEndTime(facultyDtl.getEndTime());
					manageAtt.setSectionId(sectionId);
					manageAtt.setDate(request.getFromDate().format(format).toString());
					manage.add(manageAtt);
				}

			}

			try {
				return responseGenerator.successGetResponse(context,
						messageSource.getMessage("students.attendance.list"), manage, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} else {
			return responseGenerator.errorResponse(context, messageSource.getMessage("date.error"),
					HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to fetch the class schedules.", response = Response.class)
	@PutMapping(value = "/get/attendance/studentList", produces = "application/json")
	public ResponseEntity<?> updateAttendance(
			@ApiParam(value = "The Attendance request payload") @RequestBody ManageAttendanceDto request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Faculty faculty = facultyService.findById(request.getFacultyId()).get();

		AcademicYear academicYearOptional = academicYearService.findById(request.getAcademicYearId()).get();

		Standard standardOptional = standardService.findById(request.getStdId()).get();

		Section sectionOptional = sectionService.findById(request.getSectionId()).get();

		Subject subjectOptional = subjectService.findById(request.getSubjectId()).get();

		long total;

		ManageAttendanceDetailsDto dtoObj = new ManageAttendanceDetailsDto();

		dtoObj.setAcademicYear(academicYearOptional.getName());
		dtoObj.setSection(sectionOptional.getName());
		dtoObj.setDate(request.getDate());
		dtoObj.setStandard(standardOptional.getName());
		dtoObj.setSubjectName(subjectOptional.getName());
		dtoObj.setClassSchedule(request.getClassSchedule());
		dtoObj.setFacultyName(faculty.getConcatFields());
		dtoObj.setDayId(request.getDayId());
		dtoObj.setSubjectId(request.getSubjectId());
		dtoObj.setStdId(request.getStdId());
		dtoObj.setStartTime(request.getStartTime());
		dtoObj.setEndTime(request.getEndTime());
		dtoObj.setAcademicYearId(request.getAcademicYearId());

		List<StudentsAttendanceDTO> list = new ArrayList<StudentsAttendanceDTO>();

		total = studentRegistrationRepository.findByCount(academicYearOptional.getId().toString(),
				standardOptional.getId().toString(), sectionOptional.getId().toString());
		dtoObj.setTotalStudents(total);

		List<Student> students = studentRegistrationService.findByStatusAndAcademicYearObjAndStandardObjAndSectionObj(
				Status.ACTIVE, academicYearOptional, standardOptional, sectionOptional);

		if (students.isEmpty()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("student.invalid.id"),
					HttpStatus.BAD_REQUEST);
		}

		if (request.getStatus().equals(Status.PENDING)) {

			StudentsAttendanceDTO listObj = null;
			for (Student studObj : students) {
				listObj = new StudentsAttendanceDTO();
				listObj.setStudentId(studObj.getId());
				listObj.setStudentName(studObj.getConcatFields());
				listObj.setRegistrationNumber(studObj.getRegistrationNumber());
				listObj.setStdName(studObj.getStandardObj().getName());
				listObj.setRollno(studObj.getRollNumber());
				listObj.setSectionName(studObj.getSectionObj().getName());
				listObj.setAttendance(AttendanceType.PRESENT);
				list.add(listObj);

			}
			dtoObj.setStudentsAttendanceList(list);

		} else {

			StudentsAttendanceDTO tableObj = null;
			for (Student stuObj : students) {
				Attendance attendObj = attendanceService
						.findByStatusAndYearObjAndStandardObjAndStartTimeAndEndTimeAndStudentObjAndDate(Status.ACTIVE,
								academicYearOptional, standardOptional, request.getStartTime(), request.getEndTime(),
								stuObj, request.getDate());
				tableObj = new StudentsAttendanceDTO();
				tableObj.setAttendance(attendObj.getAttendanceType());
				tableObj.setStudentId(attendObj.getStudentObj().getId());
				tableObj.setRegistrationNumber(stuObj.getRegistrationNumber());
				tableObj.setSectionName(stuObj.getSectionObj().getName());
				tableObj.setStdName(stuObj.getStandardObj().getName());
				tableObj.setRollno(stuObj.getRollNumber());
				tableObj.setStudentId(stuObj.getId());
				tableObj.setStudentName(stuObj.getConcatFields());
				list.add(tableObj);
			}
			dtoObj.setStudentsAttendanceList(list);
		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("attendance.student.list"),
					dtoObj, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to update existing Attendance info.", response = Response.class)
	@PutMapping(value = "/update", produces = "application/json")
	public ResponseEntity<?> updateAttendance(
			@ApiParam(value = "The Attendance request payload") @RequestBody ManageAttendanceDetailsDto request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(request.getAcademicYearId());

		Optional<Standard> standardOptional = standardService.findById(request.getStdId());

		Optional<DayMaster> dayMasterOptional = dayMasterService.findById(request.getDayId());

		List<Attendance> attendance = attendanceService
				.findByStatusAndYearObjAndStandardObjAndDayMasterObjAndStartTimeAndEndTimeAndDate(Status.ACTIVE,
						academicYearOptional.get(), standardOptional.get(), dayMasterOptional.get(),
						request.getStartTime(), request.getEndTime(), request.getDate());
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

				attendanceObj.setDate(request.getDate());

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
				Attendance attendObj = attendanceService
						.findByStatusAndYearObjAndStandardObjAndStartTimeAndEndTimeAndStudentObjAndDate(Status.ACTIVE,
								academicYearOptional.get(), standardOptional.get(), request.getStartTime(),
								request.getEndTime(), studentObj, request.getDate());

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

				attendObj.setDate(request.getDate());
				attendanceService.saveOrUpdate(attendObj);
			}

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
