package org.springframework.xd.analytics.linguistics.langdetect;

/**
 * @author Thomas Darimont
 */
public interface Texts {

	String LONG_ENGLISH_1 = "This document describes the runtime architecture of Spring XD and the core components and used for processing data. Use the sidebar to navigate the various sections of the documentation. The documentation on the wiki reflects the state of the master branch and the latest released version it applies to is 1.1.0.RELEASE";
	String LONG_GERMAN_1 = "Wie jede Blüte welkt und jede Jugend  Dem Alter weicht, blüht jede Lebensstufe, Blüht jede Weisheit auch und jede Tugend  Zu ihrer Zeit und darf nicht ewig dauern. Es muß das Herz bei jedem Lebensrufe  Bereit zum Abschied sein und Neubeginne,  Um sich in Tapferkeit und ohne Trauern  In andre, neue Bindungen zu geben. Und jedem Anfang wohnt ein Zauber inne, Der uns beschützt und der uns hilft, zu leben.";
	String LONG_ITALIAN_1 = "Fondata secondo la tradizione il 21 aprile 753 a.C. (sebbene scavi recenti nel Lapis niger farebbero risalire la fondazione a 2 secoli prima[11][12]), nel corso dei suoi tre millenni di storia è stata la prima grande metropoli dell'umanità[13], cuore di una delle più importanti civiltà antiche, che influenzò la società, la cultura, la lingua, la letteratura, l'arte, l'architettura, la filosofia, la religione, il diritto e i costumi dei secoli successivi. Luogo di origine della lingua latina, fu capitale dell'Impero romano, che estendeva il suo dominio su tutto il bacino del Mediterraneo e gran parte dell'Europa, dello Stato Pontificio, sottoposto al potere temporale dei papi, e del Regno d'Italia (dal 1871).";

	String SHORT_ENGLISH_1 = "Hello World";
	String SHORT_GERMAN_1 = "Hallo Welt";
	String SHORT_ITALIAN_1 = "Santo maccheroni";
	String SHORT_ENGLISH_FRENCH_MIX_1 = "Bonjour Howdy";

	String SHORT_ENGLISH_2 = "Then all detections for the same document return the same language and probability.";
}
