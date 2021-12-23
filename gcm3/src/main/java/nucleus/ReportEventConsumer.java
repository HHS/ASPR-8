package nucleus;

/**
 * 
 * Defines the handling of an event by a report
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
public interface ReportEventConsumer<T extends Event> {
	/**
	 * Instructs a report to handle the given event
	 */
	public void handleEvent(ReportContext reportContext, T event);
}
