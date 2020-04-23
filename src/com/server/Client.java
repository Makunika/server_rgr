package com.server;

import java.net.*;
import java.io.*;
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

			//то, что пришло
			ParsedRequest parsedRequest = new ParsedRequest(request);
			//то, что уйдет
            Response response = new Response(parsedRequest);
            //работа с sql
			Sql_service sqlService = new Sql_service();

			StorageService storageService=new StorageService(parsedRequest.getLogin());

			//Обработка запроса! (кстати, всегда придется работать с sql, ибо надо всегда проверять логин и пароль. Тогда может StorageService закинуть в sql?)
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
						long size=Long.getLong(parsedRequest.newTrans[1]);
						boolean isPapka;
						isPapka= parsedRequest.newTrans[2].equals("1");
						String name=parsedRequest.newTrans[0];
						ServerSocket socketData=new ServerSocket();
						response.setOut(Integer.toString(socketData.getLocalPort()),200);
						dataSocket = socketData.accept();

						if(0==storageService.prepairTrans(dataSocket.getInputStream(),name,size,isPapka,sqlService.getStorage(parsedRequest.login,parsedRequest.password))){
							sqlService.ChangeSpace(parsedRequest.login,parsedRequest.password,size);
						}

						storageService.PrepareTransfer(dataSocket.getInputStream(),size,isPapka,name);

					}

					break;
				}

			}

			//Конец обработки запроса
			//Далее уже отправка

            response.doFlush(out);

			System.out.println("out " + ServerMain.numberOfOnline + " :" + response.out + "://" + response.code);
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


	private static class ParsedRequest
	{
		private String login;
		private String password;
		private String text;
		private String inStr;
		private int code;
		private String out;
		private String [] newTrans;

		public void ParseNewTrans(){
			Pattern pattern = Pattern.compile("//");
			newTrans = pattern.split(inStr);

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
            byte[] b = str.getBytes(StandardCharsets.UTF_8);
            outputStream.writeInt(b.length);
            outputStream.write(b);
            outputStream.flush();
        }
    }
}
