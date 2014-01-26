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

import javax.swing.JButton;

import pojo.Enchere;
import pojo.MessageType;

public class Preneur extends Agent {

	private Float _money;
	private List<Enchere> _encheres;
	private Enchere _current_enchere;
	private AID _marche;
	private PreneurGUI _preneurGUI;
	private boolean _system_auto;
	
	protected void setup(){
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 2) {
	  		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Arrivee de l'acheteur");
	  		
	  		_marche = new AID((String)args[0], AID.ISLOCALNAME);
	  		_money = Float.valueOf((String)args[1]);
	  		//system automatique ?
	  		_system_auto = Integer.valueOf((String)args[2])==1;
	  		
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
  		String content = String.valueOf(MessageType.TO_UNSUBSCRIBE)+getAID();
  		unsubscribeMsg.setContent(content);
  		unsubscribeMsg.setConversationId("desabonnement-marche");
  		send(unsubscribeMsg);
  		
		_encheres = null;
		_marche = null;
		_money = null;
		_current_enchere = null;
		_preneurGUI = null;
		
		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Depart de l'acheteur");
	}
	
	public List<Enchere> getEncheres(){
		return _encheres;
	}
	
	public void addNewEnchere(Enchere e){
		int ePos = e.getEncherePosition(_encheres);
		EncherePreneurTable modele = _preneurGUI.getEncherePreneurTable();
		
		if(ePos!=-1){	//enchere existante
			_encheres.get(ePos).setCurrentPrice(e.getCurrentPrice());
		}else{
			_encheres.add(e);			
		}
		
		//Mise a jour de l'interface et de la liste
		modele.addEnchere(e);
	}
	
	public void removeEnchere(Enchere e){
		int ePos = e.getEncherePosition(_encheres);
		EncherePreneurTable modele = _preneurGUI.getEncherePreneurTable();
		
		if(ePos!=-1){	//enchere existante
			//_encheres.remove(ePos);
			modele.removeEnchere(ePos);
		}
	}
	
	public void toBid(Enchere e){
		System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : public void toBid(Enchere e)");
		if(e.getCurrentPrice()<=_money){		
			_current_enchere=e;
		}else{
			_preneurGUI.cleanCurrentEnchere();
		}
	}
	
	public float getMoney(){
		return _money;
	}
	
	/**
	 * On s'abonne au marche pour eviter toute attente active des offres.
	 *
	 * @see Marche
	 */
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
			this.registerTransition(STATE_TOWAITFORANNOUNCE, STATE_TOBID,0);				//Announce - interested
			this.registerTransition(STATE_TOWAITFORANNOUNCE, STATE_TOWAITFORANNOUNCE,1);	//Announce - not interested
			this.registerTransition(STATE_TOBID,STATE_TOBID,0);								//Nothing happened
			this.registerTransition(STATE_TOBID,STATE_TOWAITFORANNOUNCE,1);					//Rejected
			this.registerTransition(STATE_TOBID,STATE_TOPAY,2);								//Accepted
			this.registerTransition(STATE_TOPAY,STATE_TOPAY, 0);							//Attente objet enchere
			this.registerTransition(STATE_TOPAY,STATE_TOFINISH,1);							//Everything OK - le preneur se retire du marche
			this.registerTransition(STATE_TOPAY,STATE_TOWAITFORANNOUNCE, 2);				//Everything OK - le preneur continue a suivre les encheres
		}
	}
	
	/**
	 * Cette classe est la representation de l'attente d'une annonce interessante pour l'algorithme automatique 
	 * ou l'utilisateur dans le cas d'une gestion manuelle.
	 *
	 */
	private class ToWaitForAnnounceBehaviour extends OneShotBehaviour {

		int _transition;	//Used as integer for convenience in "onEnd()"
		
		public ToWaitForAnnounceBehaviour(){
			System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : ToWaitForAnnounceBehaviour");
			_current_enchere=null;
		}
		
		@Override
		public void action() {
			int type_message=0;
			String contMsg;
			Enchere e;
			_transition=1;
			
			if(_system_auto){
				//Systeme automatique - on prend la premiere a dispo
				if(_encheres.size()>0){
					int i=0;
					boolean ok=false;
					while(!ok && i<_encheres.size()){
						if(_encheres.get(i).getCurrentPrice()<=_money){
							_preneurGUI.toBid(i);
							_current_enchere=_encheres.get(i);
							//Envoi du message au vendeur
					  		ACLMessage bidMsg = new ACLMessage(ACLMessage.INFORM);
					  		AID aid_tmp = new AID(_current_enchere.getVendeur(), AID.ISLOCALNAME);
					  		bidMsg.addReceiver(aid_tmp);
					  		contMsg = String.valueOf(MessageType.TO_BID)+_current_enchere.toMessageString()+getAID().getLocalName();
					  		bidMsg.setContent(contMsg);
							send(bidMsg);
							
							//passage a l'etat suivant
							_transition=0;
							
							ok=true;
						}else{
							i++;
						}
					}
				}
			}else{
				//Une nouvelle a-t-elle ete selectionnee ?
				if(_current_enchere!=null){
					//Envoi du message au vendeur
			  		ACLMessage bidMsg = new ACLMessage(ACLMessage.INFORM);
			  		AID aid_tmp = new AID(_current_enchere.getVendeur(), AID.ISLOCALNAME);
			  		bidMsg.addReceiver(aid_tmp);
			  		contMsg = String.valueOf(MessageType.TO_BID)+_current_enchere.toMessageString()+getAID().getLocalName();
			  		bidMsg.setContent(contMsg);
					send(bidMsg);
					
					//passage a l'etat suivant
					_transition=0;
				}
			}
			
			//Si aucun "bid" sur une enchere n'a ete fait, on gere les messages recus.
			if(_current_enchere==null){
				ACLMessage msg = receive();
				
				if(msg!=null){
					contMsg = msg.getContent();
					type_message = Integer.valueOf(contMsg.substring(0, 1));
					
					switch(type_message){
					case MessageType.TO_ANNOUNCE:	//Une enchere a ete envoyee, on l'ajoute au tableau
						System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : annonce recue");
						
						e = new Enchere();
						e.fromMessageString(contMsg);
						
						addNewEnchere(e);
						_preneurGUI.numberOfOffersChanged();
	
						break;
					case MessageType.TO_WITHDRAW:	//Une enchere a ete attribuee a un autre preneur, on la retire du tableau
						System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : enchere annulee");
						
						e = new Enchere();
						e.fromMessageString(contMsg);
						
						removeEnchere(e);
						_preneurGUI.numberOfOffersChanged();
												
						break;
					default:
						System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
					}				
				}
			}
		}
		
		@Override
		public int onEnd(){
			return _transition;
		}
	}
	
	/**
	 * 
	 * Gestion du comportement apres avoir participe a une enchere.
	 *
	 */
	private class ToBidBehaviour extends OneShotBehaviour {
		
		private int _transition;
		
		public ToBidBehaviour(){
			_transition=0;
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			int type_message=0;
			String contMsg;
			Enchere e;
			
			_transition=0;
			
			if(msg!=null){
				contMsg = msg.getContent();
				System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : msg received "+contMsg);
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_ANNOUNCE:
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : annonce recue");
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					if(e.compareTo(_current_enchere)==0){	//l'enchere est celle actuellement traitee
						//l'enchere a ete refusee... on retourne a l'etat precedent
						_transition = 1;
						_current_enchere = null;
						_preneurGUI.cleanCurrentEnchere();
					}
					
					//Dans tous les cas, on met a jour le tableau
					addNewEnchere(e);

					break;
					
				case MessageType.TO_WITHDRAW:
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : enchere annulee (donnee a un autre acheteur :'( )");
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					//l'enchere est celle actuellement traitee ?
					if(e.compareTo(_current_enchere)==0){	
						//l'enchere a ete refusee... on retourne a l'etat precedent en nettoyant tout (enchere, interface, ...)
						_transition = 1;
						_current_enchere = null;
					}

					removeEnchere(e);
					_preneurGUI.cleanCurrentEnchere();
					
					break;
				case MessageType.TO_ATTRIBUTE:
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : enchere validee");
					
					//Passage a l'etat suivant (paiement et reception de l'objet)
					_transition=2;
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

		private boolean _paiement_envoye;
		private int _transition;
		
		public ToPayBehaviour(){
			_paiement_envoye=false;
		}
		
		@Override
		public void action() {

			System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : ToPayBehaviour");
			
			_transition=0;
			
			
			// --- Etape 1 : On envoi l'objet de l'enchere au preneur
			if(!_paiement_envoye){	

				System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : Envoi du paiement");
				
				AID receiver = new AID(_current_enchere.getVendeur(), AID.ISLOCALNAME);
				String content;

				// Fill the CFP message
		  		ACLMessage msgToVendeur = new ACLMessage(ACLMessage.INFORM);
		  		msgToVendeur.addReceiver(receiver); 
		  		content = String.valueOf(MessageType.TO_PAY)+_current_enchere.toMessageString();
				msgToVendeur.setContent(content);
				send(msgToVendeur);
			
				//on decremente le porte-monnaie
				_money -= _current_enchere.getCurrentPrice();
				_paiement_envoye=true;
			}
			
			
			int type_message=0;
			String contMsg;
			Enchere e;
			ACLMessage msg = receive();
			
			// --- Etape 2 : On attend la reception de l'enchere
			
			if(msg!=null){
				contMsg = msg.getContent();
				type_message = Integer.valueOf(contMsg.substring(0, 1));
				
				switch(type_message){
				case MessageType.TO_ANNOUNCE:	//Gestion des nouvelles encheres qui arrivent
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : annonce recue");
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					if(e.compareTo(_current_enchere)!=0){	//l'enchere n'est pas celle actuellement traitee
						addNewEnchere(e);
					}					
					
					break;
				case MessageType.TO_WITHDRAW:	//Gestion des encheres qui ne sont plus en cours
					System.out.println("Preneur ** TRACE ** "+getAID().getName()+" : enchere annulee - (la mienne ?)");
					
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					//l'enchere est celle actuellement traitee ?
					if(e.compareTo(_current_enchere)!=0){	
						//ce n'est pas mon enchere, il faut donc mettre a jour les informations
						removeEnchere(e);
					}
					
					break;
				case MessageType.TO_GIVE:	//Reception de l'enchere
			
					e = new Enchere();
					e.fromMessageString(contMsg);
					
					if(e.compareTo(_current_enchere)==0){
						
						//On supprime l'offre du tableau
						EncherePreneurTable modele = _preneurGUI.getEncherePreneurTable();
						int ePos = _current_enchere.getEncherePosition(_encheres);
						modele.removeEnchere(ePos);
						
						//Plus d'enchere courante
						_current_enchere=null;
						_preneurGUI.cleanCurrentEnchere();
						
						_paiement_envoye=false;
						
						//Verification du comportement selon le porte-monnaie
						if(_money<=0){
							//Passage a l'état final
							_transition=1;
						}else{
							//Retour au systeme d'encheres
							_transition=2;
						}
					}
					break;
				default:
					System.out.println("Preneur ** ERREUR ** "+getAID().getName()+" : "+msg.getContent()+" - Type de message non gere ("+type_message+")");
				}
			}
		}
		
		public int onEnd(){
			return _transition;
		}
		
	}
	
	private class ToFinishBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			takeDown();			
		}
		
	}
}
