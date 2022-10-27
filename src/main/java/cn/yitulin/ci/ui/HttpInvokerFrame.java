/*
 * Created by JFormDesigner on Mon Jul 04 16:57:59 CST 2022
 */

package cn.yitulin.ci.ui;

import cn.hutool.json.JSONUtil;
import cn.yitulin.ci.infrastructure.common.Constants;
import cn.yitulin.ci.infrastructure.common.enums.HttpMethodEnum;
import cn.yitulin.ci.infrastructure.common.event.EventBusCenter;
import cn.yitulin.ci.infrastructure.common.event.InvokeResponseMessage;
import cn.yitulin.ci.infrastructure.common.exception.ActionException;
import cn.yitulin.ci.infrastructure.common.exception.ErrorEnum;
import cn.yitulin.ci.infrastructure.common.util.JsonStringUtil;
import cn.yitulin.ci.infrastructure.model.*;
import cn.yitulin.ci.infrastructure.service.DomainConfigService;
import cn.yitulin.ci.infrastructure.service.InvokeLogService;
import cn.yitulin.ci.infrastructure.service.InvokeService;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author unknown
 */
@Slf4j
public class HttpInvokerFrame extends JFrame {

    private long eventId;

    private boolean stopCall = false;

    private Invoker invoker;

    private Map<String, JTextArea> paramJTextAreaMap = Maps.newHashMap();

    public HttpInvokerFrame(Invoker invoker) {
        this.invoker = invoker;
        initComponents();
        this.setTitle(this.invoker.getMethodDesc().buildApiPath());
        whenPressEsc();
        whenWindowClose();
        initDomainComboBoxItem();
        initHttpMethodComboBoxItem(this.invoker.getMethodDesc().getAnnotationDesc().getHttpMethod());
        urlTextField.setText(this.invoker.getMethodDesc().buildUrl(domainComboBox.getSelectedItem().toString()));
        refreshParamsLayout(this.invoker.getMethodDesc());
        refreshParamsFromLog();
        EventBusCenter.register(this);
        setVisible(true);
    }

    @Subscribe
    public void listenResponse(InvokeResponseMessage message) {
        if (message.getEventId() != eventId) {
            return;
        }
        responseTimeLabel.setText("响应时间：" + message.getTime().format(DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)));
        responseTextArea.setText(JsonStringUtil.toFormatJsonStr(message.getResponse()));
    }

    private void refreshParamsFromLog() {
        String url = urlTextField.getText();
        Object methodComboBoxSelectedItem = methodComboBox.getSelectedItem();
        if (Objects.isNull(methodComboBoxSelectedItem)) {
            return;
        }
        String httpMethod = methodComboBoxSelectedItem.toString();
        InvokeLog invokeLog = InvokeLogService.getInstance().readLog(true, url + "_" + httpMethod);
        if (Objects.isNull(invokeLog)) {
            refreshDefaultParams();
            return;
        }
        for (ParameterDesc parameterDesc : invoker.getMethodDesc().getParameterDescs()) {
            Object parameterLog = invokeLog.getParams().get(parameterDesc.getName());
            fillParamTextArea(parameterDesc, parameterLog);
        }
        requestTimeLabel.setText("请求时间：" + invokeLog.getInvokeTime().format(DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)));
        String responseData = invokeLog.getResponseData();
        responseTextArea.setText(JsonStringUtil.toFormatJsonStr(responseData));
    }

    private void fillParamTextArea(ParameterDesc parameterDesc, Object parameterLog) {
        paramJTextAreaMap.get(parameterDesc.getName()).setText(JsonStringUtil.toFormatJsonStr(parameterLog));
    }

    private void refreshDefaultParams() {
        for (ParameterDesc parameterDesc : invoker.getMethodDesc().getParameterDescs()) {
            Object defaultValue = parameterDesc.getDefaultValue();
            fillParamTextArea(parameterDesc, defaultValue);
        }
        responseTextArea.setText("");
    }

    private void initHttpMethodComboBoxItem(String defaultHtppMethod) {
        DefaultComboBoxModel methodComboBoxModel = (DefaultComboBoxModel) methodComboBox.getModel();
        HttpMethodEnum[] httpMethodEnums = HttpMethodEnum.values();
        for (int i = 0; i < httpMethodEnums.length; i++) {
            methodComboBoxModel.addElement(httpMethodEnums[i].name());
            if (httpMethodEnums[i].name().equals(defaultHtppMethod)) {
                methodComboBox.setSelectedIndex(i);
            }
        }
    }

    private void initDomainComboBoxItem() {
        List<String> domainNames = DomainConfigService.getInstance().readAllDomainName();
        if (CollectionUtils.isEmpty(domainNames)) {
            dispose();
            return;
        }
        DefaultComboBoxModel domainComboBoxModel = (DefaultComboBoxModel) domainComboBox.getModel();
        domainNames.stream().forEach(domainName -> domainComboBoxModel.addElement(domainName));
        if (domainNames.size() > 0) {
            domainComboBox.setSelectedIndex(0);
        }
    }

    private void refreshParamsLayout(MethodDesc methodDesc) {
        paramsPanel.setBackground(UIManager.getColor("Panel.background"));
        List<ParameterDesc> parameterDescs = methodDesc.getParameterDescs();
        for (int i = 0; i < parameterDescs.size(); i++) {
            String paramName = parameterDescs.get(i).getName();
            Object defaultValue = parameterDescs.get(i).getDefaultValue();
            appendParams(i, paramName, defaultValue);
        }
    }

    private void appendParams(int col, String parameterName, Object parameterValue) {
        //---- label ----
        JLabel label = new JLabel();
        label.setText(parameterName);
        label.setBackground(UIManager.getColor("Label.background"));
        label.setForeground(UIManager.getColor("Label.foreground"));
        paramsPanel.add(label, new GridBagConstraints(col, 0, 1, 1, 1.0, 0.5,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

        //======== scrollPane ========
        JScrollPane scrollPane = new JScrollPane();
        {
            JTextArea textArea = new JTextArea();
            //---- textArea ----
            textArea.setBackground(UIManager.getColor("TextArea.background"));
            textArea.setForeground(UIManager.getColor("TextArea.foreground"));
//            textArea.setForeground(Color.black);
//            textArea.setBackground(new Color(242, 242, 242));
            textArea.setLineWrap(true);
            textArea.setColumns(30);
            textArea.setRows(9);
            textArea.setMargin(new Insets(3, 3, 0, 0));
            textArea.setText(JsonStringUtil.toPrettyStr(parameterValue));
            scrollPane.setViewportView(textArea);
            paramJTextAreaMap.put(parameterName, textArea);
        }
        paramsPanel.add(scrollPane, new GridBagConstraints(col, 1, 1, 1, 1.0, 9.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    private void whenPressEsc() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        dialogPane.registerKeyboardAction(e -> {
            EventBusCenter.unregister(this);
            this.dispose();
        }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void whenWindowClose() {
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                EventBusCenter.unregister(this);
                dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    private void domainComboBoxItemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            urlTextField.setText(invoker.getMethodDesc().buildUrl(domainComboBox.getSelectedItem().toString()));
            refreshParamsFromLog();
        }
    }

    private void callButtonActionPerformed(ActionEvent e) {
        try {
            refreshInvoker();
        } catch (ActionException actionException) {
            actionException.showErrorDialog();
            return;
        }
        responseTextArea.setText("");
        invoker.setGenerateCurlText(false);
        InvokeService.getInstance().invoke(invoker);
        requestTimeLabel.setText("请求时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_PATTERN)));
    }

    private void refreshInvoker() throws ActionException {
        String domainName = domainComboBox.getSelectedItem().toString();
        String url = urlTextField.getText();
        String httpMethod = methodComboBox.getSelectedItem().toString();
        // 提取参数数据
        Map<String, Object> paramMap = Maps.newHashMap();
        List<ParameterDesc> parameterDescs = invoker.getMethodDesc().getParameterDescs();
        try {

            for (ParameterDesc parameterDesc : parameterDescs) {
                String paramName = parameterDesc.getName();
                JTextArea textArea = paramJTextAreaMap.get(paramName);
                String textValue = textArea.getText();
                if (JSONUtil.isTypeJSONObject(textValue)) {
                    paramMap.put(paramName, JSONUtil.parseObj(textValue));
                } else if (JSONUtil.isTypeJSONArray(textValue)) {
                    paramMap.put(paramName, JSONUtil.parseArray(textValue));
                } else if (NumberUtils.isParsable(textValue)) {
                    if (textValue.contains(".")) {
                        paramMap.put(paramName, NumberUtils.toDouble(textValue));
                    } else {
                        long aLong = NumberUtils.toLong(textValue);
                        paramMap.put(paramName, aLong == 0 ? textValue : aLong);
                    }
                } else {
                    paramMap.put(paramName, textValue);
                }
            }
        } catch (Exception e) {
            throw new ActionException(e.getMessage(), ErrorEnum.PARAMETER_ANALYSIS_ERROR.getTitle());
        }
        InvokeBody invokeBody = InvokeBody.builder()
                .domainName(domainName)
                .url(url)
                .httpMethod(httpMethod)
                .params(paramMap)
                .build();
        invoker.setInvokeBody(invokeBody);
        eventId = System.currentTimeMillis();
        invoker.setEventId(eventId);
    }

    private void clearResponse(ActionEvent e) {
        responseTextArea.setText("");
    }

    private void generateCurl(ActionEvent e) {
        try {
            refreshInvoker();
        } catch (ActionException actionException) {
            actionException.showErrorDialog();
            return;
        }
        responseTextArea.setText("");
        invoker.setGenerateCurlText(true);
        InvokeService.getInstance().invoke(invoker);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        label1 = new JLabel();
        domainComboBox = new JComboBox();
        label3 = new JLabel();
        urlTextField = new JTextField();
        label2 = new JLabel();
        methodComboBox = new JComboBox();
        paramsPanel = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();
        requestTimeLabel = new JLabel();
        responseTimeLabel = new JLabel();
        label5 = new JLabel();
        clearResponseButton = new JButton();
        scrollPane1 = new JScrollPane();
        responseTextArea = new JTextArea();
        buttonBar = new JPanel();
        generateCurl = new JButton();
        okButton = new JButton();

        //======== this ========
        setMinimumSize(new Dimension(1000, 700));
        setForeground(Color.black);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
//            dialogPane.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing. border
//            . EmptyBorder( 0, 0, 0, 0) , "JFor\u006dDesi\u0067ner \u0045valu\u0061tion", javax. swing. border. TitledBorder. CENTER, javax
//            . swing. border. TitledBorder. BOTTOM, new java .awt .Font ("Dia\u006cog" ,java .awt .Font .BOLD ,
//            12 ), java. awt. Color. red) ,dialogPane. getBorder( )) );
            dialogPane.addPropertyChangeListener(new java.beans
                    .PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if ("bord\u0065r".equals(e.
                            getPropertyName())) throw new RuntimeException();
                }
            });
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[]{0, 0, 0};
                ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[]{0, 0};
                ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
                ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[]{0.0, 1.0E-4};

                //======== panel1 ========
                {
                    panel1.setLayout(new GridBagLayout());
                    ((GridBagLayout) panel1.getLayout()).columnWidths = new int[]{0, 0, 0};
                    ((GridBagLayout) panel1.getLayout()).rowHeights = new int[]{0, 0, 0, 56, 0};
                    ((GridBagLayout) panel1.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
                    ((GridBagLayout) panel1.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //---- label1 ----
                    label1.setText("Domain\uff1a");
                    panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                    //---- domainComboBox ----
                    domainComboBox.addItemListener(e -> domainComboBoxItemStateChanged(e));
                    panel1.add(domainComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                    //---- label3 ----
                    label3.setText("Url\uff1a");
                    panel1.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                    //---- urlTextField ----
                    urlTextField.setEditable(false);
                    urlTextField.setBackground(UIManager.getColor("TextField.background"));
                    urlTextField.setForeground(UIManager.getColor("TextField.foreground"));
                    panel1.add(urlTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                    //---- label2 ----
                    label2.setText("Method\uff1a");
                    panel1.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));
                    panel1.add(methodComboBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                    //======== paramsPanel ========
                    {
                        paramsPanel.setLayout(new GridBagLayout());
                        ((GridBagLayout) paramsPanel.getLayout()).columnWidths = new int[]{0, 0};
                        ((GridBagLayout) paramsPanel.getLayout()).rowHeights = new int[]{0, 0, 0};
                        ((GridBagLayout) paramsPanel.getLayout()).columnWeights = new double[]{0.0, 1.0E-4};
                        ((GridBagLayout) paramsPanel.getLayout()).rowWeights = new double[]{0.0, 0.0, 1.0E-4};
                    }
                    panel1.add(paramsPanel, new GridBagConstraints(0, 3, 2, 1, 0.6, 1.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                }
                contentPanel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.5, 1.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout) panel2.getLayout()).columnWidths = new int[]{0, 0};
                    ((GridBagLayout) panel2.getLayout()).rowHeights = new int[]{0, 0, 0};
                    ((GridBagLayout) panel2.getLayout()).columnWeights = new double[]{0.0, 1.0E-4};
                    ((GridBagLayout) panel2.getLayout()).rowWeights = new double[]{0.0, 0.0, 1.0E-4};

                    //======== panel3 ========
                    {
                        panel3.setLayout(new GridBagLayout());
                        ((GridBagLayout) panel3.getLayout()).columnWidths = new int[]{127, 0, 0};
                        ((GridBagLayout) panel3.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
                        ((GridBagLayout) panel3.getLayout()).columnWeights = new double[]{1.0, 0.0, 1.0E-4};
                        ((GridBagLayout) panel3.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};

                        //---- requestTimeLabel ----
                        requestTimeLabel.setText("\u8bf7\u6c42\u65f6\u95f4\uff1a");
                        panel3.add(requestTimeLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                                new Insets(0, 0, 0, 0), 0, 0));

                        //---- responseTimeLabel ----
                        responseTimeLabel.setText("\u54cd\u5e94\u65f6\u95f4\uff1a");
                        panel3.add(responseTimeLabel, new GridBagConstraints(0, 1, 1, 1, 0.5, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                                new Insets(0, 0, 0, 0), 0, 0));

                        //---- label5 ----
                        label5.setText("\u8bf7\u6c42\u7ed3\u679c\uff1a");
                        label5.setHorizontalAlignment(SwingConstants.LEFT);
                        panel3.add(label5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                                new Insets(0, 0, 0, 0), 0, 0));

                        //---- clearResponseButton ----
                        clearResponseButton.setText("\u6e05\u9664\u7ed3\u679c");
                        clearResponseButton.addActionListener(e -> clearResponse(e));
                        panel3.add(clearResponseButton, new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0,
                                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                                new Insets(0, 0, 0, 0), 0, 0));
                    }
                    panel2.add(panel3, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                    //======== scrollPane1 ========
                    {
                        scrollPane1.setBackground(Color.pink);

                        //---- responseTextArea ----
                        responseTextArea.setBackground(Color.pink);
                        responseTextArea.setForeground(Color.darkGray);
                        responseTextArea.setLineWrap(true);
                        responseTextArea.setWrapStyleWord(true);
                        scrollPane1.setViewportView(responseTextArea);
                    }
                    panel2.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                }
                contentPanel.add(panel2, new GridBagConstraints(1, 0, 1, 1, 0.5, 1.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 0, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

                //---- generateCurl ----
                generateCurl.setText("\u751f\u6210CURL");
                generateCurl.addActionListener(e -> generateCurl(e));
                buttonBar.add(generateCurl, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- okButton ----
                okButton.setText("Call");
                okButton.addActionListener(e -> callButtonActionPerformed(e));
                buttonBar.add(okButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel1;
    private JLabel label1;
    private JComboBox domainComboBox;
    private JLabel label3;
    private JTextField urlTextField;
    private JLabel label2;
    private JComboBox methodComboBox;
    private JPanel paramsPanel;
    private JPanel panel2;
    private JPanel panel3;
    private JLabel requestTimeLabel;
    private JLabel responseTimeLabel;
    private JLabel label5;
    private JButton clearResponseButton;
    private JScrollPane scrollPane1;
    private JTextArea responseTextArea;
    private JPanel buttonBar;
    private JButton generateCurl;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
