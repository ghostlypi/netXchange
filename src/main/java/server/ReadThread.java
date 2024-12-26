package server;

import main.Main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.Key;


public class ReadThread implements Runnable{

    public BufferedInputStream in;
    private boolean run;
    private Key secret;

    public ReadThread(BufferedInputStream in, Key secret) {
        this.in = in;
        this.secret = secret;
        this.run = true;
    }

    @Override
    public void run() {
        while (run) {
            //Execute Read Code Here
        }
        try {in.close();} catch (IOException e) {}
    }

    public void close() {
        this.run = false;
    }
}
