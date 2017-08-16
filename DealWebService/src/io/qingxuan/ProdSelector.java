package io.qingxuan;

import java.util.ArrayList;
import java.util.List;

public class ProdSelector {

	private String mysql_host;
	private String mysql_db;
	private String mysql_user;
	private String mysql_pass;

	private MySQLAccess mysql;
	
	public ProdSelector(String mysqlHost,String mysqlDb,String user,String pass){

		mysql_host = mysqlHost;
		mysql_db = mysqlDb;	
		mysql_user = user;
		mysql_pass = pass;	
		//indexBuilder = new IndexBuilder(memcachedServer,memcachedPortal,mysql_host,mysql_db,mysql_user,mysql_pass);
		
		try {
			mysql = new MySQLAccess(mysql_host, mysql_db, mysql_user, mysql_pass);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	}
	
	public void init() {
		
	}
	
	public List<Prod> getDeals(String category, int k){
		List<Prod> list = new ArrayList<>();
		try {
			list = mysql.getDealData(category, k);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
