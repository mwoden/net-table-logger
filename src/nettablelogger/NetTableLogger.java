/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettablelogger;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 *
 * @author Mark
 */
public class NetTableLogger {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NetworkTable table = NetworkTable.getTable("dataLog");
    }

}
