package org.dsa.iot;

import net.iharder.jpushbullet2.PushbulletEvent;
import net.iharder.jpushbullet2.PushbulletException;
import net.iharder.jpushbullet2.PushbulletListener;

/**
 * @author Logan Gorence
 */
public class PushListener implements PushbulletListener {

    private Responder responder;

    protected PushListener(Responder responder) {
        this.responder = responder;
    }

    @Override
    public void pushReceived(PushbulletEvent pushbulletEvent) {
        try {
            Utils.buildPushNode(responder, pushbulletEvent.getPushbulletClient().getNewPushes(1).get(0));
        } catch (PushbulletException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void devicesChanged(PushbulletEvent pushbulletEvent) {
    }

    @Override
    public void websocketEstablished(PushbulletEvent pushbulletEvent) {
    }

}
