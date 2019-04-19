package com.bridgeit.druv.processor.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.bridgeit.druv.processor.domains.FeedRates;
import com.bridgeit.druv.processor.domains.FreqMinValue;
import com.bridgeit.druv.processor.domains.MachineConfiguration;
import com.bridgeit.druv.processor.domains.Rapid_Frequency;
import com.bridgeit.druv.processor.main.DruvProcessor;
import com.bridgeit.druv.processor.tbmodel.FeedRatesTableModel;
import com.bridgeit.druv.processor.tbmodel.MVTableModel;
import com.bridgeit.druv.processor.tbmodel.MachineConfTableModel;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class MachineConfPanel  extends ContentPanel implements MouseListener, ActionListener {
	
	JScrollPane scrollPane = null;
	JTable tblMachineConfs = null;
	MachineConfTableModel confTableModel = null;
	JScrollPane scrollPaneRates = null;
	JTable tblRates = null;
	
	JLabel lblRates = new JLabel("Feed Rates for : ");
	
	JButton btnAdd = new JButton("Add New");
	JButton btnClear = new JButton("Clear");
	JButton btnRun = new JButton("Run Job");
	
	MachineConfiguration newMC = null;
	ArrayList<MachineConfiguration> confs = new ArrayList<MachineConfiguration>();
	
	NewMachineConfPanel newPanel = null;
	
	
	class NewMachineConfPanel extends JPanel 
	{
		MachineConfPanel parent;
		MVPanel mvPanel;
		private JLabel lblName = new JLabel("Configuration Name:");
		private JTextField txtName = new JTextField();
		
		private JLabel lblRapidX= new JLabel("Rapid X:");
		private JTextField txtRapidX = new JTextField();
		
		private JLabel lblRapidY= new JLabel("Rapid Y:");
		private JTextField txtRapidY = new JTextField();
		
		private JLabel lblRapidZ= new JLabel("Rapid Z:");
		private JTextField txtRapidZ = new JTextField();
		
		private JLabel lblSpindleRPM= new JLabel("Spindle RPM:");
		private JTextField txtSpindleRPM = new JTextField();
		
		private JLabel lblFeedRates= new JLabel("FeedRates:");
		
		private JButton btnAddRate = new JButton("Add New FeedRate");
		private JButton btnSave = new JButton("Save Feed Rates");
		
		JTable tblNewFeedRates;
		int x = 0, y=0, height=30,gap=20, lblWidth=120, bigTextWidth=200,smallTextWidth=50;
		FeedRatesTableModel newFeedModel = null;
		
		NewMachineConfPanel(MachineConfPanel parent)
		{
			this.parent=parent;
			setLayout(null);
			JScrollPane feedPane;
			newFeedModel = FeedRatesTableModel.getInstance(new FeedRates[]{new FeedRates()});
			tblNewFeedRates = new JTable(newFeedModel);
			
			feedPane = new JScrollPane(tblNewFeedRates);
			setBounds(lblName,lblWidth,false);
			setBounds(txtName,bigTextWidth,false);
			setBounds(lblFeedRates,lblWidth,false);
			feedPane.setBounds(x-50, y, 650, 120);
			btnAddRate.setBounds(x-50, y+130, 150, 30);
			btnSave.setBounds(x+120, y+130, 150, 30);
			btnAddRate.setActionCommand("ADDRATE");
			btnSave.setActionCommand("SAVEFEEDRATES");
			btnAddRate.addActionListener(parent);
			btnSave.addActionListener(parent);
			add(btnAddRate);
			add(btnSave);
			
			x=0;
			y = y+height;
			
			add(feedPane);
			setBounds(lblRapidX,lblWidth,false);
			setBounds(txtRapidX,smallTextWidth,true);
			setBounds(lblRapidY,lblWidth,false);
			setBounds(txtRapidY,smallTextWidth,true);
			setBounds(lblRapidZ,lblWidth,false);
			setBounds(txtRapidZ,smallTextWidth,true);
			setBounds(lblSpindleRPM,lblWidth,false);
			setBounds(txtSpindleRPM,smallTextWidth,true);
			
			
			
		}

		private void setBounds(Component c, int width, boolean newLine) {
			// TODO Auto-generated method stub
			
			c.setBounds(x, y, width, height);
			x = x+width+gap;
			if(newLine)
			{
				y = y + height;
				x  = 0;
			}
			add(c);
			
		}

		public void addRate() {
			// TODO Auto-generated method stub
			newFeedModel.addRow(new  Object[]{0,0.0,0,0.0,0,0.0,0});
			
			
		}

		public void save_feedrates() {
			// TODO Auto-generated method stub
			if(!newFeedModel.validateData())
			{
				JOptionPane.showMessageDialog(this, "Please Enter Correct Feed Rates");
				return;
			}
			if(txtName.getText()==null || txtName.getText().trim().equals("") || txtName.getText().trim().equals(" "))
			{
				JOptionPane.showMessageDialog(this, "Please Provide Correct Conf Name");
				return;
			}
			if(txtName.getText().contains(" "))
			{
				JOptionPane.showMessageDialog(this, "No Spaces in conf name please...");
				return;
			}
			
			double rapid_x=0,rapid_y=0,rapid_z=0;
			int spindleRPM;
			try
			{
				rapid_x = Double.parseDouble(txtRapidX.getText().toString());
				rapid_y = Double.parseDouble(txtRapidY.getText().toString());
				rapid_z = Double.parseDouble(txtRapidZ.getText().toString());
				spindleRPM = Integer.parseInt(txtSpindleRPM.getText().toString());
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(this, "Please Provide Correct Rapid /  Spindle RPM Values");
				return;
			}
			
			newMC = new MachineConfiguration(); 
			newMC.setFeed_rates(newFeedModel.getRates());
			newMC.setConfName(txtName.getText());
			newMC.setCreated_time(Calendar.getInstance().getTimeInMillis());
			newMC.setLast_updated_time(newMC.getCreated_time());
			Rapid_Frequency freq = new Rapid_Frequency();
			freq.setRapid_x(rapid_x);
			freq.setRapid_y(rapid_y);
			freq.setRapid_z(rapid_z);
			newMC.setRapid_freq(freq);
			newMC.setSpindle_rpm(spindleRPM);
			getMVs();
			
			
		}
		
		private void getMVs()
		{
			ArrayList<Double> listFreq = new ArrayList<Double>();
			
			listFreq.add(newMC.getRapid_freq().getRapid_x());
			addFreq(listFreq,newMC.getRapid_freq().getRapid_y());
			addFreq(listFreq,newMC.getRapid_freq().getRapid_z());
			for (int i=0;i<tblNewFeedRates.getRowCount();i++)
			{
				addFreq(listFreq,(Double)newFeedModel.getValueAt(i, 1));
				addFreq(listFreq,(Double)newFeedModel.getValueAt(i, 3));
				addFreq(listFreq,(Double)newFeedModel.getValueAt(i, 5));
			}
			
			mvPanel = new MVPanel(parent.frame, "Please Enter MV Values", true,this,listFreq);
			mvPanel.setVisible(true);
			
			System.out.println("Here");
			
			/*JDialog dialog = new JDialog(mvPanel,"Please Enter MV Values", Dialog.ModalityType.APPLICATION_MODAL);
			dialog.setSize(900, 700);
			dialog.setVisible(true);
			 */
			
		}
		
		public void proceed() {
			// TODO Auto-generated method stub
			newMC.setFreq_mvs(mvPanel.getMVValues());
			saveConf();
			
		}
		
		private void saveConf()
		{
			File f = new File("conf\\machines\\"+newMC.getConfName());
			if(f.exists())
			{
				JOptionPane.showMessageDialog(this, "Configurtion with given name exists, please change name");
				return;
			}
					
			BufferedWriter writer;
			try {
				String str = new Gson().toJson(newMC);
				System.out.println(str);
				writer = new BufferedWriter(new FileWriter(f));
				writer.write(str);
				writer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error while saving Machine conf: " + e.getMessage());
				return;
			}
			
			try {
				parent.frame.setPanel(new MachineConfPanel(parent.frame));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		class MVPanel extends JDialog implements ActionListener
		{
			
			JScrollPane pane;
			JTable tbl;
			MVTableModel model;
			JButton btnContinue;
			NewMachineConfPanel panel;
			
			FreqMinValue[] mvs;
			public FreqMinValue[] getData()
			{
				return mvs;
			}
			
			
			
			MVPanel(MainFrame frame, String title, boolean modal, NewMachineConfPanel panel, ArrayList<Double> listFreqs)
			{
				super(frame,title,modal);
				this.panel = panel;
				getContentPane().setLayout(null);
				FreqMinValue[] mvs = new FreqMinValue[listFreqs.size()];
				setSize(800,600);
				model = MVTableModel.getInstance(listFreqs);
				tbl = new JTable(model);
				pane = new JScrollPane(tbl);
				pane.setBounds(10,10,780,400);
				getContentPane().add(pane);
				btnContinue = new JButton("Continue");
				btnContinue.setActionCommand("CONTINUE");
				btnContinue.addActionListener(this);
				btnContinue.setBounds(200,450,200,30);
				getContentPane().add(btnContinue);
				
				
			}
			
			public FreqMinValue[]  getMVValues()
			{
				return model.getMVs();
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				setVisible(false);
				panel.proceed();
				
				
			}
			
		}

		
		
		
	}
	
	
	MainFrame frame = null;
	
	MachineConfPanel(MainFrame mainFrame) throws FileNotFoundException
	{
		
		super("Machine Configuration");
		this.frame = mainFrame;
		File f = new File("conf\\machines");
		if(!f.exists())
			try {
				f.mkdir();
				f = new File("conf\\machines");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		File[] list = f.listFiles();
	
		for(int i=0;i<list.length;i++)
		{
			File confFile = list[i];
			if(!confFile.isDirectory())
			{
				System.out.println(confFile.getName());
				JsonReader reader = new JsonReader(new FileReader(confFile));
				confs.add((MachineConfiguration) new Gson().fromJson(reader, MachineConfiguration.class));
				
			}
		}
		confTableModel= MachineConfTableModel.getInstance(confs);
		tblMachineConfs = new JTable(confTableModel);
		scrollPane = new JScrollPane(tblMachineConfs);
		scrollPane.setBounds(20,50,600,200);
		add(scrollPane);
		tblMachineConfs.addMouseListener(this);
		btnAdd.setActionCommand("ADD");
		btnAdd.addActionListener(this);
		btnAdd.setBounds(20,280,100,30);
		add(btnAdd);
		btnClear.setActionCommand("CLEAR");
		btnClear.addActionListener(this);
		btnClear.setBounds(150,280,100,30);
		add(btnClear);
		btnRun.setActionCommand("RUN");
		btnRun.addActionListener(this);
		btnRun.setBounds(270,280,100,30);
		btnRun.setEnabled(false);
		add(btnRun);
		
		
	}

	public void addFreq(ArrayList<Double> listFreq, double value) {
		// TODO Auto-generated method stub
		if(!listFreq.contains(value))
			listFreq.add(value);
		
	}

	private void showFeedRatesTable(MachineConfiguration machineConfiguration) {
		// TODO Auto-generated method stub
		remove(lblRates);
		lblRates = new JLabel("Feed Rates for : " + machineConfiguration.getConfName());
		if(scrollPaneRates!=null)
			remove(scrollPaneRates);
		
		lblRates.setBounds(650,10,300,30);
		
		add(lblRates);
		tblRates = new JTable(FeedRatesTableModel.getInstance(machineConfiguration.getFeed_rates()));
		scrollPaneRates = new JScrollPane(tblRates);
		scrollPaneRates.setBounds(650,50,450,200);
		add(scrollPaneRates);
		repaint();
		
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		// TODO Auto-generated method stub
		JTable tbl = (JTable)me.getSource();
		if(tbl.getSelectedRow()<-1)
			return;
		showFeedRatesTable(confs.get(tbl.getSelectedRow()));
		btnRun.setEnabled(true);
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void clear()
	{

		if(newPanel!=null)
			remove(newPanel);
		btnAdd.setEnabled(true);
		repaint();
	}
	
	private void add()
	{
		if(newPanel!=null)
			remove(newPanel);
		newPanel = new NewMachineConfPanel(this);
		newPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		newPanel.setBounds(20,320,1100,350);
		add(newPanel);
		btnAdd.setEnabled(false);
		newMC = new MachineConfiguration();
		
		repaint();
		
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// TODO Auto-generated method stub
		if(ae.getActionCommand().equals("ADD"))
		{
			add();
		}
		else if(ae.getActionCommand().equals("CLEAR"))
		{
			clear();
		}
		else if(ae.getActionCommand().equals("RUN"))
		{
			try{
			run_job();
			}catch(Exception e)
			{
				e.printStackTrace();
				DruvProcessor.logExit(e.getMessage());
			}
		}
		
		else if(ae.getActionCommand().equals("ADDRATE"))
		{
			newPanel.addRate();
			
		}	
		else if(ae.getActionCommand().equals("SAVEFEEDRATES"))
		{
			newPanel.save_feedrates();
		}
	}

	private void run_job() {
		// TODO Auto-generated method stub
		int selectedRow = tblMachineConfs.getSelectedRow();
		MachineConfiguration conf = confs.get(selectedRow);
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);
		File file = fc.getSelectedFile();
		String dest = "D:\\Tejas\\imp\\Anand";
		//String dest = "F:\\";
		
		try
		{
					
			new DruvProcessor().runJob(conf,file.getPath(), dest);
			
			JOptionPane.showMessageDialog(this, "Code folder generated inside : " + dest);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error while running job:" + e.getMessage());
		}
		
	}

}
