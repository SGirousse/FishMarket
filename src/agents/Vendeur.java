package agents;

import gui.EnchereVendeurTable;
import gui.VendeurGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pojo.Enchere;
import pojo.EnchereVendeur;
import pojo.MessageType;

public class Vendeur extends Agent {

	private List<EnchereVendeur> _encheres;
	private AID _marche;
	private VendeurGUI _vendeurGUI;
	private int _time_wait_for_bid;	
	private ParallelBehaviour _vendeur_parallel;
	
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
	  		
	  		_vendeur_parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);	//WHEN_ALL : Le vendeur a un comportement qui se termine quand tous ses sous-comportement se terminent
	  		_vendeur_parallel.addSubBehaviour(new VendeurBehaviour(_encheres.get(0)));
	  		addBehaviour(_vendeur_parallel);
	  		//addBehaviour(new VendeurBehaviour(_encheres.get(0)));
				
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
		System.out.println("NOUVELLE ENCHERE");
		if(_vendeur_parallel.done()){
			System.out.println("ISDONE");
			_vendeur_parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
		}else{
			System.out.println("VENDEUR RUNNABLE OUI");	
		}
		_vendeur_parallel.addSubBehaviour(new VendeurBehaviour(ev));		
		_vendeur_parallel.restart();
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
			this.registerFirstState	(new ToAnnounceBehaviour(_e),STATE_TOANNOUNCE);
			this.registerState(new ToWaitForBehaviour(_e), STATE_TOWAITFORBID);
			this.registerState(new ToAttributeBehaviour(_e), STATE_TOATTRIBUTE);
			this.registerState(new ToGiveAndPayBehaviour(_e), STATE_TOGIVE);
			this.registerLastState(new ToFinishBehaviour(), STATE_TOFINISH);
			
			//Register transitions
			this.registerDefaultTransition(STATE_TOANNOUNCE,STATE_TOWAITFORBID);	//Annonce envoyee
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOWAITFORBID,0);		//Attente de bid
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOANNOUNCE,1);			//Renvoi de l'annonce avec prix modifie
			this.registerTransition(STATE_TOWAITFORBID,STATE_TOATTRIBUTE,2);		//Attribution de l'annonce a un preneur
			this.registerTransition(STATE_TOATTRIBUTE,STATE_TOATTRIBUTE,0);			//Annonce attribuee et AR NON recu
			this.registerTransition(STATE_TOATTRIBUTE,STATE_TOGIVE,1);				//Annonce attribuee et AR recu
			this.registerTransition(STATE_TOGIVE,STATE_TOGIVE,0);					//Objet donne + en attente du paiement
			this.registerTransition(STATE_TOGIVE, STATE_TOFINISH, 1);				//Paiement effectue
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
			msg.setContent("1"+_e.toMessageString());
			send(msg);
		}	
	}
	
	private class ToWaitForBehaviour extends OneShotBehaviour{
		private EnchereVendeur _e;
		private int _transition;
		private int _iteration;
		private boolean _timed;
		
		public ToWaitForBehaviour(EnchereVendeur e){
			_e=e;
			_timed=false;
		}
		
		@Override
		public void onStart(){
			if(!_timed){
				TimerTask task = new TimerTask(){
					@Override
					public void run() {
						_iteration++;
					}	
				};
				
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(task, 0, 1000);
				_timed=true;
			}
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
				/*try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				//_iteration++;
			}else{
				_iteration=0;
				_timed = false;
				float newprice = 0.f;
				if(_e.getBidCount()==0){ 		//pas d'acheteur - on baisse le prix
					//Pas de prix < 0
					if(_e.getCurrentPrice()>(_e.getInitPrice()*10/100)){
						newprice = _e.getCurrentPrice()-_e.getInitPrice()*10/100;
						_e.setCurrentPrice(newprice);
					}
					
					//Mise � jour de l'interface graphique
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
					
					//Remise � 0 du compteur de bid
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
		private boolean _withdraw;
		private MessageTemplate mt;
		private int _transition;
		public ToAttributeBehaviour(EnchereVendeur e){
			_e=e;
			_withdraw=true;
		}
		
		@Override
		public void onStart(){
			_transition = 0;
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToAttributeBehaviour : "+_e.getName());
			
			String content;
			
			//On assure que le message ne soit envoye qu'une seule fois !
			if(_withdraw){
				AID receiver = new AID(_e.getListPreneurs().get(0), AID.ISLOCALNAME); //1 seul preneur, donc le premier

				// Fill the CFP message
		  		ACLMessage msgToPreneur = new ACLMessage(ACLMessage.INFORM);
		  		msgToPreneur.addReceiver(receiver); 
		  		content = String.valueOf(MessageType.TO_ATTRIBUTE)+_e.toMessageString();
				msgToPreneur.setContent(content);
				msgToPreneur.setConversationId(_e.getName());
				msgToPreneur.setReplyWith("9;ok");
				send(msgToPreneur);
				
				// La reponse devra correspondre
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId(_e.getName()),MessageTemplate.MatchInReplyTo(msgToPreneur.getReplyWith()));
				
				_withdraw=false;
			}
			
			//On s'assure que l'acheteur a recu le message pour envoyer le withdraw
			
			ACLMessage reply = receive(mt);
			
			//Si ce n'est pas nul, alors on a eu l'AR
			if(reply!=null){
				System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" AR recu ");
				ACLMessage msgToMarket = new ACLMessage(ACLMessage.INFORM);
				msgToMarket.addReceiver(_marche);
				content = String.valueOf(MessageType.TO_WITHDRAW)+_e.toMessageString();
				msgToMarket.setContent(content);
				send(msgToMarket);
				
				_withdraw=true;
				_transition=1;
			}
		}	
	
		@Override
		public int onEnd(){
			return _transition;
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
		public void onStart(){
			_transition=0;
			
		}
		
		@Override
		public void action() {
			System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : ToGiveAndPayBehaviour : "+_e.getName());
			
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
						//Passage a l'�tat final
						_transition=1;
					}

					break;
				default:
					System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
				}
			}
			
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
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
