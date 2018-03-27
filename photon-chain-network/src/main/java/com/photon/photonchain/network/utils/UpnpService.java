package com.photon.photonchain.network.utils;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Wu
 *
 * Created by SKINK on 2017/12/27.
 */
public class UpnpService {
  private static Logger logger = LoggerFactory.getLogger(UpnpService.class);

  public static void main(String[] args)
      throws IOException, SAXException, ParserConfigurationException {
    logger.info("Starting weupnp");

    GatewayDiscover discover = new GatewayDiscover();
    logger.info("Looking for Gateway Devices");
    discover.discover();
    GatewayDevice d = discover.getValidGateway();
    if (null != d) {
      logger.info("Found gateway device.\n{0} ({1})",
          new Object[]{d.getModelName(), d.getModelDescription()});
    } else {
      logger.info("No valid gateway device found.");
      return;
    }
    InetAddress localAddress = d.getLocalAddress();
    logger.info("Using local address: {0}", localAddress);
    String externalIPAddress = d.getExternalIPAddress();
    logger.info("External address: {0}", externalIPAddress);
  }

}
