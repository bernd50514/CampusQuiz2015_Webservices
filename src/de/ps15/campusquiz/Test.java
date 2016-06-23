package de.ps15.campusquiz;

import java.sql.SQLException;

public class Test {
	public static EmailValidator emailval = new EmailValidator();
	
	public static DBConnectionTest dbTest = new DBConnectionTest();
	
	
	public static void main (String []arg) throws Exception{
		
		String email = "test@stud-mail.uni-wuerzbuasdasdrg.de";
		String email_new = "sniper37@163.com";
		String name = "jihahao";
		
		boolean test = emailval.validate(email);
		boolean test1 = emailval.validate(email);
		boolean test2 = emailval.validate(name);
		
		// Test User in DB
		String TestRetrive = DBConnectionTest.getUniName("Flowi");
		String TestRetrieve_studiengang = DBConnectionTest.getStudiengangName("Flowi"); 
		String TestEmailOrUsername = DBConnectionTest.getUsernameOrEmail("florian.krauter@stud-mail.uni-wuerzburg.de");
		boolean test3 = DBConnectionTest.checkUserInDB(email);
		boolean test4 = DBConnectionTest.checkUserInDB(name);
		
		System.out.println(test);
		System.out.println(test1);
		System.out.println(test2);
		System.out.println(test3);
		System.out.println(test4);
		System.out.println(TestRetrive);
		System.out.println(TestRetrieve_studiengang);
		System.out.println(TestEmailOrUsername);
		
		
	}

}
