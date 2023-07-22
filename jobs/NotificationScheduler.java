package com.iskool.jobs;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.iskool.controller.NotificationController;
import com.iskool.entity.NotificationHdr;
import com.iskool.enumeration.TriggerType;
import com.iskool.service.AppEmailService;
import com.iskool.service.NotificationHdrService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(onConstructor_ = { @Autowired })
public class NotificationScheduler {

	private @NonNull NotificationHdrService notificationHdrService;

	private @NonNull AppEmailService emailService;

	private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

	@Scheduled(cron = "0 0/10 * * * *")
	public synchronized void triggerNotifications() {

		log.info("NotificationScheduler: The time is now {}", LocalDate.now());

		List<NotificationHdr> notificationList = notificationHdrService.findAll();
		Date currentDate = new Date();

		LocalTime now = LocalTime.now();

		for (NotificationHdr notificationObj : notificationList) {

			if (notificationObj.getIsTriggered()) {
				continue;
			}

			if (notificationObj.getTriggerType().equals(TriggerType.Now)) {
				continue;
			}

			if (notificationObj.getTriggerDate().after(currentDate)
					|| notificationObj.getTriggerDate().equals(currentDate)) {
				continue;
			}

			if (notificationObj.getTriggerTime().equals(now.toString())) {
				continue;
			}

			emailService.triggerNotificationEmail(notificationObj);

			notificationObj.setIsTriggered(true);
			notificationHdrService.saveOrUpdate(notificationObj);
		}
		log.info("NotificationScheduler: completed at {}", LocalDate.now());
	}
}
