package com.iskool.event.listener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import com.iskool.email.helper.MailHelper;
import com.iskool.event.PasswordSendEvent;

@Component
public class PasswordSendListnerService {

	private static final Logger logger = LoggerFactory.getLogger(PasswordSendListnerService.class);

	@Autowired(required = true)
	@NonNull
	MailHelper mailHelper;

	@Async
	@EventListener
	public void handlePasswordSendEvent(PasswordSendEvent event) {
		logger.error("PasswordSendEvent encountered : {}", event);
		System.out.println(
				"Listener: PasswordSendEvent encountered : {}" + event.toString() + " @initiated at " + new Date());

		String toEmail = event.getEmailTo();

		Context context = new Context();
		context.setVariable("password", event.getPassword());
		String mail_subject = "Send Password For ";
		String mail_template = "password/passwordSend";

		try {
			mailHelper.sendMail(toEmail, null, mail_subject, mail_template, context);
			logger.debug("Password sent successfully");
		} catch (Exception e) {
			// simply log it and go on...
			logger.error("Password sending process failed...", e);
		}
	}

}
