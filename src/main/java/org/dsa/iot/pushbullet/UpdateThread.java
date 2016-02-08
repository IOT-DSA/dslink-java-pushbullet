package org.dsa.iot.pushbullet;

import net.iharder.jpushbullet2.Device;
import net.iharder.jpushbullet2.PushbulletClient;
import net.iharder.jpushbullet2.PushbulletException;
import org.dsa.iot.dslink.node.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Logan Gorence
 */
public class UpdateThread extends Thread {

    protected Node accountNode;
    protected PushbulletClient pbClient;
    protected Node devicesNode;
    protected Node pushesNode;
    protected Map<String, Node> deviceNodes;
    protected Map<String, Node> pushNodes;

    public UpdateThread(final Node superRoot, String accountNodeName, String apiKey) {
        this.accountNode = superRoot.createChild(accountNodeName).build();
        this.pbClient = new PushbulletClient(apiKey);
        this.pbClient.addPushbulletListener(new PushListener(this));
        this.pbClient.startWebsocket();
        this.devicesNode = accountNode.createChild("Devices").build();
        this.pushesNode = accountNode.createChild("Pushes").build();
        this.deviceNodes = new HashMap<>();
        this.pushNodes = new HashMap<>();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                for (Device d : pbClient.getDevices()) {
                    Utils.buildDeviceNode(this, d);
                }
            } catch (PushbulletException e) {
                e.printStackTrace();
            }
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
