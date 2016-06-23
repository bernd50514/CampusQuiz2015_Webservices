package de.ps15.campusquiz;

import java.security.NoSuchAlgorithmException;


import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

 
public class DBConnectionTest {
    /**
     * Method to create DB Connection
     * 
     * @return
     * @throws Exception
     */
    public static EmailValidator emailval = new EmailValidator(); // initiate Emailvalidator to check if user is an email oder an username

    
    @SuppressWarnings("finally")
    public static Connection createConnection() throws Exception {
        Connection con = null;
        try {
            Class.forName(Constants.dbClass);
            con = DriverManager.getConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPwd);
        } catch (Exception e) {
            throw e;
        } finally {
            return con;
        }
    }
    /**
     * Method to check whether email and password combination are correct
     * 
     * @param email
     * @param password
     * @return
     * @throws NoSuchAlgorithmException InvalidKeySpecException Exception
     */
    public static boolean checkLogin(String emailorUsername, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, Exception {
        boolean isUserAvailable = false;
        PasswordEncryptionService ps = new PasswordEncryptionService();
        Connection dbConn = null;
     
        boolean userIsEmail = emailval.validate(emailorUsername);
        try {
            try {
                dbConn = DBConnection.createConnection();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            
            PreparedStatement pstm_email = dbConn.prepareStatement("SELECT email, pwsalt, pwhash FROM user WHERE (email = ?)");
            PreparedStatement pstm_user = dbConn.prepareStatement("SELECT email, pwsalt, pwhash FROM user WHERE (username = ?)");
            //TODO: implement if variable = email... else of variable = username...
          // user use email to login
            if (userIsEmail == true){ 
            pstm_email.setString(1, emailorUsername); // use email sql
            ResultSet rs = pstm_email.executeQuery();
            
            while (rs.next()) {
            	
              //System.out.println(rs.getString(1) + rs.getString(2) + rs.getString(3));
            	byte[] salt = rs.getBytes("pwsalt"); // retrieve salt stored by first registering
            	byte[] existedHashPassword = rs.getBytes("pwhash"); // retrieve hashing password stored by first registering
            	byte[] encryptedAttemptedPassword = ps.getHashPassword( // compared the input password with the hashing password in database
        				password, salt);
            	
            if (Arrays.equals(existedHashPassword, encryptedAttemptedPassword)){;
            	
                isUserAvailable = true;
            }
           
            	} 
            } 
            		
            else // not a email, user use username ot login 
            {
            	pstm_user.setString(1, emailorUsername); // use user sql
            	ResultSet rs = pstm_user.executeQuery();
            	  while (rs.next()) {
                  	
                      //System.out.println(rs.getString(1) + rs.getString(2) + rs.getString(3));
                    	byte[] salt = rs.getBytes("pwsalt"); // retrieve salt stored by first registering
                    	byte[] existedHashPassword = rs.getBytes("pwhash"); // retrieve hashing password stored by first registering
                    	byte[] encryptedAttemptedPassword = ps.getHashPassword( // compared the input password with the hashing password in database
                				password, salt);
                    	
                    if (Arrays.equals(existedHashPassword, encryptedAttemptedPassword)){;
                    	
                        isUserAvailable = true;
                    }
                   
                    
                    	} 
            	
            }
            
            
        } catch (SQLException sqle) {
        	System.out.println(sqle.toString());
            throw sqle;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (dbConn != null) {
                dbConn.close();
            }
            throw e;
        } finally {
            if (dbConn != null) {
                dbConn.close();
            }
        }
        return isUserAvailable;
    }
    /**
     * Method to insert username, password, email, name, vorname, studiengangID, uniID in DB
     * 
     * @param name
     * @param username
     * @param password
     * @param email
     * @param name
     * @vorname vorname
     * @studiengangID studiengangID
     * @uniID uniID
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public static boolean insertUser(String username, String email, String password, String name, String vorname, int studiengangID, int uniID) throws SQLException, Exception {
    	//username = username.toLowerCase();
    	//email = email.toLowerCase();
    	 	
    	PasswordEncryptionService pw = new PasswordEncryptionService();
    	byte[]salt = pw.generateSalt();
    	byte[]passwordhash=pw.getHashPassword(password, salt);
    	boolean insertStatus = false;
    	// START TEST OF INITIATION EMAIL ACTIVATION 
    	String activeCode = UUIDUtils.generatedActiveCode();
    	long now = System.currentTimeMillis();
		long expires = now + 1000 * 60 * 60 * 24 *7; // we can change expires date here...
		// datetime yyyy-MM-dd hh:mm:ss
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String expiresActiveDate = dateFormat.format(new Date(expires));
	
		// END OF INITIATION OF EMAIL AVTIVATION
		
        Connection dbConn = null;
        try {
            try {
                dbConn = DBConnection.createConnection();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Statement stmt = dbConn.createStatement();
            PreparedStatement ps = null;
          
    
		//    String sql = "INSERT INTO user (username, email, name, vorname, studiengang_id, uni_id, pwhash, pwsalt, active) VALUES (?,?,?,?,?,?,?,?,?)";
            String sql = "INSERT INTO user (username, email, name, vorname, studiengang_id, uni_id, pwhash, pwsalt, active, last_login, failed_logins,activation_code, expiresActionDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            ps = dbConn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
		    ps.setString(3, name);
		    ps.setString(4, vorname);
		    ps.setInt(5, studiengangID);
		    ps.setInt(6, uniID);
		    ps.setBytes(7, passwordhash);
		    ps.setBytes(8, salt);
		    ps.setInt(9, 0);
		    ps.setInt(10, 0);
		    ps.setInt(11, 0);
		    ps.setString(12, activeCode);
		    ps.setString(13, expiresActiveDate);
		    
           
            int records = ps.executeUpdate();
            System.out.println(records);
            //When record is successfully inserted
            if (records>0){
            insertStatus = true;
            
            }
            new Thread(new SendMail(username,email,activeCode,expiresActiveDate)).start(); // send email in background, no pain.                   
            stmt.close();
          
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw sqle;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Auto-generated catch block
            if (dbConn != null) {
                dbConn.close();
            }
            throw e;
        } finally {
            if (dbConn != null) {
                dbConn.close();
            }
        }
        return insertStatus;
    }
    
  
    public static boolean checkActivationStatus(String emailorUsername) throws SQLException{
    	 boolean isUserActivated = false;    
         Connection dbConn = null;
         
         
         try {
             try {
                 dbConn = DBConnection.createConnection();
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             boolean userIsEmail = emailval.validate(emailorUsername);	
             System.out.println("user use email to login? "+userIsEmail);
             PreparedStatement pstm_username = dbConn.prepareStatement("SELECT active FROM user WHERE (username = ?)");
             PreparedStatement pstm_email = dbConn.prepareStatement("SELECT active FROM user WHERE (email = ?)");
             
             if (userIsEmail==true){
            	 //boolean userIstEmail = emailval.validate(emailorUsername);	
                 System.out.println("user use email to login? "+userIsEmail);
             // user use email to login
             pstm_email.setString(1, emailorUsername); // sql use email
             ResultSet rs = pstm_email.executeQuery();
             System.out.println("sql user email to send request");
             while (rs.next()) {
             	int active = rs.getInt("active");
             	
             	if (active == 0){
             		isUserActivated = false;
             	}
             	else {
             		isUserActivated = true;
             	}
             
             	}
             } 
             else { // user use username to log in 
            	 pstm_username.setString(1, emailorUsername); // sql use username
                 ResultSet rs = pstm_username.executeQuery();
                 System.out.println("sql user username to send request");
                 while (rs.next()) {
                 	int active = rs.getInt("active");
                 	
                 	if (active == 0){
                 		isUserActivated = false;
                 	}
                 	else {
                 		isUserActivated = true;
                 	}
                 
                 	}
             }
             
         } catch (SQLException sqle) {
         	System.out.println(sqle.toString());
             throw sqle;
         } catch (Exception e) {
             // TODO Auto-generated catch block
             if (dbConn != null) {
                 dbConn.close();
             }
             throw e;
         } finally {
             if (dbConn != null) {
                 dbConn.close();
             }
         }
         return isUserActivated;
     }
    
    //check if user in the database.
    
    //TODO: make that simple!
    public static boolean checkUserInDB(String emailorUsername) throws Exception{
    	int usersindb = 0;
    	String sql_email = "SELECT COUNT(*) FROM  user WHERE email = ?";
    	String sql_username = "SELECT COUNT(*) FROM  user WHERE username = ?";
    	
		    
		    Connection dbConn = null;
			Statement stmt = null;
			ResultSet rs = null;  

			   
			dbConn = DBConnection.createConnection();
			stmt = dbConn.createStatement();   
			boolean userIsEmail = emailval.validate(emailorUsername);
			System.out.println("User uses Email to login?"+userIsEmail);
			
				if (userIsEmail == true){// User use email to login
			      PreparedStatement ps = dbConn.prepareStatement(sql_email);
			      ps.setString(1, emailorUsername);
			      rs = ps.executeQuery();
			      System.out.println("Rs size: "+rs);
			      
			      int size= 0;
			      if (rs != null)   
			      {  
			        rs.beforeFirst();  
			        rs.last();  
			        size = rs.getRow();  
			      } 
			      rs.first();
			      
			      if(size>=0){
			    	  usersindb = rs.getInt(1);
			      }
			      
			      stmt.close();
			      dbConn.close();
			 
			         if(stmt!=null)
			            stmt.close();
			      
			         if(dbConn!=null)
			            dbConn.close();
			        
			         if(usersindb==0){
			        	 return false;
			         }else{
			        	 return true;
			         }
				}else {  // user uses username to login  
				  PreparedStatement ps = dbConn.prepareStatement(sql_username);
			      ps.setString(1, emailorUsername);
			      rs = ps.executeQuery();
			      System.out.println("Rs size: "+rs);
			      
			      int size= 0;
			      if (rs != null)   
			      {  
			        rs.beforeFirst();  
			        rs.last();  
			        size = rs.getRow();  
			      } 
			      rs.first();
			      
			      if(size>=0){
			    	  usersindb = rs.getInt(1);
			      }
			      
			      stmt.close();
			      dbConn.close();
			 
			         if(stmt!=null)
			            stmt.close();
			      
			         if(dbConn!=null)
			            dbConn.close();
			        
			         if(usersindb==0){
			        	 return false;
			         }else{
			        	 return true;
			         }
					
				}
			         
			         
	 }
    
    
    /**
   	 * Method to retreive Uni String, which will be used to store in the user login session
   	 * @Author: Bernd
   	 * @since 28.06.2015
   	 * @param uni_id
   	 * @return string uni -- Kurzname
   	 * @throws Exception 
   	 */
   	
   	public static String getUniName(String username) throws SQLException{
   		boolean userIsEmail = emailval.validate(username);
   			String UniName = "";
   			String sql_getUniname = "SELECT unis.KURZNAME FROM unis INNER JOIN user ON unis.UNI_ID = user.uni_id WHERE (username = ?) ";
   	        String sql_getUniname_byEmail = "SELECT unis.KURZNAME FROM unis INNER JOIN user ON unis.UNI_ID = user.uni_id WHERE (email = ?)";
   			Connection dbConn = null;
   	        try {
   	            try {
   	                dbConn = DBConnection.createConnection();
   	            } catch (Exception e) {
   	                // TODO Auto-generated catch block
   	                e.printStackTrace();
   	            }

   	            System.out.println("Test");
   	            if (userIsEmail == false){// user uses username to login
   	            PreparedStatement pstm = dbConn.prepareStatement(sql_getUniname);
   	           
   	            pstm.setString(1, username);
   	            ResultSet rs = pstm.executeQuery();
   	            System.out.println("rs record: "+rs.getFetchSize());
   	            while (rs.next()) {
   	            	UniName = rs.getString("KURZNAME");
   	            	}
   	            }else { // user uses email to login
   	             PreparedStatement pstm = dbConn.prepareStatement(sql_getUniname_byEmail);
     	           
    	            pstm.setString(1, username);
    	            ResultSet rs = pstm.executeQuery();
    	            System.out.println("rs record: "+rs.getFetchSize());
    	            while (rs.next()) {
    	            	UniName = rs.getString("KURZNAME");
    	            	}
   	            }
   	            
   	        } catch (SQLException sqle) {
   	        	System.out.println(sqle.toString());
   	            throw sqle;
   	        } catch (Exception e) {
   	            // TODO Auto-generated catch block
   	            if (dbConn != null) {
   	                dbConn.close();
   	            }
   	            throw e;
   	        } finally {
   	            if (dbConn != null) {
   	                dbConn.close();
   	            }
   	        }
   	        return UniName;
   	
   }
   	/**
   	 * Method to retreive Studiengang String, which will be used to store in the user login session
   	 * @Author: Bernd
   	 * @since 28.06.2015
   	 * @param studiengangID
   	 * @return string uni -- Kurzname
   	 * @throws Exception 
   	 */
   	public static String getStudiengangName(String username) throws SQLException{
   			boolean userIsEmail = emailval.validate(username);
   			String studiengangName = "";
   			String sql_studiengang = "SELECT studiengaenge.Kurzname FROM studiengaenge INNER JOIN user ON studiengaenge.Studiengang_ID = user.studiengang_id WHERE (username = ?) ";
   	        String sql_studiengang_email = "SELECT studiengaenge.Kurzname FROM studiengaenge INNER JOIN user ON studiengaenge.Studiengang_ID = user.studiengang_id WHERE (email = ?)";
   			Connection dbConn = null;
   	        try {
   	            try {
   	                dbConn = DBConnection.createConnection();
   	            } catch (Exception e) {
   	                // TODO Auto-generated catch block
   	                e.printStackTrace();
   	            }

   	            System.out.println("Test");
   	          //  PreparedStatement pstm = dbConn.prepareStatement("SELECT Kurzname, Studiengang_ID FROM studiengaenge WHERE (Studiengang_ID = ?)");
   	            if(userIsEmail == false){// user uses username to login
   	            PreparedStatement pstm = dbConn.prepareStatement(sql_studiengang);
   	           // pstm.setInt(1, studiengangID);
   	            pstm.setString(1, username);
   	            ResultSet rs = pstm.executeQuery();
   	            
   	            while (rs.next()) {
   	            	studiengangName = rs.getString("Kurzname");
   	            	}
   	            }else {
   	             PreparedStatement pstm = dbConn.prepareStatement(sql_studiengang_email);
     	           // pstm.setInt(1, studiengangID);
     	            pstm.setString(1, username);
     	            ResultSet rs = pstm.executeQuery();
     	            
     	            while (rs.next()) {
     	            	studiengangName = rs.getString("Kurzname");
     	            	}
   	            	
   	            }
   	            
   	        } catch (SQLException sqle) {
   	        	System.out.println(sqle.toString());
   	            throw sqle;
   	        } catch (Exception e) {
   	            // TODO Auto-generated catch block
   	            if (dbConn != null) {
   	                dbConn.close();
   	            }
   	            throw e;
   	        } finally {
   	            if (dbConn != null) {
   	                dbConn.close();
   	            }
   	        }
   	        return studiengangName;
   	
   }
    
   	
   	public static String getUsernameOrEmail(String usernameOrEmail) throws Exception{
   		
   		String Userinfo = "";
   		
   		
   		String sql_email = "SELECT username FROM  user WHERE email = ?";
    	String sql_username = "SELECT email FROM  user WHERE username = ?";
    	
		    
		    Connection dbConn = null;
			Statement stmt = null;
			ResultSet rs = null;  

			   
			dbConn = DBConnection.createConnection();
			stmt = dbConn.createStatement();   
			boolean userIsEmail = emailval.validate(usernameOrEmail);
			System.out.println("User uses Email to login?"+userIsEmail);
			
				if (userIsEmail == true){
					PreparedStatement ps = dbConn.prepareStatement(sql_email);
				      ps.setString(1, usernameOrEmail);
				      rs = ps.executeQuery();
				      
				      while(rs.next()){
				    	  Userinfo = rs.getString("username");
				      }
					
				}else
					{
				  PreparedStatement ps = dbConn.prepareStatement(sql_username);
			      ps.setString(1, usernameOrEmail);
			      rs = ps.executeQuery();
			      
			      while(rs.next()){
			    	  Userinfo = rs.getString("email");
			      }
					
				}
   		
				stmt.close();
			      dbConn.close();
			 
			         if(stmt!=null)
			            stmt.close();
			      
			         if(dbConn!=null)
			            dbConn.close();
   		
   	
   		
   		return Userinfo;
			
				
   		
   		
   	}
    
    
    
    
}
    









    
    
    
