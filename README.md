#######################################################
###                   README                        ###
###                                                 ###
### @authors : F. Deliancourt, S. Girousse          ###
### @years : 2013-2014                              ###
#######################################################

### Still under development (super alpha)

### Description :
That project is included in "Master 2, Technologies de l'Internet" training course at the "Université de Pau et des Pays de l'Adour".
Main goal is to program an agent system using Jade Development Framework ( http://jade.tilab.com/ ) for the "Fondement des Systèmes Multi-Agents (FSMA) " course.

It will be a representation of a fish market with :
 - 1 seller agent (which has multi-offers)
 - 1 market agent
 - n buyer agents

### Launch command
java jade.Boot -gui "V:Vendeur(M);M:Marche;P1:Preneur(M);P2:Preneur(M);P3:Preneur(M)"
