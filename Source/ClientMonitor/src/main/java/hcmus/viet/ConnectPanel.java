/*
 * Created by JFormDesigner on Thu May 04 21:40:09 ICT 2023
 */

package hcmus.viet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vietd
 */
public class ConnectPanel {
    private final ChatClient chatClient;

    public static void main(String[] args) {
        new ConnectPanel();
    }
    public ConnectPanel() {
        initComponents();
        setDefaultServer();
        chatClient = new ChatClient();
        scrollPane1.setVisible(false);
        logTextArea.setVisible(false);
        connectFrame.setVisible(true);
        connectFrame.setTitle("Client Monitor - Connect");
        connectFrame.setResizable(false);
        logTextArea.setEditable(false);
    }

    private void setDefaultServer() {
        ipTextField.setText("127.0.0.1");
        portTextField.setText("8080");
    }

    private void portTextFieldKeyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
            e.consume(); // if it's not a number, ignore the event
        }
    }

    private boolean isValidIp(String ip) {
        Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private boolean isValidPort(int port) {
        return port > 1000 && port < 10000;
    }

    private void connectBtn(ActionEvent e) {
        String getPort = portTextField.getText();
        String ip = ipTextField.getText();
        if (!getPort.isEmpty() && !ip.isEmpty()) {
            int port = Integer.parseInt(getPort);

            if (isValidIp(ip) && isValidPort(port)) {
                try {
                    chatClient.startClient(ip, port);
                    connectFrame.setTitle("Client Monitor - Connected");
                    connectPanel.setVisible(false);
                    scrollPane1.setVisible(true);
                    logTextArea.setVisible(true);
                    System.out.println("Connected to server: " + ip + ":" + port);

                    chatClient.readMessage(logTextArea);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(connectFrame, "An error occurred while connecting: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(connectFrame, "Port in range 1000 to 10 000!");
            }
        }
    }

    public static void addLog(String text) {
        logTextArea.append(text);
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    public static void showNotification(String message) {
        JOptionPane.showMessageDialog(null, message, "Notification", JOptionPane.INFORMATION_MESSAGE);
    }

    private void connectFrameWindowClosing(WindowEvent e) {
        if (chatClient.getState()) {
            int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                chatClient.sendMessage("logout");
                chatClient.stopClient();
                System.exit(0); // Terminate the application
            } else {
                // Cancel the window closing event
                connectFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        } else {
            System.exit(0); // Terminate the application if the chat client is not in the desired state
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - Viet Nguyen Duc
        connectFrame = new JFrame();
        connectPanel = new JPanel();
        ipLabel = new JLabel();
        portLabel = new JLabel();
        portTextField = new JTextField();
        connectBtn = new JButton();
        ipTextField = new JTextField();
        scrollPane1 = new JScrollPane();
        logTextArea = new JTextArea();

        //======== connectFrame ========
        {
            connectFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    connectFrameWindowClosing(e);
                }
            });
            Container connectFrameContentPane = connectFrame.getContentPane();

            //======== connectPanel ========
            {
                connectPanel.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(
                0,0,0,0), "JF\u006frmDes\u0069gner \u0045valua\u0074ion",javax.swing.border.TitledBorder.CENTER,javax.swing.border.TitledBorder
                .BOTTOM,new java.awt.Font("D\u0069alog",java.awt.Font.BOLD,12),java.awt.Color.
                red),connectPanel. getBorder()));connectPanel. addPropertyChangeListener(new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.
                beans.PropertyChangeEvent e){if("\u0062order".equals(e.getPropertyName()))throw new RuntimeException();}});

                //---- ipLabel ----
                ipLabel.setText("IP Address:");

                //---- portLabel ----
                portLabel.setText("Port Number:");

                //---- portTextField ----
                portTextField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        portTextFieldKeyTyped(e);
                    }
                });

                //---- connectBtn ----
                connectBtn.setText("Connect");
                connectBtn.addActionListener(e -> connectBtn(e));

                GroupLayout connectPanelLayout = new GroupLayout(connectPanel);
                connectPanel.setLayout(connectPanelLayout);
                connectPanelLayout.setHorizontalGroup(
                    connectPanelLayout.createParallelGroup()
                        .addGroup(connectPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(connectPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(portLabel)
                                .addComponent(ipLabel))
                            .addGap(18, 18, 18)
                            .addGroup(connectPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(portTextField)
                                .addComponent(ipTextField, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, connectPanelLayout.createSequentialGroup()
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(connectBtn)
                            .addContainerGap())
                );
                connectPanelLayout.setVerticalGroup(
                    connectPanelLayout.createParallelGroup()
                        .addGroup(connectPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(connectPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(ipLabel)
                                .addComponent(ipTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(connectPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(portLabel)
                                .addComponent(portTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(connectBtn)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(logTextArea);
            }

            GroupLayout connectFrameContentPaneLayout = new GroupLayout(connectFrameContentPane);
            connectFrameContentPane.setLayout(connectFrameContentPaneLayout);
            connectFrameContentPaneLayout.setHorizontalGroup(
                connectFrameContentPaneLayout.createParallelGroup()
                    .addGroup(connectFrameContentPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(connectFrameContentPaneLayout.createParallelGroup()
                            .addComponent(connectPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(scrollPane1))
                        .addContainerGap())
            );
            connectFrameContentPaneLayout.setVerticalGroup(
                connectFrameContentPaneLayout.createParallelGroup()
                    .addGroup(connectFrameContentPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(connectPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                        .addContainerGap())
            );
            connectFrame.pack();
            connectFrame.setLocationRelativeTo(connectFrame.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Viet Nguyen Duc
    private JFrame connectFrame;
    private JPanel connectPanel;
    private JLabel ipLabel;
    private JLabel portLabel;
    private JTextField portTextField;
    private JButton connectBtn;
    private JTextField ipTextField;
    private JScrollPane scrollPane1;
    private static JTextArea logTextArea;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
