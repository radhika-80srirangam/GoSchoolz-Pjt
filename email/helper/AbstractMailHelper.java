package com.iskool.email.helper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.iskool.enumeration.EmailType;

abstract class AbstractMailHelper implements MailHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMailHelper.class);

	private static final String RANGDE_SUPPORT_FROM_MAIL = "santhoshkumar.engr@gmail.com"/*"ulaganathantoall@gmail.com"*/;
    private static final String RANGDE_SUPPORT_FROM_NAME = "BS LOGITADE";
    private static final String SMITA_FROM_MAIL = "kjhfdjdhf@dfdfd.in";
    private static final String SMITA_FROM_NAME = "GGGGG | GGGGG";

    private SpringTemplateEngine thymeleafTemplateEngine;
    
   /* private InvestorEmailLogRepository investorEmailLogRepository;
    
    @Autowired
    final void setInvestorEmailLogRepository(InvestorEmailLogRepository investorEmailLogRepository) {
        this.investorEmailLogRepository = investorEmailLogRepository;
    }*/

    // see https://www.baeldung.com/spring-autowired-abstract-class
    @Autowired
    final void setThymeleafTemplateEngine(SpringTemplateEngine thymeleafTemplateEngine) {
        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
    }
    
    SpringTemplateEngine getThymeleafTemplateEngine() {
        return thymeleafTemplateEngine;
    }

    abstract JavaMailSender getMailSender();

//    abstract SpringTemplateEngine getThymeleafTemplateEngine();

//  @Value("${config.is.dev.mode}")
//  private boolean isDevMode;
//
//  @Value("${config.dev.mode.email.recipient}")
//  private String devModeRecipient;

    @Override
    public void sendMail(String to, String subject, String htmlTemplate, Context context, Long investorId, EmailType emailType) {
        send(to, null, RANGDE_SUPPORT_FROM_MAIL, RANGDE_SUPPORT_FROM_NAME, subject, htmlTemplate, context, null, investorId, emailType);
    }

    @Override
    public void sendMailAsync(String to, String subject, String htmlTemplate, Context context) {
        send(to, null, RANGDE_SUPPORT_FROM_MAIL, RANGDE_SUPPORT_FROM_NAME, subject, htmlTemplate, context, null, null, null);
    }

    @Override
    public void sendMail(String to, String bcc, String subject, String htmlTemplate, Context context) {
        send(to, bcc, RANGDE_SUPPORT_FROM_MAIL, RANGDE_SUPPORT_FROM_NAME, subject, htmlTemplate, context, null, null, null);
    }

    @Override
    public void sendMail(String to, String subject, String htmlTemplate, Context context, MailAttachment attachment) {
    	List<MailAttachment> attachments = null;
    	if(attachment != null) {
    		attachments = new ArrayList<MailAttachment>();
    		attachments.add(attachment);
    	}
    	send(to, null, RANGDE_SUPPORT_FROM_MAIL, RANGDE_SUPPORT_FROM_NAME, subject, htmlTemplate, context, 
    			attachments , null, null);
    }
    
    @Override
    public void sendMail(String to, String bcc, String subject, String htmlTemplate, Context context, MailAttachment attachment) {
    	List<MailAttachment> attachments = null;
    	if(attachment != null) {
    		attachments = new ArrayList<MailAttachment>();
    		attachments.add(attachment);
    	}
        send(to, bcc, RANGDE_SUPPORT_FROM_MAIL, RANGDE_SUPPORT_FROM_NAME, subject, htmlTemplate, context, 
        		attachments , null, null);
    }

    @Override
    public void sendMail(String to, String subject, String htmlTemplate, Context context, List<MailAttachment> attachments) {
        send(to, null, RANGDE_SUPPORT_FROM_MAIL, RANGDE_SUPPORT_FROM_NAME, subject, htmlTemplate, context, attachments, null, null);
    }

    @Override
    public void sendMailFromSmita(String to, String subject, String htmlTemplate, Context context) {
        send(to, null, SMITA_FROM_MAIL, SMITA_FROM_NAME, subject, htmlTemplate, context, null, null, null);
    }

    private void send(
            String to,
            String bcc,
            String from,
            String fromName,
            String subject,
            String htmlTemplate,
            Context context,
            List<MailAttachment> attachments,
            Long investorId, EmailType emailType) {
        MimeMessage message = getMailSender().createMimeMessage();

        try {
            MimeMessageHelper msg = new MimeMessageHelper(message, true, "UTF-8");
            
            if (StringUtils.isNotBlank(to)) {
            	String[] toEmailIds = to.split(",");
            	msg.setTo(toEmailIds);
            }

            if (StringUtils.isNotBlank(bcc)) {
                String[] bccEmailIds = bcc.split(",");
                msg.setBcc(bccEmailIds);
            }

            //            if (isDevMode) {
            //                msg.setTo(devModeRecipient);
            //            }

            msg.setFrom(from, fromName);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            String body = getThymeleafTemplateEngine().process(htmlTemplate, context);
            msg.setText(body, true); // html, not plain text
            LOG.debug("to: {}, body: {}", to, body);

            if (attachments != null) {
                for (MailAttachment attachment : attachments) {
                	InputStreamSource bytes = new ByteArrayResource(attachment.getInputStreamSource());
                    msg.addAttachment(attachment.getFileName(), bytes);
                }
            }

        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.error("Failed to generate email message!", e);
        }
        getMailSender().send(message);
        
        // save this to DB 
       /* try {
            if (investorId != null && emailType != null) {
                InvestorEmailLog emailLog = new InvestorEmailLog(investorId, emailType);
                investorEmailLogRepository.save(emailLog);
            }
        } catch (Exception e) {
            LOG.error("Exception occured while saving to email log for investor: {} emailType: {}!", investorId, emailType,
                    e);
        }*/
    }

  

}


