package com.qingxuan;


import com.rabbitmq.client.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String mysql_host = "127.0.0.1:3306";
    private static String mysql_db = "PriceDetection";
    private static String mysql_user = "root";
    private static String mysql_pass = "password";

    public static void main(String[] args) throws Exception{

	// write your code here

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();


        //mysql connection
        EmailScheduler scheduler = new EmailScheduler(mysql_host,mysql_db,mysql_user,mysql_pass);
        //sendEmail.init();

        //receive message from rabbitMQ
        /*
        for every msg received
        send for top k selection
         */


        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + envelope.getRoutingKey() + ":" + message + "'");
                    String[] prodInfo = message.split(";");
                    Prod prod = new Prod();
                    prod.category = prodInfo[0].trim();
                    prod.prodTitle = prodInfo[1].trim();
                    prod.prodURL = prodInfo[2].trim();
                    prod.deal = Double.valueOf(prodInfo[3].trim()) * 100;
                    prod.curPrice = Double.valueOf(prodInfo[4].trim());
                    prod.receiveTime = LocalDateTime.now();
                    scheduler.update(prod);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        inChannel.basicConsume("q_deal", true, consumer);


        //every 1 min(1 day/12hr) send email and clean current record.
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(scheduler, 1, 60, TimeUnit.SECONDS);
    }
}
