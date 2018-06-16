package com.photon.photonchain.network.test;

import com.photon.photonchain.network.utils.FoundryUtils;
import java.util.Calendar;
import org.spongycastle.util.encoders.Hex;

/**
 * @author PTN Created by PTN on 2018/2/28.
 */
public class TimeTest {

  public static void main(String[] args) {
  }

  private int getDiffYear(long genesis,long current){
    Calendar calendarOne = Calendar.getInstance();
    calendarOne.setTimeInMillis(genesis);
    Calendar calendarTwo = Calendar.getInstance();
    calendarTwo.setTimeInMillis(current);
    int year1 = calendarOne.get(Calendar.YEAR);
    int year2 = calendarTwo.get(Calendar.YEAR);
   return (year2-year1);
  }

}
