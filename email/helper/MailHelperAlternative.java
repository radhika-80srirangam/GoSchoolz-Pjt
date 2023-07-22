package com.iskool.email.helper;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.iskool.response.TransactionContext;

@Component
@Qualifier("alternative")
public class MailHelperAlternative extends AbstractMailHelper {

    private JavaMailSenderImpl mailSender;

    @Value("${email.ses.smtp.host}")
    private String host;

    @Value("${email.ses.smtp.port}")
    private int port;

    @Value("${email.ses.smtp.username}")
    private String username;

    @Value("${email.ses.smtp.password}")
    private String password;

    @PostConstruct
    void configure() {
        mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
    }
    
    @Override
    JavaMailSender getMailSender() {
        return mailSender;
    }

	@Override
	public void sendMail(String to, String bcc, String subject, TransactionContext context,
			MailAttachment mailAttachment) {
		// TODO Auto-generated method stub
		
	}

}
