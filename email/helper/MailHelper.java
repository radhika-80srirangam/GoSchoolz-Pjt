package com.iskool.email.helper;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.context.Context;

import com.iskool.enumeration.EmailType;
import com.iskool.response.TransactionContext;

public interface MailHelper {
    
    public static final String RANGDE_EMPATHY_MAIL = "ulaganathantoall@gmail.com";
    public static final String RANGDE_FINANCE_MAIL = "ulaganathantoall@gmail.com"; // mailing list
    public static final String RANGDE_FINANCE_ADMIN_MAIL = "ulaganathantoall@gmail.com";
    public static final String RANGDE_FINANCE_STATS_MAIL = "ulaganathantoall@gmail.com";
    public static final String RANGDE_TECH_STATS_MAIL = "ulaganathantoall@gmail.com";
    public static final String RANGDE_TECH_MAIL = "ulaganathantoall@gmail.com";
    public static final String RANGDE_SUPPORT_FROM_MAIL = "ulaganathantoall@gmail.com";

    void sendMail(String to, String subject, String htmlTemplate, Context context, Long investorId, EmailType emailType);
    @Async void sendMailAsync(String to, String subject, String htmlTemplate, Context context);

    void sendMail(String to, String bcc, String subject, TransactionContext context, MailAttachment mailAttachment);

    void sendMail(String to, String subject, String htmlTemplate, Context context, MailAttachment attachment);
    
    void sendMail(String to, String bcc, String subject, String htmlTemplate, Context context, MailAttachment attachment);

    void sendMail(String to, String subject, String htmlTemplate, Context context, List<MailAttachment> attachments);

    void sendMailFromSmita(String to, String subject, String htmlTemplate, Context context);
    
	void sendMail(String to, String bcc, String subject, String htmlTemplate, Context context);
    
    
}
