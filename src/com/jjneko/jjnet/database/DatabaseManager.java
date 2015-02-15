package com.jjneko.jjnet.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.h2.jdbcx.JdbcDataSource;


public class DatabaseManager {
	
	/* TODO remove the random!!!*/
	public String databaseName = "jjnetdb"+(int)(Math.random()*100.0);
	public Connection conn;
	
	public DatabaseManager(){
		try {
			
			JdbcDataSource ds = new JdbcDataSource();
			ds.setURL("jdbc:h2:./"+databaseName);
			ds.setUser("sa");
			ds.setPassword("sa");
			conn = ds.getConnection();
			
//			Scanner s = new Scanner(System.in);
//			s.useDelimiter("\\s*[;]\\s*");
//			String sql = s.next();
			System.out.println("Database initialized!");
		} catch (Exception e) {
			System.out.println("Database initialization failed!");
			e.printStackTrace();
		}
		
		
	}
	
	
	 public void readSQLfile(File f) throws SQLException{
	        String s            = new String();
	        StringBuffer sb = new StringBuffer();
	 
	        try
	        {
	            FileReader fr = new FileReader(f);
	 
	            BufferedReader br = new BufferedReader(fr);
	 
	            while((s = br.readLine()) != null)
	            {
	                sb.append(s);
	            }
	            br.close();

	            String[] inst = sb.toString().split(";");
	 
	            Statement st = conn.createStatement();
	 
	            for(int i = 0; i<inst.length; i++)
	            {
	               
	                if(!inst[i].trim().equals(""))
	                {
	                    st.executeUpdate(inst[i]);
	                    System.out.println(">>"+inst[i]);
	                }
	            }
	   
	        }
	        catch(Exception e){
	            e.printStackTrace();
	        }
	 
	}

}
