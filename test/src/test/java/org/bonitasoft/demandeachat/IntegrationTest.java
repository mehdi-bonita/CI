package org.bonitasoft.demandeachat;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.DocumentAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.ut.tooling.BonitaBPMAssert;
import com.bonitasoft.ut.tooling.ProcessExecutionDriver;
import com.bonitasoft.ut.tooling.Server;
import com.company.model.DemandeAchat;
import com.company.model.DemandeAchatAssert;

public class IntegrationTest {

	private static final String CHEMIN_VERS_FICHIER_PDF_TEST = "src/test/resources/test.pdf";

	private static final String NOM_FICHIER_PDF_TEST = "test.pdf";

	private static final String NOM_DOCUMENT = "demandeAchatPDF";

	private static final String NOM_VARIABLE_METIER_DEMANDE_ACHAT = "demandeAchat";

	private static final String PROCESSUS_DEMANDE_ACHAT = "Validation demande achat";

	private static final String PROCESSUS_SUPPRESSION_DONNEES_METIER = "Suppression demande achat";

	private static final String PROCESSES_VERSION = "7.4.2-0.0.1";

	private static final String ETAPE_VALIDATION_NIVEAU_1 = "Validation niveau 1";

	private static final String ETAPE_VALIDATION_NIVEAU_2 = "Validation niveau 2";

	private static final String PDF_MIME_TYPE = "application/pdf";

	private static APISession session;

	private static ProcessAPI processAPI;

	@BeforeClass
	public static void setUpClass() throws Exception {
		session = Server.httpConnect();
		processAPI = TenantAPIAccessor.getProcessAPI(session);

		BonitaBPMAssert.setUp(session, processAPI);
		ProcessExecutionDriver.setUp(processAPI);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		// Run a process to remove all business data
		ProcessExecutionDriver.createProcessInstance(PROCESSUS_SUPPRESSION_DONNEES_METIER, PROCESSES_VERSION);

		BonitaBPMAssert.tearDown();
		Server.logout(session);
	}

	@Before
	public void setUp() throws Exception {
		ProcessExecutionDriver.prepareServer();
	}

	// @After
	// public void tearDown() throws Exception {
	//
	// }

	@Test
	public void testApprobationN1() throws Exception {
		// Creation de l'instance de processus de demande d'achat
		long nouvelleDemandeAchatProcessInstanceId = ProcessExecutionDriver
				.createProcessInstance(PROCESSUS_DEMANDE_ACHAT, PROCESSES_VERSION, nouvelleDemandeAchatInputs());

		// Ajout du fichier attaché à la demande
		DocumentAPI documentAPI = TenantAPIAccessor.getProcessAPI(session);
		Path cheminVersFichierTest = Paths.get(CHEMIN_VERS_FICHIER_PDF_TEST);
		byte[] contenuFichierTest = Files.readAllBytes(cheminVersFichierTest);
		documentAPI.attachDocument(nouvelleDemandeAchatProcessInstanceId, NOM_DOCUMENT, NOM_FICHIER_PDF_TEST,
				PDF_MIME_TYPE, contenuFichierTest);

		// Attente et exécution de l'étape "Validation niveau 1"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_VALIDATION_NIVEAU_1, demandeAchatApprouveeInputs(), session.getUserId());

		// Vérification que l'instance de processus est terminée
		BonitaBPMAssert.assertProcessInstanceIsFinished(nouvelleDemandeAchatProcessInstanceId);

		// Vérifie le statut de la demande d'achat
		DemandeAchat demandeAchat = BonitaBPMAssert.assertBusinessDataNotNull(DemandeAchat.class,
				nouvelleDemandeAchatProcessInstanceId, NOM_VARIABLE_METIER_DEMANDE_ACHAT);
		DemandeAchatAssert.assertThat(demandeAchat).hasStatut(StatutDemandeAchat.TERMINEE_APPROUVEE_NIVEAU_1.statut);
	}

	private Map<String, Serializable> nouvelleDemandeAchatInputs() {
		HashMap<String, Serializable> demandeAchatInputs = new HashMap<String, Serializable>();

		demandeAchatInputs.put("idDemandeAchat", Integer.valueOf(1));
		demandeAchatInputs.put("description", "test demande approuvée niveau 1");
		demandeAchatInputs.put("courriel", "antoine.mottier@bonitasoft.com");
		demandeAchatInputs.put("service", "RH");
		demandeAchatInputs.put("montant", Double.valueOf(123.45));

		Map<String, Serializable> nouvelleDemandeAchatInputs = new HashMap<>();

		nouvelleDemandeAchatInputs.put("demandeAchatInput", demandeAchatInputs);
		nouvelleDemandeAchatInputs.put("demandeAchatPDFInput", null);

		return nouvelleDemandeAchatInputs;
	}

	private Map<String, Serializable> demandeAchatApprouveeInputs() {
		Map<String, Serializable> demandeAchatApprouveeInputs = new HashMap<String, Serializable>();

		demandeAchatApprouveeInputs.put("statut", ChoixApprobateur.APPROUVEE.choix);

		return demandeAchatApprouveeInputs;
	}

	@Test
	public void testRejetN1() throws Exception {
		// Creation de l'instance de processus de demande d'achat
		long nouvelleDemandeAchatProcessInstanceId = ProcessExecutionDriver
				.createProcessInstance(PROCESSUS_DEMANDE_ACHAT, PROCESSES_VERSION, nouvelleDemandeAchatInputs());

		// Ajout du fichier attaché à la demande
		DocumentAPI documentAPI = TenantAPIAccessor.getProcessAPI(session);
		Path cheminVersFichierTest = Paths.get(CHEMIN_VERS_FICHIER_PDF_TEST);
		byte[] contenuFichierTest = Files.readAllBytes(cheminVersFichierTest);
		documentAPI.attachDocument(nouvelleDemandeAchatProcessInstanceId, NOM_DOCUMENT, NOM_FICHIER_PDF_TEST,
				PDF_MIME_TYPE, contenuFichierTest);

		// Attente et exécution de l'étape "Validation du reponsable"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_VALIDATION_NIVEAU_1, demandeAchatRejeteeInputs(), session.getUserId());

		// Vérification que l'instance de processus est terminée
		BonitaBPMAssert.assertProcessInstanceIsFinished(nouvelleDemandeAchatProcessInstanceId);

		// Vérifie le statut de la demande d'achat
		DemandeAchat demandeAchat = BonitaBPMAssert.assertBusinessDataNotNull(DemandeAchat.class,
				nouvelleDemandeAchatProcessInstanceId, NOM_VARIABLE_METIER_DEMANDE_ACHAT);
		DemandeAchatAssert.assertThat(demandeAchat).hasStatut(StatutDemandeAchat.TERMINEE_REJETEE_NIVEAU_1.statut);
	}

	private Map<String, Serializable> demandeAchatRejeteeInputs() {
		Map<String, Serializable> demandeAchatApprouveeInputs = new HashMap<String, Serializable>();

		demandeAchatApprouveeInputs.put("statut", ChoixApprobateur.REJETEE.choix);

		return demandeAchatApprouveeInputs;
	}

	@Test
	public void testApprobationN2() throws Exception {
		// Creation de l'instance de processus de demande d'achat
		long nouvelleDemandeAchatProcessInstanceId = ProcessExecutionDriver
				.createProcessInstance(PROCESSUS_DEMANDE_ACHAT, PROCESSES_VERSION, nouvelleDemandeAchatNiveau2Inputs());

		// Ajout du fichier attaché à la demande
		DocumentAPI documentAPI = TenantAPIAccessor.getProcessAPI(session);
		Path cheminVersFichierTest = Paths.get(CHEMIN_VERS_FICHIER_PDF_TEST);
		byte[] contenuFichierTest = Files.readAllBytes(cheminVersFichierTest);
		documentAPI.attachDocument(nouvelleDemandeAchatProcessInstanceId, NOM_DOCUMENT, NOM_FICHIER_PDF_TEST,
				PDF_MIME_TYPE, contenuFichierTest);

		// Attente et exécution de l'étape "Validation niveau 1"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_VALIDATION_NIVEAU_1, demandeAchatApprouveeInputs(), session.getUserId());

		// Attente et exécution de l'étape "Validation niveau 2"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_VALIDATION_NIVEAU_2, demandeAchatApprouveeInputs(), session.getUserId());

		// Vérification que l'instance de processus est terminée
		BonitaBPMAssert.assertProcessInstanceIsFinished(nouvelleDemandeAchatProcessInstanceId);

		// Vérifie le statut de la demande d'achat
		DemandeAchat demandeAchat = BonitaBPMAssert.assertBusinessDataNotNull(DemandeAchat.class,
				nouvelleDemandeAchatProcessInstanceId, NOM_VARIABLE_METIER_DEMANDE_ACHAT);
		DemandeAchatAssert.assertThat(demandeAchat).hasStatut(StatutDemandeAchat.TERMINEE_APPROUVEE_NIVEAU_2.statut);
	}

	private Map<String, Serializable> nouvelleDemandeAchatNiveau2Inputs() {
		HashMap<String, Serializable> demandeAchatInputs = new HashMap<String, Serializable>();

		demandeAchatInputs.put("idDemandeAchat", Integer.valueOf(1));
		demandeAchatInputs.put("description", "test demande approuvée niveau 2");
		demandeAchatInputs.put("courriel", "antoine.mottier@bonitasoft.com");
		demandeAchatInputs.put("service", "Marketing");
		demandeAchatInputs.put("montant", Double.valueOf(123456.78));

		Map<String, Serializable> nouvelleDemandeAchatInputs = new HashMap<>();

		nouvelleDemandeAchatInputs.put("demandeAchatInput", demandeAchatInputs);
		nouvelleDemandeAchatInputs.put("demandeAchatPDFInput", null);

		return nouvelleDemandeAchatInputs;
	}

	enum ChoixApprobateur {
		APPROUVEE("Approuvée"), REJETEE("Rejetée");

		private String choix;

		private ChoixApprobateur(String choix) {
			this.choix = choix;
		}

		public String getChoix() {
			return choix;
		}
	}

	enum StatutDemandeAchat {
		NOUVELLE_DEMANDE("Nouvelle demande"), ATTENTE_VALIDATION_N1("En attente validation niveau 1"), APPROUVEE_N1(
				"Approuvée par niveau 1"), REJETEE_N1("Rejetée par niveau 1"), ATTENTE_VALIDATION_N2(
						"En attente validation niveau 2"), APPROUVEE_N2("Approuvée par niveau 2"), REJETEE_N2(
								"Rejetée par niveau 2"), TERMINEE_APPROUVEE_NIVEAU_1(
										"Approuvée par niveau 1 - Fermée"), TERMINEE_REJETEE_NIVEAU_1(
												"Rejetée par niveau 1 - Fermée"), TERMINEE_APPROUVEE_NIVEAU_2(
														"Approuvée par niveau 2 - Fermée"), TERMINEE_REJETEE_NIVEAU_2(
																"Rehetée par niveau 2 - Fermée");

		private String statut;

		private StatutDemandeAchat(String statut) {
			this.statut = statut;
		}

		public String getStatut() {
			return statut;
		}

	}
}
