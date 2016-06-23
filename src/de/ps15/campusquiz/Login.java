package de.ps15.campusquiz;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

//Path: http://localhost/<appln-folder-name>/login
@Path("/login")
public class Login {
	// HTTP Get Method
	@GET
	// Path: http://localhost/<appln-folder-name>/login/dologin
	@Path("/dologin")
	// Produces JSON as response
	@Produces(MediaType.APPLICATION_JSON)
	// Query parameters are parameters:
	// http://localhost/<appln-folder-name>/login/dologin?username=abc&password=xyz
	public String doLogin(@QueryParam("emailOrUsername") String emailOrUsername,
			@QueryParam("password") String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		String response = "";
		EmailValidator emailval = new EmailValidator();
		boolean userIsEmail = emailval.validate(emailOrUsername);
//		if (checkCredentials(email, password)) {
//			response = Utility.constructJSON("login", true);
//		} else {
//			response = Utility.constructJSON("login", false,
//					"Incorrect Email or Password");
//		}
//		return response;
		int retCode = checkLogin(emailOrUsername,password);
		if(retCode == 0){
			
			if (userIsEmail == false){ // user uses username to login
				
				try {
					String UniName = DBConnectionTest.getUniName(emailOrUsername);
					String StudiengangName= DBConnectionTest.getStudiengangName(emailOrUsername);
					String getEmailFromUser = DBConnectionTest.getUsernameOrEmail(emailOrUsername);
					response = Utility.constructJSON("login", true, emailOrUsername, getEmailFromUser);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			//response = Utility.constructJSON("login",true,emailOrUsername);
			
			}
			else { // user uses email to login
				
				try {
					String getUserFromEmail = DBConnectionTest.getUsernameOrEmail(emailOrUsername);
					response = Utility.constructJSON("login", true, getUserFromEmail, emailOrUsername);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}else if(retCode == 1){
			response = Utility.constructJSON("login",false, "account has not activated, please activate your account first.");
		}else if(retCode == 2){
			response = Utility.constructJSON("login",false, "Incorrect Unimail or Password");
		}else if(retCode == 3){
			response = Utility.constructJSON("login",false, "Error occured");
		}else if(retCode == 4){
			response = Utility.constructJSON("login",false, "Please Insert Your Unimail/Password and Password");
		}else if(retCode == 5){
			response = Utility.constructJSON("login",false, "User not in Database");
		}
		return response;
		
	}
	

	
	
	/**
	 * Method to check whether the entered credential is valid
	 * 
	 * @param email
	 * @param pwd
	 * @return int
	 * */
	public int checkLogin(String emailOrUsername, String password) {
		int result = 0; // ok
		if (Utility.isNotNull(emailOrUsername) && Utility.isNotNull(password)) {

			try {
				 // TODO: implement user is in database.
				//System.out.println("test Check USERIN DB:"+DBConnectionTest.checkUserInDB(email));
				if (DBConnectionTest.checkUserInDB(emailOrUsername)==true){
					System.out.println("USER IN DB?"+DBConnectionTest.checkUserInDB(emailOrUsername));
				if (DBConnectionTest.checkActivationStatus(emailOrUsername) == false) {
					result = 1; // user is not active
				} else {

					if (DBConnectionTest.checkLogin(emailOrUsername, password) == true) {
						result = 0; // successful login status
					} else {
						result = 2; // use has typed in false password
					}
				}
			} else {
						result = 5;
			}
				
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = 3;
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = 3;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = 3;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = 3;
			}
		} else {
			result = 4;
		}
		
	
		return result;
		
	}
}
