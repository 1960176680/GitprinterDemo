package com.wlpava.printer.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import printpp.printpp_yt.PrintPP_CPCL;

public class PrintActivity extends AppCompatActivity {
    private TextView backTextView;
    private ImageView YTOImageView;
    private ImageView TTKDImageView;
    private ImageView ZTOImageView;
    private Button connectStatusButton;
    private Button printPictureButton;
    private Button disconnectButton;
    private Button shutdownButton;
    private TextView tipTextView;
    private PrintPP_CPCL iPrinter;
    private String printerName;
    private String printerAddress;

    private static final int SIMPLE = 0;
    private static final int YTO = 1;
    private static final int TTKD = 2;
    private static final int ZTO = 3;

    private static final int MSG_CONNECTING = 1;
    private static final int MSG_CONNECTED = 2;
    private static final int MSG_NOT_CONNECTED = 3;
    private static final int MSG_DISCONNECT = 4;
    private static final int MSG_PRINTING = 5;
    private static final int MSG_PRINTED = 6;
    private static final int MSG_SHUTDOWN = 7;
    private static final int MSG_CHECKID = 8;
    private static final int MSG_DISABLEID = 9;
    private byte[] result;

    /**
     * 消息处理
     */
    Handler printHandler = new Handler(){
        @Override
        public void  handleMessage (Message msg) {
            switch (msg.what) {
                case MSG_CONNECTING:
                    tipTextView.setText("正在连接打印机，请稍后...");
                    setButtonStatus(false);
                    break;
                case MSG_CONNECTED:
                    tipTextView.setText("连接打印机成功");
                    setButtonStatus(true);
                    break;
                case MSG_NOT_CONNECTED:
                    tipTextView.setText("连接打印机失败");
                    setButtonStatus(false);
                    break;
                case MSG_PRINTING:
                    tipTextView.setText("正在打印，请稍后...");
                    break;
                case MSG_PRINTED:
                    tipTextView.setText("打印已完成");
                    break;
                case MSG_DISCONNECT:
                    tipTextView.setText("打印机已断开");
                    setButtonStatus(false);
                    break;
                case MSG_SHUTDOWN:
                    tipTextView.setText("打印机已关闭");
                    setButtonStatus(false);
                    break;
                case MSG_CHECKID:
                    tipTextView.setText("校验ID：" + result[0] + "，"+result[1] + "，" + result[2]);
                    break;
                case MSG_DISABLEID:
                    tipTextView.setText("取消ID：" + result[0] + "，"+result[1] + "，" + result[2]);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        tipTextView = (TextView) findViewById(R.id.tipTextView);
        connectStatusButton = (Button) findViewById(R.id.connectStatusButton);
        printPictureButton = (Button) findViewById(R.id.printPictureButton);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        shutdownButton = (Button) findViewById(R.id.shutdownButton);
        setButtonStatus(false);

        Bundle bundle = this.getIntent().getExtras();
        printerName = bundle.getString("printerName");
        printerAddress = bundle.getString("printerAddress");
        iPrinter = new PrintPP_CPCL();

        backTextView = (TextView) findViewById(R.id.backTextView);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintActivity.this.finish();
            }
        });

        YTOImageView = (ImageView) findViewById(R.id.YTOImageView);
        TTKDImageView = (ImageView) findViewById(R.id.TTKDImageView);
        ZTOImageView = (ImageView) findViewById(R.id.ZTOImageView);
        InputStream is;
        Bitmap picture;
        try {
            is = getAssets().open("YTO.png");
            picture = BitmapFactory.decodeStream(is);
            YTOImageView.setImageBitmap(picture);

            is = getAssets().open("TTKD.png");
            picture = BitmapFactory.decodeStream(is);
            TTKDImageView.setImageBitmap(picture);

            is = getAssets().open("STO.png"); //ZTO.png
            picture = BitmapFactory.decodeStream(is);
            ZTOImageView.setImageBitmap(picture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        YTOImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "YTOImageView click", Toast.LENGTH_SHORT).show();
                if (isConnected()) {
                    startPrint(YTO);
                }
            }
        });

        TTKDImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "TTKDImageView click", Toast.LENGTH_SHORT).show();
                if (isConnected()) {
                    startPrint(TTKD);
                }
            }
        });

        ZTOImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "ZTOImageView click", Toast.LENGTH_SHORT).show();
                if (isConnected()) {
                    startPrint(ZTO);
                }
            }
        });

        connectStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "connectStatusButton click", Toast.LENGTH_SHORT).show();
                tipTextView.setText("当前连接状态：" + isConnected()+"，打印状态："+iPrinter.printerStatus());
            }
        });

        printPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "printPictureButton click", Toast.LENGTH_SHORT).show();
                if (isConnected()) {
                    startPrint(SIMPLE);
                }
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "disconnectButton click", Toast.LENGTH_SHORT).show();
                disconnect();
            }
        });

        shutdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "shutdownButton click", Toast.LENGTH_SHORT).show();
                iPrinter.shutdown();
                printHandler.sendEmptyMessage(MSG_SHUTDOWN);
            }
        });
    }

    private static final String DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction(DISCONNECTED);
        registerReceiver(dynamicReceiver, dynamic_filter);
    }

    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() { //动态广播的Receiver
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DISCONNECTED)){
                iPrinter.disconnect();
                printHandler.sendEmptyMessage(MSG_DISCONNECT);
                //Toast.makeText(context, "打印机已关闭，连接状态："+iPrinter.isConnected(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void connect() {
        printHandler.sendEmptyMessage(MSG_CONNECTING);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //iPrinter.connect(printerAddress);
                    iPrinter.connect(printerName, printerAddress);
                    if (iPrinter.isConnected()) {
                        iPrinter.checkID("tonghao_kuaidiyun1163");

                        printHandler.sendEmptyMessage(MSG_CONNECTED);
                    } else {
                        iPrinter.disableID();

                        iPrinter.disconnect();
                        printHandler.sendEmptyMessage(MSG_NOT_CONNECTED);
                    }
                }
            }).start();
        } catch (Exception e) {
            //tipTextView.setText("连接异常：" + e.getMessage() + "；"+e.getStackTrace()[0]);
        }
    }

    public void disconnect() {
        if (iPrinter.isConnected()) {
            iPrinter.disconnect();
            printHandler.sendEmptyMessage(MSG_DISCONNECT);
        } else {
            connect();
        }
    }

    public boolean isConnected() {
        return iPrinter.isConnected();
    }

    /**
     * 设置按钮状态
     * @param status
     */
    public void setButtonStatus(boolean status) {
        printPictureButton.setEnabled(status);
        if (status) {
            disconnectButton.setText("断开打印机");
        } else {
            disconnectButton.setText("连接打印机");
        }
        disconnectButton.setEnabled(true);
        shutdownButton.setEnabled(status);
    }

    /**
     * 开始打印
     * @param type
     * @return
     */
    int startPrint(final int type) {
        int ret = -1;
        if (!iPrinter.isConnected()) {
            return -1;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                printHandler.sendEmptyMessage(MSG_PRINTING);
                switch (type) {
                    case SIMPLE:
                        sendSimplePrintData();
                        break;
                    case YTO:
                        sendYTPrintData(false);
                        break;
                    case TTKD:
                        sendTTPrintData(false);
                        break;
                    case ZTO:
                        //sendZTOPrintData(false);
                        sendSTPrintData();
                        break;

                    default:
                        break;
                }

                iPrinter.print(1, 1); //1
                //iPrinter.setPaperFeedLength(30);

                printHandler.sendEmptyMessage(MSG_PRINTED);
            }
        }).start();

        ret = 0;
        return ret;
    }

    /**
     * 打印简单模板
     */
    void sendSimplePrintData() {
        Bitmap bmp = null;
//        Bitmap bmp2 = null;
//        Bitmap bmp3 = null;
        try {
            InputStream bmpis = getAssets().open("test.png");
            bmp = BitmapFactory.decodeStream(bmpis);

//            bmp = ToBackBitmap.getImageFromAssetsFile(PrintActivity.this, "test.png");
//            bmp = ToBackBitmap.convertToBlackWhite(bmp);

            // Log.d("SimpleLabel", "Bitmap size:" + bmp.getWidth() + " X " + bmp.getHeight());
//            bmpis = getAssets().open("YTO.png");
//            bmp2 = BitmapFactory.decodeStream(bmpis);
//            bmpis = getAssets().open("TTKD.png");
//            bmp3 = BitmapFactory.decodeStream(bmpis);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bmp != null) {
            iPrinter.pageSetup(568, 240 + bmp.getHeight() + 8);
        } else {
            iPrinter.pageSetup(568, 240);
        }

        String str = "标签测试";
        iPrinter.drawText((568 - 30 * str.length()) / 2, (240 - 30) / 2, str, 4, 0, 0, false, false); //96
        //iPrinter.drawText(120, 120, str, 2, 1, 0, false, false);

        str = "210000000000";
        iPrinter.drawText(568 - 10 * str.length(), 240 - 20 - 4, str, 2, 0, 1, false, false);

        if (bmp != null) {
            iPrinter.drawGraphic((568 - bmp.getWidth()) / 2, 240, bmp.getWidth(), bmp.getHeight(), bmp);
        }
//        if (bmp2 != null) {
//            iPrinter.drawGraphic(10, 240, bmp2.getWidth(), bmp2.getHeight(), bmp2);
//        if (bmp3 != null) {
//            iPrinter.drawGraphic((568 - bmp.getWidth()) / 2+bmp.getWidth()+10, 240, bmp3.getWidth(), bmp3.getHeight(), bmp3);
//        }

    }

    /**
     * 打印圆通模板
     * @param drawBox
     */
    void sendYTPrintData(boolean drawBox){
        iPrinter.pageSetup(568, 1436 - 8);
        //第一联
        if (drawBox) {
            iPrinter.drawBox(2, 2 + 4 + 4, 1, 566, 256 + 128 + 168 + 128); //第一联边框
        } else {
            iPrinter.drawLine(2, 2+4+4, 680, 566, 680, false);
        }
        iPrinter.drawLine(2, 2+4+4, 240, 566, 240,false);//第一联横线1
        iPrinter.drawLine(2, 2+4+4, 384, 566, 384,false);//第一联横线2
        iPrinter.drawLine(2, 2+4+4, 552, 566-32, 552,false);//第一联横线3
        iPrinter.drawLine(2, 40+4+4, 384, 40+4+4, 680,false);//第一联竖线1，从左到右
        iPrinter.drawLine(2, 2+408+4+4, 552, 2+408+4+4, 680,false);//第一联竖线2，从左到右
        iPrinter.drawLine(2, 566 - 32, 384, 566 - 32, 680, false);//第一联竖线3，从左到右
        //二维码信息
        iPrinter.drawQrCode(2 + 160, 16, "www.yto.net.cn", 0, 2, 5);
        iPrinter.drawText(2+320, 16+8, "代收货款", 3,0, 1,false,false);
        //金额
        iPrinter.drawText(2+320, 48+8+8, "金额：", 3,0,  0, false, false);
        //具体金额
        iPrinter.drawText(2+8+400, 48+8+8, "0.0元", 3,0,  1, false, false);
        //目的地
        iPrinter.drawText(2 + 166 + 32, 128 + 16 + 8, "010", 6, 0, 0, false, false);
        //条码
        //条码字符
        iPrinter.drawText(2+96+76+32, 340, "858691130534", 3,0,  0, false, false);
        //收件人
        iPrinter.drawText(2+4+4+4,384+28,32,120,"收件人",3,0,  1,false,false);
        //收件人姓名＋电话，最终实施时请用变量替换
        iPrinter.drawText(2+4+32+8+4+4,264+128,480,32,"程远远"+" "+"18721088532"+"  "+"",3,0, 1,false,false);
        //收件地址 ，最终实施时请用变量替换
        iPrinter.drawText(2+4+32+8+4+4,372+40+22,448,120,"北京北京市朝阳区 北京曹威风威风威风 为氛围分为氛围阳曲",3,0,  1,false,false);
        //寄件人
        iPrinter.drawText(2+8+4+4,552+22,32,96,"寄件人",2,0,  0,false,false);
        //寄件人姓名＋电话，
        iPrinter.drawText(2+4+32+8+4+4,552+8,480,24,"chenxiang"+" "+"13512345678"+"  "+"",2,0, 0,false,false);
        //寄件人地址
        iPrinter.drawText(2+4+32+8+4+4,552+40,344,112,"上海市青浦区   华新镇华徐公路",2,0,  0,false,false);
        //签收人
        iPrinter.drawText(2+424,552+8,"签收人：",2,0,  0,false,false);
        //日期
        iPrinter.drawText(2+424,680-26,"日期：",2,0,  0,false,false);
        //派件联
        iPrinter.drawText(566-32+3,384+128,32,96,"派件联",2,0,0,false,false);
        //虚线
        //iPrinter.drawLine(2, 2, 680+8, 566, 680+8,false);
        //第二联
        if (drawBox) {
            iPrinter.drawBox(2, 2+4+4,680+16, 566, 680+16+288);//第二联边框
        } else {
            iPrinter.drawLine(2, 2+4+4, 680+16+288, 566, 680+16+288, false);
        }
        iPrinter.drawLine(2,2+4+4, 696+32, 566, 696+32,false);//第二联横线1，从左到右
        iPrinter.drawLine(2,2+4+4, 696+160, 566-32, 696+160,false);//第二联横线2，从左到右
        iPrinter.drawLine(2,2+40+4+4, 696+160+96, 566-32, 696+160+96,false);//第二联横线3，从左到右
        iPrinter.drawLine(2, 2+40+4+4, 696+32, 2+40+4+4, 696+288,false);//第二联竖线1，从左到右
        iPrinter.drawLine(2, 248+42+4+4, 696+160+96,248+42+4+4, 680+16+288,false);//第二联竖线2，从左到右
        iPrinter.drawLine(2, 566 - 32, 696 + 32, 566 - 32, 680 + 16 + 288, false);//第二联竖线3，从左到右
        //运单号+运单号
        iPrinter.drawText(2+8+4+4,696+3,"运单号："+"858691130534"+"  "+"订单号："+"DD00000014486",2,0,  0,false,false);
        //收件人
        iPrinter.drawText(2+8+4+4,696+32+16,32,96,"收件人",2,0,  0,false,false);
        //收件人姓名＋电话，最终实施时请用变量替换
        iPrinter.drawText(2+8+32+8+4+4,608+128,480,24,"程远远"+" "+"18721088532"+"  "+"",2,0, 0,false,false);
        //收件地址 ，最终实施时请用变量替换
        iPrinter.drawText(2+8+32+8+4+4,696+32+40+2,424,80,"北京北京市朝阳区 北京曹威风威风威风 为氛围分为氛围阳曲",2,0,  0,false,false);
        iPrinter.drawText(2+8+4+4,696+160+3,32,120,"内容品名",2,0, 0,false,false);
        iPrinter.drawText(2+4+32+8+4+4,696+160+8,432,136,"0",2,0, 0,false,false);
        iPrinter.drawText(2 + 4 + 32 + 8 + 4 + 4, 696 + 160 + 96 + 4, "数量：" + "1", 2, 0, 0, false, false);
        iPrinter.drawText(2 + 410, 696 + 160 + 96 + 4, "重量：" + "0" + "kg", 2, 0, 0, false, false);
        iPrinter.drawText(566 - 32 + 3, 696 + 32 + 80, 32, 96, "收件联", 2, 0, 0, false, false);
        if (drawBox) {
            iPrinter.drawBox(2, 2 + 4 + 4, 1000, 566, 1000 + 432 - 4 - 16);//第三联边框
        } else {
            iPrinter.drawLine(2, 2 + 4 + 4, 1000 + 432 - 4 - 16 , 566, 1000 + 432 - 4 - 16, false);
        }
        iPrinter.drawLine(2,2+4+4, 1096, 566, 1096,false);//第三联横线1，从左到右
        iPrinter.drawLine(2,2+4+4, 1096+104-8, 566-32, 1096+104-8,false);//第三联横线2，从左到右
        iPrinter.drawLine(2,2+4+4, 1096+104+104-8, 566-32, 1096+104+104-8,false);//第三联横线3，从左到右
        iPrinter.drawLine(2,2+40+4+4, 1096+104+104+96+4-4-2-8-4, 566-32, 1096+104+104+96+4-4-2-8-4,false);//第三联横线4，从左到右
        iPrinter.drawLine(2, 2 + 40 + 4 + 4 - 4, 1096, 2 + 40 + 4 + 4 - 4, 1432 - 4 - 16, false);//第三联竖线1，从左到右
        iPrinter.drawLine(2, 248 + 42 + 4 + 4, 1096 + 104 + 104 + 96 - 8, 248 + 42 + 4 + 4, 1432 - 4 - 16, false);//第三联竖线2，从左到右
        iPrinter.drawLine(2, 566 - 32, 1096, 566 - 32, 1432 - 4 - 16, false);//第三联竖线3，从左到右
        iPrinter.drawBarCode(2+160, 240+16, "858691130534", 1,  0, 3, 80);
        iPrinter.drawBarCode(2 + 250 + 4, 1000 + 8, "858691130534", 1, 0, 3, 56);
        //条码数据
        iPrinter.drawText(2+312, 1008+56+4, "858691130534", 2,  0,0,false,false);
        //收件人
        iPrinter.drawText(2+8+4,1096+5,32,96,"收件人",2,0,  0,false,false);
        //收件人姓名＋电话，最终实施时请用变量替换
        iPrinter.drawText(2+8+32+8+4+4,1096+8,480,24,"程远远"+" "+"18721088532"+"  "+"",2,0, 0,false,false);
        //收件地址 ，最终实施时请用变量替换
        iPrinter.drawText(2+8+32+8+4+4,1096+8+24+8,456,64,"北京北京市朝阳区 北京曹威风威风威风 为氛围分为氛围阳曲",2,0,  0,false,false);
        //寄件人
        iPrinter.drawText(2+8+4+4,1096+104+5,32,96,"寄件人",2,0,  0,false,false);
        //寄件人姓名＋电话，
        iPrinter.drawText(2+4+32+8+4+4,1096+104+8,480,24,"chenxiang"+" "+"13512345678"+"  "+"",2,0, 0,false,false);
        //寄件人地址
        iPrinter.drawText(2+4+32+8+4+4,1096+104+8+24+8,456,72,"上海市青浦区   华新镇华徐公路",2,0,  0,false,false);
        //内容品名
        iPrinter.drawText(2+8+4+4,1096+104+104+1,32,120,"内容品名",2,0, 0,false,false);
        //订单号
        //  iPrinter.drawText(2+4+32+8,1348,"订单号："+mOrderVO.getOrderNo(),2,0, 0,false,false);
        //内容品名具体
        iPrinter.drawText(2+4+32+8+4+4,1096+104+104+8,432,156,"0",2,0, 0,false,false);
        //数量
        iPrinter.drawText(2+4+32+8+4+4,1432-32+4-4-8-4,"数量："+"1",2,0, 0,false,false);
        //重量
        iPrinter.drawText(2+400,1432-32+4-4-8-4,"重量："+"0"+"kg",2,0, 0,false,false);
        //寄件联
        iPrinter.drawText(566 - 32 + 3, 1096 + 104 + 16, 32, 96, "寄件联", 2, 0, 0, false, false);
    }

    /**
     * 打印天天快递模板
     * @param drawBox
     */
    void sendTTPrintData(boolean drawBox) {
        iPrinter.pageSetup(576 , 1460); //设置页面大小

        //派件联
        if (drawBox) {
            iPrinter.drawBox(2 , 8 , 8 , 560 , 688);    //画派件联的框
        } else {
            iPrinter.drawLine(2, 8, 688, 560, 688, true);
        }
        iPrinter.drawLine(2 , 8 , 97 , 560 , 97,true);      //第一联横线1
        iPrinter.drawLine(2 , 508 , 97 , 508 , 688,true);   //第一联右侧竖线
        iPrinter.drawLine(2 , 420 , 97 , 420 , 177,true);   //第一联短竖线
        iPrinter.drawLine(2 , 8 , 177 , 508 , 177,true);    //第一联横线2
        iPrinter.drawLine(2 , 68 , 177 , 68 , 298,true);    //第一联左侧竖线
        iPrinter.drawLine(2 , 8 , 298 , 508 , 298,true);    //第一联横线3
        iPrinter.drawLine(2 , 68 , 298 , 68 , 375,true);    //第一联左侧竖线
        iPrinter.drawLine(2 , 8 , 375 , 508 , 375,true);    //第一联横线4
        iPrinter.drawLine(2 , 8 , 554 , 508 , 554,true);    //第一联横线5
        iPrinter.drawLine(2 , 377 , 554 , 377 , 688,true);  //第一联右侧竖线2

        //省市信息
        iPrinter.drawText(352 , 24 , 216 , 81 , "浙"+"  "+"杭州" , 4 , 0 , 1 , false , false);
        //包裹来源
        iPrinter.drawText(16 , 105 , 404 , 72 , "杭州公司包" , 4 , 0 , 1 , false , false);
        //包号
        iPrinter.drawText(452 , 113 , 24 , 48 , "2" , 4 , 0 , 1 , false , false);
        //派件联
        iPrinter.drawText(522 , 340 , 24 , 72 , "派  件  联" , 2 , 0 , 0 , false , false);
        //收件
        iPrinter.drawText(26 , 213 , 24 , 48 , "收件" , 2 , 0 , 0 , false , false);
        //收件人信息（姓名 + 手机号码）
        iPrinter.drawText(76 , 185 , 432 , 24 , "吉宇  13682429075" , 2 , 0 , 1 , false , false);
        //收件人地址
        iPrinter.drawText(76 , 220 , 432 , 81 , "浙江省 杭州市 滨江区 滨盛路1505号 1706室" , 2 , 0 , 1 , false , false);
        //寄件
        iPrinter.drawText(26 , 312 , 24 , 48 , "寄件" , 2 , 0 , 0 , false , false);
        //寄件人（姓名 + 手机号码）
        iPrinter.drawText(76 , 306 , 432 , 24 , "天天  13888888888" , 2 , 0 , 0 , false , false);
        //寄件人地址
        iPrinter.drawText(76 , 330 , 432 , 37 , "福建省 厦门市 湖滨南路 111号1111室 ", 2 , 0 , 0 , false , false);
        //运单条码
        iPrinter.drawBarCode(91 , 401 , "998016450402" , 1 , 0 , 3 , 80);
        iPrinter.drawText(185 , 485 , 333 , 24 , "998016450402" , 2 , 0 , 1 , false , false);
        //说明
        iPrinter.drawText(16 , 562 , 361 , 126 , "快件送达收件人地址，经收件人或寄件人允许的代收人签字，视为送达您的签字代表您已经验收此包裹，并已确认商品信息无误，包装完好没有划痕、破损等表面质量问题。" , 1 , 0 , 0 , false , false);
        //签收人
        iPrinter.drawText(385 , 562 , 123 , 24 , "签收人:" , 2 , 0 , 0 , false , false);
        //时间
        iPrinter.drawText(385 , 612 , 123 , 94 , "时间:" , 2 , 0 , 0 , false , false);
        //收件联
        if (drawBox) {
            iPrinter.drawBox(2 , 8 , 694 , 560 , 1006);     //收件联外框
        } else {
            iPrinter.drawLine(2, 8, 1006, 560, 1006, true);
        }
        iPrinter.drawLine(2 , 8 , 749 , 560 , 749,true);    //第二联横线1
        iPrinter.drawLine(2 , 68 , 749 , 68 , 1006,true);   //第二联左侧竖线1
        iPrinter.drawLine(2 , 508 , 749 , 508 , 1006,true); //第二联右侧竖线1
        iPrinter.drawLine(2 , 68 , 749 , 68 , 852,true);    //第二联横线2
        iPrinter.drawLine(2 , 8 , 852 , 508 , 852,true);    //第二联横线3
        iPrinter.drawLine(2 , 68 , 852 , 68 , 948,true);    //第二联竖线2
        iPrinter.drawLine(2 , 68 , 948 , 508 , 948,true);   //第二联横线4
        iPrinter.drawLine(2 , 288 , 948 , 288 , 998,true);  //第二联短竖线
        //运单号和订单号
        iPrinter.drawText(36 , 709 , 504 , 24 , "运单号:"+"998016450402"+"    "+"订单号:"+"DD8016450402" , 2 , 0 , 0 , false , false);
        //收件联
        iPrinter.drawText(522 , 813 , 24 , 72 , "收  件  联" , 2 , 0 , 0 , false , false);
        //收件
        iPrinter.drawText(26 , 776 , 24 , 48 , "收件" , 2 , 0 , 0 , false , false);
        //收件人（姓名+手机号）
        iPrinter.drawText(76 , 757 , 432 , 24 , "吉宇"+"  "+"13682429075" , 2 , 0 , 1 , false , false);
        //收件人地址
        iPrinter.drawText(76 , 790 , 432 , 63 , "浙江省 杭州市 滨江区 滨盛路1505号 1706室" , 2 , 0 , 1 , false , false);
        //内容
        iPrinter.drawText(26 , 900 , 24 , 48 , "内容" , 2 , 0 , 0 , false , false);
        //内容
        iPrinter.drawText(76 , 860 , 432 , 88 , "白瓷牡丹餐具套装（经典优惠套装）" , 2 , 0 , 0 , false , false);
        //数量
        iPrinter.drawText(76 , 964 , 212 , 42 , "数量: " + "1" , 2 , 0 , 0 , false , false);
        //重量
        iPrinter.drawText(296 , 964 , 212 , 42 , "重量: " + "2.6KG" , 2 , 0 , 0 , false , false);
        //寄件联
        if (drawBox) {
            iPrinter.drawBox(2 , 8 , 1012 , 560 , 1452);    //第三联外框
        } else {
            iPrinter.drawLine(2, 8, 1452, 560, 1452, true);
        }
        iPrinter.drawLine(2 , 8 , 1115 , 560 , 1115,true);      //第三联横线1
        iPrinter.drawLine(2 , 68 , 1115 , 68 , 1452,true);      //第三联左侧竖线1
        iPrinter.drawLine(2 , 508 , 1115 , 508 , 1452,true);    //第三联右侧竖线
        iPrinter.drawLine(2 , 68 , 1115 , 68 , 1245,true);      //第三联左侧竖线2
        iPrinter.drawLine(2 , 8 , 1245 , 508 , 1245,true);      //第三联横线2
        iPrinter.drawLine(2 , 68 , 1245 , 68 , 1327,true);      //第三联左侧竖线3
        iPrinter.drawLine(2 , 8 , 1327 , 508 , 1327,true);      //第三联横线3
        iPrinter.drawLine(2 , 68 , 1327 , 68 , 1404,true);      //第三联左侧竖线4
        iPrinter.drawLine(2 , 68 , 1404 , 508 , 1404,true);     //第三联横线4
        iPrinter.drawLine(2 , 288 , 1404 , 288 , 1452,true);    //第三联竖线5
        //第三联运单号条码
        iPrinter.drawBarCode(330 , 1032 , "998016450402" , 1 , 0 , 2 , 40);
        iPrinter.drawText(369 , 1076 , 222 , 24 , "998016450402" , 2 , 0 , 1 , false , false);
        //寄件联
        iPrinter.drawText(522 , 1224 , 24 , 72 , "寄  件  联" , 2 , 0 , 0 , false , false);
        //收件
        iPrinter.drawText(26 , 1156 , 24 , 48 , "收件" , 2 , 0 , 1 , false , false);
        //收件人（姓名 + 手机号码）
        iPrinter.drawText(76 , 1123 , 432 , 24 , "吉宇  13682429075" , 2 , 0 , 1 , false , false);
        //收件地址
        iPrinter.drawText(76 , 1155 , 432 , 90 , "浙江省 杭州市 滨江区 滨盛路1505号 1706室" , 2 , 0 , 1 , false , false);
        //寄件
        iPrinter.drawText(26 , 1262 , 24 , 48 , "寄件" , 2 , 0 , 0 , false , false);
        //寄件人（姓名 + 手机号码)
        iPrinter.drawText(76 , 1253 , 432 , 18 , "天天" + "  " + "13888888888" , 2 , 0 , 0 , false , false);
        //收件地址
        iPrinter.drawText(76 , 1279 , 432 , 48 , "福建省 厦门市 湖滨南路 111号1111室 ", 2 , 0 , 0 , false , false);
        //内容
        iPrinter.drawText(26 , 1365 , 24 , 48 , "内容" , 2 , 0 , 0 , false , false);
        //内容
        iPrinter.drawText(76 , 1335 , 432 , 69 , "白瓷牡丹餐具套装（经典优惠套装）" , 2 , 0 , 0 , false , false);
        //数量
        iPrinter.drawText(76 , 1416 , 212 , 40 , "数量: " + "1" , 2 , 0 , 0 , false , false);
        //重量
        iPrinter.drawText(296 , 1416 , 212 , 40 , "重量: " + "2.6KG" , 2 , 0 , 0 , false , false);
    }

    /**
     * 打印申通模板
     */
    void sendSTPrintData(){
        int width = 72*8;
        int height = 150*8;
        iPrinter.pageSetup(width, height+20);
        //第一联
        iPrinter.drawBox(4, 0, 0, width, height); //第一联边框
        iPrinter.drawLine(4, 0, 800, width, 800, true); //第一联边框

        iPrinter.drawLine(2, 0, 72, width, 72, true);//第一联横线1
        iPrinter.drawLine(2, 0, 160, width, 160, true);//第一联横线2
        iPrinter.drawLine(2, 0, 280, 440, 280,true);//第一联横线3
        iPrinter.drawLine(2, 0, 384, width, 384,true);//第一联横线4
        iPrinter.drawLine(2, 0, 488, width, 488,true);//第一联横线5
        iPrinter.drawLine(2, 0, 696, width, 696,true);//第一联横线6
        iPrinter.drawLine(2, 44, 160, 44, 488,true);//第一联竖线1，从左到右
        iPrinter.drawLine(2, 440, 160, 440, 488,true);//第一联竖线2，从左到右

        iPrinter.drawLine(2, 0, 880, width, 880, true);//第二联横线1
        iPrinter.drawLine(2, 0, 996, width, 996, true);//第二联横线2
        iPrinter.drawLine(2, 0, 1100, width, 1100,true);//第二联横线3
        iPrinter.drawLine(2, 44, 996, 44, height, true);//第二联竖线1
        iPrinter.drawLine(2, 440, 996, 440, height,true);//第二联竖线2

        //上联
        Bitmap bmp = null;
        try {
            InputStream bmpis = getAssets().open("STO2.png");
            bmp = BitmapFactory.decodeStream(bmpis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        iPrinter.drawGraphic(8, 8, bmp.getWidth(), bmp.getHeight(), bmp);
        iPrinter.drawText(width-210, 20, 210, 32, "浙江嘉兴集散", 3, 0, 1,false,false);
        iPrinter.drawText((width-230)/2, 88, 230, 64, "026E-123", 4, 0, 1,false,false);
        //收件
        iPrinter.drawText(10, 196, 24, 48, "收件", 2, 0, 0,false,false);
        //收件人信息（姓名 + 手机号码）
        iPrinter.drawText(50, 168, 392, 24, "嘉兴  张痘痘  14759288741", 2, 0, 1,false,false);
        //收件人地址
        iPrinter.drawText(50, 200, 392, 48, "浙江省嘉兴市南湖区海盐塘路与南溪路交叉口（南湖新渡口停车场）", 2, 0, 1,false,false);
        //寄件
        iPrinter.drawText(10, 308, 24, 48, "寄件", 2, 0, 0,false,false);
        //寄件人（姓名 + 手机号码）
        iPrinter.drawText(50, 288, 392, 24, "泉州  李呼呼  16958451215", 2, 0, 0,false,false);
        //寄件人地址
        iPrinter.drawText(50, 320, 392, 48, "福建厦门思明区中山路步行街海景广场1号1栋1楼24-26号", 2, 0, 0,false,false);

        //服务
        iPrinter.drawText(10, 412, 24, 48, "服务", 2, 0, 0,false,false);
        iPrinter.drawText(50, 392, 392, 24, "代收金额:2599.98元 报价费用:2599.98元", 1, 0, 1,false,false);
        iPrinter.drawText(50, 420, 392, 24, "声明价值:2599.98元 计费重量:1000.5kg", 1, 0, 1,false,false);
        iPrinter.drawText(50, 448, 392, 24, "预约配送:2016-12-20 18:00-21:00", 1, 0, 1,false,false);

        iPrinter.drawQrCode(448, 228, "www.sto.net.cn", 0, 2, 5);

        //代收货款
        iPrinter.drawText(444, 420, 130, 48, "代收货款", 3, 0, 1,false,false);

        //运单条码
        iPrinter.drawBarCode(116, 524, "DDE1256912315624567", 1,  0, 2, 108);
        iPrinter.drawText(136, 640, 480, 32, "DDE1256912315624567", 3, 0, 1,false,false);

        //免责声明
        iPrinter.drawText(10, 696+8, width-20, 80, "快件送达收件人地址，经收件人或收件人（寄件人）允许的代收人签字，视为送达您的签字代表您已验收此包裹，并已确认商品信息无误，包装完好，没有划痕，破损等表面质量问题。", 1, 0, 0,false,false);

        //下联
        iPrinter.drawGraphic(8, 808, bmp.getWidth(), bmp.getHeight(), bmp);
        iPrinter.drawQrCode(448, 808, "www.yto.net.cn", 0, 2, 3);
        //运单条码
        iPrinter.drawBarCode(116, 888, "DDE1256912315624567", 1,  0, 2, 70);
        iPrinter.drawText(136, 966, 480, 24, "DDE1256912315624567", 4, 0, 1,false,false);

        //寄件
        iPrinter.drawText(10, 996+20, 24, 48, "寄件", 2, 0, 0,false,false);
        //寄件人（姓名 + 手机号码）
        iPrinter.drawText(50, 996+8, 392, 24, "泉州  李呼呼  16958451215", 2, 0, 0,false,false);
        //寄件人地址
        iPrinter.drawText(50, 996+40, 392, 48, "福建厦门思明区中山路步行街海景广场1号1栋1楼24-26号", 2, 0, 0,false,false);

        //收件
        iPrinter.drawText(10, 1100+20, 24, 48, "收件", 2, 0, 0,false,false);
        //收件人信息（姓名 + 手机号码）
        iPrinter.drawText(50, 1108, 392, 24, "嘉兴  张痘痘  14759288741", 2, 0, 1,false,false);
        //收件人地址
        iPrinter.drawText(50, 1108+32, 392, 48, "浙江省嘉兴市南湖区海盐塘路与南溪路交叉口(南湖新渡口停车场)", 2, 0, 1,false,false);
        iPrinter.drawText(448, 996+8, 100, 24, "签收人：", 2, 0, 0,false,false);
        iPrinter.drawText(448, 1100+8, 100, 24, "时间：", 2, 0, 0,false,false);

    }

}
