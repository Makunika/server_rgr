package com.server;

import java.net.*;
import java.util.LinkedList;
import java.io.*;
import java.util.regex.Pattern;

public class Client implements Runnable {
	
	private Socket client;
	private Socket data_socket;
	private DataInputStream in;
	private DataOutputStream out;

	Client(Socket socket)
	{
		client = socket;
	}
	@Override
	public void run() {
		try{

			//Входной поток сервера
			in = new DataInputStream(client.getInputStream());

			//Выходной поток сервера
			out = new DataOutputStream(client.getOutputStream());

			int count = 5;
			String entry = in.readUTF();
			System.out.println(entry);

			StringsClient entryClient = ParseRequest(entry);

			Sql_service sqlService = new Sql_service();



			switch (entryClient.code)
			{
				case 100:
				{
					if (sqlService.Registration(entryClient.login,entryClient.password) != Codes.CodeSql.OkRegistration)
					{
						out.writeUTF(entryClient.out + "Bad Registration " + "://199");
					}
					else
					{
						out.writeUTF(entryClient.out + "Ok Registration " + "://100");
					}
					out.flush();
					break;
				}
				case 101:
				{
					if (sqlService.Authorization(entryClient.login,entryClient.password) != Codes.CodeSql.OkAuthorization)
					{
						out.writeUTF(entryClient.out + "Bad Authorization " + "://199");
					}
					else
					{
						out.writeUTF(entryClient.out + "Ok Authorization " + "://100");
					}
					out.flush();
				}
				default: break;
			}

		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		try {
			in.close();
			out.close();
			client.close();
			ServerMain.numberOfOnline--;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
	}
	public StringsClient ParseRequest(String entry) throws IOException
	{
		StringsClient stringsClient = new StringsClient();
		Pattern pattern = Pattern.compile("://");
		String[] strings_all = pattern.split(entry);
		stringsClient.code = Integer.parseInt(strings_all[2]);

		pattern = Pattern.compile("/");

		String[] string_login_password = pattern.split(strings_all[0]);
		stringsClient.login = string_login_password[0];
		stringsClient.password = string_login_password[1];

		pattern = Pattern.compile(" /");

		String[] string_text_url = pattern.split(strings_all[1]);
		stringsClient.text = string_text_url[0];
		stringsClient.url = string_text_url[1];

		stringsClient.out = strings_all[0] + "://" + stringsClient.text + "://";

		return stringsClient;
	}




	public class StringsClient
	{
		public String login;
		public String password;
		public String text;
		public String url;
		public int code;
		public String out;
	}
}
