package com.foxconn.mac1.zebraprintdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.foxconn.mac1.zebraprinter.Entity.ResultObj;
import com.foxconn.mac1.zebraprinter.ZebraPrinter.ZQ520Printer;

public class MainActivity extends AppCompatActivity {

    protected Button btnPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPrint = this.findViewById(R.id.btnPrintTest);
        btnPrint.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                printTest();
            }
        });
    }

    public void printTest(){
        try {
            ZQ520Printer zq520Printer = new ZQ520Printer();
            ResultObj resultObj = zq520Printer.zeroSymbolBill();
            Toast.makeText(MainActivity.this, String.valueOf(resultObj.isStatus()), Toast.LENGTH_SHORT).show();
            if (resultObj.isStatus()){
                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
