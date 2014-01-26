package pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * EnchereVendeur permet de manipuler des enchere de type Enchere en ajoutant 
 * une surcouche propre au vendeur et facilitant leur manipulation : y sont
 * conserves des informations sur les bids (temps attente, Preneur interesse) et
 * l'etape de manipulation ( Step ) courante de l'enchere. 
 * 
 * @author simeon
 *
 */
public class EnchereVendeur extends Enchere {

	private List<String> _list_preneurs;
	private int _step;
	private int _temps_attente;
		
	public EnchereVendeur(){
		super();
		
		_list_preneurs=new ArrayList<String>();
		_step=Step.to_announce;
		_temps_attente=0;
	}
	
	public EnchereVendeur(List<String> list_preneurs, String name, float current_price, String vendeur){
		super(name, current_price, vendeur);
		
		_list_preneurs=list_preneurs;
		_step=Step.to_announce;
		_temps_attente=0;
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
	
	public void setStep(int step){
		_step=step;
	}
	
	public int getStep(){
		return _step;
	}
	
	public void setTempsAttente(int temps_attente){
		_temps_attente=temps_attente;
	}
	
	public int getTempsAttente(){
		return _temps_attente;
	}
}
