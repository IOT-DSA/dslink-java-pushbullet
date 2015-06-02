package org.dsa.iot;

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
import org.vertx.java.core.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Logan Gorence
 */
public class Utils {

    private static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String epochToISO8601(long epoch) {
        Date date = new Date(epoch * 1000L);
        return isoFormat.format(date);
    }

    public static Node buildDeviceNode(final Responder responder, final Device pbDevice) {
        // Root device Node
        Node deviceNode = responder.devicesNode.createChild(pbDevice.getIden()).build();
        deviceNode.setDisplayName(pbDevice.getNickname());

        // Actions
        Action sendNoteAction = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                Value title = event.getParameter("Title", ValueType.STRING);
                Value message = event.getParameter("Message", ValueType.STRING);
                try {
                    responder.pbClient.sendNote(pbDevice.getIden(), title.getString(), message.getString());
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
                    responder.pbClient.sendAddress(pbDevice.getIden(), url.getString(), url.getString());
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

        if (!responder.deviceNodes.containsKey(pbDevice.getIden())) {
            responder.deviceNodes.put(pbDevice.getIden(), deviceNode);
        }

        return deviceNode;
    }

    public static Node buildPushNode(final Responder responder, Push pbPush) {
        Responder.LOGGER.info("Building push node for " + pbPush.getIden());
        Node pushNode = responder.pushesNode.createChild(pbPush.getIden())
                .setDisplayName((pbPush.getIden() == null || pbPush.getIden().isEmpty()) ? pbPush.getIden() : "Push")
                .build();

        responder.pushNodes.put(pbPush.getIden(), pushNode);

        return pushNode;
    }

}
