package com.qingxuan;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by qingxuan on 8/6/17.
 */
public class AmazonCrawler {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private final String port = "60099";
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";

    private int roundRobinCnt = 0;
    private final int totalIPNum = 30;
    private String[] IPAddr = new String[totalIPNum];

    private HashSet<String> existProd = new HashSet<>();

    //constructor
    public AmazonCrawler(String proxyFile){

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

    public List<String> getURLByQuery(String query, int pageNum){
        //AssignIPByRoundRobin();

        List<String> URLs = new ArrayList<>();

        String queryURL = query.replaceAll(" ", "+");
        System.out.println(query);
        String url = AMAZON_QUERY_URL + queryURL + "&page=" + pageNum;

        Boolean found = false;

        while(!found) {
            AssignIPByRoundRobin();
            try {
                Document doc = Jsoup.connect(url).maxBodySize(0).userAgent(USER_AGENT).timeout(10000).get();
                System.out.println(doc.text());
                if(!doc.text().startsWith("Robot Check")) found = true;
                else continue;
                Elements prods = doc.getElementsByClass("s-result-item celwidget ");

                System.out.println("number of prod: " + prods.size());
                if (prods.size() == 0) return null;
                int startInd = Integer.valueOf(prods.get(0).id().substring(7));


                for (Integer i = startInd; i < prods.size() + startInd; i++) {

                    //get current product through prod id.
                    String id = "result_" + i.toString();
                    Element prodsById = doc.getElementById(id);

                    //if current result_i not exit.
                    if (prodsById == null) {
                        //System.out.println(id + "not exist");

                        continue;
                    }

                    //Use asin as HashSet key value for dedupe.
                    String asin = prodsById.attr("data-asin");
                    //System.out.println("prod asin: " + asin);
                    if (existProd.contains(asin)) {
                        //ads.add(null);
                        continue; //dedupe
                    }

                    Elements prodURLs = prodsById.getElementsByClass("a-link-normal s-access-detail-page  s-color-twister-title-link a-text-normal");
                    String prodURL = prodURLs.attr("href");
                    if (prodURL == null || prodURL == "") continue; //not a product
                    if (prodURL.startsWith("/gp/")) {
                        int startURL = prodURL.indexOf("https");
                        int endURL = prodURL.lastIndexOf("psc");
                        prodURL = prodURL.substring(startURL, endURL);
                        prodURL = java.net.URLDecoder.decode(prodURL, "UTF-8");

                        //URLDecoder decoder = new URLDecoder();

                    }
                    if (prodURL.contains("ref=")) {
                        int trimInd = prodURL.indexOf("ref=");
                        prodURL = prodURL.substring(0, trimInd);
                    }

                    URLs.add(prodURL);

                }


            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }





        return URLs;
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

    private void AssignIPByRoundRobin(){
        String curIP = IPAddr[roundRobinCnt % totalIPNum];

        initProxy(curIP);
        roundRobinCnt++;
        return;
    }
}
