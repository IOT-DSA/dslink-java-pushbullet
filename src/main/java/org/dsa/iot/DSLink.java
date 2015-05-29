package org.dsa.iot;

import net.iharder.jpushbullet2.PushbulletClient;
import org.dsa.iot.dslink.DSLinkFactory;

public class DSLink {

    public static final PushbulletClient pbClient = new PushbulletClient("Tu9JdmdwfjJQjIEiQwiwGoMW5PUFfwMg");

    public static void main(String[] args) {
        DSLinkFactory.startResponder("Pushbullet-", new String[]{"-b", "http://localhost:8080/conn"}, new Responder());
    }

}

