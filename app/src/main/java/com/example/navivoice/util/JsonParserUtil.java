package com.example.navivoice.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

/**
 * Json结果解析类
 */
public class JsonParserUtil {
    private static final String[] posname = {"<callCmd>","<contact>","<callCmdmax>","<contactmin>","<before>","<next>","<silkroad>","<dunhuang>","<zhangye>","<qinghai>","<lanzhou>","<wuwei>","<xining>","<menyuan>","<haidong>","<chakayanhu>","<delingha>","<dachaidan>","<shuishangyadan>","<guazhou>","<yumen>","<jiayuguan>","<qiliandacaoyuan>","<gangcha>","<dalian>","<beijing>","<shanghai>","<guangzhou>","<shenzhen>","<zhengzhou>","<xuchang>"};

    public static int parseGrammarResultcontact(String json) {
        int ret=0;
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                String item = words.getJSONObject(i).getString("slot");
                if(item.equals("<contact>"))
                {
                    JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                    for(int j = 0; j < items.length(); j++)
                    {
                        JSONObject obj = items.getJSONObject(j);
                        if(obj.getString("w").contains("nomatch"))
                        {
                            //ret.append("没有匹配结果.");
                            return 0;
                        }


                        ret= obj.getInt("sc");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            //ret.append("没有匹配结果.");
        }
        return ret;
    }

    public static Map<String,Integer> parseGrammarResultScore(String json) {
        Map<String, Integer> map = new HashMap<>();
        int ret=0;
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                String item = words.getJSONObject(i).getString("slot");
                for (String s : posname) {
                    if (item.equals(s)) {
                        JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                        for (int j = 0; j < items.length(); j++) {
                            JSONObject obj = items.getJSONObject(j);
                            if (obj.getString("w").contains("nomatch")) {
                                //ret.append("没有匹配结果.");
                                map.put("error", 0);
                                return map;
                            }
                            ret = obj.getInt("sc");
                            map.put(s, ret);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //ret.append("没有匹配结果.");
        }
        return map;
    }

}
