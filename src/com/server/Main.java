package com.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        int count = 0;
        while (true)
        {
            try (ServerSocket server = new ServerSocket(4999))
            {
                Socket client = server.accept();
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream());

                String entry = in.readUTF();
                if (entry.equalsIgnoreCase("yes")) {
                    out.writeUTF("Connection on");
                    out.flush();
                } else if (entry.equalsIgnoreCase("count")) {
                    out.writeUTF(Integer.toString(count));
                    out.flush();
                    count++;
                    System.out.println("Send" + count);
                }
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
