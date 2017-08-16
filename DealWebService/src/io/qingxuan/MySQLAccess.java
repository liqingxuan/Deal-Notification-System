package io.qingxuan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MySQLAccess {
	
	private Connection d_connect = null;
	private String d_user_name;
	private String d_password;
	private String d_server_name;
	private String d_db_name;
	
    public void close() throws Exception {
        System.out.println("Close database");
          try {
            if (d_connect != null) {
                d_connect.close();
            }
          } catch (Exception e) {
              throw e;
          }
    }
    
    public MySQLAccess(String server, String db, String user, String pass) throws Exception{
		d_user_name = user;
		d_password = pass;
		d_server_name = server;
		d_db_name = db;
		
		try {
	     	   // This will load the MySQL driver, each DB has its own driver
	            Class.forName("com.mysql.jdbc.Driver");
	            //"jdbc:mysql://127.0.0.1:3306/searchads?user=root&password=bittiger2017"
	            String conn = "jdbc:mysql://" + d_server_name + "/" + 
	                    d_db_name+"?user="+d_user_name+"&password="+d_password;
	            System.out.println("Connecting to database: " + conn);
	            d_connect = DriverManager.getConnection(conn);
	            System.out.println("Connected to database");
	       } catch(Exception e) {
	            throw e;
	       }     	
	}
    
    
    public List<Prod> getDealData(String category, int k) throws Exception{ // change to return List<Prod> eventually
    		PreparedStatement selStatement = null;
    		Prod prod = new Prod();
    		ResultSet result_set = null;
    		List<Prod> prodList = new ArrayList<>();
    		
    		//SELECT amount FROM mytable ORDER BY amount DESC LIMIT 5
    		String sql_string = "select * from " + d_db_name + ".ProdInfo having category = '" + category + "' order by deal_percent ASC LIMIT " + k; 
    		System.out.println(sql_string);
    		try {
    			selStatement = d_connect.prepareStatement(sql_string);
    			result_set = selStatement.executeQuery();
    			System.out.println(result_set);
    			while(result_set.next()) {
    				prod = new Prod();
    				prod.category = category;
    				prod.curPrice = result_set.getDouble("cur_price");
    				prod.deal = result_set.getDouble("deal_percent");
    				prod.prodTitle = result_set.getString("prod_title");
    				prod.prodURL = result_set.getString("prod_URL");
    				prodList.add(prod);
    				//System.out.println(prod.prodTitle);
    			}
    			
    		}catch(SQLException e) {
    			System.out.println(e.getMessage());
    			throw e;
    		}finally {
    			if(selStatement != null) {
    				selStatement.close();
    			}
    			if(result_set != null) {
    				result_set.close();
    			}
    		}
    		return prodList;
    
    }
    
   
}
