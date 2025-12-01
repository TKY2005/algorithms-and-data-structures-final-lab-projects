/*
 *  Name: Youssef Mohamed Torki Ahmed
 *  ID: 445820246
 *  Project1: Packet receiver using linked list
 * 
 *  Techinical description:
 * 
 *      class Server
 *      The Server class has all the packets stored in its own buffer and has an internal counter to track next packet to send
 *      The server can send packets sequentially (according to server's order) or send an explicitly requested packet with a sequence number
 *      
 *      class PacketReceiver
 *      when the packet receiver is invoked. it will keep requesting the next packet from the server and keeps it in sync using its internal sequence counter
 *      the receiver adds all received packets to the tail of the packet buffer (duplicates are dropped)
 *      the receiver assumes that as long as the packet sequence matches the counter all the packets in the buffer are in sorted order from head to tail
 *      
 *      if a packet arrives out of order (the sequence of the packet doesn't match the counter)
 *      the received packet is pushed to the tail of the buffer
 *      the timeout counter is incremented and a debug warning message is raised
 *      if the timeout counter reaches a certain limit and the expected packet is yet to arrive
 *      the receiver will explicitly ask for the missing packet from the server
 *      the packet will be pushed to the tail of the buffer
 *      the packets will be sorted
 *      the sequence counter is incremented and the timeout counter is reset
 * 
 *      once the server sends a null packet
 *      the receiver knows that it received all the packets
 *      so it stops sending requests to the server and starts forming the output
 *      the packets are pushed to the output buffer from head to tail
 *      the final output is shown to the user
 */

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Project1 {

    
    public static void main(String[] args) {
        PacketReceiver receiver = new PacketReceiver();
        Server server = new Server();
        // disrupted order...
        server.addPacket(new Packet(2, "!"));
        server.addPacket(new Packet(1, "world"));
        server.addPacket(new Packet(0, "Hello "));

        //server.addPacket(new Packet(1, "Mohamed "));
        //server.addPacket(new Packet(2, "Torki "));
        //server.addPacket(new Packet(0, "Youssef "));
        //server.addPacket(new Packet(3, "Ahmed's "));
        //server.addPacket(new Packet(5, "Receiver!"));
        //server.addPacket(new Packet(4, "Packet "));

        // in order
        //List<Packet> l = server.toPackets("Hello world !", " ");
        //for(Packet p : l) server.addPacket(p);
        receiver.receiveAll();
        System.out.println("output: " + receiver.getOutput());
        receiver.flush();
    }
}

class Server{
    private static List<Packet> messageFragments;
    private static int counter;
    public Server() {
        messageFragments = new ArrayList<>();
        counter = 0;
    }

    public void addPacket(Packet p) {messageFragments.add(p);}

    public List<Packet> toPackets(String message, String delimiter) {
        String[] x = message.split(delimiter);
        List<Packet> l = new ArrayList<>();
        for(int i = 0; i < x.length; i++) {
            l.add(new Packet(i, x[i] + delimiter));
        }
        return l;
    }

    public static Packet sendNextPacket(){
        if (counter >= messageFragments.size()) return null;
        else{
            Packet p = messageFragments.get(counter);
            counter++;
            return p;
        }
    }

    public static Packet requestPacket(int sequence) {
        if (sequence >= messageFragments.size() || sequence < 0) return null;
        else{
            for(int i = 0; i < messageFragments.size(); i++) {
                if (messageFragments.get(i).sequenceNum == sequence) return messageFragments.get(i);
            }
        }
        return null;
    }

    public static void displayFragments() {
        for(int i = 0; i < messageFragments.size(); i++) System.out.print(messageFragments.get(i).payload + " ");
        System.out.println();
    }

}

class Packet{
    int sequenceNum;
    Object payload;

    public Packet(int num, Object data){
        sequenceNum = num;
        payload = data;
    }

    public int getSequence() {return sequenceNum;}
}

class PacketReceiver{

    private int expectedSequence;
    private int timeout;
    private int timeoutMax = 2;
    private List<Packet> outputBuffer;
    private LinkedList<Packet> packetBuffer;

    public boolean debugEnable = true;

    public PacketReceiver(){
        expectedSequence = timeout = 0;
        outputBuffer = new ArrayList<>();
        packetBuffer = new LinkedList<Packet>();
    }

    public void receiveAll(){
        Packet next;
        
        while ( (next = Server.sendNextPacket()) != null ) {
            
            printDebug("Received packet #" + next.sequenceNum + ", Payload: '" + next.payload + "'");
            addPacketToBuffer(next);

            if (next.sequenceNum != expectedSequence) handleLoss();
            else{
                 expectedSequence++;
                 if (timeout > 0){
                     printDebug("Missing packet #" + next.sequenceNum + " Received. resetting timeout...");
                     sortPackets();
                 }
                 timeout = 0;
            }
        }
        copyPacketBufferToOutput();
    }

    private void handleLoss(){
        timeout++;
        printDebug("Disrupted sequence... expected packet #" + expectedSequence + 
        " but received packet #" + packetBuffer.getLast().sequenceNum);

        if (timeout == timeoutMax) {
            printDebug("Timeout occurred... sending an explicit request for missing packet #" + expectedSequence);
            Packet missing = Server.requestPacket(expectedSequence);
            printDebug("Adding missing packet #" + missing.sequenceNum + " to packet buffer's tail...");
            addPacketToBuffer(missing);
            sortPackets();
            timeout = 0;
            expectedSequence++;
        }
    }

    private void copyPacketBufferToOutput(){
        printDebug("Done receiving packets... received " + packetBuffer.size() + " Total packets... forming output");
        
        while ( !packetBuffer.isEmpty() ) {
            outputBuffer.add(packetBuffer.removeFirst());
        }
    }

    public void addPacketToBuffer(Packet p) {

        if (packetExists(p.sequenceNum)){
            printDebug("Duplicate packet #" + p.sequenceNum + " Skipping...");
            return;
        }

        printDebug("Adding to buffer... packet #" + p.sequenceNum);
        packetBuffer.addLast(p);
    }

    private boolean packetExists(int sequence){

        if (sequence >= packetBuffer.size()) return false;
        
        /*for(int i = 0; i < packetBuffer.size(); i++) {
            if (packetBuffer.get(i).sequenceNum == sequence) return true;
        }*/

        int low = 0;
        int high = packetBuffer.size() - 1;
        int mid;

        while (low <= high){
            mid = (low + high) / 2;

            if (packetBuffer.get(mid).sequenceNum == sequence) return true;

            if (packetBuffer.get(mid).sequenceNum < sequence) low = mid + 1;
            else if (packetBuffer.get(mid).sequenceNum > sequence) high = mid - 1;
        }
        return false;
    }
    

    private void sortPackets(){
        printDebug("Sorting packets...");
        Collections.sort(packetBuffer, Comparator.comparingInt(Packet::getSequence));
    }

    public void flush(){
        packetBuffer = null;
        outputBuffer = null;
    }

    public String getOutput(){
        StringBuilder s = new StringBuilder();
        for(Packet p : outputBuffer) s.append(p.payload);
        return s.toString();
    }

    public void displayBufferContents(){
        for(int i = 0; i < packetBuffer.size(); i++) System.out.print(" " + packetBuffer.get(i).payload);
        System.out.println();
    }

    public void printDebug(String txt){
        if (debugEnable) System.out.println(txt);
    }
}
