package com.foxconn.mac1.zebraprinter.ZebraPrinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Set;

/**
 * ZQ520 Printer
 */
public class ZQ520Printer {

    private Context context;
    private Connection connection = null;
    private boolean isConnByBluetooth = true;
    private String bluetoothMacAddress = "";
    private String tcpAddress = "127.0.0.1";
    private Integer tcpPortNumber = 0;
    private String errMsg = "";
    private final String ip = "";
    private final String mac = this.getLocalMacAddress();
    private final String appName = null;
    private final String appVersionName = null;
    private final String appVersionCode = null;
    private final String arrName;
    private final String arrVersionName;
    private final String arrVersionCode;

    public ZQ520Printer(Context context) {
        this.context = context;
//        this.appName = PackageUtils.getApplicationName(this.context);
//        this.appVersionName = PackageUtils.getVersionName(this.context);
//        this.appVersionCode = PackageUtils.getVersionCode(this.context);
        this.arrName = "ZebraPrinter";
        this.arrVersionName = "1.0";
        this.arrVersionCode = "1";
    }

    /**
     * ZeroSymbolBill Printer
     *
     * @param codeMsg  BQ code message.
     *                 Example:
     *                 W,NEW00182631190729C0001,P2a-J60102,2T459M000-000-G5,20190729,WmL-J76036,2000,PCS,VCN00182631190729C0001
     * @param splitQty split quantity
     * @return
     */
    public ResultObj zeroSymbolBill(String codeMsg, Integer splitQty) {

        String[] codeMsgArr = codeMsg.split(",");
        ArrayList<ZeroSymbolBill> zsbs = new ArrayList<>();

        /* BQ code message before split. include split quantity */
        ZeroSymbolBill zsb = new ZeroSymbolBill();

        try {
            zsb.setType(codeMsgArr[0].trim());     // type.
            zsb.setNewGuid(codeMsgArr[1].trim());  // current guid.    -- after split. It will become the old guid
            zsb.setVar1(codeMsgArr[2].trim());     //
            zsb.setPn(codeMsgArr[3].trim());       // p/n
            zsb.setDate(codeMsgArr[4].trim());     // date
            zsb.setVar2(codeMsgArr[5].trim());     //
            zsb.setQty(Integer.valueOf(codeMsgArr[6].trim())); // quantity
            zsb.setUnit(codeMsgArr[7].trim());     // unit
            zsb.setSplitQty(splitQty);
        } catch (NumberFormatException e) {
            return new ResultObj(false, "Exc-03: " + e.getMessage());
        }

        if (zsb.getSplitQty() >= zsb.getQty())
            return new ResultObj(false, "the split quantity must be less than quantity");

        /* BQ code message after split. return an arrayList */
        zsbs.add(new ZeroSymbolBill(
                zsb.getType(),
                Utils.GUID(), // set new guid for BQ code after split
                zsb.getVar1(),
                zsb.getPn(),
                zsb.getDate(),
                zsb.getVar2(),
                zsb.getSplitQty(),// first split.    quantity = split quantity
                zsb.getUnit(),
                zsb.getNewGuid(),   // become the old guid after split
                0
        ));

        zsbs.add(new ZeroSymbolBill(
                zsb.getType(),
                Utils.GUID(), // set new guid for BQ code after split
                zsb.getVar1(),
                zsb.getPn(),
                zsb.getDate(),
                zsb.getVar2(),
                zsb.getQty() - zsb.getSplitQty(),// second split.    quantity = quantity - split quantity
                zsb.getUnit(),
                zsb.getNewGuid(),   // become the old guid after split
                0
        ));
        /* BQ code split completion */

        if (isConnByBluetooth) {
            /* get first bonded mac address */
            bluetoothMacAddress = findBluetoothMacAddress().get(0);
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

            if (printerStatus.isReadyToPrint) {
                ResultObj obj = sendToPrint(printer, zsbs);
                errMsg = obj.getMessage();
            } else if (printerStatus.isHeadOpen)
                errMsg = "Err-01: Head Open! \n Please close Printer Head to print. ";
            else if (printerStatus.isHeadCold)
                errMsg = "Err-02: Head Cold! \n Please try again. ";
            else if (printerStatus.isHeadTooHot)
                errMsg = "Err-03: Head too hot! \n Please do it later. ";
            else if (printerStatus.isPaperOut)
                errMsg = "Err-04: Media Out! \n Please load Media to Print. ";
            else if (printerStatus.isPartialFormatInProgress)
                errMsg = "Err-05: Head Open! \n Please try again later. ";
            else if (printerStatus.isPaused)
                errMsg = "Err-06: Printer Paused. ";
            else if (printerStatus.isReceiveBufferFull)
                errMsg = "Err-07: Buffer full! \n Please do it later. ";
            else if (printerStatus.isRibbonOut)
                errMsg = "Err-08: Ribbon Out! \n Please retry after adjustment. ";

            connection.close();

            /**
             * record the first use log
             */
            String result = recordFirstUseLog(ip, arrName + arrVersionName + "." + arrVersionCode);
            return new ResultObj("".equals(errMsg), ip + "\n" + arrName + "\n" + arrVersionCode + "\n" + arrVersionName + "\n\n" + result);
        } catch (Exception e) {
            return new ResultObj(false, "Exc-02: " + e.getMessage());
        } finally {

        }
    }


    private ResultObj sendToPrint(ZebraPrinter printer, ArrayList<ZeroSymbolBill> zsbs) {
        String filename = "TEMP.LBL";
        try {
            File file = context.getFileStreamPath(filename);
            if (!file.exists())
                file.createNewFile();
            createZPLFile(printer, filename, zsbs);
            printer.sendFileContents(file.getAbsolutePath());
            return new ResultObj(true, "");
        } catch (IOException e) {
            return new ResultObj(false, e.getMessage());
        } catch (ConnectionException e) {
            return new ResultObj(false, e.getMessage());

        }
    }

    private void createZPLFile(ZebraPrinter printer, String filename, ArrayList<ZeroSymbolBill> zsbs) throws IOException {

        FileOutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
        byte[] zplByte = null;

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        if (printerLanguage == PrinterLanguage.ZPL) {
            StringBuilder sb = new StringBuilder();
            for (ZeroSymbolBill zsb : zsbs) {
                sb.append(buildZPLTemplate(zsb));
            }
            zplByte = sb.toString().getBytes();
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
                .append("^FO54,102^AEN,10,10^FD" + zsb.getPn() + "^FS\n")//2T459M000-000-G5
                .append("^FO24,142^AEN,10,10^FDQTY:^FS\n")//QTY
                .append("^FO54,182^AEN,10,10^FD" + zsb.getQty() + " PCS^FS\n")//200000
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

    /**
     * find bluetooth mac address list
     *
     * @return
     */
    private ArrayList<String> findBluetoothMacAddress() {
        ArrayList<String> macAddressList = new ArrayList<>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices)
            macAddressList.add(device.getAddress());
        return macAddressList;
    }


    /**
     * get Local Mac Address
     *
     * @return
     */
    private String getLocalMacAddress() {
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            String line;
            while ((line = input.readLine()) != null) {
                macSerial += line.trim();
            }

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return macSerial;
    }


    /**
     * send a request to call the C# API
     * record the first use log
     *
     * @param ip      the client ip address
     * @param creater application name and version code
     * @return
     */
    public String recordFirstUseLog(String ip, String creater) {
        final String api = "http://10.151.138.63:8080/api/Home/InsertPrintLog";
        String path = api + "?ipaddress=" + ip + "&creater=" + creater;
        String result = Utils.doGet(path);
        return result;
    }
}
