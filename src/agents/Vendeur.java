package agents;

import gui.VendeurGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pojo.Enchere;
import pojo.EnchereVendeur;

public class Vendeur extends Agent {

	private List<EnchereVendeur> _encheres;
	private AID _marche;
	private VendeurGUI _vendeurGUI;
		
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : Arrivee du vendeur");
	  		
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);
	  		
	  		_encheres = new ArrayList<EnchereVendeur>();
	  		_encheres.add(new EnchereVendeur(new ArrayList<AID>(), "GoldenFish", 42.42f, getAID()));
	  		
	  		_vendeurGUI = new VendeurGUI(this);
	  		_vendeurGUI.showGui();
				
			addBehaviour(new VendeurBehaviour(_encheres.get(0)));
	  	}else{
	  		System.out.println("Vendeur ** ERROR ** "+getAID().getName()+" : nom du marche manquant.");
	  		doDelete();
	  	}
	}
	
	protected void takeDown(){
		System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : Depart du vendeur");
	}
	
	public void newOffer(String title, float price){
		_encheres.add(new EnchereVendeur(null, title, price, getAID()));
	}
	
	public List<EnchereVendeur> getEncheres(){
		return _encheres;
	}
	
	private class VendeurBehaviour extends FSMBehaviour{
		private Enchere _e;
		
		private static final String STATE_TOANNOUNCE = "STATE_TOANNOUNCE";
		private static final String STATE_TOWAITFORBID = "STATE_TOWAITFORBID";
		private static final String STATE_TOATTRIBUTE = "STATE_TOATTRIBUTE";
		private static final String STATE_TOGIVE = "STATE_TOGIVE";
		private static final String STATE_TOFINISH = "STATE_TOFINISH";
		
		public VendeurBehaviour(Enchere e){
			_e = e;
			
			//Register states
			this.registerFirstState	(new ToAnnounceBehaviour(e),STATE_TOANNOUNCE);
			this.registerState(new ToWaitForBehaviour(e), STATE_TOWAITFORBID);
			this.registerState(new ToAttributeBehaviour(e), STATE_TOATTRIBUTE);
			this.registerState(new ToGiveBehaviour(e), STATE_TOGIVE);
			this.registerLastState(new ToFinishBehaviour(), STATE_TOFINISH);
			
			//Register transitions
			this.registerDefaultTransition(STATE_TOANNOUNCE,STATE_TOWAITFORBID);
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOWAITFORBID,0);
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOANNOUNCE,1);
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOATTRIBUTE,2);
			this.registerDefaultTransition(STATE_TOATTRIBUTE,STATE_TOGIVE);
			this.registerDefaultTransition(STATE_TOGIVE,STATE_TOFINISH);
		}
	}
	
	private class ToAnnounceBehaviour extends OneShotBehaviour{
		private Enchere _e;
		
		public ToAnnounceBehaviour(Enchere e){
			_e=e;
		}
		
		@Override
		public void action() {
			//System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToAnnounceBehaviour");
			//Test de l'enchere
			if(_e.getBidCount()>1){
				//augmentation du prix
				_e.setCurrentPrice(_e.getCurrentPrice()*0.25f);				
			}else if(_e.getBidCount()==0){
				//baisse du prix
				_e.setCurrentPrice(_e.getCurrentPrice()/0.75f);
			}
			
			//reinit du compte de bid
			_e.setBidCount(0);
			
			// Fill the CFP message
	  		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	  		msg.addReceiver(_marche);
			// We want to receive a reply in 10 seconds
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			msg.setContent("1"+_e.toMessageString());
			send(msg);
		}	
	}
	
	private class ToWaitForBehaviour extends OneShotBehaviour{
		private Enchere _e;
		
		public ToWaitForBehaviour(Enchere e){
			_e=e;
		}
		
		@Override
		public void action() {
			//System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToWaitForBehaviour");
			// TODO Auto-generated method stub
		}
		
		public int onEnd(){
			return 0;
		}
	}
	
	private class ToAttributeBehaviour extends OneShotBehaviour{
		private Enchere _e;
		
		public ToAttributeBehaviour(Enchere e){
			_e=e;
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToAttributeBehaviour");
			// TODO Auto-generated method stub
		}	
	}
	
	private class ToGiveBehaviour extends OneShotBehaviour{
		private Enchere _e;
		
		public ToGiveBehaviour(Enchere e){
			_e=e;
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToGiveBehaviour");
			// TODO Auto-generated method stub
		}	
	}
	
	private class ToFinishBehaviour extends OneShotBehaviour{
		
		public ToFinishBehaviour(){

		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToFinishBehaviour");
			// TODO Auto-generated method stub
		}	
	}
}
