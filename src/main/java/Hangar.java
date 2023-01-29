import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Hangar extends Thread {

    private DatagramSocket socket;
    private byte[] buf = new byte[1024];
    private final int id;
    private LinkedList<Airplane> airplanes = new LinkedList<>();

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

    private void processMessage(String message) {
        String[] afterSplit = message.split(",");
        int senderId = Integer.parseInt(afterSplit[0]);
        int receivedAirplanes = (afterSplit.length - 1);
        String logMessage = "Transfer: H"+senderId + "-> H" + id + " (" + receivedAirplanes +")";
        for(int i = 1; i<afterSplit.length; i++) {
            String airplaneId = afterSplit[i];
            System.out.println(logMessage);
            airplanes.add(new Airplane(airplaneId));
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
                processMessage(received);
            } catch (IOException e) {
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
