package com.photon.photonchain.storage.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @Author:PTN
 * @Description:
 * @Date:10:13 2018/1/22
 * @Modified by:
 */
@Entity
public class NodeAddress {
    @Id
    String hexIp;
    int port;

    public NodeAddress() {

    }

    public NodeAddress(String hexIp, int port) {
        this.hexIp = hexIp;
        this.port = port;
    }

    public String getHexIp() {
        return hexIp;
    }

    public void setHexIp(String hexIp) {
        this.hexIp = hexIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeAddress that = (NodeAddress) o;

        if (port != that.port) return false;
        return hexIp != null ? hexIp.equals(that.hexIp) : that.hexIp == null;
    }

    @Override
    public int hashCode() {
        int result = hexIp != null ? hexIp.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "NodeAddress{" +
                "hexIp='" + hexIp + '\'' +
                ", port=" + port +
                '}';
    }
}
