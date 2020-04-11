package com.server;

import java.net.*;
import java.io.*;
import java.util.regex.Pattern;

public class Client implements Runnable {
	
	private Socket client;
	private Socket data_socket;
	private DataInputStream in;
	private DataOutputStream out;

	Client(Socket socket) {
		client = socket;
	}
	@Override
	public void run() {
		try{

			//Входной поток сервера
			in = new DataInputStream(client.getInputStream());

			//Выходной поток сервера
			out = new DataOutputStream(client.getOutputStream());

			String request = in.readUTF();
			System.out.println("in " + ServerMain.numberOfOnline + " :" + request);

			ParsedRequest parsedRequest = new ParsedRequest(request);

			Sql_service sqlService = new Sql_service();


			String strOut = "bad";
			switch (parsedRequest.getCode())
			{
				case 100:
				{
					if (sqlService.registration(entryClient.login,entryClient.password) != Codes.CodeSql.OkRegistration) {
						strOut = entryClient.out + "Bad Registration " + "://199";
						out.writeUTF(entryClient.out + "Bad Registration " + "://199");
					}
					else {
						strOut = parsedRequest.out + "Ok Registration " + "://100";
						out.writeUTF(parsedRequest.out + "Ok Registration " + "://100");
					}
					out.flush();
					break;
				}
				case 101:
				{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						strOut = parsedRequest.out + "Bad Authorization " + "://199";
						out.writeUTF(parsedRequest.out + "Bad Authorization " + "://199");
					}
					else {
						strOut = parsedRequest.out + "Ok Authorization " + "://100";
						out.writeUTF(parsedRequest.out + "Ok Authorization " + "://100");
					}
					out.flush();
					break;
				}
				//кейсы служебной инфы (сколько места, список файлов)
				case 200:
				{
					Storage storage = sqlService.getStorage(parsedRequest.getLogin(),parsedRequest.getPassword());
					strOut = parsedRequest.out + storage.getStorageAll() +"/" + storage.getStorageFill() + "://200";
					out.writeUTF(parsedRequest.out + storage.getStorageAll() +"/" + storage.getStorageFill() + "://200");
					out.flush();
					break;
				}

				default: break;
			}
			System.out.println("out " + ServerMain.numberOfOnline + " :" + strOut);
		}

		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		try {
			in.close();
			out.close();
			client.close();
			System.out.println("Close Connection " + ServerMain.numberOfOnline);
			ServerMain.numberOfOnline--;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
	}


	public class ParsedRequest
	{
		private String login;
		private String password;
		private String text;
		private String urlOrData;
		private int code;
		private String out;

        public ParsedRequest(String request) throws IOException
        {
            Pattern pattern = Pattern.compile("://");
            String[] strings_all = pattern.split(request);
            code = Integer.parseInt(strings_all[2]);

            pattern = Pattern.compile("/");

            String[] string_login_password = pattern.split(strings_all[0]);
            login = string_login_password[0];
            password = string_login_password[1];

            pattern = Pattern.compile(" /");

            String[] string_text_url = pattern.split(strings_all[1]);
            text = string_text_url[0];
            urlOrData = string_text_url[1];

            out = strings_all[0] + "://" + text + "://";

        }

        public int getCode() {
            return code;
        }

        public String getText() {
            return text;
        }

        public String getLogin() {
            return login;
        }

        public String getOut() {
            return out;
        }

        public String getPassword() {
            return password;
        }

        public String getUrlOrData() {
            return urlOrData;
        }
    }
}
