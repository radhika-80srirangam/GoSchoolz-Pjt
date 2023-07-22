package com.iskool.email.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.iskool.response.TransactionContext;

@Component
@Primary
public class MailHelperDefault extends AbstractMailHelper {

    private JavaMailSender mailSender;
//    private SpringTemplateEngine thymeleafTemplateEngine;

    @Autowired
    public MailHelperDefault(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

//    @Autowired
//    public MailHelperDefault(JavaMailSender mailSender, SpringTemplateEngine thymeleafTemplateEngine) {
//        this.mailSender = mailSender;
//        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
//    }
//    
    @Override
    JavaMailSender getMailSender() {
        return mailSender;
    }

	@Override
	public void sendMail(String to, String bcc, String subject, TransactionContext context,
			MailAttachment mailAttachment) {
		// TODO Auto-generated method stub
		
	}

//    @Override
//    SpringTemplateEngine getThymeleafTemplateEngine() {
//        return thymeleafTemplateEngine;
//    }

}
