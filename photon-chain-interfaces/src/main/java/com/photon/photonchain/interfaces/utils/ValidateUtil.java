package com.photon.photonchain.interfaces.utils;

/**
 * @author: lqh
 * @description: data check
 * @program: photon-chain
 * @create: 2018-03-08 13:40
 **/
public class ValidateUtil {

    public static boolean rexCheckPassword(String input) {
        //String reg = "^([A-Z]|[a-z]|[0-9]|[`-=[];,./~!@#$%^*()_+}{:?]){6,20}$";
        String regStr = "^([A-Z]|[a-z]|[0-9]|[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]){6,20}$";
        return input.matches(regStr);
    }


    public static boolean checkLetter(String input) {
        String regStr = "^[A-Za-z0-9]+$";
        return input.matches(regStr);
    }


    public static boolean checkPositiveInteger(String input) {
        String regStr = "^[0-9]*[1-9][0-9]*$";
        return input.matches(regStr);
    }


}
