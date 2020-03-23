package com.server;

import java.net.*;
import java.util.LinkedList;
import java.io.*;
public class Client implements Runnable {
	
	private Socket client;
	private Socket data_socket;
	private DataInputStream in;
	private PrintWriter out;

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
			out = new PrintWriter(client.getOutputStream(),true);
			BufferedReader bRead = new BufferedReader(new InputStreamReader(System.in));

			int count = 5;
			String entry = in.readUTF();
			System.out.println(entry);


			if (entry.equalsIgnoreCase("yes")) {
				out.write("Connection on");
				//out.flush();
			} else if (entry.equalsIgnoreCase("count")) {
				out.write(Integer.toString(count));
				//out.flush();
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
			ServerMain.numberOfOnline--;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
	}
}
