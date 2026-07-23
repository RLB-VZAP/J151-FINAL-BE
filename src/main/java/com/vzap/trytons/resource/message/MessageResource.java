package com.vzap.trytons.resource.message;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.message.ConversationThreadDTO;
import com.vzap.trytons.dto.message.DirectMessageResponseDTO;
import com.vzap.trytons.dto.message.SendDirectMessageRequestDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.message.DirectMessageService;
import com.vzap.trytons.service.message.UserBlockService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestScoped
@Path("/messages")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    private DirectMessageService directMessageService;

    @Inject
    private UserBlockService userBlockService;

    @Context
    private ContainerRequestContext request;

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @POST
    @Path("/direct")
    public Response sendDirectMessage(SendDirectMessageRequestDTO body) {
        DirectMessageResponseDTO created = directMessageService.send(currentUserId(), body);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/threads")
    public Response listThreads() {
        List<ConversationThreadDTO> threads = directMessageService.getThreads(currentUserId());
        return Response.ok(threads).build();
    }

    @GET
    @Path("/direct/{userId}")
    public Response getConversation(@PathParam("userId") UUID userId,
                                    @QueryParam("since") String since) {
        List<DirectMessageResponseDTO> messages =
                directMessageService.getConversation(currentUserId(), userId, parseSince(since));
        return Response.ok(messages).build();
    }

    @PUT
    @Path("/direct/{userId}/read")
    public Response markThreadRead(@PathParam("userId") UUID userId) {
        int updated = directMessageService.markThreadRead(currentUserId(), userId);
        return Response.ok(Map.of("updatedCount", updated)).build();
    }

    @GET
    @Path("/unread-count")
    public Response getUnreadCount() {
        int count = directMessageService.getUnreadCount(currentUserId());
        return Response.ok(Map.of("unreadCount", count)).build();
    }

    @POST
    @Path("/block/{userId}")
    public Response blockUser(@PathParam("userId") UUID userId) {
        userBlockService.block(currentUserId(), userId);
        return Response.ok(Map.of("blocked", userId.toString())).build();
    }

    @DELETE
    @Path("/block/{userId}")
    public Response unblockUser(@PathParam("userId") UUID userId) {
        userBlockService.unblock(currentUserId(), userId);
        return Response.ok(Map.of("unblocked", userId.toString())).build();
    }

    @GET
    @Path("/blocked")
    public Response listBlocked() {
        return Response.ok(userBlockService.listBlocked(currentUserId())).build();
    }

    private LocalDateTime parseSince(String since) {
        if (since == null || since.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(since.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
