package com.iskool.event.listener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.iskool.enumeration.GenderType;
import com.iskool.event.MessageSentToParentEvent;
import com.iskool.service.SectionAllotmentHistoryService;

@Component
public class MessageSentToParentListner {

	private static final Logger logger = LoggerFactory.getLogger(MessageSentToParentListner.class);

	@Autowired(required = true)
	@NonNull
	private SectionAllotmentHistoryService sectionAllotmentHistoryService;

	@Async
	@EventListener
	public void handleMessageSentToParentEvent(MessageSentToParentEvent event) {
		logger.debug("MessageSentToParentEvent encountered : {}", event);
		System.out.println("Listener: MessageSentToParentEvent encountered : {}" + event.toString()+ " @initiated at " + new Date());
		
		//Plug the messages to application to send message for parents
		
		String children=event.getStudent().getGender().equals(GenderType.Female)?"DAUGHTER ":"SON ";
		String message="YOUR "+children+"CURRENT ACADEMIC TERM FEES "+event.getComponent().getAmount()+". THE DUE DATE FOR THE FEES DUE IS "+event.getComponent().getDueDate().toString();
		
		System.out.println(message);

		try {
			logger.debug("Message successfully sent to parents.");
		} catch (Exception e) {
			// simply log it and go on...
			logger.error("Message sent event is failed", e);
		}
	}

}
