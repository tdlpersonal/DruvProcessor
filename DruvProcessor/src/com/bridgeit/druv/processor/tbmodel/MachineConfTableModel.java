package com.bridgeit.druv.processor.tbmodel;

import java.util.ArrayList;
import java.util.Iterator;

import com.bridgeit.druv.processor.domains.MachineConfiguration;

public class MachineConfTableModel extends BaseTableModel {

	MachineConfTableModel(Object[][] data, Object[] colNames) {
		super(data, colNames);
		// TODO Auto-generated constructor stub
	}
	
	private static MachineConfTableModel instance  = null;
	
	public static MachineConfTableModel getInstance(ArrayList<MachineConfiguration> confs)
	{
		int size = confs.size();
		Object data[][] = new Object[size][5];
		Object colNames[]  = new  String[]{"Configuration Name","Rapid X","Rapid Y","Rapid Z","Spindle RPM"};
		
		for(int i=0;i<confs.size();i++)
		{
			MachineConfiguration conf  = confs.get(i);
			data[i][0]= conf.getConfName();
			data[i][1]= conf.getRapid_freq().getRapid_x();
			data[i][2]= conf.getRapid_freq().getRapid_y();
			data[i][3]= conf.getRapid_freq().getRapid_z();
			data[i][4]= conf.getSpindle_rpm();
		}
		instance = new MachineConfTableModel(data, colNames);
		return instance;
	}
	

}
