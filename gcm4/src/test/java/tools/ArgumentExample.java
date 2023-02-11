package tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ArgumentExample {

	private ArgumentExample() {
	}
	
	private static enum Command{
		PACKAGE_NAME("-p"),
		DIRECTORY_NAME("-d");
		private final String commandString;
		private Command(String commandString){
			this.commandString = commandString;
		}
		public static Command getCommand(String value) {
			for(Command command : Command.values()) {
				if(command.commandString.equals(value)) {
					return command;
				}
			}
			return null;
		}
	}

	public static void main(String[] args) {
		
		Map<Command,List<String>> commandMap = new LinkedHashMap<>();
		for(Command command : Command.values()) {
			commandMap.put(command, new ArrayList<>());
		}

		Command currentCommand = null;
		for(String arg : args) {
			Command command = Command.getCommand(arg);
			if(command != null) {
				currentCommand = command;
			}else {
				if(currentCommand != null) {
					commandMap.get(currentCommand).add(arg);
				}else {
					throw new IllegalArgumentException(arg);
				}
			}
			
		}
		
		System.out.println("package names = " + commandMap.get(Command.PACKAGE_NAME));
		System.out.println("directory names = " + commandMap.get(Command.DIRECTORY_NAME));

	}

}
