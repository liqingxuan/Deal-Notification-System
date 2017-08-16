package com.qingxuan;

import com.rabbitmq.client.*;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws Exception{

        if(args.length < 1){
            System.out.println("Please input: proxyFilePath");
            System.exit(0);
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        Channel outChannel = connection.createChannel();

        String proxyFilePath = args[0];

        Crawler crawler = new Crawler(proxyFilePath);

        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    String[] msgs = message.split(";");
                    String category = msgs[0].trim();
                    String prodURL = msgs[1].trim();
                    String sendMsg = crawler.GetAdsInfoByURL(prodURL);
                    System.out.println(sendMsg);
                    if(sendMsg != null){
                        String routingKeyInfo = category;
                        outChannel.basicPublish("e_prod_info", routingKeyInfo, null, sendMsg.getBytes());

                        System.out.println(" [x] Sent '" + sendMsg + "'");
                    }
                    Thread.sleep(2000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        inChannel.basicConsume("q_URLs", true, consumer);
    }

}
