package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
	public static final int PORT1 = 24571;
	public static int numberOfOnline = 0;


	public static void main(String[] args) {
		Socket client = null;
		ServerSocket serverSocket = null;
		try {
			try {
				serverSocket = new ServerSocket(PORT1);
				while (true) {
					System.out.println("Waiting...");
					client = serverSocket.accept();
					numberOfOnline++;
					System.out.println("One more clients has been connected");
					System.out.println("The are " + numberOfOnline + " clients online");
					Runnable r = new Client(client);
					Thread t =  new Thread(r);
					t.start();
				}
			}
			finally {
				client.close();
				serverSocket.close();
			}
		}

		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
