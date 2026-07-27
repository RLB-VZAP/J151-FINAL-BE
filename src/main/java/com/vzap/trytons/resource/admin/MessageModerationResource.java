package com.vzap.trytons.resource.admin;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.message.BlockedPhraseDTO;
import com.vzap.trytons.dto.message.DirectMessageResponseDTO;
import com.vzap.trytons.dto.message.LeagueMessageResponseDTO;
import com.vzap.trytons.dto.message.PendingLeagueMessageDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.message.BlockedPhraseService;
import com.vzap.trytons.service.message.DirectMessageService;
import com.vzap.trytons.service.message.LeagueMessageService;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestScoped
@Path("/admin/message-moderation")
@Authenticated
@AdminOnly
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageModerationResource {

    @Inject
    private LeagueMessageService leagueMessageService;

    @Inject
    private BlockedPhraseService blockedPhraseService;

    @Inject
    private DirectMessageService directMessageService;

    @Context
    private ContainerRequestContext request;

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @GET
    @Path("/pending")
    public Response listPending() {
        List<PendingLeagueMessageDTO> pending = leagueMessageService.listPending();
        return Response.ok(pending).build();
    }

    @PUT
    @Path("/{messageId}/approve")
    public Response approve(@PathParam("messageId") UUID messageId) {
        LeagueMessageResponseDTO approved = leagueMessageService.approve(currentUserId(), messageId);
        return Response.ok(approved).build();
    }

    @PUT
    @Path("/{messageId}/reject")
    public Response reject(@PathParam("messageId") UUID messageId) {
        leagueMessageService.reject(currentUserId(), messageId);
        return Response.ok(Map.of("messageId", messageId.toString(), "status", "REJECTED")).build();
    }

    @GET
    @Path("/direct/{messageId}")
    public Response getDirectMessageContext(@PathParam("messageId") UUID messageId) {
        List<DirectMessageResponseDTO> window = directMessageService.getAdminWindow(currentUserId(), messageId);
        return Response.ok(window).build();
    }

    @GET
    @Path("/blocklist")
    public Response listBlocklist() {
        return Response.ok(blockedPhraseService.list()).build();
    }

    @POST
    @Path("/blocklist")
    public Response addBlocklistPhrase(BlockedPhraseDTO body) {
        String phrase = body == null ? null : body.getPhrase();
        BlockedPhraseDTO created = blockedPhraseService.add(currentUserId(), phrase);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/blocklist/{blocklistId}")
    public Response removeBlocklistPhrase(@PathParam("blocklistId") UUID blocklistId) {
        blockedPhraseService.remove(blocklistId);
        return Response.noContent().build();
    }
}
