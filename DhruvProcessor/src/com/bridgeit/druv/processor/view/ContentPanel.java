package com.bridgeit.druv.processor.view;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {
	private JLabel lblPanelTitle = null;
	ContentPanel(String panelTitle)
	{
		lblPanelTitle = new JLabel(panelTitle);
		setLayout(null);
		lblPanelTitle.setBounds(20,10,500,30);
		add(lblPanelTitle);
	}

}
