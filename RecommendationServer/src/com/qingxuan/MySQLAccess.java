package com.qingxuan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Created by qingxuan on 8/10/17.
 */
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


    public List<Long> getExpiredSubscribers() throws Exception{
        List<Long> userList = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        //SELECT user_id FROM PriceDetection.UserInfo where ADDTIME(last_send, sub_freq) <= now()
        String sql_string = "select user_id FROM " + d_db_name + ".UserInfo where ADDTIME(last_send, sub_freq) <= now()";
        System.out.println(sql_string);
        try{
            statement = d_connect.prepareStatement(sql_string);
            resultSet = statement.executeQuery();
            //System.out.println(resultSet);
            while(resultSet.next()){
                userList.add(resultSet.getLong("user_id"));
                //System.out.println(userList.size()-1);
            }
        }catch(SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }finally {
            if(statement != null) {
                statement.close();
            }
            if(resultSet != null) {
                resultSet.close();
            }
        }
        System.out.println(userList.toString());
        return userList;
    }

    public User getSubscriptionInfo(Long id) throws Exception{
        User subInfo = new User();
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql_string = "select * from " + d_db_name + ".UserInfo where user_id = " + id;
        System.out.println(sql_string);
        try{
            statement = d_connect.prepareStatement(sql_string);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                subInfo.email = resultSet.getString("email");
                subInfo.userId = id;
                subInfo.subs = new HashMap<>();

                subInfo.subs.put("Electronics", resultSet.getBoolean("Electronics"));
                subInfo.subs.put("Beauty", resultSet.getBoolean("Beauty"));
                subInfo.subs.put("Kids", resultSet.getBoolean("Kids"));
                subInfo.subs.put("Home", resultSet.getBoolean("Home"));
                subInfo.subs.put("Outdoors", resultSet.getBoolean("Outdoors"));

            }

        }catch(SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }finally {
            if(statement != null) {
                statement.close();
            }
            if(resultSet != null) {
                resultSet.close();
            }
        }
        return subInfo;
    }

    public void updateTimeStamp(Long userId) throws Exception{
        PreparedStatement statement = null;
        String sql_string = "update " + d_db_name + ".UserInfo set last_send = now() where user_id = " + userId;
        System.out.println(sql_string);
        try{
            statement = d_connect.prepareStatement(sql_string);
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
            throw e;
        }finally{
            if(statement != null){
                statement.close();
            }
        }
    }
}
