package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import pojo.EnchereMarche;


public class Marche extends Agent{
	
	private List<EnchereMarche> _list_offres;
	
	@Override
	protected void setup(){
		System.out.println("Marche ** TRACE ** "+getAID().getName()+" : Ouverture du marche");
		
		_list_offres = new ArrayList<EnchereMarche>();
		
		addBehaviour(new MarketBehaviour());
	}
	
	@Override
	protected void takeDown(){
		_list_offres = null;
		System.out.println("Marche ** TRACE ** "+getAID().getName()+" : Cloture du marche");
	}
	
	private class MarketBehaviour extends CyclicBehaviour {
		
		private static final int TO_ANNOUNCE = 1; 		//le vendeur fait un annonce d'une enchere existante ou non
		private static final int TO_WITHDRAW = 2;		//le vendeur retire une enchere (attribuee a un preneur)
		private static final int TO_SUBSCRIBE = 3;		//un preneur s'inscris aupres du marche
		private static final int TO_SUBSCRIBE_BID = 4;	//un preneur s'inscris pour une enchere particuliere
		
		@Override
		public void action() {
			int type_message = 0;
			String contMsg; 
			
			ACLMessage msg = receive();
			if (msg != null) {
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 0));
				
				switch(type_message){
					case TO_ANNOUNCE: System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce recue");
						break;
					case TO_WITHDRAW: System.out.println("Marche ** TRACE ** "+getAID().getName()+" : annonce retiree");
						break;
					case TO_SUBSCRIBE: System.out.println("Marche ** TRACE ** "+getAID().getName()+" : abonnement");
						break;
					case TO_SUBSCRIBE_BID: System.out.println("Marche ** TRACE ** "+getAID().getName()+" : abonnement a une annonce");
						break;
					default : System.out.println("Marche ** ERROR ** "+getAID().getName()+" : type de message non connu.");
				}
			}
		}
	}
}
