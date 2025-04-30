package com.vgerbot.propify;

import com.ibm.icu.text.MessageFormat;

import java.text.FieldPosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        test1();
    }
    private static void test1() {

        String format = "hello {name}";
        MessageFormat mf = new MessageFormat(format);
        Map<Object, Object> arguments = new HashMap<>();
        arguments.put("name", "world");

        FieldPosition fp = new FieldPosition(1);
        StringBuffer sb = new StringBuffer();
        try{
            sb = mf.format(arguments, sb, fp);
            System.out.println(sb.toString());
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    private static void test0() {
        String format = "At {1,time,::jmm} on {1,date,::dMMMM}, there was {2} on planet {3,number,integer}.";
        MessageFormat mf = new MessageFormat(format);
//        Map<Object, Object> arguments = new HashMap<>();
//        arguments.put(1, new Date());
//        arguments.put(2, new Date());
//        arguments.put(3, "a Disturbance in the Force");
//        arguments.put(4, 5);

        Object[] arguments = new Object[]{
                new Date(),
                new Date(),
                "a Disturbance in the Force",
                5
        };

        FieldPosition fp = new FieldPosition(1);
        StringBuffer sb = new StringBuffer();
        try{
            sb = mf.format(arguments, sb, fp);
            System.out.println(sb.toString());
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }
}
