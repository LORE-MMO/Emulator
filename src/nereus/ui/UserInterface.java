package nereus.ui;

import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.config.ConfigData;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import it.gotoandplay.smartfoxserver.extensions.ExtensionManager;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;

public class UserInterface
        extends JFrame {
    private static final long serialVersionUID = 1L;
    private World world;
    private Timer refreshTimer;
    private JLabel activeThreads;
    private JLabel aurasCount;
    private JButton btnAbout;
    private JButton btnClear;
    private JButton btnRefresh;
    private JButton btnReload;
    private JButton btnRestart;
    private JButton btnShutdown;
    private JCheckBox chkAuto;
    private JLabel dataIn;
    private JLabel dataOut;
    private JLabel dataTotal;
    private JLabel dbConnections;
    private JLabel idleConnections;
    private JLabel effectsCount;
    private JLabel enhancementsCount;
    private JLabel factionsCount;
    private JLabel hairsCount;
    private JLabel hairshopsCount;
    private JLabel highestUserCount;
    private JLabel itemCount;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel15;
    private JLabel jLabel16;
    private JLabel jLabel17;
    private JLabel jLabel18;
    private JLabel jLabel30;
    private JLabel jLabel19;
    private JLabel jLabel2;
    private JLabel jLabel20;
    private JLabel jLabel21;
    private JLabel jLabel22;
    private JLabel jLabel23;
    private JLabel jLabel24;
    private JLabel jLabel25;
    private JLabel jLabel26;
    private JLabel jLabel27;
    private JLabel jLabel28;
    private JLabel jLabel29;
    private JLabel jLabel3;
    private JLabel jLabel32;
    private JLabel jLabel33;
    private JLabel jLabel4;
    private JLabel jLabel44;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JTabbedPane mainTabPane;
    private JLabel mapsCount;
    private JLabel memoryFree;
    private JLabel memoryPercent;
    private JProgressBar memoryProgress;
    private JLabel memoryTotal;
    private JLabel memoryUsed;
    private JLabel monstersCount;
    private JLabel numOfRestarts;
    private JPanel panelStatus;
    private JLabel partyCount;
    private JLabel questsCount;
    private JLabel rooms;
    private JLabel serverRates;
    private JLabel shopsCount;
    private JLabel skillsCount;
    private JLabel socketsConnected;
    private JLabel upTime;
    private JLabel users;

    public UserInterface(World world) throws IOException {
        initComponents();
        setLocationRelativeTo(null);
        Image i = ImageIO.read(getClass().getResource("/nereus/ui/icon.ico"));
        setIconImage(i);
        this.world = world;
        this.refreshTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.refreshTimerActionPerformed(evt);
            }
        });
        this.refreshTimer.setRepeats(true);
        refresh();
        this.chkAuto.setSelected(true);
    }

    private void refreshTimerActionPerformed(ActionEvent evt) {
        refresh();
    }

    private String getUptime() {
        StringBuilder result = new StringBuilder();
        long now = System.currentTimeMillis();
        long start = SmartFoxServer.getInstance().getServerStartTime();
        long elapsed = now - start;
        int days = (int) Math.floor(elapsed / 86400000L);
        long temp = 86400000L * days;
        elapsed -= temp;
        int hours = (int) Math.floor(elapsed / 3600000L);
        temp = 3600000 * hours;
        elapsed -= temp;
        int minutes = (int) Math.floor(elapsed / 60000L);
        String s_days = String.valueOf(days);
        for (int i = 0; i < 4 - s_days.length(); i++) {
            result.append("0");
        }
        result.append(s_days);
        result.append(":");
        if (hours < 10) {
            result.append("0");
        }
        result.append(hours);
        result.append(":");
        if (minutes < 10) {
            result.append("0");
        }
        result.append(minutes);
        return result.toString();
    }

    private void refresh() {
        if ((this.world == null) || (this.world.db == null)) {
            return;
        }
        this.upTime.setText(getUptime());
        this.users.setText(String.valueOf(SmartFoxServer.getInstance().getGlobalUserCount()));
        this.rooms.setText(String.valueOf(SmartFoxServer.getInstance().getRoomNumber()));
        this.highestUserCount.setText(String.valueOf(ConfigData.maxSimultanousConnections));
        this.socketsConnected.setText(String.valueOf(SmartFoxServer.getInstance().getChannels().size()));
        this.activeThreads.setText(String.valueOf(Thread.activeCount()));
        this.numOfRestarts.setText(String.valueOf(ConfigData.restartCount));
        this.dbConnections.setText(String.valueOf(world.db.getActiveConnections()));
        this.idleConnections.setText(String.valueOf(world.db.getIdleConnections()));
        this.dataOut.setText(FileUtils.byteCountToDisplaySize(ConfigData.dataOUT));
        this.dataIn.setText(FileUtils.byteCountToDisplaySize(ConfigData.dataIN));
        this.dataTotal.setText(FileUtils.byteCountToDisplaySize(ConfigData.dataIN + ConfigData.dataOUT));
        Runtime rt = Runtime.getRuntime();
        this.memoryUsed.setText(FileUtils.byteCountToDisplaySize(rt.totalMemory() - rt.freeMemory()));
        this.memoryTotal.setText(FileUtils.byteCountToDisplaySize(rt.totalMemory()));
        this.memoryFree.setText(FileUtils.byteCountToDisplaySize(rt.freeMemory()));
        this.memoryProgress.setMaximum(Long.valueOf(rt.totalMemory()).intValue());
        this.memoryProgress.setValue(Long.valueOf(rt.totalMemory() - rt.freeMemory()).intValue());
        int percentage = (int) ((rt.totalMemory() - rt.freeMemory()) * 100.0D / rt.totalMemory() + 0.5D);
        this.memoryPercent.setText(percentage + "%");
        this.itemCount.setText(String.valueOf(this.world.items.size()));
        this.skillsCount.setText(String.valueOf(this.world.skills.size()));
        this.mapsCount.setText(String.valueOf(this.world.areas.size()));
        this.shopsCount.setText(String.valueOf(this.world.shops.size()));
        this.aurasCount.setText(String.valueOf(this.world.auras.size()));
        this.monstersCount.setText(String.valueOf(this.world.monsters.size()));
        this.questsCount.setText(String.valueOf(this.world.quests.size()));
        this.factionsCount.setText(String.valueOf(this.world.factions.size()));
        this.enhancementsCount.setText(String.valueOf(this.world.enhancements.size()));
        this.hairshopsCount.setText(String.valueOf(this.world.hairshops.size()));
        this.hairsCount.setText(String.valueOf(this.world.hairs.size()));
        this.effectsCount.setText(String.valueOf(this.world.effects.size()));
        this.partyCount.setText(String.valueOf(this.world.parties.size()));
        this.serverRates.setText(String.format("Server Rates: %dx EXP, %dx Gold, %dx Rep, %dx CP", new Object[]{this.world.EXP_RATE, this.world.GOLD_RATE, this.world.REP_RATE, this.world.CP_RATE}));
    }

    private void initComponents() {
        this.mainTabPane = new JTabbedPane();
        this.panelStatus = new JPanel();
        this.btnRefresh = new JButton();
        this.chkAuto = new JCheckBox();
        this.jPanel1 = new JPanel();
        this.jLabel1 = new JLabel();
        this.jLabel2 = new JLabel();
        this.jLabel3 = new JLabel();
        this.jLabel4 = new JLabel();
        this.jLabel5 = new JLabel();
        this.jLabel6 = new JLabel();
        this.jLabel7 = new JLabel();
        this.numOfRestarts = new JLabel();
        this.activeThreads = new JLabel();
        this.socketsConnected = new JLabel();
        this.highestUserCount = new JLabel();
        this.users = new JLabel();
        this.rooms = new JLabel();
        this.upTime = new JLabel();
        this.jLabel17 = new JLabel();
        this.jLabel27 = new JLabel();
        this.partyCount = new JLabel();
        this.jPanel2 = new JPanel();
        this.jLabel18 = new JLabel();
        this.jLabel30 = new JLabel();
        this.dbConnections = new JLabel();
        this.idleConnections = new JLabel();
        this.jLabel32 = new JLabel();
        this.jPanel3 = new JPanel();
        this.jLabel19 = new JLabel();
        this.dataIn = new JLabel();
        this.jLabel33 = new JLabel();
        this.jLabel20 = new JLabel();
        this.dataOut = new JLabel();
        this.jLabel21 = new JLabel();
        this.dataTotal = new JLabel();
        this.jPanel4 = new JPanel();
        this.jLabel8 = new JLabel();
        this.memoryProgress = new JProgressBar();
        this.memoryPercent = new JLabel();
        this.jLabel10 = new JLabel();
        this.jLabel11 = new JLabel();
        this.memoryTotal = new JLabel();
        this.memoryUsed = new JLabel();
        this.jLabel12 = new JLabel();
        this.memoryFree = new JLabel();
        this.jPanel6 = new JPanel();
        this.jLabel9 = new JLabel();
        this.jLabel13 = new JLabel();
        this.jLabel14 = new JLabel();
        this.jLabel15 = new JLabel();
        this.jLabel16 = new JLabel();
        this.jLabel22 = new JLabel();
        this.jLabel23 = new JLabel();
        this.itemCount = new JLabel();
        this.effectsCount = new JLabel();
        this.skillsCount = new JLabel();
        this.mapsCount = new JLabel();
        this.hairsCount = new JLabel();
        this.shopsCount = new JLabel();
        this.jPanel7 = new JPanel();
        this.jLabel24 = new JLabel();
        this.jLabel25 = new JLabel();
        this.jLabel26 = new JLabel();
        this.jLabel28 = new JLabel();
        this.jLabel29 = new JLabel();
        this.questsCount = new JLabel();
        this.enhancementsCount = new JLabel();
        this.monstersCount = new JLabel();
        this.hairshopsCount = new JLabel();
        this.aurasCount = new JLabel();
        this.jLabel44 = new JLabel();
        this.factionsCount = new JLabel();
        this.btnRestart = new JButton();
        this.btnClear = new JButton();
        this.btnShutdown = new JButton();
        this.btnAbout = new JButton();
        this.serverRates = new JLabel();
        this.btnReload = new JButton();
        setDefaultCloseOperation(0);
        ResourceBundle bundle = ResourceBundle.getBundle("nereus/ui/Bundle");
        String pad = "";
        //pad = String.format("%" + (125 - 14) + "s", pad);
        setTitle(pad + bundle.getString("UserInterface.title"));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setResizable(false);
        this.btnRefresh.setText(bundle.getString("UserInterface.btnRefresh.text"));
        this.btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.btnRefreshActionPerformed(evt);
            }
        });
        this.chkAuto.setText(bundle.getString("UserInterface.chkAuto.text"));
        this.chkAuto.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {
                UserInterface.this.chkAutoItemStateChanged(evt);
            }

        });
        this.jPanel1.setMaximumSize(new Dimension(250, 200));
        this.jPanel1.setMinimumSize(new Dimension(250, 200));
        this.jPanel1.setPreferredSize(new Dimension(250, 200));
        this.jLabel1.setText(bundle.getString("UserInterface.jLabel1.text"));
        this.jLabel2.setText(bundle.getString("UserInterface.jLabel2.text"));
        this.jLabel3.setText(bundle.getString("UserInterface.jLabel3.text"));
        this.jLabel4.setText(bundle.getString("UserInterface.jLabel4.text"));
        this.jLabel5.setText(bundle.getString("UserInterface.jLabel5.text"));
        this.jLabel6.setText(bundle.getString("UserInterface.jLabel6.text"));
        this.jLabel7.setText(bundle.getString("UserInterface.jLabel7.text"));
        this.numOfRestarts.setText(bundle.getString("UserInterface.numOfRestarts.text"));
        this.activeThreads.setText(bundle.getString("UserInterface.activeThreads.text"));
        this.socketsConnected.setText(bundle.getString("UserInterface.socketsConnected.text"));
        this.highestUserCount.setText(bundle.getString("UserInterface.highestUserCount.text"));
        this.users.setText(bundle.getString("UserInterface.users.text"));
        this.rooms.setText(bundle.getString("UserInterface.rooms.text"));
        this.upTime.setText(bundle.getString("UserInterface.upTime.text"));
        this.jLabel17.setFont(new Font("Tahoma", 1, 11));
        this.jLabel17.setText(bundle.getString("UserInterface.jLabel17.text"));
        this.jLabel27.setText(bundle.getString("UserInterface.jLabel27.text"));
        this.partyCount.setText(bundle.getString("UserInterface.partyCount.text"));
        GroupLayout jPanel1Layout = new GroupLayout(this.jPanel1);
        this.jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jLabel7, GroupLayout.Alignment.TRAILING)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(this.jLabel5)
                                                                        .addComponent(this.jLabel6)
                                                                        .addComponent(this.jLabel3)
                                                                        .addComponent(this.jLabel4)
                                                                        .addComponent(this.jLabel1)
                                                                        .addComponent(this.jLabel2))
                                                                .addGap(1, 1, 1)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 126, 32767)
                                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.numOfRestarts, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.rooms, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.upTime, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.highestUserCount, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.users, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.socketsConnected, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.activeThreads, GroupLayout.Alignment.TRAILING)))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(this.jLabel17)
                                                .addGap(0, 0, 32767))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(this.jLabel27)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.partyCount)))
                                .addContainerGap())
        );

        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jLabel17)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel1)
                                        .addComponent(this.upTime))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel2)
                                        .addComponent(this.rooms))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel3)
                                        .addComponent(this.users))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel4)
                                        .addComponent(this.highestUserCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel5).addComponent(this.socketsConnected))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel6)
                                        .addComponent(this.activeThreads))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel7)
                                        .addComponent(this.numOfRestarts))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel27)
                                        .addComponent(this.partyCount))
                                .addContainerGap(15, 32767))
        );

        this.jPanel2.setMaximumSize(new Dimension(250, 200));
        this.jPanel2.setMinimumSize(new Dimension(250, 200));
        this.jPanel2.setPreferredSize(new Dimension(250, 100));
        this.jLabel18.setText(bundle.getString("UserInterface.jLabel18.text"));
        this.jLabel30.setText(bundle.getString("UserInterface.jLabel30.text"));
        this.dbConnections.setText(bundle.getString("UserInterface.dbConnections.text"));
        this.idleConnections.setText(bundle.getString("UserInterface.idleConnections.text"));
        this.jLabel32.setFont(new Font("Tahoma", 1, 11));
        this.jLabel32.setText(bundle.getString("UserInterface.jLabel32.text"));
        GroupLayout jPanel2Layout = new GroupLayout(this.jPanel2);
        this.jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(this.jLabel18)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 128, 32767)
                                                .addComponent(this.dbConnections))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(this.jLabel30)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 128, 32767)
                                                .addComponent(this.idleConnections))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(this.jLabel32)
                                                .addGap(0, 0, 32767)))
                                .addContainerGap())
        );

        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(this.jLabel32)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.dbConnections)
                                        .addComponent(this.jLabel18))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.idleConnections)
                                        .addComponent(this.jLabel30))
                                .addContainerGap(-1, 32767))
        );

        this.jPanel3.setMaximumSize(new Dimension(250, 200));
        this.jPanel3.setMinimumSize(new Dimension(250, 200));
        this.jPanel3.setPreferredSize(new Dimension(250, 100));
        this.jLabel19.setText(bundle.getString("UserInterface.jLabel19.text"));
        this.dataIn.setText(bundle.getString("UserInterface.dataIn.text"));
        this.jLabel33.setFont(new Font("Tahoma", 1, 11));
        this.jLabel33.setText(bundle.getString("UserInterface.jLabel33.text"));
        this.jLabel20.setText(bundle.getString("UserInterface.jLabel20.text"));
        this.dataOut.setText(bundle.getString("UserInterface.dataOut.text"));
        this.jLabel21.setText(bundle.getString("UserInterface.jLabel21.text"));
        this.dataTotal.setText(bundle.getString("UserInterface.dataTotal.text"));
        GroupLayout jPanel3Layout = new GroupLayout(this.jPanel3);
        this.jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(this.jLabel19)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.dataIn))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(this.jLabel33)
                                                .addGap(0, 0, 32767))
                                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                                .addComponent(this.jLabel20)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 176, 32767)
                                                .addComponent(this.dataOut))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(this.jLabel21)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.dataTotal)))
                                .addContainerGap())
        );

        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jLabel33)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel19)
                                        .addComponent(this.dataIn))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel20)
                                        .addComponent(this.dataOut))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel21)
                                        .addComponent(this.dataTotal))
                                .addContainerGap(-1, 32767))
        );

        this.jLabel8.setFont(new Font("Tahoma", 1, 11));
        this.jLabel8.setText(bundle.getString("UserInterface.jLabel8.text"));
        this.memoryPercent.setLabelFor(this.memoryProgress);
        this.memoryPercent.setText(bundle.getString("UserInterface.memoryPercent.text"));
        this.jLabel10.setText(bundle.getString("UserInterface.jLabel10.text"));
        this.jLabel11.setText(bundle.getString("UserInterface.jLabel11.text"));
        this.memoryTotal.setText(bundle.getString("UserInterface.memoryTotal.text"));
        this.memoryUsed.setText(bundle.getString("UserInterface.memoryUsed.text"));
        this.jLabel12.setText(bundle.getString("UserInterface.jLabel12.text"));
        this.memoryFree.setText(bundle.getString("UserInterface.memoryFree.text"));
        GroupLayout jPanel4Layout = new GroupLayout(this.jPanel4);
        this.jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(this.jLabel10)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.memoryTotal))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(this.jLabel11)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.memoryUsed))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(this.jLabel8)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.memoryProgress, -1, 147, 32767)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(this.memoryPercent))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(this.jLabel12)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.memoryFree)))
                                .addContainerGap())
        );

        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jLabel8)
                                        .addComponent(this.memoryProgress, -2, -1, -2)
                                        .addComponent(this.memoryPercent))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jLabel10)
                                        .addComponent(this.memoryTotal))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jLabel11)
                                        .addComponent(this.memoryUsed))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel12)
                                        .addComponent(this.memoryFree))
                                .addContainerGap(15, 32767))
        );

        this.jLabel9.setFont(new Font("Tahoma", 1, 11));
        this.jLabel9.setText(bundle.getString("UserInterface.jLabel9.text"));
        this.jLabel13.setText(bundle.getString("UserInterface.jLabel13.text"));
        this.jLabel14.setText(bundle.getString("UserInterface.jLabel14.text"));
        this.jLabel15.setText(bundle.getString("UserInterface.jLabel15.text"));
        this.jLabel16.setText(bundle.getString("UserInterface.jLabel16.text"));
        this.jLabel22.setText(bundle.getString("UserInterface.jLabel22.text"));
        this.jLabel23.setText(bundle.getString("UserInterface.jLabel23.text"));
        this.itemCount.setText(bundle.getString("UserInterface.itemCount.text"));
        this.effectsCount.setText(bundle.getString("UserInterface.effectsCount.text"));
        this.skillsCount.setText(bundle.getString("UserInterface.skillsCount.text"));
        this.mapsCount.setText(bundle.getString("UserInterface.mapsCount.text"));
        this.hairsCount.setText(bundle.getString("UserInterface.hairsCount.text"));
        this.shopsCount.setText(bundle.getString("UserInterface.shopsCount.text"));
        GroupLayout jPanel6Layout = new GroupLayout(this.jPanel6);
        this.jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel14)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.effectsCount))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel9)
                                                .addGap(0, 0, 32767))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel13)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.itemCount))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel15)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.skillsCount)).addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel16).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.mapsCount)).addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel22).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.hairsCount)).addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(this.jLabel23).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.shopsCount)))
                                .addContainerGap())
        );

        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jLabel9)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel13)
                                        .addComponent(this.itemCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel14)
                                        .addComponent(this.effectsCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel15)
                                        .addComponent(this.skillsCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel16)
                                        .addComponent(this.mapsCount))
                                .addGap(7, 7, 7)
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel22).addComponent(this.hairsCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel23).addComponent(this.shopsCount))
                                .addContainerGap(-1, 32767))
        );

        this.jLabel24.setText(bundle.getString("UserInterface.jLabel24.text"));
        this.jLabel25.setText(bundle.getString("UserInterface.jLabel25.text"));
        this.jLabel26.setText(bundle.getString("UserInterface.jLabel26.text"));
        this.jLabel28.setText(bundle.getString("UserInterface.jLabel28.text"));
        this.jLabel29.setText(bundle.getString("UserInterface.jLabel29.text"));
        this.questsCount.setText(bundle.getString("UserInterface.questsCount.text"));
        this.enhancementsCount.setText(bundle.getString("UserInterface.enhancementsCount.text"));
        this.monstersCount.setText(bundle.getString("UserInterface.monstersCount.text"));
        this.hairshopsCount.setText(bundle.getString("UserInterface.hairshopsCount.text"));
        this.aurasCount.setText(bundle.getString("UserInterface.aurasCount.text"));
        this.jLabel44.setText(bundle.getString("UserInterface.jLabel44.text"));
        this.factionsCount.setText(bundle.getString("UserInterface.factionsCount.text"));
        GroupLayout jPanel7Layout = new GroupLayout(this.jPanel7);
        this.jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(this.jLabel24)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.questsCount))
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(this.jLabel25)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.enhancementsCount))
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(this.jLabel26)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.monstersCount))
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(this.jLabel28)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.hairshopsCount))
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(this.jLabel29)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.aurasCount))
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(this.jLabel44)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.factionsCount)))
                                .addContainerGap())
        );

        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel25)
                                        .addComponent(this.enhancementsCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel26)
                                        .addComponent(this.monstersCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel28)
                                        .addComponent(this.hairshopsCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel29)
                                        .addComponent(this.aurasCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel44)
                                        .addComponent(this.factionsCount))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabel24)
                                        .addComponent(this.questsCount))
                                .addContainerGap(12, 32767))
        );

        this.btnRestart.setText(bundle.getString("UserInterface.btnRestart.text"));
        this.btnRestart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.btnRestartActionPerformed(evt);
            }
        });
        this.btnClear.setText(bundle.getString("UserInterface.btnClear.text"));
        this.btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.btnClearActionPerformed(evt);
            }
        });
        this.btnShutdown.setText(bundle.getString("UserInterface.btnShutdown.text"));
        this.btnShutdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.btnShutdownActionPerformed(evt);
            }
        });
        this.btnAbout.setText(bundle.getString("UserInterface.btnAbout.text"));
        this.btnAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.btnAboutActionPerformed(evt);
            }
        });
        this.serverRates.setText(bundle.getString("UserInterface.serverRates.text"));
        this.btnReload.setText(bundle.getString("UserInterface.btnReload.text"));
        this.btnReload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                UserInterface.this.btnReloadActionPerformed(evt);
            }
        });
        GroupLayout panelStatusLayout = new GroupLayout(this.panelStatus);
        this.panelStatus.setLayout(panelStatusLayout);

        panelStatusLayout.setHorizontalGroup(
                panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(panelStatusLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(panelStatusLayout.createSequentialGroup()
                                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jPanel1, -2, -1, -2)
                                                        .addComponent(this.jPanel2, -2, -1, -2))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(panelStatusLayout.createSequentialGroup()
                                                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(this.jPanel3, -2, -1, -2)
                                                                        .addComponent(this.jPanel6, -1, -1, 32767))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(this.jPanel4, -1, -1, 32767)
                                                                        .addComponent(this.jPanel7, -1, -1, 32767)))
                                                        .addGroup(panelStatusLayout.createSequentialGroup()
                                                                .addGap(0, 0, 32767)
                                                                .addComponent(this.chkAuto)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(this.btnRefresh))))
                                        .addGroup(panelStatusLayout.createSequentialGroup()
                                                .addComponent(this.btnShutdown)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.btnRestart, -2, 76, -2)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.btnAbout, -2, 73, -2)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.btnClear)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.btnReload)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                                .addComponent(this.serverRates)))
                                .addContainerGap())
        );

        panelStatusLayout.setVerticalGroup(
                panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(panelStatusLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addGroup(panelStatusLayout.createSequentialGroup()
                                                .addComponent(this.jPanel1, -2, -1, -2)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.jPanel2, -2, 0, 32767))
                                        .addGroup(panelStatusLayout.createSequentialGroup()
                                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jPanel3, -2, -1, -2)
                                                        .addComponent(this.jPanel4, -2, -1, -2))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jPanel7, -2, -1, -2)
                                                        .addComponent(this.jPanel6, -2, -1, -2))))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.btnRefresh)
                                        .addComponent(this.chkAuto))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)
                                .addGroup(panelStatusLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.btnShutdown)
                                        .addComponent(this.btnRestart)
                                        .addComponent(this.btnAbout)
                                        .addComponent(this.btnClear)
                                        .addComponent(this.serverRates)
                                        .addComponent(this.btnReload))
                                .addContainerGap())
        );

        this.mainTabPane.addTab(bundle.getString("UserInterface.panelStatus.TabConstraints.tabTitle"), this.panelStatus);
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.mainTabPane));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.mainTabPane));
        pack();
    }

    private void btnShutdownActionPerformed(ActionEvent evt) {
        int result = MessageBox.showConfirm("Continue shutdown operation? Players will rage.", "Confirmation", 0);
        if (result == 0) {
            this.world.send(new String[]{"logoutWarning", "", "60"}, this.world.zone.getChannelList());
            this.world.shutdown();
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(2L));
            } catch (InterruptedException ex) {
            }
            System.exit(0);
        }
    }

    private void btnClearActionPerformed(ActionEvent evt) {
        if (this.world.retrieveDatabaseObject("all")) {
            MessageBox.showMessage("Data Memory Cleared!", "Operation Successful");
        }
    }

    private void btnRestartActionPerformed(ActionEvent evt) {
        int result = MessageBox.showConfirm("Continue restart operation? Players will rage.", "Confirmation", 0);
        if (result == 0) {
            this.world.send(new String[]{"logoutWarning", "", "60"}, this.world.zone.getChannelList());
            this.world.shutdown();
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(2L));
            } catch (InterruptedException ex) {
            }
            ExtensionHelper.instance().rebootServer();
        }
    }

    private void chkAutoItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == 2) {
            this.refreshTimer.stop();
        } else if (evt.getStateChange() == 1) {
            this.refreshTimer.start();
        }
    }

    private void btnRefreshActionPerformed(ActionEvent evt) {
        refresh();
    }

    private void btnAboutActionPerformed(ActionEvent evt) {
        MessageBox.showMessage("Aghatosune (NGEWExt v69) by Frengki Putri" + System.getProperty("line.separator") + "(c) 2069 Ngewreus", "About");
    }

    private void btnReloadActionPerformed(ActionEvent evt) {
        String zoneName = "zone_master";
        String extName = "zm";
        Zone zone = SmartFoxServer.getInstance().getZone(zoneName);
        if (zone != null) {
            ExtensionManager em = zone.getExtManager();
            if (em != null) {
                em.reloadExtension(extName);
            }
        }
    }
}