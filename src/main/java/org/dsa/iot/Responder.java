package org.dsa.iot;

import org.dsa.iot.dslink.*;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.node.Node;

/**
 * @author Logan Gorence
 */
public class Responder extends DSLinkHandler {

    @Override
    public void onResponderConnected(DSLink link) {
        super.onResponderConnected(link);

        Node superRoot = link.getNodeManager().getSuperRoot();
        superRoot.createChild("test").build();
    }
}
