package belezirev.encho.bluetoothcarparktronic;


import android.app.Notification;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.function.Consumer;

public class BluetoothMessageHandler extends Thread{
    private LinkedBlockingQueue<String> messageQueue;
    private List<Consumer<String>> processors;

    public BluetoothMessageHandler(Consumer<String>... processors){
        this.messageQueue = new LinkedBlockingQueue<>();
        this.processors = Arrays.asList(processors);
    }

    public void handleMessage(String message){
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            while (true){
                String message = messageQueue.poll();
                if (message == null){
                    Thread.sleep(200);
                    continue;
                }
                String[] splitted = message.split("&");
                //Suppose there are only four processors and only four values in the splitted array
                if (splitted.length != processors.size()){
                    System.out.println("The processors are not matching to the values which the sensors provide");
                    continue;
                }
                for (int i = 0; i< splitted.length; i++){
                    processors.get(i).accept(splitted[i]);
                }
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }


    }
}
