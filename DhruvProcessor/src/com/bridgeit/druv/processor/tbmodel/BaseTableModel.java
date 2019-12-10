package com.bridgeit.druv.processor.tbmodel;

import javax.swing.table.DefaultTableModel;

public class BaseTableModel extends DefaultTableModel{
	
	Object[][] data = null;
	Object[] colNames  = null;
	
	BaseTableModel(Object[][] data,Object[] colNames)
	{
		super(data,colNames);
		this.data=data;
		this.colNames=colNames;
	}
	
	
}
