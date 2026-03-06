package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.model.EmailDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final JavaMailSender javaMailSender;

    public String sendEmailForgotPassword(String fullName, String toEmail, String  linkForgotPassword, String uid) {

        EmailDetails model = new EmailDetails();
        model.setSubject(AppSys.TITLE_EMAIL_FORGOT_PASSWORD);

        StringBuilder body =  new StringBuilder();
        body.append(AppSys.FORGOT_PASSWORD_BODY_ROW_1.replace("{full_name}", fullName));
        body.append(AppSys.FORGOT_PASSWORD_BODY_ROW_2);

        // Set user mobile uuid
        String fullLink = linkForgotPassword + "/?uid=" + uid;
        body.append(AppSys.REGISTER_BODY_EMAIL_ROW_3.replace("{link_register}", fullLink));

        model.setMsgBody(body.toString());
        model.setRecipient(toEmail);

        String logs = this.sendEmail(model);
        log.info("Send email forgot password. -> {}", logs);

        return "success";
    }


    public String sendEmailRegister(String fullName, String toEmail, String  linkRegister, String uuid) {

        EmailDetails model = new EmailDetails();
        model.setSubject(AppSys.TITLE_EMAIL_REGISTER);

        StringBuilder body =  new StringBuilder();
        body.append(AppSys.REGISTER_BODY_EMAIL_ROW_1.replace("{full_name}", fullName));
        body.append(AppSys.REGISTER_BODY_EMAIL_ROW_2);

        // Set user mobile uuid
        String fullLink = linkRegister + "/?uid=" + uuid;
        body.append(AppSys.REGISTER_BODY_EMAIL_ROW_3.replace("{link_register}", fullLink));

        model.setMsgBody(body.toString());
        model.setRecipient(toEmail);

        String logs = this.sendEmail(model);
        log.info("Send email register. -> {}", logs);
        return "success";
    }

    public String sendEmailDeleteUser(String toEmail, Map<String, String> resultUser,  Map<String, String> resultCert) {

        EmailDetails model = new EmailDetails();
        model.setSubject("Delete user mobile.");

        StringBuilder body =  new StringBuilder();

        body.append("รายชื่อที่ถูกลบ <br/>");

        // User
        for(String key : resultUser.keySet()){
            body.append(resultUser.get(key)).append("\n");
        }

        // Cert
        for(String key : resultCert.keySet()){
            body.append(resultCert.get(key)).append("\n");
        }

        model.setMsgBody(body.toString());
        model.setRecipient(toEmail);

        String logs = this.sendEmail(model);
        log.info("Send email delete user. -> {}", logs);
        return "success";
    }

    private String sendEmail(EmailDetails details)
    {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setSubject(details.getSubject());
            helper.setFrom(AppSys.SENDER_NAME, AppSys.SENDER_NAME_SPACE);
            helper.setTo(details.getRecipient());
            helper.setText(details.getMsgBody(), true);

            // Sending the mail
            javaMailSender.send(message);

            return "Mail Sent Successfully...";
        } catch (Exception e) {
            log.error("Exception ->  {}", e);
            return "Error while Sending Mail";
        }
    }

}
