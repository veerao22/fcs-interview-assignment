package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.events.StoreEvent;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject Event<StoreEvent> storeEvent;

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException(
          Response.status(404).entity("Store with id of " + id + " does not exist.").build());
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException(
          Response.status(422).entity("Id was invalidly set on request.").build());
    }
    if (store.name == null || store.name.isBlank()) {
      throw new WebApplicationException(
          Response.status(422).entity("Store name is required.").build());
    }
    if (Store.find("name", store.name).firstResult() != null) {
      throw new WebApplicationException(
          Response.status(409)
              .entity("Store with name '" + store.name + "' already exists.")
              .build());
    }

    store.persist();

    storeEvent.fire(new StoreEvent(store, StoreEvent.ActionType.CREATED));

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(@PathParam("id") Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException(
          Response.status(422).entity("Store Name was not set on request.").build());
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException(
          Response.status(404).entity("Store with id of " + id + " does not exist.").build());
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    storeEvent.fire(new StoreEvent(entity, StoreEvent.ActionType.UPDATED));

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(@PathParam("id") Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException(
          Response.status(422).entity("Store Name was not set on request.").build());
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException(
          Response.status(404).entity("Store with id of " + id + " does not exist.").build());
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    storeEvent.fire(new StoreEvent(entity, StoreEvent.ActionType.UPDATED));

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException(
          Response.status(404).entity("Store with id of " + id + " does not exist.").build());
    }
    entity.delete();
    return Response.status(204).build();
  }
}
