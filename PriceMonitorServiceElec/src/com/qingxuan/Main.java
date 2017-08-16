package com.qingxuan;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Main {

    //private PriceTracker priceTracker;
    private static String mMemcachedServer = "127.0.0.1";
    private static int mMemcachedPortal = 11211;
    private static String mysql_host = "127.0.0.1:3306";
    private static String mysql_db = "PriceDetection";
    private static String mysql_user = "root";
    private static String mysql_pass = "password";

    private String MQServer = "127.0.0.1";

    private Object lock = new Object();

    private static int cnt = 0;

    public static void main(String[] args) throws Exception {
        // write your code here
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();

        PriceTracker priceTracker = new PriceTracker();
        priceTracker.init(mMemcachedServer,mMemcachedPortal,mysql_host,mysql_db,mysql_user,mysql_pass);
        //PriceTracker priceTracker = new PriceTracker(mMemcachedServer,mMemcachedPortal,mysql_host,mysql_db,mysql_user,mysql_pass);



        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + envelope.getRoutingKey() + ":" + message + "'");
                    String[] prodInfo = message.split(";");

                    Prod prod = new Prod();
                    prod.category = envelope.getRoutingKey();
                    prod.curPrice = Double.valueOf(prodInfo[0].substring(1).trim());
                    System.out.println(prod.curPrice);
                    prod.prodTitle = prodInfo[1].trim();
                    System.out.println(prod.prodTitle);
                    prod.prodURL = prodInfo[2].trim();
                    System.out.println(prod.prodURL);

                    prod.prodId = prod.prodURL.substring(prod.prodURL.length() - 11, prod.prodURL.length() - 1);
                    System.out.println("prodID = " + prod.prodURL);

                    //search in Memcacahed.
                    Double lastPrice = priceTracker.lastPrice(prod);
                    prod.lastPrice = lastPrice;
                    System.out.println("lastPrice = " + lastPrice);
                    System.out.println("curPrice = " + prod.curPrice);

                    //deal detected.
                    if(lastPrice > prod.curPrice) {
                        //send to rabbitMQ outchannel


                        StringBuilder sb = new StringBuilder();
                        sb.append(prod.category+ ";");
                        sb.append(prod.prodTitle + ";");
                        sb.append(prod.prodURL + ";");
                        sb.append((prod.curPrice-prod.lastPrice)/prod.lastPrice  + ";");
                        sb.append(prod.curPrice);


                        String msgSend = sb.toString();

                        Channel outChannel = connection.createChannel();
                        outChannel.queueDeclare("q_deal", true, false, false, null);
                        System.out.println(" [x] Sent '" + msgSend + "'");

                        outChannel.basicPublish("", "q_deal", null, msgSend.getBytes("UTF-8"));
                        //outChannel.close();

                    }


                    //System.out.println("SQL: " + prod.prodURL);
                    //prod.prodURL = prod.prodURL.substring(23);

                    //update mySQL database
                    if(priceTracker.isProdExist(prod)) {
                        priceTracker.updateProdInfo(prod);
                    }else {
                        priceTracker.addProdInfo(prod);
                    }


                    //Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        inChannel.basicConsume("q_Electronics", true, consumer);
        //inChannel.basicConsume("q_Computers", true, consumer);
        //inChannel.close();
        //connection.close();


    }
}
