package com.server;

import java.net.*;
import java.sql.*;
import java.util.LinkedList;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
	public static int PORT1 = 24571;
	public static int PORT2 = 24570;
	public static int numberOfOnline = 0;


	public static void main(String[] args) {
		Socket client = null;
		ServerSocket serverSocket = null;
		try {
			try {
				serverSocket = new ServerSocket(PORT1);
				System.out.println("Waiting...");
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
