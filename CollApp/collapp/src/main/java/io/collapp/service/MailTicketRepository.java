
package io.collapp.service;

import io.collapp.model.ProjectMailTicket;
import io.collapp.model.ProjectMailTicketConfig;
import io.collapp.model.ProjectMailTicketConfigData;
import io.collapp.query.MailTicketQuery;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional(readOnly = true)
public class MailTicketRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final MailTicketQuery queries;

    public MailTicketRepository(NamedParameterJdbcTemplate jdbc, MailTicketQuery queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public ProjectMailTicketConfig findConfig(final int id) {
        return queries.findConfigById(id);
    }

    public ProjectMailTicket findTicket(final int id) {
        return queries.findTicketById(id);
    }

    @Transactional(readOnly = false)
    public int addConfig(final String name, final int projectId, final ProjectMailTicketConfigData config, final String subject, final String body) {
        return queries.addConfig(name, projectId, config.toString(), subject, body);
    }

    public ProjectMailTicketConfig findLastCreatedConfig() {
        return queries.findLastCreatedConfig();
    }

    @Transactional(readOnly = false)
    public int updateConfig(final int id, final String name, final boolean enabled, final ProjectMailTicketConfigData config, final String subject, final String body, final int projectId) {
        return queries.updateConfig(id, name, enabled, config.toString(), subject, body, projectId);
    }

    @Transactional(readOnly = false)
    public int updateLastChecked(final int id, final Date date) {
        return queries.updateConfigLastChecked(id, date);
    }

    @Transactional(readOnly = false)
    public int deleteConfig(final int id, final int projectId) {
        return queries.deleteConfig(id, projectId);
    }

    @Transactional(readOnly = false)
    public int addTicket(final String name, final String alias, final boolean useAlias, final boolean overrideNotification, final String subject, final String body, final int columnId, final int configId, String metadata) {
        return queries.addTicket(name, alias, useAlias, overrideNotification, subject, body, columnId, configId, metadata);
    }

    public ProjectMailTicket findLastCreatedTicket() {
        return queries.findLastCreatedTicket();
    }

    @Transactional(readOnly = false)
    public int updateTicket(final int id, final String name, final boolean enabled, final String alias, final boolean useAlias, final boolean overrideNotification, final String subject, final String body, final int columnId, final int configId, String metadata) {
        return queries.updateTicket(id, name, enabled, alias, useAlias, overrideNotification, subject, body, columnId, configId, metadata);
    }

    @Transactional(readOnly = false)
    public int deleteTicket(final int id) {
        return queries.deleteTicket(id);
    }

    public List<ProjectMailTicketConfig> findAll() {
        return aggregateByConfig(queries.findAllConfigs(), queries.findAllTickets());
    }

    public List<ProjectMailTicketConfig> findAllByProject(final int projectId) {
        return aggregateByConfig(queries.findConfigsByProject(projectId), queries.findAllByProject(projectId));
    }

    private List<ProjectMailTicketConfig> aggregateByConfig(List<ProjectMailTicketConfig> configs, List<ProjectMailTicket> tickets) {
        Map<Integer, List<ProjectMailTicket>> ticketConfigsByConfigId = new HashMap<>();
        for(ProjectMailTicket ticketConfig: tickets) {
            if(!ticketConfigsByConfigId.containsKey(ticketConfig.getConfigId())) {
                ticketConfigsByConfigId.put(ticketConfig.getConfigId(), new ArrayList<ProjectMailTicket>());
            }

            ticketConfigsByConfigId.get(ticketConfig.getConfigId()).add(ticketConfig);
        }

        for(ProjectMailTicketConfig config: configs) {
            if(ticketConfigsByConfigId.containsKey(config.getId())) {
                config.getEntries().addAll(ticketConfigsByConfigId.get(config.getId()));
            }
        }

        return configs;
    }
}
