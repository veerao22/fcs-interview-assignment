package com.fulfilment.application.monolith.fulfilment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfilment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfilmentResource {

  @Inject FulfilmentService fulfilmentService;

  /**
   * Assigns a warehouse to fulfil a product for a store. Idempotent if already assigned. Returns
   * 204 on success, 400 if constraints are violated.
   */
  @POST
  @Path("store/{storeId}/product/{productId}/warehouse/{warehouseId}")
  @Transactional
  public Response assign(
      @PathParam("storeId") Long storeId,
      @PathParam("productId") Long productId,
      @PathParam("warehouseId") Long warehouseId) {
    fulfilmentService.assign(storeId, productId, warehouseId);
    return Response.noContent().build();
  }

  /** Removes the assignment. Idempotent. Returns 204. */
  @DELETE
  @Path("store/{storeId}/product/{productId}/warehouse/{warehouseId}")
  @Transactional
  public Response unassign(
      @PathParam("storeId") Long storeId,
      @PathParam("productId") Long productId,
      @PathParam("warehouseId") Long warehouseId) {
    fulfilmentService.unassign(storeId, productId, warehouseId);
    return Response.noContent().build();
  }

  /**
   * Lists fulfilment assignments. Provide either storeId or warehouseId (or both). Returns list of
   * { id, storeId, productId, warehouseId }.
   */
  @GET
  public List<StoreProductFulfilment> list(
      @QueryParam("storeId") Long storeId, @QueryParam("warehouseId") Long warehouseId) {
    if (storeId != null) {
      return fulfilmentService.listByStore(storeId);
    }
    if (warehouseId != null) {
      return fulfilmentService.listByWarehouse(warehouseId);
    }
    return List.of();
  }
}
