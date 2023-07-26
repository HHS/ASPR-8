package gov.hhs.aspr.ms.taskit.core;

/**
 * Used as a {@link Translator} identifier
 */
public interface TranslatorId {
    /**
     * Implementationn consistent with equals()
     */
    @Override
    public int hashCode();

    /**
     * Two Translator ids are equal if and only if they represent the same
     * Translator.
     * Translator ids are generally implemented as static instances.
     */
    @Override
    public boolean equals(Object obj);
}
