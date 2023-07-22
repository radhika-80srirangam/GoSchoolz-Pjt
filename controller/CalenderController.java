
package com.iskool.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.CalenderRequestDTO;
import com.iskool.dto.CalenderResponseDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.DayMaster;
import com.iskool.entity.Faculty;
import com.iskool.entity.FacultyMappingDTL;
import com.iskool.entity.FacultyMappingHDR;
import com.iskool.entity.User;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.DayMasterService;
import com.iskool.service.FacultyMappingDTLService;
import com.iskool.service.FacultyMappingHDRService;
import com.iskool.service.FacultyService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.SubjectService;
import com.iskool.service.UserService;
import com.iskool.util.message.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor(onConstructor_ = { @Autowired })
@RequestMapping("/api/calender")
@Api(value = "Calender: calender Rest API", produces = "application/json", consumes = "application/json")
public class CalenderController {
	private static final Logger logger = Logger.getLogger(CalenderController.class);

	private @NonNull ResponseGenerator responseGenerator;
	
	private @NonNull FacultyMappingHDRService facultyMappingHDRService;
	
	private @NonNull FacultyMappingDTLService facultyMappingDTLService;
	
	private @NonNull SubjectService subjectService;
	
	private @NonNull DayMasterService dayMasterService;
	
	private @NonNull AcademicYearService academicYearService;
	
	private @NonNull MessagePropertyService messageSource;
	
	private @NonNull UserService userService;
	
	private @NonNull FacultyService facultyService;

	@ApiOperation(value = "Allows to fetch student by register number.", response = Response.class)
	@PutMapping(value = "/get/weekly/time/table", produces = "application/json")
	public ResponseEntity<?> get(@RequestHeader HttpHeaders httpHeader, @RequestBody CalenderRequestDTO request,
			Principal principal) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);

		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,
					HttpStatus.BAD_REQUEST);
		}

		Map<String,String> localDateMap=new HashMap<>();
		List<String> list = new ArrayList<>();
		while(!request.getFromDate().equals(request.getToDate())) {
			localDateMap.put(request.getFromDate().getDayOfWeek().toString().toUpperCase(), DateTimeFormatter.ofPattern("dd/MM/YYYY").format(request.getFromDate()));
			list.add(request.getFromDate().getDayOfWeek().toString());
			request.setFromDate(request.getFromDate().plusDays(1));
		}
		
		list.add(request.getToDate().getDayOfWeek().toString());
		localDateMap.put(request.getToDate().getDayOfWeek().toString().toUpperCase(), DateTimeFormatter.ofPattern("dd/MM/YYYY").format(request.getToDate()));
		

		List<DayMaster> dayList = dayMasterService.findByNameIn(list);

		User userObj = userService.getUserByUserName(principal.getName());
		
		Faculty faculty = facultyService.findById(userObj.getReferenceId()).get();

		Optional<AcademicYear> academicYearObjOptional = academicYearService.findByCurrentAcademicYear();
		if (!academicYearObjOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("create.current.academic.year"),
					HttpStatus.BAD_REQUEST);
		}

		List<FacultyMappingHDR> facultyList = facultyMappingHDRService.findByFacultyObjAndAcademicYearObj(faculty,academicYearObjOptional.get());
		
		
		Map<UUID, List<FacultyMappingDTL>> dayIdMap = new HashMap<>();
		if (facultyList != null && !facultyList.isEmpty()) {
			dayIdMap = facultyList.stream().flatMap(f -> f.getFacultyMappingDtlList().stream()).collect(Collectors.groupingBy(fd -> fd.getDayMasterObj().getId()));
		}

		List<CalenderResponseDTO> responseList = new ArrayList<CalenderResponseDTO>();
		if (!dayIdMap.isEmpty()) {
			for (DayMaster day : dayList) {
				List<FacultyMappingDTL> facultyDtlList = dayIdMap.get(day.getId());
				if (facultyDtlList != null && !facultyDtlList.isEmpty()) {
					for (FacultyMappingDTL dtl : facultyDtlList) {
						CalenderResponseDTO dto = new CalenderResponseDTO();
						dto.setId(dtl.getId());
						dto.setFacultyName(dtl.getFacultyMappingHDRObj().getFacultyObj().getConcatFields());
						dto.setFacultyId(dtl.getFacultyMappingHDRObj().getFacultyObj().getFacultyId());
						dto.setContentFullName(dtl.getFacultyMappingHDRObj().getSubjectObj().getName() +" "+dtl.getFacultyMappingHDRObj().getSubjectObj().getSubjectType());
						dto.setDayName(dtl.getDayMasterObj().getName());
						dto.setDayId(dtl.getDayMasterObj().getId());
					//	dto.setEndTime(localDateMap.get(dtl.getDayMasterObj().getName().toUpperCase())+" "+print24(dtl.getEndTime()));
					//	dto.setStartTime(localDateMap.get(dtl.getDayMasterObj().getName().toUpperCase())+" "+print24(dtl.getStartTime()));
						dto.setEndTime(dtl.getEndTime());
						dto.setStartTime(dtl.getStartTime());
						dto.setSection(dtl.getFacultyMappingHDRObj().getSectionObj().getName());
						dto.setSectionId(dtl.getFacultyMappingHDRObj().getSectionObj().getId());
						dto.setTitle(dtl.getFacultyMappingHDRObj().getSubjectObj().getName());
						dto.setSubjectId(dtl.getFacultyMappingHDRObj().getSubjectObj().getId());
						//System.out.println(facultyMappingHDRIdMap.get(dtl.getFacultyMappingHDRObj().getId()));
						dto.setStdId(dtl.getFacultyMappingHDRObj().getStandardObj().getId());
						dto.setStdName(dtl.getFacultyMappingHDRObj().getStandardObj().getName());
						dto.setAcademicYearId(dtl.getFacultyMappingHDRObj().getAcademicYearObj().getId());
//						dto.setFromDate(request.getFromDate());
//						dto.setToDate(request.getToDate());
						dto.setStartDate(localDateMap.get(dtl.getDayMasterObj().getName().toUpperCase()));
						dto.setEndDate(localDateMap.get(dtl.getDayMasterObj().getName().toUpperCase()));
						responseList.add(dto);
					}
				}

			}
		}
		
		Map<String ,Object> response=new HashMap<String, Object>();
		response.put("responseList", responseList);

		try {
			return responseGenerator.successGetResponse(context, messageSource.getMessage("calender.weekly.time.table"),
					response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	public static String print24(String str)
	{
		
		String time="";
	    // Get hours
	    int h1 = (int)str.charAt(1) - '0';
	    int h2 = (int)str.charAt(0) - '0';
	    int hh = (h2 * 10 + h1 % 10);
	 
	    // If time is in "AM"
	    if (str.charAt(6) == 'A')
	    {
	        if (hh == 12)
	        {
	        	time+="00";
	            for (int i = 2; i <= 4; i++)
	            	time+= str.charAt(i);
	        }
	        else
	        {
	            for (int i = 0; i <= 4; i++)
	            	time+= str.charAt(i);
	        }
	    }
	 
	    // If time is in "PM"
	    else
	    {
	        if (hh == 12)
	        {
	        	time+="12";
	            for (int i = 2; i <= 4; i++)
	            	time+=str.charAt(i);
	        }
	        else
	        {
	            hh = hh + 12;
	            time+=hh;
	            for (int i = 2; i <= 4; i++)
	            	time+=str.charAt(i);
	        }
	    }
	    return time+":00";
	}
	
}
