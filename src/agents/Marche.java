package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import pojo.Enchere;
import pojo.MessageType;


public class Marche extends Agent{
	
	private List<Enchere> _list_offres;
	private List<AID> _list_preneurs_abonnes;
	
	@Override
	protected void setup(){
		System.out.println("Marche ** TRACE ** "+getAID().getName()+" : Ouverture du marche");
		
		_list_offres = new ArrayList<Enchere>();
		_list_preneurs_abonnes = new ArrayList<AID>();
		
		addBehaviour(new MarketBehaviour());
	}
	
	@Override
	protected void takeDown(){
		_list_offres = null;
		System.out.println("Marche ** TRACE ** "+getAID().getName()+" : Cloture du marche");
	}
	
	public int getEncherePosition(Enchere e){
		int i = -1;
		boolean found = false;
		while(i<_list_offres.size() && !found ){
			i++;
			found = _list_offres.get(i).compareTo(e) == 0;
		}
		
		if(found){
			return i;
		}else{
			return -1;
		}	
	}
	
	private class MarketBehaviour extends CyclicBehaviour {
		
		@Override
		public void action() {
			int type_message = 0;
			String contMsg; 
			Enchere e = new Enchere();
			AID preneur;
			int pos,p;
			boolean pFound;
			
			ACLMessage msg = receive();
			
			if (msg != null) {
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
					case MessageType.TO_ANNOUNCE:
						System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce recue");
						
						String sEnchereAdd[] = contMsg.split("|");
						//e.fromMessageString(sEnchereAdd[1]);
						//pos = getEncherePosition(e);
						
						/*if(pos==-1){
							_list_offres.add(e);
						}*/

						ACLMessage nouvelleEnchere = new ACLMessage(ACLMessage.INFORM);
						
						for(int i=0; i<_list_preneurs_abonnes.size(); i++){
							nouvelleEnchere.addReceiver(_list_preneurs_abonnes.get(i));
						}
						nouvelleEnchere.setContent(contMsg);
				  		send(nouvelleEnchere);
						
						break;
						
					case MessageType.TO_WITHDRAW: 
						System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce retiree");
						
						String sEnchereSuppr[] = contMsg.substring(1).split(";");
						e = new Enchere(sEnchereSuppr[1], Float.valueOf(sEnchereSuppr[2]), new AID(sEnchereSuppr[0], true));
						pos = getEncherePosition(e);
						
						if(pos!=-1){
							ACLMessage suppressionEnchere = new ACLMessage(ACLMessage.INFORM);
							
							for(int i=0; i<_list_preneurs_abonnes.size(); i++){
								suppressionEnchere.addReceiver(_list_preneurs_abonnes.get(i));
							}
							suppressionEnchere.setContent(contMsg);
					  		send(suppressionEnchere);
					  		
					  		_list_offres.remove(pos);
						}

						
						break;
						
					case MessageType.TO_SUBSCRIBE: 
						System.out.println("Marche ** TRACE ** "+getAID().getName()+" : abonnement");
						
						preneur = msg.getSender();
						p=0;
						pFound=false;
						while(p<_list_preneurs_abonnes.size() && !pFound){
							if(_list_preneurs_abonnes.get(p).compareTo(preneur)==0){
								pFound=true;
								System.out.println("Marche ** ERROR ** "+getAID().getName()+" : le PRENEUR "+preneur+" est deja abonne !");
							}
						}
						
						if(!pFound){
							_list_preneurs_abonnes.add(preneur);
							System.out.println("Marche ** TRACE ** "+getAID().getName()+" : le PRENEUR "+preneur+" vient de s'abonner.");
						}
						
						break;
						
					case MessageType.TO_UNSUBSCRIBE:
						System.out.println("Marche ** TRACE ** "+getAID().getName()+" : desabonnement");
						
						preneur = msg.getSender();
						p=0;
						pFound=false;
						while(p<_list_preneurs_abonnes.size() && !pFound){
							if(_list_preneurs_abonnes.get(p).compareTo(preneur)==0){
								_list_preneurs_abonnes.remove(p);
								pFound=true;
								System.out.println("Marche ** TRACE ** "+getAID().getName()+" : le PRENEUR "+preneur+" vient d'etre desabonne.");
							}
						}
						
						if(!pFound){
							System.out.println("Marche ** ERROR ** "+getAID().getName()+" : le PRENEUR "+preneur+" n'a pas pu etre desabonne : inconnu dans la liste.");
						}
						
						break;
						
					default : 
						System.out.println("Marche ** ERROR ** "+getAID().getName()+" : type de message non connu.");
				}
				
				msg = null;
			}
		}
	}
}