package com.omar.jersey;

import com.omar.entities.Item;
import com.omar.jersey.service.ItemService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    private final ItemService service = new ItemService();

    @GET
    public List<Item> list(@QueryParam("categoryId") Long categoryId,
                           @QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("20") int size) {
        if (categoryId != null) {
            return service.findByCategory(categoryId, page, size);
        }
        return service.findAll(page, size);
    }

    @GET
    @Path("/{id}")
    public Item get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Item create(Item item) {
        return service.create(item);
    }

    @PUT
    @Path("/{id}")
    public Item update(@PathParam("id") Long id, Item item) {
        return service.update(id, item);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        service.delete(id);
    }
}
