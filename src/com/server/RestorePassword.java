package com.server;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class RestorePassword {

    static public void main(String args[]) throws MessagingException {
        sendMail("pshblo.max@gmail.com", "lol", "hello");
    }

    public static void sendMail(String recepient, String login, String _password) {
        try {
            Properties properties=new Properties();
            /*properties.put("mail.smpt.auth","true");
            properties.put("mail.smpt.starttls.enable","true");
            properties.put("mail.smpt.host","smpt.gmail.com");
            properties.put("mail.smpt.port","687");*/


            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.debug", "false");
            properties.put("mail.smtp.ssl.enable", "true");



            InternetAddress from = new InternetAddress("Bolt@bolt.com", "Drive");

            String myAccount="69.teambolt.69@gmail.com";
            String password="BOLTBOLT";

            Session session=Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(myAccount,password);
                }
            });

            Message message =  prepairMessage(session,myAccount,recepient, login, _password);
            message.setFrom(from);
            Transport.send(message);

        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }

    }

    private static Message prepairMessage(Session session,String myAccount,String recipient, String login, String password) {
        try {
            Message message=new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccount));
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(recipient));
            message.setSubject("Your password and login");
            message.setText(
                    "login: " + login +
                    "\npassword: " + password);
            return message;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
