package com.fulfilment.application.monolith.stores.events;

import com.fulfilment.application.monolith.stores.Store;

public class StoreEvent {

  public enum ActionType {
    CREATED,
    UPDATED
  }

  private final Store store;
  private final ActionType actionType;

  public StoreEvent(Store store, ActionType actionType) {
    this.store = store;
    this.actionType = actionType;
  }

  public Store getStore() {
    return store;
  }

  public ActionType getActionType() {
    return actionType;
  }
}
