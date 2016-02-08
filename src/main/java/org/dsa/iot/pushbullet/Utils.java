package org.dsa.iot.pushbullet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.iharder.jpushbullet2.Device;
import net.iharder.jpushbullet2.Push;
import net.iharder.jpushbullet2.PushbulletException;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Logan Gorence
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Utils {

    private static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Gson gson = new Gson();

    public static String epochToISO8601(long epoch) {
        Date date = new Date(epoch * 1000L);
        return isoFormat.format(date);
    }

    public static Node buildDeviceNode(final UpdateThread updateThread, final Device pbDevice) {
        // Root device Node
        Node deviceNode = updateThread.devicesNode.createChild(pbDevice.getIden()).build();
        deviceNode.setDisplayName(pbDevice.getNickname());

        // Actions
        Action sendNoteAction = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                Value title = event.getParameter("Title", ValueType.STRING);
                Value message = event.getParameter("Message", ValueType.STRING);
                try {
                    updateThread.pbClient.sendNote(pbDevice.getIden(), title.getString(), message.getString());
                } catch (PushbulletException e) {
                    e.printStackTrace();
                }
                event.setStreamState(StreamState.CLOSED);
            }
        });
        sendNoteAction.addParameter(new Parameter("Title", ValueType.STRING));
        sendNoteAction.addParameter(new Parameter("Message", ValueType.STRING));
        Action sendUrlAction = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                Value url = event.getParameter("URL", ValueType.STRING);
                try {
                    updateThread.pbClient.sendAddress(pbDevice.getIden(), url.getString(), url.getString());
                } catch (PushbulletException e) {
                    e.printStackTrace();
                }
                event.setStreamState(StreamState.CLOSED);
            }
        });
        sendUrlAction.addParameter(new Parameter("URL", ValueType.STRING));

        // Create NodeBuilders
        NodeBuilder fingerprintNode = deviceNode.createChild("Fingerprint")
                .setValueType(ValueType.STRING);
        NodeBuilder idenNode = deviceNode.createChild("Iden")
                .setValueType(ValueType.STRING);
        NodeBuilder appVersionNode = deviceNode.createChild("App Version")
                .setValueType(ValueType.STRING);
        NodeBuilder createdNode = deviceNode.createChild("Created")
                //        .setValueType(ValueType.TIME);
                .setValueType(ValueType.STRING);
        NodeBuilder modifiedNode = deviceNode.createChild("Modified")
                //        .setValueType(ValueType.TIME);
                .setValueType(ValueType.STRING);
        NodeBuilder manufacturerNode = deviceNode.createChild("Manufacturer")
                .setValueType(ValueType.STRING);
        NodeBuilder modelNode = deviceNode.createChild("Model")
                .setValueType(ValueType.STRING);
        NodeBuilder pushTokenNode = deviceNode.createChild("Push Token")
                .setValueType(ValueType.STRING);
        NodeBuilder activeNode = deviceNode.createChild("Active")
                .setValueType(ValueType.BOOL);
        NodeBuilder pushableNode = deviceNode.createChild("Pushable")
                .setValueType(ValueType.BOOL);
        NodeBuilder sendNoteNode = deviceNode.createChild("Push Note")
                .setAction(sendNoteAction);
        NodeBuilder sendURLNode = deviceNode.createChild("Push URL")
                .setAction(sendUrlAction);


        // If values are null, don't set them to null.
        if (pbDevice.getFingerprint() != null) {
            fingerprintNode.setValue(new Value(pbDevice.getFingerprint()));
        }
        if (pbDevice.getIden() != null) {
            idenNode.setValue(new Value(pbDevice.getIden()));
        }
        if (pbDevice.getApp_version() != null) {
            appVersionNode.setValue(new Value(pbDevice.getApp_version()));
        }
        if (pbDevice.getCreated() != 0.0f) {
            createdNode.setValue(new Value(epochToISO8601((long) pbDevice.getCreated())));
        }
        if (pbDevice.getModified() != 0.0f) {
            modifiedNode.setValue(new Value(epochToISO8601((long) pbDevice.getModified())));
        }
        if (pbDevice.getManufacturer() != null) {
            manufacturerNode.setValue(new Value(pbDevice.getManufacturer()));
        }
        if (pbDevice.getModel() != null) {
            modelNode.setValue(new Value(pbDevice.getModel()));
        }
        if (pbDevice.getPush_token() != null) {
            pushTokenNode.setValue(new Value(pbDevice.getPush_token()));
        }

        // These are booleans, we can't tell if
        //  they aren't set or not...
        activeNode.setValue(new Value(pbDevice.isActive()));
        pushableNode.setValue(new Value(pbDevice.isPushable()));

        // Build the nodes
        fingerprintNode.build();
        idenNode.build();
        appVersionNode.build();
        createdNode.build();
        modifiedNode.build();
        manufacturerNode.build();
        modelNode.build();
        pushTokenNode.build();
        activeNode.build();
        pushableNode.build();
        sendNoteNode.build();
        sendURLNode.build();

        if (!updateThread.deviceNodes.containsKey(pbDevice.getIden())) {
            updateThread.deviceNodes.put(pbDevice.getIden(), deviceNode);
        }

        return deviceNode;
    }

    public static Node buildPushNode(final UpdateThread updateThread, Push pbPush) {
        Responder.LOGGER.info("Building push node for " + pbPush.getIden());
        Node pushNode = updateThread.pushesNode.createChild(pbPush.getIden())
                .setDisplayName((pbPush.getIden() == null || pbPush.getIden().isEmpty()) ? pbPush.getIden() : "Push")
                .build();

        updateThread.pushNodes.put(pbPush.getIden(), pushNode);

        return pushNode;
    }

    /**
     * Load a configuration file and parses the JSON to return a Map that
     * contains the name and API key.
     *
     * @param configFile Path of configuration file.
     * @return Map of Pushbullet names and API keys.
     */
    public static Map<String, String> loadConfiguration(File configFile) throws IOException {
        Map<String, String> configs;
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        configs = gson.fromJson(reader, stringStringMap);
        reader.close();
        if (configs == null) {
            configs = new HashMap<>();
        }
        return configs;
    }

    public static void saveConfiguration(File configFile, Map<String, String> storedConfigs) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
        writer.write(gson.toJson(storedConfigs));
        writer.close();
    }

    public static void addAccountNode(final Node superRoot, String name, String apiKey) {
        if (!Responder.updateThreads.containsKey(name)) {
            UpdateThread updateThread = new UpdateThread(superRoot, name, apiKey);
            updateThread.start();
            Responder.updateThreads.put(name, updateThread);
        }
    }

}
