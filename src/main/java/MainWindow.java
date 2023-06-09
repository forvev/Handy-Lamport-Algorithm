import lombok.Getter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MainWindow extends JFrame {

	@Getter
	private DefaultListModel<String> historyListModel;
	private JList<String> historyList;

	private JLabel snapshot1Label;
	private JButton snapshot1Button;

	private JLabel snapshot2Label;
	private JButton snapshot2Button;

	private JLabel snapshot3Label;
	private JButton snapshot3Button;
	@Getter
	private static MainWindow instance;

	Hangar hangar1;
	Hangar hangar2;
	Hangar hangar3;

	private DatagramSocket socket;
	public static void main(String[] args) {
		instance = new MainWindow();
		instance.setVisible(true);
	}

	public MainWindow() {
		setSize(400, 300);
		setTitle("TK1-EX6");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// history list
		historyListModel = new DefaultListModel<String>();
		historyList = new JList<String>(historyListModel);
		historyList.setAutoscrolls(true);

		JScrollPane historyScroll = new JScrollPane(historyList);
		add(historyScroll, BorderLayout.CENTER);

		createHangars();

		// slide panel
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new GridLayout(3, 1));

		// snapshot 1
		snapshot1Label = new JLabel("Hangar 1 (#0)");
		snapshot1Button = new JButton("Snapshot");
		snapshot1Button.addActionListener(x -> snapshot(1));

		JPanel snapshot1 = new JPanel();
		snapshot1.setLayout(new GridLayout(2, 1));
		snapshot1.add(snapshot1Label);
		snapshot1.add(snapshot1Button);
		sidePanel.add(snapshot1);

		// snapshot 2
		snapshot2Label = new JLabel("Hangar 2 (#0)");
		snapshot2Button = new JButton("Snapshot");
		snapshot2Button.addActionListener(x -> snapshot(2));

		JPanel snapshot2 = new JPanel();
		snapshot2.setLayout(new GridLayout(2, 1));
		snapshot2.add(snapshot2Label);
		snapshot2.add(snapshot2Button);
		sidePanel.add(snapshot2);

		// snapshot 3
		snapshot3Label = new JLabel("Hangar 3 (#0)");
		snapshot3Button = new JButton("Snapshot");
		snapshot3Button.addActionListener(x -> snapshot(3));

		JPanel snapshot3 = new JPanel();
		snapshot3.setLayout(new GridLayout(2, 1));
		snapshot3.add(snapshot3Label);
		snapshot3.add(snapshot3Button);
		sidePanel.add(snapshot3);

		add(sidePanel, BorderLayout.EAST);

	}

	private void snapshot(int snapshot) {
		// TODO

		historyListModel.addElement("Snapshot: "+snapshot +" initiator");
		try {
			send("start", snapshot);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	private void createHangars() {
		hangar1 = new Hangar(1);
		hangar2 = new Hangar(2);
		hangar3 = new Hangar(3);
	}

	public void send(String message, int hangarId) throws SocketException {
		socket = new DatagramSocket(null);
		try {
			socket.connect(InetAddress.getLocalHost(),hangarId+4000);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		message = hangarId+","+message;
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

