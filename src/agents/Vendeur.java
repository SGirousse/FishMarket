package agents;

import gui.EnchereVendeurTable;
import gui.VendeurGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

import pojo.Enchere;
import pojo.EnchereVendeur;
import pojo.MessageType;

public class Vendeur extends Agent {

	private List<EnchereVendeur> _encheres;
	private AID _marche;
	private VendeurGUI _vendeurGUI;
	private int _time_wait_for_bid;	
	
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : Arrivee du vendeur");
	  		
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);

	  		_time_wait_for_bid = 10;
	  		
	  		_encheres = new ArrayList<EnchereVendeur>();
	  		_encheres.add(new EnchereVendeur(new ArrayList<String>(), "GoldenFish", 42.f, getAID().getLocalName()));
	  		
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
		EnchereVendeur ev = new EnchereVendeur(new ArrayList<String>(), title, price, getAID().getLocalName());
		_encheres.add(ev);
		addBehaviour(new VendeurBehaviour(ev));		
	}
	
	public List<EnchereVendeur> getEncheres(){
		return _encheres;
	}
	
	private class VendeurBehaviour extends FSMBehaviour{
		private EnchereVendeur _e;
		
		private static final String STATE_TOANNOUNCE = "STATE_TOANNOUNCE";
		private static final String STATE_TOWAITFORBID = "STATE_TOWAITFORBID";
		private static final String STATE_TOATTRIBUTE = "STATE_TOATTRIBUTE";
		private static final String STATE_TOGIVE = "STATE_TOGIVE";
		private static final String STATE_TOFINISH = "STATE_TOFINISH";
		
		public VendeurBehaviour(EnchereVendeur e){
			_e = e;
			
			//Register states
			this.registerFirstState	(new ToAnnounceBehaviour(e),STATE_TOANNOUNCE);
			this.registerState(new ToWaitForBehaviour(e), STATE_TOWAITFORBID);
			this.registerState(new ToAttributeBehaviour(e), STATE_TOATTRIBUTE);
			this.registerState(new ToGiveAndPayBehaviour(e), STATE_TOGIVE);
			this.registerLastState(new ToFinishBehaviour(), STATE_TOFINISH);
			
			//Register transitions
			this.registerDefaultTransition(STATE_TOANNOUNCE,STATE_TOWAITFORBID);
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOWAITFORBID,0);
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOANNOUNCE,1);
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOATTRIBUTE,2);
			this.registerDefaultTransition(STATE_TOATTRIBUTE,STATE_TOGIVE);
			this.registerTransition(STATE_TOGIVE,STATE_TOGIVE,0);
			this.registerTransition(STATE_TOGIVE, STATE_TOFINISH, 1);
		}
	}
	
	private class ToAnnounceBehaviour extends OneShotBehaviour{
		private EnchereVendeur _e;
		
		public ToAnnounceBehaviour(EnchereVendeur e){
			_e=e;
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToAnnounceBehaviour");
			
			// Fill the CFP message
	  		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	  		msg.addReceiver(_marche);
			//Si attente reponse dans les 10 secondes
			//msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			msg.setContent("1"+_e.toMessageString());
			send(msg);
		}	
	}
	
	private class ToWaitForBehaviour extends OneShotBehaviour{
		private EnchereVendeur _e;
		private int _transition;
		private int _iteration;
		
		public ToWaitForBehaviour(EnchereVendeur e){
			_e=e;
		}
		
		@Override
		public void action() {
			_transition=0;
			int type_message=0;
			String contMsg;
			Enchere e;
			ACLMessage msg = receive();
	
			if(_iteration<_time_wait_for_bid){
				if(msg!=null){

					System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : Message recu");
					contMsg = msg.getContent();
					type_message = Integer.valueOf(contMsg.substring(0, 1));
					
					switch(type_message){
					case MessageType.TO_BID:
						System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : bid recu");
						
						e = new Enchere();
						e.fromMessageString(contMsg);
						
						if(e.compareTo(_e)==0){
							int newcount = _e.getBidCount()+1;
							_e.setBidCount(newcount);
							String[] sEnchere = contMsg.split(";");
							_e.addPreneur(sEnchere[5]);
						}
	
						break;
					default:
						System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
					}				
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				_iteration++;
			}else{
				_iteration=0;
				float newprice = 0.f;
				if(_e.getBidCount()==0){ 		//pas d'acheteur - on baisse le prix
					//Pas de prix < 0
					if(_e.getCurrentPrice()>(_e.getInitPrice()*10/100)){
						newprice = _e.getCurrentPrice()-_e.getInitPrice()*10/100;
						_e.setCurrentPrice(newprice);
					}
					
					//Mise à jour de l'interface graphique
					EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
					ev.addEnchere(new EnchereVendeur(new ArrayList<String>(), _e.getName(), _e.getCurrentPrice(), _e.getVendeur()));
					
					_transition=1;
				}else if(_e.getBidCount()==1){	//un seul acheteur - on va attribuer
					_transition=2;
				}else{							//plusieurs acheteurs - on augmente le prix
					newprice = _e.getCurrentPrice()+_e.getInitPrice()*10/100;
					_e.setCurrentPrice(newprice);

					EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
					ev.addEnchere(new EnchereVendeur(new ArrayList<String>(), _e.getName(), _e.getCurrentPrice(), _e.getVendeur()));
					
					//Remise à 0 du compteur de bid
					_e.setBidCount(0);
					_e.setListPreneurs(new ArrayList<String>());
					
					_transition=1;
				}
			}
		}
		
		public int onEnd(){
			return _transition;
		}
	}
	
	private class ToAttributeBehaviour extends OneShotBehaviour{
		private EnchereVendeur _e;
		
		public ToAttributeBehaviour(EnchereVendeur e){
			_e=e;
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToAttributeBehaviour");
			
			AID receiver = new AID(_e.getListPreneurs().get(0), AID.ISLOCALNAME); //1 seul preneur, donc le premier
			String content;
			
			// Fill the CFP message
	  		ACLMessage msgToPreneur = new ACLMessage(ACLMessage.INFORM);
	  		msgToPreneur.addReceiver(receiver); 
	  		content = String.valueOf(MessageType.TO_ATTRIBUTE)+_e.toMessageString();
			msgToPreneur.setContent(content);
			send(msgToPreneur);
			
			ACLMessage msgToMarket = new ACLMessage(ACLMessage.INFORM);
			msgToMarket.addReceiver(_marche);
			content = String.valueOf(MessageType.TO_WITHDRAW)+_e.toMessageString();
			msgToMarket.setContent(content);
			send(msgToMarket);
		}	
	}
	
	private class ToGiveAndPayBehaviour extends OneShotBehaviour{
		private EnchereVendeur _e;
		private boolean _objet_envoye;
		private int _transition;
		
		public ToGiveAndPayBehaviour(EnchereVendeur e){
			_e=e;
			_objet_envoye=false;
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToGiveAndPayBehaviour");
			
			_transition=0;
			
			// --- Etape 1 : On envoi l'objet de l'enchere au preneur
			if(!_objet_envoye){	
				AID receiver = new AID(_e.getListPreneurs().get(0), AID.ISLOCALNAME); //1 seul preneur, donc le premier
				String content;

				// Fill the CFP message
		  		ACLMessage msgToPreneur = new ACLMessage(ACLMessage.INFORM);
		  		msgToPreneur.addReceiver(receiver); 
		  		content = String.valueOf(MessageType.TO_GIVE)+_e.toMessageString();
				msgToPreneur.setContent(content);
				send(msgToPreneur);
			
				_objet_envoye=true;
			}
			
			int type_message=0;
			String contMsg;
			Enchere e;
			ACLMessage msg = receive();
			
			// --- Etape 2 : On attend le paiement
			if(msg!=null){
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_PAY:
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					if(e.compareTo(_e)==0){
						//On supprime l'offre
						EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
						int evPos = _e.getEnchereVendeurPosition(_encheres);
						ev.removeEnchere(evPos);
						//_encheres.remove(evPos);
						//Passage a l'état final
						_transition=1;
					}

					break;
				default:
					System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		@Override
		public int onEnd(){
			return _transition;
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
