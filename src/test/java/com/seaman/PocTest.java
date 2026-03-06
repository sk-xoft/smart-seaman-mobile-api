package com.seaman;

import com.google.gson.Gson;

public class PocTest {

    public static void main(String[] agre){

        String jsonString = "{ \"dataDate\":\"20/,:23\"}";

        Gson json = new Gson();

        PocData data = json.fromJson(jsonString, PocData.class);

        System.out.println("value " + data.getDataDate());

    }

}
