package io.gitsocratic.client

import grakn.client.Grakn
import grakn.client.concept.answer.ConceptMap
import graql.lang.Graql
import graql.lang.query.GraqlQuery
import groovy.util.logging.Slf4j
import io.gitsocratic.command.config.ConfigOption

/**
 * Used to execute queries/questions on the Grakn knowledge graph.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
class GraknClient implements Closeable {

    private final String host
    private final int port
    private final String keyspace
    private grakn.client.GraknClient.Core client
    private Grakn.Session session

    GraknClient() {
        if (Boolean.valueOf(ConfigOption.use_docker_grakn.value)) {
            this.host = ConfigOption.docker_host.value
        } else {
            this.host = ConfigOption.grakn_host.value
        }
        this.port = ConfigOption.grakn_port.value as int
        this.keyspace = ConfigOption.grakn_keyspace.value

        client = grakn.client.GraknClient.core("$host:$port")
    }

    GraknClient(String host, int port, String keyspace) {
        this.host = host
        this.port = port
        this.keyspace = keyspace

        client = grakn.client.GraknClient.core("$host:$port")
    }

    List<ConceptMap> executeQuery(Grakn.Transaction tx, String query) {
        return executeQuery(tx, Graql.parseQuery(query))
    }

    List<ConceptMap> executeQuery(String query) {
        return executeQuery(Graql.parseQuery(query))
    }

    List<ConceptMap> executeQuery(GraqlQuery query) {
        try (def tx = makeWriteSession()) {
            return executeQuery(tx, query)
        }
    }

    List<ConceptMap> executeQuery(Grakn.Transaction tx, GraqlQuery graqlQuery) {
        if (graqlQuery.class.toString().contains("Match")) {
            log.info("Executing match query")
            return tx.query().match(graqlQuery).collect() as List<ConceptMap>
        } else if (graqlQuery.class.toString().contains("Insert")) {
            log.info("Executing insert query")
            return tx.query().insert(graqlQuery).collect() as List<ConceptMap>
        } else if (graqlQuery.class.toString().contains("GraqlDefine")) {
            log.info("Executing define query")
            try (def schemaSession = client.session(keyspace, Grakn.Session.Type.SCHEMA)) {
                try (def writeTx = schemaSession.transaction(Grakn.Transaction.Type.WRITE)) {
                    writeTx.query().define(graqlQuery).collect()
                    writeTx.commit()
                    return []
                }
            }
        } else {
            throw new UnsupportedOperationException(graqlQuery.toString())
        }
    }

    Grakn.Transaction makeWriteSession() {
        if (session == null) session = client.session(keyspace, Grakn.Session.Type.DATA)
        return session.transaction(Grakn.Transaction.Type.WRITE)
    }

    Grakn.Transaction makeReadSession() {
        if (session == null) session = client.session(keyspace, Grakn.Session.Type.DATA)
        return session.transaction(Grakn.Transaction.Type.READ)
    }

    void resetKeyspace() {
        session?.close()
        session = null

        if (client.databases().contains(keyspace)) client.databases().delete(keyspace)
        client.databases().create(keyspace)
    }

    grakn.client.GraknClient.Core getGraknClient() {
        return client
    }

    @Override
    void close() {
        session?.close()
        client.close()
    }
}
