import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Hangar extends Thread {

    private DatagramSocket socket;
    private byte[] buf = new byte[1024];
    private final int id;
    private LinkedList<Airplane> airplanes = new LinkedList<>();
    private ArrayList<Airplane> received_messages = new ArrayList<>();
    private boolean hasnt_recorded_its_state=false, recorded_its_state=false;

    private int finished_counter=0;


    public Hangar(int id) {
        this.id = id;
        try {
            socket = new DatagramSocket(id+4000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        initiateAirplanes();
        start();
        transferTask();
    }

    private void initiateAirplanes() {
        for(int i = 0; i<10; i++) {
            airplanes.add(new Airplane());
        }
    }

    private void processMessage(String message) throws InterruptedException {
        //message = id of the sender, list of the transferred airplanes
        String[] afterSplit = message.split(",");
        int senderId = Integer.parseInt(afterSplit[0]);
        int receivedAirplanes = (afterSplit.length - 1);
        String logMessage = "Transfer: H"+senderId + "-> H" + id + " (" + receivedAirplanes +")";
        System.out.println(logMessage);

        String my_message = afterSplit[1];

        MainWindow.getInstance().getHistoryListModel().addElement(logMessage);

        //if (my_message.equals("start")) System.out.println("Snapshot: "+id +" initiator");

        //if the recording is finished in different processes increment the counter
        //Once the counter will be equal 2 we are sure that the all of the processes are finished and we can start a new snapshot
        if(my_message.equals("finished")){
            finished_counter++;
            if(finished_counter==2){
                hasnt_recorded_its_state=false;
                recorded_its_state=false;
            }
        }

        //once the process has already started its recording and there is another mark that has arrived (like record or start) it should stop recording
        if (hasnt_recorded_its_state == true && recorded_its_state == false && (my_message.equals("record") || my_message.equals("start"))){
            System.out.println("recorded messages:"+received_messages);
            System.out.println("stop recording inside hangar: "+id);
            for (Airplane airplane: received_messages){
                MainWindow.getInstance().getHistoryListModel().addElement("Recorded airplane inside hangar: "+id+" :"+airplane.getId());
            }
            received_messages.clear();
            for (int i=1;i<=3;i++) {
                if (i == id) continue;
                send("finished,",i);
            }
            recorded_its_state = true;
        }
        //if there is a new mark and the process hasn't recorded its state yet start recording
        else if ((my_message.equals("record") || my_message.equals("start")) && hasnt_recorded_its_state==false && recorded_its_state==false){
            hasnt_recorded_its_state = true;
            //it is needed for the future snapshots (the counter may be equal 2 form the previous recordings
            finished_counter=0;

            //save arrived messages during recoding process
//            for(int i = 1; i<afterSplit.length; i++) {
//                String airplaneId = afterSplit[i];
//                received_messages.add(new Airplane(airplaneId));
//            }

            System.out.println("Start recording inside hangar: "+id);
            Thread.sleep(2000);
            //send marks to all processes
            for (int i=1;i<=3;i++){
                if(i==id) continue;
                System.out.println("sending to: "+i+" from: "+id);
                MainWindow.getInstance().getHistoryListModel().addElement("Mark: H"+id + "-> H" + i);
                System.out.println("Mark: H"+id + "-> H" + i);
                send("record",i);
            }
        }

        for(int i = 1; i<afterSplit.length; i++) {
            String airplaneId = afterSplit[i];
            airplanes.add(new Airplane(airplaneId));
        }


        if (hasnt_recorded_its_state==true && recorded_its_state==false && !(my_message.equals("record") || my_message.equals("start"))){
            for(int i = 1; i<afterSplit.length; i++) {
                String airplaneId = afterSplit[i];
                Airplane airplane = new Airplane(airplaneId);
                System.out.println("recorded: "+airplane.getId()+" inside id: "+id);
                received_messages.add(new Airplane(airplaneId));
            }
        }
    }

    private void transferTask() {
        Random random = new Random();
        new Thread(() -> {
            while(true) {
                int delay = random.nextInt(3) + 1;
                try {
                    Thread.sleep(delay * 1000);
                    transferAirplanes();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void transferAirplanes() {
        Random random = new Random();
        //calculate the number of airplanes that we want to transfer
        int toTransfer = Math.min(random.nextInt(4) + 1, airplanes.size());
        int current = random.nextInt(toTransfer + 1);
        for(int i = 1; i<=3; i++) {
            if(i == id) continue;
            sendAirplanesToHangar(i, current);
            current = toTransfer - current;
        }
    }

    private void sendAirplanesToHangar(int hangarId, int amount) {
        while(amount > 1) {
            String message = String.join(",", airplanes.stream().limit(amount).map(Airplane::getId)
                    .collect(Collectors.toList()));

            send(message, hangarId);
            amount--;
        }
    }

    @Override
    public void run() {
        while(true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                //System.out.println("received from: "+received+ " inside hangar: "+id);
                //once it receives the message to the socket it will display a message
                processMessage(received);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send(String message, int hangarId) {
        message = id+","+message;
        byte[] buf = message.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), hangarId+4000);
            socket.send(packet);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
