package com.qingxuan;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by qingxuan on 8/6/17.
 */



public class TestRunnable implements Runnable{

    private String dataFilePath;
    private Channel channel;
    private Connection connection;
    private String category;

    @Override
    public void run() {
        System.out.println("run called");


        //read from all message
        try {
            FileReader fr = new FileReader(dataFilePath);
            BufferedReader br = new BufferedReader(fr);

            String line;

            try{
                connect();
                while((line = br.readLine()) != null){
                    String msg = category + " ; " + line;
                    System.out.println(" [x] Sent '" + msg + "'");
                    channel.basicPublish("", "q_URLs", null, msg.getBytes("UTF-8"));

                }

                channel.close();
                connection.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public TestRunnable(String dataFilePath,
                        String category){
        this.dataFilePath = dataFilePath;
        this.category = category;
    }

    private void connect() throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare("q_URLs", true, false, false, null);
    }

}
