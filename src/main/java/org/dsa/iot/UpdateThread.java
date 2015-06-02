package org.dsa.iot;

import net.iharder.jpushbullet2.Device;
import net.iharder.jpushbullet2.Push;
import net.iharder.jpushbullet2.PushbulletException;
import org.dsa.iot.dslink.node.Node;

/**
 * @author Logan Gorence
 */
public class UpdateThread extends Thread {

    private Responder responder;

    public UpdateThread(Responder responder) {
        this.responder = responder;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                for (Device d : responder.pbClient.getDevices()) {
                    Utils.buildDeviceNode(responder, d);
                }
                /*for (final Push p : responder.pbClient.getNewPushes(5)) {

                }*/
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
