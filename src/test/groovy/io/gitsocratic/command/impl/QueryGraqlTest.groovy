package io.gitsocratic.command.impl

import grakn.client.answer.Numeric
import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.command.SocraticCommandTest
import org.junit.Test

import static org.junit.Assert.assertEquals

class QueryGraqlTest extends SocraticCommandTest {

    @Test
    void "returns nothing query"() {
        def result = SocraticAPI.knowledge().graql()
                .query('match $x isa thing; get; limit 0;').build().execute()
        assertEquals(0, result.status)
        assertEquals(0, result.answer.size())
    }

    @Test
    void "returns numbers query"() {
        def result = SocraticAPI.knowledge().graql()
                .query('match $x isa thing; get; limit 0; count;').build().execute()
        assertEquals(0, result.status)
        assertEquals(1, result.answer.size())
        assertEquals(0, (result.answer.get(0) as Numeric).number().intValue())
    }
}
