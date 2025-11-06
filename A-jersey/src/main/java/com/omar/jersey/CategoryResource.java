package com.omar.jersey;

import com.omar.entities.Category;
import com.omar.entities.Item;
import com.omar.jersey.service.CategoryService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private final CategoryService service = new CategoryService();

    @GET
    public List<Category> list(@QueryParam("page") @DefaultValue("0") int page,
                               @QueryParam("size") @DefaultValue("20") int size) {
        return service.findAll(page, size);
    }

    @GET
    @Path("/{id}")
    public Category get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Category create(Category c) {
        return service.create(c);
    }

    @PUT
    @Path("/{id}")
    public Category update(@PathParam("id") Long id, Category c) {
        return service.update(id, c);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        service.delete(id);
    }

    // Relationnel : /categories/{id}/items?page=&size=
    @GET
    @Path("/{id}/items")
    public List<Item> itemsByCategory(@PathParam("id") Long id,
                                      @QueryParam("page") @DefaultValue("0") int page,
                                      @QueryParam("size") @DefaultValue("20") int size) {
        return service.findItemsByCategory(id, page, size);
    }
}
