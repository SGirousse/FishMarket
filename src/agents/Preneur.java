package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.List;

import pojo.Enchere;

public class Preneur extends Agent {

	private Float _money;
	private List<Enchere> _encheres;
	private AID _marche;
	
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Arrivee de l'acheteur");
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);
	  		
	  		ACLMessage subscribeMsg = new ACLMessage(ACLMessage.CFP);
	  		subscribeMsg.addReceiver(_marche);
	  		subscribeMsg.setContent("3"+getAID());
	  		subscribeMsg.setConversationId("abonnement-marche");
	  		send(subscribeMsg);
	  		
	  		addBehaviour(new AbonnementMarcheBehaviour());
	  		
	  	}else{
	  		System.out.println("Preneur ** ERROR ** "+getAID().getName()+" : Missing Marche Agent Name");
	  	}
	}
	
	protected void takeDown(){
		_encheres = null;
		_marche = null;
		_money = null;
		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Depart de l'acheteur");
	}
	
	private class AbonnementMarcheBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
	  		// Abonnement aupres du marche
			
			// Demande des offres
			
			// Attente de la reponse
			
			// Abonnement aux offres plaisantes
			
		}
	}
}
