package pojo;

import jade.core.AID;

public class Enchere {
	private String _name;
	private float _init_price;
	private float _current_price;
	private AID _vendeur;
	private int _bidCount;
	
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
	
	public AID getAID(){
		return _vendeur;
	}
	
	public int getBidCount(){
		return _bidCount;
	}
	
	public void setBidCount(int bidCount){
		_bidCount=bidCount;
	}
}
