package pojo;

import java.util.List;

public class EnchereVendeur extends Enchere {

	private List<String> _list_preneurs;
		
	public EnchereVendeur(List<String> list_preneurs, String name, float current_price, String vendeur){
		super(name, current_price, vendeur);
		
		_list_preneurs = list_preneurs;
	}
	
	public List<String> getListPreneurs(){
		return _list_preneurs;
	}
	
	public void setListPreneurs(List<String> list_preneurs){
		_list_preneurs=list_preneurs;
	}
	
	public void addPreneur(String preneur){
		_list_preneurs.add(preneur);
	}
	
	public int getEnchereVendeurPosition(List<EnchereVendeur> list_e){
		int i = 0;
		boolean found = false;
		
		while(i<list_e.size() && !found ){
			if(list_e.get(i).compareTo(this) == 0){
				found=true;
			}else{
				i++;
			}
		}
		
		if(found){
			return i;
		}else{
			return -1;
		}	
	}
}
