package de.ps15.campusquiz;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class EmailEndingValidator. TODO: bind it to restful register methdode..
 */
public class EmailEndingValidator {
	
	// JDBC driver name and database URL
	/** The Constant JDBC_DRIVER. */
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    
    /** The Constant DB_URL. */
    static final String DB_URL = "jdbc:mysql://ps15server.cloudapp.net:3306/test_users";
   
//  Database credentials
   /** The Constant USER. */
static final String USER = "root";
   
   /** The Constant PASS. */
   static final String PASS = "Integration15";
   
	/** The pattern. */
	private static Pattern pattern;
	
	/** The matcher. */
	private static Matcher matcher;
   
   /** The endungen. */
   static String[] endungen;
   
   /** The endungen_pos. */
   static int endungen_pos;
   
   /**
    * Instantiates a new email ending validator.
    */
   public EmailEndingValidator(){
	   System.out.println("hello, this is emailendingvalidator");
	   try {
		endungen = getEndings();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }

	/**
	 * Gets the endings.
	 *
	 * @return the endings
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	public static String[] getEndings() throws ClassNotFoundException, SQLException{
		String sql = "SELECT endung FROM  email_whitelist";
		final String DB_URL = "jdbc:mysql://ps15server.cloudapp.net:3306/ps15_prod"; 
		final String USER = "root";
	    final String PASS = "Integration15";
	    
	    Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;  


		      //Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");
		      //Open connection
		      System.out.print("Connecting to database...getEndings...");
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);     
		      stmt = conn.createStatement();   
		      
		      PreparedStatement ps = conn.prepareStatement(sql);
		      rs = ps.executeQuery(sql);
		      
		      int size= 0;
		      if (rs != null)   
		      {  
		        rs.beforeFirst();  
		        rs.last();  
		        size = rs.getRow();  
		      } 
		      endungen = new String[size];
		      rs.first();
		     
		      for(int i = 0; i < rs.getRow(); i++){
		    	  endungen[i]  = rs.getString("endung");
		    	  rs.next();
		      }
		      
/*		      while (rs.next()) {
		    	  System.out.println(rs.getRow());
		    	  System.out.println(rs.getString("endung"));
		      }*/
		      
		      stmt.close();
		      conn.close();
		 
		         if(stmt!=null)
		            stmt.close();
		      
		         if(conn!=null)
		            conn.close();
		         System.out.println("Done!\n\n");
		 return endungen;
	}
	
	/**
	 * Check ending.
	 *
	 * @param email the email
	 * @return true, if successful
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	public boolean checkEnding(String email) throws ClassNotFoundException, SQLException{
		 
		String endung_pattern = null;
		//System.out.println(email);
		//System.out.println("Laenge: "+endungen.length);
		
		
		
		for(int i = 0; i < endungen.length; i++){
			//System.out.println(endungen[i]);	
			endung_pattern = "(.*?)@(.*?)"+endungen[i];
			//System.out.println(email+" "+endung_pattern+"  "+endingValidator(endung_pattern,email));
			if(endingValidator(endung_pattern,email)){
				endungen_pos = i;
				System.out.println("endungen_pos: "+endungen_pos+" | endungen: "+endungen[i]);
				return true;
			}else{
				System.out.println("ENDUNG NICHT IN DATENBANK");
				endungen_pos = -1;
				
			}
		}
		return false;
	}
	
	
	/**
	 * Ending validator.
	 *
	 * @param ending_pattern the ending_pattern
	 * @param email the email
	 * @return true, if successful
	 */
	public static boolean endingValidator(String ending_pattern,String email) {
		pattern = Pattern.compile(ending_pattern);
		
		return validate(email);
	}
 
	/**
	 * Validate hex with regular expression.
	 *
	 * @param email            email for validation
	 * @return true valid email, false invalid email
	 */
	public static boolean validate(final String email) {
 
		matcher = pattern.matcher(email);
		return matcher.matches();
	}	
	
	 /**
 	 * Gets the uni id by email.
 	 *
 	 * @return the uni id by email
 	 * @throws ClassNotFoundException the class not found exception
 	 * @throws SQLException the SQL exception
 	 */
 	public int getUniIdByEmail() throws ClassNotFoundException, SQLException{
		 int id1 = -1;
		 if(endungen_pos >= 0){ 
			String sql = "SELECT unis.UNI_ID "
					+"FROM unis "
					+"JOIN email_whitelist "
					+"ON unis.UNI_ID = email_whitelist.UNI "
					+"WHERE email_whitelist.endung = ?";
			final String DB_URL = "jdbc:mysql://ps15server.cloudapp.net:3306/ps15_prod"; 
			final String USER = "root";
		    final String PASS = "Integration15";
		    
		    Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;  


			      //Register JDBC driver
			      Class.forName("com.mysql.jdbc.Driver");
			      //Open connection
			      //System.out.println("Connecting to database...getUniID...");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);     
			      stmt = conn.createStatement();  
			      
			      System.out.println("\nGesuchte Endung: 	"+endungen[endungen_pos]);
			      String a = endungen[endungen_pos];
			      //sql = sql + "'"+ a+"'";
			      PreparedStatement ps = conn.prepareStatement(sql);
			      
			      //System.out.println(a.toLowerCase()+"\n"+ps.toString());
			      
			      ps.setString(1, a.toLowerCase()); 
			      //ps.setString(1, "uni-wuerzburg.de"); 
			      //System.out.println(rs.getStatement().toString());
			      //System.out.println("FIRE!");
			      rs = ps.executeQuery();
			      
			      int size= 0;
			      if (rs != null)   
			      {  
			        rs.beforeFirst();  
			        rs.last();  
			        size = rs.getRow();  
			      } 
			      
			      rs.first();
			      
			      if(size>=0){
				       id1 = rs.getInt(1);
			      }
			      
			      stmt.close();
			      conn.close();
			 
			         if(stmt!=null)
			            stmt.close();
			      
			         if(conn!=null)
			            conn.close();
		 }else{
			 //UNI NICHT VORHANDEN
			 }
		 
			 return id1;
		}
	
}
