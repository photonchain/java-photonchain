package com.photon.photonchain.network.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author PTN
 *
 * Created by PTN on 2017/12/27.
 */
public class UpnpService {

  private static Logger logger = LoggerFactory.getLogger(UpnpService.class);

  public static void main(String[] args)
      throws IOException, SAXException, ParserConfigurationException {

    logger.info("Looking for UPnP gateway device...");
    GatewayDiscover discover = new GatewayDiscover();
    Map<InetAddress, GatewayDevice> gatewayMap = discover.discover();
    if (gatewayMap == null || gatewayMap.isEmpty()) {
      logger.info("There are no UPnP gateway devices");
    } else {
      gatewayMap.forEach((addr, device) ->
          logger.info("UPnP gateway device found on " + addr.getHostAddress()));
      GatewayDevice gateway = discover.getValidGateway();
      if (gateway == null) {
        logger.info("There is no connected UPnP gateway device");
      } else {
        InetAddress localAddress = gateway.getLocalAddress();
        InetAddress externalAddress = InetAddress.getByName(gateway.getExternalIPAddress());
        logger.info("Using UPnP gateway device on " + localAddress.getHostAddress());
        logger.info("External IP address is " + externalAddress.getHostAddress());
      }
    }
  }

}
