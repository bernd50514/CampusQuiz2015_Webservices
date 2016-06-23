package de.ps15.campusquiz;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 
 * @author Jiahao Liang This program is set to send email from my personal gmail
 *         Account
 * TODO: change gmail account to our official QUizduell Account
 */

public class SendMail implements Runnable {

	private String username;
	private String email;
	private String activationCode;
	private String expiredDate;

	public SendMail(String username, String email, String activationCode,
			String expiredDate) {
		this.username = username;
		this.email = email;
		this.activationCode = activationCode;
		this.expiredDate = expiredDate;
	}

	@Override
	public void run() {

		// Get a Properties object
		Properties props = System.getProperties();

		// ******************** FOR PROXY ******************

		// props.setProperty("proxySet","true");
		// props.setProperty("socksProxyHost","9.10.11.12");
		// props.setProperty("socksProxyPort","80");
		// props.setProperty("socksProxyVersion","5");

		props.setProperty("mail.smtp.host", "smtp.gmail.com");

		// ******************** FOR SSL ******************
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");

		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "true");
		props.put("mail.store.protocol", "pop3");
		props.put("mail.transport.protocol", "smtp");
		
		final String myusername = "campusquiz.uni.wuerzburg";
		final String password = "campusquiz1337mfpbp";

		Session session = Session.getDefaultInstance(props,
				new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(myusername, password);
					}
				});

		// -- Create a new message --
		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress("campusquiz.uni.wuerzburg@gmail.com"));

//			msg.setFrom(new InternetAddress("campusquiz.uni.wuerzburg@gmail.com"));

			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(email, false));
			msg.setSubject("CampusQuiz Aktivierungsservice");
			msg.setSentDate(new Date());
			msg.setContent(
					"<h4>Herzlich Willkommen zum CampusQuiz! Ihre Benutzername ist: "
							+ username
							+ " </h4><h4> bitte vor "
							+ expiredDate
							+ "die Link aktivieren! sonst kannst du nicht einloggen</h4>"
							+ "<h3><a href='http://ps15server.cloudapp.net:8080/useraccount/activate/doActivation?activeCode="
							+ activationCode
							+ "'>http://ps15server.cloudapp.net:8080/useraccount/activate/doActivation?activeCode="
							+ activationCode + "</a></h3>",
					"text/html;charset=utf-8");

			Transport.send(msg);

		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// **************** Without Attachments ******************

		System.out.println("Message sent.");

	}

}
