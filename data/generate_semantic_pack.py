# -*- coding: utf-8 -*-
"""Génère semantic_pack_demo.json : 50 mots secrets FR, 320-420 voisins scorés chacun.
Architecture remplaçable plus tard par word2vec/FastText : même format de sortie.
Usage: python generate_semantic_pack.py
"""
import json, hashlib, os

THEMES = {
"cuisine": "restaurant cuisine repas serveur menu café plat chef table couvert fourchette couteau cuillère assiette verre vin pain sel poivre sauce viande poisson légume fruit dessert gâteau sucre farine four casserole poêle recette goût saveur déjeuner dîner manger boire faim soif bistrot brasserie commande addition pourboire terrasse gastronomie épice bouillon rôti grillade tarte crêpe fromage beurre lait œuf riz pâtes soupe salade".split(),
"nature": "forêt arbre feuille branche racine tronc bois champignon mousse sentier clairière chêne sapin pin bouleau fleur herbe prairie buisson fougère écorce nid cerf sanglier renard écureuil hibou champ campagne montagne colline vallée rivière ruisseau cascade lac étang rocher pierre terre boue verdure feuillage randonnée sommet falaise grotte plaine pollen bourgeon sève".split(),
"mer": "mer océan plage sable vague marée poisson bateau voile port phare île côte coquillage crabe méduse dauphin baleine requin algue corail pêche pêcheur filet horizon écume embrun mouette croisière navire ancre quai matelot capitaine profondeur abysse courant naufrage bouée plongée surf marin littoral baie golfe récif épave sirène".split(),
"musique": "musique chanson mélodie rythme note partition concert orchestre guitare piano violon batterie tambour flûte trompette chant voix chanteur musicien compositeur harmonie accord gamme refrain couplet paroles album disque radio scène public applaudissement danse opéra symphonie jazz rock studio micro casque solfège chorale instrument fanfare tempo vinyle".split(),
"sport": "sport football tennis basket rugby course coureur marathon vélo cyclisme natation piscine gymnase stade terrain ballon balle raquette filet but gardien équipe joueur entraîneur arbitre match victoire défaite score champion médaille podium record entraînement muscle effort sueur compétition tournoi sprint endurance dribble penalty mi-temps vestiaire supporter maillot".split(),
"ecole": "école élève professeur classe cours leçon devoir cahier stylo crayon tableau craie cartable récréation cantine collège lycée université étudiant examen note diplôme apprentissage lecture écriture calcul mathématiques histoire géographie sciences grammaire dictée bibliothèque livre savoir connaissance éducation pédagogie rentrée vacances copie correction directeur pupitre trousse gomme règle".split(),
"voyage": "voyage train avion aéroport gare valise bagage passeport billet destination touriste hôtel réservation carte itinéraire route autoroute voiture bus métro tramway taxi conducteur pilote vol escale frontière douane aventure découverte excursion départ arrivée retard horaire quai correspondance étranger embarquement décollage atterrissage hublot compartiment wagon locomotive rail".split(),
"maison": "maison appartement toit mur fenêtre porte clé serrure salon chambre salle garage jardin balcon escalier étage cave grenier meuble canapé fauteuil lit armoire chaise lampe rideau tapis cheminée chauffage électricité plomberie loyer propriétaire locataire déménagement adresse voisin immeuble façade volet couloir parquet plafond terrasse potager pelouse clôture portail".split(),
"animaux": "animal chien chat cheval vache mouton chèvre cochon poule coq canard lapin souris rat hamster oiseau perroquet tortue serpent lézard grenouille loup ours lion tigre éléphant girafe singe zèbre ferme zoo vétérinaire patte queue museau poil plume griffe niche litière croquette aboiement miaulement galop troupeau étable écurie".split(),
"corps": "corps tête bras jambe main pied doigt épaule genou coude cœur poumon estomac cerveau peau os muscle sang veine œil oreille nez bouche dent langue cheveu visage santé médecin docteur hôpital infirmier maladie fièvre douleur médicament ordonnance vaccin guérison fatigue sommeil respiration squelette nerf foie rein gorge ventre".split(),
"ville": "ville rue avenue boulevard trottoir carrefour place marché boutique magasin centre quartier banlieue mairie église cathédrale musée parc fontaine statue pont tunnel lampadaire passant foule circulation embouteillage parking gratte-ciel commerce vitrine enseigne piéton urbain habitant population capitale métropole village agglomération ruelle pavé kiosque abribus égout".split(),
"meteo": "météo pluie soleil nuage vent orage tonnerre éclair tempête neige grêle brouillard brume gel givre verglas chaleur canicule froid température degré climat saison été hiver automne printemps ciel arc-en-ciel averse ondée parapluie prévision thermomètre humidité sécheresse inondation rafale bourrasque flocon goutte éclaircie crachin mistral".split(),
"travail": "travail bureau emploi métier salaire patron employé collègue réunion projet dossier ordinateur clavier imprimante entreprise société usine atelier embauche entretien démission retraite carrière promotion contrat syndicat grève pause horaires badge secrétaire directeur stagiaire mission tâche productivité télétravail chômage formation candidature recrutement licenciement prime congé".split(),
"fete": "fête anniversaire cadeau gâteau bougie invité ballon guirlande danse mariage noces cérémonie noël sapin réveillon champagne toast vœux carnaval déguisement masque artifice célébration banquet buffet apéritif famille ami rire joie surprise décoration confetti tradition festin bal cotillon feu retrouvailles convive".split(),
"vetements": "vêtement pantalon chemise robe jupe pull manteau veste blouson écharpe gant bonnet chapeau chaussure botte basket chaussette ceinture cravate costume pyjama maillot short tissu coton laine soie cuir couture bouton fermeture poche manche col taille mode style garde-robe cintre repassage lessive tailleur uniforme dentelle velours".split(),
"tech": "technologie ordinateur téléphone smartphone tablette écran clavier souris internet réseau wifi application logiciel programme code données serveur cloud email message photo vidéo jeu console robot intelligence batterie chargeur câble bluetooth processeur mémoire disque fichier dossier piratage bug numérique algorithme pixel webcam imprimante antivirus navigateur".split(),
"argent": "argent monnaie euro billet pièce banque compte carte crédit dette emprunt prêt épargne économie richesse pauvreté prix coût achat vente commerce client vendeur caisse facture budget impôt fortune investissement bourse action marché bénéfice perte dépense paiement chèque distributeur portefeuille banquier guichet liquide virement remboursement solde".split(),
"art": "art peinture tableau toile pinceau couleur dessin sculpture statue artiste peintre galerie exposition musée œuvre portrait paysage abstrait aquarelle fresque gravure photographie cinéma théâtre poésie roman littérature création inspiration imagination beauté esthétique style mouvement renaissance moderne contemporain atelier vernissage palette chevalet croquis esquisse mécène".split(),
"amour": "amour cœur passion tendresse câlin baiser couple amoureux rencontre rendez-vous séduction charme romance sentiment émotion bonheur joie tristesse jalousie rupture mariage fiançailles alliance saint-valentin rose déclaration lettre poème regard sourire complicité confiance fidélité âme désir affection attachement étreinte soupir flirt promesse".split(),
"temps": "temps heure minute seconde montre horloge réveil calendrier date jour semaine mois année siècle époque passé présent futur instant moment durée retard avance ponctualité agenda délai chronomètre sablier aiguille cadran midi minuit aube crépuscule matin soir nuit journée éternité décennie hier demain aujourd'hui".split(),
}

GLOBAL_POOL = "chose idée monde vie homme femme enfant personne gens eau feu air mot nom question réponse problème solution exemple raison cause effet début fin milieu côté partie ensemble groupe nombre forme couleur taille papier journal lettre histoire image bruit silence lumière ombre force énergie machine outil objet boîte sac bouteille bouton liste point ligne cercle carré chemin direction endroit lieu pays région nord sud est ouest gauche droite haut bas intérieur extérieur dessus dessous loi droit règle ordre liberté paix guerre armée police justice pouvoir gouvernement président vote nation peuple langue parole phrase texte page chapitre titre auteur lecteur main-d'œuvre nature humain esprit pensée mémoire rêve souvenir oubli attention regard geste pas saut chute mouvement vitesse poids mesure moitié double total reste différence ressemblance changement développement croissance baisse hausse niveau degré qualité défaut avantage chance risque danger sécurité aide secours service besoin envie choix décision projet plan but objectif moyen méthode façon manière habitude coutume usage essai erreur succès échec progrès avenir espoir peur courage honte fierté colère calme repos action réaction situation état condition cas fait événement nouvelle information détail secret vérité mensonge blague humour sérieux jeu règle carte dé pion hasard victoire prix lot ticket file attente porte-monnaie poche montagne-russe vacarme odeur parfum goûter toucher".split()

# 50 mots secrets : (mot, thème, cœur curé = ~10 mots les plus proches)
SECRETS = [
("restaurant","cuisine","cuisine repas serveur menu chef bistrot brasserie plat addition terrasse gastronomie commande".split()),
("cuisine","cuisine","recette plat chef casserole four restaurant repas poêle ingrédient sauce fourneau marmite".split()),
("dessert","cuisine","gâteau tarte sucre crêpe chocolat glace pâtisserie crème fruit douceur".split()),
("pain","cuisine","boulangerie baguette farine croûte mie levure four boulanger céréale tartine".split()),
("forêt","nature","arbre bois sentier clairière chêne sapin feuillage sous-bois champignon mousse bûcheron futaie".split()),
("montagne","nature","sommet colline vallée altitude alpinisme randonnée pic falaise pente neige refuge versant".split()),
("fleur","nature","pétale bouquet rose tulipe jardin pollen tige parfum floraison bourgeon jardinier".split()),
("rivière","nature","ruisseau fleuve eau courant berge cascade lac pont affluent torrent rive".split()),
("océan","mer","mer vague marée abysse profondeur large atlantique pacifique houle immensité marin".split()),
("plage","mer","sable mer serviette parasol baignade côte littoral vague coquillage bronzage crème".split()),
("bateau","mer","navire voile capitaine port matelot ancre quai coque voilier paquebot mât équipage".split()),
("phare","mer","côte falaise lumière gardien navire signal port balise nuit tempête".split()),
("musique","musique","mélodie chanson rythme note concert instrument harmonie compositeur partition orchestre".split()),
("guitare","musique","corde instrument guitariste accord riff acoustique électrique médiator manche rock concert".split()),
("concert","musique","scène public orchestre spectacle salle applaudissement tournée festival billet musicien".split()),
("piano","musique","clavier touche pianiste partition note instrument concerto mélodie queue droit".split()),
("football","sport","ballon but gardien match équipe stade penalty joueur arbitre championnat dribble crampon".split()),
("course","sport","coureur sprint marathon vitesse piste dossard chronomètre foulée départ arrivée endurance".split()),
("piscine","sport","natation bassin nageur eau plongeoir maillot brasse crawl chlore longueur".split()),
("vélo","sport","cyclisme pédale roue guidon selle cycliste casque peloton vtt chaîne tour".split()),
("école","ecole","élève professeur classe cours leçon cartable récréation maître apprentissage cantine pupitre".split()),
("livre","ecole","lecture page roman bibliothèque auteur chapitre lecteur librairie couverture récit écrivain".split()),
("examen","ecole","épreuve note copie révision diplôme correction concours réussite échec candidat bac".split()),
("voyage","voyage","destination valise touriste aventure départ itinéraire découverte séjour périple bagage excursion".split()),
("avion","voyage","aéroport vol pilote décollage atterrissage hublot aile passager escale réacteur hélice".split()),
("train","voyage","gare wagon rail locomotive quai voie cheminot tgv compartiment contrôleur billet".split()),
("hôtel","voyage","chambre réservation réception hall séjour nuitée auberge palace concierge étoile".split()),
("maison","maison","toit mur foyer habitation demeure logement villa pavillon domicile façade résidence".split()),
("jardin","maison","potager pelouse fleur jardinier arrosoir tonte haie plate-bande gazon clôture serre".split()),
("chambre","maison","lit oreiller matelas couette sommeil armoire chevet drap dormir literie".split()),
("chien","animaux","aboiement niche chiot laisse croquette patte museau fidèle maître truffe berger labrador".split()),
("chat","animaux","miaulement félin chaton litière griffe ronronnement moustache coussinet matou poil".split()),
("cheval","animaux","galop écurie jument poulain crinière selle cavalier équitation sabot hennissement poney".split()),
("cœur","corps","battement sang artère cardiaque poumon organe pouls poitrine circulation veine".split()),
("médecin","corps","docteur patient ordonnance consultation hôpital diagnostic stéthoscope généraliste cabinet infirmier soin".split()),
("sommeil","corps","dormir rêve nuit fatigue sieste insomnie réveil lit oreiller repos somnolence berceuse".split()),
("ville","ville","rue quartier urbain habitant centre métropole agglomération avenue cité commune capitale".split()),
("marché","ville","étal marchand stand primeur halle vendeur panier forain commerce achat".split()),
("pont","ville","rivière arche traversée pilier viaduc passerelle fleuve enjambée suspendu tablier".split()),
("pluie","meteo","averse goutte parapluie nuage ondée crachin orage déluge flaque imperméable bruine".split()),
("soleil","meteo","rayon lumière chaleur été astre éclat midi bronzage lever coucher canicule".split()),
("neige","meteo","flocon hiver blanc luge bonhomme poudreuse congère ski froid igloo verglas".split()),
("travail","travail","emploi métier bureau salaire tâche profession boulot labeur mission carrière entreprise".split()),
("bureau","travail","réunion dossier collègue ordinateur secrétaire open-space classeur agrafeuse travail étage".split()),
("salaire","travail","paie rémunération prime fiche revenu smic augmentation virement mensuel brut net".split()),
("fête","fete","célébration anniversaire invité joie banquet réjouissance festivité bal cotillon ambiance".split()),
("cadeau","fete","paquet surprise ruban emballage offrir anniversaire noël étrenne présent papier".split()),
("ordinateur","tech","clavier écran souris processeur logiciel informatique pc portable mémoire disque programme".split()),
("téléphone","tech","smartphone appel sonnerie sms numéro mobile combiné répondeur forfait allô".split()),
("peinture","art","tableau pinceau toile peintre couleur palette chevalet aquarelle fresque vernis huile".split()),
]

RELATED = {
"cuisine":["fete","maison","argent"], "nature":["meteo","animaux","mer"], "mer":["voyage","nature","meteo"],
"musique":["art","fete"], "sport":["corps","ecole"], "ecole":["travail","temps","art"],
"voyage":["ville","mer","temps"], "maison":["ville","vetements","cuisine"], "animaux":["nature","maison"],
"corps":["sport","vetements"], "ville":["voyage","travail","argent"], "meteo":["nature","temps"],
"travail":["argent","tech","ecole"], "fete":["musique","cuisine","amour"], "vetements":["maison","art"],
"tech":["travail","temps"], "argent":["travail","ville"], "art":["musique","amour"],
"amour":["fete","art"], "temps":["meteo","ecole"],
}

def h(s):
    return int(hashlib.md5(s.encode("utf-8")).hexdigest()[:8], 16)

def build(secret, theme, core):
    used = {secret}
    neigh = []
    # 1) cœur curé : 0.97 -> 0.86
    for i, w in enumerate(core):
        if w in used: continue
        used.add(w)
        neigh.append((w, round(0.97 - i * (0.11 / max(1, len(core)-1)), 4)))
    # 2) reste du thème : 0.85 -> 0.55
    rest = sorted([w for w in THEMES[theme] if w not in used], key=lambda w: h(secret + w))
    for i, w in enumerate(rest):
        used.add(w)
        neigh.append((w, round(0.85 - i * (0.30 / max(1, len(rest))), 4)))
    # 3) thèmes liés : 0.54 -> 0.35
    rel = []
    for t in RELATED[theme]:
        rel += [w for w in THEMES[t] if w not in used]
    rel = sorted(set(rel), key=lambda w: h(secret + w))
    for i, w in enumerate(rel):
        used.add(w)
        neigh.append((w, round(0.54 - i * (0.19 / max(1, len(rel))), 4)))
    # 4) remplissage pool global + autres thèmes : 0.34 -> 0.15, jusqu'à 420
    fill = [w for w in GLOBAL_POOL if w not in used]
    for t in THEMES:
        if t != theme and t not in RELATED[theme]:
            fill += [w for w in THEMES[t] if w not in used]
    fill = sorted(set(fill), key=lambda w: h(secret + w))
    room = 420 - len(neigh)
    for i, w in enumerate(fill[:room]):
        used.add(w)
        neigh.append((w, round(0.34 - i * (0.19 / max(1, room)), 4)))
    return neigh

def main():
    secrets_out = []
    vocab = set(GLOBAL_POOL)
    for ws in THEMES.values():
        vocab.update(ws)
    for secret, theme, core in SECRETS:
        vocab.add(secret)
        vocab.update(core)
        n = build(secret, theme, core)
        assert len(n) >= 300, f"{secret}: {len(n)} voisins"
        secrets_out.append({"word": secret, "theme": theme, "neighbors": [[w, s] for w, s in n]})
    pack = {"version": 1, "language": "fr", "secrets": secrets_out, "dictionary": sorted(vocab)}
    out = json.dumps(pack, ensure_ascii=False, separators=(",", ":"))
    here = os.path.dirname(os.path.abspath(__file__))
    targets = [
        os.path.join(here, "semantic_pack_demo.json"),
        os.path.join(here, "..", "backend", "semantic_pack_demo.json"),
        os.path.join(here, "..", "android-app", "app", "src", "main", "assets", "semantic_pack_demo.json"),
    ]
    for t in targets:
        os.makedirs(os.path.dirname(t), exist_ok=True)
        with open(t, "w", encoding="utf-8") as f:
            f.write(out)
        print("écrit:", os.path.normpath(t))
    print(f"{len(secrets_out)} secrets, ~{len(secrets_out[0]['neighbors'])} voisins/secret, dico={len(vocab)} mots")

if __name__ == "__main__":
    main()
