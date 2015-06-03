package org.dsa.iot;

import net.iharder.jpushbullet2.PushbulletClient;
import org.dsa.iot.dslink.*;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Logan Gorence
 */
public class Responder extends DSLinkHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Responder.class);
    protected static final Map<String, UpdateThread> updateThreads = new HashMap<>();
    private static final File configFile = new File("pushbullet_config.json");
    private static Map<String, String> storedConfigs;

    public Responder() {
        try {
            storedConfigs = Utils.loadConfiguration(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onResponderConnected(DSLink link) {
        LOGGER.info("Connected");

        final Node superRoot = link.getNodeManager().getSuperRoot();

        try {
            storedConfigs = Utils.loadConfiguration(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String s : storedConfigs.keySet()) {
            Utils.addAccountNode(superRoot, s, storedConfigs.get(s));
        }

        // Create our child roots
        NodeBuilder createAccountNode = superRoot.createChild("Create Account");
        Action createAccountAction = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                String name = event.getParameter("Name", ValueType.STRING).getString();
                String apiKey = event.getParameter("API Key", ValueType.STRING).getString();

                if (!storedConfigs.containsKey(name)) {
                    Utils.addAccountNode(superRoot, name, apiKey);
                    storedConfigs.put(name, apiKey);
                    try {
                        Utils.saveConfiguration(configFile, storedConfigs);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                event.setStreamState(StreamState.CLOSED);
            }
        });
        createAccountAction.addParameter(new Parameter("Name", ValueType.STRING));
        createAccountAction.addParameter(new Parameter("API Key", ValueType.STRING));
        createAccountNode.setAction(createAccountAction).build();
    }
}
