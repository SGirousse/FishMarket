package pojo;

import jade.core.AID;

import java.util.List;

public class EnchereVendeur extends Enchere {

	private List<AID> _list_preneurs;
		
	public EnchereVendeur(List<AID> list_preneurs, String name, float current_price, AID vendeur){
		super(name, current_price, vendeur);
		
		_list_preneurs = list_preneurs;
	}
	
	public List<AID> getListPreneurs(){
		return _list_preneurs;
	}
	
	public void setListPreneurs(List<AID> list_preneurs){
		_list_preneurs=list_preneurs;
	}
	
	public void addPreneur(AID preneur){
		_list_preneurs.add(preneur);
	}
}
