package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import pojo.EnchereVendeur;
import agents.Vendeur;

/**
 * Gestion de l'interface graphique du vendeur.
 *
 */
public class VendeurGUI extends JFrame {

	private Vendeur _vendeur_agent;
	private JTextField  _titlefield, _pricefield;
	private JTable _tableau;
	private EnchereVendeurTable _modele;
	
	public VendeurGUI(Vendeur vendeur_agent){
		super(vendeur_agent.getLocalName());
		_vendeur_agent=vendeur_agent;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Article vendu:"));
		_titlefield = new JTextField(15);
		p.add(_titlefield);
		p.add(new JLabel("Prix:"));
		_pricefield = new JTextField(15);
		p.add(_pricefield);
		getContentPane().add(p, BorderLayout.NORTH);
		
		JButton addButton = new JButton("Ajouter");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = _titlefield.getText().trim();
					String price = _pricefield.getText().trim();
					_vendeur_agent.newOffer(title, Float.valueOf(price));
					_titlefield.setText("");
					_pricefield.setText("");
					_modele.addEnchere(new EnchereVendeur(new ArrayList<String>(), title, Float.valueOf(price), _vendeur_agent.getAID().getName()));
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(VendeurGUI.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.CENTER);
		
		_modele = new EnchereVendeurTable(_vendeur_agent.getEncheres());
		_tableau = new JTable(_modele);
		getContentPane().add(new JScrollPane(_tableau), BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_vendeur_agent.doDelete();
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
	
	public EnchereVendeurTable getEnchereVendeurTable(){
		return _modele;
	}
}
