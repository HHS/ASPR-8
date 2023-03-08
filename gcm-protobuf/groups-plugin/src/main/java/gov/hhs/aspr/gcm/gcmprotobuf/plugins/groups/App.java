package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.gcmprotobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.gcmprotobuf.people.PeoplePluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundle;
import nucleus.PluginData;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.GroupsPluginData;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;

public class App {
    public JsonObject deepMerge(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            JsonElement value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.add(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value.isJsonObject()) {
                    JsonObject valueJson = value.getAsJsonObject();
                    deepMerge(valueJson, target.getAsJsonObject(key));
                } else if (value.isJsonArray()) {
                    JsonArray valueArray = value.getAsJsonArray();
                    JsonArray targetArray = target.getAsJsonArray(key);
                    targetArray.addAll(valueArray);
                } else {
                    target.add(key, value);
                }
            }
        }
        return target;
    }

    public static void main(String[] args) {

        String inputFileName = "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\groups-plugin\\src\\main\\resources\\json\\testJson1.json";
        String outputFileName = "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\groups-plugin\\src\\main\\resources\\json\\output\\testJson1Output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(GroupsPluginBundle.getPluginBundle(inputFileName, outputFileName))
                .addBundle(PropertiesPluginBundle.getPluginBundle())
                .addBundle(PeoplePluginBundle.getPluginBundle())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GroupsPluginData pluginData = (GroupsPluginData) pluginDatas.get(0);

        // List<PersonId> people = new ArrayList<>();
        // for(int i = 0; i < 100; i++) {
        //     people.add(new PersonId(i));
        // }
        // GroupsPluginData groupsPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(5, 100, people, 524805676405822016L);

        // System.out.println(pluginData.getGlobalPropertyIds());
        // for (GlobalPropertyId id : pluginData.getGlobalPropertyIds()) {
        //     PropertyDefinition propertyDefinition = pluginData.getGlobalPropertyDefinition(id);
        //     Object value = pluginData.getGlobalPropertyValue(id);
        //     System.out.println(propertyDefinition);
        //     System.out.println(value);

        // }

        translatorController.writeOutput();
    }
}
