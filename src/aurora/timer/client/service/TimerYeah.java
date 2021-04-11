package aurora.timer.client.service;

import aurora.timer.client.ServerURL;
import aurora.timer.client.view.util.SmartHttpUtil;
import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by hao on 17-1-30.
 */
//TODO:用SmartHttpUtil重写
public class TimerYeah {
    //    private String id;
    public boolean isStop;
    private static Logger logger = Logger.getLogger("timer");

//    @Override
//    public void run() {
//        this.isStop = true;
//        logger.info("开始计时");
//        while (isStop) {
//            addTime(this.id);
//            long sleepTime = 5 * 60 * 1000;
//            try {
//                Thread.sleep(sleepTime);
//                logger.fine("是的，我正在计时");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(null, "后台计时崩了\n", "提示", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//        logger.info("后台计时结束");
//    }
//
//    public TimerYeah() {
//        this("0");
//    }

//    public TimerYeah(String id) {
//        this.id = id;
//    }

    public static boolean addTime(String id){
        Properties locVersion = new Properties();
        String res;
        try {
            locVersion.load(TimerYeah.class.getResourceAsStream("/aurora/timer/client/view/version/version.properties"));
            res = SmartHttpUtil.sendGet(ServerURL.TIMER + "?id=" + id + "&ver=" + locVersion.get("version"), null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if(res.equals("true")){
            logger.info("上传时间");
            //在这关掉弹窗
            if(SmartHttpUtil.dialog!=null) {
                SmartHttpUtil.dialog.dispose();
                SmartHttpUtil.dialog=null;
            }
            return true;
        }else if(res.equals("")){
            logger.warning("重连失败");
            return false;
        }else
            {
            logger.warning("加时返回错误信息：" + res);
            JOptionPane.showMessageDialog(null, "连接服务器成功，但是上传时间失败\n" + res, "提示", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
