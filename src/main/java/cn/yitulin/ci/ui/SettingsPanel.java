/*
 * Created by JFormDesigner on Sat Nov 06 22:50:50 CST 2021
 */

package cn.yitulin.ci.ui;

import cn.yitulin.ci.infrastructure.common.Constants;
import cn.yitulin.ci.infrastructure.common.enums.BrowserEnum;
import cn.yitulin.ci.infrastructure.common.exception.ErrorEnum;
import cn.yitulin.ci.infrastructure.model.PluginConfig;
import cn.yitulin.ci.infrastructure.service.PluginConfigService;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;

/**
 * @author unknown
 */
@Slf4j
public class SettingsPanel extends JPanel implements Configurable {

    private ButtonGroup radioButtonGroup;

    private PluginConfigService pluginConfigService = PluginConfigService.getInstance();

    public SettingsPanel() {
        initComponents();
        radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(chromeRadioButton);
        radioButtonGroup.add(edgeRadioButton);
        PluginConfig pluginConfig = pluginConfigService.read();
        if (Objects.isNull(pluginConfig)) {
            return;
        }
        if (StringUtils.isNotBlank(pluginConfig.getConfigFileDirectory())) {
            saveDirPath.setText(pluginConfig.getConfigFileDirectory());
        } else {
            saveDirPath.setText(Constants.CONFIG_FILE_DIRECTORY_PLACEHOLDER);
        }
        if (StringUtils.isNotBlank(pluginConfig.getCookieDbPath())) {
            customCookieDbPathTextField.setText(pluginConfig.getCookieDbPath());
        }
        if (StringUtils.isNotBlank(pluginConfig.getBrowserType())) {
            if (BrowserEnum.MICROSOFT_EDGE.getName().equals(pluginConfig.getBrowserType())) {
                edgeRadioButton.setSelected(true);
            }
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "HttpInvokerIdeaPlugin设置";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this;
    }

    @Override
    public boolean isModified() {
        PluginConfig pluginConfig = pluginConfigService.read();
        if (Objects.isNull(pluginConfig)) {
            pluginConfig = new PluginConfig();
        }
        PluginConfig newPluginConfig = getNewGlobalConfig();
        return !pluginConfig.equals(newPluginConfig);
    }

    @Override
    public void apply() {
        PluginConfig pluginConfig = getNewGlobalConfig();
        if (StringUtils.isNotBlank(pluginConfig.getCookieDbPath()) && !pluginConfig.getCookieDbPath().endsWith("Cookies")) {
            ErrorEnum.CONFIG_COOKIE_DB_PATH_ILLEGAL.showErrorDialog();
            return;
        }
        pluginConfigService.save(pluginConfig);
        Project project = DataManager.getInstance().getDataContext(this).getData(CommonDataKeys.PROJECT);
        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(pluginConfig.concatDefaultConfigFilePath());
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, fileByPath);
        openFileDescriptor.navigate(true);
    }

    private PluginConfig getNewGlobalConfig() {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setConfigFileDirectory(Constants.CONFIG_FILE_DIRECTORY_PLACEHOLDER.equals(saveDirPath.getText()) ? "" : saveDirPath.getText());
        pluginConfig.setBrowserType(chromeRadioButton.isSelected() ? BrowserEnum.GOOGLE_CHROME.getName() : BrowserEnum.MICROSOFT_EDGE.getName());
        pluginConfig.setCookieDbPath(customCookieDbPathTextField.getText());
        return pluginConfig;
    }

    private void selectSaveDirButtonActionPerformed(ActionEvent e) {
        log.info("===选择配置文件存储文件夹===");
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File(""));
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setMultiSelectionEnabled(false);
        int openDialog = jFileChooser.showOpenDialog(this);
        if (openDialog != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = jFileChooser.getSelectedFile();
        log.info("选中的配置文件存储文件夹是：[{}]", selectedFile.getPath());
        saveDirPath.setText(selectedFile.getPath());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        label1 = new JLabel();
        panel2 = new JPanel();
        chromeRadioButton = new JRadioButton();
        edgeRadioButton = new JRadioButton();
        label2 = new JLabel();
        customCookieDbPathTextField = new JTextField();
        label4 = new JLabel();
        panel1 = new JPanel();
        saveDirPath = new JLabel();
        selectSaveDirButton = new JButton();
        vSpacer1 = new JPanel(null);

        //======== this ========
        setMinimumSize(new Dimension(800, 500));
        setPreferredSize(new Dimension(640, 400));
//        setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(
//                0, 0, 0, 0), "JF\u006frm\u0044es\u0069gn\u0065r \u0045va\u006cua\u0074io\u006e", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder
//                .BOTTOM, new java.awt.Font("D\u0069al\u006fg", java.awt.Font.BOLD, 12), java.awt.Color.
//                red), getBorder()));
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.
                                               beans.PropertyChangeEvent e) {
                if ("\u0062or\u0064er".equals(e.getPropertyName())) throw new RuntimeException();
            }
        });
        setLayout(new GridBagLayout());
        ((GridBagLayout) getLayout()).columnWidths = new int[]{0, 0, 0};
        ((GridBagLayout) getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0};
        ((GridBagLayout) getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
        ((GridBagLayout) getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("\u6d4f\u89c8\u5668\uff1a");
        add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

        //======== panel2 ========
        {
            panel2.setLayout(new FlowLayout());

            //---- chromeRadioButton ----
            chromeRadioButton.setText("Google Chrome");
            chromeRadioButton.setSelected(true);
            panel2.add(chromeRadioButton);

            //---- edgeRadioButton ----
            edgeRadioButton.setText("Microsoft Edge");
            panel2.add(edgeRadioButton);
        }
        add(panel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 0), 0, 0));

        //---- label2 ----
        label2.setText("\u81ea\u5b9a\u4e49Cookie\u6570\u636e\u5e93\u5730\u5740\uff1a");
        add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));
        add(customCookieDbPathTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

        //---- label4 ----
        label4.setText("\u914d\u7f6e\u5b58\u653e\u8def\u5f84\uff1a");
        add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout());

            //---- saveDirPath ----
            saveDirPath.setText("\u8bf7\u9009\u62e9\u7528\u4e8e\u5b58\u50a8\u914d\u7f6e\u6587\u4ef6\u6587\u4ef6\u5939");
            panel1.add(saveDirPath);

            //---- selectSaveDirButton ----
            selectSaveDirButton.setText("\u9009\u62e9\u6587\u4ef6\u5939");
            selectSaveDirButton.addActionListener(e -> selectSaveDirButtonActionPerformed(e));
            panel1.add(selectSaveDirButton);
        }
        add(panel1, new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 5, 0), 0, 0));
        add(vSpacer1, new GridBagConstraints(1, 3, 1, 1, 0.0, 5.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JLabel label1;
    private JPanel panel2;
    private JRadioButton chromeRadioButton;
    private JRadioButton edgeRadioButton;
    private JLabel label2;
    private JTextField customCookieDbPathTextField;
    private JLabel label4;
    private JPanel panel1;
    private JLabel saveDirPath;
    private JButton selectSaveDirButton;
    private JPanel vSpacer1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
