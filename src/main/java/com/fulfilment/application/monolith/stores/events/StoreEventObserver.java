package com.fulfilment.application.monolith.stores.events;

import com.fulfilment.application.monolith.stores.LegacyStoreManagerGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StoreEventObserver {

  private static final Logger LOGGER = Logger.getLogger(StoreEventObserver.class.getName());

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  public void onStoreChanged(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreEvent event) {
    LOGGER.infov(
        "Store event received after transaction commit: action={0}, store={1}",
        event.getActionType(), event.getStore().name);
    switch (event.getActionType()) {
      case CREATED -> {
        LOGGER.infov("Propagating store creation to legacy system: {0}", event.getStore().name);
        legacyStoreManagerGateway.createStoreOnLegacySystem(event.getStore());
      }
      case UPDATED -> {
        LOGGER.infov("Propagating store update to legacy system: {0}", event.getStore().name);
        legacyStoreManagerGateway.updateStoreOnLegacySystem(event.getStore());
      }
    }
  }
}
