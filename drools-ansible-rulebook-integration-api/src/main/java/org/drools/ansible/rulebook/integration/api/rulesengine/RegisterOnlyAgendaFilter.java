package org.drools.ansible.rulebook.integration.api.rulesengine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.core.common.InternalFactHandle;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;

public class RegisterOnlyAgendaFilter implements AgendaFilter {

    public static final String SYNTHETIC_RULE_TAG = "SYNTHETIC_RULE";

    private final RulesExecutorSession rulesExecutorSession;

    private final Set<Long> ephemeralFactHandleIds = ConcurrentHashMap.newKeySet();

    private final Set<Match> matchedRules = new LinkedHashSet<>();

    private final List<FactHandle> factsToBeDeleted = new ArrayList<>();

    public RegisterOnlyAgendaFilter(RulesExecutorSession rulesExecutorSession) {
        this.rulesExecutorSession = rulesExecutorSession;
    }

    public void registerephemeralFact(Long factId) {
        ephemeralFactHandleIds.add(factId);
    }

    @Override
    public boolean accept(Match match) {
        if ( match.getRule().getMetaData().get(SYNTHETIC_RULE_TAG) != null ) {
            return true;
        }
        matchedRules.add(match);
        if (!ephemeralFactHandleIds.isEmpty()) {
            for (FactHandle fh : match.getFactHandles()) {
                if (ephemeralFactHandleIds.remove(((InternalFactHandle) fh).getId())) {
                    factsToBeDeleted.add(fh);
                }
            }
        }
        return true;
    }

    public List<Match> finalizeAndGetResults() {
        factsToBeDeleted.forEach(rulesExecutorSession::delete);
        List<Match> matches = new ArrayList<>( matchedRules );
        matchedRules.clear();
        return matches;
    }
}