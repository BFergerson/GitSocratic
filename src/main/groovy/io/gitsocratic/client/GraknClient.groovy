package io.gitsocratic.client

import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typedb.client.api.connection.TypeDBClient
import com.vaticle.typedb.client.api.connection.TypeDBSession
import com.vaticle.typedb.client.api.connection.TypeDBTransaction
import com.vaticle.typedb.client.connection.core.CoreClient
import com.vaticle.typeql.lang.query.TypeQLDefine
import com.vaticle.typeql.lang.query.TypeQLInsert
import com.vaticle.typeql.lang.query.TypeQLMatch
import com.vaticle.typeql.lang.query.TypeQLQuery
import groovy.util.logging.Slf4j
import io.gitsocratic.command.config.ConfigOption

import static com.vaticle.typeql.lang.TypeQL.parseQuery

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
    private TypeDBClient client
    private TypeDBSession session

    GraknClient() {
        if (Boolean.valueOf(ConfigOption.use_docker_grakn.value)) {
            this.host = ConfigOption.docker_host.value
        } else {
            this.host = ConfigOption.grakn_host.value
        }
        this.port = ConfigOption.grakn_port.value as int
        this.keyspace = ConfigOption.grakn_keyspace.value

        client = new CoreClient("$host:$port")
    }

    GraknClient(String host, int port, String keyspace) {
        this.host = host
        this.port = port
        this.keyspace = keyspace

        client = new CoreClient("$host:$port")
    }

    List<ConceptMap> executeQuery(TypeDBTransaction tx, String query) {
        return executeQuery(tx, parseQuery(query))
    }

    List<ConceptMap> executeQuery(String query) {
        return executeQuery(parseQuery(query))
    }

    List<ConceptMap> executeQuery(TypeQLQuery query) {
        try (def tx = makeWriteSession()) {
            return executeQuery(tx, query)
        }
    }

    List<ConceptMap> executeQuery(TypeDBTransaction tx, TypeQLQuery graqlQuery) {
        if (graqlQuery instanceof TypeQLMatch) {
            log.info("Executing match query")
            return tx.query().match(graqlQuery).collect() as List<ConceptMap>
        } else if (graqlQuery instanceof TypeQLInsert) {
            log.info("Executing insert query")
            return tx.query().insert(graqlQuery).collect() as List<ConceptMap>
        } else if (graqlQuery instanceof TypeQLDefine) {
            log.info("Executing define query")
            try (def schemaSession = client.session(keyspace, TypeDBSession.Type.SCHEMA)) {
                try (def writeTx = schemaSession.transaction(TypeDBTransaction.Type.WRITE)) {
                    writeTx.query().define(graqlQuery).collect()
                    writeTx.commit()
                    return []
                }
            }
        } else {
            throw new UnsupportedOperationException(graqlQuery.toString())
        }
    }

    TypeDBTransaction makeWriteSession() {
        if (session == null) session = client.session(keyspace, TypeDBSession.Type.DATA)
        return session.transaction(TypeDBTransaction.Type.WRITE)
    }

    TypeDBTransaction makeReadSession() {
        if (session == null) session = client.session(keyspace, TypeDBSession.Type.DATA)
        return session.transaction(TypeDBTransaction.Type.READ)
    }

    void resetKeyspace() {
        session?.close()
        session = null

        if (client.databases().contains(keyspace)) client.databases().get(keyspace).delete()
        client.databases().create(keyspace)
    }

    TypeDBClient getGraknClient() {
        return client
    }

    @Override
    void close() {
        session?.close()
        client.close()
    }
}
