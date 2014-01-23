package agents;

import gui.EncherePreneurTable;
import gui.PreneurGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import pojo.Enchere;
import pojo.MessageType;

public class Preneur extends Agent {

	private Float _money;
	private List<Enchere> _encheres;
	private Enchere _current_enchere;
	private AID _marche;
	private PreneurGUI _preneurGUI;
	
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Arrivee de l'acheteur");
	  		
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);
	  		//_money = Float.valueOf((String)args[1]);
	  		_money = 150.f;
	  		
	  		_encheres = new ArrayList<Enchere>();
	  		
	  		_preneurGUI = new PreneurGUI(this);
	  		_preneurGUI.showGui();
	  	  		
	  		addBehaviour(new AbonnementMarcheBehaviour());
	  		addBehaviour(new PreneurBehaviour());
	  		
	  	}else{
	  		System.out.println("Preneur ** ERROR ** "+getAID().getName()+" : Missing Marche Agent Name and/or money");
	  	}
	}
	
	protected void takeDown(){
		ACLMessage unsubscribeMsg = new ACLMessage(ACLMessage.INFORM);
  		unsubscribeMsg.addReceiver(_marche);
  		unsubscribeMsg.setContent("4"+getAID());
  		unsubscribeMsg.setConversationId("desabonnement-marche");
  		send(unsubscribeMsg);
  		
		_encheres = null;
		_marche = null;
		_money = null;
		_current_enchere = null;
		
		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Depart de l'acheteur");
	}
	
	public List<Enchere> getEncheres(){
		return _encheres;
	}
	
	public void addNewEnchere(Enchere e){
		int ePos = e.getEncherePosition(_encheres);
		EncherePreneurTable modele = _preneurGUI.getEncherePreneurTable();
		
		if(ePos!=-1){	//enchere existante
			_encheres.remove(ePos);
			modele.removeEnchere(ePos);
		}
		
		//Mise a jour de l'interface et de la liste
		_encheres.add(e);
		modele.addEnchere(e);
	}
	
	public void removeEnchere(Enchere e){
		int ePos = e.getEncherePosition(_encheres);
		EncherePreneurTable modele = _preneurGUI.getEncherePreneurTable();
		
		if(ePos!=-1){	//enchere existante
			_encheres.remove(ePos);
			modele.removeEnchere(ePos);
		}
	}
	
	private class AbonnementMarcheBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
	  		// Abonnement aupres du marche
	  		ACLMessage subscribeMsg = new ACLMessage(ACLMessage.INFORM);
	  		subscribeMsg.addReceiver(_marche);
	  		subscribeMsg.setContent("3"+getAID().getName());
	  		subscribeMsg.setConversationId("abonnement-marche");
	  		send(subscribeMsg);		
		}
		
	}
	
	private class PreneurBehaviour extends FSMBehaviour {
		
		private Enchere _e;
		
		private static final String STATE_TOWAITFORANNOUNCE = "STATE_TOWAITFORANNOUNCE";
		private static final String STATE_TOBID = "STATE_TOBID";
		private static final String STATE_TOPAY = "STATE_TOPAY";
		private static final String STATE_TOFINISH = "STATE_TOFINISH";
		
		public PreneurBehaviour(){
			//Register states
			this.registerFirstState	(new ToWaitForAnnounceBehaviour(), STATE_TOWAITFORANNOUNCE);
			this.registerState(new ToBidBehaviour(), STATE_TOBID);
			this.registerState(new ToPayBehaviour(), STATE_TOPAY);
			this.registerLastState(new ToFinishBehaviour(), STATE_TOFINISH);
			
			//Register transitions
			this.registerTransition(STATE_TOWAITFORANNOUNCE, STATE_TOBID,0);	//Announce - not interested
			this.registerTransition(STATE_TOWAITFORANNOUNCE, STATE_TOWAITFORANNOUNCE,1);	//Announce - interested
			this.registerTransition(STATE_TOBID,STATE_TOBID,0);					//Nothing happened
			this.registerTransition(STATE_TOBID,STATE_TOWAITFORANNOUNCE,1);		//Rejected
			this.registerTransition(STATE_TOBID,STATE_TOPAY,2);					//Accepted
			this.registerDefaultTransition(STATE_TOPAY,STATE_TOFINISH);			//Everything OK
		}
	}
	
	private class ToWaitForAnnounceBehaviour extends OneShotBehaviour {

		int _bid;	//boolean used as integer for convenience in "onEnd()"
		
		public ToWaitForAnnounceBehaviour(){
			System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : ToWaitForAnnounceBehaviour");
			_bid=1;
			_current_enchere=null;
		}
		
		@Override
		public void action() {
			ACLMessage msg = receive();
			int type_message=0;
			String contMsg;
			Enchere e;
			
			if(msg!=null){
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_ANNOUNCE:
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : annonce recue");
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					addNewEnchere(e);
					
					////////////////////////////////////////////////
					/// TEST AND DEBUG - TO BE NOTIFIED FROM ITF ///
					if(_money>e.getCurrentPrice()){	//si on a les fonds pour
						_bid=0;
						_current_enchere=e;
					}
					/// TEST AND DEBUG - TO BE NOTIFIED FROM ITF ///
					////////////////////////////////////////////////
					
					break;
				default:
					System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
				}				
			}
		}
		
		@Override
		public int onEnd(){
			return _bid;
		}
		
	}
	
	private class ToBidBehaviour extends OneShotBehaviour {
		
		private int _transition;
		
		public ToBidBehaviour(){
			System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : ToBidBehaviour");
			_transition=0;
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			int type_message=0;
			String contMsg;
			Enchere e;
			
			if(msg!=null){
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_ANNOUNCE:
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : annonce recue");
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					if(e.compareTo(_current_enchere)==1){	//l'enchere est celle actuellement traitee
						//l'enchere a ete refusee... on retourne a l'etat precedent
						_transition = 1;
					}
					
					//Dans tous les cas, on met a jour le tableau
					addNewEnchere(e);

					break;
				case MessageType.TO_ATTRIBUTE:
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : enchere validee");
					
					//Passage a l'etat suivant (paiement et reception de l'objet)
					_transition=0;
					break;
				default:
					System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
				}				
			}
		}
		
		@Override
		public int onEnd(){
			return _transition;
		}
		
	}
	
	private class ToPayBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ToFinishBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
