package com.vzap.trytons.resource.message;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.message.LeagueMessageResponseDTO;
import com.vzap.trytons.dto.message.MessageReportResponseDTO;
import com.vzap.trytons.dto.message.ReportMessageRequestDTO;
import com.vzap.trytons.dto.message.SendLeagueMessageRequestDTO;
import com.vzap.trytons.enums.MessageScope;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.message.LeagueMessageService;
import com.vzap.trytons.service.message.MessageReportService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
import java.util.UUID;

@RequestScoped
@Path("/leagues/{leagueId}/messages")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeagueMessageResource {

    @Inject
    private LeagueMessageService leagueMessageService;

    @Inject
    private MessageReportService messageReportService;

    @Context
    private ContainerRequestContext request;

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @GET
    public Response getFeed(@PathParam("leagueId") UUID leagueId,
                            @QueryParam("since") String since) {
        List<LeagueMessageResponseDTO> messages =
                leagueMessageService.getFeed(currentUserId(), leagueId, parseSince(since));
        return Response.ok(messages).build();
    }

    @POST
    public Response postMessage(@PathParam("leagueId") UUID leagueId,
                                SendLeagueMessageRequestDTO body) {
        LeagueMessageResponseDTO created = leagueMessageService.post(currentUserId(), leagueId, body);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/{messageId}/report")
    public Response reportMessage(@PathParam("leagueId") UUID leagueId,
                                  @PathParam("messageId") UUID messageId,
                                  ReportMessageRequestDTO body) {
        String reason = body == null ? null : body.getReason();
        MessageReportResponseDTO created =
                messageReportService.report(currentUserId(), MessageScope.LEAGUE, messageId, reason);
        return Response.status(Response.Status.CREATED).entity(created).build();
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
