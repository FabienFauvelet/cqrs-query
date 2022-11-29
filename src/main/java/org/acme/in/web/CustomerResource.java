package org.acme.in.web;

import org.acme.domain.services.CustomerService;
import org.acme.in.dto.CreateCustomerQuery;
import org.acme.in.dto.UpdateCustomerQuery;
import org.acme.in.mapper.CustomerInMapper;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;


@Path("/customer")
public class CustomerResource {
    @Inject
    CustomerService customerService;
    @Inject
    CustomerInMapper mapper;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCustomer(CreateCustomerQuery createCustomerQuery){
        customerService.addCustomer(mapper.toCustomer(createCustomerQuery));
        return Response.created(null).build();
    }
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCustomer(UpdateCustomerQuery updateCustomerQuery){
        return Response.ok().build();
    }

    @DELETE
    @Path("/{customerId}")
    public void deleteCustomer(@PathParam("customerId") UUID customerId){

    }
}