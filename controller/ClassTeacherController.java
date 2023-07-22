package com.iskool.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.ClassTeacherDTO;
import com.iskool.dto.ClassTeacherDTO.ClassTeacherTrackingDTO;
import com.iskool.dto.ClassTeacherTrackingRequestDTO;
import com.iskool.dto.FacultyClassTeacherResponceDTO;
import com.iskool.dto.SectionDTO;
import com.iskool.dto.SectionResponceDTO;
import com.iskool.dto.StudentInformationDTO;
import com.iskool.dto.StudentListDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.ClassTeacherTracking;
import com.iskool.entity.Faculty;
import com.iskool.entity.Section;
import com.iskool.entity.Standard;
import com.iskool.entity.Student;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.ClassTeacherTrackingService;
import com.iskool.service.FacultyService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.SectionService;
import com.iskool.service.StandardService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/class/teacher")
@Api(value = "VehicleRest API", produces = "application/json", consumes = "application/json")
public class ClassTeacherController {
	private static final Logger logger = Logger.getLogger(ClassTeacherController.class);

	private @NotNull FacultyService facultyService;

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull StandardService standardService;

	private @NonNull StudentRegistrationService studentRegistrationService;

	private @NonNull AcademicYearService academicYearService;

	private @NonNull SectionService sectionService;

	private @NonNull ClassTeacherTrackingService classTeacherTrackingService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all user.", response = Response.class)
	@GetMapping(value = "/get/All/faculty", produces = "application/json")
	public ResponseEntity<?> getAllFaculty(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		{
			List<Faculty> facultyList = facultyService.findAll();
			List<FacultyClassTeacherResponceDTO> responseList = new ArrayList<FacultyClassTeacherResponceDTO>();
			FacultyClassTeacherResponceDTO facultyResponseDTOObj = null;
			for (Faculty facultyObj : facultyList) {
				if (facultyObj.getStatus().equals(Status.INACTIVE))
					continue;
				facultyResponseDTOObj = new FacultyClassTeacherResponceDTO();
				facultyResponseDTOObj.setFacultyId(facultyObj.getFacultyId());
				facultyResponseDTOObj.setName(facultyObj.getFirstName()
						+ (null != facultyObj.getMiddleName() ? " " + facultyObj.getMiddleName() : "") + " "
						+ facultyObj.getLastName());
				responseList.add(facultyResponseDTOObj);

			}
			try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("user.faculty.fetch"),
						responseList, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
	}

	@ApiOperation(value = "Allows to Section lists.", response = Response.class)
	@GetMapping(value = "/search/{academicYearId}/{stdId}", produces = "application/json")
	public ResponseEntity<?> search(@PathVariable("academicYearId") UUID academicYearId,
			@PathVariable("stdId") UUID stdId,
			@ApiParam(value = "The Section fetch request payload") @RequestHeader HttpHeaders httpHeader)
			throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		Optional<Standard> standardOptional = standardService.findById(stdId);
		if (!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);
		if (!academicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.academic.year"),
					HttpStatus.BAD_REQUEST);
		}

		List<SectionDTO> sectionObj = studentRegistrationService.findBySectionList(academicYearOptional.get().getId(),
				standardOptional.get().getId());
		List<SectionResponceDTO> sectionList = new ArrayList<SectionResponceDTO>();
		SectionResponceDTO sectionDTO = null;
		for (SectionDTO list : sectionObj) {
			sectionDTO = new SectionResponceDTO();
			sectionDTO.setId(list.getId());
			sectionDTO.setName(list.getName());
			sectionList.add(sectionDTO);
		}

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("section.fetch"), sectionList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to Section lists.", response = Response.class)
	@PutMapping(value = "/allotment/student/search", produces = "application/json")
	public ResponseEntity<?> search(@RequestBody ClassTeacherTrackingRequestDTO request,
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
		List<StudentInformationDTO> dtoList = new ArrayList<StudentInformationDTO>();

		if (request.getSectionId() == null) {

			 List<StudentListDTO>  studentList = studentRegistrationService.findByStudentList(academicYearOptional.get().getId(),
							standardOptional.get().getId());
			 StudentInformationDTO dto = null;
			for (StudentListDTO list : studentList) {
				dto = new StudentInformationDTO();
				dto.setName(list.getName());
				dto.setRegistrationNumber(list.getRegistrationNumber());
				dto.setRollNumber(list.getRollNumber());
				dto.setSectionName(list.getSectionName());
				dto.setStudentId(list.getStudentId());
				dto.setClassTeacherId(list.getClassTeacherId());
				if (list.getClassTeacherId() != null) {
					Optional<Faculty> facultyOptional = facultyService.findById(list.getClassTeacherId());
					if (!facultyOptional.isPresent()) {
						return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),
								HttpStatus.BAD_REQUEST);
					}
					dto.setClassTeacherName(facultyOptional.get().getConcatFields());
				}

				dtoList.add(dto);
			}
		} else {

			Optional<Section> sectionOptional = sectionService.findById(request.getSectionId());
			if (!sectionOptional.isPresent()) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("invalid.section"),
						HttpStatus.BAD_REQUEST);
			}

			List<Student> studentList = studentRegistrationService
					.findByAcademicYearObjAndStandardObjAndSectionObjAndStatus(academicYearOptional.get(),
							standardOptional.get(), sectionOptional.get(), Status.ACTIVE);
			StudentInformationDTO dto = null;
			for (Student list : studentList) {
				dto = new StudentInformationDTO();
				dto.setName(list.getConcatFields());
				dto.setRegistrationNumber(list.getRegistrationNumber());
				dto.setRollNumber(list.getRollNumber());
				dto.setSectionName(list.getSectionObj().getName());
				dto.setStudentId(list.getId());
				if (list.getClassTeacherId() != null) {
					Optional<Faculty> facultyOptional = facultyService.findById(list.getClassTeacherId());
					if (!facultyOptional.isPresent()) {
						return responseGenerator.errorResponse(context, messageSource.getMessage("faculty.id.invalid"),
								HttpStatus.BAD_REQUEST);
					}
					dto.setClassTeacherName(facultyOptional.get().getConcatFields());
				}
				dtoList.add(dto);
			}
		}
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("section.fetch"), dtoList,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to create Class teacher allotments.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(
			@ApiParam(value = "The State request payload") @RequestBody ClassTeacherDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}
		ClassTeacherTracking classTeacher = null;
		for (ClassTeacherTrackingDTO classTeacherTrackingDTOObj : request.getClassTeacherTrackingDTOList()) {
			classTeacher = new ClassTeacherTracking();
			if (!classTeacherTrackingDTOObj.getIsAllocated()) {
				continue;
			}
			classTeacher.setClassTeacherId(request.getClassTeacherId());
			classTeacher.setStudentId(classTeacherTrackingDTOObj.getStudentId());
			classTeacher.setStatus(Status.ACTIVE);
			classTeacherTrackingService.saveOrUpdate(classTeacher);

			Optional<Student> studentOptional = studentRegistrationService
					.findById(classTeacherTrackingDTOObj.getStudentId());
			Student student = studentOptional.get();
			student.setClassTeacherId(classTeacher.getClassTeacherId());
			studentRegistrationService.saveOrUpdate(student);
		}

		try {
			return responseGenerator.successResponse(context, messageSource.getMessage("class.teacher.tracking.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}