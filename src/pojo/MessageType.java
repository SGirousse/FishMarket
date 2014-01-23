package pojo;

public class MessageType {

	public static final int TO_ANNOUNCE = 1;		//le vendeur fait un annonce d'une enchere existante ou non
	public static final int TO_WITHDRAW = 2;		//le vendeur retire une enchere (attribuee a un preneur)
	public static final int TO_SUBSCRIBE = 3;		//un preneur s'inscris aupres du marche
	public static final int TO_UNSUBSCRIBE = 4;		//desinscription d'un preneur (avant depart)
	public static final int TO_BID = 5;			//un preneur fait une enchere
}
