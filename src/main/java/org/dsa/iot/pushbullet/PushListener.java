package org.dsa.iot.pushbullet;

import net.iharder.jpushbullet2.PushbulletEvent;
import net.iharder.jpushbullet2.PushbulletException;
import net.iharder.jpushbullet2.PushbulletListener;

/**
 * @author Logan Gorence
 */
public class PushListener implements PushbulletListener {

    private UpdateThread updateThread;

    protected PushListener(UpdateThread updateThread) {
        this.updateThread = updateThread;
    }

    @Override
    public void pushReceived(PushbulletEvent pushbulletEvent) {
        try {
            Utils.buildPushNode(updateThread, pushbulletEvent.getPushbulletClient().getNewPushes(1).get(0));
        } catch (PushbulletException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void devicesChanged(PushbulletEvent pushbulletEvent) {
        Responder.LOGGER.info("Devices changed");
    }

    @Override
    public void websocketEstablished(PushbulletEvent pushbulletEvent) {
    }

}
