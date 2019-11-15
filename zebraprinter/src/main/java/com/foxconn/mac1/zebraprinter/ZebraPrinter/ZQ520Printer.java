package com.foxconn.mac1.zebraprinter.ZebraPrinter;


import com.foxconn.mac1.zebraprinter.Entity.ResultObj;
import com.foxconn.mac1.zebraprinter.Entity.ZeroSymbolBill;
import com.foxconn.mac1.zebraprinter.Utils.Utils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ZQ520 Printer
 */
public class ZQ520Printer {

    private Connection connection = null;
    private boolean isConnByBluetooth = true;

    private String bluetoothMacAddress = "AC:3F:A4:E4:D6:3F";
    private String tcpAddress = "127.0.0.1";
    private Integer tcpPortNumber = 0;

    private String printerStatusMsg = "";

    public ResultObj zeroSymbolBill() {

        if (isConnByBluetooth) {
            connection = new BluetoothConnection(bluetoothMacAddress);
        } else {
            try {
                connection = new TcpConnection(tcpAddress, tcpPortNumber);
            } catch (NumberFormatException e) {
                return new ResultObj(false, "Exc-01: Tcp connection open failed");
            }
        }

        try {
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
            PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();

            if (printerStatus.isReadyToPrint)
                sendToPrint(printer);
            if (printerStatus.isHeadOpen)
                printerStatusMsg = "Err-01: Head Open! \n Please close Printer Head to print. ";
            if (printerStatus.isHeadCold)
                printerStatusMsg = "Err-02: Head Cold! \n Please try again. ";
            if (printerStatus.isHeadTooHot)
                printerStatusMsg = "Err-03: Head too hot! \n Please do it later. ";
            if (printerStatus.isPaperOut)
                printerStatusMsg = "Err-04: Media Out! \n Please load Media to Print. ";
            if (printerStatus.isPartialFormatInProgress)
                printerStatusMsg = "Err-05: Head Open! \n Please try again later. ";
            if (printerStatus.isPaused)
                printerStatusMsg = "Err-06: Printer Paused. ";
            if (printerStatus.isReceiveBufferFull)
                printerStatusMsg = "Err-07: Buffer full! \n Please do it later. ";
            if (printerStatus.isRibbonOut)
                printerStatusMsg = "Err-08: Ribbon Out! \n Please retry after adjustment. ";

            connection.close();

            if (!("".equals(printerStatusMsg)))
                return new ResultObj(false, printerStatusMsg);
            return new ResultObj(true);
        } catch (Exception e) {
            return new ResultObj(false, "Exc-02: " + e.getMessage());
        } finally {

        }
    }


    private void sendToPrint(ZebraPrinter printer) {
        String filename = "TEMP.LBL";
        try {
            File file = new File(filename);
            if (!file.exists())
                file.createNewFile();
            createZPLFile(printer, filename);
            printer.sendFileContents(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    private void createZPLFile(ZebraPrinter printer, String filename) throws IOException {

        byte[] zplByte = null;
        FileOutputStream os = new FileOutputStream(filename);

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        if (printerLanguage == printerLanguage.ZPL) {

            String zpl = buildZPLTemplate(new ZeroSymbolBill());
            zplByte = zpl.getBytes();
        }
        os.write(zplByte);
        os.flush();
        os.close();
    }

    private String buildZPLTemplate(ZeroSymbolBill zsb) {

        final String formName = "Zero Symbol Bill";

        zsb.setNewGuid(Utils.GUID());

        StringBuilder sb = new StringBuilder();

        sb.append("^XA\n")
                .append("^LL320\n")
                .append("^PW400\n")
                .append("^LH0,0\n")
                .append("^CI26\n")
                .append("^SEE:GB18030.DAT\n")

                .append("^FO48,20^AEN,10,10^FD" + formName + "^FS\n")//Zero Symbol Bill
                .append("^FO24,62^AEN,10,10^FDP/N:^FS\n")//P/N
                .append("^FO54,102^AEN,10,10^FD" + zsb.getPn() + "\n")//2T459M000-000-G5
                .append("^FO24,142^AEN,10,10^FDQTY:^FS\n")//QTY
                .append("^FO54,182^AEN,10,10^FD2" + zsb.getQty() + " PCS^FS\n")//200000
                .append("^FO24,222^AEN,10,10^FDDATE:^FS\n")//DATE
                .append("^FO54,264^AEN,10,10^FD" + zsb.getDate() + "^FS\n")//20191107

                .append("^FO280,172^BQN,2,2^FD\n")
                .append("   " + zsb.getType()
                        + "," + zsb.getNewGuid()
                        + "," + zsb.getVar1()
                        + "," + zsb.getPn()
                        + "," + zsb.getDate()
                        + "," + zsb.getVar2() + "," + zsb.getQty() + ",PCS," + zsb.getOldGuid() + "^FS\n")

                .append("^XZ");

        return sb.toString();
    }
}
