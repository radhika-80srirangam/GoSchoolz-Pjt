package com.iskool.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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

import com.iskool.dto.ExamTimeTableDtlDTO;
import com.iskool.dto.ExamTimeTableHdrDTO;
import com.iskool.entity.AcademicYear;
import com.iskool.entity.Exam;
import com.iskool.entity.ExamTimeTableDtl;
import com.iskool.entity.ExamTimeTableHdr;
import com.iskool.entity.Standard;
import com.iskool.entity.SubjectOfferingDTL;
import com.iskool.entity.SubjectOfferingHDR;
import com.iskool.enumeration.Status;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.service.AcademicYearService;
import com.iskool.service.ExamService;
import com.iskool.service.ExamTimeTableDtlService;
import com.iskool.service.ExamTimeTableHdrService;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.StandardService;
import com.iskool.service.SubjectOfferingHDRService;
import com.iskool.util.DateUtil;
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
@RequestMapping("/api/exam/time/table")
@Api(value = "ExamTimeTable Rest API", produces = "application/json", consumes = "application/json")

public class ExamTimeTableController {

	private static final Logger logger = Logger.getLogger(ExamTimeTableHdrDTO.class);
	private @NonNull ResponseGenerator responseGenerator;

	private @NonNull ExamTimeTableHdrService examTimeTableHdrService;
	
	private @NonNull ExamTimeTableDtlService examTimeTableDtlService;
	
	private @NonNull StandardService standardService;
	
	private @NonNull AcademicYearService academicYearService;
	
	private @NonNull ExamService examService;
	
	private @NonNull SubjectOfferingHDRService subjectOfferingHDRService;
	
	private @NonNull MessagePropertyService messageSource;
	
	@ApiOperation(value = "Allows to create or update new ExamTimeTable.", response = Response.class)
	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<?> create(@ApiParam(value = "The ExamTimeTable request payload") @RequestBody ExamTimeTableHdrDTO request,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (null == request) {
			return responseGenerator.errorResponse(context, ResponseMessage.INVALID_REQUEST_FORMAT,HttpStatus.BAD_REQUEST);
		}
		for(ExamTimeTableDtlDTO dto:request.getExamTimeTableDtlDTO()) {
			if(dto.getStartTime().trim().equals(dto.getEndTime().trim())) {
				return responseGenerator.errorResponse(context, messageSource.getMessage("start.end.time"),HttpStatus.BAD_REQUEST);
			}
			if(LocalTime.parse(IskoolUtil.print24(dto.getStartTime())).compareTo(LocalTime.parse(IskoolUtil.print24(dto.getEndTime().trim())))>0) {
				String[] params=new String[] {dto.getStartTime().trim(),dto.getEndTime().trim()};
				return responseGenerator.errorResponse(context, messageSource.getMessage("start.time.bigger",params),HttpStatus.BAD_REQUEST);
			}
	
		}
		
		Map<Date,List<ExamTimeTableDtlDTO>> dateMap=new HashMap<>();
		for(ExamTimeTableDtlDTO dtlDto:request.getExamTimeTableDtlDTO()) {
			
			List<ExamTimeTableDtlDTO> list = dateMap.get(dtlDto.getExamDate());
			if (list == null) {
				list = new ArrayList<ExamTimeTableDtlDTO>();
				list.add(dtlDto);
				dateMap.put(dtlDto.getExamDate(), list);
			} else {
				if (list.stream().anyMatch(
						i -> (i.getStartTime() + i.getEndTime()).equals(dtlDto.getStartTime() + dtlDto.getEndTime()))
						|| list.stream().anyMatch(d -> IskoolUtil.checkTime(IskoolUtil.print24(d.getStartTime()),
								IskoolUtil.print24(d.getEndTime()), IskoolUtil.print24(dtlDto.getStartTime()))
								|| IskoolUtil.checkTime(IskoolUtil.print24(d.getStartTime()),
										IskoolUtil.print24(d.getEndTime()), IskoolUtil.print24(dtlDto.getEndTime())))) {
					String[] params = new String[] { dtlDto.getStartTime().trim(), dtlDto.getEndTime().trim() };
					return responseGenerator.errorResponse(context,
							messageSource.getMessage("exam.time.table.start.time.equal.fall", params),
							HttpStatus.BAD_REQUEST);
				} else {
					list.add(dtlDto);
				}
			}
		}

		if (request.getId() != null) {
			
			for(ExamTimeTableDtlDTO dto:request.getExamTimeTableDtlDTO()) {
				if(dto.getStartTime().trim().equals(dto.getEndTime().trim())) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("start.end.time"),HttpStatus.BAD_REQUEST);
				}
				if(LocalTime.parse(IskoolUtil.print24(dto.getStartTime())).compareTo(LocalTime.parse(IskoolUtil.print24(dto.getEndTime().trim())))>0) {
					String[] params=new String[] {dto.getStartTime().trim(),dto.getEndTime().trim()};
					return responseGenerator.errorResponse(context, messageSource.getMessage("start.time.bigger",params),HttpStatus.BAD_REQUEST);
				}
		
			}
			
			
			Optional<ExamTimeTableHdr> examTimeTableOptional = examTimeTableHdrService.findById(request.getId());
			
			ExamTimeTableHdr examTimeTableObj = examTimeTableOptional.get();
			Map<UUID, ExamTimeTableDtl> dtlIdMap = examTimeTableOptional.get().getExamTimeTableDtlList().stream().collect(Collectors.toMap(ExamTimeTableDtl::getId, a -> a));

			AcademicYear academicYearObj = new AcademicYear();
			academicYearObj.setId(request.getAcademicYearId());
			examTimeTableObj.setAcademicYearObj(academicYearObj);
			
			Standard stdObj = new Standard();
			stdObj.setId(request.getStdId());
			examTimeTableObj.setStandardObj(stdObj);
			
			Exam examObj = new Exam();
			examObj.setId(request.getExamId());
			examTimeTableObj.setExamObj(examObj);
			examTimeTableObj.setStatus(Status.ACTIVE);
			
			List<ExamTimeTableDtl> dtlList = new ArrayList<ExamTimeTableDtl>();
			for (ExamTimeTableDtlDTO examdtlDto : request.getExamTimeTableDtlDTO()) {
				ExamTimeTableDtl removedObj = dtlIdMap.remove(examdtlDto.getId());
				if(removedObj==null)
					removedObj=new ExamTimeTableDtl();
				removedObj.setStatus(Status.ACTIVE);
				removedObj.setExamTimeTableHdrObj(examTimeTableObj);
				SubjectOfferingDTL subObj = new SubjectOfferingDTL();
				subObj.setId(examdtlDto.getSubjectId());
				removedObj.setSubjectObj(subObj);
				removedObj.setExamDate(examdtlDto.getExamDate());
				removedObj.setStartTime(examdtlDto.getStartTime());
				removedObj.setEndTime(examdtlDto.getEndTime());
				
				dtlList.add(removedObj);

			}
			examTimeTableObj.setExamTimeTableDtlList(dtlList);
			examTimeTableHdrService.saveOrUpdate(examTimeTableObj);

			for (UUID dtlId : dtlIdMap.keySet()) {
				examTimeTableDtlService.deleteById(dtlId);
			}
		}

		else {
			
			for(ExamTimeTableDtlDTO dto:request.getExamTimeTableDtlDTO()) {
//				if(dto.getStartTime().trim().equals(dto.getEndTime().trim())) {
//					return responseGenerator.errorResponse(context, messageSource.getMessage("start.end.time"),HttpStatus.BAD_REQUEST);
//				}
				LocalDate date = dto.getExamDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if(date.isBefore(LocalDate.now())) {
					return responseGenerator.errorResponse(context, messageSource.getMessage("date.valid.exam.date"), HttpStatus.BAD_REQUEST);
				}
				
//				else if(LocalTime.parse(IskoolUtil.print24(dto.getStartTime())).compareTo(LocalTime.parse(IskoolUtil.print24(dto.getEndTime().trim())))>0) {
//					String[] params=new String[] {dto.getStartTime().trim(),dto.getEndTime().trim()};
//					return responseGenerator.errorResponse(context, messageSource.getMessage("start.time.bigger",params),HttpStatus.BAD_REQUEST);
//				}
		
			}
			
			
			ExamTimeTableHdr examTimeTableHdr = new ExamTimeTableHdr();
			
			AcademicYear academicYearObj = new AcademicYear();
			academicYearObj.setId(request.getAcademicYearId());
			examTimeTableHdr.setAcademicYearObj(academicYearObj);
			
			Standard stdObj = new Standard();
			stdObj.setId(request.getStdId());
			examTimeTableHdr.setStandardObj(stdObj);
			
			Exam examObj = new Exam();
			examObj.setId(request.getExamId());
			examTimeTableHdr.setExamObj(examObj);
			examTimeTableHdr.setStatus(Status.ACTIVE);

			List<ExamTimeTableDtl> examTimeTableDtlList = new ArrayList<>();
			ExamTimeTableDtl examTimeTableDtl = null;
			for (ExamTimeTableDtlDTO examTimeTableDtlDTO : request.getExamTimeTableDtlDTO()) {
				examTimeTableDtl = new ExamTimeTableDtl();
				examTimeTableDtl.setExamTimeTableHdrObj(examTimeTableHdr);
				SubjectOfferingDTL subObj = new SubjectOfferingDTL();
				subObj.setId(examTimeTableDtlDTO.getSubjectId());
				examTimeTableDtl.setSubjectObj(subObj);
				examTimeTableDtl.setExamDate(examTimeTableDtlDTO.getExamDate());
				examTimeTableDtl.setStartTime(examTimeTableDtlDTO.getStartTime());
				examTimeTableDtl.setEndTime(examTimeTableDtlDTO.getEndTime());
				examTimeTableDtl.setStatus(Status.ACTIVE);
				examTimeTableDtlList.add(examTimeTableDtl);
			}

			examTimeTableHdr.setExamTimeTableDtlList(examTimeTableDtlList);
			examTimeTableHdrService.saveOrUpdate(examTimeTableHdr);

		}

		try {
			return responseGenerator.successResponse(context, request.getId() != null?messageSource.getMessage("exam.time.table.update"):messageSource.getMessage("exam.time.table.create"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}
	
	@ApiOperation(value = "Allows to fetch exam time table.", response = Response.class)
	@GetMapping(value = "/get/{academicYearId}/{stdId}/{examId}", produces = "application/json")
	public ResponseEntity<?> getAll(@PathVariable("academicYearId") UUID academicYearId,@PathVariable("stdId") UUID stdId,@PathVariable("examId") UUID examId,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		Optional<Standard> standardOptional = standardService.findById(stdId);
		if(!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),HttpStatus.BAD_REQUEST);
		}
		

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);
		if(!academicYearOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("create.current.academic.year"),HttpStatus.BAD_REQUEST);
		}
		
		Optional<Exam> examOptional = examService.findById(examId);
		if(!examOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("exam.id.invalid"),HttpStatus.BAD_REQUEST);
		}
		
		ExamTimeTableHdrDTO examHdrDTOObj  = new ExamTimeTableHdrDTO();
		examHdrDTOObj.setStdId(stdId);
		examHdrDTOObj.setAcademicYearId(academicYearId);
		examHdrDTOObj.setExamId(examId);
		
		Optional<ExamTimeTableHdr> examObj = examTimeTableHdrService.findByAcademicYearObjAndStandardObjAndExamObj(academicYearOptional.get(),standardOptional.get(),examOptional.get());
		if(!examObj.isPresent()) {
      	  	List<ExamTimeTableDtlDTO> examTimeTableDtlDTOList = new ArrayList<>();
      	  	Optional<SubjectOfferingHDR> subjectOfferingOptional = subjectOfferingHDRService.findByYearObjAndStandardObj(IskoolUtil.getAcademicYear(academicYearId), IskoolUtil.getStandard(stdId));
      	  	
      	  	if(!subjectOfferingOptional.isPresent()) {
      	  		String[] paramater=new String[] {standardOptional.get().getName()};
      	  		return responseGenerator.errorResponse(context, messageSource.getMessage("subject.offer.create.new",paramater),HttpStatus.BAD_REQUEST);
      	  	}
      	  	
      	  	subjectOfferingOptional.get().getSubjectOfferingDTLList().stream().forEach(dtl->{
	      		ExamTimeTableDtlDTO dto=new ExamTimeTableDtlDTO();
	      		dto.setSubjectId(dtl.getId());
	      		dto.setSubjectName(dtl.getSubjectObj().getName());
	      		examTimeTableDtlDTOList.add(dto);
      	  	});
      	  	
      	  	examHdrDTOObj.setExamTimeTableDtlDTO(examTimeTableDtlDTOList);
       }
		
		else {
			examHdrDTOObj.setId(examObj.get().getId());
			ExamTimeTableDtlDTO examDtlDTOObj = null;
			List<ExamTimeTableDtlDTO> examDtlDTOList = new ArrayList<>();
       	 	for(ExamTimeTableDtl examDtlObj : examObj.get().getExamTimeTableDtlList() ) {
	       		examDtlDTOObj = new ExamTimeTableDtlDTO();
	       		examDtlDTOObj.setId(examDtlObj.getId());
	       		examDtlDTOObj.setSubjectId(examDtlObj.getSubjectObj().getId());
	       		examDtlDTOObj.setSubjectName(examDtlObj.getSubjectObj().getSubjectObj().getName());
	       		examDtlDTOObj.setExamDate(examDtlObj.getExamDate());
	       		examDtlDTOObj.setStartTime(examDtlObj.getStartTime());
	       		examDtlDTOObj.setEndTime(examDtlObj.getEndTime());
	       		examDtlDTOList.add(examDtlDTOObj);
				
			}
       	 	examHdrDTOObj.setExamTimeTableDtlDTO(examDtlDTOList);
		}
		
		try {
				return responseGenerator.successGetResponse(context, messageSource.getMessage("exam.time.table.get"),examHdrDTOObj, HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	
	@ApiOperation(value = "Allows to fetch exam time table.", response = Response.class)
	@GetMapping(value = "/get/download/{academicYearId}/{stdId}/{examId}", produces = "application/json")
	public ResponseEntity<?> getAllDownload(@PathVariable("academicYearId") UUID academicYearId,@PathVariable("stdId") UUID stdId,@PathVariable("examId") UUID examId,@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		Optional<Standard> standardOptional = standardService.findById(stdId);
		if(!standardOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("std.id.invalid"),HttpStatus.BAD_REQUEST);
		}
		

		Optional<AcademicYear> academicYearOptional = academicYearService.findById(academicYearId);
		if(!academicYearOptional.isPresent()) {
		
			return responseGenerator.errorResponse(context, messageSource.getMessage("create.current.academic.year"),HttpStatus.BAD_REQUEST);
		}
		
		Optional<Exam> examOptional = examService.findById(examId);
		if(!examOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messageSource.getMessage("create.current.academic.year"),HttpStatus.BAD_REQUEST);
		}
	
		Optional<ExamTimeTableHdr> examObj = examTimeTableHdrService.findByAcademicYearObjAndStandardObjAndExamObj(academicYearOptional.get(),standardOptional.get(),examOptional.get());
		
		ExamTimeTableDtlDTO examDtlDTOObj = null;
        List<ExamTimeTableDtlDTO> examDtlDTOList = new ArrayList<>();
       	 for(ExamTimeTableDtl examDtlObj : examObj.get().getExamTimeTableDtlList() ) {
       		examDtlDTOObj = new ExamTimeTableDtlDTO();
       		examDtlDTOObj.setId(examDtlObj.getId());
       		examDtlDTOObj.setSubjectName(examDtlObj.getSubjectObj().getSubjectObj().getName());
       		examDtlDTOObj.setExamDate(examDtlObj.getExamDate());
       		examDtlDTOObj.setStartTime(examDtlObj.getStartTime());
       		examDtlDTOObj.setEndTime(examDtlObj.getEndTime());
       		examDtlDTOList.add(examDtlDTOObj);
				
		}
       
		try {
			Resource resource = new ClassPathResource("Exam_Time_Table.xlsx");
			InputStream fileStream = resource.getInputStream();

			XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
			XSSFSheet worksheet = workbook.getSheetAt(0);
			int rowvariable = 1;
			Cell cell = null;
			for (ExamTimeTableDtlDTO examTimeTableDtlDTO : examDtlDTOList) {
				Row row = worksheet.createRow(rowvariable);

				cell = row.createCell(0);
				cell.setCellValue(examTimeTableDtlDTO.getSubjectName());

				cell = row.createCell(1);
				cell.setCellValue(DateUtil.getStrDate(examTimeTableDtlDTO.getExamDate()));

				cell = row.createCell(2);
				cell.setCellValue(examTimeTableDtlDTO.getStartTime());

				cell = row.createCell(3);
				cell.setCellValue(examTimeTableDtlDTO.getEndTime());

				rowvariable++;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();
			byte[] barray = bos.toByteArray();
			InputStream excelInputStream = new ByteArrayInputStream(barray);

			return ResponseEntity.ok().header("Access-Control-Expose-Headers", "Content-Disposition")
					.header("Content-Disposition", "attachment; filename=\"Exam_Time_Table.xlsx\"")
					.body(new InputStreamResource(excelInputStream));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
}
	

