package io.gitsocratic.client

import ai.grakn.GraknSession
import ai.grakn.GraknTxType
import ai.grakn.Keyspace
import ai.grakn.client.Grakn
import ai.grakn.graql.answer.ConceptMap
import ai.grakn.util.SimpleURI
import io.gitsocratic.command.config.ConfigOption

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class GraknClient {

    private final String host
    private final int port
    private final String keyspace
    private final GraknSession session

    GraknClient() {
        if (Boolean.valueOf(ConfigOption.use_docker_grakn.value)) {
            this.host = ConfigOption.docker_host.value
        } else {
            this.host = ConfigOption.grakn_host.value
        }
        this.port = ConfigOption.grakn_port.value as int
        this.keyspace = ConfigOption.grakn_keyspace.value
        session = new Grakn(new SimpleURI("$host:$port")).session(Keyspace.of(keyspace))
    }

    GraknClient(String host, int port, String keyspace) {
        this.host = host
        this.port = port
        this.keyspace = keyspace
        session = new Grakn(new SimpleURI("$host:$port")).session(Keyspace.of(keyspace))
    }

    List<ConceptMap> executeQuery(String query) {
        def tx = session.transaction(GraknTxType.WRITE)
        try {
            def graql = tx.graql()
            return graql.parse(query).execute() as List<ConceptMap>
        } finally {
            tx.close()
        }
    }

    void close() {
        session.close()
    }
}
