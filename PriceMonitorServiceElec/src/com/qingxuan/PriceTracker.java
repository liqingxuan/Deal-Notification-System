package com.qingxuan;

/**
 * Created by qingxuan on 8/9/17.
 */
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;

public class PriceTracker {
    private int EXP = 0; //0: never expire
    private String mMemcachedServer;
    private int mMemcachedPortal;
    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    private MySQLAccess mysql;
    private MemcachedClient cache;

    public PriceTracker(){

    }

    public void init(String memcachedServer,int memcachedPortal,String mysqlHost,String mysqlDb,String user,String pass){

        mMemcachedServer = memcachedServer;
        mMemcachedPortal = memcachedPortal;
        mysql_host = mysqlHost;
        mysql_db = mysqlDb;
        mysql_user = user;
        mysql_pass = pass;
        try {
            mysql = new MySQLAccess(mysql_host, mysql_db, mysql_user, mysql_pass);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String address = mMemcachedServer + ":" + mMemcachedPortal;
        try
        {
            cache = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(address));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Double lastPrice(Prod prod){
        String key = prod.prodURL;
        Double prePrice = -1.0;
        //List<String> tokens = Utility.cleanedTokenize(keyWords);

        if(cache.get(key) != null){ //instanceof Set){
            @SuppressWarnings("unchecked")
            Double prodPrice = (Double)cache.get(key);
            System.out.println("found prod in MemCached");
            //if(prodPrice < prod.curPrice) {
            prePrice = prodPrice;

            //}
            cache.set(key, EXP, prod.curPrice);
        }else{
            System.out.println("not found");
            cache.set(key, EXP, prod.curPrice);
        }
        System.out.println("preprice is" + prePrice);
        return prePrice;
    }

    public Boolean isProdExist(Prod prod) throws Exception{
        Boolean exist;
        try {
            exist = mysql.isRecordExist(prod.prodId);
        }catch(Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
        return exist;
    }

    public Boolean updateProdInfo(Prod prod) {
        try {
            mysql.updateProdData(prod);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean addProdInfo(Prod prod) {
        try {
            mysql.addProdData(prod);
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
