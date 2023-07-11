package hcmus.viet;

import java.awt.event.*;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * @author vietd
 */
public class MainFrame {
    static DefaultTableModel clientModel;
    public DefaultTableModel monitorModel;
    private String previousSelected;
    private final Map<ClientHandler, String> watchList;
    private Timer timer;

    public static boolean isExists;

    public static void main(String[] args) {
        new MainFrame();
    }
    public MainFrame() {
        initComponents();
        setIpCbb();
        portTf.setText("8080");
        setClientTable();
        setMonitorTable();
        mainframe.setResizable(false);
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainframe.setVisible(true);
        previousSelected = "None";
        watchList = new HashMap<>();
        mainframe.setTitle("Server Monitor");
        isExists = false;
    }

    private void setIpCbb() {
        try {
            ipCbb.addItem("127.0.0.1");
            InetAddress localHost = InetAddress.getLocalHost();
            ipCbb.addItem(localHost.getHostAddress());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    private void setClientTable() {
        clientModel = (DefaultTableModel) clientTable.getModel();
        clientModel.addColumn("Active Client Name");
        clientTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering
        clientTable.setDefaultEditor(Object.class, null);
    }

    private void setMonitorTable() {
        monitorModel = (DefaultTableModel) monitorTable.getModel();
        monitorModel.addColumn("Date");
        monitorModel.addColumn("Time");
        monitorModel.addColumn("Operation");
        monitorModel.addColumn("Detail");
        monitorTable.getTableHeader().setReorderingAllowed(false);

        monitorTable.getColumnModel().getColumn(0).setPreferredWidth(60); // Adjust the width as needed
        monitorTable.getColumnModel().getColumn(1).setPreferredWidth(60); // Adjust the width as needed
        monitorTable.getColumnModel().getColumn(2).setPreferredWidth(60); // Adjust the width as needed
        monitorTable.getColumnModel().getColumn(3).setPreferredWidth(250); // Adjust the width as needed
    }

    private void showNotification(String message) {
        JOptionPane.showMessageDialog(mainframe, message, "Notification", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String showInputTextDialog(String message, String title) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
    }
    public static void removeRow(DefaultTableModel tableModel, String value) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(value)) {
                tableModel.removeRow(i);
            }
        }
    }

    private Timer startMessageLogUpdateTimer(ClientHandler handler) {
        int delay = 1000; // Delay in milliseconds (adjust as needed)
        ActionListener taskPerformer = e -> updateMonitorTableIfNecessary(handler);
        Timer timer = new Timer(delay, taskPerformer);
        timer.start();
        return timer;
    }

    private void updateMonitorTableIfNecessary(ClientHandler handler) {
        if (handler != null && handler.getMessageLog().size() > monitorModel.getRowCount()) {
            monitorModel.setNumRows(0); // Clear the existing rows
            handler.getMessageLog().forEach(message -> monitorModel.addRow((Object[]) message));
        }
    }

    private ClientHandler getClientHandler(String selectedClient) {
        for (ClientHandler handler : Server.clientHandlerList) {
            if (Objects.equals(handler.getName(), selectedClient)) {
                return handler;
            }
        }
        return null;
    }

    private void connectBtn(ActionEvent e) {
        connectBtn.setEnabled(false);
        portTf.setEditable(false);
        ipCbb.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    int port = Integer.parseInt(portTf.getText());
                    String ip = (String) ipCbb.getSelectedItem();
                    showNotification("Server is starting on port " + port);
                    Server server = new  Server(ip, port);
                    Thread serverThread = new Thread(server);
                    serverThread.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        };

        worker.execute();
    }

    private void clientTableMouseClicked(MouseEvent e) {
        // Get selected client on the clients list
        String selectedClient = (String) clientTable.getValueAt(clientTable.getSelectedRow(), 0);
        if (selectedClient.equals(previousSelected) && !previousSelected.isEmpty()) {
            return;
        }
        previousSelected = selectedClient;

        // Get that handler for selected client
        ClientHandler handler = getClientHandler(selectedClient);
        currentLb.setText("Current Selected: " + selectedClient);

        // Stop timer to stop update monitor table and reset the table
        if (this.timer != null) {
            this.timer.stop();
        }
        monitorModel.setNumRows(0);

        // if contains this handler
        if (handler != null) {
            this.timer = startMessageLogUpdateTimer(handler);
            handler.getMessageLog().forEach(message -> monitorModel.addRow((Object[]) message));
        }

        // if this handler haven't selected the folder to watch before
        if (!watchList.containsKey(handler)) {
            String userInput = showValidFolderPathDialog(handler, "Enter the directory path to watch:");
            selectedFolder.setText("Selected Folder: " + userInput);
        }
    }

    private String showValidFolderPathDialog(ClientHandler handler, String message) {
        String title = "Folder Watcher";
        String userInput = null;
        String regex = "^(?:[a-zA-Z]:)?[\\\\\\/](?:(?![\\\\\\/]|\\.$|\\.\\.$)[^\\\\\\/\\r\\n])+([\\\\\\/](?:(?![\\\\\\/]|\\.$|\\.\\.$)[^\\\\\\/\\r\\n])+)*$";
        boolean isValidPath = false;

        while (!isValidPath) {
            userInput = showInputTextDialog(message, title);
            isValidPath = Pattern.matches(regex, userInput);

            if (!isValidPath) {
                userInput = ""; // Reset userInput to trigger re-prompting
            } else {
                System.out.println("Send path to the client: " + userInput);
                handler.sendMessage("1#" + userInput);

                try {
                    Thread.sleep(500); // Pause for 0.5 seconds waits for the respond
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (handler.isExists) {
                    watchList.put(handler, userInput);
                    System.out.println("Watching directory: " + userInput);
                } else {
                    showNotification("This folder does not exist " + userInput + "!");
                    isValidPath = false; // Prompt for input again
                }
            }
        }

        return userInput;
    }

    private void initComponents() {
        mainframe = new JFrame();
        label1 = new JLabel();
        label2 = new JLabel();
        scrollPane1 = new JScrollPane();
        clientTable = new JTable();
        ipCbb = new JComboBox<>();
        portTf = new JTextField();
        connectBtn = new JButton();
        scrollPane2 = new JScrollPane();
        monitorTable = new JTable();
        currentLb = new JLabel();
        selectedFolder = new JLabel();

        //======== mainframe ========
        {
            Container mainframeContentPane = mainframe.getContentPane();

            mainframe.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Perform your action here
                    System.out.println("Window is closing...");
                    System.exit(0);
                }
            });

            //---- label1 ----
            label1.setText("IP Address:");

            //---- label2 ----
            label2.setText("Port Number:");

            //======== scrollPane1 ========
            {

                //---- clientTable ----
                clientTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        clientTableMouseClicked(e);
                    }
                });
                scrollPane1.setViewportView(clientTable);
            }

            //---- connectBtn ----
            connectBtn.setText("Start");
            connectBtn.addActionListener(e -> connectBtn(e));

            //======== scrollPane2 ========
            {
                scrollPane2.setViewportView(monitorTable);
            }

            //---- currentLb ----
            currentLb.setText("Current Selected: None");
            currentLb.setFont(currentLb.getFont().deriveFont(currentLb.getFont().getStyle() | Font.BOLD, currentLb.getFont().getSize() + 3f));

            //---- selectedFolder ----
            selectedFolder.setText("Selected Folder:");
            selectedFolder.setFont(selectedFolder.getFont().deriveFont(selectedFolder.getFont().getStyle() | Font.BOLD, selectedFolder.getFont().getSize() + 3f));

            GroupLayout mainframeContentPaneLayout = new GroupLayout(mainframeContentPane);
            mainframeContentPane.setLayout(mainframeContentPaneLayout);
            mainframeContentPaneLayout.setHorizontalGroup(
                    mainframeContentPaneLayout.createParallelGroup()
                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(mainframeContentPaneLayout.createParallelGroup()
                                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(scrollPane2))
                                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                                    .addGroup(mainframeContentPaneLayout.createParallelGroup()
                                                            .addGroup(GroupLayout.Alignment.TRAILING, mainframeContentPaneLayout.createSequentialGroup()
                                                                    .addComponent(label1)
                                                                    .addGap(18, 18, 18))
                                                            .addGroup(GroupLayout.Alignment.TRAILING, mainframeContentPaneLayout.createSequentialGroup()
                                                                    .addComponent(label2)
                                                                    .addGap(5, 5, 5)))
                                                    .addGroup(mainframeContentPaneLayout.createParallelGroup()
                                                            .addComponent(portTf, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
                                                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                                                    .addComponent(ipCbb, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(18, 18, 18)
                                                                    .addComponent(connectBtn)))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 403, Short.MAX_VALUE)
                                                    .addGroup(mainframeContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                                                    .addComponent(currentLb)
                                                                    .addGap(57, 57, 57))
                                                            .addComponent(selectedFolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                    .addContainerGap())
            );
            mainframeContentPaneLayout.setVerticalGroup(
                    mainframeContentPaneLayout.createParallelGroup()
                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                    .addGap(5, 5, 5)
                                    .addGroup(mainframeContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label1)
                                            .addComponent(ipCbb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(connectBtn)
                                            .addComponent(currentLb))
                                    .addGroup(mainframeContentPaneLayout.createParallelGroup()
                                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                                    .addGap(14, 14, 14)
                                                    .addGroup(mainframeContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(label2)
                                                            .addComponent(portTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(mainframeContentPaneLayout.createSequentialGroup()
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(selectedFolder)))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(mainframeContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                            .addComponent(scrollPane2)
                                            .addComponent(scrollPane1))
                                    .addGap(23, 23, 23))
            );
            mainframe.pack();
            mainframe.setLocationRelativeTo(mainframe.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Viet Nguyen Duc
    private JFrame mainframe;
    private JLabel label1;
    private JLabel label2;
    private JScrollPane scrollPane1;
    private JTable clientTable;
    private JComboBox<String> ipCbb;
    private JTextField portTf;
    private JButton connectBtn;
    private JScrollPane scrollPane2;
    private JTable monitorTable;
    private JLabel currentLb;
    private JLabel selectedFolder;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}