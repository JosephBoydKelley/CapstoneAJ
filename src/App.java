import javax.swing.JFrame;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {
	static final String DB_URL = "jdbc:mysql://localhost:3306/capstone";
	static final String USER = "root"; // username created in mySQL query
	static final String PASS = "password"; // password created in mySQL query
	private static JFrame frame;
	private static JTextField input;
	private static JLabel id_label;
	private static JTextField input_id;
	private static JLabel fname_label;
	private static JTextField input_fname;
	private static JLabel lname_label;
	private static JTextField input_lname;
	private static JLabel email_label;
	private static JTextField input_email;
	private static JLabel label;
	private static JButton button1;
	private static JButton addEmployees;
	private static JButton viewEmployees;
	private static JButton deleteEmployees;
	private static JButton addBttn;
	private static JButton generateReport;
	static String company_name;
	private int width;
	private int height;
	static String ceoEmail;
	static SendEmail sd = new SendEmail();

	public App() {

	}

	public App(int w, int h) {
		frame = new JFrame();
		label = new JLabel("<html>Welcome! Please enter your company name.</html>");

		input = new JTextField(10);
		button1 = new JButton("Enter");
		viewEmployees = new JButton("View Employees");

		addEmployees = new JButton("Add Employees");

		deleteEmployees = new JButton("Delete Employees");
		id_label = new JLabel("Enter ID: ");
		input_id = new JTextField(10);
		fname_label = new JLabel("Enter first name: ");
		input_fname = new JTextField(10);
		lname_label = new JLabel("Enter last name: ");
		input_lname = new JTextField(10);
		email_label = new JLabel("Enter email: ");
		input_email = new JTextField(10);

		addBttn = new JButton("Add Employee");
		generateReport = new JButton("Generate Report");
		width = w;
		height = h;
	}

	public static void SendReport() throws SchedulerException {
		// use quartz job scheduler

		JobDetail j = JobBuilder.newJob(ScheduleSend.class).build();
		// Trigger t = TriggerBuilder.newTrigger().withIdentity("CroneTrigger")
		// 		.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(604800)).build();
		Trigger t = TriggerBuilder.newTrigger().withIdentity("CroneTrigger").build();
		Scheduler s = StdSchedulerFactory.getDefaultScheduler();
		s.start();
		s.scheduleJob(j, t);
	}

	public static void SendEmail() throws SQLException {
		String from = "JkelleyAKlein"; // GMail user name (just the part before "@gmail.com")
		String pass = "JKelleyAKlein1!"; // GMail password
		String subject = "Hello";
		ceoEmail = JOptionPane.showInputDialog("Enter email to send report to: ");
		if (ceoEmail == null) {
			return;
		}
		while(!ceoEmail.contains("@")) {
			ceoEmail = JOptionPane.showInputDialog("Enter valid email. Must contain an @."); 
		}
		System.out.println(ceoEmail);
		ArrayList<String> Emails = new ArrayList<String>();
		ArrayList<String> Ids = new ArrayList<String>();
		HashMap<String, String> emId = new HashMap<>();
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();) {

			DatabaseMetaData dbm = conn.getMetaData();

			String tblname = company_name + "_employees";

			// body = "<h1>This is actual message embedded in HTML tags</h1>";
			String query = " SELECT email, id FROM " + tblname;
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String email = rs.getString("email");
				Integer id = rs.getInt("id");
				System.out.println(email);
				System.out.println(id);
				sd.SendEmail(email, id, company_name);

			}

		}
		try {
			SendReport();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(frame, "Expect a report emailed to you in one week.");
	}

	public static void DeleteEmployee() throws SQLException {
		addEmployees.setVisible(false);
		viewEmployees.setVisible(false);
		deleteEmployees.setVisible(false);
		generateReport.setVisible(false);

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();) {
			DatabaseMetaData dbm = conn.getMetaData();
			String tblname = company_name + "_employees";
			ResultSet tables = dbm.getTables(null, null, tblname, null);
			String end = "yes";
			outerloop: if (tables.next()) {
				System.out.println("entered1");
				while (end.equals("yes")) {
					System.out.println("entered2");
					String empId = JOptionPane.showInputDialog("Enter ID");
					if (empId == null) {
						end = "no";
						break outerloop;

					}
					String sql = "SELECT * FROM " + company_name + "_employees where id=" + empId;
					ResultSet rs = stmt.executeQuery(sql);

					if (rs.next()) {
						System.out.println("entered3");
						System.out.println("Success");
						String query = " delete from " + company_name + "_employees where id=" + empId;

						// create the mysql insert preparedstatement
						PreparedStatement preparedStmt = conn.prepareStatement(query);
						// preparedStmt.setString(1, empId);
						preparedStmt.execute();
						JOptionPane.showMessageDialog(null, "Employee Has Been Removed", "Results",
								JOptionPane.PLAIN_MESSAGE);
						end = JOptionPane.showInputDialog("Would you like to continue?");

					} else {

						System.out.println("entered4");
						JOptionPane.showMessageDialog(null, "Employee Does Not Exist", "Results",
								JOptionPane.YES_NO_OPTION);
						end = JOptionPane.showInputDialog("Would you like to continue?");
						if (end == null) {
							System.out.println("entered5");
							break;
						}
					}
					// the mysql insert statement
					if (end == null) {
						System.out.println("entered6");
						end = "no";
					}

				}
			}

		}
	}

	public static void AddEmployees() throws SQLException {
		// open a connection
		// addEmployeeDisplay();
		addEmployees.setVisible(false);
		viewEmployees.setVisible(false);
		deleteEmployees.setVisible(false);
		generateReport.setVisible(false);

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();) {

			DatabaseMetaData dbm = conn.getMetaData();
			String tblname = company_name + "_employees";
			ResultSet tables = dbm.getTables(null, null, tblname, null);

			String end = "yes";
			if (tables.next()) {
				outerloop: while (end.equals("yes")) {

					String empId = JOptionPane.showInputDialog("Enter ID");
					if (empId == null) {
						System.out.println("entered1");
						end = "no";
						break outerloop;
					}
					String queryCheck = "SELECT id from " + company_name + "_employees";
					PreparedStatement st = conn.prepareStatement(queryCheck);
					ResultSet rs = st.executeQuery();
					
					ArrayList<String> ids = new ArrayList<String>();
					while(rs.next()) {
						System.out.println("lets add to arr list");
						ids.add(rs.getString(1));
						System.out.println(ids);
					}
//					
					while(ids.contains(empId)) 	{
						System.out.println("lets check if contains");
						empId = JOptionPane.showInputDialog("Please enter valid ID. ID cannot already belong to employee.");
					}
					String firstName = JOptionPane.showInputDialog("Enter First Name");
					if (firstName == null) {
						System.out.println("entered1");
						end = "no";
						break outerloop;
					}
					String lastName = JOptionPane.showInputDialog("Enter Last Name");
					if (lastName == null) {
						System.out.println("entered1");
						end = "no";
						break outerloop;
					}
					String empEmail = JOptionPane.showInputDialog("Enter Email");
					if (empEmail == null) {
						System.out.println("entered1");
						end = "no";
						break outerloop;
					}
					while(!empEmail.contains("@")) {
						empEmail = JOptionPane.showInputDialog("Enter valid email. Must contain an @."); 
					}

					// the mysql insert statement
					String query = " insert into " + company_name + "_employees  (id, fname, lname, email)"
							+ " values (?, ?, ?, ?)";

					// create the mysql insert preparedstatement
					PreparedStatement preparedStmt = conn.prepareStatement(query);
					preparedStmt.setString(1, empId);
					preparedStmt.setString(2, firstName);
					preparedStmt.setString(3, lastName);
					preparedStmt.setString(4, empEmail);
					preparedStmt.execute();
					JOptionPane.showMessageDialog(null, "Employee Has Been Added", "Results",
							JOptionPane.PLAIN_MESSAGE);
					end = JOptionPane.showInputDialog("Would you like to continue?");
					if (end == null) {
						System.out.println("entered2");
						break;
					}

				}
			} else {

				// optionsDisplay();

			}
			addEmployees.setVisible(true);
			viewEmployees.setVisible(true);
			deleteEmployees.setVisible(true);
		}

	}

	public static void ViewEmployees() throws SQLException {
		// open a connection
		// addEmployeeDisplay();
		addEmployees.setVisible(false);
		viewEmployees.setVisible(false);
		deleteEmployees.setVisible(false);
		generateReport.setVisible(false);

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();) {

			DatabaseMetaData dbm = conn.getMetaData();
			String tblname = company_name + "_employees";
			ResultSet tables = dbm.getTables(null, null, tblname, null);

			Statement satmt = conn.createStatement();
			String query = " SELECT * FROM " + company_name + "_employees";
			ResultSet rs = stmt.executeQuery(query);
			System.out.println("id   fname   lname   email");
			String data = "";
			while (rs.next()) {
				int id = rs.getInt("id");
				String fname = rs.getString("fname");
				String lname = rs.getString("lname");
				String email = rs.getString("email");
				System.out.println(id + "   " + fname + "    " + lname + "    " + email);
				data += id + "   " + fname + "    " + lname + "    " + email + "\n";
			}
			JOptionPane.showMessageDialog(frame, data);
			addEmployees.setVisible(true);
			viewEmployees.setVisible(true);
			deleteEmployees.setVisible(true);
		}

	}

	public static void optionsDisplay() {
		// String cname = company_name;
		Container cp = frame.getContentPane();
		FlowLayout flow = new FlowLayout();
		cp.setLayout(flow);
		frame.setSize(640, 480); // same width/height as GUI set up

		cp.add(viewEmployees);
		cp.add(addEmployees);
		cp.add(deleteEmployees);
		cp.add(generateReport);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Phising service");

		frame.setVisible(true);
	}

	public static void AddCompany() throws SQLException {
		// open a connection
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();) {

			// --CREATE TABLE TO HOLD COMPANY EMPLOYEE INFO--
			// this is going to act as like "logging in" for the company... lets remember
			// that for the GUI
			// user input of employee name
			// String company_name = JOptionPane.showInputDialog("Enter Company");
			company_name = input.getText();
			String tblName = company_name + "_employees";
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tblName, null);
			if (tables.next()) {
				JOptionPane.showMessageDialog(null, "Signing in..", "Results", JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Creating Company", "Results", JOptionPane.PLAIN_MESSAGE);
				String sql_table = "CREATE TABLE " + company_name + "_employees " + "(id INTEGER not NULL, "
						+ " fname VARCHAR(255), " + " lname VARCHAR(255), " + " email VARCHAR(255), "
						+ " PRIMARY KEY ( id ))";

				stmt.executeUpdate(sql_table);
				JOptionPane.showMessageDialog(null, "Company created, signing in now..", "Results", JOptionPane.PLAIN_MESSAGE);
				// optionsDisplay();
			}
			button1.setVisible(false);
			label.setVisible(false);
			input.setVisible(false);
			optionsDisplay();
		}

	}

	public void setUpGUI() {
		Container cp = frame.getContentPane();
		FlowLayout flow = new FlowLayout();
		cp.setLayout(flow);
		frame.setSize(width, height);
		frame.setTitle("Vulnerability Scanner");
		cp.add(label);
		cp.add(input);
		cp.add(button1);
		// cp.add(button2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void setUpButtonListeners() {
		ActionListener buttonListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				Object o = ae.getSource();
				if (o == button1) {
					// String s = input.getText();
					// label.setText(s); //changes label value
					// input.setText("");
					try {
						AddCompany();
						button1.setVisible(false);
						label.setVisible(false);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("logged in ");
				} else if (o == addEmployees) {
					System.out.println("lets add an employee");

					try {
						AddEmployees();
						/*
						 * addEmployees.setVisible(false);
						 * viewEmployees.setVisible(false);
						 * deleteEmployees.setVisible(false);
						 */
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					addEmployees.setVisible(true);
					viewEmployees.setVisible(true);
					deleteEmployees.setVisible(true);
					generateReport.setVisible(true);
					System.out.println("employee add ");
				} else if (o == viewEmployees) {
					System.out.println("lets view employees");
					try {
						ViewEmployees();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					addEmployees.setVisible(true);
					viewEmployees.setVisible(true);
					deleteEmployees.setVisible(true);
					generateReport.setVisible(true);
					System.out.println("employee viewed ");
				} else if (o == deleteEmployees) {
					System.out.println("delete an employee");
					try {
						DeleteEmployee();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					addEmployees.setVisible(true);
					viewEmployees.setVisible(true);
					deleteEmployees.setVisible(true);
					generateReport.setVisible(true);
					System.out.println("employee deleted ");
				} else if (o == generateReport) {
					System.out.println("generate report");
					try {
						SendEmail();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					addEmployees.setVisible(true);
					viewEmployees.setVisible(true);
					deleteEmployees.setVisible(true);
					generateReport.setVisible(true);
					System.out.println("emails sent ");
				}
			}

		};

		button1.addActionListener(buttonListener);
		addEmployees.addActionListener(buttonListener);
		viewEmployees.addActionListener(buttonListener);
		deleteEmployees.addActionListener(buttonListener);
		generateReport.addActionListener(buttonListener);

	}

}
