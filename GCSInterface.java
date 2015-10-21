import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GCSInterface extends JFrame {

	/**
         *
        * 
         */
	private static final long serialVersionUID = 1L;

	// Database Connection
	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private ResultSetMetaData md;

	private int second = 60, phasecount, logcount;
	private File background1 = new File("./src/image/background.jpg");

	private File image = new File("./src/image/marslander.png");

	private File touchdown1 = new File("./src/image/touchdown.png");
	private File marslandercutoff1 = new File(
			"./src/image/marslandercutoff.png");
	private BufferedImage lcurve = new BufferedImage(1000, 600,
			BufferedImage.TYPE_INT_ARGB);

	private Graphics2D g = lcurve.createGraphics();

	private NumberFormat fmt = new DecimalFormat("#0.00");

	private JTabbedPane tabbedPane;
	private JFrame mainFrame;
	private JPanel panel1, panel2, panel3, panel4;

	private JTextArea fSqlQuery;
	private JButton submit, phase1, phase2, phase3, phase4, phase5, graphView,
			newSimulation, liveSimulation;

	private JFormattedTextField fAltitude, fEng1, fThrust, fEng2, fEng3,
			fAccel1, fAccel2, fAccel3, fTime, fAngle, fVar1, fTempSensor,
			fDropHeight, fVertical, fHorizontal, fParachuteIndicator,
			fTimeCount, fAxialEng1, fAxialEng2, fAxialEng3, fRollEng1,
			fRollEng2, fRollEng3;

	private double axialEng1, axialEng2, axialEng3, rollEng1, rollEng2,
			rollEng3;

	private double timeCount = 0;
	private int engineTemp, engineDesiredTemp, tempDegree;

	private double eng1, eng2, eng3, accel1, accel2, accel3, dropHeight = 500,
			airspeed, elevation, distance, angle, time, var1, thrust,
			altitude = 25960;

	private boolean parachuteIndicator;

	private String query;

	JMenuBar menuBar;
	JMenu menu, edit;
	JMenuItem exit, cSimulation, cSimulationGraphical, tdEdit;

	public GCSInterface() {

		// Create the menu bar.
		mainFrame = new JFrame();
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create the tab pages
		createPage1();
		createPage2();
		createPage3();
		createPage4();

		// Phase2
		parachuteIndicator = true; // true = attached, false = released
		engineTemp = 0; // all temperature is in 'F'
		engineDesiredTemp = 300; // temp a little higher than the average car
		tempDegree = 10; // degree increment rate

		// Create TabbedPanes
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Tregectory Data", panel1);
		tabbedPane.addTab("Database", panel2);
		tabbedPane.addTab("Predefined Attributes", panel3);
		tabbedPane.addTab("Graphical View", panel4);
		topPanel.add(tabbedPane, BorderLayout.PAGE_START);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.setBackground(Color.BLACK);
		tabbedPane.setForeground(Color.WHITE);

		// Add MenuBar and all MenuItems
		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menuBar.add(menu);
		menu.setBackground(Color.BLACK);
		menu.setForeground(Color.WHITE);
		menuBar.setBackground(Color.BLACK);
		menuBar.setForeground(Color.WHITE);
		cSimulation = new JMenuItem("New Simulation");
		cSimulation.setMnemonic('n');
		menu.add(cSimulation);
		cSimulation.setBackground(Color.BLACK);
		cSimulation.setForeground(Color.WHITE);
		menu.addSeparator();
		cSimulationGraphical = new JMenuItem("Graphical Simulation");
		cSimulation.setMnemonic('g');
		menu.add(cSimulationGraphical);
		cSimulationGraphical.setBackground(Color.BLACK);
		cSimulationGraphical.setForeground(Color.WHITE);
		menu.addSeparator();
		exit = new JMenuItem("Exit");
		exit.setMnemonic('x');
		menu.add(exit);
		exit.setBackground(Color.BLACK);
		exit.setForeground(Color.WHITE);
		menu.setMnemonic('f');
		edit = new JMenu("Edit");
		edit.setMnemonic('e');
		menuBar.add(edit);
		edit.setBackground(Color.BLACK);
		edit.setForeground(Color.WHITE);
		tdEdit = new JMenuItem("Tragectory Data Editor");
		tdEdit.setMnemonic('t');
		edit.add(tdEdit);
		tdEdit.setBackground(Color.BLACK);
		tdEdit.setForeground(Color.WHITE);
		mainFrame.add(menuBar, BorderLayout.NORTH);

		topPanel.setBackground(Color.BLACK);
		topPanel.setForeground(Color.BLACK);
		// Add TAbbedPane and MainFrame
		mainFrame.add(tabbedPane);
		mainFrame.setTitle("GCS Mars Lander Software");
		mainFrame.setSize(700, 900);
		mainFrame.setBackground(Color.BLACK);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);

		exit.addActionListener(new exitApp());// add Exit Button

		// Implement New Simulation and all classes
		cSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == cSimulation && parachuteIndicator(95)) {
					tabbedPane.setSelectedIndex(0);

					panel1.add(phase1);
					panel1.add(phase2);
					panel1.add(phase3);
					panel1.add(phase4);
					panel1.add(phase5);

					phase2.setEnabled(false);
					phase3.setEnabled(false);
					phase4.setEnabled(false);
					phase5.setEnabled(false);
					cSimulation.setEnabled(false);
					cSimulationGraphical.setEnabled(false);
					panel1.add(newSimulation);

					newSimulation.setVisible(false);

				}

			}
		});
		cSimulationGraphical.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == cSimulationGraphical) {
					tabbedPane.setSelectedIndex(3);
					panel4.add(phase1);
					panel4.add(phase2);
					panel4.add(phase3);
					panel4.add(phase4);
					panel4.add(phase5);
					panel4.add(newSimulation);

					liveSimulation.setBackground(Color.GREEN);
					phase1.setEnabled(true);
					phase2.setEnabled(false);
					phase3.setEnabled(false);
					phase4.setEnabled(false);
					phase5.setEnabled(false);

					cSimulation.setEnabled(false);
					cSimulationGraphical.setEnabled(false);
					newSimulation.setVisible(false);

				}

			}
		});

		phase1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == phase1) {

					liveSimulation.setEnabled(false);

					File phase1open = new File(
							"./src/image/marslanderphase1.png");

					BufferedImage marslanderphase1 = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);

					try {

						marslanderphase1 = ImageIO.read(phase1open);

					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					Graphics2D g = lcurve.createGraphics();
					g.drawImage(marslanderphase1, null, -100, -60);
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					panel4.setSize(600, 600);
					try {

						Phase1();

						phase1.setEnabled(false);
						phase2.setEnabled(true);
						phase3.setEnabled(false);
						phase4.setEnabled(false);
						phase5.setEnabled(false);
						phase1.setBackground(Color.RED);
						liveSimulation.setBackground(Color.RED);

					} catch (SQLException e1) {

						e1.printStackTrace();
					}

				}

			}

		});
		phase2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == phase2) {

					File parachute1 = new File(
							"./src/image/parachuteReleased.png");

					BufferedImage parachute = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					try {
						parachute = ImageIO.read(parachute1);
					} catch (IOException em) {
					}
					Graphics2D g = lcurve.createGraphics();

					g.drawImage(parachute, null, -10, -10);
					panel4.setSize(600, 800);
					Phase2();
					phase1.setEnabled(false);
					phase2.setEnabled(false);
					phase3.setEnabled(true);
					phase4.setEnabled(false);
					phase5.setEnabled(false);
					phase2.setBackground(Color.RED);

				}
			}

		});
		phase3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == phase3) {

					File image = new File("./src/image/marslander.png");

					BufferedImage marslander = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);
					try {
						marslander = ImageIO.read(image);
					} catch (IOException em) {
					}
					Graphics2D g = lcurve.createGraphics();

					g.drawImage(marslander, null, 255, 25);
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					panel4.setSize(600, 800);
					Phase3();
					phase1.setEnabled(false);
					phase2.setEnabled(false);
					phase3.setEnabled(false);
					phase4.setEnabled(true);
					phase5.setEnabled(false);

					phase3.setBackground(Color.RED);

				}
			}

		});
		phase4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == phase4 && parachuteIndicator(95)) {

					File marslandercutoff1 = new File(
							"./src/image/marslandercutoff.png");

					BufferedImage marslandercutoff = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);
					try {
						marslandercutoff = ImageIO.read(marslandercutoff1);
					} catch (IOException em) {
					}
					Graphics2D g = lcurve.createGraphics();
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					panel4.setSize(600, 600);
					g.drawImage(marslandercutoff, null, 330, 340);
					Phase4(altitude, thrust);
					phase1.setEnabled(false);
					phase2.setEnabled(false);
					phase3.setEnabled(false);
					phase4.setEnabled(false);
					phase5.setEnabled(true);
					phase4.setBackground(Color.RED);

				} else if (parachuteIndicator(5)) {
					phase4.setEnabled(false);
					phase5.setEnabled(false);
					phase4.setBackground(Color.RED);
					phase5.setBackground(Color.RED);
					File boom1 = new File("./src/image/Eplosion.png");

					BufferedImage boom = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);
					try {
						boom = ImageIO.read(boom1);
					} catch (IOException em) {
					}
					Graphics2D g = lcurve.createGraphics();
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					panel4.setSize(600, 600);
					g.drawImage(boom, null, 130, 200);
					JLabel crash = new JLabel(
							"GCS has failed you have crashed the Lander!");

					panel1.add(crash);
					crash.setForeground(Color.RED);
					crash.setBackground(Color.BLACK);
					JLabel crash2 = new JLabel(
							"GCS has failed you have crashed the Lander!");

					panel4.add(crash2);
					crash2.setForeground(Color.RED);
					crash2.setBackground(Color.BLACK);
					newSimulation.setVisible(true);

				}

			}

		});

		phase5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == phase5 && parachuteIndicator(95)) {

					File touchdown1 = new File("./src/image/touchdown.png");

					BufferedImage touchdown = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);
					try {
						touchdown = ImageIO.read(touchdown1);
					} catch (IOException em) {
					}
					Graphics2D g = lcurve.createGraphics();

					g.drawImage(touchdown, null, 110, 400);
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					Phase5();
					panel4.setSize(600, 600);
					phase1.setEnabled(false);
					phase2.setEnabled(false);
					phase3.setEnabled(false);
					phase4.setEnabled(false);
					phase5.setEnabled(false);
					phase5.setBackground(Color.RED);
					newSimulation.setVisible(true);
					JTextArea landed1 = new JTextArea("GCS has touchdown!!");
					panel1.add(landed1);
					landed1.setForeground(Color.RED);
					landed1.setBackground(Color.BLACK);

					JTextArea landed = new JTextArea("GCS has touchdown!!");
					panel4.add(landed);
					landed.setForeground(Color.RED);
					landed.setBackground(Color.BLACK);

				} else if (parachuteIndicator(5)) {
					phase5.setEnabled(false);
					phase5.setBackground(Color.RED);
					File boom1 = new File("./src/image/Eplosion.png");

					BufferedImage boom = new BufferedImage(10, 15,
							BufferedImage.TYPE_INT_ARGB);
					try {
						boom = ImageIO.read(boom1);
					} catch (IOException em) {
					}
					Graphics2D g = lcurve.createGraphics();
					getContentPane().setLayout(null);
					setDefaultLookAndFeelDecorated(true);
					panel4.setSize(600, 600);
					g.drawImage(boom, null, 130, 200);
					JLabel crash = new JLabel(
							"GCS has failed you have crashed the Lander!");

					panel1.add(crash);
					JLabel crash2 = new JLabel(
							"GCS has failed you have crashed the Lander!");

					panel4.add(crash2);
					crash2.setForeground(Color.RED);
					crash2.setBackground(Color.BLACK);
					crash.setForeground(Color.RED);
					crash.setBackground(Color.BLACK);
					newSimulation.setVisible(true);

				}
			}

		});

		liveSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == liveSimulation && parachuteIndicator(95)) {

					try {
						sysSleep();
						liveSimulation.setEnabled(false);
						phase1.setEnabled(false);
						phase2.setEnabled(false);
						phase3.setEnabled(false);
						phase4.setEnabled(false);
						phase5.setEnabled(false);
						phase1.setBackground(Color.RED);
						phase2.setBackground(Color.RED);
						phase3.setBackground(Color.RED);
						phase4.setBackground(Color.RED);
						phase5.setBackground(Color.RED);
						liveSimulation.setBackground(Color.RED);

						File phase1open = new File(
								"./src/image/marslanderphase1.png");

						BufferedImage marslanderphase1 = new BufferedImage(10,
								15, BufferedImage.TYPE_INT_ARGB);

						marslanderphase1 = ImageIO.read(phase1open);
						Graphics2D g = lcurve.createGraphics();
						g.drawImage(marslanderphase1, null, -100, -60);
						getContentPane().setLayout(null);
						setDefaultLookAndFeelDecorated(true);
						panel4.setSize(600, 800);

					} catch (Exception em) {
						second--;
					}
					// /////////////////////////////////////////////////

					try {
						sysSleep();
						File parachute1 = new File(
								"./src/image/parachuteReleased.png");

						BufferedImage parachute = new BufferedImage(10, 15,
								BufferedImage.TYPE_INT_ARGB);
						getContentPane().setLayout(null);
						setDefaultLookAndFeelDecorated(true);
						parachute = ImageIO.read(parachute1);
						g.drawImage(parachute, null, -10, -10);
						panel4.setSize(600, 800);

					} catch (Exception em) {
						second--;
					}
					// /////////////////////////////////////////////////////////////////////

					try {
						sysSleep();
						File img2 = new File("./src/image/marslander.png");

						BufferedImage marslander = new BufferedImage(10, 15,
								BufferedImage.TYPE_INT_ARGB);
						marslander = ImageIO.read(img2);
						g.drawImage(marslander, null, 255, 25);
						getContentPane().setLayout(null);
						setDefaultLookAndFeelDecorated(true);
						panel4.setSize(600, 800);

					} catch (Exception em) {
						second--;
					}
					// ////////////////////////////////////////////////////////////////////////////

					try {
						sysSleep();
						File marslandercutoff1 = new File(
								"./src/image/marslandercutoff.png");

						BufferedImage marslandercutoff = new BufferedImage(10,
								15, BufferedImage.TYPE_INT_ARGB);
						marslandercutoff = ImageIO.read(marslandercutoff1);
						getContentPane().setLayout(null);
						setDefaultLookAndFeelDecorated(true);
						panel4.setSize(600, 800);
						g.drawImage(marslandercutoff, null, 330, 340);

					} catch (Exception em) {
						second--;
					}
					// /////////////////////////////////////////////////////////////////////////////////////////

					try {
						sysSleep();
						File touchdown1 = new File("./src/image/touchdown.png");

						BufferedImage touchdown = new BufferedImage(10, 15,
								BufferedImage.TYPE_INT_ARGB);
						touchdown = ImageIO.read(touchdown1);
						g.drawImage(touchdown, null, 110, 400);
						getContentPane().setLayout(null);
						setDefaultLookAndFeelDecorated(true);

						panel4.setSize(600, 800);

						newSimulation.setVisible(true);

						JTextField landed = new JTextField(
								"GCS has touchdown!!");
						panel4.add(landed);
						landed.setForeground(Color.RED);
						landed.setBackground(Color.BLACK);

					} catch (Exception em) {
						second--;
					}
				} else if (parachuteIndicator(5)) {

					try {
						sysSleep();
						File boom1 = new File("./src/image/Eplosion.png");

						BufferedImage boom = new BufferedImage(10, 15,
								BufferedImage.TYPE_INT_ARGB);
						boom = ImageIO.read(boom1);
						Graphics2D g = lcurve.createGraphics();
						getContentPane().setLayout(null);
						setDefaultLookAndFeelDecorated(true);
						panel4.setSize(600, 800);
						g.drawImage(boom, null, 130, 200);

						JLabel crash2 = new JLabel(
								"GCS has failed you have crashed the Lander!");

						panel4.add(crash2);
						crash2.setForeground(Color.RED);
						crash2.setBackground(Color.BLACK);

						newSimulation.setVisible(true);

					} catch (Exception em) {
						second--;
					}

				}
			}

		});
		newSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == newSimulation) {
					try {
						new GCSInterface();
						newSimulation.setBackground(Color.RED);

					} catch (Exception em) {
						System.exit(0);
					}
				}
			}

		});
		// File Menu Trajectory Data editor
		tdEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cSimulation.setEnabled(false);
				tabbedPane.setSelectedIndex(0);
				fAltitude.setEditable(true);
				fAltitude.setEditable(true);
				fThrust.setEditable(true);
				fEng1.setEditable(true);
				fEng2.setEditable(true);
				fEng3.setEditable(true);
				fAccel1.setEditable(true);
				fAccel2.setEditable(true);
				fAccel3.setEditable(true);
				fTime.setEditable(true);
				fAngle.setEditable(true);
				fVar1.setEditable(true);
				fTempSensor.setEditable(true);
				fVertical.setEditable(true);
				fHorizontal.setEditable(true);
				fDropHeight.setEditable(true);
				fTimeCount.setEditable(true);
				fParachuteIndicator.setEditable(true);
				fAxialEng1.setEditable(true);
				fAxialEng2.setEditable(true);
				fAxialEng3.setEditable(true);
				fRollEng1.setEditable(true);
				fRollEng2.setEditable(true);
				fRollEng3.setEditable(true);
				fTime.setEditable(true);

				tdEdit.setEnabled(false);

				final JButton done = new JButton(" Done ");
				panel1.add(done);
				done.setBackground(Color.GREEN);

				done.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tdEdit.setEnabled(true);
						fAltitude.setEditable(false);
						fAltitude.setEditable(false);
						fThrust.setEditable(false);
						fEng1.setEditable(false);
						fEng2.setEditable(false);
						fEng3.setEditable(false);
						fAccel1.setEditable(false);
						fAccel2.setEditable(false);
						fAccel3.setEditable(false);
						fTime.setEditable(false);
						fAngle.setEditable(false);
						fVar1.setEditable(false);
						fTempSensor.setEditable(false);
						fVertical.setEditable(false);
						fHorizontal.setEditable(false);
						fDropHeight.setEditable(false);
						fTimeCount.setEditable(false);
						fParachuteIndicator.setEditable(false);
						fAxialEng1.setEditable(false);
						fAxialEng2.setEditable(false);
						fAxialEng3.setEditable(false);
						fRollEng1.setEditable(false);
						fRollEng2.setEditable(false);
						fRollEng3.setEditable(false);
						fTime.setEditable(false);

						done.setVisible(false);
						cSimulation.setEnabled(false);
						done.setForeground(Color.RED);
					}
				});

			}
		});

	}

	// Create Page 1

	public void createPage1() {
		panel1 = new JPanel();

		JLabel lAltitude = new JLabel("Altitiude:");
		fAltitude = new JFormattedTextField("NA");
		JLabel lThrust = new JLabel("Engine Thrust:");
		fThrust = new JFormattedTextField("NA");
		JLabel lEng1 = new JLabel("Eng 1 Speed:");
		fEng1 = new JFormattedTextField("NA");
		JLabel lEng2 = new JLabel("Eng 2 Speed:");
		fEng2 = new JFormattedTextField("NA");
		JLabel lEng3 = new JLabel("Eng 3 Speed:");
		fEng3 = new JFormattedTextField("NA");
		JLabel lAccel1 = new JLabel("Accelorometer 1:");
		fAccel1 = new JFormattedTextField("NA");
		JLabel lAccel2 = new JLabel("Accelorometer 2:");
		fAccel2 = new JFormattedTextField("NA");
		JLabel lAccel3 = new JLabel("Accelorometer 3:");
		fAccel3 = new JFormattedTextField("NA");
		JLabel lAngle = new JLabel("Slope/Angle: ");
		fAngle = new JFormattedTextField("NA");
		JLabel lVar1 = new JLabel("Decent Rate: ");
		fVar1 = new JFormattedTextField("NA");
		JLabel lTempSensor = new JLabel("Engine Temperature");
		fTempSensor = new JFormattedTextField("NA");
		JLabel lVertical = new JLabel("Vertical Drop Angle: ");
		fVertical = new JFormattedTextField("NA");
		JLabel lHorizontal = new JLabel("Horizontal Drop Angle");
		fHorizontal = new JFormattedTextField("NA");
		JLabel lTimeCount = new JLabel("Time to heat the engines(sec):");
		fTimeCount = new JFormattedTextField("NA");
		JLabel lParachuteIndicator = new JLabel("Parachute Status: ");
		fParachuteIndicator = new JFormattedTextField("NA");
		JLabel lDropHeight = new JLabel("Drop Height: ");
		fDropHeight = new JFormattedTextField("NA");
		JLabel lAxialEng1 = new JLabel("Axial Eng1: ");
		fAxialEng1 = new JFormattedTextField("NA");
		JLabel lAxialEng2 = new JLabel("Axial Eng2: ");
		fAxialEng2 = new JFormattedTextField("NA");
		JLabel lAxialEng3 = new JLabel("Axial Eng3: ");
		fAxialEng3 = new JFormattedTextField("NA");
		JLabel lRollEng1 = new JLabel("Roll Eng1: ");
		fRollEng1 = new JFormattedTextField("NA");
		JLabel lRollEng2 = new JLabel("Roll Eng2: ");
		fRollEng2 = new JFormattedTextField("NA");
		JLabel lRollEng3 = new JLabel("Roll Eng3: ");
		fRollEng3 = new JFormattedTextField("NA");
		graphView = new JButton("Graphical Simulation View");
		fTime = new JFormattedTextField("NA");
		JLabel lTime = new JLabel("GCS Timer: ");
		newSimulation = new JButton("New Simulation");

		phase1 = new JButton("Phase 1");
		phase2 = new JButton("Phase 2");
		phase3 = new JButton("Phase 3");
		phase4 = new JButton("Phase 4");
		phase5 = new JButton("Phase 5");

		panel1.add(lTimeCount);
		lTimeCount.setForeground(Color.WHITE);
		panel1.add(fTimeCount);
		panel1.add(lTime);
		lTime.setForeground(Color.WHITE);
		panel1.add(fTime);
		panel1.add(lAxialEng1);
		lAxialEng1.setForeground(Color.WHITE);
		panel1.add(fAxialEng1);
		panel1.add(lAxialEng2);
		lAxialEng2.setForeground(Color.WHITE);
		panel1.add(fAxialEng2);
		panel1.add(lAxialEng3);
		lAxialEng3.setForeground(Color.WHITE);
		panel1.add(fAxialEng3);
		panel1.add(lRollEng1);
		lRollEng1.setForeground(Color.WHITE);
		panel1.add(fRollEng1);
		panel1.add(lRollEng2);
		lRollEng2.setForeground(Color.WHITE);
		panel1.add(fRollEng2);
		panel1.add(lRollEng3);
		lRollEng3.setForeground(Color.WHITE);
		panel1.add(fRollEng3);
		panel1.add(lAltitude);
		lAltitude.setForeground(Color.WHITE);
		panel1.add(fAltitude);
		panel1.add(lParachuteIndicator);
		lParachuteIndicator.setForeground(Color.WHITE);
		panel1.add(fParachuteIndicator);
		panel1.add(lThrust);
		lThrust.setForeground(Color.WHITE);
		panel1.add(fThrust);
		panel1.add(lEng1);
		lEng1.setForeground(Color.WHITE);
		panel1.add(fEng1);
		panel1.add(lEng2);
		lEng2.setForeground(Color.WHITE);
		panel1.add(fEng2);
		panel1.add(lEng3);
		lEng3.setForeground(Color.WHITE);
		panel1.add(fEng3);
		panel1.add(lAccel1);
		lAccel1.setForeground(Color.WHITE);
		panel1.add(fAccel1);
		panel1.add(lAccel2);
		lAccel2.setForeground(Color.WHITE);
		panel1.add(fAccel2);
		panel1.add(lAccel3);
		lAccel3.setForeground(Color.WHITE);
		panel1.add(fAccel3);
		panel1.add(lAngle);
		lAngle.setForeground(Color.WHITE);
		panel1.add(fAngle);
		panel1.add(lTempSensor);
		lTempSensor.setForeground(Color.WHITE);
		panel1.add(fTempSensor);
		panel1.add(lVertical);
		lVertical.setForeground(Color.WHITE);
		panel1.add(fVertical);
		panel1.add(lHorizontal);
		lHorizontal.setForeground(Color.WHITE);
		panel1.add(fHorizontal);
		panel1.add(lDropHeight);
		lDropHeight.setForeground(Color.WHITE);
		panel1.add(fDropHeight);

		fAltitude.setEditable(false);
		fAltitude.setEditable(false);
		fThrust.setEditable(false);
		fEng1.setEditable(false);
		fEng2.setEditable(false);
		fEng3.setEditable(false);
		fAccel1.setEditable(false);
		fAccel2.setEditable(false);
		fAccel3.setEditable(false);
		fAngle.setEditable(false);
		fVar1.setEditable(false);
		fTempSensor.setEditable(false);
		fVertical.setEditable(false);
		fHorizontal.setEditable(false);
		fTimeCount.setEditable(false);
		fParachuteIndicator.setEditable(false);
		fDropHeight.setEditable(false);
		fAxialEng1.setEditable(false);
		fAxialEng2.setEditable(false);
		fAxialEng3.setEditable(false);
		fRollEng1.setEditable(false);
		fRollEng2.setEditable(false);
		fRollEng3.setEditable(false);
		fTime.setEditable(false);
		phase1.setBackground(Color.GREEN);
		phase2.setBackground(Color.GREEN);
		phase3.setBackground(Color.GREEN);
		phase4.setBackground(Color.GREEN);
		phase5.setBackground(Color.GREEN);
		panel1.setBackground(Color.BLACK);

		GridLayout bLayout = new GridLayout(27, 2, 10, 10);
		panel1.setLayout(bLayout);

	}

	// Create Page 2

	public void createPage2() {
		/*
		 * String sql = "SELECT * FROM MARSLANDER"; final Vector columnNames =
		 * new Vector(); final Vector data = new Vector(); panel2 = new
		 * JPanel(); panel2.setBackground(Color.BLACK); JLabel lSqlQuery = new
		 * JLabel( "Enter SQL Query = Select * From Mars Lander"); fSqlQuery =
		 * new JTextArea();
		 * 
		 * submit = new JButton(" Submit "); panel2.add(lSqlQuery);
		 * lSqlQuery.setForeground(Color.WHITE); panel2.add(fSqlQuery);
		 * panel2.add(submit); submit.setBackground(Color.GREEN);
		 * 
		 * try { Class.forName("oracle.jdbc.driver.OracleDriver"); Connection
		 * con = DriverManager
		 * .getConnection("jdbc:oracle:thin:@localhost:1521:teamb", "SYSTEM",
		 * "e307016.ACCT04");
		 * 
		 * Statement stmt = con.createStatement(); ResultSet rs =
		 * stmt.executeQuery(sql); ResultSetMetaData md = rs.getMetaData(); int
		 * columns = md.getColumnCount(); for (int i = 1; i <= columns; i++) {
		 * columnNames.addElement(md.getColumnName(i)); }
		 * 
		 * while (rs.next()) { Vector row = new Vector(columns); for (int i = 1;
		 * i <= columns; i++) { row.addElement(rs.getObject(i)); }
		 * data.addElement(row); } rs.close(); stmt.close();
		 * 
		 * } catch (Exception e) { System.out.println(e); }
		 * 
		 * JTable table = new JTable(data, columnNames); TableColumn col; for
		 * (int i = 0; i < table.getColumnCount(); i++) { col =
		 * table.getColumnModel().getColumn(i); col.setMaxWidth(250); }
		 * JScrollPane scrollPane = new JScrollPane(table);
		 * panel2.add(scrollPane);
		 * 
		 * BoxLayout bLayout = new BoxLayout(panel2, 1);
		 * panel2.setLayout(bLayout); submit.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent e) { if
		 * (e.getSource() == submit) {
		 * 
		 * try { Class.forName("oracle.jdbc.driver.OracleDriver"); String url =
		 * "jdbc:oracle:thin:@localhost:1521:teamb";
		 * 
		 * String Username = "SYSTEM";
		 * 
		 * String Password = "e307016.ACCT04";
		 * 
		 * /** this part takes the String (which is a query) from a TextField
		 * 
		 * and execute the query after the connection to mysql database has been
		 * made
		 * 
		 * 
		 * String query = fSqlQuery.getText();
		 * 
		 * conn = DriverManager.getConnection(url, Username, Password);
		 * 
		 * System.out.print("connected");
		 * 
		 * stmt = conn.createStatement();
		 * 
		 * ResultSet result = stmt.executeQuery(query);
		 * 
		 * Vector columnNames = new Vector(); Vector data = new Vector();
		 * 
		 * int columns = md.getColumnCount(); for (int i = 1; i <= columns; i++)
		 * { columnNames.addElement(md.getColumnName(i)); }
		 * 
		 * while (rs.next()) { Vector row = new Vector(columns); for (int i = 1;
		 * i <= columns; i++) { row.addElement(rs.getString(query)); }
		 * data.addElement(row); } rs.close(); stmt.close();
		 * 
		 * } catch (Exception em) { System.out.println(em); }
		 * 
		 * JTable table = new JTable(data, columnNames); TableColumn col; for
		 * (int i = 0; i < table.getColumnCount(); i++) { col =
		 * table.getColumnModel().getColumn(i); col.setMaxWidth(250); }
		 * JScrollPane scrollPane = new JScrollPane(table);
		 * panel2.add(scrollPane);
		 * 
		 * BoxLayout bLayout = new BoxLayout(panel2, 1);
		 * panel2.setLayout(bLayout); } }
		 * 
		 * });
		 */

	}

	// Create Page 3

	public void createPage3() {
		/*
		 * String sql = "SELECT * FROM PREDEFINED"; Vector columnNames = new
		 * Vector(); Vector data = new Vector(); panel3 = new JPanel(); JLabel
		 * lSqlQuery = new JLabel(
		 * "Enter SQL Query = Select * From Mars Lander");
		 * lSqlQuery.setBackground(Color.GREEN); fSqlQuery = new JTextArea(null,
		 * sql, 2, 2); submit = new JButton(" Submit "); panel3.add(lSqlQuery);
		 * panel3.add(fSqlQuery); panel3.add(submit);
		 * submit.setBackground(Color.GREEN);
		 * lSqlQuery.setForeground(Color.WHITE);
		 * panel3.setBackground(Color.BLACK); try {
		 * Class.forName("oracle.jdbc.driver.OracleDriver"); Connection con =
		 * DriverManager
		 * .getConnection("jdbc:oracle:thin:@localhost:1521:teamb", "SYSTEM",
		 * "e307016.ACCT04");
		 * 
		 * Statement stmt = con.createStatement(); ResultSet rs =
		 * stmt.executeQuery(sql); ResultSetMetaData md = rs.getMetaData(); int
		 * columns = md.getColumnCount(); for (int i = 1; i <= columns; i++) {
		 * columnNames.addElement(md.getColumnName(i)); }
		 * 
		 * while (rs.next()) { Vector row = new Vector(columns); for (int i = 1;
		 * i <= columns; i++) { row.addElement(rs.getObject(i)); }
		 * data.addElement(row); } rs.close(); stmt.close();
		 * 
		 * } catch (Exception e) { System.out.println(e); }
		 * 
		 * JTable table = new JTable(data, columnNames); TableColumn col; for
		 * (int i = 0; i < table.getColumnCount(); i++) { col =
		 * table.getColumnModel().getColumn(i); col.setMaxWidth(250); }
		 * JScrollPane scrollPane = new JScrollPane(table);
		 * panel3.add(scrollPane);
		 * 
		 * BoxLayout bLayout = new BoxLayout(panel3, 1);
		 * panel3.setLayout(bLayout);
		 */
	}

	// Create Page 4
	public void createPage4() {
		Color bg = new Color(Color.TRANSLUCENT);
		panel4 = new JPanel();
		JLabel l = new JLabel();
		liveSimulation = new JButton("Real Time Simulation");

		panel4.setSize(600, 800);
		BufferedImage marslander = new BufferedImage(10, 15,
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage background = new BufferedImage(10, 15,
				BufferedImage.TYPE_INT_ARGB);
		try {
			marslander = ImageIO.read(image);
			background = ImageIO.read(background1);
		} catch (IOException e) {
		}

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(1));
		Shape s = new QuadCurve2D.Double(-200, 40, 280, 70, 350, 600);
		g.drawImage(background, null, 5, -200);
		g.draw(s);
		g.dispose();
		l.setIcon(new ImageIcon(lcurve));

		panel4.add(l);

		l.setBackground(bg);
		BoxLayout bLayout = new BoxLayout(panel4, 1);
		panel4.setLayout(bLayout);
		panel4.setBackground(Color.BLACK);
		panel4.add(liveSimulation);
		panel4.add(newSimulation);
		newSimulation.setBackground(Color.GREEN);
		newSimulation.setVisible(false);
		liveSimulation.setVisible(false);

	}

	public String setQuery(String query) {
		return query;
	}

	public class exitApp implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	// Getters and setters for all attributes NEEDED!!
	public double getDropHeight() {

		return dropHeight;

	}

	public void setDropHeight(double dropHeight) {

		this.dropHeight = dropHeight;

	}

	public double getEng1() {

		return eng1;
	}

	public void setEng1(double eng1) {
		this.eng1 = eng1;
	}

	public double getEng2() {
		return eng2;
	}

	public void setEng2(double eng2) {
		this.eng2 = eng2;
	}

	public void setEng3(double eng3) {
		this.eng3 = eng3;
	}

	public double getEng3() {
		return eng3;
	}

	public double getAccel1() {
		return accel1;
	}

	public void setAccel1(double accel1) {
		this.accel1 = accel1;
	}

	public double getAccel2() {
		return accel2;
	}

	public void setAccel2(double accel2) {
		this.accel2 = accel2;
	}

	public double getAccel3() {
		return accel3;
	}

	public void setAccel3(double accel3) {
		this.accel3 = accel3;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getAltitude() {

		return altitude;
	}

	public void setTime(double timeCount) {
		this.timeCount = timeCount;
	}

	public double getTime() {
		return timeCount;
	}

	public void setSYSTime(double time) {
		this.time = time;
	}

	public double getSYSTime() {
		return time;
	}

	public double getThrust() {
		return thrust;
	}

	public void setThrust(double thrust) {
		this.thrust = thrust;
	}

	public double getVar1() {
		return var1;
	}

	public void setVar1(double var1) {
		this.var1 = var1;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public void setParachutIndicator(boolean parachuteIndicator) {
		this.parachuteIndicator = parachuteIndicator;
	}

	public boolean getParachuteIndictor() {
		return parachuteIndicator;

	}

	// Setters and Getters for all variables. Mike, you said setters aren't
	// needed but I am
	// including them just in case.
	public void setAxialEng1(double axialEng1) {
		this.axialEng1 = axialEng1;
	}

	public void setAxialEng2(double axialEng2) {
		this.axialEng2 = axialEng2;
	}

	public void setAxialEng3(double axialEng3) {
		this.axialEng3 = axialEng3;
	}

	public void setRollEng1(double rollEng1) {
		this.rollEng1 = rollEng1;
	}

	public void setRollEng2(double rollEng2) {
		this.rollEng2 = rollEng2;
	}

	public void setRollEng3(double rollEng3) {
		this.rollEng3 = rollEng3;
	}

	public double getAxialEng1() {
		return axialEng1;
	}

	public double getAxialEng2() {
		return axialEng2;
	}

	public double getAxialEng3() {
		return axialEng3;
	}

	public double getRollEng1() {
		return rollEng1;
	}

	public double getRollEng2() {
		return rollEng2;
	}

	public double getRollEng3() {
		return rollEng3;
	}

	public void setEngTemp(int engineTemp) {

		this.engineTemp = engineTemp;
	}

	public int getEngTemp() {
		return engineTemp;
	}

	// Produces 95 % chance of Mars Lander Process succeeding
	public boolean parachuteIndicator(int percentGiven) {
		Random rand = new Random();
		int roll = rand.nextInt(100);
		if (roll < percentGiven)
			return true;
		else
			return false;
	}

	public void paintComponent(Graphics g) {
		super.paintComponents(g);
		ImageIcon i = new ImageIcon("marslander.png");
	}

	public void sysLog() {
		PrintWriter out;
		try {
			out = new PrintWriter("./src/GCSlog" + logcount + ".txt");
			out.println("Phase "
					+ phasecount
					+ " + \n/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////"
					+ "Altitude: " + Double.toString(altitude) + "\n"
					+ "Engine 1: " + Double.toString(eng1) + "\n"
					+ "  Engine 2: " + Double.toString(eng2) + "\n"
					+ "  Engine 3: " + Double.toString(eng3) + "\n"
					+ "  Accelorometer 1: " + Double.toString(accel1)
					+ " MPH  " + "\n" + " Accelorometer 2: "
					+ Double.toString(accel2) + " MPH  " + "\n"
					+ " Accelrometer 3: " + Double.toString(accel3) + " MPH "
					+ "\n" + "  Engine 50% Thrust: " + Double.toString(thrust)
					+ "\n" + "  Descent Time: " + fmt.format(time)
					+ " minutes " + "\n" + "  Descent Rate: "
					+ Double.parseDouble(fmt.format(Math.toDegrees(var1)))
					+ " feet per minute  " + "  Drop Height: "
					+ Double.toString(dropHeight) + "\n" + "   Air Speed: "
					+ Double.toString(airspeed) + "\n" + "   Elevation: "
					+ Double.toString(elevation) + "   Landing Distance:"
					+ Double.toString(distance));
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}

	}

	public void sysSleep() {

		while (second > 0) {

			try {

				Thread.sleep(100);

			}

			catch (Exception e) {
			}

			second--;

		}

	}

	// Phase 1

	public void Phase1() throws SQLException {

		/*
		 * try {
		 * 
		 * conn = DriverManager
		 * .getConnection("jdbc:oracle:thin:@localhost:1521:teamb", "SYSTEM",
		 * "e307016.ACCT04"); Statement stmt = conn.createStatement(); rs = stmt
		 * .
		 * executeQuery("SELECT ALTITUDE FROM PREDEFINED WHERE ALTITUDE = 26400"
		 * ); while (rs.next()) { String alt = rs.getString("altitude");
		 * System.out.println(alt); altitude = Double.parseDouble(alt);
		 */

		fAltitude.setText("" + altitude);
		fAxialEng1.setText(" Ignited");
		fAxialEng2.setText(" Ignited");
		fAxialEng3.setText(" Ignited");
		fRollEng1.setText(" Ignited");
		fRollEng2.setText(" Ignited");
		fRollEng3.setText(" Ignited");
		fThrust.setText("NA");
		fEng1.setText("ON");
		fEng2.setText("ON");
		fEng3.setText("ON");
		fAccel1.setText("ON");
		fAccel2.setText("ON");
		fAccel3.setText("ON");
		fAngle.setText("" + getAngle() * 2);
		fVar1.setText("" + getVar1());

		fAxialEng1.setText("Warming!");
		fAxialEng2.setText("Warming!");
		fAxialEng3.setText("Warming!");
		fRollEng1.setText("Warming!");
		fRollEng2.setText("Warming!");
		fRollEng3.setText("Warming!");
		fTempSensor.setText("" + engineTemp * .05);
		fVertical.setText("" + getAngle() * 1.14);
		fHorizontal.setText("" + getAngle() * 1.14);
		fParachuteIndicator.setText("Open = "
				+ (parachuteIndicator = "Yes" != null));
		fTime.setText("" + fmt.format(timeCount + 30));
		phasecount++;
		logcount++;
		PrintWriter out;

		try {
			out = new PrintWriter("./src/GCSlog" + logcount + ".txt");
			out.println("Phase " + phasecount + " \n" + " Altitude: "
					+ (fAltitude.getText()) + " \n" + "Engine 1: "
					+ (fEng1.getText()) + "\n" + "  Engine 2: "
					+ (fEng2.getText()) + "\n" + "  Engine 3: "
					+ (fEng2.getText()) + "\n" + "  Accelorometer 1: "
					+ (fAccel1.getText()) + " MPH  " + "\n"
					+ " Accelorometer 2: " + (fAccel2.getText()) + " MPH  "
					+ "\n" + " Accelrometer 3: " + (fAccel3.getText())
					+ " MPH " + "\n" + "  Engine 50% Thrust: "
					+ (fThrust.getText()) + "\n" + "  Descent Time: "
					+ (fTime.getText()) + " minutes " + "\n"
					+ "  Descent Rate: " + (fVar1.getText())
					+ " feet per minute  " + "  Drop Height: "
					+ (fDropHeight.getText()) + "\n" + "   Air Speed: "
					+ (fAngle.getText()) + "\n" + "   Horizontal Speed: "
					+ (fHorizontal.getText()) + "   Vertical Speed: "
					+ (fVertical.getText()) + "  Engine Warm Up Time: "
					+ (fTimeCount.getText()) + "   Parachute Indicator: "
					+ (fParachuteIndicator.getText()) + "   Dropheight: "
					+ (fDropHeight.getText()) + "  Axial Engine 1 Status: "
					+ (fAxialEng1.getText()) + "  Axial Engine 2 Status: "
					+ (fAxialEng2.getText()) + "  Axial Engine 3 Status: "
					+ (fAxialEng3.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng1.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng2.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng3.getText()));
			out.close();
		} catch (FileNotFoundException ey) {
			System.out.println(ey);

		}
		/*
		 * } conn.close(); } catch (SQLException e) {
		 * System.err.println("Got an exception! ");
		 * System.err.println(e.getMessage());
		 * 
		 * }
		 */

	}

	// Phase 2
	public void Phase2() {

		do {

			try {

				Thread.currentThread().sleep(10);// sleep for 1000 ms = 1 sec
				engineTemp = engineTemp + tempDegree;
			} catch (InterruptedException ie) {
				// in case thread.sleep() is tired
			}

			timeCount++;

			if (engineTemp >= engineDesiredTemp) {
				parachuteIndicator = false;
			}
		} while (parachuteIndicator);

		fAltitude.setText("" + (altitude - 1000));
		fAxialEng1.setText("Idling " + engineTemp + " Degrees");
		fAxialEng2.setText("Idling " + engineTemp + " Degrees");
		fAxialEng3.setText("Idling " + engineTemp + " Degrees");
		fRollEng1.setText("ON");
		fRollEng2.setText("ON");
		fRollEng3.setText("ON");
		fTempSensor.setText("" + engineTemp);
		fTimeCount.setText("" + fmt.format(timeCount));
		fTime.setText("" + fmt.format(timeCount + 30));
		parachuteIndicator = true;
		fParachuteIndicator.setText("Open = " + parachuteIndicator);
		phasecount++;
		logcount++;
		PrintWriter out;

		try {
			out = new PrintWriter("./src/GCSlog" + logcount + ".txt");
			out.println("Phase " + phasecount + " \n" + " Altitude: "
					+ (fAltitude.getText()) + "\n" + "Engine 1: "
					+ (fEng1.getText()) + "\n" + "  Engine 2: "
					+ (fEng2.getText()) + "\n" + "  Engine 3: "
					+ (fEng2.getText()) + "\n" + "  Accelorometer 1: "
					+ (fAccel1.getText()) + " MPH  " + "\n"
					+ " Accelorometer 2: " + (fAccel2.getText()) + " MPH  "
					+ "\n" + " Accelrometer 3: " + (fAccel3.getText())
					+ " MPH " + "\n" + "  Engine 50% Thrust: "
					+ (fThrust.getText()) + "\n" + "  Descent Time: "
					+ (fTime.getText()) + " minutes " + "\n"
					+ "  Descent Rate: " + (fVar1.getText())
					+ " feet per minute  " + "  Drop Height: "
					+ (fDropHeight.getText()) + "\n" + "   Air Speed: "
					+ (fAngle.getText()) + "\n" + "   Horizontal Speed: "
					+ (fHorizontal.getText()) + "   Vertical Speed: "
					+ (fVertical.getText()) + "  Engine Warm Up Time: "
					+ (fTimeCount.getText()) + "   Parachute Indicator: "
					+ (fParachuteIndicator.getText()) + "   Dropheight: "
					+ (fDropHeight.getText()) + "  Axial Engine 1 Status: "
					+ (fAxialEng1.getText()) + "  Axial Engine 2 Status: "
					+ (fAxialEng2.getText()) + "  Axial Engine 3 Status: "
					+ (fAxialEng3.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng1.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng2.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng3.getText()));
			out.close();
			out.close();
		} catch (FileNotFoundException ey) {
			System.out.println(ey);

		}
	}

	// Phase3
	public void Phase3() {
		parachuteIndicator = true;
		Scanner in = new Scanner(System.in);
		String type = new String();
		eng1 = 500; // engine 1 top speed
		eng2 = 500;// engine 2 top speed
		eng3 = 500;// engine 3 top speed
		thrust = (eng1 + eng2 + eng3) * .5;
		accel1 = eng1 + thrust;
		accel2 = eng2 + thrust;
		accel3 = eng3 + thrust;
		/*
		 * try {
		 * 
		 * conn = DriverManager
		 * .getConnection("jdbc:oracle:thin:@localhost:1521:teamb", "SYSTEM",
		 * "e307016.ACCT04"); Statement stmt = conn.createStatement(); rs = stmt
		 * .
		 * executeQuery("SELECT DROPHEIGHT FROM PREDEFINED WHERE DROPHEIGHT = 500"
		 * ); while (rs.next()) { String dead = rs.getString("dropheight");
		 * System.out.println(dead); dropHeight = Double.parseDouble(dead);
		 * 
		 * } conn.close(); } catch (SQLException e) {
		 * System.err.println("Got an exception! ");
		 * System.err.println(e.getMessage());
		 * 
		 * }
		 */

		while (altitude > dropHeight) {
			int i;
			for (i = 1; i < (altitude - dropHeight); i++) {

				break;
			}

			eng1 = eng1 * .5;
			eng2 = eng2 * .5;
			eng3 = eng3 * .5;
			accel1 = ((thrust / 3) * 1);
			accel2 = ((thrust / 3) * 1);
			accel3 = ((thrust / 3) * 1);

			altitude = dropHeight;
			elevation = 20;
			distance = 2000;
			airspeed = (airspeed * 6076) / 60;
			distance = distance * 6076;
			altitude = altitude - 50 - elevation;
			angle = Math.acos(Math.toRadians(altitude / distance));
			var1 = Math.cos(angle) * airspeed;
			time = (altitude / Math.toDegrees(var1));

			// Set JTextFields With Results
			fAltitude.setText("" + altitude);
			fThrust.setText("Engine 50% Thrust: " + fmt.format(thrust));
			fEng1.setText("MPH: " + eng1);
			fEng2.setText("MPH: " + eng2);
			fEng3.setText("MPH: " + eng3);
			fAccel1.setText("" + fmt.format(accel1));
			fAccel2.setText("" + fmt.format(accel2));
			fAccel3.setText("" + fmt.format(accel3));
			fAltitude.setText("" + altitude);
			fAxialEng1.setText("Warm Up Done! " + engineTemp * 1.05
					+ " Degrees");
			fAxialEng2.setText("Warm Up Done! " + engineTemp * 1.05
					+ " Degrees");
			fAxialEng3.setText("Warm Up Done! " + engineTemp * 1.05
					+ " Degrees");
			fRollEng1.setText("Ready ");
			fRollEng2.setText("Ready ");
			fRollEng3.setText("Ready ");
			fTempSensor.setText("" + engineTemp * 1.05);
			fTime.setText("" + fmt.format(timeCount + 60));
			fAngle.setText("" + angle);
			fVar1.setText("" + var1);
			fVertical.setText("" + distance + var1);
			fHorizontal.setText("" + (distance - var1));
			fParachuteIndicator.setText("Released = " + parachuteIndicator);
			phasecount++;
			logcount++;
			PrintWriter out;

			try {
				out = new PrintWriter("./src/GCSlog" + logcount + ".txt");
				out.println("Phase " + phasecount + " \n" + " Altitude: "
						+ (fAltitude.getText()) + "\n" + "Engine 1: "
						+ (fEng1.getText()) + "\n" + "  Engine 2: "
						+ (fEng2.getText()) + "\n" + "  Engine 3: "
						+ (fEng2.getText()) + "\n" + "  Accelorometer 1: "
						+ (fAccel1.getText()) + " MPH  " + "\n"
						+ " Accelorometer 2: " + (fAccel2.getText()) + " MPH  "
						+ "\n" + " Accelrometer 3: " + (fAccel3.getText())
						+ " MPH " + "\n" + "  Engine 50% Thrust: "
						+ (fThrust.getText()) + "\n" + "  Descent Time: "
						+ (fTime.getText()) + " minutes " + "\n"
						+ "  Descent Rate: " + (fVar1.getText())
						+ " feet per minute  " + "  Drop Height: "
						+ (fDropHeight.getText()) + "\n" + "   Air Speed: "
						+ (fAngle.getText()) + "\n" + "   Horizontal Speed: "
						+ (fHorizontal.getText()) + "   Vertical Speed: "
						+ (fVertical.getText()) + "  Engine Warm Up Time: "
						+ (fTimeCount.getText()) + "   Parachute Indicator: "
						+ (fParachuteIndicator.getText()) + "   Dropheight: "
						+ (fDropHeight.getText()) + "  Axial Engine 1 Status: "
						+ (fAxialEng1.getText()) + "  Axial Engine 2 Status: "
						+ (fAxialEng2.getText()) + "  Axial Engine 3 Status: "
						+ (fAxialEng3.getText()) + "  Roll Engine 1 Status: "
						+ (fRollEng1.getText()) + "  Roll Engine 1 Status: "
						+ (fRollEng2.getText()) + "  Roll Engine 1 Status: "
						+ (fRollEng3.getText()));
				out.close();
				out.close();
			} catch (FileNotFoundException ey) {
				System.out.println(ey);

			}
		}
	}

	// Phase 4
	public void Phase4(double altitude, double thrust) {

		// Need to know the predetermined values for drop height

		// the Altitude and Velocity

		if (altitude > dropHeight)

		{

			// Set JTextFields With Results

			fDropHeight.setText("NO");
		}

		else

			fAxialEng1
					.setText("Warm Up Done! " + engineTemp * .75 + " Degrees");
		fAxialEng2.setText("Warm Up Done! " + engineTemp * .75 + " Degrees");
		fAxialEng3.setText("Warm Up Done! " + engineTemp * .75 + " Degrees");
		fRollEng1.setText("Warm Up Done! " + engineTemp * .75 + " Degrees");
		fRollEng2.setText("Warm Up Done! " + engineTemp * .75 + " Degrees");
		fRollEng3.setText("Warm Up Done! " + engineTemp * .75 + " Degrees");
		fTempSensor.setText("" + engineTemp * .75);
		fEng1.setText(" OFF");
		fEng2.setText(" OFF");
		fEng3.setText(" OFF");
		fAccel1.setText("OFF");
		fAccel2.setText("OFF");
		fAccel3.setText("OFF");
		fRollEng1.setText(" OFF");
		fRollEng2.setText(" OFF");
		fRollEng3.setText(" OFF");
		fTime.setText("" + fmt.format(timeCount + 30));
		fDropHeight.setText("YES"); // changes the text box from
		phasecount++;
		logcount++;
		PrintWriter out;

		try {
			out = new PrintWriter("./src/GCSlog" + logcount + ".txt");
			out.println("Phase " + phasecount + " \n" + " Altitude: "
					+ (fAltitude.getText()) + "\n" + "Engine 1: "
					+ (fEng1.getText()) + "\n" + "  Engine 2: "
					+ (fEng2.getText()) + "\n" + "  Engine 3: "
					+ (fEng2.getText()) + "\n" + "  Accelorometer 1: "
					+ (fAccel1.getText()) + " MPH  " + "\n"
					+ " Accelorometer 2: " + (fAccel2.getText()) + " MPH  "
					+ "\n" + " Accelrometer 3: " + (fAccel3.getText())
					+ " MPH " + "\n" + "  Engine 50% Thrust: "
					+ (fThrust.getText()) + "\n" + "  Descent Time: "
					+ (fTime.getText()) + " minutes " + "\n"
					+ "  Descent Rate: " + (fVar1.getText())
					+ " feet per minute  " + "  Drop Height: "
					+ (fDropHeight.getText()) + "\n" + "   Air Speed: "
					+ (fAngle.getText()) + "\n" + "   Horizontal Speed: "
					+ (fHorizontal.getText()) + "   Vertical Speed: "
					+ (fVertical.getText()) + "  Engine Warm Up Time: "
					+ (fTimeCount.getText()) + "   Parachute Indicator: "
					+ (fParachuteIndicator.getText()) + "   Dropheight: "
					+ (fDropHeight.getText()) + "  Axial Engine 1 Status: "
					+ (fAxialEng1.getText()) + "  Axial Engine 2 Status: "
					+ (fAxialEng2.getText()) + "  Axial Engine 3 Status: "
					+ (fAxialEng3.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng1.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng2.getText()) + "  Roll Engine 1 Status: "
					+ (fRollEng3.getText()));
			out.close();
			out.close();
		} catch (FileNotFoundException ey) {
			System.out.println(ey);

		}
	}

	// Phase 5
	public int Phase5() {

		int counter = 1;
		altitude = altitude - altitude;

		do {

			if (altitude < (dropHeight - 499)) {
				counter = 0;

				// Set JTextFields With Results
				fAltitude.setText("" + getAltitude());
				fAxialEng1.setText("" + engineTemp * .55 + " Degrees");
				fAxialEng2.setText("" + engineTemp * .55 + " Degrees");
				fAxialEng3.setText("" + engineTemp * .55 + " Degrees");
				fRollEng1.setText("" + engineTemp * .55 + " Degrees");
				fRollEng2.setText("" + engineTemp * .55 + " Degrees");
				fRollEng3.setText("" + engineTemp * .55 + " Degrees");
				fTempSensor.setText("" + engineTemp * .55);
				fVertical.setText("" + distance + var1);
				fHorizontal.setText("" + (distance - var1));
				fTime.setText("" + fmt.format(timeCount + 30));
				fDropHeight.setText("Touchdown ");
				phasecount++;
				logcount++;
				PrintWriter out;

				try {
					out = new PrintWriter("./src/GCSlog" + logcount + ".txt");
					out.println("Phase " + phasecount + " \n" + " Altitude: "
							+ (fAltitude.getText()) + "\n" + "Engine 1: "
							+ (fEng1.getText()) + "\n" + "  Engine 2: "
							+ (fEng2.getText()) + "\n" + "  Engine 3: "
							+ (fEng2.getText()) + "\n" + "  Accelorometer 1: "
							+ (fAccel1.getText()) + " MPH  " + "\n"
							+ " Accelorometer 2: " + (fAccel2.getText())
							+ " MPH  " + "\n" + " Accelrometer 3: "
							+ (fAccel3.getText()) + " MPH " + "\n"
							+ "  Engine 50% Thrust: " + (fThrust.getText())
							+ "\n" + "  Descent Time: " + (fTime.getText())
							+ " minutes " + "\n" + "  Descent Rate: "
							+ (fVar1.getText()) + " feet per minute  "
							+ "  Drop Height: " + (fDropHeight.getText())
							+ "\n" + "   Air Speed: " + (fAngle.getText())
							+ "\n" + "   Horizontal Speed: "
							+ (fHorizontal.getText()) + "   Vertical Speed: "
							+ (fVertical.getText()) + "  Engine Warm Up Time: "
							+ (fTimeCount.getText())
							+ "   Parachute Indicator: "
							+ (fParachuteIndicator.getText())
							+ "   Dropheight: " + (fDropHeight.getText())
							+ "  Axial Engine 1 Status: "
							+ (fAxialEng1.getText())
							+ "  Axial Engine 2 Status: "
							+ (fAxialEng2.getText())
							+ "  Axial Engine 3 Status: "
							+ (fAxialEng3.getText())
							+ "  Roll Engine 1 Status: "
							+ (fRollEng1.getText())
							+ "  Roll Engine 1 Status: "
							+ (fRollEng2.getText())
							+ "  Roll Engine 1 Status: "
							+ (fRollEng3.getText()));
					out.close();
					out.close();
				} catch (FileNotFoundException ey) {
					System.out.println(ey);

				}
			}
		} while (counter == 1);
		return 0;
	}

	// Main method to get things started
	public static void main(String args[]) {
		// Create an instance of the test application
		GCSInterface mainFrame = new GCSInterface();
	}

}
