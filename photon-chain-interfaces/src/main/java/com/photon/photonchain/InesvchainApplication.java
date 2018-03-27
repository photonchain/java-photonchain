package com.photon.photonchain;


import com.photon.photonchain.interfaces.utils.PropertyUtil;
import com.photon.photonchain.network.core.GenesisBlock;
import com.photon.photonchain.network.core.Initialization;
import com.photon.photonchain.network.core.SyncBlock;
import com.photon.photonchain.network.peer.PeerClient;
import com.photon.photonchain.storage.entity.NodeAddress;
import com.photon.photonchain.storage.repository.NodeAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @Author PTN
 */
@SpringBootApplication
@EnableCaching
public class InesvchainApplication implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private GenesisBlock genesisBlock;
    @Autowired
    private SyncBlock syncBlock;
    @Autowired
    private Initialization initialization;
    @Autowired
    private PeerClient peerClient;
    @Autowired
    private NodeAddressRepository nodeAddressRepository;

    public static void main(String[] args) {
        SpringApplication.run(InesvchainApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        loadInitialNode();
        genesisBlock.init();
        initialization.init();
        peerClient.init();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        syncBlock.init();
    }

    public void loadInitialNode() {
        String NODE_ADDRESS = PropertyUtil.getProperty("NODE_ADDRESS");
        String[] nodeAddress = NODE_ADDRESS.split("\\|");
        for (String address : nodeAddress) {
            System.out.println(address);
            nodeAddressRepository.save(new NodeAddress(address, 0));
        }
    }
}


