package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import pojo.Enchere;
import pojo.EnchereVendeur;

/**
 * Tableau des enchères de l'acheteur.
 * 
 * @see Preneur PreneurGUI
 *
 */
public class EncherePreneurTable extends AbstractTableModel {

	private final List<Enchere> _encheres;
	private final String[] entetes = {"Article", "Prix courant", "Prix initial", "Vendeur"};
	
	public EncherePreneurTable(List<Enchere> encheres){
		super();
		_encheres=encheres;
	}
	
	public int getColumnCount() {
		return entetes.length;
	}

	public int getRowCount() {
		return _encheres.size();
	}

    public String getColumnName(int columnIndex) {
        return entetes[columnIndex];
    }
	
	public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex){
        case 0:
            return _encheres.get(rowIndex).getName();
        case 1:
            return _encheres.get(rowIndex).getInitPrice();
        case 2:
            return _encheres.get(rowIndex).getCurrentPrice();
        case 3:
        	return _encheres.get(rowIndex).getVendeur();
        default:
            return null;
        }
	}
	
    public void addEnchere(EnchereVendeur enchere) {
        fireTableRowsInserted(_encheres.size()-1, _encheres.size()-1);
    }
 
    public void removeEnchere(int rowIndex) {
    	_encheres.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}
