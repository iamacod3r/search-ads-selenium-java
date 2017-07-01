package infrastructure;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import config.MailConfig;

public class Mail {

	public Mail() {
		// TODO Auto-generated constructor stub
	}

	
	public void Send(String mailBody, MailConfig mailconf)
	{
		if(mailconf.MailSend)
		{
			Properties props = new Properties();
			props.put("mail.smtp.host", mailconf.SmtpHost);
			props.put("mail.smtp.socketFactory.port", mailconf.SmtpFactoryPort);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", mailconf.SmtpAuth);
			props.put("mail.smtp.port", mailconf.SmtpPort);
	
			Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(mailconf.Username, mailconf.Pswrd);
					}
				});
	
			try {
	
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(mailconf.Username));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailconf.ToList));
		
				message.setSubject(mailconf.Subject);
				message.setContent(mailBody, "text/html");
	
				Transport.send(message);
	
				System.out.println("Mail Sent !");
	
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
		else
		{
			System.out.println("Mail Send config is false");
		}
	}	
	
}
