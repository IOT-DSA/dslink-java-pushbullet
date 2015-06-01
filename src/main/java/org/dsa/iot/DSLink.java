package org.dsa.iot;

import org.dsa.iot.dslink.DSLinkFactory;

import java.io.*;

public class DSLink {

    public static void main(String[] args) {
        try {
            String token = new BufferedReader(new FileReader(new File("pushbulletKey"))).readLine();
            DSLinkFactory.startResponder("Pushbullet", args, new Responder(token));
        } catch (FileNotFoundException e) {
            System.err.println("pushbulletKey not found.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}

