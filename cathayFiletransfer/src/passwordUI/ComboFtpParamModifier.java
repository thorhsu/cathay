package passwordUI;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.springframework.core.io.ClassPathResource;

import com.fxdms.util.CommonDataValidator;
import com.fxdms.util.PwdDES;

public class ComboFtpParamModifier implements ActionListener {
	JTextField userField, ftpUri;
	JPasswordField dbPwdField1, dbPwdField2;
	JDialog dialog;
	JLabel displayTxt = null;
	String password1 = "";
	String password2 = "";
	Properties props;

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		String propsFile = props.getProperty("systemJars")
				+ "staticData.properties";

		if (cmd.equals("確定")) {
			boolean pwdEq = checkPwdEq();
			String user = userField.getText();
			String ftpUrl = ftpUri.getText();
			boolean isUri = CommonDataValidator.isUrl(ftpUrl);
			if ("".equals(password1) || "".equals(password2)
					|| "".equals(user) || "".equals(ftpUrl)) {
				dialog.setTitle("Combo FTP參數修改  -- 欄位不可為空白 ");
			} else if (!isUri) {
				dialog.setTitle("Combo FTP參數修改  -- db uri含有特殊符號");
			} else if (!pwdEq) {
				dialog.setTitle("Combo FTP參數修改  -- 兩個密碼欄位必須相同");
			} else {
				String success = "成功";
				FileWriter fw = null;
				String encPwd = PwdDES.getEncPwd(password1);
				props.setProperty("ftpPassword", encPwd);
				props.setProperty("ftpServerIP", ftpUrl);
				props.setProperty("ftpUser", user);
				try {
					fw = new FileWriter(propsFile);
					props.store(fw, "");
				} catch (IOException e1) {
					success = "失敗";
					e1.printStackTrace();
				} finally {
					if (fw != null)
						try {
							fw.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					fw = null;
				}
				dialog.setTitle("DB參數修改  -- 修改" + success);
			}

		} else if (cmd.equals("取消")) {
			dialog.dispose();
		}
	}

	public boolean checkPwdEq() {
		boolean equal = true;
		char[] pwd1 = dbPwdField1.getPassword();
		char[] pwd2 = dbPwdField2.getPassword();
		password1 = "";
		password2 = "";
		for (char oneChar : pwd1) {
			password1 += oneChar;
		}
		for (char oneChar : pwd2) {
			password2 += oneChar;
		}
		
		if (!password1.equals(password2))
			equal = false;
		return equal;
	}

	ComboFtpParamModifier(JFrame f) {

		props = new Properties();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new ClassPathResource("staticData.properties")
							.getInputStream()));
			props.load(reader);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		dialog = new JDialog(f, "Combo FTP參數修改", true);
		GridBagConstraints c;
		int gridx, gridy, gridwidth, gridheight, anchor, fill, ipadx, ipady;
		double weightx, weighty;
		Insets inset;

		GridBagLayout gridbag = new GridBagLayout();
		Container dialogPane = dialog.getContentPane();
		dialogPane.setLayout(gridbag);

		JLabel label = new JLabel("Combo FTP User Name: ");
		gridx = 0; // 第0列
		gridy = 0; // 第0行
		gridwidth = 1; // 佔一單位寬度
		gridheight = 1; // 佔一單位高度
		weightx = 0; // 視窗增大時元件寬度增大比率0
		weighty = 0; // 視窗增大時元件高度增大比率0
		anchor = GridBagConstraints.CENTER; // 容器大於元件size時將元件置於容器中央
		fill = GridBagConstraints.BOTH; // 視窗拉大時會填滿水平與垂直空間
		inset = new Insets(0, 0, 0, 0); // 元件間間距
		ipadx = 0; // 元件內水平寬度
		ipady = 0; // 元件內垂直高度
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(label, c);
		dialogPane.add(label);

		label = new JLabel("   ");
		gridx = 3;
		gridy = 0;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(label, c);
		dialogPane.add(label);

		label = new JLabel("Combo FTP PWD: ");
		gridx = 0;
		gridy = 1;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(label, c);
		dialogPane.add(label);

		label = new JLabel("Confirm Password: ");
		gridx = 3;
		gridy = 1;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(label, c);
		dialogPane.add(label);

		label = new JLabel("COMBO FTP IP: ");
		gridx = 0;
		gridy = 2;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(label, c);
		dialogPane.add(label);

		/*
		 * ftpServerIP=10.113.139.69
            #combo ftp user
            ftpUser=fubonadmin
            #combo ftp password
            ftpPassword=mhhw1onwL5uPRFV3GysFlg==
		 */

		String pwd = PwdDES.getDecPwd(props.getProperty("ftpPassword"));
		String userName = props.getProperty("ftpUser");
		String ftpUrl = props.getProperty("ftpServerIP");
		userField = new JTextField(userName);
		gridx = 1;
		gridy = 0;
		gridwidth = 2;
		gridheight = 1;
		weightx = 1;
		weighty = 0;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(userField, c);
		dialogPane.add(userField);

		label = new JLabel("   ");
		gridx = 4;
		gridy = 0;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(label, c);
		dialogPane.add(label);

		dbPwdField1 = new JPasswordField(pwd);
		gridx = 1;
		gridy = 1;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(dbPwdField1, c);
		dialogPane.add(dbPwdField1);

		dbPwdField2 = new JPasswordField(pwd);
		gridx = 4;
		gridy = 1;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(dbPwdField2, c);
		dialogPane.add(dbPwdField2);

		ftpUri = new JTextField(ftpUrl);
		gridx = 1;
		gridy = 2;
		gridwidth = 5;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(ftpUri, c);
		dialogPane.add(ftpUri);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		JButton b = new JButton("確定");
		b.addActionListener(this);
		panel.add(b);
		b = new JButton("取消");
		b.addActionListener(this);
		panel.add(b);

		gridx = 0;
		gridy = 3;
		gridwidth = 6;
		weightx = 1;
		weighty = 1;
		c = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, inset, ipadx, ipady);
		gridbag.setConstraints(panel, c);
		dialogPane.add(panel);

		dialog.setBounds(200, 150, 400, 130);
		dialog.setVisible(true);
	}
}
