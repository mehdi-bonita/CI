package org.bonitasoft.demandeachat;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import com.company.model.Voiture;
import com.company.model.VoitureAssert;

public class IntegrationTest {

	private static final String NOM_VARIABLE_METIER_DEMANDE_ACHAT = "voiture";

	private static final String PROCESSUS_ACHAT_VOITURE = "AjouterVoiture";

	private static final String PROCESSUS_SUPPRESSION_DONNEES_METIER = "Suppression demande achat";

	private static final String PROCESSES_VERSION = "1.0";

	private static final String ETAPE_VALIDATION = "Validation";

	private static final String ETAPE_VALIDEE = "Demande validée";
	
	private static final String ETAPE_REFUS = "Demande refusée";

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
	public void testApprobation() throws Exception {
		// Creation de l'instance de processus de demande d'achat
		long nouvelleDemandeAchatProcessInstanceId = ProcessExecutionDriver
				.createProcessInstance(PROCESSUS_ACHAT_VOITURE, PROCESSES_VERSION, nouvelleDemandeAchatInputs());

		// Attente et exécution de l'étape "Validation"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_VALIDATION, demandeAchatApprouveeInputs(), session.getUserId());
		
		// Attente et exécution de l'étape "Demande Validée"
				BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
						ETAPE_VALIDEE, null, session.getUserId());

		// Vérification que l'instance de processus est terminée
		BonitaBPMAssert.assertProcessInstanceIsFinished(nouvelleDemandeAchatProcessInstanceId);

		// Vérifie le statut de la demande d'achat de voiture
		Voiture demandeAchat = BonitaBPMAssert.assertBusinessDataNotNull(Voiture.class,
				nouvelleDemandeAchatProcessInstanceId, NOM_VARIABLE_METIER_DEMANDE_ACHAT);
		VoitureAssert.assertThat(demandeAchat).hasModele("Ferrari");
		VoitureAssert.assertThat(demandeAchat).hasPrix(Integer.valueOf(123));
		VoitureAssert.assertThat(demandeAchat).hasStatut("Validation");
	}

	private Map<String, Serializable> nouvelleDemandeAchatInputs() {
		HashMap<String, Serializable> voitureInput = new HashMap<String, Serializable>();

		voitureInput.put("modele", "Ferrari");
		voitureInput.put("dateAchat", new Date());
		voitureInput.put("prix", Integer.valueOf(123));

		Map<String, Serializable> nouvelleDemandeAchatInputs = new HashMap<String, Serializable>();

		nouvelleDemandeAchatInputs.put("demandeAchatInput", voitureInput);

		return nouvelleDemandeAchatInputs;
	}

	private Map<String, Serializable> demandeAchatApprouveeInputs() {
		Map<String, Serializable> demandeAchatApprouveeInputs = new HashMap<String, Serializable>();

		demandeAchatApprouveeInputs.put("statut", "Validation");

		return demandeAchatApprouveeInputs;
	}

	@Test
	public void testRejetN1() throws Exception {
		// Creation de l'instance de processus de demande d'achat
		long nouvelleDemandeAchatProcessInstanceId = ProcessExecutionDriver
				.createProcessInstance(PROCESSUS_ACHAT_VOITURE, PROCESSES_VERSION, nouvelleDemandeAchatInputs());

		// Attente et exécution de l'étape "Validation du reponsable"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_VALIDATION, demandeAchatRejeteeInputs(), session.getUserId());
		
		// Attente et exécution de l'étape "Demande Validée"
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(nouvelleDemandeAchatProcessInstanceId,
				ETAPE_REFUS, null, session.getUserId());

		// Vérification que l'instance de processus est terminée
		BonitaBPMAssert.assertProcessInstanceIsFinished(nouvelleDemandeAchatProcessInstanceId);

		// Vérifie le statut de la demande d'achat
		Voiture demandeAchat = BonitaBPMAssert.assertBusinessDataNotNull(Voiture.class,
				nouvelleDemandeAchatProcessInstanceId, NOM_VARIABLE_METIER_DEMANDE_ACHAT);
		VoitureAssert.assertThat(demandeAchat).hasModele("Ferrari");
		VoitureAssert.assertThat(demandeAchat).hasPrix(Integer.valueOf(123));
		VoitureAssert.assertThat(demandeAchat).hasStatut("Refus");
	}

	private Map<String, Serializable> demandeAchatRejeteeInputs() {
		Map<String, Serializable> demandeAchatApprouveeInputs = new HashMap<String, Serializable>();

		demandeAchatApprouveeInputs.put("statut", "Refus");

		return demandeAchatApprouveeInputs;
	}
}
