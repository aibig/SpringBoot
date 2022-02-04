package hello.hellospring.repository;

import hello.hellospring.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcTemplateMemberRepository implements MemberRepository{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateMemberRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /*
    SimpleJdbcInsert는 삽입 작업을 간편하게 수행하도록 지원해주는 클래스다.
    DataSource 혹은 JdbcTemplate으로 SimpleJdbcInsert 객체를 생성할 수 있으며, 이 때 테이블 이름과 자동으로 생성되는 PK 칼럼명을 명시한다.
    auto_increment 옵션으로 생성된 값이 해당 칼럼명에 자동으로 입력된다.
    별도의 쿼리문 없이 파라미터들을 Map에 담아 넣어주면 Key값에 해당하는 칼럼명에 Value값을 삽입한다.
    executeAndReturnKey()는 작업 수행과 동시에 자동 생성된 PK(auto_increment)를 반환한다.
    모든 DB가 특정 Java 클래스를 반환할 것이라고 의존해서는 안 되며, 해당 메서드는 Number 클래스를 반환한다.
    이를 원하는 타입으로 적절히 변환해서 사용한다.
    자동으로 생성(증가)되는 칼럼이 여러 개이거나 숫자 형태가 아닌 경우, executeAndReturnKeyHolder()를 호출하여 KeyHolder를 반환받아 값을 원하는 형태로 적절히 변환한다.
     */

    @Override
    public Member save(Member member) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("member").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", member.getName());

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        member.setId(key.longValue());
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        List<Member> result = jdbcTemplate.query("select * from member where id = ?", memberRowMapper(),id);
        return result.stream().findAny();
    }

    @Override
    public Optional<Member> findByName(String name) {
        List<Member> result = jdbcTemplate.query("select * from member where name = ?", memberRowMapper(), name);
        return result.stream().findAny();
    }

    @Override
    public List<Member> findAll() {
        return jdbcTemplate.query("select * from member", memberRowMapper());
    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {

            Member member = new Member();
            member.setId(rs.getLong("id"));
            member.setName(rs.getString("name"));
            return member;
        };
    }
}
