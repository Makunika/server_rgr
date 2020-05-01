package com.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class Client implements Runnable {
	
	private Socket client;
	private Socket dataSocket;
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

			boolean isResponsed = true;
			//то, что пришло
			ParsedRequest parsedRequest = new ParsedRequest(request);
			//то, что уйдет
            Response response = new Response(parsedRequest);
            //работа с sql
			Sql_service sqlService = new Sql_service();
			//работа с памятью
			StorageService storageService=new StorageService(parsedRequest.getLogin());

			//Обработка запроса!
			switch (parsedRequest.getCode())
			{
				case 100:
				{
					if (sqlService.registration(parsedRequest.getLogin(),parsedRequest.getPassword(),parsedRequest.getInStr()) != Codes.CodeSql.OkRegistration) {
						response.setOut("Bad Registration",199);
					}
					else {

                        response.setOut("Ok Registration", 100);
					}
					break;
				}
				case 101:
				{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
                        response.setOut("Bad Authorization", 199);
					}
					else {
						long storageFill = Long.parseLong(storageService.GetSize());
						long storageAll = sqlService.getStorage(parsedRequest.getLogin(),parsedRequest.getPassword()).getStorageAll();
                        response.setOut(storageAll + "/" + storageFill + "//" + storageService.GetTree(), 100);
                        sqlService.ChangeSpaceFill(parsedRequest.getLogin(),parsedRequest.getPassword(),storageFill);
					}
					break;
				}
				case 200: {
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						response.setOut("Bad request", 299);
					} else{
						parsedRequest.ParseNewTrans();
						long size=Long.parseLong(parsedRequest.getSplitData()[1]);
						boolean isPapka;
						isPapka = !parsedRequest.splitData[2].equals("1");
						String name=parsedRequest.splitData[0];

						Storage storage = sqlService.getStorage(parsedRequest.login,parsedRequest.password);
						if (storage.storageFill + size > storage.storageAll)
						{
							response.setOut("Mesta net", 298);
						}
						else
						{
							response.setOut("ok file",200);
							response.doFlush(out);
							isResponsed = false;
							if(0==storageService.prepairTrans(in,name,size,isPapka)){
								sqlService.ChangeSpace(parsedRequest.login,parsedRequest.password,size);
							}
						}
					}
					break;
				}
				case 201:{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						response.setOut("Bad request", 299);
					} else{
						response.setOut("ok file",201);
						response.doFlush(out);
						isResponsed = false;
						storageService.OutTrans(new BufferedOutputStream(out),parsedRequest.inStr);
					}
					break;
				}
				/*Rename*/
				case 202:{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						response.setOut("Bad request", 299);
					} else{
						parsedRequest.parseRename();
						if(storageService.Rename(parsedRequest.splitData[0],parsedRequest.splitData[1])){
							response.setOut("Rename successful", 202);
						} else
							response.setOut("Rename failed",298);
					}
					break;
				}
				/*Relocate
				* Нужно ли отсылать тебе дерево?
				* */
				case 203:{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						response.setOut("Bad request", 299);
					} else{
						parsedRequest.parseRename();
						if(storageService.Relocate(parsedRequest.splitData[0],parsedRequest.splitData[1])){
							response.setOut("Relocate successful", 203);
						} else
							response.setOut("Relocate failed",298);
					}
					break;
				}
			/* Новая папка*/
				case 204:{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						response.setOut("Bad request", 299);
					} else{
						parsedRequest.parseNewCatalog();
						if(storageService.AddCatalog(parsedRequest.splitData[0],parsedRequest.splitData[1]))
							response.setOut("CreateDir failed",297);
						else
							response.setOut("CreateDir successful",294);
					}
					break;
				}
				/*Удаление*/
				case 205:{
					if (sqlService.authorization(parsedRequest.getLogin(),parsedRequest.getPassword()) != Codes.CodeSql.OkAuthorization) {
						response.setOut("Bad request", 299);
					} else{
						parsedRequest.parseDelete();
						if(storageService.Remove(parsedRequest.splitData[0])){
							response.setOut("Delete failed",297);
							Storage storage=sqlService.getStorage(parsedRequest.login,parsedRequest.password);
							sqlService.ChangeSpaceFill(parsedRequest.login,parsedRequest.password,storage.storageFill-Long.getLong(parsedRequest.splitData[1]));
						}
						else
							response.setOut("CreateDir successful",294);
					}
					break;
				}

				/*забыл парольчик*/
				case 300:{
					String[] loginAndPassword = sqlService.getPasswordAndLogin(parsedRequest.inStr);
					if (loginAndPassword[0].equals("null") || loginAndPassword[1].equals("null"))
					{
						response.setOut("Bad email", 399);
					}
					else
					{
						response.setOut("Send password and login ok", 300);
						RestorePassword.sendMail(parsedRequest.inStr,loginAndPassword[0],loginAndPassword[1]);
					}
					break;
				}

			}

			//Конец обработки запроса
			//Далее уже отправка
			if (isResponsed) response.doFlush(out);

			System.out.println("out " + ServerMain.numberOfOnline + " :" + response.out + "://" + response.code);
		}
		catch (IOException ex) {
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


	private class ParsedRequest
	{
		private String login;
		private String password;
		private String text;
		private String inStr;
		private int code;
		private String out;
		private String[] splitData;

		public void ParseNewTrans(){
			Pattern pattern = Pattern.compile("//");
			splitData = pattern.split(inStr);

		}
		public void parseRename(){
			Pattern pattern = Pattern.compile("//");
			splitData = pattern.split(inStr);
		}
		public void parseNewCatalog(){
			Pattern pattern = Pattern.compile("//");
			splitData = pattern.split(inStr);
		}
		public void parseDelete(){
			Pattern pattern = Pattern.compile("//");
			splitData = pattern.split(inStr);
		}

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
            inStr = string_text_url[1];

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

        public String getInStr() {
            return inStr;
        }

		public String[] getSplitData() {
			return splitData;
		}
	}

    private static class Response
    {
        private ParsedRequest parsedRequest;
        private int code;
        private String out;

        public Response(ParsedRequest parsedRequest)
        {
            this.parsedRequest = parsedRequest;
        }


        public void setOut(String out, int code) {
            this.out = out;
            this.code = code;
        }

        public void doFlush(DataOutputStream outputStream) throws IOException {
            if (code < 99) throw new IOException("code == null or < 99 in response");
            if (out == null || out.equals("")) throw  new IOException("out == null in response");

            String str = parsedRequest.getOut() + out + "://" + code;

            System.out.println("length out :" + str.length());
            System.out.println("out : " + str);
            byte[] b = str.getBytes(StandardCharsets.UTF_8);
            outputStream.writeInt(b.length);
            outputStream.write(b);
            outputStream.flush();
        }
    }
}
