package pojo;

import jade.core.AID;

import java.util.ArrayList;
import java.util.List;

import listeners.PreneurEnchereListener;

public class EnchereMarche extends Enchere{
	private List<PreneurEnchereListener> _abonnes;

	public EnchereMarche(String name, Float init_price, AID vendeur) {
		super(name, init_price, vendeur);
		_abonnes = new ArrayList<PreneurEnchereListener>();
	}

	public void addAbonne(PreneurEnchereListener abonne){
		_abonnes.add(abonne);
	}
	
	public void delAbonneById(int i){
		_abonnes.remove(i);
	}
}
