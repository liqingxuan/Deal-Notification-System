package com.qingxuan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Random;

/**
 * Created by qingxuan on 8/6/17.
 */
public class Crawler {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private final String port = "60099";

    private int roundRobinCnt = 0;
    private final int totalIPNum = 30;
    private String[] IPAddr = new String[totalIPNum];

    private Boolean test = false;

    //constructor
    public Crawler(String proxyFile){

        // initialize proxy.
        try(BufferedReader br = new BufferedReader(new FileReader(proxyFile))){

            String line;
            int IPCnt = 0;

            while((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;

                //Read each query.
                //System.out.println(line);
                String[] fields = line.split(",");
                String IP = fields[0].trim();
                IPAddr[IPCnt++] = IP;
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public String GetAdsInfoByURL(String URL){

        Boolean found = false;

        while(!found) {
            AssignIPByRoundRobin();


            try {
                //HashMap<String, String> headers = new HashMap<>();
                //headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                //headers.put("Accept-Encoding", "gzip, deflate, br");
                //headers.put("Accept-Language", "en-US,en;q=0.8");
                //Document doc = Jsoup.connect(url).maxBodySize(0).headers(headers).userAgent(USER_AGENT).timeout(10000).get();
                Document doc = Jsoup.connect(URL).maxBodySize(0).userAgent(USER_AGENT).timeout(10000).get();

                System.out.println(doc.text());
                if(!doc.text().startsWith("Robot Check")) found = true;
                else continue;

                // get prod price
                String price = "$500.00";


                    Element priceEle = doc.getElementById("priceblock_ourprice");
                    price = priceEle.text();
                    //System.out.println("price: " + price);
                    if(price.contains("-")) price = price.substring(0, price.indexOf("-")).trim();
                    System.out.println("price: " + price);
                    if (price == null) {
                        priceEle = doc.getElementById("priceblock_saleprice");
                        price = priceEle.text();
                        System.out.println("price: " + price);
                    }
                    if (price == null) {
                        System.out.println("price is null");
                        return null;
                    }
                    if(test && roundRobinCnt%2 == 0){
                        Random r = new Random();
                        //int randomInt = r.nextInt(100) + 1;
                        price = "$" + String.valueOf(Double.valueOf(price.substring(1)) + r.nextInt(50) + 1);
                        System.out.println("Change price to: " + price);
                    }



                // get prod name
                Element titleEle = doc.getElementById("productTitle");
                String title = titleEle.text();
                System.out.println("title: " + title);
                if (title == null) {
                    System.out.println("title is null");
                    return null;
                }

                String msg = price + " ; " + title + " ; " + URL;
                return msg;


            } catch (Exception e) {

            }
        }
        return null;
    }

    private void AssignIPByRoundRobin(){
        String curIP = IPAddr[roundRobinCnt % totalIPNum];

        initProxy(curIP);
        roundRobinCnt++;
        return;
    }

    public void initProxy(String IP) {
        //socks5 set up
        //System.setProperty("socksProxyHost", "199.101.97.161"); // set socks proxy server
        //System.setProperty("socksProxyPort", "61336"); // set socks proxy port

        //http setup
        System.setProperty("http.proxyHost", IP); // set proxy server
        System.setProperty("http.proxyPort", port); // set proxy port

        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
    }

}
