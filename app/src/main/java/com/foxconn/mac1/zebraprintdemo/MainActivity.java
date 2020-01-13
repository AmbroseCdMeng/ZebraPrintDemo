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

        btnPrint = (Button) this.findViewById(R.id.btnPrintTest);
        btnPrint.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                printTest();
            }
        });

    }

    public void printTest(){
        try {
            /* Example for Test */
            String BQcode = "W,VCN00182631190729C0001,P2a-J60102,2T459M000-000-G5,20190729,WmL-J76036,7200,PCS";
            Double split = 2000d;
            ZQ520Printer zq520Printer = new ZQ520Printer(this);
            ResultObj resultObj = zq520Printer.zeroSymbolBill(BQcode, split);
            if (resultObj.isStatus()){
                Toast.makeText(MainActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
            }

            /* Example end */
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
