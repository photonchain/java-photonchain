package com.photon.photonchain.network.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: PTN
 * @description: process list
 * @program: photon-chain
 * @create: 2018-04-27 17:16
 **/
public class ListUtil {
    public static List diffSet(List list1, List list2) {
        List listRes = new ArrayList();
        listRes.addAll(list1);
        listRes.addAll(list2);

        List listRep = new ArrayList();

        for (int i = 0; i < list1.size(); i++) {
            for (int k = 0; k < list2.size(); k++) {
                if (list1.get(i).toString().equals(list2.get(k).toString())) {
                    listRep.add(list1.get(i));
                }
            }
        }
        listRes.removeAll(listRep);
        return listRes;
    }
}
