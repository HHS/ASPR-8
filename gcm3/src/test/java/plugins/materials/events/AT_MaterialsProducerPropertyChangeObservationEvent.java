package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = MaterialsProducerPropertyChangeObservationEvent.class)
public class AT_MaterialsProducerPropertyChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent = new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerPropertyId, materialsProducerPropertyChangeObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent = new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerId, materialsProducerPropertyChangeObservationEvent.getMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyId", args = {})
	public void testGetMaterialsProducerPropertyId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent = new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerPropertyId, materialsProducerPropertyChangeObservationEvent.getMaterialsProducerPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGgetPreviousPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent = new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(previousPropertyValue, materialsProducerPropertyChangeObservationEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent = new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(currentPropertyValue, materialsProducerPropertyChangeObservationEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent = new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		
		String expectedValue = "MaterialsProducerPropertyChangeObservationEvent [materialsProducerId=MATERIALS_PRODUCER_3, materialsProducerPropertyId=MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, previousPropertyValue=896.5, currentPropertyValue=3762.87]";
		String actualValue = materialsProducerPropertyChangeObservationEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByMaterialsProducerAndProperty", args = { SimulationContext.class, MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetEventLabelByMaterialsProducerAndProperty() {
		MaterialsActionSupport.testConsumer(6182040571479306522L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					EventLabel<MaterialsProducerPropertyChangeObservationEvent> eventLabel = MaterialsProducerPropertyChangeObservationEvent.getEventLabelByMaterialsProducerAndProperty(c,
							testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(MaterialsProducerPropertyChangeObservationEvent.class, eventLabel.getEventClass());
					assertEquals(testMaterialsProducerPropertyId, eventLabel.getPrimaryKeyValue());
					assertEquals(MaterialsProducerPropertyChangeObservationEvent.getEventLabelerForMaterialsProducerAndProperty().getId(), eventLabel.getLabelerId());
				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForMaterialsProducerAndProperty", args = {})
	public void testGetEventLabelerForMaterialsProducerAndProperty() {

		MaterialsActionSupport.testConsumer(3114721828344288343L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<MaterialsProducerPropertyChangeObservationEvent> eventLabeler = MaterialsProducerPropertyChangeObservationEvent.getEventLabelerForMaterialsProducerAndProperty();
			assertEquals(MaterialsProducerPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					assertEquals(MaterialsProducerPropertyChangeObservationEvent.getEventLabelByMaterialsProducerAndProperty(c, testMaterialsProducerId,testMaterialsProducerPropertyId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					MaterialsProducerPropertyChangeObservationEvent event = new MaterialsProducerPropertyChangeObservationEvent(testMaterialsProducerId, testMaterialsProducerPropertyId, 45,72);

					// derive the expected event label for this event
					EventLabel<MaterialsProducerPropertyChangeObservationEvent> expectedEventLabel = MaterialsProducerPropertyChangeObservationEvent.getEventLabelByMaterialsProducerAndProperty(c,
							testMaterialsProducerId,testMaterialsProducerPropertyId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<MaterialsProducerPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

}
