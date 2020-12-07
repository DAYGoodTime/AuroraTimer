package aurora.timer.client.view;

import aurora.timer.client.ServerURL;
import aurora.timer.client.service.AdminDataService;
import aurora.timer.client.service.TimerYeah;
import aurora.timer.client.service.UserDataService;
import aurora.timer.client.service.UserOnlineTimeService;
import aurora.timer.client.view.until.SaveBg;
import aurora.timer.client.view.until.TableUntil;
import aurora.timer.client.vo.UserData;
import aurora.timer.client.vo.UserOnlineTime;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by hao on 17-2-22.
 */
public class Main2Form {
    private static MainFrame FRAME;
    private JPanel parent;
    private JButton minButton;
    private JButton outButton;
    private JPanel timePanel;
    private JPanel headPanel;
    private JLabel timeLabel;
    private JButton changeButton;
    private JButton settingButton;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private Long thisWeekTime = 0L;
    private UserOnlineTime onlineTime;
    private UserData userData;
    private Timer freshAddTimer; // 用来加时的计时器
    private Timer paintTimer; // 用来不停的画的计时器
    private JPanel weekAllPane; // 指向周计时的panel
    private JTable thisWeekList; // 指向本周计时的表
    Point mousePoint; //鼠标位置，判断挂机用
    WeekInfoForm weekInfoForm; //用来查看周计时的panel
    WorkForm workForm;
    SettingForm settingForm;
    Vector<UserOnlineTime> userOnlineTimes; //本周时间所有人的集合，本周时间存在u.todayOnlineTime
    String[] theRedPerson;
    int page; //查看周计时的页面
    int pageLimited = 20; //查看上x周最大值
    int mx, my, jfx, jfy; //鼠标位置，给自己设置的拖动窗口用的
    Logger logger = Logger.getLogger("MAIN");
    Boolean SHOW_LOAD_BG_ERROR_DIALOG = false;

    /**
     * 初始化函数
     */
    public void init() {
        page = 0;
        loadSystemTray();
        mousePoint = MouseInfo.getPointerInfo().getLocation();
        weekInfoForm = new WeekInfoForm();
        workForm = new WorkForm();
//        settingForm = new SettingForm(parent, settingButton, userData);
        workForm.setUserData(userData);
        thisWeekList = weekInfoForm.weekList;
        weekAllPane = weekInfoForm.parent;
//        settingForm.setTimePanel(timePanel);
//        settingForm.setWeekAllPane(weekAllPane);
        // 判断是不是管理员
        if (userData.getIsAdmin()) {
            workForm.announceText.setEditable(true);
            workForm.dutyList.setEnabled(true);
            workForm.submitBtn.setEnabled(true);
            workForm.submitBtn.setVisible(true);
        }
        //TODO:bug:timePanel时间圆盘在点切换后会下移(发现是timeLabel变长了)
        weekInfoForm.changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.remove(weekAllPane);
                weekAllPane.setVisible(false);
                timePanel.setVisible(true);
                parent.repaint();
            }
        });
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timePanel.setVisible(false);
                try {
                    TimerYeah.addTime(userData.getID());
                } catch (Throwable throwable) {
                    JOptionPane.showMessageDialog(null, "计时线程异常，请检查网络或者服务器\n", "提示", JOptionPane.ERROR_MESSAGE);
                }
                loadWeekTime(0);
                setAllTime();
                weekAllPane.setVisible(true);
                parent.add(weekAllPane);
            }
        });
        weekInfoForm.leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (page < pageLimited) {
                    page++;
                    loadWeekTime(page);
                    setAllTime();
                }
            }
        });
        weekInfoForm.rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (page > 0) {
                    page--;
                    loadWeekTime(page);
                    setAllTime();
                }
            }
        });
        weekInfoForm.announceBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (x < 56 || y < 56 || x > 324 || y > 110)
                    return;
                if (workForm.loadWorkInfo()) {
                    weekAllPane.setVisible(false);
                    weekAllPane.setEnabled(false);
                    parent.add(workForm.parent);
                } else {
                    JOptionPane.showMessageDialog(null, "加载公告界面失败，请检查网络或者服务器\n", "提示", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        workForm.announceBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //parent.remove(weekAllPane);
                parent.remove(workForm.parent);
                weekAllPane.setVisible(true);
                weekAllPane.setEnabled(true);
            }
        });
        settingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                settingForm.parent.setVisible(true);
                settingForm = new SettingForm(parent, settingButton, userData);
                settingButton.setEnabled(false);
                parent.add(settingForm.parent);
                // 在哪点进去设置的，用于显示回来
                if (timePanel.isVisible())
                    settingForm.setMain2BeforeInComponent(timePanel);
                else if (weekAllPane.isVisible())
                    settingForm.setMain2BeforeInComponent(weekAllPane);
                weekAllPane.setVisible(false);
                timePanel.setVisible(false);
                parent.repaint();
            }
        });
        minButton.setUI(new LoginButtonUI());
        outButton.setUI(new LoginButtonUI());
        changeButton.setUI(new LoginButtonUI());
        changeButton.setContentAreaFilled(false);
        timePanel.setUI(new BasicPanelUI() {
            @Override
            public void update(Graphics g, JComponent c) {
                super.update(g, c);
                c.setSize(565, 780);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 40, 50, 140));
                g2.fillOval(c.getWidth() / 2 - 242, 38, 484, 484);

                g2.setStroke(new BasicStroke(26));
                //如果时间大于24小时，那么进度条就要画金黄色
                if (Integer.parseInt(parseTime(thisWeekTime).split(":")[0]) >= 24) {
                    g2.setColor(new Color(88, 222, 234, 200));
                    g2.drawArc(c.getWidth() / 2 - 187, 86, 375, 375, 0, 360);

                    g2.setColor(new Color(251, 216, 96, 255));
                } else {
                    g2.setColor(new Color(12, 96, 108, 140));
                    g2.drawArc(c.getWidth() / 2 - 187, 86, 375, 375, 0, 360);

                    g2.setColor(new Color(88, 222, 234, 200));
                }
                int angel = (int) (thisWeekTime / (60 * 1000 * 4)); //转成分，每分钟0.25度
                angel = -(angel % 360); //这是drawArc的角度
                g2.drawArc(timePanel.getWidth() / 2 - 187, 86, 375, 375, 90, angel);
                g2.setColor(Color.white);
                g2.drawArc(timePanel.getWidth() / 2 - 187, 86, 375, 375, angel + 89, 1);
            }
        });
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 120));
    }

    /**
     * 将userOnlineTimes中的数据画到表上
     */
    public void setAllTime() {
        // 如果这一页为空白，上限就是这一页了
        if (userOnlineTimes.size() == 0)
            pageLimited = page;
        Iterator<UserOnlineTime> uiIt = userOnlineTimes.iterator();
        DefaultTableModel model = (DefaultTableModel) thisWeekList.getModel();
        if (page == 0) {
            thisWeekList.getColumnModel().getColumn(2).setHeaderValue("本周在线总时间");
        } else {
            thisWeekList.getColumnModel().getColumn(2).setHeaderValue("前" + page + "周在线总时间");
        }
        int index;
        for (index = model.getRowCount() - 1; index >= 0; index--) {
            model.removeRow(index);
        }
        //使用list存储并排序
        java.util.List<UserOnlineTime> list = new LinkedList<>();
        while (uiIt.hasNext()) {
            UserOnlineTime t = uiIt.next();
            list.add(t);
        }
        list.sort(new Comparator<UserOnlineTime>() {
            @Override
            public int compare(UserOnlineTime o1, UserOnlineTime o2) {
                if (o1.getTodayOnlineTime() > o2.getTodayOnlineTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        uiIt = list.iterator();
        //显示出来
        int redListFlag = 0;
        int[] redList = new int[theRedPerson.length];
        while (uiIt.hasNext()) {
            UserOnlineTime t = uiIt.next();
            for (int i = 0; i < theRedPerson.length; i++) {
                if (t.getID().equals(theRedPerson[i])) {
                    redList[redListFlag] = model.getRowCount();
                    redListFlag++;
                }
            }
            try {
                byte[] bytes = t.getName().getBytes();
                String s = new String(bytes, StandardCharsets.UTF_8);
                model.addRow(new Object[]{"   " + s.substring(0, s.length() - 1), parseTime(t.getTermOnlineTime()), "   " + parseTime(t.getTodayOnlineTime())}); //填入表格
            } catch (Exception e) {
                e.printStackTrace();
            }
            //将红名的index集合传入变色
        }
        if (page == 0) {
            TableUntil.setOneRowBackgroundColor(thisWeekList, redList, new Color(255, 77, 93, 150), page);
        } else {
            TableUntil.setOneRowBackgroundColor(thisWeekList, new int[0], Color.black, page);
        }
        parent.repaint();
    }

    /**
     * 获取用户信息
     * 这里之前写的时候就把servlet中一次返回了userData和userOnlineTime
     *
     * @param id 用户ID
     */
    public void loadUserData(String id) {
        UserDataService uds = new UserDataService();
        JSONObject object = uds.findById(id);
        userData = new UserData();
        onlineTime = new UserOnlineTime();
        userData.setID((String) object.get("id"));
        userData.setIsAdmin(Boolean.parseBoolean((String) object.get("isAdmin")));
        userData.setNickName((String) object.get("name"));
        userData.setDisplayURL((String) object.get("disp"));
        userData.setTelNumber((String) object.get("tel"));
        userData.setShortTelNumber((String) object.get("stel"));
        onlineTime.setID((String) object.get("id"));
        onlineTime.setTodayOnlineTime(Long.decode((String) object.getOrDefault("totime", "0")));
        onlineTime.setLastOnlineTime(new Time((Long) object.getOrDefault("laslog", Long.decode("0"))));
    }

    /**
     * 一次性加载前lastX周所有人的计时
     *
     * @param lastX 表示前第多少周，0表示本周
     */
    public void loadWeekTime(int lastX) {
        UserOnlineTimeService service = new UserOnlineTimeService();
        userOnlineTimes = service.getLastXWeekTime(lastX);
        Iterator<UserOnlineTime> uiIt = userOnlineTimes.iterator();
        UserOnlineTime uot;
        //当加载的周计时为0的时候刷新本地的周计时
        logger.info("加载第" + lastX + "周计时数据");
        while (uiIt.hasNext() && lastX == 0) {
            uot = uiIt.next();
            if (uot.getID().equals(userData.getID())) {
                thisWeekTime = uot.getTodayOnlineTime();
                break;
            }
        }
    }

    public boolean loadBg() throws IOException {
        Preferences preferences = Preferences.userRoot().node(ServerURL.PRE_PATH);
        UserDataService uds = new UserDataService();
        InputStream bg = uds.findBgByid(userData.getID(), userData.getPassWord());
        boolean flag = false;
        // 图片不存在或者返回数据过小，失败
        if (bg == null || bg.available() < 1000) {
            logger.warning("从服务器加载背景图片失败");
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "从服务器加载背景图片失败，请检查网络或者服务器\n", "提示", JOptionPane.ERROR_MESSAGE);
                    SHOW_LOAD_BG_ERROR_DIALOG = false;
                }
            });
        } else {
            try {
                String bgPath = System.getProperty("java.io.tmpdir") + File.separator + userData.getID() + "_bg.png";
                if (SaveBg.saveBg(bgPath, bg, true)) {
                    // 修改注册表
                    preferences.put("bg", bgPath);
                    logger.info("从服务器加载背景图片");
                    flag = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 优先从注册表里读取，没有就设置为默认。（在上边从服务器读取到到话会改注册表）
//        ServerURL.BG_PATH = preferences.get("bg", "res" + File.separator + "bg1.png");
        parent.setUI(new MainParentPanelUI());
        return flag;
    }

    /**
     * 后台计时，每隔24分钟提交一次
     */
    public void backAddTime() {
        freshAddTimer = new Timer(5 * 60 * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TimerYeah.addTime(userData.getID());
                    loadUserData(userData.getID());
                    loadWeekTime(0);
                } catch (Throwable throwable) {
                    JOptionPane.showMessageDialog(null, "计时线程异常，请检查网络或者服务器\n", "提示", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        freshAddTimer.setRepeats(true);
        freshAddTimer.start();

        Timer checkTimer = new Timer(24 * 60 * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //挂机检测，就是鼠标24分钟前后在相同位置则暂停加时，在对话框被取消后继续加时
                if (MouseInfo.getPointerInfo().getLocation().equals(mousePoint)) {
                    freshAddTimer.stop();
                    AdminDataService ads = new AdminDataService();
                    // 如果不是自由时间，就弹出对话框
                    if (!ads.isFreeTime())
                        createDialog();//打开提示框，此时计时线程会停止
                    freshAddTimer.start();
                }
                mousePoint = MouseInfo.getPointerInfo().getLocation();
            }
        });
        checkTimer.setRepeats(true);
        checkTimer.start();
    }

    /**
     * 创建检测到挂机时候的dialog
     */
    public void createDialog() {
        String[] option = {"不在", "不在"};
        int o = JOptionPane.showOptionDialog(null, "在？", "提示", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, option, option[0]);
    }

    /**
     * 画界面的Timer
     */
    public void backPaintTime() {
        paintTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisWeekTime += 1000;
                timePanel.repaint();
                timeLabel.setText(parseTime(thisWeekTime));
            }
        });
        paintTimer.setRepeats(true);
        paintTimer.start();
    }

    /**
     * 将Long转换成 时:分 的字符串
     *
     * @param time Long型的时间
     * @return 转换后的字符串
     */
    public String parseTime(Long time) {
        StringBuffer sb = new StringBuffer("");
        if (time / 3600000 < 10) {
            sb.append("0");
        }
        sb.append((time / 3600000) + ":");
        if (time % 3600000 / 60000 < 10) {
            sb.append("0");
        }
        sb.append(time % 3600000 / 60000);
        return sb.toString();
    }

    /**
     * 构造函数，进行初始化和开启Timer
     */
    public Main2Form(String id, String password) {
        loadUserData(id);
        init();
        this.userData.setPassWord(password);
        // 加载背景图片地址，在MainParentPanelUI里会用 ServerURL.BG_PATH 设置背景图,所以要在这之前从服务器加载背景图片。要用到id所以要在loadUserData之后
        try {
            loadBg();
        } catch (IOException e) {
            e.printStackTrace();
        }
        workForm.setUserData(userData);
        // 判断是不是管理员
        if (userData.getIsAdmin()) {
            workForm.announceText.setEditable(true);
            workForm.dutyList.setEnabled(true);
            workForm.submitBtn.setEnabled(true);
            workForm.submitBtn.setVisible(true);
        }
        backAddTime();
        backPaintTime();
        TimerYeah.addTime(id);
        //缩小到托盘按钮
        minButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FRAME.setVisible(false);
            }
        });
        //关闭按钮
        outButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onExit();
            }
        });
        //设置拖动
        headPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mx = e.getX();
                my = e.getY();
                jfx = headPanel.getX();
                jfy = headPanel.getY();
            }
        });
        headPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                FRAME.setLocation(jfx + (e.getXOnScreen() - mx), jfy + (e.getYOnScreen() - my));
            }
        });
    }

    /**
     * 加载托盘图标
     */
    public void loadSystemTray() {
        if (!SystemTray.isSupported()) {
            return;
        }
        systemTray = SystemTray.getSystemTray();
        PopupMenu popupMenu = new PopupMenu();
        popupMenu.setFont(new Font("YaHei Consolas Hybrid", Font.PLAIN, 10));
        Menu pluginMenu = new Menu("插件");
        MenuItem restoreItem = new MenuItem("还原");
        MenuItem logoutItem = new MenuItem("注销");
        MenuItem exitItem = new MenuItem("退出");

        // 通过反射加载插件
//         try {
//             File pluginFile = new File("src/plugins");
//             if (pluginFile.isDirectory() && pluginFile.exists()) {
//                 File[] files = pluginFile.listFiles(new FileFilter() {
//                     @Override
//                     public boolean accept(File pathname) {
//                         String name = pathname.getName();
//                         if (name.contains(".class")) {
//                             return true;
//                         }
//                         return false;
//                     }
//                 });
//                 URL[] urls;
//                 if (files.length != 0 && files != null) {
//                     System.out.println("存在url");
//                     urls = new URL[files.length];
//                     for (int i = 0; i < files.length; i++) {
//                         urls[i] = files[i].toURI().toURL();
//                     }
//                     URLClassLoader classLoader = new URLClassLoader(urls);
//                     Class<?> plugin = classLoader.loadClass("aurora.timer.client.plugin.TimerPlugin");
//                     Method startMethod = plugin.getMethod("getName");
//                     String he = (String) startMethod.invoke(plugin.newInstance());
//                     //TODO:The Plugin System
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

        restoreItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!FRAME.isVisible()) {
                    FRAME.setVisible(true);
                }
            }
        });
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExit();
            }
        });
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences preferences = Preferences.userRoot().node(ServerURL.PRE_PATH);
                preferences.putBoolean("auto", false);
                TimerYeah.addTime(userData.getID());
                if (freshAddTimer.isRunning()) {
                    freshAddTimer.stop();
                }
                if (paintTimer.isRunning()) {
                    paintTimer.stop();
                }
                FRAME.dispose();
                systemTray.remove(trayIcon);
                LoginForm.main(new String[1]);
            }
        });

        popupMenu.add(restoreItem);
        popupMenu.addSeparator();
        popupMenu.add(logoutItem);
//        popupMenu.addSeparator();
//        popupMenu.add(pluginMenu);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        try {
            trayIcon = new TrayIcon(ImageIO.read(getClass().getResource("trayIcon.png")));
            trayIcon.setPopupMenu(popupMenu);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && !FRAME.isVisible()) {
                        FRAME.setVisible(true);
//                        systemTray.remove(trayIcon);
                    }
                }
            });
            systemTray.add(trayIcon);
        } catch (Exception e) {
            logger.warning("托盘初始化失败");
        }
    }

    public void onExit() {
        String[] option = {"退出", "取消"};
        int isExit = JOptionPane.showOptionDialog(null, "是否退出计时器？", "提示", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, option, option[0]);
        if (isExit == 0) {
            System.exit(2);
        }
    }

    public void setLastWeekRedPerson(int x) {
        loadWeekTime(1);
        Iterator<UserOnlineTime> uiIt = userOnlineTimes.iterator();

        //使用list存储并排序
        java.util.List<UserOnlineTime> list = new LinkedList<>();
        while (uiIt.hasNext()) {
            UserOnlineTime t = uiIt.next();
            list.add(t);
        }
        list.sort(new Comparator<UserOnlineTime>() {
            @Override
            public int compare(UserOnlineTime o1, UserOnlineTime o2) {
                if (o1.getTodayOnlineTime() > o2.getTodayOnlineTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        uiIt = list.iterator();
        theRedPerson = new String[x];
        for (int i = 0; i < x; i++) {
            if (uiIt.hasNext()) {
                theRedPerson[i] = uiIt.next().getID();
//                System.out.println(theRedPerson[i]);
            }
        }
    }

    public static void main(String args[]) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    FRAME = new MainFrame("极光");
                    Main2Form main2Form = new Main2Form(args[0], args[1]);
                    //设置上周前N名至theRedPerson
                    main2Form.setLastWeekRedPerson(3);

                    main2Form.loadWeekTime(0);

                    FRAME.setContentPane(main2Form.parent);
                    FRAME.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    FRAME.setLocation((d.width - FRAME.getWidth()) / 2, (d.height - FRAME.getHeight()) / 2);
//                    FRAME.setBounds((d.width - FRAME.getWidth()) / 2, (d.height - FRAME.getHeight()) / 2, FRAME.getWidth(), FRAME.getHeight());
                    FRAME.setResizable(false);
                    FRAME.setVisible(true);
                    FRAME.setAlwaysOnTop(true);
                    FRAME.setAlwaysOnTop(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
