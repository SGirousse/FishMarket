package listeners;

import pojo.Enchere;

public interface PreneurEnchereListener {
	void nouvelleEnchere(Enchere e);
	void prixEnchereModifie(Enchere e);
	void enchereRetiree(Enchere e);
}
