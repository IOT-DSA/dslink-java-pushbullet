package org.dsa.iot;

import net.iharder.jpushbullet2.PushbulletClient;
import org.dsa.iot.dslink.*;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Logan Gorence
 */
public class Responder extends DSLinkHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Responder.class);
    protected final PushbulletClient pbClient;

    protected Node devicesNode;
    protected Node pushesNode;
    protected Map<String, Node> deviceNodes;
    protected Map<String, Node> pushNodes;
    private UpdateThread updateThread;

    public Responder(String token) {
        pbClient = new PushbulletClient(token);
        deviceNodes = new HashMap<>();
        pushNodes = new HashMap<>();
        updateThread = new UpdateThread(this);
    }

    @Override
    public void onResponderConnected(DSLink link) {
        LOGGER.info("Connected");

        // Create our child roots
        Node superRoot = link.getNodeManager().getSuperRoot();
        devicesNode = superRoot.createChild("Devices").build();
        pushesNode = superRoot.createChild("Pushes").build();

        // Start the update thread
        updateThread.start();
    }
}
