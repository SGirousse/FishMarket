package pojo;

import jade.core.AID;

import java.util.List;

public class Enchere implements Comparable<Enchere>{
	private String _name;
	private float _init_price;
	private float _current_price;
	private AID _vendeur;
	private int _bidCount;
	
	public Enchere(){
	}
	
	public Enchere(String name, float init_price, AID vendeur){
		_name = name;
		_init_price = init_price;
		_current_price = init_price;
		_vendeur = vendeur;
		_bidCount = -1;
	}
	
	public void setCurrentPrice(float price){
		_current_price = price;
	}
	
	public float getCurrentPrice(){
		return _current_price;
	}

	public float getInitPrice(){
		return _init_price;
	}
	
	public void setName(String name){
		_name = name;
	}
	
	public String getName(){
		return _name;
	}
	
	public AID getVendeur(){
		return _vendeur;
	}
	
	public int getBidCount(){
		return _bidCount;
	}
	
	public void setBidCount(int bidCount){
		_bidCount=bidCount;
	}

	@Override
	public int compareTo(Enchere o) {		
		if(_name.compareTo(o.getName())==0 && _vendeur.compareTo(o.getVendeur())==0){
			return 1;
		}else{
			return -1;
		}
	}
	
	public String toMessageString() {
		return ";"+_name+";"+_init_price+";"+_current_price+";"+_vendeur;
	}
	
	public void fromMessageString(String msg) {
		System.out.println("Enchere ** TRACE ** public void fromMessageString(String msg) : "+msg);
		String[] sEnchere = msg.split(";");
		
		_name = sEnchere[1];
		_init_price = Float.valueOf(sEnchere[2]);
		_current_price = Float.valueOf(sEnchere[3]);
		_vendeur = new AID(sEnchere[4]);
	}
	
	public int getEncherePosition(List<Enchere> list_e){
		System.out.println("Enchere ** DEBUG ** : public int getEncherePosition(Enchere e)");
		int i = 0;
		boolean found = false;
		
		while(i<list_e.size() && !found ){
			found = list_e.get(i).compareTo(this) == 0;
			i++;
		}
		
		if(found){
			return i;
		}else{
			return -1;
		}	
	}
	
}
