package agents;

import gui.EnchereVendeurTable;
import gui.VendeurGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pojo.EnchereVendeur;
import pojo.MessageType;
import pojo.Step;

public class Vendeur extends Agent {

	private List<EnchereVendeur> _ev_list;
	private AID _marche;
	private VendeurGUI _vendeurGUI;
	private int _time_wait_for_bid;	
	
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : Arrivee du vendeur");
	  		
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);

	  		_time_wait_for_bid = 10;
	  		
	  		_ev_list = new ArrayList<EnchereVendeur>();
	  		
	  		_vendeurGUI = new VendeurGUI(this);
	  		_vendeurGUI.showGui();

	  		addBehaviour(new VendeurBehaviour());
				
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
		_ev_list.add(ev);
	}
	

	public List<EnchereVendeur> getEncheres(){
		return _ev_list;
	}
	
	private class VendeurBehaviour extends CyclicBehaviour{
		
		public VendeurBehaviour(){			
			//Toutes les 1 secondes, un compteur est incremente pour les enchere en attente
			//de bids. Si la limite de temps d'attente est atteinte, on traite l'enchere 
			//selon le nombre de bids.
			TimerTask task = new TimerTask(){
				@Override
				public void run() {
					int temps_attente=0;
					for(int i=0;i<_ev_list.size();i++){
						//On filtre les encheres a traiter
						if(_ev_list.get(i).getStep()==Step.wait_for_bid){
							temps_attente=_ev_list.get(i).getTempsAttente()+1;
							_ev_list.get(i).setTempsAttente(temps_attente);
						}
					}
				}	
			};
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(task, 0, 1000);
		}
			
		@Override
		public void onStart(){
		}
			
		@Override
		public void action(){
			
			String contMsg;
			int type_message,e_pos;
			EnchereVendeur e;
			
			// -- ETAPE 1
			// -- Gestion des offres
			for(int i=0;i<_ev_list.size();i++){
				
				//Les annonces a effectuer
				if(_ev_list.get(i).getStep()==0){
					System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : envoi d'une annonce");
					
					//On previent le marche
			  		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			  		msg.addReceiver(_marche);
			  		contMsg = String.valueOf(MessageType.TO_ANNOUNCE)+_ev_list.get(i).toMessageString();
					msg.setContent(contMsg);
					send(msg);
					
					//Le message passe à l'étape suivante : init des parametres
					_ev_list.get(i).setStep(Step.wait_for_bid);
					_ev_list.get(i).setBidCount(0);
					_ev_list.get(i).setTempsAttente(0);
					_ev_list.get(i).setListPreneurs(new ArrayList<String>());
					
				//Gestion des attentes de bids
				}else if(_ev_list.get(i).getStep()==Step.wait_for_bid && _ev_list.get(i).getTempsAttente()>_time_wait_for_bid){
					System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : traitement des bids");
					float newprice=0;
					
					//Pas de bids -> On decremente le prix
					if(_ev_list.get(i).getBidCount()==0){
						System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : prix decremente");
						
						//Pas de prix negatif
						if(_ev_list.get(i).getCurrentPrice()>(_ev_list.get(i).getInitPrice()*10/100)){
							newprice = _ev_list.get(i).getCurrentPrice()-_ev_list.get(i).getInitPrice()*10/100;
							_ev_list.get(i).setCurrentPrice(newprice);
						}
						
						//Mise à jour de l'interface graphique
						EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
						ev.addEnchere(new EnchereVendeur(new ArrayList<String>(), _ev_list.get(i).getName(), _ev_list.get(i).getCurrentPrice(), _ev_list.get(i).getVendeur()));
						
						//Retour a l'etat precedent
						_ev_list.get(i).setStep(Step.to_announce);
						
					//Un seul bid -> On attribue l'offre
					}else if(_ev_list.get(i).getBidCount()==1){
						System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : enchere validee");
						
						if(_ev_list.get(i).getListPreneurs().size()==1){	
							AID receiver = new AID(_ev_list.get(i).getListPreneurs().get(0), AID.ISLOCALNAME); //1 seul preneur, donc le premier
		
							//Envoi du message d'attribution au preneur
					  		ACLMessage msgToPreneur = new ACLMessage(ACLMessage.INFORM);
					  		msgToPreneur.addReceiver(receiver); 
					  		contMsg = String.valueOf(MessageType.TO_ATTRIBUTE)+_ev_list.get(i).toMessageString();
					  		msgToPreneur.setContent(contMsg);
							send(msgToPreneur);
							
							//Passage a l'etat final
							_ev_list.get(i).setStep(Step.to_give_pay);
							
						}else{
							System.out.println("Vendeur ** ERREUR ** "+getAID().getName()+" : aucun preneur mais 1 bid compte. Retour en annonce.");
							//Retour a l'etat precedent
							_ev_list.get(i).setStep(Step.to_announce);
						}
					//Plusieurs bids -> On augmente le prix
					}else{
						System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : prix incremente");
						
						//Mise à jour du prix
						newprice = _ev_list.get(i).getCurrentPrice()+_ev_list.get(i).getInitPrice()*10/100;
						_ev_list.get(i).setCurrentPrice(newprice);
						
						//Mise à jour de l'interface graphique
						EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
						ev.addEnchere(new EnchereVendeur(new ArrayList<String>(), _ev_list.get(i).getName(), _ev_list.get(i).getCurrentPrice(), _ev_list.get(i).getVendeur()));
						
						//Retour a l'etat precedent
						_ev_list.get(i).setStep(Step.to_announce);	
					}
				}
			}
			
			// -- ETAPE 2
			// -- Gestion des messages recus
			
			ACLMessage msg = receive();
			
			if(msg!=null){
				System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : Message recu");
				
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_BID:	//Reception d'un message d'enchere pour une offre donnee
					System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : bid recu");
					
					e=new EnchereVendeur();
					e.fromMessageString(contMsg);
					
					//Cette enchere existe dans la liste ?
					e_pos=e.getEnchereVendeurPosition(_ev_list);
					if(e_pos>-1){
						//Si c'est une enchere a l'etape d'attente de bid
						if( _ev_list.get(e_pos).getStep() == Step.wait_for_bid ){
							int newcount = _ev_list.get(e_pos).getBidCount()+1;
							_ev_list.get(e_pos).setBidCount(newcount);
							String[] sEnchere = contMsg.split(";");
							_ev_list.get(e_pos).addPreneur(sEnchere[5]);
						}
					}
	
					break;
				case MessageType.TO_PAY:	//Reception d'un message de paiement pour une offre donnee
					System.out.println("Vendeur ** TRACE ** "+getAID().getName()+" : paiement recu");
					
					e=new EnchereVendeur();
					e.fromMessageString(contMsg);
					
					//Si l'enchere est dans la liste
					e_pos=e.getEnchereVendeurPosition(_ev_list);
					if(e_pos>-1){
						//Si c'est une enchere a l'etape d'attente de paiement
						if(_ev_list.get(e_pos).getStep() == Step.to_give_pay){
							//On accepte le paiement et l'on envoi l'objet
	
							if(_ev_list.get(e_pos).getListPreneurs().size()==1){
								
								//On previent le marche
						  		ACLMessage msgToMarket = new ACLMessage(ACLMessage.INFORM);
						  		msgToMarket.addReceiver(_marche);
						  		contMsg=String.valueOf(MessageType.TO_WITHDRAW)+_ev_list.get(e_pos).toMessageString();
						  		msgToMarket.setContent(contMsg);
								send(msgToMarket);
								
								AID receiver = new AID(_ev_list.get(e_pos).getListPreneurs().get(0), AID.ISLOCALNAME); //1 seul preneur, donc le premier
								
								//Envoi de l'objet au preneur
						  		ACLMessage msgToPreneur = new ACLMessage(ACLMessage.INFORM);
						  		msgToPreneur.addReceiver(receiver); 
						  		contMsg = String.valueOf(MessageType.TO_GIVE)+_ev_list.get(e_pos).toMessageString();
								msgToPreneur.setContent(contMsg);
								send(msgToPreneur);
								
								//On supprime l'offre
								EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
								ev.removeEnchere(e_pos);
							}else{
								System.out.println("Vendeur ** ERREUR ** "+getAID().getName()+" : nombre de preneurs incoherent.");
								_ev_list.get(e_pos).setStep(Step.to_announce);
							}
						}
					}
					
					break;
				default:
					System.out.println("Vendeur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
				}	
			}
		}
	}
}
	
	/*
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
						System.out.println("Vendeur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
					}				
				}

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
			
			//Si ce n'est pas null, alors on a eu l'AR
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
		private MessageTemplate _mt;
		
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
			
			String content;
			
			// --- Etape 1 : On envoi l'objet de l'enchere au preneur
			if(!_objet_envoye){	
				AID receiver = new AID(_e.getListPreneurs().get(0), AID.ISLOCALNAME); //1 seul preneur, donc le premier

				// Fill the CFP message
		  		ACLMessage msgToPreneur = new ACLMessage(ACLMessage.INFORM);
		  		msgToPreneur.addReceiver(receiver); 
		  		content = String.valueOf(MessageType.TO_GIVE)+_e.toMessageString();
				msgToPreneur.setContent(content);
				msgToPreneur.setConversationId(_e.getName());
				msgToPreneur.setReplyWith("9;ok");
				send(msgToPreneur);
			
				// La reponse devra correspondre
				_mt = MessageTemplate.and(MessageTemplate.MatchConversationId(_e.getName()),MessageTemplate.MatchInReplyTo(msgToPreneur.getReplyWith()));
				
				_objet_envoye=true;
			}
			
			int type_message=0;
			Enchere e;
			ACLMessage msg = receive(_mt);
			
			// --- Etape 2 : On attend le paiement
			if(msg!=null){
				//On supprime l'offre
				EnchereVendeurTable ev = _vendeurGUI.getEnchereVendeurTable();
				int evPos = _e.getEnchereVendeurPosition(_encheres);
				ev.removeEnchere(evPos);
				//Passage a l'état final
				_transition=1;
*/
				/*content = msg.getContent();
				type_message = Integer.valueOf(content.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_PAY:
					
					e = new Enchere();
					e.fromMessageString(content);
					
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
				}*/
/*			}
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
	}*/
	
