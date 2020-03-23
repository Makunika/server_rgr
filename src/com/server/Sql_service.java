package com.server;

import java.net.*;
import java.sql.*;
import java.io.*;
public class Sql_service {
	private static final String sql_url = "jdbc:mysql://localhost/users?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private static final String sql_login = "root";
	private static final String sql_password = "Root_password";
	private String query;
	ErrorCodes a;
	private Connection conn;
	private Statement state;
	private ResultSet result;
	int Connect()
	{
		try {
			conn = DriverManager.getConnection(sql_url, sql_login, sql_password);
		return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 1;
		}
	}
	int OpenChanells() throws SQLException
	{
		state =  conn.createStatement();
		result=state.executeQuery(query);
		return 0;
	}
	int CloseConnection()
	{
		return 0;
	}
	
	int Registration(String login,String password)
	{
		try {
		if(conn==null)
		{
			if(Connect()==0);
				else return 1;
		} else
			if(!conn.isValid(1))
					if(Connect()==0);
						else return 1;
		}
		catch(SQLException e1){
			return 101;//Connection error
		}
		
		try {
		query = "SELECT * FROM clients"
				+ " WHERE LOGIN = '" + login+ "';";
		OpenChanells();
			if(!result.isBeforeFirst())
			{
				query="INSERT clients(login,password) VALUES ('"+login+"','"+password+"');";
				state.executeUpdate(query);
			}
			else return -1;//��� ���� ������������
		}
		catch(SQLException e1){
			//slomalos
		}
		finally {
			CloseConnection();
		}
		return 0;
	}
	int Authorization(String login,String password)
	{
		if(conn==null)
		{
			if(Connect()==0);
				else return 1;
		} else
			try {
				if(!conn.isValid(1))
					if(Connect()==0);
						else return 1;
			} catch (SQLException e1) {}
		try {
			state =  conn.createStatement();
			result=state.executeQuery(query);
			if(result.isBeforeFirst())
			{
				conn.close();
				state.close();
				result.close();
				return 0;
			}
			else return 3;
		} catch (SQLException e) { }
		finally
		{
			try {
				conn.close();
				state.close();
				result.close();
			} catch (SQLException e) {
				return 5;
			}
		}
		return 0;
	}
	
}
