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

@UnitTest(target = MaterialsProducerPropertyUpdateEvent.class)
public class AT_MaterialsProducerPropertyUpdateEvent {

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
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerPropertyId, materialsProducerPropertyUpdateEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerId, materialsProducerPropertyUpdateEvent.getMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyId", args = {})
	public void testGetMaterialsProducerPropertyId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerPropertyId, materialsProducerPropertyUpdateEvent.getMaterialsProducerPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGgetPreviousPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(previousPropertyValue, materialsProducerPropertyUpdateEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(currentPropertyValue, materialsProducerPropertyUpdateEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		
		String expectedValue = "MaterialsProducerPropertyUpdateEvent [materialsProducerId=MATERIALS_PRODUCER_3, materialsProducerPropertyId=MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, previousPropertyValue=896.5, currentPropertyValue=3762.87]";
		String actualValue = materialsProducerPropertyUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByMaterialsProducerAndProperty", args = { SimulationContext.class, MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetEventLabelByMaterialsProducerAndProperty() {
		MaterialsActionSupport.testConsumer(6182040571479306522L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					EventLabel<MaterialsProducerPropertyUpdateEvent> eventLabel = MaterialsProducerPropertyUpdateEvent.getEventLabelByMaterialsProducerAndProperty(c,
							testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(MaterialsProducerPropertyUpdateEvent.class, eventLabel.getEventClass());
					assertEquals(testMaterialsProducerPropertyId, eventLabel.getPrimaryKeyValue());
					assertEquals(MaterialsProducerPropertyUpdateEvent.getEventLabelerForMaterialsProducerAndProperty().getEventLabelerId(), eventLabel.getLabelerId());
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
			EventLabeler<MaterialsProducerPropertyUpdateEvent> eventLabeler = MaterialsProducerPropertyUpdateEvent.getEventLabelerForMaterialsProducerAndProperty();
			assertEquals(MaterialsProducerPropertyUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					assertEquals(MaterialsProducerPropertyUpdateEvent.getEventLabelByMaterialsProducerAndProperty(c, testMaterialsProducerId,testMaterialsProducerPropertyId).getLabelerId(), eventLabeler.getEventLabelerId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					MaterialsProducerPropertyUpdateEvent event = new MaterialsProducerPropertyUpdateEvent(testMaterialsProducerId, testMaterialsProducerPropertyId, 45,72);

					// derive the expected event label for this event
					EventLabel<MaterialsProducerPropertyUpdateEvent> expectedEventLabel = MaterialsProducerPropertyUpdateEvent.getEventLabelByMaterialsProducerAndProperty(c,
							testMaterialsProducerId,testMaterialsProducerPropertyId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<MaterialsProducerPropertyUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

}
