package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.util.errors.ContractException;

public abstract class DimensionData {
    protected final List<String> levelNames;
    private final Map<String, Integer> levelNameToLevelMap = new LinkedHashMap<>();
    protected final int levelCount;

    /** 
     * The passed in list of levelNames is assumed to be a list of unique names. This constructor does NO validation. It is up to the class implementing this class to do the validation beforehand.
     */
    protected DimensionData(List<String> levelNames) {
        this.levelCount = levelNames.size();

        int counter = 0;
        for(String levelName : levelNames) {
            this.levelNameToLevelMap.put(levelName, counter++);
        }

        this.levelNames = new ArrayList<>(levelNames);
    }

    /**
     * returns the number of levels
     */
    public final int getLevelCount() {
        return this.levelCount;
    }

    /**
     * returns the level name for the given int level
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain NucleusError#INVALID_DIMENSION_LEVEL}
     *                           if the int level is less than 0 or greater than the
     *                           number of levels</li>
     *                           </ul>
     */
    public final String getLevelName(int level) {
        if (level < 0 || level > this.getLevelCount()) {
            throw new ContractException(NucleusError.INVALID_DIMENSION_LEVEL, level);
        }

        return this.levelNames.get(level);
    }

    /**
     * returns the list of level names
     */
    public final List<String> getLevelNames() {
        return Collections.unmodifiableList(levelNames);
    }

    /**
     * returns the int level associated with the given levelName
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain NucleusError#UNKNOWN_DIMENSION_LEVEL_NAME}
     *                           if the levelName does not exist</li>
     *                           </ul>
     */
    public final int getLevel(String levelName) {
        Integer level = this.levelNameToLevelMap.get(levelName);

        if (level != null) {
            return level.intValue();
        }

        throw new ContractException(NucleusError.UNKNOWN_DIMENSION_LEVEL_NAME, levelName);
    }

    public abstract int hashCode();

    public abstract boolean equals(Object obj);

    public abstract String toString();
}
