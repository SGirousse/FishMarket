package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import agents.Preneur;

/**
 * Gestion de l'interface graphique de l'acheteur.
 *
 */
public class PreneurGUI extends JFrame{

	private Preneur _preneur_agent;
	private JTable _tableau;
	private EncherePreneurTable _modele;
	private JLabel _enchereName, _encherePrice;
	
	public PreneurGUI(Preneur preneur_agent){
		super(preneur_agent.getLocalName());
		
		_preneur_agent = preneur_agent;
		
		_enchereName = new JLabel("");
		_encherePrice = new JLabel("");
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2,2));
		p.add(new JLabel("Enchere en cours :"));
		p.add(_enchereName);
		p.add(new JLabel("Prix de l'enchère :"));
		p.add(_encherePrice);
		getContentPane().add(p, BorderLayout.NORTH);
		
		_modele = new EncherePreneurTable(_preneur_agent.getEncheres());
		_tableau = new JTable(_modele);
		getContentPane().add(new JScrollPane(_tableau), BorderLayout.CENTER);
		
        p = new JPanel();
        p.add(new JButton(new BidAction()));
        getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_preneur_agent.doDelete();
			}
		} );
		
		setResizable(false);
	}

	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
	
	public EncherePreneurTable getEncherePreneurTable(){
		return _modele;
	}
	
	private class BidAction extends AbstractAction {
		
		private BidAction() {
			super("Encherir");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(_tableau.getRowCount()>0){
	            int[] selection = _tableau.getSelectedRows();
	            
	            //On ne considere que la premiere enchere selectionnee (pas de multiples encheres)
	            _enchereName.setText(_modele.getEnchere(selection[0]).getName());
	            _encherePrice.setText(String.valueOf(_modele.getEnchere(selection[0]).getCurrentPrice()));
	            
	            _preneur_agent.toBid(_preneur_agent.getEncheres().get(selection[0]));
			}
		}
	}
}
