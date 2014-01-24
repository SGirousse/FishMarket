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
	
	private class MarketBehaviour extends CyclicBehaviour {
		
		@Override
		public void action() {
			int type_message = 0,p;
			String contMsg; 
			Enchere e = new Enchere();
			AID preneur;
			boolean pFound;
			ACLMessage nouvelleEnchere;
			
			ACLMessage msg = receive();
			
			if (msg != null) {
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
					case MessageType.TO_ANNOUNCE:
						System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce recue = "+contMsg);

						e.fromMessageString(contMsg);
						
						if(e.getEncherePosition(_list_offres)==-1){
							System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce sur le point d'etre ajoutee");
							_list_offres.add(e);
						}

						nouvelleEnchere = new ACLMessage(ACLMessage.INFORM);
						
						for(int i=0; i<_list_preneurs_abonnes.size(); i++){
							nouvelleEnchere.addReceiver(_list_preneurs_abonnes.get(i));
						}
						nouvelleEnchere.setContent(contMsg);
				  		send(nouvelleEnchere);
						
						break;
						
					case MessageType.TO_WITHDRAW: 
						System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce retiree");
						
						e.fromMessageString(contMsg);
						contMsg = String.valueOf(MessageType.TO_WITHDRAW)+e.toMessageString();

						if(e.getEncherePosition(_list_offres)!=-1){
							ACLMessage suppressionEnchere = new ACLMessage(ACLMessage.INFORM);
							
							for(int i=0; i<_list_preneurs_abonnes.size(); i++){
								suppressionEnchere.addReceiver(_list_preneurs_abonnes.get(i));
							}
							suppressionEnchere.setContent(contMsg);
					  		send(suppressionEnchere);
					  		
					  		_list_offres.remove(e.getEncherePosition(_list_offres));
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
							}else{
								p++;
							}
						}
						
						if(!pFound){
							_list_preneurs_abonnes.add(preneur);
							System.out.println("Marche ** TRACE ** "+getAID().getName()+" : le PRENEUR "+preneur+" vient de s'abonner.");

							nouvelleEnchere = new ACLMessage(ACLMessage.INFORM);
							nouvelleEnchere.addReceiver(preneur);
							String offre;
							for(int i=0;i<_list_offres.size();i++){
								offre = _list_offres.get(i).toMessageString();
								nouvelleEnchere.setContent("1"+offre);
						  		send(nouvelleEnchere);
							}
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
							}else{
								p++;
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