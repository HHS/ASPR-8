package plugins.people;

import java.util.ArrayList;
import java.util.List;

import nucleus.DataManagerContext;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;

public class Junk {
	public static void main(String[] args) {
		
		PeoplePluginData peoplePluginData = PeoplePluginData.builder()//
		.addPersonRange(new PersonRange(12, 25))//
		.addPersonRange(new PersonRange(6, 17))//
		.addPersonRange(new PersonRange(13, 14))//
		.addPersonRange(new PersonRange(22, 30))//
		.addPersonRange(new PersonRange(36, 47))//
		.setPersonCount(50)//
		.build();
		
		System.out.println(peoplePluginData);
		
		List<PersonId> personIds = peoplePluginData.getPersonIds();
		List<PersonId> expandedPeople = getExpandedPeople(personIds, peoplePluginData.getPersonCount());
		//System.out.println(personIds);
		PeoplePluginData peoplePluginData2 = getPeoplePluginData(expandedPeople);
		System.out.println(peoplePluginData2);

	}
	
	private static List<PersonId> getExpandedPeople(List<PersonId> people, int personCount){
		List<PersonId> result = new ArrayList<>(personCount);
		for(int i = 0;i<personCount;i++) {
			result.add(null);
		}
		for(PersonId personId : people) {
			result.set(personId.getValue(), personId);
		}
		return result;
	}
	
	private static PeoplePluginData getPeoplePluginData(List<PersonId> personIds) {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		builder.setPersonCount(personIds.size());

		int a = -1;
		int b = -1;
		PersonId lastPersonId = null;
		for (int i = 0; i < personIds.size(); i++) {
			PersonId personId = personIds.get(i);
			if (personId != null) {
				if (lastPersonId == null) {
					a = i;
					b = i;
				} else {
					b = i;
				}
			}else {
				if(lastPersonId != null) {
					builder.addPersonRange(new PersonRange(a, b));
				}
			}
			lastPersonId = personId;
		}
		return builder.build();
	}
}
