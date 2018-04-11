package turnitup.model;

import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import turnitup.utils.Setting;

public class MatcherTableModel extends AbstractTableModel{
    final String title[] = { "ID ", "Type", "HOST", "URIQ", "REQUEST METHOD", "Match" }; // 二维表列名
    private List<GathererMatcher> matcher_list=null;
    
    
    	public MatcherTableModel(List<GathererMatcher> matcher_list) {
    	    this.matcher_list=matcher_list;
	}
	
	@Override
	public int getColumnCount() {
	    return title.length;
	}

	@Override
	public int getRowCount() {
	    return this.matcher_list.size();
	}
//	@Override
//	public void fireTableDataChanged() {
//	    MatcherModel.this.setValueAt(new String("test"), matcher_list.size()-1, 0);
//	    
//	    // TODO Auto-generated method stub
//	    //super.fireTableDataChanged();
//	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
	    GathererMatcher gathererMatcher = this.matcher_list.get(rowIndex);
	    switch (columnIndex) {
	    case 0:
		return Integer.toString(gathererMatcher.getId());
	    case 1:
		return gathererMatcher.getType();
	    case 2:
		return gathererMatcher.getHost();
	    case 3:
		return gathererMatcher.geturiq();
	    case 4:
		return gathererMatcher.getMethod();
	    case 5:
		return gathererMatcher.getMatch();
	    default:
		return "";
	    }
	}
	@Override
	public String getColumnName(int columnIndex) {
	    try {
		return title[columnIndex];
	    } catch (ArrayIndexOutOfBoundsException e) {
		e.printStackTrace();
	    }
	    return null;
	   
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
	    return  String.class;
	}

}
