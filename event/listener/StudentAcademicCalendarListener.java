package com.iskool.event.listener;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.iskool.dto.StudentWorkingDayDTO;
import com.iskool.dto.StudentWorkingMonthDTO;
import com.iskool.entity.StudentWorkingDay;
import com.iskool.enumeration.Status;
import com.iskool.event.StudentAcademicCalendarEvent;
import com.iskool.service.AppEmailService;
import com.iskool.service.StudentWorkingDayService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(onConstructor_ = { @Autowired })
public class StudentAcademicCalendarListener {

	private static final Logger logger = LoggerFactory.getLogger(StudentAcademicCalendarListener.class);

	@Autowired(required = true)

	private @NonNull AppEmailService emailService;
	private @NonNull StudentWorkingDayService studentWorkingDayService;
	@Async
	@EventListener
	public void handleStudentAcademicCalendarListener(StudentAcademicCalendarEvent event) {
		
		
		 StudentWorkingDay studentWorkingDay =null;
		 StudentWorkingMonthDTO obj = event.getStudentWorkingMonthDTO();
		 
		 for(StudentWorkingDayDTO list:obj.getStudentWorkingDayList()) {
			 if (list.getId()== null) {
			 studentWorkingDay = new StudentWorkingDay();
			 studentWorkingDay.setAcademicYearId(obj.getAcademicYearId());
			 studentWorkingDay.setMonth(obj.getMonth());
			 studentWorkingDay.setDate(list.getDate());
			 studentWorkingDay.setDayType(list.getDayType());
			 studentWorkingDay.setNotes(list.getNotes());
			 studentWorkingDay.setStatus(Status.ACTIVE);
			 studentWorkingDayService.save(studentWorkingDay);
		 }
		 else {
			 
			 Optional<StudentWorkingDay> studentWorkingDayOptional = studentWorkingDayService.findById(list.getId());
			 StudentWorkingDay studentWorkingDayObj = studentWorkingDayOptional.get();
			 studentWorkingDayObj.setAcademicYearId(obj.getAcademicYearId());
			 studentWorkingDayObj.setDate(list.getDate());
			 studentWorkingDayObj.setMonth(obj.getMonth());
			 studentWorkingDayObj.setDayType(list.getDayType());
			 studentWorkingDayObj.setNotes(list.getNotes());
			 studentWorkingDayObj.setStatus(Status.ACTIVE);
			 studentWorkingDayService.saveOrUpdate(studentWorkingDayObj);
			 
		 }
			 
		 }
		 
		 try {
				logger.debug("Working days created successfully");
			} catch (Exception e) {
				// simply log it and go on...
				logger.error("Working days created operation failed", e);
		logger.debug("Working days successfully executed...");
	}

	}}
