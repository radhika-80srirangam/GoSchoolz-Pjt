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
import com.iskool.entity.SectionAllotmentHistory;
import com.iskool.entity.Student;
import com.iskool.enumeration.Status;
import com.iskool.event.SectionAllotmentHistoryEvent;
import com.iskool.service.SectionAllotmentHistoryService;

@Component
public class SectionAllotmentHistoryListner {

	private static final Logger logger = LoggerFactory.getLogger(SectionAllotmentHistoryListner.class);

	@Autowired(required = true)
	@NonNull
	private SectionAllotmentHistoryService sectionAllotmentHistoryService;

	@Async
	@EventListener
	public void handleSectionAllotmentHistoryEvent(SectionAllotmentHistoryEvent event) {
		logger.debug("SectionAllotmentHistoryEvent encountered : {}", event);
		System.out.println("Listener: SectionAllotmentHistoryEvent encountered : {}" + event.toString() + " @initiated at " + new Date());
		SectionAllotmentHistory his=new SectionAllotmentHistory();
		
		Student stu=new Student();
		stu.setId(event.getDto().getId());
		
		Section section=new Section();
		section.setId(event.getDto().getSectionId());
		
		his.setSectionObj(section);
		his.setStuObj(stu);
		his.setStatus(Status.ACTIVE);
		
		sectionAllotmentHistoryService.saveOrUpdate(his);
		try {
			logger.debug("Secton allotment history saved successfully");
		} catch (Exception e) {
			// simply log it and go on...
			logger.error("Secton allotment history saving operation failed", e);
		}
	}

}
