package com.qingxuan;

/**
 * Created by qingxuan on 8/9/17.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public Boolean isRecordExist(String prodId) throws SQLException {
        String sql_string = "select category from " + d_db_name + ".ProdInfo where prod_id = '" + prodId + "'";
        System.out.println(sql_string);
        PreparedStatement existStatement = null;
        boolean isExist = false;

        try{
            existStatement = d_connect.prepareStatement(sql_string);
            ResultSet result_set = existStatement.executeQuery();
            if (result_set.next()){
                isExist = true;
            }
        }catch(SQLException e ){
            return false;
            //System.out.println(e.getMessage());
            //throw e;
        }finally{
            if(existStatement != null){
                existStatement.close();
            }
        }

        return isExist;
    }

    public void addProdData(Prod prod) throws Exception{
        PreparedStatement prod_info = null;

        String sql_string = "insert into " + d_db_name +".ProdInfo values(?,?,?,?,?,?,?)";
        System.out.println(sql_string);
        try {
            prod_info = d_connect.prepareStatement(sql_string);
            prod_info.setString(1, prod.prodURL);
            prod_info.setString(2, prod.prodTitle);
            prod_info.setDouble(3, prod.lastPrice);
            prod_info.setDouble(4, prod.curPrice);
            prod_info.setString(5, prod.category);
            prod_info.setString(6, prod.prodId);
            prod_info.setDouble(7, (prod.curPrice-prod.lastPrice)/Math.abs(prod.lastPrice));
            prod_info.executeUpdate();

        }catch(SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }finally {
            if(prod_info != null) {
                prod_info.close();
            }
        }
    }

    public void updateProdData(Prod prod) throws Exception{
        PreparedStatement updateStatement_last = null;
        PreparedStatement updateStatement_cur = null;
        PreparedStatement updateStatement_deal = null;
        String sql_string_last = "update " + d_db_name + ".ProdInfo set last_price=" + prod.lastPrice + " where prod_id ='" + prod.prodId + "'";
        String sql_string_cur = "update " + d_db_name + ".ProdInfo set cur_price=" + prod.curPrice + " where prod_id ='" + prod.prodId + "'";
        String sql_string_deal = "update " + d_db_name + ".ProdInfo set deal_percent=" + (prod.curPrice-prod.lastPrice)/Math.abs(prod.lastPrice) + " where prod_id ='" + prod.prodId + "'";
        System.out.println(sql_string_last);
        System.out.println(sql_string_cur);
        System.out.println(sql_string_deal);

        try {
            updateStatement_last = d_connect.prepareStatement(sql_string_last);
            updateStatement_cur = d_connect.prepareStatement(sql_string_cur);
            updateStatement_deal = d_connect.prepareStatement(sql_string_deal);
            updateStatement_last.executeUpdate();
            updateStatement_cur.executeUpdate();
            updateStatement_deal.executeUpdate();
        }catch(SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }finally {
            if(updateStatement_last!=null) {
                updateStatement_last.close();
            }
            if(updateStatement_cur!=null) {
                updateStatement_cur.close();
            }
        }

    }



}

