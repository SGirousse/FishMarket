package listeners;

import pojo.Enchere;

public abstract class PreneurEnchereAdapter implements PreneurEnchereListener {

	@Override
	public void nouvelleEnchere(Enchere e) {}

	@Override
	public void prixEnchereModifie(Enchere e) {}

	@Override
	public void enchereRetiree(Enchere e) {}
}
