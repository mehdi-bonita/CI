package org.mycompany.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public abstract class AbstractSommeConnectorImpl extends AbstractConnector {

	protected final static String ENTIER1_INPUT_PARAMETER = "entier1";
	protected final static String ENTIER2_INPUT_PARAMETER = "entier2";
	protected final String SOMME_OUTPUT_PARAMETER = "somme";

	protected final java.lang.Integer getEntier1() {
		return (java.lang.Integer) getInputParameter(ENTIER1_INPUT_PARAMETER);
	}

	protected final java.lang.Integer getEntier2() {
		return (java.lang.Integer) getInputParameter(ENTIER2_INPUT_PARAMETER);
	}

	protected final void setSomme(java.lang.Integer somme) {
		setOutputParameter(SOMME_OUTPUT_PARAMETER, somme);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			getEntier1();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("entier1 type is invalid");
		}
		try {
			getEntier2();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("entier2 type is invalid");
		}

	}

}
