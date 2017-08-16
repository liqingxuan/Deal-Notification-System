package com.qingxuan;

import java.io.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException{
        if(args.length < 3){
            System.out.println("Please input: rawQueryDataFilePath, proxyFilePath and outputDataFilePath.");
            System.exit(0);
        }

        String rawQueryDataFilePath = args[0];
        String proxyFilePath = args[1];
        String URLDataFilePath = args[2];

        AmazonCrawler crawler = new AmazonCrawler(proxyFilePath);

//        File URLDataFile = new File(URLDataFilePath);
//        if(!URLDataFile.exists()) URLDataFile.createNewFile();
//
//        FileWriter fw = new FileWriter(URLDataFile.getAbsoluteFile());
//        BufferedWriter bw = new BufferedWriter(fw);

        try(BufferedReader br = new BufferedReader(new FileReader(rawQueryDataFilePath))) {

            String line;
            BufferedWriter bw = null;

            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;

                //read each query
                if (line.startsWith("category") || line.startsWith("Category")) {
                    if(bw != null) bw.close();

                    File URLDataFile = new File(URLDataFilePath+"/FeedsURL" + line.substring(9) + ".txt");
                    if(!URLDataFile.exists()) URLDataFile.createNewFile();


                    FileWriter fw = new FileWriter(URLDataFile.getAbsoluteFile());
                    bw = new BufferedWriter(fw);

                    //bw.write(line);
                    //sbw.newLine();
                    continue;
                }

                for (int pageNum = 1; pageNum <= 1; pageNum++) {
                    List<String> URLs = crawler.getURLByQuery(line.trim(), pageNum);
                    if (URLs == null || URLs.size() == 0) continue;
                    for (String URL : URLs) {
                        if (URL == null || !URL.startsWith("http")) continue;
                        bw.write(URL);
                        bw.newLine();
                    }
                    Thread.sleep(2000);
                }
            }
            bw.close();
        } catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
