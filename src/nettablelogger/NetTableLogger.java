/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettablelogger;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mark
 */
public class NetTableLogger implements ITableListener {

    private NetworkTable table;
    private BufferedWriter outputFile = null;

    private final DateFormat TimeStampFormat = new SimpleDateFormat("MM/dd/yyyy\tHH:mm:ss.SSS");
    private final String FileHeader = "Date\tTime\tKey\tValue";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length != 3) {
            printUsage();
            return;
        }

        try {
            InetAddress ipAddress = getAddressFromTeamNumber(Integer.parseInt(args[1]));
            new NetTableLogger().run(args[0], ipAddress, args[2]);
        }
        catch (java.net.UnknownHostException ex) {
            Logger.getLogger(NetTableLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage() {
        System.out.println("NetTableLogger <table name> <team number> <output directory>");
    }

    private static InetAddress getAddressFromTeamNumber(int teamNumber) throws java.net.UnknownHostException {

        byte[] ipBytes = new byte[]{10, (byte) (teamNumber / 100), (byte) (teamNumber % 100), 2};

        return InetAddress.getByAddress(ipBytes);
    }

    private void setup(InetAddress ipAddress, String tableName, Path path) {

        try {
            outputFile = Files.newBufferedWriter(path, Charset.defaultCharset(), StandardOpenOption.CREATE);
            outputFile.write(FileHeader);
            outputFile.newLine();

            NetworkTable.setClientMode();
            String address = ipAddress.toString().substring(1);
            NetworkTable.setIPAddress(address);
            table = NetworkTable.getTable(tableName);
            table.putBoolean("RecordData", false);
            table.addTableListener(this);
        }
        catch (IOException ex) {
            outputFile = null;
        }
    }

    private void run(String tableName, InetAddress ipAddress, String outputDirectory) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        Date date = new Date();
        String fileName = dateFormat.format(date) + ".txt";

        Path newPath = java.nio.file.Paths.get(outputDirectory, fileName);
        setup(ipAddress, tableName, newPath);

        if (outputFile == null) {
            System.out.println("Bad output directory: " + outputDirectory);
            return;
        }

        // Flush the file every second
        while (true) {
            try {
                try {
                    outputFile.flush();
                }
                catch (IOException ex) {
                }

                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {
                Logger.getLogger(NetTableLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void valueChanged(ITable itable, String string, Object o, boolean firstTime) {

        String timeStamp = TimeStampFormat.format(new Date());

        try {
            outputFile.write(timeStamp + "\t" + string + "\t" + o);
            outputFile.newLine();
        }
        catch (IOException ex) {
        }
    }
}
