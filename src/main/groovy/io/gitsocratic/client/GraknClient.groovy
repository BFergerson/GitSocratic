package io.gitsocratic.client

import grakn.core.concept.answer.ConceptMap
import graql.lang.Graql
import io.gitsocratic.command.config.ConfigOption

/**
 * Used to execute queries/questions on the Grakn knowledge graph.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class GraknClient {

    private final String host
    private final int port
    private final String keyspace
    private grakn.client.GraknClient client
    private grakn.client.GraknClient.Session session

    GraknClient() {
        if (Boolean.valueOf(ConfigOption.use_docker_grakn.value)) {
            this.host = ConfigOption.docker_host.value
        } else {
            this.host = ConfigOption.grakn_host.value
        }
        this.port = ConfigOption.grakn_port.value as int
        this.keyspace = ConfigOption.grakn_keyspace.value

        client = new grakn.client.GraknClient("$host:$port")
        session = client.session(keyspace)
    }

    GraknClient(String host, int port, String keyspace) {
        this.host = host
        this.port = port
        this.keyspace = keyspace

        client = new grakn.client.GraknClient("$host:$port")
        session = client.session(keyspace)
    }

    List<ConceptMap> executeReadQuery(String query) {
        def tx = makeReadSession()
        try {
            executeQuery(tx, query)
        } finally {
            tx.close()
        }
    }

    List<ConceptMap> executeWriteQuery(String query) {
        def tx = makeWriteSession()
        try {
            executeQuery(tx, query)
        } finally {
            tx.close()
        }
    }

    static List<ConceptMap> executeQuery(grakn.client.GraknClient.Transaction tx, String query) {
        return tx.execute(Graql.parse(query)) as List<ConceptMap>
    }

    grakn.client.GraknClient.Transaction makeWriteSession() {
        return session.transaction().write()
    }

    grakn.client.GraknClient.Transaction makeReadSession() {
        return session.transaction().read()
    }

    void resetKeyspace() {
        client.keyspaces().delete(keyspace)
        session.close()
        session = client.session(keyspace)
    }

    void close() {
        session.close()
        client.close()
    }
}
