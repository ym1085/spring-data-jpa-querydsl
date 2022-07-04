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
    public void createQuerydslQueueClass() {
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
}
