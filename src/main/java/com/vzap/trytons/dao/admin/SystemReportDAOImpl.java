package com.vzap.trytons.dao.admin;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.SystemReportType;
import com.vzap.trytons.model.admin.SystemReport;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class SystemReportDAOImpl extends BaseDAO implements SystemReportDAO {
    private static final Logger LOG = Logger.getLogger(SystemReportDAOImpl.class.getName());

    @Override
    public SystemReport save(SystemReport systemReport) {
        // TODO: Insert the given system report into the systemReport table (reportId, reportType, reportTitle, parametersJson, resultJson, generatedAt, generated_by_admin_user_id) and return the persisted report.
    }

    @Override
    public Optional<SystemReport> findById(UUID reportId) {
        // TODO: Load a single system report from the systemReport table by its reportId and map it to a SystemReport model, or return Optional.empty() when no row exists.
    }

    @Override
    public List<SystemReport> findAll() {
        // TODO: Read every row from the systemReport table and return them as a list of SystemReport models, most recent first.
    }

    @Override
    public List<SystemReport> findByType(SystemReportType reportType) {
        // TODO: Read the rows from the systemReport table whose reportType matches the given type and return them as a list of SystemReport models.
    }
}
