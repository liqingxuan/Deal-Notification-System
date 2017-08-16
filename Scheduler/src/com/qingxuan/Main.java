package com.qingxuan;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void main(String[] args) {

        if(args.length < 1){
            System.out.println("Please input: URLDataFilePath.");
            System.exit(0);
        }

        String URLDataFilePath = args[0] + "/FeedsURL";

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


        // for loop, based on all category name.
        TestRunnable ECORunnable = new TestRunnable(URLDataFilePath + "Electronics" + ".txt", "Electronics");
        TestRunnable KidsRunnable = new TestRunnable(URLDataFilePath + "Kids" + ".txt", "Kids");
        TestRunnable BHRunnable = new TestRunnable(URLDataFilePath + "Beauty" + ".txt", "Beauty");
        TestRunnable HGTRunnable = new TestRunnable(URLDataFilePath + "Home" + ".txt", "Home");
        TestRunnable SORunnable = new TestRunnable(URLDataFilePath + "Outdoors" + ".txt", "Outdoors");

        executorService.scheduleAtFixedRate(ECORunnable, 0,12, TimeUnit.HOURS);
        executorService.scheduleAtFixedRate(KidsRunnable, 0,36, TimeUnit.HOURS);
        executorService.scheduleAtFixedRate(BHRunnable, 0,24, TimeUnit.HOURS);
        executorService.scheduleAtFixedRate(HGTRunnable, 0,24, TimeUnit.HOURS);
        executorService.scheduleAtFixedRate(SORunnable, 0,24, TimeUnit.HOURS);

    }
}
