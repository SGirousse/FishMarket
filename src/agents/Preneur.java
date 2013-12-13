package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.List;

import listeners.PreneurEnchereAdapter;
import listeners.PreneurEnchereListener;

import pojo.Enchere;

public class Preneur extends Agent {

	private Float _money;
	private List<Enchere> _encheres;
	private AID _marche;
	private PreneurEnchereListener _listener;
	
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Arrivee de l'acheteur");
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);
	  		
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
	  		_listener = new PreneurEnchereAdapter() {
	  			
			};
			// Demande des offres
			
			// Attente de la reponse
			
			// Abonnement aux offres plaisantes
			
		}
	}
}
