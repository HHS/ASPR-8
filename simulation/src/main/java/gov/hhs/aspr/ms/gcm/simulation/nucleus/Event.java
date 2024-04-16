package gov.hhs.aspr.ms.gcm.simulation.nucleus;

/**
 * Represents an observable data change. Events can be produced by either actors
 * or data managers. Both actors and data mangers can subscribe to events using
 * various means. Data managers receive events immediately each time an event is
 * released to a simulation context. Actors receive events only after the
 * current actor and data managers have completed their current processing and
 * before the next plan is processed from the simulation's planning queue.
 */
public interface Event {

}
