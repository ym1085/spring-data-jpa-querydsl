package com.study.querydsl;

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
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.study.querydsl.entity.QMember.member;
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
        Team teamD = new Team("APP 개발 팀");
        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);
        em.persist(teamD);

        Member member1 = new Member("김영민", 30, teamA);
        Member member2 = new Member("원영식", 30, teamA);
        em.persist(member1);
        em.persist(member2);

        Member member3 = new Member("김진엽", 29, teamB);
        Member member4 = new Member("박진우", 29, teamB);
        Member member5 = new Member("임수현", 29, teamB);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);

        Member member6 = new Member("김주한", 19, teamD);
        em.persist(member6);
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
                        member.age.eq(30)
                )
                .fetchOne();

        assertThat(findMember.getUserName()).isEqualTo("김영민");
        assertThat(findMember.getAge()).isEqualTo(30);
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
}
