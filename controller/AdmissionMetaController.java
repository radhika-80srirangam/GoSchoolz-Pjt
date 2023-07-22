package com.iskool.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.entity.AcadamicBatch;
import com.iskool.entity.AdmissionCategory;
import com.iskool.entity.Batch;
import com.iskool.entity.Board;
import com.iskool.entity.Branch;
import com.iskool.entity.Caste;
import com.iskool.entity.Category;
import com.iskool.entity.City;
import com.iskool.entity.Country;
import com.iskool.entity.Degree;
import com.iskool.entity.MotherTong;
import com.iskool.entity.Religion;
import com.iskool.entity.Scheme;
import com.iskool.entity.Section;
import com.iskool.entity.Semester;
import com.iskool.entity.Session;
import com.iskool.entity.Shift;
import com.iskool.entity.Standard;
import com.iskool.entity.State;
import com.iskool.entity.Subject;
import com.iskool.entity.Year;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcadamicBatchService;
import com.iskool.service.AdmissionCategoryService;
import com.iskool.service.BatchService;
import com.iskool.service.BoardService;
import com.iskool.service.BranchService;
import com.iskool.service.CasteService;
import com.iskool.service.CategoryService;
import com.iskool.service.CityService;
import com.iskool.service.CountryService;
import com.iskool.service.DegreeService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.MotherTongService;
import com.iskool.service.ReligionService;
import com.iskool.service.SchemeService;
import com.iskool.service.SectionService;
import com.iskool.service.SemesterService;
import com.iskool.service.SessionService;
import com.iskool.service.ShiftService;
import com.iskool.service.StandardService;
import com.iskool.service.StateService;
import com.iskool.service.SubjectService;
import com.iskool.service.YearService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/meta")
@Api(value = "Admission batch Rest API", produces = "application/json", consumes = "application/json")
public class AdmissionMetaController {

	private static final Logger logger = Logger.getLogger(AdmissionMetaController.class);

	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull StateService stateService;
	private @NonNull CityService cityService;
	private @NonNull CountryService countryService;
	private @NonNull CategoryService categoryService;
	private @NonNull AdmissionCategoryService admissionCategoryService;
	private @NotNull ReligionService religionService;
	private @NonNull CasteService casteService;
	private @NonNull DegreeService degreeService;
	private @NonNull BranchService branchService;
	private @NonNull SchemeService schemeService;
	private @NotNull YearService yearService;
	private @NotNull SemesterService semesterService;
	private @NotNull SessionService sessionService;
	private @NonNull BatchService batchService;
	private @NonNull SectionService sectionService;
	private @NonNull ShiftService shiftService;
	private @NonNull StandardService standardService;
	private @NonNull BoardService boardService;
	private @NonNull SubjectService subjectService;
	private @NonNull AcadamicBatchService academicBatchService;
	private @NotNull MotherTongService motherTongService;

	@Autowired
	MessagePropertyService messageSource;

	@ApiOperation(value = "Allows to fetch all state.", response = Response.class)
	@GetMapping(value = "/get/state", produces = "application/json")
	public ResponseEntity<?> getState(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context=responseGenerator.generateTransationContext(httpHeader);
		
		List<State>stateList=stateService.findAll();
		stateList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("state.get"),
					stateList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all city.", response = Response.class)
	@GetMapping(value = "/get/city", produces = "application/json")
	public ResponseEntity<?> getcity(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<City>cityList=cityService.findAll();
		cityList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("city.get"),
					cityList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all country.", response = Response.class)
	@GetMapping(value = "/get/country", produces = "application/json")
	public ResponseEntity<?> getCountry(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Country>countryList=countryService.findAll();
		countryList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("country.get"),
					countryList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Category.", response = Response.class)
	@GetMapping(value = "/get/category", produces = "application/json")
	public ResponseEntity<?> getCategory(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Category>catList=categoryService.findAll();
		catList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("category.get"),
					catList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all admission category.", response = Response.class)
	@GetMapping(value = "/get/admission/category", produces = "application/json")
	public ResponseEntity<?> getAdmimissionCategory(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<AdmissionCategory>admCatList=admissionCategoryService.findAll();
		admCatList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("admissionCategory.get"),
					admCatList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all religion.", response = Response.class)
	@GetMapping(value = "/get/religion", produces = "application/json")
	public ResponseEntity<?> getReligion(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Religion>religionList=religionService.findAll();
		religionList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("religion.get"),
					religionList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all caste.", response = Response.class)
	@GetMapping(value = "/get/caste", produces = "application/json")
	public ResponseEntity<?> getCaste(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Caste>castelist=casteService.findAll();
		castelist.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("caste.get"),
					castelist, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Degree.", response = Response.class)
	@GetMapping(value = "/get/degree", produces = "application/json")
	public ResponseEntity<?> getDegree(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Degree>degreeList=degreeService.findAll();
		degreeList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("degree.get"),
					degreeList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Branch.", response = Response.class)
	@GetMapping(value = "/get/branch", produces = "application/json")
	public ResponseEntity<?> getBranch(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Branch>branchList=branchService.findAll();
		branchList.sort((o1,o2)->o1.getLongName().compareToIgnoreCase(o2.getLongName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("branch.get"),
					branchList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all scheme.", response = Response.class)
	@GetMapping(value = "/get/scheme", produces = "application/json")
	public ResponseEntity<?> getScheme(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Scheme>schemeList=schemeService.findAll();
		schemeList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("scheme.get"),
					schemeList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Year", response = Response.class)
	@GetMapping(value = "/get/year", produces = "application/json")
	public ResponseEntity<?> getYear(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		List<Year>yearList=yearService.findAll();
		yearList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("Year.get"),
					yearList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all semester", response = Response.class)
	@GetMapping(value = "/get/semester", produces = "application/json")
	public ResponseEntity<?> getSemester(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		List<Semester>semList=semesterService.findAll();
		semList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("semester.get"),
					semList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Allows to fetch all session", response = Response.class)
	@GetMapping(value = "/get/session", produces = "application/json")
	public ResponseEntity<?> getSession(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		List<Session>sessionList=sessionService.findAll();
		sessionList.sort((o1,o2)->o1.getLongName().compareToIgnoreCase(o2.getLongName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("session.get"),
					sessionList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all Section.", response = Response.class)
	@GetMapping(value = "/get/section", produces = "application/json")
	public ResponseEntity<?> getSection(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Section> sectionList = sectionService.findAll();
		sectionList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("section.get"),
					sectionList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all shift.", response = Response.class)
	@GetMapping(value = "/get/shift", produces = "application/json")
	public ResponseEntity<?> getShift(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Shift>shiftList=shiftService.findAll();
		shiftList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("shift.get"),
					shiftList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all standard.", response = Response.class)
	@GetMapping(value = "/get/std", produces = "application/json")
	public ResponseEntity<?> getStandard(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Standard>stdList=standardService.findAll();
		stdList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("std.get"),
					stdList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all board.", response = Response.class)
	@GetMapping(value = "/get/board", produces = "application/json")
	public ResponseEntity<?> getBoard(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Board>boardList=boardService.findAll();
		boardList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("board.get"),
					boardList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all subject.", response = Response.class)
	@GetMapping(value = "/get/subject", produces = "application/json")
	public ResponseEntity<?> getSubject(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Subject>subList=subjectService.findAll();
		subList.sort((o1,o2)->o1.getName().compareTo(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("subject.get"),
					subList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all batch.", response = Response.class)
	@GetMapping(value = "/get/batch", produces = "application/json")
	public ResponseEntity<?> getBatch(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<Batch>batchList=batchService.findAll();
		batchList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("batch.get"),
					batchList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all AcademicBatch.", response = Response.class)
	@GetMapping(value = "/get/academic/batch", produces = "application/json")
	public ResponseEntity<?> getAcademicBatch(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		List<AcadamicBatch>acaBatchList=academicBatchService.findAll();
		acaBatchList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("academicBatch.get"),
					acaBatchList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to fetch all motherTongue", response = Response.class)
	@GetMapping(value = "/get/mother/tongue", produces = "application/json")
	public ResponseEntity<?> getMotherTongue(@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		List<MotherTong>motherTongueList=motherTongService.findAll();
		motherTongueList.sort((o1,o2)->o1.getName().compareToIgnoreCase(o2.getName()));
		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("motherTong.get"),
					motherTongueList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
