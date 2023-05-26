package curso.api.rest.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class ServiceEnviaEmail {

    private static final String EMAIL = "pedrotesteprojetos@gmail.com";
    private static final String SENHA = "lwvxctzasbrqkcvz";

    public void enviarEmail(String assunto, String emailDestino, String mensagem) throws Exception {
        Properties properties = new Properties();
        properties.put("mail.smtp.ssl.trust", "*");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, SENHA);
            }
        });

        Address[] toUser = InternetAddress.parse(emailDestino);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL)); //Quem está enviando o e-mail
        message.setRecipients(Message.RecipientType.TO, toUser); //Quem vai receber
        message.setSubject(assunto);
        message.setText(mensagem);

        Transport.send(message);

    }

}
