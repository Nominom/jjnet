package com.jjneko.jjnet.database;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.jdbcx.JdbcDataSource;

import com.jjneko.jjnet.messaging.XML;
import com.jjneko.jjnet.networking.discovery.Advertisement;


public class DatabaseManager {
	
	private Connection conn;
	
	private TObjectLongHashMap<String> advertisements = new TObjectLongHashMap<String>();
	private TObjectIntHashMap<String> classes = new TObjectIntHashMap<String>();
	
	public DatabaseManager(){
		try {
			String databaseName;
			/* TODO remove the random!!!*/
//			databaseName = System.getProperty("jjneko.jjnet.db.dbname", "jjnetdb"+(int)(Math.random()*20.0));
			databaseName = System.getProperty("jjneko.jjnet.db.dbname", "jjnetdb");
			JdbcDataSource ds = new JdbcDataSource();
			ds.setURL("jdbc:h2:./"+databaseName);
			conn = ds.getConnection();
			
			if(isEmpty()){
				System.out.println("Database empty! Creating new schema..");
				readSQLfile(new File("sqlfile.sql"));
			}else{
				loadToMemory();
			}
			
			System.out.println("Database initialized!");
		} catch (Exception e) {
			System.out.println("Database initialization failed!");
			e.printStackTrace();
		}
		
		
	}
	
	protected boolean isEmpty() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("show tables");
        try {
            ResultSet rs = stmt.executeQuery();
            try {
                return ! rs.next();
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
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

	public Connection getConnection() {
		return conn;
	}
	
	private synchronized void loadToMemory(){
		String sql1 = "SELECT hash, advertisement FROM t_advertisements";
		PreparedStatement stmt1 = null;
		String sql2 = "SELECT class, id FROM t_classes";
		PreparedStatement stmt2 = null;
		String sql3 = "DELETE FROM t_advertisements where id IN(SELECT ad.id FROM t_advertisements AS ad JOIN t_advertisement_classes ON ad.id=advertisement_id JOIN t_classes AS cla ON cla.id=class_id WHERE class = 'com.jjneko.jjnet.networking.discovery.WorldGroupAdvertisement')";
		PreparedStatement stmt3 = null;
		try{
			stmt1=conn.prepareStatement(sql1);
			stmt2=conn.prepareStatement(sql2);
			stmt3=conn.prepareStatement(sql3);

			System.out.println(">> "+stmt1);
			ResultSet rs1 = stmt1.executeQuery();
			while(rs1.next()){
				String hash = new String(rs1.getBytes(1),"ISO-8859-1");
				String adv = new String(rs1.getBytes(2),"ISO-8859-1");
				Advertisement ad = (Advertisement) XML.parseUnsignedXML(adv);
				long valid = ad.valid_until;
				advertisements.put(hash,valid);
			}
			
			System.out.println(">> "+stmt2);
			ResultSet rs2 = stmt2.executeQuery();
			while(rs2.next()){
				String classs = rs2.getString(1);
				int id = rs2.getInt(2);
				classes.put(classs,id);
			}
			
			System.out.println(">> "+stmt3);
			stmt3.executeUpdate();
		}catch(Exception ex){ex.printStackTrace();}
		finally {
			try {
				stmt1.close();
				stmt2.close();
				stmt3.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void insertAdvertisement(Advertisement ad){
		/* TODO add timestamp checks and stuff */
		String hash = Advertisement.generateHash(ad);
		List<String> adclass = ad.getSuperClasses();
		
		
		if(!advertisements.containsKey(hash)){
			String sql1 = "INSERT INTO t_advertisements(hash, advertisement) VALUES (?,?)";
			String sql2 = "INSERT INTO t_advertisement_classes(advertisement_id, class_id) VALUES (?,?)";
			PreparedStatement stmt1 = null;
			PreparedStatement stmt2 = null;
			
			try{
				stmt1 = conn.prepareStatement(sql1);
				stmt1.setBytes(1, hash.getBytes("ISO-8859-1"));
				stmt1.setBytes(2, XML.toUnsignedXML(ad).getBytes("ISO-8859-1"));
				
				System.out.println(">> "+stmt1);
				stmt1.execute();
								
				ResultSet rs = stmt1.getGeneratedKeys();
				int id=0;
				
				if(rs.next())
					id=rs.getInt(1);
				
				stmt2 = conn.prepareStatement(sql2);
				
				for(String classs : adclass){
					if(!classes.containsKey(classs)){
						insertClass(classs);
					}
					
					
					
					stmt2.setInt(1, id);
					stmt2.setInt(2, classes.get(classs));
					
					System.out.println(">> "+stmt2);
					stmt2.execute();
				}
				
				
				
				advertisements.put(hash, ad.valid_until);
			}catch(Exception ex){ex.printStackTrace();}
			finally {
				try {
					stmt1.close();
					stmt2.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			System.out.println("ad already exists");
			if(advertisements.get(hash) < ad.valid_until){
				advertisements.put(hash, ad.valid_until);
			}
		}
	}
	
	public synchronized ArrayList<Advertisement> getAdvertisements(String className, int limit){
		ArrayList<Advertisement> ads = new ArrayList<Advertisement>();
		String sql="";
		PreparedStatement stmt1 = null;
		if(limit>0)
			sql = "SELECT ad.id, hash, advertisement FROM t_advertisements AS ad JOIN t_advertisement_classes ON ad.id=advertisement_id JOIN t_classes AS cla ON cla.id=class_id WHERE class = ? ORDER BY RAND() LIMIT ?";
		else
			sql = "SELECT ad.id, hash, advertisement FROM t_advertisements AS ad JOIN t_advertisement_classes ON ad.id=advertisement_id JOIN t_classes AS cla ON cla.id=class_id WHERE class = ? ORDER BY RAND()";
		try{
			stmt1 = conn.prepareStatement(sql);
			stmt1.setString(1, className);
			if(limit>0)
			stmt1.setInt(2, limit);
			
			System.out.println(stmt1);
			ResultSet rs = stmt1.executeQuery();
			
			while(rs.next()){
				Advertisement ad = (Advertisement) XML.parseUnsignedXML(new String(rs.getBytes(3),"ISO-8859-1"));
				ads.add(ad);
				System.out.println(ad);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try {
				stmt1.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ads;
	}
	
	private void insertClass(String classs){
		String sql1 = "INSERT INTO t_classes(class) VALUES(?)";
		PreparedStatement stmt = null;
		try{
			stmt = conn.prepareStatement(sql1);
			stmt.setString(1, classs);
			
			
			System.out.println(">> "+stmt);
			stmt.execute();
			
			ResultSet rs = stmt.getGeneratedKeys();
			int id=0;
			
			if(rs.next())
				id=rs.getInt(1);
			
			classes.put(classs, id);
			
			
		}catch(Exception ex){ex.printStackTrace();}
		finally{
			try {
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void cleanUp(){
		//TODO delete old stuff
	}
	

}
