package com.server;

import java.net.*;
import java.util.LinkedList;
import java.io.*;
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
			BufferedReader bRead = new BufferedReader(new InputStreamReader(System.in));

			int count = 5;
			String entry = in.readUTF();
			System.out.println(entry);


			if (entry.equalsIgnoreCase("yes")) {
				System.out.println("Send");
				out.writeUTF("Connection on");
				out.flush();
				System.out.println("Send ok");
			} else if (entry.equalsIgnoreCase("count")) {
				out.writeUTF(Integer.toString(count));
				out.flush();
				count++;
				System.out.println("Send" + count);
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
			System.out.println("Close Connection " + ServerMain.numberOfOnline);
			ServerMain.numberOfOnline--;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
	}
}
