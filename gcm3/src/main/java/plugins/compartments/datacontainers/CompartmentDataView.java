package plugins.compartments.datacontainers;

import java.util.Set;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;

/**
 * Published data view that provides compartment property information.
 * 
 * @author Shawn Hatch
 *
 */
public final class CompartmentDataView implements DataView {

	private final CompartmentDataManager compartmentDataManager;
	private Context context;

	/**
	 * Creates the Compartment Data View from the given {@link Context} and
	 * {@link CompartmentDataManager}. Not null tolerant.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_DATA_MANAGER}
	 *             if compartment data manager is null</li>
	 * 
	 */
	public CompartmentDataView(Context context, CompartmentDataManager compartmentDataManager) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (compartmentDataManager == null) {
			throw new ContractException(CompartmentError.NULL_COMPARTMENT_DATA_MANAGER);
		}

		this.context = context;
		this.compartmentDataManager = compartmentDataManager;
	}

	/**
	 * Returns the property definition for the given {@link CompartmentId} and
	 * {@link CompartmentPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is not associated with the
	 *             compartment</li>
	 * 
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		validateCompartmentId(compartmentId);
		validateCompartmentProperty(compartmentId, compartmentPropertyId);
		return compartmentDataManager.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns the {@link CompartmentPropertyId} values for the given
	 * {@link CompartmentId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 */
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId) {
		validateCompartmentId(compartmentId);
		return compartmentDataManager.getCompartmentPropertyIds(compartmentId);
	}

	/**
	 * Returns the value of the compartment property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is not associated with the
	 *             compartment</li>
	 */
	public <T> T getCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		validateCompartmentId(compartmentId);
		validateCompartmentProperty(compartmentId, compartmentPropertyId);
		return compartmentDataManager.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns the time when the of the compartment property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is not associated with the
	 *             compartment</li>
	 */

	public double getCompartmentPropertyTime(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		validateCompartmentId(compartmentId);
		validateCompartmentProperty(compartmentId, compartmentPropertyId);
		return compartmentDataManager.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns the set of {@link CompartmentId} values that are defined by the
	 * {@link CompartmentInitialData}.
	 */
	public <T extends CompartmentId> Set<T> getCompartmentIds() {
		return compartmentDataManager.getCompartmentIds();
	}

	public boolean compartmentIdExists(CompartmentId compartmentId) {
		return compartmentDataManager.compartmentIdExists(compartmentId);
	}

	private void validateCompartmentId(final CompartmentId compartmentId) {
		if (compartmentId == null) {
			context.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}

		if (!compartmentDataManager.compartmentIdExists(compartmentId)) {
			context.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private void validateCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {

		if (compartmentPropertyId == null) {
			context.throwContractException(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID);
		}

		if (!compartmentDataManager.compartmentPropertyIdExists(compartmentId, compartmentPropertyId)) {
			context.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
		}
	}
}
