package com.bridgeit.druv.processor.view;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Menu;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.bridgeit.druv.processor.main.DruvProcessor;

public class MainFrame extends  JFrame implements ActionListener, MenuListener{
	
	private JMenuBar menuBar = new JMenuBar();
	private JPanel content = null;
	private HomePanel homePanel= null;
	private RunPanel runPanel= null;
	private MachineConfPanel machinePanel = null;
	JMenu menuHome = new JMenu("Home");
	JMenu menuRun = new JMenu("Run Utility");
	JMenu menuConfiguration = new JMenu("Configuration");
	
	private MainFrame() throws  Exception{
		// TODO Auto-generated constructor stub
		setSize(1200,800);
		setLayout(null);
		setTitle("Druv Processor Utility");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    final JLabel status = new JLabel("Version :  2.0.1, 26-Apr-2019");
	    statusBar.add(status);
	    statusBar.setBounds(0,700, 1200, 50);
	    add(statusBar);
	    
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon("images/logo.png");
		setIconImage(icon.getImage());
		menuHome.addMenuListener(this);
		
		menuRun.addMenuListener(this);
	
		JMenuItem itemMachineConf = new JMenuItem("Machine Configuration");
		itemMachineConf.setActionCommand("MACHINE_CONF");
		itemMachineConf.addActionListener(this);
		
		menuConfiguration.add(itemMachineConf);
		
		menuBar.add(menuHome);
		menuBar.add(menuConfiguration);
		menuBar.add(menuRun);
		
		setJMenuBar(menuBar);
		/*runPanel = new  RunPanel();
		setPanel(runPanel);
		*/
		
		machinePanel = new  MachineConfPanel(this);
		setPanel(machinePanel);
		setVisible(true);
		
	}
	
	public void setPanel(JPanel panel)
	{
		if(content!=null)
			remove(content);
		content = panel;
		content.setBounds(20,20, 1160, 750);
		add(content);
		repaint();
		
	}
	
	public static void main(String[] args) throws Exception{
		new MainFrame();
	}
	@Override
	public void actionPerformed(ActionEvent ae){
		// TODO Auto-generated method stub
		System.out.println(ae.getActionCommand());
		if(ae.getActionCommand().equals("MACHINE_CONF"))
		{
			System.out.println("Machine Conf Clicked");
			try{
			if(machinePanel==null)
				machinePanel = new MachineConfPanel(this);
			setPanel(machinePanel);
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				DruvProcessor.logExit("Error while loading Machine Conf: ") ;
			}
		}
		
	}

	@Override
	public void menuCanceled(MenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void menuDeselected(MenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void menuSelected(MenuEvent me) {
		// TODO Auto-generated method stub
		//System.out.println(me.getSource());
		if(me.getSource() == menuHome)
		{
			if(homePanel==null)
				homePanel = new HomePanel();
			setPanel(homePanel);
		}
		else if(me.getSource() == menuRun)
		{
			if(runPanel==null)
				runPanel = new RunPanel();
			setPanel(runPanel);
		}
		else
		{
			// do nothing
		}
	}

}
