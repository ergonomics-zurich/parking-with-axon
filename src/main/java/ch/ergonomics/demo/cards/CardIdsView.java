package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.CardIdsQuery;
import ch.ergonomics.demo.cards.api.CardIssuedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Component
public class CardIdsView {
    private final SortedSet<String> ids = new TreeSet<>();

    @QueryHandler
    public List<String> cardIds(CardIdsQuery query) {
        return List.copyOf(ids.subSet(query.getFromId(), query.getToId()));
    }

    @EventHandler
    public void on(CardIssuedEvent event) {
        ids.add(event.getUid());
    }

}
