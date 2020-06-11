package com.mt.nettool;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import net.qiujuer.genius.kit.cmd.Cmd;
import net.qiujuer.genius.kit.cmd.Command;
import net.qiujuer.genius.kit.cmd.DnsResolve;
import net.qiujuer.genius.kit.cmd.Ping;
import net.qiujuer.genius.kit.cmd.Telnet;
import net.qiujuer.genius.kit.cmd.TraceRoute;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CmdActivity extends AppCompatActivity {
    private static final String TAG = CmdActivity.class.getSimpleName();
    private TextView mText;
    private String mHost = "www.baidu.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = findViewById(R.id.text);

        init();
        testCommand();
//        testNetTool();
    }

    private void init() {
        Application application = getApplication();
        Cmd.init(application);
    }

    /**
     * Command
     * 测试命令行执行
     */
    private void testCommand() {
        Thread thread = new Thread() {
            public void run() {
                // The same way call way and the ProcessBuilder mass participation
                // 调用方式与ProcessBuilder传参方式一样
                Command command = new Command(Command.TIMEOUT, "/system/bin/ping",
                        "-c", "4", "-s", "100",
                        mHost);

               /* String res = null;
                try {
                    res = Command.command(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showLog(TAG, "\n\nCommand Sync: " + res);*/

                command = new Command("/system/bin/ping",
                        "-c", "3", "-s", "100",
                        mHost);

                // callback by listener
                // 回调方式
                try {
                    Command.command(command, new Command.CommandListener() {
                        @Override
                        public void onCompleted(String str) {
                            showLog(TAG, "\n\nCommand Async onCompleted: \n" + str);

                            testNetTool();

                        }

                        @Override
                        public void onCancel() {
                            Log.i(TAG, "\n\nCommand Async onCancel");

                            testNetTool();
                        }

                        @Override
                        public void onError(Exception e) {
                            showLog(TAG, "\n\nCommand Async onError:" + (e != null ? e.toString() : "null"));

                            testNetTool();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * NetTool
     * 基本网络功能测试
     */
    public void testNetTool() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                // Packets， Packet size，The target，Whether parsing IP
                // 包数，包大小，目标，是否解析IP
                Ping ping = new Ping(3, 32, mHost, true);
                ping.start();
                showLog(TAG, "Ping: " + ping.toString());
                // target
                // 目标，可指定解析服务器
                DnsResolve dns = null;
                try {
                    // Add DNS service
                    // 添加DNS服务器
                    dns = new DnsResolve(mHost, InetAddress.getByName(mHost));
                    dns.start();
                    showLog(TAG, "DnsResolve: " + dns.toString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                // target port
                // 目标，端口
                Telnet telnet = new Telnet(mHost, 80);
                telnet.start();
                showLog(TAG, "Socket: " + telnet.toString());
                // target
                // 目标
                TraceRoute traceRoute = new TraceRoute(mHost);
                traceRoute.start();
                showLog(TAG, "\n\nTraceRoute: " + traceRoute.toString());
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private void showLog(String tag, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mText != null) {
                    mText.append("\n" + msg);
                }
            }
        });

        Log.d(tag, msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Command.dispose();
    }
}
