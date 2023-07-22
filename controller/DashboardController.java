package com.iskool.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.BirthdayDTO;
import com.iskool.dto.DashBoardNoticeBoardDTO;
import com.iskool.dto.ExamScheduleDTO;
import com.iskool.dto.ExamScheduleResponceDtlDTO;
import com.iskool.dto.ExamScheduleResponceHdrDTO;
import com.iskool.dto.FacultyAndStudentClassDTO;
import com.iskool.dto.NoticeBoardDTO;
import com.iskool.dto.StudentInfomationDtlDTO;
import com.iskool.dto.StudentTeacherParentCountDTO;
import com.iskool.dto.TodayBirthdayDateDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.DayMaster;
import com.iskool.entity.Exam;
import com.iskool.entity.ExamScheduleDtl;
import com.iskool.entity.ExamScheduleHdr;
import com.iskool.entity.ExamTimeTableDtl;
import com.iskool.entity.ExamTimeTableHdr;
import com.iskool.entity.Faculty;
import com.iskool.entity.FacultyMappingDTL;
import com.iskool.entity.FacultyMappingHDR;
import com.iskool.entity.NotificationDtl;
import com.iskool.entity.NotificationHdr;
import com.iskool.entity.Student;
import com.iskool.entity.StudentFeesReceipt;
import com.iskool.entity.User;
import com.iskool.enumeration.Status;
import com.iskool.enumeration.UserType;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicStandardFeeComponentService;
import com.iskool.service.AcademicStandardFeeService;
import com.iskool.service.AcademicYearService;
import com.iskool.service.DayMasterService;
import com.iskool.service.ExamScheduleDtlService;
import com.iskool.service.ExamScheduleHdrService;
import com.iskool.service.ExamService;
import com.iskool.service.ExamTimeTableDtlService;
import com.iskool.service.ExamTimeTableHdrService;
import com.iskool.service.FacultyMappingDTLService;
import com.iskool.service.FacultyMappingHDRService;
import com.iskool.service.FacultyService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.NotificationDtlService;
import com.iskool.service.NotificationHdrService;
import com.iskool.service.StandardService;
import com.iskool.service.StudentFeesReceiptService;
import com.iskool.service.StudentMarkPublishService;
import com.iskool.service.StudentParentDetailsService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.service.UserService;
import com.iskool.service.UserService1;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/dashboard")
@Api(value = "Trading: Section Rest API", produces = "application/json", consumes = "application/json")
public class DashboardController {
	private static final Logger logger = Logger.getLogger(DashboardController.class);

	private static final UUID UUID = null;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull StudentMarkPublishService studentMarkPublishService;

	private @NonNull StudentRegistrationService studentRegistrationService;

	private @NonNull StudentParentDetailsService studentParentDetailsService;

	private @NonNull AcademicYearService academicYearService;

	private @NonNull AcademicStandardFeeComponentService academicStandardFeeComponentService;

	private @NonNull AcademicStandardFeeService academicStandardFeeService;

	private @NonNull StudentFeesReceiptService studentFeesReceiptService;

	private @NonNull ExamTimeTableHdrService examTimeTableHdrService;

	private @NotNull UserService1 userService1;

	private @NotNull FacultyService facultyService;

	private @NonNull ExamService examService;

	private @NonNull StandardService standardService;

	private @NonNull ExamScheduleHdrService examScheduleHdrService;

	private @NonNull ExamScheduleDtlService examScheduleDtlService;

	private @NonNull UserService userService;

	private @NonNull FacultyMappingHDRService facultyMappingHDRService;

	private @NonNull DayMasterService dayMasterService;

	private @NonNull FacultyMappingDTLService facultyMappingDTLService;

	private @NonNull ExamTimeTableDtlService examTimeTableDtlService;

	private @NonNull NotificationHdrService notificationHdrService;

	private @NonNull NotificationDtlService notificationDtlService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all Student.", response = Response.class)
	@GetMapping(value = "/get/student", produces = "application/json")
	public ResponseEntity<?> getStudent(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		StudentTeacherParentCountDTO studentTeacherParentCountDTO = new StudentTeacherParentCountDTO();

		int studentCount = 0;
		Optional<AcademicYear> acadamicYearOptional = academicYearService.findByCurrentAcademicYear();
		if (!acadamicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("academic.year.create.error"),
					HttpStatus.BAD_REQUEST);
		}
		List<Student> studentList = studentRegistrationService.findByStatus(Status.ACTIVE);

		List<Student> finalList = studentList.stream()
				.filter(s -> s.getAcademicYearObj().getId().equals(acadamicYearOptional.get().getId()))
				.collect(Collectors.toList());
		studentCount = finalList.size();

		studentTeacherParentCountDTO.setStudents(studentCount);

		studentTeacherParentCountDTO.setParents(studentCount);

		int teachersCount = 0;
		List<Faculty> facultyList = facultyService.findByStatus(Status.ACTIVE);
		teachersCount = facultyList.size();

		studentTeacherParentCountDTO.setTeachers(teachersCount);

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("student.count"),
					studentTeacherParentCountDTO, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch current year fees amount.", response = Response.class)
	@GetMapping(value = "/get/earnings", produces = "application/json")
	public ResponseEntity<?> getEearnings(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Double totalAmount = 0.0;

		Optional<AcademicYear> acadamicYearOptional = academicYearService.findByCurrentAcademicYear();

		List<StudentFeesReceipt> feesDetailslist = studentFeesReceiptService.findAll();
		totalAmount = feesDetailslist.stream()
				.filter(f -> f.getAcademicYearObj().getId().equals(acadamicYearOptional.get().getId()))
				.collect(Collectors.summingDouble(StudentFeesReceipt::getPaidAmount));

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("earnings.amount"),
					totalAmount, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch student by register number.", response = Response.class)
	@PutMapping(value = "/get/select/birthday", produces = "application/json")
	public ResponseEntity<?> get(@RequestHeader HttpHeaders httpHeader, @RequestBody TodayBirthdayDateDTO request,
			Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		Calendar calendar = Calendar.getInstance();
		List<BirthdayDTO> currentdateList = new ArrayList<BirthdayDTO>();
		List<BirthdayDTO> birthdayList = studentRegistrationService.findBirthdays();

		calendar.setTime(request.getDate());
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		for (BirthdayDTO birthday : birthdayList) {
			calendar.setTime(birthday.getDate());
			int dayy = calendar.get(Calendar.DAY_OF_MONTH);
			int monthh = calendar.get(Calendar.MONTH) + 1;
			if (dayy == day && monthh == month) {
				currentdateList.add(birthday);
			}
		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("birth.day"), currentdateList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Student.", response = Response.class)
	@GetMapping(value = "/get/noticeboard", produces = "application/json")
	public ResponseEntity<?> getNoticeBoard(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		ExamScheduleHdr examScheduleDtlObj = new ExamScheduleHdr();
		List<ExamScheduleDtl> examschedulelist = new ArrayList<ExamScheduleDtl>();
		NoticeBoardDTO noticeBoardDtoObj = new NoticeBoardDTO();

		List<NoticeBoardDTO> noticeboardDTOlist = new ArrayList<NoticeBoardDTO>();

		examschedulelist = examScheduleDtlService.findAll();
		for (ExamScheduleDtl notification : examschedulelist) {
			noticeBoardDtoObj = new NoticeBoardDTO();
			noticeBoardDtoObj.setExamName(notification.getExamObject().getExamName());
			noticeBoardDtoObj.setStartdate(notification.getStartDate());
			noticeBoardDtoObj.setEnddate(notification.getEndDate());
			noticeboardDTOlist.add(noticeBoardDtoObj);

		}
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("birth.day"),
					noticeboardDTOlist, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch class.", response = Response.class)
	@GetMapping(value = "/get/class", produces = "application/json")
	public ResponseEntity<?> get(@RequestHeader HttpHeaders httpHeader, Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		User userObject = userService.getUserByUserName(principal.getName());

		Date fromDate = new Date();
		LocalDate localDate = LocalDate.now();
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE");

		List<FacultyAndStudentClassDTO> facultyAndStudentClassDTOList = new ArrayList<>();
		if (userObject.getUserType().equals(UserType.FACULTY)) {
			Faculty facultyObj = facultyService.findById(userObject.getReferenceId()).get();

			for (int i = 0; i <= 5; i++) {

				fromDate = Date.from(localDate.plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant());
				String fromDay = formatter.format(fromDate);
				Optional<DayMaster> dayMasterOptional = dayMasterService.findByName(fromDay);
				List<FacultyMappingHDR> facultyList = facultyMappingHDRService.findByFacultyObj(facultyObj);

				for (FacultyMappingHDR facultyMappingHDRObject : facultyList) {

					FacultyAndStudentClassDTO facultyAndStudentClassDTOObject = null;
					List<FacultyMappingDTL> facultyMappingDtlList = facultyMappingDTLService
							.findByFacultyMappingHDRObj(facultyMappingHDRObject);

					List<FacultyMappingDTL> facultyMappingDTtList = facultyMappingDtlList.stream()
							.filter(j -> j.getDayMasterObj().getId().equals(dayMasterOptional.get().getId()))
							.collect(Collectors.toList());

					for (FacultyMappingDTL facultyMappingDTLObject : facultyMappingDTtList) {
						facultyAndStudentClassDTOObject = new FacultyAndStudentClassDTO();
						facultyAndStudentClassDTOObject.setStdName(facultyMappingHDRObject.getStandardObj().getName());
						facultyAndStudentClassDTOObject
								.setSubjectName(facultyMappingHDRObject.getSubjectObj().getName());
						facultyAndStudentClassDTOObject.setRoomName(facultyMappingHDRObject.getRoomObj().getName());
						facultyAndStudentClassDTOObject.setStartTime(facultyMappingDTLObject.getStartTime());
						facultyAndStudentClassDTOObject.setEndTime(facultyMappingDTLObject.getEndTime());
						facultyAndStudentClassDTOObject.setDay(fromDay);
						facultyAndStudentClassDTOList.add(facultyAndStudentClassDTOObject);
					}
				}
				if (facultyAndStudentClassDTOList.size() >= 5) {
					break;
				}
			}
		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("parent.profile.fetch"),
					facultyAndStudentClassDTOList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Student upcoming exam schedule.", response = Response.class)
	@GetMapping(value = "/upcoming/exam", produces = "application/json")
	public ResponseEntity<?> upcommingExamSearch(@RequestHeader HttpHeaders httpHeader, Principal principal)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<User> userOptional = userService
				.findByUserNameAndStatusAndIsDeletedFalseAndIsLockedFalse(principal.getName(), Status.ACTIVE);

		Optional<Student> studentOptional = studentRegistrationService.findById(userOptional.get().getReferenceId());

		Optional<ExamScheduleHdr> examScheduleHdrOptional = examScheduleHdrService
				.findByStandardObjAndAcademicYearObjAndStatus(studentOptional.get().getStandardObj(),
						studentOptional.get().getAcademicYearObj(), Status.ACTIVE);
		if (!examScheduleHdrOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.schedule.error"),
					HttpStatus.OK);
		}

		Date date = new Date();
		List<ExamScheduleDTO> examScheduleDtlOptional = examScheduleDtlService
				.findByExamScheduleHdrObjectAndStartDateGreaterThanEqual(examScheduleHdrOptional.get().getId(), date);
		if (examScheduleDtlOptional.isEmpty() || examScheduleDtlOptional == null) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.schedule.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		List<ExamScheduleResponceHdrDTO> examScheduleResponceHdrDTOList = new ArrayList<ExamScheduleResponceHdrDTO>();

		StudentInfomationDtlDTO studentInformationDTOObj = new StudentInfomationDtlDTO();
		studentInformationDTOObj.setName(studentOptional.get().getConcatFields());
		studentInformationDTOObj.setRegistrationNumber(studentOptional.get().getRegistrationNumber());
		studentInformationDTOObj.setGender(studentOptional.get().getGender());
		if (studentOptional.get().getSectionObj() != null) {
			studentInformationDTOObj.setSection(studentOptional.get().getSectionObj().getName());
		}
		studentInformationDTOObj.setStandard(studentOptional.get().getStandardObj().getName());
		ExamScheduleResponceHdrDTO examScheduleResponceHdrDTO = null;
		for (ExamScheduleDTO scheduleObj : examScheduleDtlOptional) {
			examScheduleResponceHdrDTO = new ExamScheduleResponceHdrDTO();
			examScheduleResponceHdrDTO.setExamId(scheduleObj.getExamId());
			examScheduleResponceHdrDTO.setExamName(scheduleObj.getExamName());
			examScheduleResponceHdrDTO.setStartDate(scheduleObj.getStartDate());
			examScheduleResponceHdrDTO.setEndDate(scheduleObj.getEndDate());
			examScheduleResponceHdrDTOList.add(examScheduleResponceHdrDTO);
			Optional<Exam> examOptional = examService.findById(scheduleObj.getExamId());
			Optional<ExamTimeTableHdr> examTimeTableHdrOptional = examTimeTableHdrService
					.findByAcademicYearObjAndStandardObjAndExamObj(examScheduleHdrOptional.get().getAcademicYearObj(),
							examScheduleHdrOptional.get().getStandardObj(), examOptional.get());
			if (examScheduleDtlOptional == null) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("exam.time.table.error"),
						HttpStatus.OK);
			}
			List<ExamTimeTableDtl> timetabledtl = examTimeTableDtlService
					.findByExamTimeTableHdrObj(examTimeTableHdrOptional.get());

			List<ExamScheduleResponceDtlDTO> examScheduleResponceDTlDTOList = new ArrayList<ExamScheduleResponceDtlDTO>();
			ExamScheduleResponceDtlDTO ExamScheduleResponceDtlDTO = null;
			for (ExamTimeTableDtl timeTableObj : timetabledtl) {
				ExamScheduleResponceDtlDTO = new ExamScheduleResponceDtlDTO();
				ExamScheduleResponceDtlDTO.setStartTime(timeTableObj.getStartTime());
				ExamScheduleResponceDtlDTO.setEndTime(timeTableObj.getEndTime());
				ExamScheduleResponceDtlDTO.setSubjectName(timeTableObj.getSubjectObj().getSubjectObj().getName());
				ExamScheduleResponceDtlDTO.setExamDate(timeTableObj.getExamDate());
				examScheduleResponceDTlDTOList.add(ExamScheduleResponceDtlDTO);
			}

			examScheduleResponceHdrDTO.setExamScheduleDtlDTOList(examScheduleResponceDTlDTOList);
			examScheduleResponceHdrDTO.setStudentInformationObj(studentInformationDTOObj);
		}

		examScheduleResponceHdrDTOList.sort((o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate()));

		try {
			return responseGenerator.successGetResponse(context,
					messageSource.getMessage("student.upcoming.exam.schedule"), examScheduleResponceHdrDTOList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch notice board.", response = Response.class)
	@GetMapping(value = "/get/notice/board", produces = "application/json")
	public ResponseEntity<?> getType(@RequestHeader HttpHeaders httpHeader, Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<User> userOptional = userService
				.findByUserNameAndStatusAndIsDeletedFalseAndIsLockedFalse(principal.getName(), Status.ACTIVE);
		DashBoardNoticeBoardDTO dashBoardNoticeBoardDTOObject = null;
		List<DashBoardNoticeBoardDTO> dashBoardNoticeBoardDTOList = new ArrayList<>();
		if (userOptional.get().getUserType().equals(UserType.STUDENT)) {

			Optional<Student> studentoptional = studentRegistrationService
					.findById(userOptional.get().getReferenceId());

			List<NotificationDtl> notificationDtlList = notificationDtlService
					.findByStandardObj(studentoptional.get().getStandardObj());

			Map<UUID, List<NotificationDtl>> map = notificationDtlList.stream()
					.collect(Collectors.groupingBy(k -> k.getStandardObj().getId()));

			for (UUID id : map.keySet()) {

				for (NotificationDtl notificationDtlObject : map.get(id)) {
					dashBoardNoticeBoardDTOObject = new DashBoardNoticeBoardDTO();
					Optional<NotificationHdr> notificationHdrOptional = notificationHdrService
							.findById(notificationDtlObject.getNotificationHdrObj().getId());
					if (notificationHdrOptional.get().getIsTriggered().equals(true)
							&& notificationHdrOptional.get().getIsapplicableforStudent().equals(true)) {
						dashBoardNoticeBoardDTOObject.setMessage(notificationHdrOptional.get().getMessage());
						dashBoardNoticeBoardDTOObject.setStandardName(notificationDtlObject.getStandardObj().getName());
						dashBoardNoticeBoardDTOObject.setCreateDate(notificationHdrOptional.get().getCreatedOn());
						dashBoardNoticeBoardDTOList.add(dashBoardNoticeBoardDTOObject);
					}
				}
			}
		}
		if (userOptional.get().getUserType().equals(UserType.PARENT)) {

			Optional<Student> studentoptional = studentRegistrationService
					.findById(userOptional.get().getReferenceId());

			List<NotificationDtl> notificationDtlList = notificationDtlService
					.findByStandardObj(studentoptional.get().getStandardObj());

			Map<UUID, List<NotificationDtl>> map = notificationDtlList.stream()
					.collect(Collectors.groupingBy(k -> k.getStandardObj().getId()));

			for (UUID id : map.keySet()) {

				for (NotificationDtl notificationDtlObject : map.get(id)) {
					dashBoardNoticeBoardDTOObject = new DashBoardNoticeBoardDTO();
					Optional<NotificationHdr> notificationHdrOptional = notificationHdrService
							.findById(notificationDtlObject.getNotificationHdrObj().getId());
					if (notificationHdrOptional.get().getIsTriggered().equals(true)
							&& notificationHdrOptional.get().getIsapplicableforParent().equals(true)) {
						dashBoardNoticeBoardDTOObject.setMessage(notificationHdrOptional.get().getMessage());
						dashBoardNoticeBoardDTOObject.setStandardName(notificationDtlObject.getStandardObj().getName());
						dashBoardNoticeBoardDTOObject.setCreateDate(notificationHdrOptional.get().getCreatedOn());
						dashBoardNoticeBoardDTOList.add(dashBoardNoticeBoardDTOObject);
					}

				}

			}
		}
		if (userOptional.get().getUserType().equals(UserType.FACULTY)) {

			List<NotificationHdr> notificationHdrList = notificationHdrService.findAll();
			for (NotificationHdr notificationHdrObject : notificationHdrList) {
				dashBoardNoticeBoardDTOObject = new DashBoardNoticeBoardDTO();
				if (notificationHdrObject.getIsTriggered().equals(true)
						&& notificationHdrObject.getIsapplicableforFaculty().equals(true)) {
					dashBoardNoticeBoardDTOObject.setMessage(notificationHdrObject.getMessage());
					dashBoardNoticeBoardDTOObject.setCreateDate(notificationHdrObject.getCreatedOn());
					Optional<NotificationDtl> notificationDtloptional = notificationDtlService
							.findByNotificationHdrObj(notificationHdrObject);
					dashBoardNoticeBoardDTOObject
							.setStandardName(notificationDtloptional.get().getStandardObj().getName());
					dashBoardNoticeBoardDTOList.add(dashBoardNoticeBoardDTOObject);
				}
			}
		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("parent.profile.fetch"),
					dashBoardNoticeBoardDTOList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}