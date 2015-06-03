package org.dsa.iot;

import org.dsa.iot.dslink.DSLinkFactory;

public class DSLink {

    public static void main(String[] args) {
        DSLinkFactory.startResponder("Pushbullet", args, new Responder());
    }

}

