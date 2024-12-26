package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.Key;

public class WriteThread implements Runnable{

    BufferedOutputStream out;
    private boolean run;
    private Key secret;

    public WriteThread(BufferedOutputStream out, Key secret) {
        this.out = out;
        this.secret = secret;
        this.run = true;
    }

    @Override
    public void run() {
        while(run) {
            //Execute Write Code Here
        }
        try {out.close();}catch(IOException e) {} // Free the BufferedOutputStream
    }

    public void close() {
        this.run = false;
    }
}
