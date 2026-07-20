package com.vzap.trytons.service.admin;

import com.vzap.trytons.dao.admin.SystemReportDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.admin.SystemReportRequestDTO;
import com.vzap.trytons.dto.admin.SystemReportResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SystemReportServiceImpl implements SystemReportService {
    @Inject
    private SystemReportDAO systemReportDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public SystemReportResponseDTO generateReport(UUID actorUserId, SystemReportRequestDTO request) {
        // TODO: Confirm the actor is an active administrator, validate the request, gather the data for the requested SystemReportType, build and persist a SystemReport (recording parametersJson, resultJson and generatedByAdminUserId), then return it as a SystemReportResponseDTO.
    }

    @Override
    public List<SystemReportResponseDTO> listReports(UUID actorUserId) {
        // TODO: Confirm the actor is an active administrator, load all stored system reports via the DAO, and map them to a list of SystemReportResponseDTO.
    }

    @Override
    public SystemReportResponseDTO getReportById(UUID actorUserId, UUID reportId) {
        // TODO: Confirm the actor is an active administrator, load the system report by reportId (throwing ResourceNotFoundException when absent), and map it to a SystemReportResponseDTO.
    }
}
