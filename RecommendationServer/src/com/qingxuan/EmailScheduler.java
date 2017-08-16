package com.qingxuan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by qingxuan on 8/10/17.
 */
public class EmailScheduler implements Runnable{

    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    private MySQLAccess mysql;

    private int k = 5;
    // key: category. Value: priorityQueue
    private HashMap<String, PriorityQueue<Prod>> topk = new HashMap<>();


    public EmailScheduler(String mysqlHost,String mysqlDb,String user,String pass){

        mysql_host = mysqlHost;
        mysql_db = mysqlDb;
        mysql_user = user;
        mysql_pass = pass;
        try {
            mysql = new MySQLAccess(mysql_host, mysql_db, mysql_user, mysql_pass);
        }catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    public void update(Prod prod){

        if(!topk.containsKey(prod.category)){
            topk.put(prod.category, new PriorityQueue<Prod>(new Comparator<Prod>() {
                @Override
                public int compare(Prod prod1, Prod prod2) {
                    return (int)(prod1.deal - prod2.deal);
                }
            }));
            //topk.get(prod.category).add(prod);
        }

        PriorityQueue<Prod> pq = topk.get(prod.category);
        if(pq.size() < k){
            pq.add(prod);
        }else{
            if(prod.deal > pq.peek().deal){
                pq.poll();
                pq.offer(prod);
            }
        }
    }

    @Override
    public void run() {
        System.out.println("run called");



        //send each category's top k to customer.
        /*
        for each in mySQL with timestamp expired
            get subscription info
            for each subscripted category
                append email msg
            endfor

            sendemail
            update user's timestamp
        endfor
        */

        List<Long> userList = getUsers();
        for(Long userID: userList){
            User subscriptionInfo = getSubscription(userID);
            StringBuilder emailMsg = new StringBuilder();
            String emailAddr = subscriptionInfo.email;
            String emailTitle = null;
            double bestDeal = 0;
            for(String category: topk.keySet()){
                if(subscriptionInfo.subs.get(category)){
                    for(Prod prod : topk.get(category)){
                        emailMsg.append(prod.prodTitle + "\n");
                        emailMsg.append(prod.prodURL + "\n");
                        emailMsg.append("by " + prod.category + "\n");
                        emailMsg.append( round(-prod.deal, 2) + "% price reduced \n");
                        emailMsg.append("Current Price: " + prod.curPrice + "\n\n");
                        bestDeal = Math.max(bestDeal, -prod.deal);
                    }
                }
            }
            emailTitle = "Up to " + round(bestDeal, 2) + "% price reduced at Amazon website";

            //send eamil;
            System.out.println(emailAddr);
            System.out.println(emailTitle);
            System.out.println(emailMsg.toString());
            new SendMail(emailAddr, emailTitle, emailMsg.toString());
            updateTimestamp(userID);
        }


        //clean all records in HashMap.
        //topk = new HashMap<>();

    }

    public List<Long> getUsers(){
        List<Long> list = new ArrayList<>();
        try {
            list = mysql.getExpiredSubscribers();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public User getSubscription(Long userId){
        User user = new User();
        try{
            user = mysql.getSubscriptionInfo(userId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return user;
    }

    public void updateTimestamp(Long userId){
        try{
            mysql.updateTimeStamp(userId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
