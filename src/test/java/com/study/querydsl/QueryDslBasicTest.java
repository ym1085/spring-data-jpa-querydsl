package com.study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("JPQL <> Querydsl 테스트")
public class QueryDslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("데이터 플랫폼 팀");
        Team teamB = new Team("인프라 팀");
        Team teamC = new Team("웹 개발 팀");
        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);

        Member member1 = new Member("김영민", 33, teamA);
        Member member2 = new Member("원영식", 30, teamA);
        em.persist(member1);
        em.persist(member2);

        Member member3 = new Member("김진엽", 27, teamB);
        Member member4 = new Member("박진우", 28, teamB);
        Member member5 = new Member("임수현", 29, teamB);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
    }

    @Test
    @DisplayName("단일 회원 검색 - JPQL 사용")
    public void findMemberByUserNameWithJPQL() throws Exception {
        String qlString =
                "select m from Member m " +
                "where m.userName = :userName";

        Member findMember = em.createQuery(qlString, Member.class)
                              .setParameter("userName", "임수현")
                              .getSingleResult();

        assertThat(findMember.getUserName()).isEqualTo("임수현");
    }

    @Test
    @DisplayName("단일 회원 검색 - Querydsl 사용")
    public void findMemberByUserNameWithQuerydsl() throws Exception {
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.userName.eq("임수현"))
                .fetchOne();

        assertThat(findMember.getUserName()).isEqualTo("임수현");
    }

    @Test
    @DisplayName("Querydsl에서 Q Class를 사용하는 방법 테스트")
    public void createQuerydslQueueClass() throws Exception {
        // 기본 인스턴스 사용
        QMember m1 = member;

        // QMember.member -> member ==> static import
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.userName.eq("김영민"))
                .fetchOne();

        assertThat(findMember.getUserName()).isEqualTo("김영민");
    }

    @Test
    @DisplayName("Querydsl의 where절에 사용되는 구문 테스트 - eq, ne, between")
    public void search() throws Exception {
        // 이름이 임수현이 아닌 모든 회원 조회
        List<Member> findMember_1 = queryFactory
                .selectFrom(member)
                .where(member.userName.ne("임수현"))
                .fetch(); // ne

        assertThat(findMember_1).hasSizeGreaterThan(0);

        // 이름이 임수현이고, 나이가 29인 단일 회원 조회
        Member findMember_2 = queryFactory
                .selectFrom(member)
                .where(member.userName.eq("임수현")
                                      .and(member.age.eq(29)))
                .fetchOne(); // eq

        assertThat(findMember_2.getUserName()).isEqualTo("임수현");

        // 나이가 20 ~ 29 사이에 존재하는 회원 조회
        List<Member> findMember_3 = queryFactory
                .select(member)
                .from(member)
                .where(member.age.between(20, 29))
                .fetch();

        assertThat(findMember_3).isNotEmpty();
        assertThat(findMember_3).hasSize(3); // expect) 3명의 회원

        for (Member member : findMember_3) {
            System.out.println(">>>>>>>> member.getUserName = " + member.getUserName() + ", age = " + member.getAge());
        }
    }

    @Test
    @DisplayName("Querydsl의 where절에 사용되는 구문 테스트 - and를 다른 방식으로 사용")
    public void searchAndParam() throws Exception {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.userName.eq("김영민"),
                        member.age.eq(33)
                )
                .fetchOne();

        assertThat(findMember.getUserName()).isEqualTo("김영민");
        assertThat(findMember.getAge()).isEqualTo(33);
    }

    @Test
    @DisplayName("결과 조회 테스트")
    public void resultFetchTest() throws Exception {
        /*List<Member> memberList = queryFactory
                .selectFrom(member)
                .fetch();

        Member findMember = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member firstMember = queryFactory
                .selectFrom(member)
                .fetchFirst();*/

        /*QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .fetchResults();

        result.getTotal();
        List<Member> results = result.getResults();*/

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    @Test
    @DisplayName("정렬 테스트 진행")
    public void test() throws Exception {
        // 회원 이름 순으로 내림차순, 나이 순으로 오름차순
        em.persist(new Member(null, 100));
        em.persist(new Member("김영민", 100));
        em.persist(new Member("정주리", 100));

        //querydsl 작성
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.asc(), member.userName.desc().nullsLast())
                .fetch();

        System.out.println(">>>> memberList = " + memberList.toString());

        Member member = memberList.get(0);
        Member member1 = memberList.get(1);
        Member member2 = memberList.get(2);
        assertThat(member.getUserName()).isEqualTo("정주리");
        assertThat(member1.getUserName()).isEqualTo("김영민");
        assertThat(member2.getUserName()).isNull();
    }

    @Test
    @DisplayName("일반 페이징 테스트 진행")
    public void paging() throws Exception {
        // Team 3개, Member 5명
        // Querydsl의 offset은 0부터 시작이 된다
        // offset: 시작 인덱스(row)
        // limit: 조회 개수
        List<Member> memberListByPaging = queryFactory // 김영민, 원영식, 임수현, 박진우, 김진엽
                .selectFrom(member)
                .orderBy(member.age.desc())
                .offset(0)
                .limit(5)
                .fetch();
        System.out.println(">>>> memberListByPaging = " + memberListByPaging.toString());

        assertThat(memberListByPaging.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Count 포함 페이징 테스트 진행")
    public void pagingWithCount() throws Exception {
        QueryResults<Member> queryResult = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc())
                .offset(0)
                .limit(10)
                .fetchResults();

        assertThat(queryResult.getResults().size()).isEqualTo(5); // 전체 회원 사이즈
        assertThat(queryResult.getLimit()).isEqualTo(10);
        assertThat(queryResult.getTotal()).isEqualTo(5);
        assertThat(queryResult.getOffset()).isEqualTo(0);
    }

    @Test
    @DisplayName("집합 테스트")
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min(),
                        member.age.sum()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(5);
        assertThat(tuple.get(member.age.avg())).isEqualTo(29.4);
        assertThat(tuple.get(member.age.max())).isEqualTo(33);
        assertThat(tuple.get(member.age.min())).isEqualTo(27);
        assertThat(tuple.get(member.age.sum())).isEqualTo(147);
    }

    @Test
    @DisplayName("groupBy 테스트")
    public void groupBy() throws Exception {
        List<Tuple> result = queryFactory
                .select(
                        team.name, member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("데이터 플랫폼 팀");
        assertThat(teamA.get(member.age.avg())).isEqualTo(31.5);

        assertThat(teamB.get(team.name)).isEqualTo("인프라 팀");
        assertThat(teamB.get(member.age.avg())).isEqualTo(28);
    }

    /**
     *  데이터 플랫폼 팀에 소속된 모든 회원을 조인하여 가져온다
     */
    @Test
    @DisplayName("조인 테스트")
    public void join() throws Exception {
        // INNER JOIN
        /*List<Member> memberList = queryFactory
                .selectFrom(member)
                .join(member.team, team) // QTeam
                .where(team.name.eq("데이터 플랫폼 팀"))
                .fetch();*/

        // LEFT JOIN
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team) // QTeam
                .where(team.name.eq("데이터 플랫폼 팀"))
                .fetch();

        for (Member member : memberList) {
            System.out.println("[TEST] member = " + member.toString());
        }

        assertThat(memberList)
                .extracting("userName")
                .containsExactly("김영민", "원영식");

        /*List<Team> teamList = queryFactory
                .selectFrom(team)
                .join(team.members, member)
                .where(team.name.eq("데이터 플랫폼 팀"))
                .fetch();

        for (Team team : teamList) {
            System.out.println("[TEST] team = " + team.toString());
        }*/
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    @DisplayName("세타 조인 - 연관관계가 없는 필드로 조인")
    public void theta_join() throws Exception {
        // 연관관계가 없는 경우 조인을 하는 방식
        em.persist(new Member("데이터 플랫폼 팀"));
        em.persist(new Member("인프라 팀"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.userName.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("userName")
                .containsExactly("데이터 플랫폼 팀", "인프라 팀");
    }

    /**
     * ex) 회원 팀을 조인하면서, 팀 이름이 '데이터 플랫폼 팀'인 팀만 조회, 회원은 모두 조회
     * JPQL
     *  - select m, t
     *      from Member m
     *      left join m.team t
     *      on t.name = 'teamA'
     */
    @Test
    @DisplayName("조인 - on 절 테스트")
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                //.where(team.name.eq("데이터 플랫폼 팀")) // 전체 데이터에서 필터링(조건에 맞는 데이터만 출력)
                .on(team.name.eq("데이터 플랫폼 팀")) // left join 의 조건이 되주어 null로 남는다
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @DisplayName("다양한 on 절 케이스 테스트")
    public void join_on_test() throws Exception {
        // -------------------------------------------------------------- //
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple  = " + tuple.toString());
        }

        List<Tuple> resultByOn = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("데이터 플랫폼 팀"))
                .fetch();

        for (Tuple tuple : resultByOn) {
            System.out.println("tuple  = " + tuple.toString());
        }

        List<Tuple> resultByWhere = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("데이터 플랫폼 팀"))
                .fetch();

        for (Tuple tuple : resultByWhere) {
            System.out.println("tuple  = " + tuple.toString());
        }
        System.out.println();

        // -------------------------------------------------------------- //
    }

    /**
     * 연관 관계가 없는 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상을 외부 조인해라
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("데이터 플랫폼 팀"));
        em.persist(new Member("인프라 팀"));

        // 연관 관계가 없는 경우
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(team) // member.team - x
                .on(member.userName.eq(team.name)) // 이름으로만 조인(필터링)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple.toString());
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("패치 조인 적용 안한 경우 테스트")
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.userName.eq("김영민"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    @DisplayName("패치 조인 적용 후 테스트")
    public void fetchJoin() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team).fetchJoin()
                .where(member.userName.eq("김영민"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    //------------------------------------------------------------------------------------//
    /**
     서브쿼리
        1. 나이가 가장 많은 회원 조회
        2. 나이가 평균 이상인 회원 조회
     */

    @Test
    @DisplayName("서브 쿼리 테스트 - 나이가 가장 많은 회원 조회")
    public void select_max_age_member() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        System.out.println(">>>> member = " + result);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAge()).isEqualTo(33);
        assertThat(result).extracting("age").containsExactly(33);
    }

    @Test
    @DisplayName("서브 쿼리 테스트 - 나이가 포함 되어 있는 회원 조회")
    public void select_age_in_member() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.goe(30)) // 30 이상, expect -> 2명
                ))
                .orderBy(member.age.desc())
                .fetch();

        System.out.println(">>> result = " + result);
        assertThat(result).extracting("age").containsExactly(33, 30);
    }

    @Test
    @DisplayName("select 절에 subquery 사용")
    public void select_subquery() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.userName,
                        select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .orderBy(member.age.desc())
                .fetch();

        System.out.println("result = " + result);
    }
}
