package com.photon.photonchain.network.proto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:9:58 2018/1/2
 * @Modified by:
 */
public class GenerateProto {
    public static void main(String[] args) throws IOException {
        String protoPath = System.getProperty("user.dir") + "\\photon-chain-network\\src\\main\\resources\\proto";
        List<String> protoFileList = new ArrayList<String>();
        File f = new File(protoPath);
        File fa[] = f.listFiles();
        for (File fs : fa) {
            if (fs.isFile()) {
                protoFileList.add(fs.getName());
            }
        }
        for (String protoFile : protoFileList) {
            String strCmd = "protoc --java_out=../../java " + protoFile;
            Runtime.getRuntime().exec(strCmd, null, new File(protoPath));
            System.out.println("protoPath:"+protoPath+":"+strCmd);
        }
    }

}