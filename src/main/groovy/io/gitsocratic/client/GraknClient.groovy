package io.gitsocratic.client

import grakn.client.Grakn
import grakn.client.concept.answer.ConceptMap
import graql.lang.Graql
import graql.lang.query.GraqlInsert
import graql.lang.query.GraqlMatch
import io.gitsocratic.command.config.ConfigOption

/**
 * Used to execute queries/questions on the Grakn knowledge graph.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class GraknClient {

    private final String host
    private final int port
    private final String keyspace
    private grakn.client.GraknClient client
    private Grakn.Session session

    GraknClient() {
        if (Boolean.valueOf(ConfigOption.use_docker_grakn.value)) {
            this.host = ConfigOption.docker_host.value
        } else {
            this.host = ConfigOption.grakn_host.value
        }
        this.port = ConfigOption.grakn_port.value as int
        this.keyspace = ConfigOption.grakn_keyspace.value

        client = new grakn.client.GraknClient("$host:$port")
        if (client.databases().contains(keyspace)) client.databases().delete(keyspace) //todo: remove
        client.databases().create(keyspace) //todo: remove
        session = client.session(keyspace, Grakn.Session.Type.DATA)
    }

    GraknClient(String host, int port, String keyspace) {
        this.host = host
        this.port = port
        this.keyspace = keyspace

        client = new grakn.client.GraknClient("$host:$port")
        session = client.session(keyspace, Grakn.Session.Type.DATA)
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

    static List<ConceptMap> executeQuery(Grakn.Transaction tx, String query) {
        def graqlQuery = Graql.parseQuery(query)
        if (graqlQuery.class.toString().contains("Match")) {
            return tx.query().match(graqlQuery).collect() as List<ConceptMap>
        } else if (graqlQuery.class.toString().contains("Insert")) {
            return tx.query().insert(graqlQuery).collect() as List<ConceptMap>
        } else {
            throw new UnsupportedOperationException(graqlQuery.toString())
        }
    }

    Grakn.Transaction makeWriteSession() {
        return session.transaction(Grakn.Transaction.Type.WRITE)
    }

    Grakn.Transaction makeReadSession() {
        return session.transaction(Grakn.Transaction.Type.READ)
    }

    void resetKeyspace() {
        client.databases().delete(keyspace)
        client.databases().create(keyspace)
        session = client.session(keyspace, Grakn.Session.Type.DATA)
    }

    void close() {
        session.close()
        client.close()
    }
}
