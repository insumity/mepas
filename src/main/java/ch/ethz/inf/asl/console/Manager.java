package ch.ethz.inf.asl.console;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static java.sql.Types.*;

/**
 * Class that corresponds to a GUI manager. With this manager a user can see the state
 * of the tables: client, queue and message from a database.
 */
public class Manager extends JFrame {

    // text fields used for login
    private final JTextField host = new JTextField(20);
    private final JTextField port = new JTextField(10);
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JTextField databaseName = new JTextField(20);
    private final JButton loginButton = new JButton("Login");

    // corresponds to whether the user has connected to a database
    private Connection connection;
    private boolean isConnected = false;


    public Manager() {
        super("Management Console");

        setBounds(250, 250, 850, 750);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));


        try {
            JPanel databaseCredentialsPanel = createDatabaseCredentialsPanel();
            add(databaseCredentialsPanel);

            JPanel clientsPanel = createPanel("Clients", "client", new int[]{INTEGER, VARCHAR}, new String[]{"id", "name"});
            JPanel queuesPanel = createPanel("Queues", "queue", new int[]{INTEGER, VARCHAR}, new String[]{"id", "name"});
            JPanel messagesPanel = createPanel("Messages", "message",
                    new int[]{INTEGER, INTEGER, INTEGER, INTEGER, TIMESTAMP, VARCHAR},
                    new String[]{"id", "sender_id", "receiver_id", "queue_id", "arrival_time", "message"}
            );

            add(clientsPanel);
            add(queuesPanel);
            add(messagesPanel);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    private void getConnection() throws SQLException {
        if (isConnected) {
            return;
        }

        int portNumber = Integer.valueOf(port.getText());
        String URL = "jdbc:postgresql://" + host.getText() + ":" + portNumber + "/" + databaseName.getText();
        connection = DriverManager.getConnection(URL, username.getText(), String.valueOf(password.getPassword()));

        // if it arrives here, no exception is throw, it means it got connected
        isConnected = true;
        loginButton.setEnabled(false);
    }

    private Object[][] getRowsFromTable(String name, int[] typeOfFields, String[] fieldsNames) throws SQLException {

        if (!isConnected) {
            return new Object[0][fieldsNames.length];
        }

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(String.format("SELECT * FROM %s", name));

        List<Object[]> rows = new LinkedList<>();

        int numberOfRows = 0;
        while (rs.next()) {
            Object[] row = new Object[fieldsNames.length];
            for (int i = 0; i < fieldsNames.length; ++i) {
                if (typeOfFields[i] == INTEGER) {
                    row[i] = rs.getInt(fieldsNames[i]);
                } else if (typeOfFields[i] == VARCHAR) {
                    row[i] = rs.getString(fieldsNames[i]);
                } else if (typeOfFields[i] == TIMESTAMP) {
                    row[i] = rs.getTimestamp(fieldsNames[i]);
                }
            }
            rows.add(row);
            numberOfRows++;
        }

        return rows.toArray(new Object[numberOfRows][fieldsNames.length]);
    }

    private DefaultTableModel refreshData(final String tableName, final int[] typeOfFields, final String[] fieldsNames) throws SQLException, ClassNotFoundException {
        Object[][] data = getRowsFromTable(tableName, typeOfFields, fieldsNames);

        final DefaultTableModel defaultTableModel = new DefaultTableModel(data, fieldsNames);

        int rowsInTheTableModel = defaultTableModel.getRowCount();
        for (int i = rowsInTheTableModel - 1; i >= 0; --i)	{
            defaultTableModel.removeRow(i);
        }

        for (int i = 0; i < data.length; ++i)	{
            defaultTableModel.addRow(data[i]);
        }

        return defaultTableModel;
    }

    private JPanel createPanel(final String borderTitle, final String tableName, final int[] typeOfFields, final String[] fieldsNames) throws SQLException, ClassNotFoundException	{

        final DefaultTableModel defaultTableModel = refreshData(tableName, typeOfFields, fieldsNames);

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        final TitledBorder titledBorder = BorderFactory.createTitledBorder(borderTitle + "(0)");
        final CompoundBorder border = BorderFactory.createCompoundBorder(
                titledBorder,
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBorder(border);

        final JTable table = new JTable();
        table.setModel(defaultTableModel);
        table.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        JButton refreshButton = new JButton("Refresh");

        refreshButton.addActionListener(new ActionListener()	{

            @Override
            public void actionPerformed(ActionEvent e)
            {
                try {
                    DefaultTableModel dtm = refreshData(tableName, typeOfFields, fieldsNames);
                    int numberOfRows = dtm.getRowCount();
                    table.setModel(dtm);
                    table.setEnabled(false);
                    titledBorder.setTitle(borderTitle + "(" + numberOfRows + ")");
                    panel.updateUI();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });

        panel.add(refreshButton);
        panel.add(scrollPane);

        return panel;
    }

    Box createHelperVerticalBox(String label, JTextField field) {
        Box vertical = Box.createVerticalBox();
        vertical.add(new JLabel(label));
        vertical.add(field);
        return vertical;
    }

    JPanel createDatabaseCredentialsPanel() throws SQLException, ClassNotFoundException	{

        JPanel dbInformationPanel = new JPanel();
        dbInformationPanel.setLayout(new BorderLayout());
        dbInformationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Database Credentials"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        Box credentials = Box.createHorizontalBox();
        credentials.add(createHelperVerticalBox("Host", host));
        credentials.add(createHelperVerticalBox("Port", port));
        credentials.add(createHelperVerticalBox("Username", username));
        credentials.add(createHelperVerticalBox("Password", password));
        credentials.add(createHelperVerticalBox("Database Name", databaseName));

        final JLabel loginMessage = new JLabel(" ");
        Box loginBox = Box.createVerticalBox();
        loginBox.add(new JLabel(" ")); // needed so "Login" button is at the correct place
        loginBox.add(loginButton);
        loginButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getConnection();

                    loginMessage.setText("Successfully connected!");
                    loginMessage.setForeground(Color.GREEN);
                    isConnected = true;
                } catch (IllegalArgumentException iae) {
                    loginMessage.setText("Missing credentials! Please try again!");
                    loginMessage.setForeground(Color.RED);
                    iae.printStackTrace();
                }
                catch (SQLException se) {
                    loginMessage.setText("Couldn't connect! Try again!");
                    loginMessage.setForeground(Color.RED);
                    se.printStackTrace();
                }
                catch (Exception e2) {
                    loginMessage.setText("There was an error! Close the application and try again!");
                    loginMessage.setForeground(Color.RED);
                    e2.printStackTrace();
                }
            }
        });
        credentials.add(loginBox);

        Box credentialsBox = Box.createVerticalBox();
        credentialsBox.add(credentials);
        credentialsBox.add(loginMessage);
        dbInformationPanel.add(credentialsBox);

        return dbInformationPanel;
    }

    public static void main(String[] args) {
        new Manager();
    }
}