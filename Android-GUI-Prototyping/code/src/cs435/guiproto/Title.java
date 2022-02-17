package cs435.guiproto;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.xml.sax.SAXException;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JFileChooser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * This class implements the GUI. The constructor takes a boolean indicating whether or not we actually
 * need to see the GUI, which is generally true. For testing, we can pass false to the constructor to 
 * use this object only for storing important fields
 * @author Michael, Jacob
 *
 */
public class Title implements ActionListener {

	private JFrame frame;
	private JScrollPane scroll;
	private JPanel contents;
	private GridBagConstraints c;
	private JButton startButton;
	private JButton browseButton;
	private JTextField text;
	private JLabel outputLabel;
	private JTextField outputText;
	private JButton outputButton;
	private JLabel label;
	private String file;
	private String outputFile;
	private JLabel locationSDK;
	private String sdkLocation;
	private JButton sdkButton;
	private JTextField sdkText;
	private DatabaseWrapper db;
	private JLabel submittedFilesLabel;
	private Dimension standardDim;
	private Dimension buttonDim;
	private Dimension textFieldDim;
	private Font ourFont;
	private JLabel screenshotLabel;
	private JTextField screenshotText;
	private JButton screenshotButton;
	private String screenshotPath;
	private JLabel statusLabel;
	private ImageIcon screenshot;
	private JLabel screen;
	private JTextField dpi;
	private JLabel dpiLabel;
	private JTextField sqlpass;
	private JLabel sqlpassLabel;
	private JLabel sqlpassSubLabel;
	
	
	private JLabel inputscreenHeightLabel;
	private JLabel inputscreenWidthLabel;
	private JTextField inputscreenWidth; 
	private JTextField inputscreenHeight;

	
	private JLabel	outputscreenHeightLabel;
	private JLabel outputscreenWidthLabel;
	private JTextField outputscreenWidth; 
	private JTextField outputscreenHeight;
	
	/**
	 * Pull up the GUI. The GUI only has one page right now and works only for one screen
	 */
	public Title(Boolean useGui){
//		db = new DatabaseWrapper();
		statusLabel = new JLabel("...");
		//for command line functionality, we don't care about the gui stuff, we just need this object to hold data
		if (useGui){
			frame = new JFrame("GUI Prototyper");
			frame.setExtendedState(JFrame.NORMAL);
			
			//		frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			initializeGUI();
		}
	}

	/**
	 * Handles most of the code for the swing/awt packages. We use a gridBagLayout to position
	 * our components. 
	 */
	private void initializeGUI(){


		text = new JTextField(20);
		text.setMinimumSize(text.getPreferredSize());
		browseButton = new JButton();
		
		
		outputText = new JTextField(20);
		outputText.setMinimumSize(outputText.getPreferredSize());
		outputLabel = new JLabel("Output folder:");
		
		
		//set screenshot placer image
		screenshot = new ImageIcon( "resources/PigCapitalist.jpg");
		Image image = screenshot.getImage();
		Image newimg = image.getScaledInstance(100, 200,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		screenshot = new ImageIcon(newimg);  // transform it back
		
		
		outputButton = new JButton();
		locationSDK = new JLabel("Android SDK location:");
		
		sdkText = new JTextField(20);
		sdkText.setMinimumSize(sdkText.getPreferredSize());
		sdkButton = new JButton();
		
		submittedFilesLabel = new JLabel("Submitted Screenshot:");
		standardDim = new Dimension(150,75);
		
		buttonDim = new Dimension(85,85);
		textFieldDim = new Dimension(100,30);
		
		ourFont = new Font("serif", Font.BOLD, 20);
		
		screenshotLabel = new JLabel("Input a screenshot:");
		screenshotText = new JTextField(20);
		screenshotText.setMinimumSize(screenshotText.getPreferredSize());
		screenshotButton = new  JButton();
		
		dpi = new JTextField(10);
		dpi.setMinimumSize(dpi.getPreferredSize());
		dpiLabel = new JLabel("Screen DPI :");
		
		sqlpass = new JTextField(10);
		sqlpass.setMinimumSize(sqlpass.getPreferredSize());
		sqlpassLabel = new JLabel("If you have a password for mySQL, input it :");
		sqlpassSubLabel = new JLabel("(Leave blank otherwise.)");
		
		
		inputscreenWidth = new JTextField(10);
		inputscreenWidth.setMinimumSize(inputscreenWidth.getPreferredSize());
		inputscreenWidthLabel = new JLabel("Input Screen Width :");
		
		inputscreenHeight = new JTextField(10);
		inputscreenHeight.setMinimumSize(inputscreenHeight.getPreferredSize());
		inputscreenHeightLabel = new JLabel("Input Screen Height :");
		
		outputscreenWidth =new JTextField(10);
		outputscreenWidth.setMinimumSize(outputscreenWidth.getPreferredSize());
		outputscreenWidthLabel = new JLabel("Output Screen Width :");
		
		outputscreenHeight = new JTextField(10);
		outputscreenHeight.setMinimumSize(outputscreenHeight.getPreferredSize());
		outputscreenHeightLabel = new JLabel("Output Screen Height Here:");
		
		
		try{
			Image img = ImageIO.read(Paths.get("resources/openFile.png").toUri().toURL());
			img = img.getScaledInstance(70, 70, Image.SCALE_DEFAULT);
			browseButton.setIcon(new ImageIcon(img));
			outputButton.setIcon(new ImageIcon(img));
			sdkButton.setIcon(new ImageIcon(img));
			screenshotButton.setIcon(new ImageIcon(img));
			Image img2 = ImageIO.read(Paths.get("resources/Android.png").toUri().toURL());
			img2 = img2.getScaledInstance(300, 200, Image.SCALE_DEFAULT);
			ImageIcon icon = new ImageIcon(img2);
			startButton = new JButton(icon);
			startButton.addActionListener(this);
			startButton.setActionCommand("startButton");

		}
		catch(IOException ex){
			//for debugging
			System.out.println("Couldn't load /resources/openFile.png: " +ex.getMessage());
		}
		screenshotButton.addActionListener(this);
		screenshotButton.setActionCommand("screenshot");
		browseButton.addActionListener(this);
		browseButton.setActionCommand("browseButton");
		outputButton.addActionListener(this);
		outputButton.setActionCommand("outputButton");
		sdkButton.addActionListener(this);
		sdkButton.setActionCommand("sdkButton");
		label = new JLabel("UX-Dump File:");

		
		//GRIDBAG LAYOU
		contents = new JPanel(new GridBagLayout());
		
		
		Border padding = BorderFactory.createEmptyBorder(1, 2, 2, 1);
		contents.setBorder(padding);
		
	
		frame.setContentPane(contents);
		
		c = new GridBagConstraints(); //since there aren't too  many components, we just use one constraints object
		c.insets = new Insets(0, 0, 0, 0);

		c.anchor = GridBagConstraints.LINE_START;
		//UX DUMP
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		label.setFont(ourFont);
		contents.add(label,c);
		
		//Submitted Screenshots
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 1; 
		submittedFilesLabel.setFont(ourFont);
		c.anchor = GridBagConstraints.CENTER;
		contents.add(submittedFilesLabel, c);
		
		//Image Sample
		c.gridx = 3;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight =1;
		
		screen = new JLabel(screenshot);
		screen.setName("image");
		contents.add(screen,c);
	
		

		//UX DUMP text and button
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		text.setPreferredSize(textFieldDim);
		contents.add(text, c);

		c.gridx = 1;
		c.gridy = 1;
		c.weightx=1;
		c.anchor = GridBagConstraints.WEST;
		browseButton.setPreferredSize(buttonDim);
		contents.add(browseButton, c);
		
		//OUTPUT FOLDER
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		outputLabel.setFont(ourFont);
		c.anchor = GridBagConstraints.CENTER;
		contents.add(outputLabel, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;
		outputText.setPreferredSize(textFieldDim);
		contents.add(outputText, c);

		c.gridx = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		outputButton.setPreferredSize(buttonDim);
		contents.add(outputButton, c);
		
		//ANDROID SDK LOCATION
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		locationSDK.setFont(ourFont);
		c.anchor = GridBagConstraints.CENTER;
		contents.add(locationSDK, c);

		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		sdkText.setPreferredSize(textFieldDim);
		
		contents.add(sdkText, c);

		c.gridx = 1;
		c.gridy = 5;
		sdkButton.setPreferredSize(buttonDim);
		c.anchor = GridBagConstraints.WEST;
		contents.add(sdkButton, c);

		
		c.gridx = 3;
		c.gridy = 6;
		contents.add(statusLabel, c);
		
		
		c.gridx = 3;
		c.gridy = 7;
		JLabel buildLabel = new JLabel("Build Project:");
		contents.add(buildLabel, c);
		//ADD A SCREENSHOT
		c.gridx = 0;
		c.gridy = 7;
		screenshotLabel.setPreferredSize(standardDim);
		screenshotLabel.setFont(ourFont);
		c.anchor = GridBagConstraints.CENTER;
		contents.add(screenshotLabel,c);
		
		c.gridx = 0;
		c.gridy = 8;
		screenshotText.setPreferredSize(textFieldDim);
		c.anchor = GridBagConstraints.CENTER;
		contents.add(screenshotText,c);
		
		c.gridx = 1;
		c.gridy = 8;
		screenshotButton.setPreferredSize(buttonDim);
		c.anchor = GridBagConstraints.WEST;
		contents.add(screenshotButton, c);
		
		//SET DPI
		c.gridx = 0;
		c.gridy = 9;
		c.anchor =  GridBagConstraints.NORTH;
		
		contents.add(dpiLabel,c);
		
		c.gridx = 0;
		c.gridy = 10;
		c.anchor = GridBagConstraints.CENTER;
		dpi.setPreferredSize(textFieldDim);
		contents.add(dpi,c);
		
		//SET SCREEN INPUT WIDTH
		c.gridx = 2;
		c.gridy = 1;
		c.anchor =  GridBagConstraints.CENTER;
		
		contents.add(inputscreenWidthLabel,c);
		
		c.gridx = 2;
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;

		contents.add(inputscreenWidth,c);
		
		//SET SCREEN OUTPUT HEIGHT
		c.gridx = 2;
		c.gridy = 3;
		c.anchor =  GridBagConstraints.CENTER;
		
		contents.add(inputscreenHeightLabel,c);
		
		c.gridx = 2;
		c.gridy = 4;
		c.anchor = GridBagConstraints.CENTER;
		contents.add(inputscreenHeight,c);
		
		
		//SET SCREEN OUPUT WIDTH
		c.gridx = 2;
		c.gridy = 5;
		c.anchor =  GridBagConstraints.CENTER;
		contents.add(outputscreenWidthLabel,c);
		
		c.gridx = 2;
		c.gridy = 6;
		c.anchor = GridBagConstraints.CENTER;
		contents.add(outputscreenWidth,c);
		
		//SET SCREEN OUTPUT HEIGHT
		c.gridx = 2;
		c.gridy = 7;
		c.anchor =  GridBagConstraints.CENTER;	
		contents.add(outputscreenHeightLabel,c);
		
		c.gridx = 2;
		c.gridy = 8;
		c.anchor = GridBagConstraints.CENTER;
		contents.add(outputscreenHeight,c);
		
		//SET MYSQL PASSWORD
				c.gridx = 2;
				c.gridy = 9;
				c.anchor = GridBagConstraints.WEST;
				contents.add(sqlpassLabel, c);
				
				c.gridx = 2;
				c.gridy = 10;
				c.anchor = GridBagConstraints.WEST;
				contents.add(sqlpassSubLabel, c);
				
				c.gridx = 2;
				c.gridy = 10;
				c.anchor = GridBagConstraints.EAST;
				contents.add(sqlpass, c);
		
		//BUILD BUTTON
		
		
		c.anchor = GridBagConstraints.SOUTH;
		c.gridx = 3;
		c.gridy = 8;
		c.weightx=1;
		c.fill = GridBagConstraints.NONE;
		startButton.setPreferredSize(new Dimension(150,75));
		c.gridwidth = 1;
		contents.add(startButton, c);
		
		//

		/*
		 * For some reason, my machine messed with the window size when
		 * resizable == true. It would start maximized, and when restored,
		 * it'd have a width/height of 0. Since we don't need to resize the
		 * window yet, this is a good... hack, I guess. --Ben
		 */
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//		frame.setResizable(false);
		//		frame.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e){
		String command = e.getActionCommand();


		if (command != "startButton"){
			JFileChooser fc = new JFileChooser();

			if (e.getActionCommand() == "browseButton"){
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.showOpenDialog(frame);
				if (fc.getSelectedFile() != null){
					file = fc.getSelectedFile().getAbsolutePath();
					text.setText(file);
				}
			}
			else if (e.getActionCommand() == "outputButton"){
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.showOpenDialog(frame);
				if (fc.getSelectedFile() != null){
					outputFile = fc.getSelectedFile().getAbsolutePath();
					outputText.setText(outputFile);
				}
			}
			else if (e.getActionCommand() == "sdkButton"){
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.showOpenDialog(frame);
				if (fc.getSelectedFile() != null){
					sdkLocation = fc.getSelectedFile().getAbsolutePath();
					sdkText.setText(sdkLocation);
				}
			}
			else if (e.getActionCommand() == "screenshot"){
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.showOpenDialog(frame);
				if (fc.getSelectedFile() != null){
					screenshotPath = fc.getSelectedFile().getAbsolutePath();
					screenshotText.setText(fc.getSelectedFile().getAbsolutePath());
					
					//display screenshot
					contents.remove(screen);;
					c.gridx = 3;
					c.gridy = 3;
					c.gridwidth = 1;
					c.gridheight =1;
					screenshot = new ImageIcon(screenshotPath);
					//bless stack overflow, this is for scaling down screenshot.png
					Image image = screenshot.getImage();
					Image newimg = image.getScaledInstance(100, 200,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
					screenshot = new ImageIcon(newimg);  // transform it back
					screen = new JLabel(screenshot);
					
					contents.add(screen,c); 
					contents.updateUI();
				}
			}
		}
		else{
			if (file != null && outputFile != null && sdkLocation != null && dpi.getText() !=null) {
				//System.out.println(text.getText());
				Constants.dpi   = Double.parseDouble(dpi.getText());
				Constants.setGUIconstants(Double.parseDouble(dpi.getText()),
						Float.parseFloat(inputscreenWidth.getText()), 
						Float.parseFloat(inputscreenHeight.getText()), 
						Float.parseFloat(outputscreenWidth.getText()),
						Float.parseFloat(outputscreenHeight.getText()));
//				db.setPassword(sqlpass.getText());
//				makeEntireProject();
			}
		}
	}

	public String getInputFile(){
		return file;
	}
	
	public void setInputFile(String str){
		file = str;
	}
	
	public String getScreenshot(){
		return screenshotPath;
	}
	
	public void setScreenshot(String str){
		screenshotPath = str;
	}

	public String getOutputFile(){
		return outputFile;
	}
	
	public void setOutputFile(String str){
		outputFile = str;
	}

	public String getSdkLocation(){
		return sdkLocation;
	}
	
	public void setSdkLocation(String str){
		sdkLocation = str;
	}

	public void makeEntireProject(boolean absolute) {
		//		file = text.getText();
		//		directory = outputText.getText();
		//		sdkText.getText();
		try {

//			db.build();

			//			System.out.print(getInputFile());
			
			ActivityHolder mainActivity = XMLParser.parseActivityFromFile(Paths.get(getInputFile()), 
					Paths.get(screenshotPath), "MainActivity");
			ProjectBuilder.generateProject("cs435.guiproto.autogen",
					Paths.get(getOutputFile()),
					Paths.get(getSdkLocation()),
					new ActivityHolder[] {mainActivity},
					absolute
					);
//			db.traverseViewTree((ViewGroup) mainActivity.getRoot());
			
			statusLabel.setText("Build's good!");
		} catch (IOException | SAXException e) {
			e.printStackTrace();
			statusLabel.setText("Build failed!");
		}

		// Generate output files
	}
	
	private void compileApk() {
		ProjectBuilder.compileProject(Paths.get(getOutputFile()));
		statusLabel.setText("build/outputs/apks/");
	}

}
