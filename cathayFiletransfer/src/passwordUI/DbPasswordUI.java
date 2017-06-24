package passwordUI;

import javax.swing.*;
import javax.swing.border.*;

import org.springframework.core.io.ClassPathResource;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

public class DbPasswordUI implements ActionListener {
	JFrame f = null;

	Properties props = null;

	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();
		if (cmd.equals("修改JDBC參數")) {
			new JdbcParamModifier(f);
		}else if (cmd.equals("修改Combo FTP參數")) {
			new ComboFtpParamModifier(f);
		}else if (cmd.equals("修改SIP Server FTP參數")) {
			new SipFtpParamModifier(f);
		}else if (cmd.equals("修改寰影 Sever FTP參數")) {
			new ImgFtpParamModifier(f);
		}else if (cmd.equals("離開系統")) {
			System.exit(0);
		}
	}

	public DbPasswordUI() {
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

		f = new JFrame("富邦保單管理系統參數修改");
		Container contentPane = f.getContentPane();
		JPanel buttonPanel = new JPanel();
		JButton b = new JButton("修改JDBC參數");
		b.addActionListener(this);
		buttonPanel.add(b);

		b = new JButton("修改Combo FTP參數");
		b.addActionListener(this);
		buttonPanel.add(b);

		b = new JButton("修改SIP Server FTP參數");
		b.addActionListener(this);
		buttonPanel.add(b);
		
		b = new JButton("修改寰影 Sever FTP參數");
		b.addActionListener(this);
		buttonPanel.add(b);
		
		b = new JButton("離開系統");
		b.addActionListener(this);
		buttonPanel.add(b);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.blue, 2), "遠端連結參數修改",
				TitledBorder.CENTER, TitledBorder.TOP));

		contentPane.add(buttonPanel, BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public static void main(String[] args) {
		new DbPasswordUI();
	}
}

