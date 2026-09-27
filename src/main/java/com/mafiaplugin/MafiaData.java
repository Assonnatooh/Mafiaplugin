package com.mafiaplugin;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class MafiaData {

    private final String displayName;
    private final UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();

    public MafiaData(String displayName, UUID leader) {
        this.displayName = displayName;
        this.leader = leader;
        this.members.add(leader);
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }
}
