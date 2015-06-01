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

/**
 * @author Logan Gorence
 */
public class UpdateThread extends Thread {

    private Responder parentResponder;

    public UpdateThread(Responder parentResponder) {
        this.parentResponder = parentResponder;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                for (final Device d : parentResponder.pbClient.getDevices()) {
                    // Root device Node
                    Node deviceNode = parentResponder.devicesNode.createChild(d.getNickname()).build();
                    //deviceNode.setConfig()

                    // Actions
                    Action sendNoteAction = new Action(Permission.WRITE, new Handler<ActionResult>() {
                        @Override
                        public void handle(ActionResult event) {
                            Value title = event.getParameter("Title", ValueType.STRING);
                            Value message = event.getParameter("Message", ValueType.STRING);
                            try {
                                parentResponder.pbClient.sendNote(d.getIden(), title.getString(), message.getString());
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
                                parentResponder.pbClient.sendAddress(d.getIden(), url.getString(), url.getString());
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
                    if (d.getFingerprint() != null) {
                        fingerprintNode.setValue(new Value(d.getFingerprint()));
                    }
                    if (d.getIden() != null) {
                        idenNode.setValue(new Value(d.getIden()));
                    }
                    if (d.getApp_version() != null) {
                        appVersionNode.setValue(new Value(d.getApp_version()));
                    }
                    if (d.getCreated() != 0.0f) {
                        createdNode.setValue(new Value(Utils.epochToISO8601((long) d.getCreated())));
                    }
                    if (d.getModified() != 0.0f) {
                        modifiedNode.setValue(new Value(Utils.epochToISO8601((long) d.getModified())));
                    }
                    if (d.getManufacturer() != null) {
                        manufacturerNode.setValue(new Value(d.getManufacturer()));
                    }
                    if (d.getModel() != null) {
                        modelNode.setValue(new Value(d.getModel()));
                    }
                    if (d.getPush_token() != null) {
                        pushTokenNode.setValue(new Value(d.getPush_token()));
                    }

                    // These are booleans, we can't tell if
                    //  they aren't set or not...
                    activeNode.setValue(new Value(d.isActive()));
                    pushableNode.setValue(new Value(d.isPushable()));

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

                    if (!parentResponder.deviceNodes.containsKey(d.getIden())) {
                        parentResponder.deviceNodes.put(d.getIden(), deviceNode);
                    }
                }
                for (final Push p : parentResponder.pbClient.getNewPushes(5)) {
                    Node pushNode = parentResponder.pushesNode.createChild(p.getIden())
                            .setDisplayName((p.getIden() == null || p.getIden().isEmpty()) ? p.getIden() : "Push")
                            .build();

                    parentResponder.pushNodes.put(p.getIden(), pushNode);
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
