package com.iskool.event.listener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.iskool.entity.Section;
import com.iskool.entity.Student;
import com.iskool.entity.StudentRollnumberAllotmentHistory;
import com.iskool.enumeration.Status;
import com.iskool.event.StudentRollnumberAllotmentHistoryEvent;
import com.iskool.service.StudentRollnumberAllotmentHistoryService;

@Component
public class StudentRollnumberAllotmentHistoryListnerService {
 
	private static final Logger logger = LoggerFactory.getLogger(StudentRollnumberAllotmentHistoryListnerService.class);

	@Autowired(required = true)
	@NonNull
	private StudentRollnumberAllotmentHistoryService studentRollnumberAllotmentHistoryService;

	@Async
	@EventListener
	public void handleStudentRollnumberAllotmentHistoryEvent(StudentRollnumberAllotmentHistoryEvent event) {
		logger.debug("StudentRollnumberAllotmentHistoryEvent encountered : {}", event);
		System.out.println("Listener: StudentRollnumberAllotmentHistoryEvent encountered : {}" + event.toString() + " @initiated at " + new Date());
		StudentRollnumberAllotmentHistory his=new StudentRollnumberAllotmentHistory();
		
		Student stu=new Student();
		stu.setId(event.getDto().getId());
		
		Section section=new Section();
		section.setId(event.getDto().getSectionId());
		his.setRollNumber(event.getDto().getNewRollNumber());
		his.setStudentObj(stu);
		his.setStatus(Status.ACTIVE);
		
		studentRollnumberAllotmentHistoryService.saveOrUpdate(his);
		try {
			logger.debug("Student Rollnumber allotment history saved successfully");
		} catch (Exception e) {
			// simply log it and go on...
			logger.error("Student Rollnumber allotment history saving operation failed", e);
		}
	}
	
}
