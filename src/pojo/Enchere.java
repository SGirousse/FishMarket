package pojo;

import jade.core.AID;

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
		return "|"+_name+";"+_init_price+";"+_current_price+";"+_vendeur+"|";
	}
	
	public void fromMessageString(String msg) {
		String sEnchere[] = msg.split(";");
		
		_name = sEnchere[0];
		_init_price = Float.valueOf(sEnchere[1]);
		_current_price = Float.valueOf(sEnchere[2]);
		_vendeur = new AID(sEnchere[3]);
	}
}
