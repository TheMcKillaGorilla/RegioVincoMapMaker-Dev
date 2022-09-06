package rvmm.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LeaderPrototype {
    public static final String SUBREGION_NAME = "subregionName";
    public static final String CURRENT_LEADER = "currentLeader";
    public static final String FOUND_LEADER = "foundLeader";
    public static final String UPDATE_LEADER = "updateLeader";
    StringProperty subregionName;
    StringProperty currentLeader;
    StringProperty foundLeader;
    BooleanProperty updateLeader;
    
    public LeaderPrototype() {
        subregionName = new SimpleStringProperty("");
        currentLeader = new SimpleStringProperty("");
        foundLeader = new SimpleStringProperty("");
        updateLeader = new SimpleBooleanProperty(false);
    }
    
    public LeaderPrototype(String initSubregionName,
                            String initCurrentLeader,
                            String initFoundLeader,
                            boolean initUpdateLeader) {
        this();
        subregionName.set(initSubregionName);
        currentLeader.set(initCurrentLeader);
        foundLeader.set(initFoundLeader);
        updateLeader.set(initUpdateLeader);
    }

    public String getSubregionName() {
        return subregionName.get();
    }

    public void setSubregionName(String value) {
        subregionName.set(value);
    }

    public StringProperty subregionNameProperty() {
        return subregionName;
    }

    public String getCurrentLeader() {
        return currentLeader.get();
    }

    public void setCurrentLeader(String value) {
        currentLeader.set(value);
    }

    public StringProperty currentLeaderProperty() {
        return currentLeader;
    }

    public String getFoundLeader() {
        return foundLeader.get();
    }

    public void setFoundLeader(String value) {
        foundLeader.set(value);
    }

    public StringProperty foundLeaderProperty() {
        return foundLeader;
    }

    public boolean getUpdateLeader() {
        return updateLeader.get();
    }

    public void setUpdateLeader(boolean value) {
        updateLeader.set(value);
    }

    public BooleanProperty updateLeaderProperty() {
        return updateLeader;
    }
    
    public String toString() {
        String desc = this.getSubregionName() + ", "
                + this.getCurrentLeader() + ", "
                + this.getFoundLeader() + ", "
                + this.getUpdateLeader();
        return desc;
    }
}
