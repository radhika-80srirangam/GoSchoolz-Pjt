package com.iskool.event.listener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.iskool.email.helper.MailHelper;
import com.iskool.entity.NotificationHdr;
import com.iskool.event.NotificationEvent;
import com.iskool.service.AppEmailService;
import com.iskool.service.NotificationDtlService;
import com.iskool.service.NotificationHdrService;
import com.iskool.service.SectionAllotmentHistoryService;
import com.iskool.service.StudentRegistrationService;
import com.iskool.service.UserService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(onConstructor_ = { @Autowired })
public class NotificationListener {

	private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

	@Autowired(required = true)

	private @NonNull SectionAllotmentHistoryService sectionAllotmentHistoryService;

	private @NonNull AppEmailService emailService;

	@Async
	@EventListener
	public void handleNotificationLister(NotificationEvent event) {
		logger.debug("NotificationEvent encountered : {}", event);
		System.out.println(
				"Listener: NotificationEvent encountered : {}" + event.toString() + " @initiated at " + new Date());
		emailService.triggerNotificationEmail(event.getNotification());
		logger.debug("NotificationEvent successfully executed...");
	}

}
