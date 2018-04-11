package turnitup.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class RegexTableModel extends AbstractTableModel{
    private final String[] columnNames = new String[]{"Enabled", "Match Type", "Regex"};
    private List<Regexer> regexerList = new ArrayList<Regexer>();
    @Override
    public int getRowCount() {
	return regexerList.size();
    }
    public RegexTableModel(List<Regexer> regexerList){
	this.regexerList = regexerList;
    }
    public Class<?> getColumnClass(int col) {
	      switch(col) {
	      case 2:
	         return String.class;
	      case 1:
	         return String.class;
	      case 0:
	         return Boolean.class;
	      default:
	         return String.class;
	      }
	   }
    
    
    public Object getValueAt(int rowIndex, int columnIndex) {
		Regexer regexer = this.regexerList.get(rowIndex);
	      switch(columnIndex) {
	      case 0:
	         return regexer.getEnabled();
	      case 1:
	         return regexer.getType();
	      case 2:
	         return regexer.getRegex();
	      default:
	         return null;
	      }
	   }
    
    @Override
    public int getColumnCount() {
	return columnNames.length;
    }
    @Override
    public String getColumnName(int column) {
	return this.columnNames[column];
    }
}
