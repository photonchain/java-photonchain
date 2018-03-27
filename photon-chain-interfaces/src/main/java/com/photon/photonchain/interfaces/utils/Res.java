package com.photon.photonchain.interfaces.utils;

/**
 * @Author:lqh
 * @Description:
 * @Date:10:31 2018/1/11
 * @Modified by:
 */
public class Res {

    public int code;

    public String msg;

    public Object data;

    public final static int CODE_100 = 100; //get data success
    public final static int CODE_101 = 101; //get data fail
    public final static int CODE_102 = 102; //unknow account
    public final static int CODE_103 = 103; //Illegal password
    public final static int CODE_104 = 104; //syncing
    public final static int CODE_105 = 105; //unknow sender
    public final static int CODE_106 = 106; //not sufficient funds
    public final static int CODE_107 = 107; //export success
    public final static int CODE_108 = 108; //Memorizing words error
    public final static int CODE_109 = 109; //wallet import already;
    public final static int CODE_110 = 110; //import success
    public final static int CODE_111 = 111; //import fail
    public final static int CODE_112 = 112; //init account success
    public final static int CODE_113 = 113; //init account fail
    public final static int CODE_114 = 114; //unknow transaction hash
    public final static int CODE_115 = 115; //syncing,can't start mine.
    public final static int CODE_116 = 116;  //start mine.
    public final static int CODE_117 = 117;  //stop mine fail
    public final static int CODE_118 = 118;  //stop mine success
    public final static int CODE_119 = 119;  //token success
    public final static int CODE_120 = 120;  //mining
    public final static int CODE_121 = 121;  //unmine
    public final static int CODE_122 = 122;  //op success
    public final static int CODE_123 = 123;  //token already
    public final static int CODE_124 = 124;  //token amount in 1000-100000000000
    public final static int CODE_125 = 125;  //token precision in 6-12
    public final static int CODE_126 = 126;  //deal amount lager ZERO
    public final static int CODE_127 = 127;  //Token name in 3-20
    public final static int CODE_128 = 128;  //deal token precision out of range
    public final static int CODE_200 = 200; //commit transaction
    public final static int CODE_201 = 201; //to account never deal,please use publickey
    public final static int CODE_202 = 202; //success check
    public final static int CODE_301 = 301; //password error
    public final static int CODE_401 = 401; //wallet address & publickey not same.


    public Res() {
        this.code = 0;
        this.msg = "";
        this.data = "";
    }

    public Res(int code, String msg, Object data) {
        super();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Res [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
