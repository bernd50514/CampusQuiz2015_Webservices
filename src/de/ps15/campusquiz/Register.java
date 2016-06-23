package de.ps15.campusquiz;


import java.sql.SQLException;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

//Path: http://localhost/<appln-folder-name>/register
@Path("/register")
public class Register {
	// HTTP Get Method
	@GET 
	// Path: http://localhost/<appln-folder-name>/register/doregister
	@Path("/doregister")  
	// Produces JSON as response
	@Produces(MediaType.APPLICATION_JSON) 
	// Query parameters are parameters: http://locnamealhost/<appln-folder-name>/register/doregister?name=pqrs&username=abc&password=xyz.....
	
	public String doRegister(@QueryParam("username") String username, @QueryParam("email") String email, @QueryParam("password") String password,@QueryParam("name")String name,@QueryParam("vorname")String vorname,@QueryParam("studiengangID") int studiengangID){
		String response = "";
		//System.out.println("Inside doLogin "+uname+"  "+pwd);
		int retCode = registerUser(username, email, password, name, vorname, studiengangID);
		if(retCode == 0){
		//	response = Utility.constructJSON("register",true);
			//response = Utility.constructJSON("register",true);
			response = Utility.constructJSON("register", true, username, email);
		}else if(retCode == 1){
			response = Utility.constructJSON("register",false, "You are already registered: duplicate username or unimail");
		}else if(retCode == 2){
			response = Utility.constructJSON("register",false, "Special Characters are not allowed in Username and Password");
		}else if(retCode == 3){
			response = Utility.constructJSON("register",false, "Error occured");
		}else if(retCode == 4){
			response = Utility.constructJSON("register",false, "Incorrect unimail");
		}
		return response;
	}
	
	private int registerUser(String username, String email, String password, String name, String vorname, int studiengangID){
		System.out.println("Inside checkCredentials");
		int result = 3;
		//Test for Email validator
		EmailEndingValidator eval = new EmailEndingValidator();
		int uniID = 0;
		
		//TODO: build up studiengangID and UniID
		if(Utility.isNotNull(username) && Utility.isNotNull(password)&& Utility.isNotNull(Integer.toString(studiengangID))&& Utility.isNotNull(Integer.toString(uniID))){
			try{
				
				if (eval.checkEnding(email)){
					uniID = eval.getUniIdByEmail();			
				
				
				if(DBConnectionTest.insertUser(username, email, password, name, vorname, studiengangID, uniID)){
					//TODO: implement a method which can automatically identify the university of users.
					System.out.println("RegisterUSer if");
					result = 0;
				}
				
				} else {
					result = 4; // TODO: Implement Email Domain Exception
				}
				
			} catch(SQLException sqle){
				System.out.println("RegisterUSer catch sqle");
				//When Primary key violation occurs that means user is already registered
				if(sqle.getErrorCode() == 1062){
					result = 1;
				} 
				//When special characters are used in name,username or password
				else if(sqle.getErrorCode() == 1064){
					System.out.println(sqle.getErrorCode());
					result = 2;
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Inside checkCredentials catch e ");
				System.out.println(e.toString());
				result = 3;
			}
		}else{
			System.out.println("Inside checkCredentials else");
			result = 3;
		}
			
		return result;
	}
	
}

