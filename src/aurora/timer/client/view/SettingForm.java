package aurora.timer.client.view;

import aurora.timer.client.ServerURL;
import aurora.timer.client.view.until.CustomFileChooser;
//import org.omg.CORBA.FREE_MEM;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Created by hao on 17-4-27.
 */
public class SettingForm {
    private static JFrame FRAME;
    private JPanel parent;
    private JPanel Main2FormParent;
    private JButton OkButton;
    private JButton CancelButton;
    private CustomFileChooser fileChooser;
    private JButton selectBgImgBtn;
    private JComboBox<String> imgComboBox;
    private final Preferences preferences = Preferences.userRoot().node(ServerURL.PRE_PATH);
    ;
    private String filePath;

    public SettingForm(JPanel Main2FormParent) {
        initComboBox();
        filePath = preferences.get("bg", "res" + File.separator + "bg.png");
        setBgForThisParent(ServerURL.BG_PATH);
        this.Main2FormParent = Main2FormParent;
        CancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FRAME.dispose();
            }
        });
        OkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setBgForMain2FormParent(filePath);
                //TODO:上传图片到服务器
                FRAME.dispose();
            }
        });

        selectBgImgBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileChooser = new CustomFileChooser();
                fileChooser.setMultiSelectionEnabled(false);
                FileFilter filter = new FileNameExtensionFilter("图片(PNG,JPG,JPEG)", "png", "jpg", "jpeg");
                fileChooser.setFileFilter(filter);

                if (fileChooser.showOpenDialog(selectBgImgBtn) == JFileChooser.APPROVE_OPTION) {
                    filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    // 预览背景图
                    setBgForThisParent(filePath);
                }
            }
        });
    }

    private void initComboBox() {
        imgComboBox.addItem("");
        imgComboBox.addItem("经典1");
        imgComboBox.addItem("经典2");
        imgComboBox.addItem("经典3");
        imgComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String imgName = (String) imgComboBox.getSelectedItem();
                    if (imgName.equals("经典1")) {
                        filePath = "res" + File.separator + "bg.png";
                        setBgForThisParent(filePath);
                    } else if (imgName.equals("经典2")) {
                        filePath = "res" + File.separator + "bg4.png";
                        setBgForThisParent(filePath);
                    } else if (imgName.equals("经典3")) {
                        filePath = "res" + File.separator + "bg5.png";
                        setBgForThisParent(filePath);
                    }
                }
            }
        });
    }

    private void setBgForThisParent(String filePath) {
        parent.setUI(new BasicPanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                File bg = new File(filePath);
                if (bg.exists()) {
                    g.drawImage(new ImageIcon(bg.getPath()).getImage(), 0, 0, c.getWidth(), c.getHeight(), null);
                } else {
                    g.drawImage(new ImageIcon(getClass().getResource("bg.png")).getImage(), 0, 0, c.getWidth(), c.getHeight(), null);
                }
            }
        });
    }

    private void setBgForMain2FormParent(String filePath) {
        Main2FormParent.setUI(new BasicPanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                File bg = new File(filePath);
                ServerURL.BG_PATH = filePath;
                if (bg.exists()) {
                    g.drawImage(new ImageIcon(bg.getPath()).getImage(), 0, 0, c.getWidth(), c.getHeight(), null);
                    preferences.put("bg", filePath);
                } else {
                    g.drawImage(new ImageIcon(getClass().getResource("bg.png")).getImage(), 0, 0, c.getWidth(), c.getHeight(), null);
                    preferences.put("bg", "res" + File.separator + "bg.png");
                }
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRect(0, 0, c.getWidth(), 37);
                g2.setColor(new Color(50, 50, 50, 200));
                g2.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
            }
        });
    }

    public static void main(JPanel Main2FormParent) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    FRAME = new MainFrame("设置");
                    SettingForm settingForm = new SettingForm(Main2FormParent);
                    FRAME.setContentPane(settingForm.parent);
                    FRAME.setBounds((d.width - FRAME.getWidth()) / 2, (d.height - FRAME.getHeight()) / 2, FRAME.getWidth(), FRAME.getHeight());
                    FRAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    FRAME.setResizable(false);
                    FRAME.setVisible(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
