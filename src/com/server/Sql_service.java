package com.server;

import com.server.Codes.CodeSql;

import java.sql.*;

public class Sql_service {
	private static final String sql_url = "jdbc:mysql://localhost/users?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private static final String sql_login = "root";
	private static final String sql_password = "12345"; //Root_password
	private String query;
	private Connection connection;
	private Statement state;
	private ResultSet result;

	private void Connect() throws SQLException
	{
		if(connection == null || !connection.isValid(1))
			connection = DriverManager.getConnection(sql_url, sql_login, sql_password);
	}

	private void OpenChancels() throws SQLException
	{
		state =  connection.createStatement();
		result = state.executeQuery(query);
	}

	private void CloseConnection()
	{
		try {
			connection.close();
			state.close();
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public Storage getStorage(String login,String password)
	{
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		Storage storage = new Storage();

		query = "SELECT storage_all,storage_fill FROM users"
				+ " WHERE LOGIN = '" + login + "' AND "
				+ "PASSWORD = '" + password + "';";

		try {
			state = connection.createStatement();
			result=state.executeQuery(query);
			if(result.isBeforeFirst())
			{
				result.next();
				storage.setStorageAll(result.getLong("storage_all"));
				storage.setStorageFill(result.getLong("storage_fill"));
			}
			else
			{
				storage.setStorageAll(0);
				storage.setStorageFill(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			CloseConnection();
		}


		return storage;
	}


	public String[] getPasswordAndLogin(String email)
	{
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
		}

		String[] loginAndPassword = new String[]
				{
					"null",
					"null"
				};

		try {
			query = "SELECT * FROM users"
					+ " WHERE EMAIL = '" + email + "';";
			OpenChancels();
			state = connection.createStatement();
			result=state.executeQuery(query);
			if(result.isBeforeFirst()) {
				result.next();
				loginAndPassword[1] = result.getString("password");
				loginAndPassword[0] = result.getString("login");
			}
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		finally {
			CloseConnection();
		}
		return loginAndPassword;
	}

	public long GetVersion(String login,String password){
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
		}

		query = "SELECT version FROM users"
				+ " WHERE LOGIN = '" + login + "' AND "
				+ "PASSWORD = '" + password + "';";
		long version=-1;
		try {
			state = connection.createStatement();
			result=state.executeQuery(query);
			if(result.isBeforeFirst())
			{
				result.next();
				version=(result.getLong("version"));
			}
			else
			{
				version=0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			CloseConnection();
		}
		return version;
	}

	public void UpdateVersion(String login,String password){
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		try {
			query = "SELECT * FROM users"
					+ " WHERE LOGIN = '" + login + "' AND "
					+ "PASSWORD = '" + password + "';";
			OpenChancels();
			if(!result.isBeforeFirst()) {
				query="INSERT users(version) VALUES ('"+1+"');";
				state.executeUpdate(query);
			}
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		finally {
			CloseConnection();
		}
	}

	public void ChangeSpaceFill(String login, String password,long newSize){
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
		}

		try {
			query = "SELECT * FROM users"
					+ " WHERE LOGIN = '" + login + "' AND "
					+ "PASSWORD = '" + password + "';";
			OpenChancels();
			if(!result.isBeforeFirst()) {
				query="INSERT users(storage_fill) VALUES ('"+newSize+"');";
				state.executeUpdate(query);
			}
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		finally {
			CloseConnection();
		}
	}
	public void ChangeSpace(String login, String password,long newSize)
	{
		ChangeSpaceFill(login,password, getStorage(login, password).storageFill+newSize);
	}

	public CodeSql registration(String login, String password, String email)
	{
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
			return CodeSql.BadSqlConnection;
		}
		
		try {
		query = "SELECT * FROM users"
				+ " WHERE LOGIN = '" + login + "' OR "
				+ "EMAIL = '" + email + "';";
		OpenChancels();
			if(!result.isBeforeFirst()) {
				query="INSERT users(login,password, storage_all, storage_fill,email) VALUES ('"+login+"','"+password+"','16106127360','0','"+email+"');";
				state.executeUpdate(query);
			}
			else return CodeSql.Bad;
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		finally {
			CloseConnection();
		}
		return CodeSql.OkRegistration;
	}

	public CodeSql authorization(String login, String password)
	{
		try {
			Connect();
		}
		catch(SQLException e){
			e.printStackTrace();
			return CodeSql.BadSqlConnection;
		}
		query = "SELECT * FROM users"
				+ " WHERE LOGIN = '" + login + "' AND "
				+ "PASSWORD = '" + password + "';";

		try {
			state = connection.createStatement();
			result=state.executeQuery(query);
			if(result.isBeforeFirst())
			{
				return CodeSql.OkAuthorization;
			}
			else return CodeSql.BadAuthorization;
		} catch (SQLException e) { }
		finally
		{
			CloseConnection();
		}
		return CodeSql.OkAuthorization;

	}
	
}
