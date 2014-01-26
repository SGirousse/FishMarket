#######################################################
###                   README                        ###
###                                                 ###
### @authors : F. Deliancourt, S. Girousse          ###
### @years : 2013-2014                              ###
#######################################################

### Description :
That project is included in "Master 2, Technologies de l'Internet" at the "Universitee de Pau et des Pays de l'Adour".
Main goal is to program an agent system using Jade Development Framework ( http://jade.tilab.com/ ) for the "Fondement des Systemes Multi-Agents (FSMA) " course.

It will be a representation of a fish market with :
 - 1 seller agent (which has multi-offers)
 - 1 market agent
 - n buyer agents
 
### NB :
It's possible to launch n sellers instead of only one.

### Parameters
Vendeur(seller) : market name
Preneur(buyer) : market name, amount of available money and mode (1=auto,0=manual)

### Launch command example
java jade.Boot -gui "market:agents.Marche;seller1:agents.Vendeur(market);buyer1:agents.Preneur(market,150,1);buyer2 :agents.Preneur(market,360,1) ;buyer3 :agents.Preneur(market,500,0)"